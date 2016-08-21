package advance.thread;

public class TimeSlicer extends Thread {
	private long timeslice;

	public TimeSlicer(long milliseconds, int priority) {
		this.timeslice = milliseconds;
		this.setPriority(priority);
		// 如果这是所剩的最后一个县城，这不应当组织VM退出
		this.setDaemon(true);
	}

	public TimeSlicer(long milliseconds) {
		this(milliseconds, 10);
	}

	public TimeSlicer() {
		this(100, 10);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(timeslice);
			} catch (InterruptedException e) {
			}
		}
	}

}
