package concurrency;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrencyCase {

	/**
	 * 因式分解的servlet
	 * 无状态servlet
	 * 计算过程的临时状态仅仅保存在线程栈的局部变量上，并且只能由正在执行的线程访问
	 * 无状态的方法是线性安全的
	 */
	public class StatelessServlet implements Servlet{

		@Override
		public void service(ServletRequest req, ServletResponse resp) { 
			BigInteger i = extractFromServletRequest(req);
			BigInteger[] factors = factor(i);
			encodeIntoResponse(resp, factors);
		}
		
	}
	
	/** 统计访问次数：每处理一个请求就加1，多个线程同时对count进行读写，无法达到线程安全*/
	public class UnsafeCountServlet implements Servlet{

		private int count = 0;
		
		@Override
		public void service(ServletRequest req, ServletResponse resp) { 
			BigInteger i = extractFromServletRequest(req);
			BigInteger[] factors = factor(i);
			this.count ++;
			encodeIntoResponse(resp, factors);
		}
		
	}
	/**
	 * 上述实现，存在比较多的竞态条件。当某个计算的正确性取决于多线程交替执行时序时，就会发生竞态条件。
	 * 常见的竞态条件类型：Check-Then-Act，即通过一个可能失效的观测结果来决定下一步的动作
	 * 示例：延迟初始化的竞态条件
	 * 
	 * 假定A和B同时执行getInstance，A看到lazyInit为空，就创建实例。
	 * 在创建的过程中，B也看到lazyInit为空，就也去创建一个实例。
	 * 这样在两次调用后，就会产生不同的结果。
	 * 
	 * 
	 */
	public class LazyInit{
		private LazyInit lazyInit;
		public LazyInit getInstance() {
			if (null == lazyInit) 
				lazyInit = new LazyInit();
			return lazyInit;
		}
	}
	
	/** 对竞争的数据加锁，保证线程安全*/
	public class SafeCountServlet implements Servlet{

		private final AtomicLong count = new AtomicLong(0);
		
		public long getCount() {
			return count.get();
		}
		
		@Override
		public void service(ServletRequest req, ServletResponse resp) { 
			BigInteger i = extractFromServletRequest(req);
			BigInteger[] factors = factor(i);
			this.count.incrementAndGet();
			encodeIntoResponse(resp, factors);
		}
	}
	
	/** 
	 * 为了提升Servlet的性能，将最近计算的结果缓存起来
	 * 当两个连续的请求对相同的数值进行因数分解时，可以直接使用上次的计算结果，无需计算
	 * 
	 * 尽管引用本身是原子的，线程安全的，但是存在两个因素，存在对应关系，需要保证两者的更新和获取是一致的
	 */
	public class UnsafeCacheServlet implements Servlet{

		/** 可以用原子方式更新的对象引用*/
		private final AtomicReference<BigInteger> lastNumber = new AtomicReference<BigInteger>();
		private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<BigInteger[]>();
		 
		
		@Override
		public void service(ServletRequest req, ServletResponse resp) { 
			BigInteger i = extractFromServletRequest(req);
			if (i.equals(lastNumber.get())) {
				encodeIntoResponse(resp, lastFactors.get());
			} else { 
				BigInteger[] factors = factor(i);
				lastNumber.set(i);
				lastFactors.set(factors);
				encodeIntoResponse(resp, factors);
			}
		}
	}
	
	/** 
	 * 内置锁:可以支持原子性，同步代码块 
	 * 同步代码块包括两个部分:一个作为锁的对象引用，一个作为由这个锁保护的代码块
	 * synchronized方法以Class对象为锁
	 * synchronized(lock) {
	 * 访问或修改由锁保护的共享状态}
	 * 
	 * 每个java对象都可以用作一个实现同步的锁，这些锁被称为内置锁或者监视器锁
	 * 线程进入同步代码块之前会自动获得锁，并且退出代码块时会自动释放锁(无论是正常路径还是异常退出都会释放)
	 * 内置锁相当于一种互斥体，意味着只有一个线程能持有这种锁
	 * 
	 * 由于多个客户端无法同时使用因式分解，服务的响应将变得很低
	 */
	public class SynchronizedServlet implements Servlet{

		/** 可以用原子方式更新的对象引用*/
		private final AtomicReference<BigInteger> lastNumber = new AtomicReference<BigInteger>();
		private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<BigInteger[]>();
		 
		
		@Override
		public synchronized void service(ServletRequest req, ServletResponse resp) { 
			BigInteger i = extractFromServletRequest(req);
			if (i.equals(lastNumber.get())) {
				encodeIntoResponse(resp, lastFactors.get());
			} else { 
				BigInteger[] factors = factor(i);
				lastNumber.set(i);
				lastFactors.set(factors);
				encodeIntoResponse(resp, factors);
			}
		}

	}
	
	/*
	 * 重入
	 * 当某个线程请求一个由其他线程持有的锁时，发出请求的线程会被阻塞。
	 * 由于内置锁是可重入的，因此如果某个线程试图获得一个已经由它自己持有的锁，
	 * 那么这个请求就会成功。
	 * 
	 * 重入意味着获取锁的操作的粒度是"线程"，而不是调用。
	 * 重入的实现方式，为每个锁关联一个获取计数值和一个所有者线程。
	 * 
	 * 当计数值为0时，这个锁被认为是没有被任何线程持有
	 * 当线程请求一个未被持有的锁时，JVM会记下锁的持有者，计数值置为1.
	 * 如果同一个线程再次获得锁，计数值加1，推出时递减，为0时，表示释放。
	 * 
	 * 重入提升了加锁行为的封装性，简化了面向对象并发代码的开发。
	 * 子类改写了父类的synchronized方法，再调用父类的方法，如果不可重入，将产生死锁
	 */
	
	/*
	 * 常见的加锁约定，将所有的可变状态都封装在对象内部，
	 * 并通过对象的内置锁对所有访问可变状态的代码路径进行同步，
	 * 使得在该对象不会发生并发访问。
	 * 
	 * 许多线程安全类就采用这中方式，对象中的所有变量都由对象的内置锁保护起来。
	 * 如果新加方法忘记使用同步，那么这种加锁协议就会被破坏。
	 * 
	 */
	
	/*
	 * 通过内置锁来保护每个状态变量，对整个service方法进行同步。
	 * 这种简单且粗粒度的方法能确保线程安全性，但付出代价很高。
	 * 
	 * 降低代价的方法：缩小同步代码块的作用范围
	 * 注意：确保代码块不要过小，并且不要将本应是原子的操作拆分到多个同步代码块。
	 * 应该尽量将不影响共享状态且执行时间较长的操作从同步代码块中分离出来。
	 * 
	 */
	/**
	 *  添加一个命中计数器，缓存命中计数器，并且在第一个同步块中更新这两个变量
	 *  将同步代码块的路径缩短到足够短，就充分平衡了性能和简单性。
	 *  
	 *  另外，当执行时间很长，最好不要持有锁。
	 */
	public class CachedServlet implements Servlet{

		private BigInteger lastNumber;
		private BigInteger[] lastFactors;
		private long hits;
		private long cacheHits;
		
		public synchronized long getHits() {
			return this.hits;
		}
		
		public synchronized double getCacheHitRatio() {
			return (double) cacheHits/ (double) hits;
		}
		
		@Override
		public void service(ServletRequest req, ServletResponse resp) {
			BigInteger i = extractFromServletRequest(req);
			BigInteger[] factors = null;
			synchronized(this) {
				++ hits;
				if (i.equals(lastNumber)) {
					++ cacheHits;
					factors = lastFactors.clone();
				}
			}
			if (factors == null) {
				factors = factor(i);
				synchronized(this) {
					lastNumber = i;
					lastFactors = factors.clone();
				}
			}
			encodeIntoResponse(resp, factors);
		}
	}
	
	/**
	 * 
	 * @param req
	 * @return
	 */
	
	private BigInteger extractFromServletRequest(ServletRequest req){
		return BigInteger.valueOf(123456789032L);
	}
	
	private void encodeIntoResponse(ServletResponse resp, BigInteger[] factors){
		
	}
	
	private BigInteger[] factor(BigInteger i) {
		return null;
	}
}

interface Servlet{
	void service(ServletRequest req, ServletResponse resp);
}

class ServletRequest{
	
}

class ServletResponse{
	
}
