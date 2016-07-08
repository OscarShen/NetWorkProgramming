package socketShutdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
	private int port = 8000;
	private ServerSocket serverSocket;
	private static int stopWay = 1;
	private final int NATURAL_STOP = 1;
	private final int SUDDEN_STOP = 2;
	private final int SOCKET_STOP = 3;
	private final int OUTPUT_STOP = 4;

	public Receiver() throws IOException {
		serverSocket = new ServerSocket(port);
		System.out.println("服务器已经启动！");
	}

	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream is = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(is));
	}

	public void receive() throws IOException, InterruptedException {
		Socket socket = null;
		socket = serverSocket.accept();
		BufferedReader br = getReader(socket);

		for (int i = 0; i < 20; i++) {
			String msg = br.readLine();
			System.out.println("receive:" + msg);
			Thread.sleep(1000);
			if (i == 2) {
				if (stopWay == SUDDEN_STOP) {
					System.out.println("突然停止程序 ！");
					System.exit(0);
				} else if (stopWay == SOCKET_STOP) {
					System.out.println("关闭Socket并终止程序！");
					socket.close();
					break;
				} else if (stopWay == OUTPUT_STOP) {
					socket.shutdownOutput();
					System.out.println("关闭输出流并终止程序");
					break;
				}
			}
		}
		if (stopWay == NATURAL_STOP) {
			socket.close();
			serverSocket.close();
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		stopWay = 2;
		new Receiver().receive();
	}
}
