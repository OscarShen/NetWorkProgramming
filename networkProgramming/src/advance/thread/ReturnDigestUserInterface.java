package advance.thread;

import java.io.File;

/**
 * 使用存取方法获得线程输出的主程序
 * 
 * @author ruiyao.shen
 *
 */
public class ReturnDigestUserInterface {

	public static void main(String[] args) throws InterruptedException {
		// raceCondition(args);
		polling(args);
	}

	/**
	 * 和处理器比速度：一般会返回空指针异常
	 */
	public static void raceCondition(String[] args) {
		for (int i = 0; i < args.length; i++) {
			// 计算摘要
			File f = new File(args[i]);
			ReturnDigest dr = new ReturnDigest(f);
			dr.start();

			// 现在显示结果
			StringBuffer result = new StringBuffer(f.toString());
			result.append(": ");
			byte[] digest = dr.getDigest();
			for (int j = 0; j < digest.length; i++) {
				result.append(digest[j] + " ");
			}
			System.out.println(result);
		}
	}

	/**
	 * 效率非常低的轮询
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void polling(String[] args) throws InterruptedException {
		ReturnDigest[] digests = new ReturnDigest[args.length];
		for (int i = 0; i < args.length; i++) {
			// 计算摘要
			File f = new File(args[i]);
			digests[i] = new ReturnDigest(f);
			digests[i].start();
		}

		for (int i = 0; i < args.length; i++) {
			while (true) {
				// 现在显示结果
				byte[] digest = digests[i].getDigest();
				if (digest != null) {
					StringBuffer result = new StringBuffer(args[i]);
					result.append(": ");
					for (int j = 0; j < digest.length; j++) {
						result.append(digest[j] + " ");
					}
					System.out.println(result);
					break;
				}
			}
		}
	}
}
