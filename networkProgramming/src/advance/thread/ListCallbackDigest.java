package advance.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class ListCallbackDigest implements Runnable {
	private File input;
	List<DigestListener> listenerList = new Vector<>();

	public ListCallbackDigest(File input) {
		this.input = input;
	}

	public synchronized void addDigestListener(DigestListener l) {
		listenerList.add(l);
	}

	public synchronized void removeDigestListener(DigestListener l) {
		listenerList.remove(l);
	}

	private synchronized void sendDigest(byte[] digest) {
		ListIterator<DigestListener> iterator = listenerList.listIterator();
		while (iterator.hasNext()) {
			DigestListener dl = iterator.next();
			dl.digestCalculated(digest);
		}
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
			this.sendDigest(digest);
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
