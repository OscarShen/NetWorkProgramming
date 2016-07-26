package httpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {
	private int port = 8000;
	private ServerSocketChannel serverSocketChannel = null;
	private ExecutorService executorService;
	private static final int POOL_MULTIPLE = 4;
	private Charset charset = Charset.forName("UTF8");

	public SimpleHttpServer() throws IOException {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().setReuseAddress(true);
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		System.out.println("服务器启动");
	}

	public static void main(String[] args) throws IOException {
		new SimpleHttpServer().service();
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

	class Handler implements Runnable {
		private SocketChannel socketChannel;

		public Handler(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		@Override
		public void run() {
			handle(socketChannel);
		}

		private void handle(SocketChannel socketChannel) {
			try {
				Socket socket = socketChannel.socket();
				System.out.println("接收到客户连接，来自：" + socket.getInetAddress() + "：" + socket.getPort());
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				socketChannel.read(buffer);
				buffer.flip();
				String request = decode(buffer);
				System.out.println(request);

				StringBuffer sb = new StringBuffer("HTTP/1.1 200 OK\r\n");
				sb.append("Content-type:text/html;charset=UTF8\r\n\r\n");
				socketChannel.write(encode(sb.toString()));

				FileInputStream in;
				// 生成HTTP请求的第一行
				String firstLine = request.substring(0, request.indexOf("\r\n"));
				if (firstLine.indexOf("login.html") != -1) {
					in = new FileInputStream("login.html");
				} else {
					in = new FileInputStream("hello.html");
				}

				FileChannel fileChannel = in.getChannel();
				fileChannel.transferTo(0, fileChannel.size(), socketChannel);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socketChannel != null) {
						socketChannel.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private ByteBuffer encode(String string) {
			return charset.encode(string);
		}

		private String decode(ByteBuffer buffer) {
			CharBuffer charBuffer = charset.decode(buffer);
			return charBuffer.toString();
		}

	}
}
