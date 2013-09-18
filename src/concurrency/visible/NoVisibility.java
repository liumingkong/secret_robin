package concurrency.visible;

/**
 * @ClassName: NoVisibility 
 * @Description: TODO
 * @author liuzhao
 * @date 2013-7-20 下午4:45:26 
 * 
 * 主线程和读线程都将访问共享变量ready和number
 * 主线程启动读线程，然后将number设为42，并将ready设为true;
 * 读线程循环直到发现ready的值变为0，随后输出number的值。
 * 
 * 事实上，很有可能输出0，或者根本无法终止。
 * 
 * 重排序的问题
 * 输出0，因为读线程可能看到了ready=true，但没看到number的值
 * 这种现象被称为"重排序"。
 * 
 * 在没有同步的情况下，编译器，处理器以及JVM都可能对多线程操作的执行顺序进行一些
 * 意想不到的调整，虽然不太合理，但可以充分利用现代多核处理器的强大性能。
 * 
 * 失效数据
 * 无限循环，因为主线程始终没有拿到执行机会
 * 当读线程查看ready时，可能会得到一个已经失效的数据，除非每次访问变量时都使用同步，
 * 否则可能获得该变量的一个失效值。
 * 
 */
public class NoVisibility {

	private static boolean ready;
	private static int number;
	
	private static class ReaderThread extends Thread {
		public void run() {
			while(!ready){
				Thread.yield();
			}
			System.out.println(number);
		}
	}
	
	public static void main(String[] args) {
		new ReaderThread().start();
		number = 42;
		ready = true;
	}
}
