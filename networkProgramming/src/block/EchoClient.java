package block;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * 阻塞式的EchoClient
 * 
 * @author Administrator
 *
 */
public class EchoClient {
	private SocketChannel socketChannel = null;

	public EchoClient() throws IOException {
		socketChannel = SocketChannel.open();
		InetAddress ia = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(ia, 8000);
		socketChannel.connect(isa);
		System.out.println("与服务器的连接建立成功！");
	}

	public static void main(String[] args) throws IOException {
		new EchoClient().talk();
	}

	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		return new PrintWriter(new OutputStreamWriter(os));
	}

	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream is = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(is));
	}

	public void talk() {
		try {
			BufferedReader br = getReader(socketChannel.socket());
			PrintWriter pw = getWriter(socketChannel.socket());
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				pw.println(msg);
				pw.flush();
				System.out.println(br.readLine());

				if ("bye".equals(msg)) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
