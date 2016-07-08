package socketShutdown;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sender {
	private String host = "localhost";
	private int port = 8000;
	private Socket socket;
	private static int stopWay = 1;
	private final int NATURAL_STOP = 1;
	private final int SUDDEN_STOP = 2;
	private final int SOCKET_STOP = 3;
	private final int OUTPUT_STOP = 4;

	public Sender() throws UnknownHostException, IOException {
		socket = new Socket(host, port);
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException, Exception {
		stopWay = 2;
		new Sender().send();
	}

	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		return new PrintWriter(os);
	}

	public void send() throws Exception {
		PrintWriter pw = getWriter(socket);
		for (int i = 0; i < 20; i++) {
			String msg = "hello_" + i;
			pw.println(msg);
			System.out.println("send:" + msg);
			Thread.sleep(500);
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
		}
	}
}
