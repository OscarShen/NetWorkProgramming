package multiThread3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import multiThread2.ThreadPool;

public class EchoServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	private ThreadPool threadPool;// 线程池
	private final int POOL_SIZE = 4;// 单个CPU时线程池中工作线程的数目

	public EchoServer() throws IOException {
		serverSocket = new ServerSocket(port);
		// 创建线程池
		// Runtime的availableProcessors()方法返回当前系统的CPU数目
		// 系统的CPU越多，线程池中工作线程的数目也越多
		threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		System.out.println("服务器启动！");
	}

	public void service() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				threadPool.execute(new Handler(socket));
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
