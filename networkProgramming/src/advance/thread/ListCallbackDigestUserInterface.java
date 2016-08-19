package advance.thread;

import java.io.File;

public class ListCallbackDigestUserInterface implements DigestListener {
	private File input;
	private byte[] digest;

	public ListCallbackDigestUserInterface(File input) {
		this.input = input;
	}

	public void calculateDigest() {
		ListCallbackDigest cb = new ListCallbackDigest(input);
		cb.addDigestListener(this);
		Thread t = new Thread(cb);
		t.start();
	}

	@Override
	public void digestCalculated(byte[] digest) {
		this.digest = digest;
		System.out.println(this);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(input.getName());
		result.append(": ");
		if (digest != null) {
			for (int i = 0; i < digest.length; i++) {
				result.append(digest[i]);
				result.append(" ");
			}
		} else {
			result.append("digest not available");
		}
		return result.toString();
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			File f = new File(args[i]);
			ListCallbackDigestUserInterface d = new ListCallbackDigestUserInterface(f);
			d.calculateDigest();
		}
	}

}
