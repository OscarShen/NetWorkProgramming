package advance.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestThread extends Thread {
	private File input;

	public DigestThread(File input) {
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
			byte[] digest = sha.digest();
			StringBuffer result = new StringBuffer(input.toString());
			result.append(": ");
			for (int i = 0; i < digest.length; i++) {
				result.append(digest[i] + " ");
			}
			System.out.println(result);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			File f = new File(args[i]);
			Thread t = new DigestThread(f);
			t.start();
		}
	}

}
