package pingClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 一个网站连接响应时间的连接器：非阻塞的
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
	// 用于控制Connector线程
	boolean shutdown = false;

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

	/**
	 * 向targets队列中加入一个任务，主线程会调用该方法
	 * 
	 * @param target
	 */
	public void addTarget(Target target) {

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

	/**
	 * 向finishedTarget队列中加入一个任务，主线程和 Connector线程会调用该方法
	 * 
	 * @param target
	 */
	public void addFinishedTarget(Target target) {
		synchronized (finishedTargets) {
			finishedTargets.notify();
			finishedTargets.add(target);
		}
	}

	/**
	 * 打印finishedTargets队列中的任务，Printer线程会调用该方法
	 */
	public void printFinishedTarget() {
		try {
			for (;;) {
				Target target = null;
				synchronized (finishedTargets) {
					while (finishedTargets.size() == 0) {
						finishedTargets.wait();
					}
					target = finishedTargets.removeFirst();
				}
				target.show();
			}
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * 接收用户输入的域名，向targets队列中加入任务，主线程会调用该方法
	 */
	public void receiveTarget() {
		try {
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				if (!"bye".equals(msg)) {
					Target target = new Target(msg);
					addTarget(target);
				} else {
					shutdown = true;
					selector.wakeup();// 使Connector线程从Selector的select()方法中退出
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取出targets队列中的任务，向Selector注册链接就绪事件，Connector线程会调用该方法
	 */
	public void registerTargets() {
		synchronized (targets) {
			while (targets.size() > 0) {
				Target target = targets.removeFirst();
				try {
					target.channel.register(selector, SelectionKey.OP_CONNECT, target);
				} catch (ClosedChannelException e) {
					try {
						target.channel.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					target.failure = e;
					addFinishedTarget(target);
				}
			}
		}
	}

	/**
	 * 处理链接就绪事件，Connector线程会调用该方法
	 */
	public void processSelectedKeys() {
		//
		for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
			SelectionKey selectionKey = it.next();
			it.remove();

			Target target = (Target) selectionKey.attachment();
			SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
			try {
				if (socketChannel.finishConnect()) {
					selectionKey.cancel();
					target.connectFinish = System.currentTimeMillis();
					socketChannel.close();
					addFinishedTarget(target);
				}
			} catch (IOException e) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				target.failure = e;
				addFinishedTarget(target);
			}
		}
	}

	/**
	 * 连接器
	 * 
	 * @author ruiyao.shen
	 *
	 */
	public class Connector extends Thread {
		public Connector() {

		}

		@Override
		public void run() {
			while (!shutdown) {
				try {
					registerTargets();
					if (selector.select() > 0) {
						processSelectedKeys();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 打印器
	 * 
	 * @author ruiyao.shen
	 *
	 */
	public class Printer extends Thread {
		public Printer() {
			setDaemon(true);// 设置为后台线程
		}

		@Override
		public void run() {
			printFinishedTarget();
		}
	}
}

/**
 * 任务目标
 * 
 * @author ruiyao.shen
 *
 */
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

	/**
	 * 打印任务执行的结果
	 */
	void show() {
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