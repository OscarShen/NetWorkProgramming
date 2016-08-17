package advance.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 使用存取方法 返回结果的线程
 * 
 * @author ruiyao.shen
 *
 */
public class ReturnDigest extends Thread {
	private File input;
	private byte[] digest;

	public ReturnDigest(File input) {
		this.input = input;
	}

	@Override
	public void run() {
		try {
			FileInputStream in = new FileInputStream(input);
			MessageDigest sha = MessageDigest.getInstance("SHA");
			DigestInputStream din = new DigestInputStream(in, sha);
			@SuppressWarnings("unused")
			int b;
			while ((b = din.read()) != -1)
				;
			din.close();
			digest = sha.digest();
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public byte[] getDigest() {
		return digest;
	}
}
