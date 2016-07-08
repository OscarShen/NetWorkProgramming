package multiThread2;

public class ThreadPoolTester {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("用法：java ThreadPoolTest numTasks poolSize");
			System.out.println("  numTasks - integer:任务的数目");
			System.out.println("  numThreads - integer:线程池中的线程数目");
			return;
		}
		int numTasks = Integer.parseInt(args[0]);
		int poolSize = Integer.parseInt(args[1]);

		ThreadPool threadPool = new ThreadPool(poolSize);// 创建线程池

		// 运行任务
		for (int i = 0; i < numTasks; i++) {
			threadPool.execute(createTask(i));
		}
		// threadPool.join();
		threadPool.close();
	}

	/**
	 * 创建一个简单的任务
	 * 
	 * @param taskID
	 * @return
	 */
	private static Runnable createTask(final int taskID) {
		return new Runnable() {

			@Override
			public void run() {
				System.out.println("Task" + taskID + ":start");
				try {
					Thread.sleep(500);// 增加执行一个任务的时间
				} catch (InterruptedException e) {
				}
				System.out.println("Task" + taskID + ":end");
			}
		};
	}
}
