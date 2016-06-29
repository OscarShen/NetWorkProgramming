package echoSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * echo客户端
 * 
 * @author Oscar
 *
 */
public class EchoClient {
	private int port = 8000;
	private Socket socket;
	private String host = "localhost";

	public static void main(String[] args) throws UnknownHostException, IOException {
		new EchoClient().talk();
	}

	public EchoClient() throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		System.out.println("客户端启动！");
		System.out.println(socket.getInetAddress() + ";" + socket.getPort());
	}

	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}

	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}

	public void talk() {
		try {
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				pw.println(msg);
				System.out.println(br.readLine());
				if ("bye".equals(msg)) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
