package advance.address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 仿写Unix上的nslookup，输入IP返回地址，输入地址返回IP
 * 
 * @author ruiyao.shen
 *
 */
public class HostLookup {
	public static void main(String[] args) {
		if (args.length > 0) {// 使用命令行
			for (int i = 0; i < args.length; i++) {
				System.out.println(lookup(args[i]));
			}
		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter names and IP addresses. Enter \"exit\" to quit.");
			try {
				while (true) {
					String host = in.readLine();
					if (host.equalsIgnoreCase("exit") || host.equalsIgnoreCase("quit")) {
						break;
					}
					System.out.println(lookup(host));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String lookup(String host) {
		InetAddress node;
		try {
			node = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return "Cannot find host " + host;
		}

		if (isHostname(host)) {
			return node.getHostAddress();
		} else {
			return node.getHostName();
		}
	}

	private static boolean isHostname(String host) {
		if (host.indexOf(':') != -1)// 判断是否为IPV6地址
			return false;
		char[] ca = host.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			if (!Character.isDigit(ca[i])) {
				if (ca[i] != '.')
					return true;
			}
		}
		return false;
	}
}
