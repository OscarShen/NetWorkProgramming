package nonblock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 非阻塞式的EchoClient
 * 
 * @author Oscar
 *
 */
public class EchoClient {
	private SocketChannel socketChannel = null;
	private ByteBuffer sendBuffer = ByteBuffer.allocateDirect(1024);
	private ByteBuffer receiveBuffer = ByteBuffer.allocateDirect(1024);
	private Charset charset = Charset.forName("UTF8");
	private Selector selector;

	public EchoClient() throws IOException {
		socketChannel = SocketChannel.open();
		InetAddress ia = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(ia, 8000);
		socketChannel.connect(isa);
		socketChannel.configureBlocking(false);
		System.out.println("与服务器的连接建立成功！");
		selector = Selector.open();
	}

	public static void main(String[] args) throws IOException {
		final EchoClient client = new EchoClient();
		Thread receiver = new Thread() {
			@Override
			public void run() {
				client.receiveFromUser();// 接收用户向控制台输入的数据
			}
		};
		receiver.start();
		client.talk();
	}

	public void receiveFromUser() {// 接收用户从控制台的数据，把它放到sendBuffer中
		try {
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				synchronized (sendBuffer) {
					sendBuffer.put(encode(msg + "\r\n"));
				}
				if ("bye".equals(msg)) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void talk() throws IOException {// 接收和发送数据
		socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		while (selector.select() > 0) {
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> it = readyKeys.iterator();
			while (it.hasNext()) {
				SelectionKey key = null;
				try {
					key = (SelectionKey) it.next();
					it.remove();

					if (key.isReadable()) {
						receive(key);
					}
					if (key.isWritable()) {
						send(key);
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

	/**
	 * 接收数据
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void receive(SelectionKey key) throws IOException {
		// 接收EchoServer发送的数据，把它放到receiveBuffer中
		// 如果receiveBuffer中有一行数据，就打印这行数据，然后把它从receiveBuffer中删除
		SocketChannel socketChannel = (SocketChannel) key.channel();
		socketChannel.read(receiveBuffer);
		receiveBuffer.flip();
		String receiveData = decode(receiveBuffer);
		if (receiveData.indexOf("\n") == -1) {
			return;
		}

		String outputData = receiveData.substring(0, receiveData.indexOf("\n") + 1);
		System.out.println(outputData);
		if ("echo:bye\r\n".equals(outputData)) {
			key.cancel();
			socketChannel.close();
			System.out.println("关闭与服务器的连接");
			selector.close();
			System.exit(0);
		}

		ByteBuffer temp = encode(outputData);
		receiveBuffer.position(temp.limit());
		receiveBuffer.compact();
	}

	/**
	 * 发送数据
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void send(SelectionKey key) throws IOException {
		// 发送sendBuffer中的数据
		SocketChannel socketChannel = (SocketChannel) key.channel();
		synchronized (sendBuffer) {
			sendBuffer.flip();// 把极限设为位置，把位置设为零
			socketChannel.write(sendBuffer);// 发送数据
			sendBuffer.compact();// 删除已经发送的数据
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
