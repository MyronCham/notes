-----------------------
Garbage	Collection     |
-----------------------
	# GC考虑的三个东西
		* 哪些内存需要回收?
		* 什么时候回收?
		* 如何回收?
	
-----------------------
引用计数算法		   |
-----------------------
	# 给对象添加一个引用计数器
		* 添加引用, 计数器 +1
		* 引用时效, 计数器 -1

		* 计数器 == 0, 则对象不可用
	
	# Java主流的JVM没有选用这个算法来管理内存
		* 主要是这东西很难解决'循环引用'的问题
			class A {
				public Object instance;
			}
			class B{
				public Object instance;
			}
			public class Main {
				public static void main(String[] args) {
					A a = new A();
					B b = new B();
					a.instance = b;
					b.instance = a;
					a = null;
					b = null;
					System.gc();
				}
			}
	
-----------------------
可达性分析算法		   |
-----------------------
	# 主流的语言, 都是采用这个GC算法
		- Java
		- C#(辣鸡语言)
		- Lisp
	
	# 基本的思想
		* 通过一系列的 'GC Roots' 作为起始点
		* 从起始点往下搜索的路径, 成为引用链(树状)
		* 当一个对象到 'GC Roots' 没有任何引用链的时候, 就可以证明该对象不可用的
	
	# 可以作为 'GC Roots' 的对象
		* 虚拟机栈用引用的对象
		* 方法区中类静态属性引用的对象
		* 本地方法栈中JNI(Native方法)引用的对象
	

-----------------------
引用				   |
-----------------------
	# 强引用(Strong Refrence)
		* 普遍存在的: Object instance = new Object();
		* 只要关联还在, 不会回收内存

	# 软引用(Soft Reference)
		* 描述一些有用,但非必须的对象
		* 在系统即将发生内存溢出之前, 会把这些对象列进回收范围内, 进行二次回收
		* 如果内存还是不足, 则抛出内存溢出异常
		* JDK 提供的实现类:SoftReference<T>

	# 弱引用(Weak Reference)
		* 描述非必须的对象
		* 它只能存在于下一次GC之前, 下一次GC, 不论内存是否足够, 都会回收掉那些仅被弱引用关联的对象
		* JDK 提供的实现类:WeakReference<T>

	# 虚引用(Phantom Reference)
		* 虚引用关系的存在不会影响它的生存时间, 无法根据虚引用访问到对象
		* 设置虚引用的唯一目的:在这个对象被GC回收时, 收到系统通知
		* JDK提供的实现类:PhantomReference<T>



----------------------------
对象的回收至少会经过两次标记|
----------------------------
	# 第一次标记
		* 经过可达性分析后, 发现对象没有与 Gc Roots 关联
		* 如果覆写了 finalize 方法, 并且还没被调用过
			- 把对象放置到一个名为:F-Queue 的队列中, 稍后会通过一个虚拟机自动创建的一个低优先级线程(Finalizer)去执行finalize方法
			- 虚拟机只是会触发这个方法, 但是不会阻塞到这个方法结束, 因为如果这个方法执行缓慢, 或者发生了死循环, 可能导致: F-Queue 队列中的其他对象永远处于等待状态, 甚至导致GC系统崩溃

	# 第二次标记
		* 如果在第一次标记的时候,如果对象通过 finalize() 发生了自救的行为(把自己赋值到其他的对象或者其他的行为, 反正可以引用到GC Roots), 那么第二次标记就会把对象从即将回收集合移除
	
	# finalize()  不建议使用, 快忘记
		* 这并不是C/C++的析构函数, 而是为了骗搞C/C++的开发者上Java贼船而鼓捣的东西
		* 运行代价高昂, 不确定性大, 无法保证各个对象的调用顺序
	
			
----------------------------
关于方法区的回收			|
----------------------------
	# 蛮多人认为方法区(HopSpot中的 Metaspeace(永久代))是没有GC的
		* jvm规范也定义了不需要在方法区中实现gc
		* 而且在方法区中实现gc, 性价比比较低(耗费了性能, 但是回收不到多少空间)
		* 比起搜刮方法区, 不如果搜刮新生代
	
	# 元空间的垃圾回收, 分为两个部分: 废弃的常量, 无用的类
	
	# 废弃的常量
		* 这个跟回收Java对象非常相似, 不存在引用则回收
	
	# 无用的类, 这个判断比较苛刻, 必须满足以下的所有条件
		* 该类的所有实例都已经被回收, 系统中不存在该类的实例
		* 加载该类的 ClassLoader 已经被回收
		* 该类的 Class 对象, 没有在任何地方被引用, 也就说无法在任何地方通过反射获取到该类的方法

		* 满足条件, 可以被进行回收, 而回收不是必须的, 可以通过 jvm 参数控制
			* 关闭Class的回收
				-Xnoclassgc
			
			* 查看类的加载/卸载信息
				-verbose:class -XX:+TraceClassLoading -XX:+TraceClassUnloading


------------------------------------
垃圾收集算法-标记清除法				|
------------------------------------
	# 这个是基本的一个算法, 其他的很多算法都是基于这个算法的思路进行优化的
	# 主要就是两个阶段, 标记和清除
		* 标记的过程, 上面已经说了
		* 清除就不用多说
	
	# 它有两个问题
		* 效率问题: 标记和清除的效率都不高
		* 空间问题
			- 标记清除之后会产生大量不连续的内存碎片
			- 内存随便过多后, 会占用大量的内存空间, 可能会导致内存不足而不得不提前触发下一次GC

	
------------------------------------
垃圾收集算法-复制算法				|
------------------------------------
	# 为了解决效率问题, 把内存分为同等大小的两块儿, 每次使用其中一块儿
		* 当一块儿用完了之后, 就把该块儿上已经存活的对象移动到另一块儿, 然后释放该内存块儿
		* 这样内存分配的时候, 不用考虑内存碎片的问题, 只需要移动堆指针,　按照顺序分配内存
		* 实现简单, 运行高效, 但是内存被缩小为了原来的一半

	# 现在商业的JVM都是这种算法
		* IMB研究表明: 98% 的新生对象都是朝生夕死, 所以并不需要按照 1:1 的比例来划分内存空间
		* 而是划分为一块较大的:Eden空间,和两块较小的:Survivor空间

		* 每次使用Eden和其中一块Survivor(from)空间, 执行回收的时候, 把存活的对象复制到另一块Survivor(to)空间,
		* 最后清理掉Eden和刚才使用的Survivor(from)空间
	
		* HotSpot默认的Edent和Survivor空间大小比例是: 8:1
			[8|1|1]
		
		* 也就是说,新生代的容量为 90%, 仅有 10% 的内存被'浪费'

		* 也不能保证每次GC后, 存活的对象大小都不大余 10% (要移动到另一块Survivor(to)空间,它只有 10% 的内存大小)
		* 当Survivor(to)不够使用的时候, 还需要依赖其他的内存进行担保分配 - 老年代(ParOldGen)


------------------------------------
垃圾收集算法-标记-整理算法			|
------------------------------------
	# 复制收集算法在存活对象较多的时候, 要进行较多的复制工作, 效率会更低
	# 更关键的是, 不想浪费 50% 的空间, 就需要有额外的空间进行担保, 以应对内存中所有对象都是 100% 存活的情况
		* 所以老年代(ParOldGen)一般不能直接选用这种算法
	
	# 根据老年代的特点, 一种: 标记 - 整理 的算法被人提了出来

		* 与标记清除的过程仍然是一样的, 但是后续的步骤不是对可回收对象直接进行清理
		* 而是让所有存活对象都向一端移动, 然后直接清理掉端边界以外的内存
	

------------------------------------
垃圾收集算法-分代收集算法			|
------------------------------------
	# 目前商业的JVM的垃圾收集都采用这种算法

	# 根据对象存活周期的不同, 把内存划分为几块, 根据不同年代的特点采用不同的收集算法
	
	# 新生代(PSYoungGen)
		* 每次垃圾回收都有大量的对象死去, 只有少量存活,
		* 使用复制法, 仅仅需要复制少量存活的对象就可以完成收集

	# 老年代(ParOldGen)
		* 对象存活率高, 没有额外的空间对它进行分配担保
		* 必须使用 标记清除 或者 标记整理 算法来完成回收

------------------------------------
HotSpot的算法实现					|
------------------------------------
	# 枚举根节点
		* 作为GC Roots的对象一般都是在:全局引用(常量, 类静态属性), 执行上下文中(栈帧, 本地变量表)
		* 现在很多应用, 仅方法区就有好几百MB, 如果挨个找的话, 会消费非常多的时间

		* GC 执行时, 必须要停顿所有的Java线程,Sun称之为: Stop The World
		* GC过程, 如果对象引用关系还可以不断变化的话, 可达性分析算法的结果可能不准确

		* 系统暂停后,不需要去一个不漏的检查全局引用, 系统上下文
		* 使用一个 OoMap的数据结构来完成


	# 安全点
		* 并非执行程序也并非在任何执行点都能停顿下来执行GC, 只有在到达安全点才能暂停
		

	# 安全区域
		