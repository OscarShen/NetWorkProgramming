package portScanner;

import java.io.IOException;
import java.net.Socket;

/**
 * 扫描1~1024的端口，观察 是否连接。（扫描非常慢）
 * @author ruiyao.shen
 *
 */
public class PortScanner {
	public static void main(String[] args) {
		String host = "localhost";
		if (args.length > 0) {
			host = args[0];
		}
		new PortScanner().scan(host);
	}

	public void scan(String host) {
		Socket socket = null;
		for (int port = 1; port < 1024; port++) {
			try {
				socket = new Socket(host, port);
				System.out.println("There is a server on port " + port);
			} catch (Exception e) {
				System.out.println("Can't connect to port " + port);
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
