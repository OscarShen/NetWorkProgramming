package multiThread4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 增加关闭功能的echoserver
 * 
 * @author ruiyao.shen
 *
 */
public class EchoServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	private ExecutorService executorService;// 线程池
	private final int POOL_SIZE = 4;// 单个CPU时线程池中工作线程的数目

	private int portForShutdown = 8001;
	private ServerSocket serverSocketForShutdown;
	private boolean isShutdown = false;

	private Thread shutdownThread = new Thread() {// 负责关闭服务器的线程
		public void start() {
			this.setDaemon(true);// 后台线程
			super.start();
		};

		public void run() {
			while (!isShutdown) {
				Socket socketForShutdown = null;
				try {
					socketForShutdown = serverSocketForShutdown.accept();
					BufferedReader br = new BufferedReader(new InputStreamReader(socketForShutdown.getInputStream()));
					String command = br.readLine();
					if ("shutdown".equals(command)) {
						long beginTime = System.currentTimeMillis();
						socketForShutdown.getOutputStream().write("服务器正在关闭\r\n".getBytes());
						isShutdown = true;
						// 请求关闭线程池
						// 线程池不再接受新任务，但会继续执行完现有任务
						executorService.shutdown();

						// 等待关闭线程池，每次等待30秒
						while (!executorService.isTerminated()) {
							executorService.awaitTermination(30, TimeUnit.SECONDS);
						}

						serverSocket.close();// 关闭EchoClient客户通信的ServerSocket
						long endTime = System.currentTimeMillis();
						socketForShutdown.getOutputStream()
								.write(("服务器已经关闭，关闭服务器用了" + (endTime - beginTime) + "毫秒\r\n").getBytes());
						socketForShutdown.close();
						serverSocketForShutdown.close();
					} else {
						socketForShutdown.getOutputStream().write("错误的命令\r\n".getBytes());
						socketForShutdown.close();
					}
				} catch (IOException e) {
				} catch (InterruptedException e) {
				} finally {
					System.out.println("服务器已经关闭");
				}
			}
		};
	};

	public EchoServer() throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(6000);
		serverSocketForShutdown = new ServerSocket(portForShutdown);
		// 创建线程池
		// Runtime的availableProcessors()方法返回当前系统的CPU数目
		// 系统的CPU越多，线程池中工作线程的数目也越多
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		shutdownThread.start();
		System.out.println("服务器启动！");
	}

	public void service() {
		while (!isShutdown) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				socket.setSoTimeout(6000);
				executorService.execute(new Handler(socket));
			} catch (SocketTimeoutException e) {
			} catch (RejectedExecutionException e) {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (SocketException e) {
				if (e.getMessage().indexOf("socket closed") != -1) {
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws IOException {
		new EchoServer().service();
	}

	class Handler implements Runnable {
		private Socket socket;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		private PrintWriter getWriter(Socket socket) throws IOException {
			OutputStream os = socket.getOutputStream();
			return new PrintWriter(os, true);
		}

		private BufferedReader getReader(Socket socket) throws IOException {
			InputStream is = socket.getInputStream();
			return new BufferedReader(new InputStreamReader(is));
		}

		public String echo(String msg) {
			return "echo:" + msg;
		}

		@Override
		public void run() {
			try {
				System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
				BufferedReader br = getReader(socket);
				PrintWriter pw = getWriter(socket);

				String msg = null;
				while (null != (msg = br.readLine())) {
					System.out.println(msg);
					String s = echo(msg);
					pw.println(s);
					if ("bye".equals(msg)) {
						break;
					}
				}
			} catch (SocketTimeoutException e) {

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
