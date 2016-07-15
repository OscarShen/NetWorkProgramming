package nonblock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * 非阻塞的PingClient
 * 
 * @author ruiyao.shen
 *
 */
public class PingClient {
	private Selector selector;
	// 存放用户新提交的任务
	private LinkedList<Target> targets = new LinkedList<>();
	// 存放已经完成的需要打印的任务
	private LinkedList<Target> finishedTargets = new LinkedList<>();

	public PingClient() throws IOException {
		selector = Selector.open();
		Connector connector = new Connector();
		Printer printer = new Printer();
		connector.start();// 启动连接线程
		printer.start();// 启动打印线程
		receiveTarget();// 主线程接收用户从控制台输入的主机名，然后提交Target
	}

	public static void main(String[] args) throws IOException {
		new PingClient();
	}

	public void addTarget(Target target) {
		// 向targets队列中加入一个任务，主线程会调用该方法
		SocketChannel socketChannel = null;

		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(target.address);

			target.channel = socketChannel;
			target.connectStart = System.currentTimeMillis();

			synchronized (target) {
				targets.add(target);
			}
			selector.wakeup();

		} catch (IOException e) {
			if (socketChannel != null) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			target.failure = e;
			addFinishedTarget(target);
		}
	}

	private void addFinishedTarget(Target target) {
		
	}

	private void receiveTarget() {

	}

	public class Connector extends Thread {

	}

	public class Printer extends Thread {

	}
}

class Target {
	InetSocketAddress address;
	SocketChannel channel;
	Exception failure;
	long connectStart;// 开始连接的时间
	long connectFinish = 0;// 连接成功时的时间
	boolean shown = false;// 该任务是否已经打印

	Target(String host) {
		try {
			address = new InetSocketAddress(InetAddress.getByName(host), 80);
		} catch (UnknownHostException e) {
			failure = e;
		}
	}

	void show() {// 打印任务执行的结果
		String result;
		if (connectFinish != 0) {
			result = (connectFinish - connectStart) + "ms";
		} else if (failure != null) {
			result = failure.toString();
		} else {
			result = "Timed out!";
		}
		System.out.println(address + ":" + result);
		shown = true;
	}
}
