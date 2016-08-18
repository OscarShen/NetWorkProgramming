package advance.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CallbackDigest implements Runnable {
	private File input;

	public CallbackDigest(File input) {
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
			while (-1 != (b = din.read()))
				;
			din.close();
			byte[] digest = sha.digest();
			CallbackDigestUserInterface.receiveDigest(digest, input.getName());
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
