package block;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用nio实现阻塞方式EchoServer
 * 
 * @author ruiyao.shen
 *
 */
public class EchoServer {
	private int port = 8000;
	private ServerSocketChannel serverSocketChannel = null;
	private ExecutorService executorService;// 线程池
	private static final int POOL_SIZE = 4;// 线程池中的工作线程数目

	public EchoServer() throws IOException {
		// 创建一个线程池
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		// 创建一个ServerSocketChannel对象
		serverSocketChannel = ServerSocketChannel.open();
		// 保证关闭服务器程序后可以马上再次启动服务器程序
		serverSocketChannel.socket().setReuseAddress(true);
		// 把服务器进程与一个本地端口绑定
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		System.out.println("服务器启动");
	}

	public void service() {
		while (true) {
			SocketChannel socketChannel = null;
			try {
				socketChannel = serverSocketChannel.accept();
				executorService.execute(new Handler(socketChannel));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new EchoServer().service();
	}

	class Handler implements Runnable {
		private SocketChannel socketChannel;

		public Handler(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		@Override
		public void run() {
			handle(socketChannel);
		}

		public void handle(SocketChannel socketChannel) {
			try {
				Socket socket = socketChannel.socket();
				System.out.println("接收到客户连接，来自：" + socket.getInetAddress() + ":" + socket.getPort());
				BufferedReader br = getReader(socket);
				PrintWriter pw = getWriter(socket);

				String msg = null;
				while ((msg = br.readLine()) != null) {
					System.out.println(msg);
					pw.println(echo(msg));
					if ("bye".equals(msg)) {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socketChannel != null)
						socketChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		private String echo(String msg) {
			return "echo:" + msg;
		}

		private PrintWriter getWriter(Socket socket) throws IOException {
			OutputStream os = socket.getOutputStream();
			return new PrintWriter(new OutputStreamWriter(os));
		}

		private BufferedReader getReader(Socket socket) throws IOException {
			InputStream is = socket.getInputStream();
			return new BufferedReader(new InputStreamReader(is));
		}
	}
}
