package advance.thread.pool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩文件线程
 * 
 * @author ruiyao.shen
 *
 */
public class GZipThread extends Thread {
	private List<File> pool;
	private static int filesCompressed = 0;

	public GZipThread(List<File> pool) {
		this.pool = pool;
	}

	private static synchronized void incrementFilesCompressed() {
		filesCompressed++;
	}

	@Override
	public void run() {
		while (filesCompressed != GZipAllFiles.getNumberOfFilesToBeCompressed()) {
			File input = null;

			synchronized (pool) {
				while (pool.isEmpty()) {
					if (filesCompressed == GZipAllFiles.getNumberOfFilesToBeCompressed()) {
						System.out.println("Thread ending");
						return;
					}
					try {
						pool.wait();
					} catch (InterruptedException e) {
					}
				}

				input = (File) pool.remove(pool.size() - 1);
				incrementFilesCompressed();
			}

			if (!input.getName().endsWith(".gz")) {
				try {
					InputStream in = new FileInputStream(input);
					in = new BufferedInputStream(in);

					File output = new File(input.getParentFile(), input.getName() + ".gz");
					if (!output.exists()) {
						OutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(output)));
						int len;
						byte[] b = new byte[512];
						while (-1 != (len = in.read(b))) {
							out.write(b, 0, len);
						}
						out.flush();
						out.close();
						in.close();
					}
				} catch (FileNotFoundException e) {
					System.err.println(e);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}
	}

}
