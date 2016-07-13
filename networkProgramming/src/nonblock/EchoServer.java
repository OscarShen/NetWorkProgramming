package nonblock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 使用nio实现非阻塞方式EchoServer
 * 
 * @author ruiyao.shen
 *
 */
public class EchoServer {
	private int port = 8000;
	private ServerSocketChannel serverSocketChannel = null;
	private Selector selector = null;
	private Charset charset = Charset.forName("UTF8");

	public EchoServer() throws IOException {
		// 创建一个Selector对象
		selector = Selector.open();
		// 创建一个ServerSocketChannel对象
		serverSocketChannel = ServerSocketChannel.open();
		// 保证关闭服务器程序后可以马上再次启动服务器程序
		serverSocketChannel.socket().setReuseAddress(true);
		// 使serverSocketChannel工作于非阻塞模式
		serverSocketChannel.configureBlocking(false);
		// 把服务器进程与一个本地端口绑定
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		System.out.println("服务器启动");
	}

	public void service() {
		try {
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			while (selector.select() > 0) {
				Set<SelectionKey> readKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = readKeys.iterator();
				while (it.hasNext()) {
					SelectionKey key = null;
					try {
						key = (SelectionKey) it.next();
						it.remove();
						if (key.isAcceptable()) {
							// 获得与SelectionKey关联的ServerSocketChannel
							ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
							// 获得与客户连接的SocketChannel
							SocketChannel socketChannel = ssc.accept();
							System.out.println("收到客户连接，来自：" + socketChannel.socket().getInetAddress() + ":"
									+ socketChannel.socket().getPort());
							// 把SocketChannel设置为非阻塞模式
							socketChannel.configureBlocking(false);
							// 创建一个用于存放用户发送来的数据的缓冲区
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							// SocketChannel向Selector注册读就绪时间和写就绪时间
							// 关联了一个buffer附件
							socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
						}
						if (key.isReadable()) {
							receive(key);
						}
						if (key.isWritable()) {
							send(key);
						}
					} catch (IOException e) {
						e.printStackTrace();
						if (key != null) {
							// 使得这个SelectionKey失效
							// 使得Selector不再监控这个SelectionKey感兴趣的时间
							key.cancel();
							key.channel().close();// 关闭与SelectionKey关联的SocketChannel
						}
					}
				}
			}
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 接收数据
	 * @param key
	 * @throws IOException
	 */
	public void receive(SelectionKey key) throws IOException {
		// 获得与SelectionKey关联的附件
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		// 获得与SelectionKey关联的SocketChannel
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// 创建一个ByteBuffer，用于存放读到的 数据
		ByteBuffer readBuff = ByteBuffer.allocate(32);
		socketChannel.read(readBuff);
		readBuff.flip();

		// 把buffer的极限设为容量
		buffer.limit(buffer.capacity());
		// 把readBuff中的内容拷贝到buffer中
		// 假定buffer的容量足够大，不会出现缓冲区溢出异常
		buffer.put(readBuff);
	}

	/**
	 * 发送数据
	 * @param key
	 * @throws IOException
	 */
	public void send(SelectionKey key) throws IOException {
		// 获得与SelectionKey关联的附件
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		// 获得与SelectionKey关联的SocketChannel
		SocketChannel socketChannel = (SocketChannel) key.channel();
		buffer.flip();// 把极限设为位置，把位置设为0
		// 按照UTF8编码，把buffer中的字节转换为字符串
		String data = decode(buffer);
		// 如果还没有读到一行数据，就返回
		if (data.indexOf("\r\n") == -1)
			return;
		// 截取一行数据
		String outputData = data.substring(0, data.indexOf("\n") + 1);
		System.out.println(outputData);
		// 把输出的 字符串按照GBK编码，转换为字节，把它放在outputBuffer中
		ByteBuffer outputBuffer = encode("echo:" + outputData);
		// 输出outputBuffer中的所有字节
		while (outputBuffer.hasRemaining()) {
			socketChannel.write(outputBuffer);
		}

		// 把outputData字符串按照UTF8编码，转换为字节，把它放在ByteBuffer中
		ByteBuffer temp = encode(outputData);
		// 把buffer的位置设为temp的极限
		buffer.position(temp.limit());
		// 删除buffer中已经处理的数据
		buffer.compact();
		// 如果已经输出了字符串"bye\r\n"，就是SelectionKey失效，并关闭SocketChannel
		if ("bye\r\n".equals(outputData)) {
			key.cancel();
			socketChannel.close();
			System.out.println("关闭与客户的连接");
		}
	}

	private ByteBuffer encode(String string) {
		return charset.encode(string);
	}

	private String decode(ByteBuffer buffer) {
		CharBuffer charBuffer = charset.decode(buffer);
		return charBuffer.toString();
	}

	public static void main(String[] args) throws IOException {
		new EchoServer().service();
	}
}
