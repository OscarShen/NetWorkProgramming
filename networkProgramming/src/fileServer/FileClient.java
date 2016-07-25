package fileServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class FileClient {
	private SocketChannel socketChannel = null;
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer reqBuffer = ByteBuffer.allocate(64);
	private Selector selector;
	private Charset charset = Charset.forName("UTF8");

	private static final String FILE_PATH = "D:/my/FileClient/";

	public FileClient() throws IOException {
		socketChannel = SocketChannel.open();
		InetAddress ia = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(ia, 8000);
		socketChannel.connect(isa);
		socketChannel.configureBlocking(false);
		selector = Selector.open();
		System.out.println("与服务器建立连接成功");
	}

	public static void main(String[] args) throws IOException {
		new FileClient().service();
	}

	private void service() {
		try {
			while (true) {
				String msg = receiveFromUser();
				if (msg == null || msg.trim().equals("")) {
					continue;
				}
				reqBuffer.put(encode(msg + "\r\n"));
				String req = msg.substring(0, 3);
				String url = msg.substring(4);
				if (!(req.equals("get") || req.equals("put"))) {
					System.out.println("输入有误，请重新输入");
					continue;
				}

				socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				while (selector.select() > 0) {
					Set<SelectionKey> readyKey = selector.selectedKeys();
					Iterator<SelectionKey> it = readyKey.iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						it.remove();
						if (req.equals("get")) {
							sendReq(key);
							receiveFile(key, url);
						} else if (req.equals("put")) {
							sendReq(key);
							sendFile(key, url);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendReq(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		reqBuffer.flip();
		socketChannel.write(reqBuffer);
		reqBuffer.compact();
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

	private String receiveFromUser() throws IOException {
		BufferedReader bw = new BufferedReader(new InputStreamReader(System.in));
		String msg = bw.readLine();
		return msg;
	}

	private ByteBuffer encode(String msg) {
		return charset.encode(msg);
	}
}
