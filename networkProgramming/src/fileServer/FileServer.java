package fileServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class FileServer {
	private ServerSocketChannel serverSocketChannel = null;
	private Selector selector = null;
	private Charset charset = Charset.forName("UTF8");
	private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
	private static final String FILE_PATH = "D:/my/FileServer/";

	public FileServer() throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		selector = Selector.open();
		serverSocketChannel.socket().setReuseAddress(true);
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(8000));
		System.out.println("服务器启动");
	}

	public static void main(String[] args) throws IOException {
		new FileServer().service();
	}

	public void service() {
		try {
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			while (selector.select() > 0) {
				Set<SelectionKey> readyKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = readyKeys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isAcceptable()) {
						ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
						SocketChannel socketChannel = ssc.accept();
						System.out.println("收到客户连接，来自：" + socketChannel.socket().getInetAddress() + ":"
								+ socketChannel.socket().getPort());
						socketChannel.configureBlocking(false);
						socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					}
					if (key.isReadable() || key.isWritable()) {
						SocketChannel socketChannel = (SocketChannel) key.channel();
						receiveBuffer.clear();
						while (receiveBuffer.position() < 64) {
							socketChannel.read(receiveBuffer);
						}
						String msg = decode(receiveBuffer);
						int n = msg.indexOf("\r\n");
						if (n == -1) {
							return;
						}
						String req = msg.substring(0, 3);
						String url = msg.substring(4, n);
						if (!(req.equals("get") || req.equals("put"))) {
							System.out.println("输入有误，请重新输入");
							continue;
						}
						if (req.equals("get")) {
							sendFile(key, url);
						} else if (req.equals("put")) {
							receiveFile(key, url);
						}
					}
				}
			}
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendFile(SelectionKey key, String url) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		FileInputStream fis = new FileInputStream(new File(FILE_PATH + url));
		byte[] b = new byte[1024];
		int len = 0;
		sendBuffer.clear();
		while ((len = fis.read(b)) != -1) {
			sendBuffer.put(b, 0, len);
			while (sendBuffer.hasRemaining()) {
				sendBuffer.flip();
				socketChannel.write(sendBuffer);
			}
			sendBuffer.compact();
		}
		fis.close();
	}

	private void receiveFile(SelectionKey key, String url) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		FileOutputStream fos = new FileOutputStream(new File(FILE_PATH + url));
		byte[] b = new byte[1024];
		int len = 0;
		receiveBuffer.clear();
		while ((len = socketChannel.read(receiveBuffer)) != -1) {
			receiveBuffer.flip();
			receiveBuffer.get(b, 0, len);
			fos.write(b, 0, len);
			receiveBuffer.compact();
		}
		fos.close();
	}

	private String decode(ByteBuffer buffer) {
		return charset.decode(buffer).toString();
	}
}
