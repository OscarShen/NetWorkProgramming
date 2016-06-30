package smtpMailSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 一个邮件发送客户端
 * 
 * @author ruiyao.shen
 *
 */
public class MailSender {
	private String smtpServer = "smtp.mydomain.com";
	private int port = 25;

	public static void main(String[] args) {
		MailSender ms = new MailSender();
		Message msg = ms.new Message("tom@abc.com", "lina@def.com", "hello",
				"hi,I miss you very much.");
		ms.sendMail(msg);
	}

	/**
	 * 发送邮件
	 * 
	 * @param msg
	 */
	public void sendMail(Message msg) {
		Socket socket = null;
		try {
			socket = new Socket(smtpServer, port);
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			String localhost = InetAddress.getLocalHost().getHostName();

			sendAndReceive(null, br, pw); // 接收服务器的相应数据
			sendAndReceive("HELO" + localhost, br, pw);
			sendAndReceive("MAIL FROM:<" + msg.from + ">", br, pw);
			sendAndReceive("RCPTTO:<" + msg.to + ">", br, pw);
			sendAndReceive("DATA", br, pw);
			pw.println(msg.data);
			System.out.println("Client>" + msg.data);
			sendAndReceive(".", br, pw);
			sendAndReceive("QUIT", br, pw);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发送并接收一行数据
	 * 
	 * @param str
	 * @param br
	 * @param pw
	 * @throws IOException
	 */
	private void sendAndReceive(String str, BufferedReader br, PrintWriter pw)
			throws IOException {
		if (str != null) {
			System.out.println("Client>" + str);
			pw.println(str);
		}

		String responser;
		if ((responser = br.readLine()) != null) {
			System.out.println("Service>" + responser);
		}
	}

	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		return new PrintWriter(new OutputStreamWriter(os));
	}

	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream is = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(is));
	}

	/**
	 * 信息内部类
	 * 
	 * @author ruiyao.shen
	 *
	 */
	class Message {

		String data;
		String to;
		String from;
		String subject;
		String content;

		public Message(String from, String to, String subject, String content) {
			this.to = to;
			this.from = from;
			this.subject = subject;
			this.content = content;
			data = "Subject:" + subject + "\r\n" + content;
		}

	}
}
