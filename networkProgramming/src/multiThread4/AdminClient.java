package multiThread4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class AdminClient {
	public static void main(String[] args) {
		Socket socket = null;
		try {
			socket = new Socket("localhost", 8001);
			OutputStream os = socket.getOutputStream();
			os.write("shutdown\r\n".getBytes());

			// 接受服务器的反馈
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg = null;
			while ((msg = br.readLine()) != null) {
				System.out.println(msg);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
