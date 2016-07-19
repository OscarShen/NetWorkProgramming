package simpleFTPServer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class FileClient {
	private SocketChannel socketChannel = null;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(102400);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(102400);
	private Charset charset = Charset.forName("UTF8");
	private Selector selector;
	String root = "D:/my/FileClient";// 我本地的目录
	String url = null;

	public FileClient() throws IOException {
		socketChannel = SocketChannel.open();
		InetAddress ia = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(ia, 8000);
		socketChannel.connect(isa);
		socketChannel.configureBlocking(false);
		System.out.println("与服务器连接成功！");
		selector = Selector.open();
	}

	/**
	 * 接收从控制台输入的指令
	 */
	public void receiveFromUser() {
		try {
			BufferedReader locanReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = locanReader.readLine()) != null) {
				String[] s = msg.split(" ");
				if ("GET".equals(s[0]) || "PUT".equals(s[0])) {
					url = s[1];
				} else if ("bye".equals(msg)) {
					break;
				} else {
					System.out.println("请求有误，请重新输入！");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用于发送和接收文件
	 * 
	 * @throws IOException
	 */
	public void talk() throws IOException {
		socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		while (selector.select() > 0) {
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = readyKeys.iterator();
			while (it.hasNext()) {
				SelectionKey key = null;
				try {
					key = it.next();
					it.remove();
					if (key.isReadable()) {
						receiveFile(key);
					}
					if (key.isWritable()) {
						sendFile(key);
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (key != null) {
						key.cancel();
						key.channel().close();
					}
				}
			}
		}
	}

	private void sendFile(SelectionKey key) {

	}

	private void receiveFile(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Socket socket = socketChannel.socket();
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		FileOutputStream fos = new FileOutputStream(new File(root + url));
		byte[] b = new byte[1024];
		int len;
		while (-1 != (len = bis.read(b))) {
			fos.write(b, 0, len);
		}
		fos.flush();
		fos.close();
		bis.close();
	}

	private ByteBuffer encode(String msg) {
		return charset.encode(msg);
	}
}
