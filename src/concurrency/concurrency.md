### 线程安全
<pre>
线程安全的定义：多线程情况下保证执行的正确性。
</pre>

####  非原子的64位操作

 * 非volatile的64位数值变量double和long，JVM允许将64位的读写操作分为两个32位的操作
 * 当读取一个非volatile类型的long变量时，不同线程读写很可能读取到某个值的高32和另一个值的低32位
 * 多线程程序中使用"共享且可变"的long和double等类型的变量也是不安全的
 * 除非用volatie来声明，或者用锁保护起来。
 
#### Volatile变量
<pre>
volatile变量，用来确保将变量的更新操作通知到其他线程。
编译器与运行时都会注意到这个变量是共享的，不会将该变量的操作与其他内存操作一起重排序。

volatie变量不会被缓存在寄存器或者其他处理器不可见的地方，因此在读取volatile变量时总会返回最新写入的值。

volatile变量，就是SynchronizedInteger。
在访问volatie变量时不会执行加锁操作，也就不会使执行线程阻塞，
比synchronized更加轻量级的同步机制。

</pre>

<pre>
当volatile变量能够简化代码的实现以及同步策略的验证时，才应该使用它。
如果在验证正确性时需要对可见性进行复杂的判断，那就不要使用volatile变量。

volatie的正确使用方式：确保它们自身状态的可见性，确保他们所引用的对象的状态的可见性，以及标识一些重要的程序生命周期事件的发生(初始化或者关闭)
</pre>

<pre>
volatile的典型用法：检查某个状态标记以判断是否退出循环
volatile boolean asleep;

while(!asleep)
dosomething（）;

volatile通常用做某个操作完成，发生中断或者状态的标识
但是volatile用于其他操作时要很小心
例如，volatile的语义不足保证递增操作的原子性
</pre>

<pre>
当且仅当满足一下所有条件时，才可以使用volatile变量：
1.对变量的写入操作不依赖变量的当前值，或者能偶确保只有单个线程更新变量的值
2.该变量不会与其他状态变量一起纳入不变性条件中
3.在访问变量时不需要加锁
</pre>


#### 线程封闭技术
<pre>
当访问共享的可变数据时，需要用同步，一种避免同步的方式就是不共享数据
如果仅仅在单线程内访问数据，就不需要同步，这种技术被称为线程封闭。
当某个对象封闭在一个线程中时，这种用法将自动实现线程安全性。
</pre>
<pre>
Swing中大量使用线程封闭技术，Swing的可视化组件和数据模型对象都不是线程安全的。
Swing通过将它们封闭到Swing的事件分发线程中来实现线程安全。
</pre>
<pre>
JDBC的Connection对象必须是线程安全的。
典型的服务器应用中，线程从连接池获得一个Connection对象，且该对象来处理请求，使用完后再将对象返回给连接池。大多数请求都是由单线程采用同步的方式来处理，且在Connection对象返回前，连接池不会把它分配给其他线程。
因此，这种连接管理模式在处理请求时隐含地将Connection对象封闭在线程中。
</pre>

##### 栈封闭
<pre>
局部变量就是被封闭在执行线程之中。
</pre>

##### ThreadLocal类
<pre>
采用ThreadLocal来封闭线程

ThreadLocal的目的是保存一些线程级别的全局变量。
例如，connection，或者事务上下文，避免这些值需要一直通过函数参数的方式一路传递下去。
</pre>

<pre>
常见的用法:
public class Test2 {
public static void main(String[] args)throwsInterruptedException{ testThreadLocal();} 

private static void testThreadLocal(){
Util.setGlobalName("zili.dengzl");                
new Foo().printName();}}

class Foo{        
public void printName({System.out.println("globalName="+Util.getGlobalName());}}

class Util {        
private static final ThreadLocal<String> globalName = new ThreadLocal<String>();         
public static String getGlobalName(){return globalName.get();}         
public static void setGlobalName(String name){globalName.set(name);}
}
</pre>

<pre>
上面的例子采用Map<Thread,T>
class MockThreadLocal<T>{        
private Map<Thread, T> map =new HashMap<Thread, T>();         
public T get(){return (T)map.get(Thread.currentThread());}
public void set(T value){map.put(Thread.currentThread(), value);}}

也可以实现ThreadLocal的效果，但是，当对应的线程消失，map中对应的线程不会被内存回收，会造成内存泄露。

ThreadLocal：每个Thread都有一个threadLocalMap，key是threadLocal对象，value是具体使用的值，ThreadLocal对象的get就是先取得当前的Thread，然后从这个Thread的threadLcoalMap中取出值。
</pre>

<pre>
ThreadLocal的实现
public T get(){        
Thread t = Thread.currentThread();        
ThreadLocalMap map = getMap(t);        
if(map !=null){            
ThreadLocalMap.Entry e = map.getEntry(this);            
if(e !=null)
return(T)e.value;}        
return setInitialValue();}
注意这里如果取到没有该线程对应的值，会调用setInitialValue();，最终调用initialValue()生成一个值，这也是我们很多场景下要override这个方法的原因；

ThreadLocalMap getMap(Thread t){return t.threadLocals;}

Thread类中
ThreadLocal.ThreadLocalMap threadLocals =null;
所有的ThreadLocal的信息，最终是关联到Thread上的，线程消失后，对应的Thread对象也被回收，这时对应的ThreadLocal对象(该线程部分)也会被回收。

每个线程的内部变量，内部的map--threadLocals。
这里为什么是一个ThreadLocalMap呢，因为一个线程可以有多个ThreadLocal变量，通过map.getEntry(this)取得对应的某个具体的变量。
 
ThreadLocalMap的Entry是一个weakReference
这里主要因为ThreadLocalMap的key是ThreadLocal对象，如果某个ThreadLocal对象所有的强引用没有了，会利用weakref的功能把他回收掉，然后复用这个entry。

如果是这个样子
private final ThreadLocal<String> globalName =new ThreadLocal<String>();

没有static，ThreadLocal会不断地被new出来，然后死掉，每次ThreadLocalmap中都会多出一个entry，然后这个entry强引用一个ThreadLocal对象，ThreadLocalMap本身就没有办法确定哪个entry是不用了的，如果恰好这个线程是线程池中的，会存活很久，那就杯具了。

ThreadLocalMap用了weakReference，失去强引用的ThreadLocal对象会在下次gc时被回收，然后ThreadLocalMap本身在get和set的时候会考察key为空的Entry，并复用它或者清除，从而避免内存泄露。

</pre>

<pre>
调用get的Thread类中
ThreadLocal.ThreadLocalMap threadLocals;
该ThreadLocalMap中，
<key,value> <(ThreadLocal)当前的threadlocal线程，(Entry)缓存的信息体>
</pre>

<pre>
private static final ThreadLocal<String> globalName =new ThreadLocal<String>();
ThreadLocalMap是以ThreadLocal对象为key的，如果ThreadLocal所在的类不是static，也不是单例的，那么两个Test对象就有两个key，取出来的数据肯定不同。
</pre>

<pre>
ThreadLocal为每个使用该变量的线程都存有一份独立的副本，因此get返回的总是当前执行线程在调用set时设置的最新值。

</pre>

#### 安全发布的常用模式
<pre>
可变对象必须通过安全的方式来发布，这通常意味着在发布和使用该对象的线程时都必须使用同步。
要安全地发布一个对象，对象的引用以及对象的状态必须同时对其他线程可见。
一个正确构造的对象可以通过以下方式来安全地发布：
1.静态初始化函数中初始化一个对象引用
2.将对象的引用保存到volatile类型的域或者AtomicReferance对象
3.将对象的引用到某个正确构造对象的final类型域中
4.将对象的引用保存在一个由锁保护的域中
</pre>

<pre>
线程安全库中的容器类提供以下的安全发布保证：
1.Hashtable，synchronizedMap，ConcurrentMap
2.Vector，CopyOnWriteArrayList，CopyOnWriteArraySet等
3.BlockingQueue，ConcurrentLinkedQueue
</pre>



