package advance.thread;

import java.io.File;

public class JoinDigestUserInterface {
	public static void main(String[] args) {
		ReturnDigest[] digestThreads = new ReturnDigest[args.length];
		for (int i = 0; i < args.length; i++) {
			File f = new File(args[i]);
			digestThreads[i] = new ReturnDigest(f);
			digestThreads[i].start();
		}

		for (int i = 0; i < args.length; i++) {
			try {
				digestThreads[i].join();
				StringBuffer result = new StringBuffer(args[i]);
				result.append(": ");
				byte[] digest = digestThreads[i].getDigest();
				for (int j = 0; j < digest.length; j++) {
					result.append(digest[j] + " ");
				}
				System.out.println(result);
			} catch (InterruptedException e) {
				System.err.println("Thread Interrupted before completion");
			}
		}
	}
}
