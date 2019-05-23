------------------------
Gc收集器				|
------------------------
	# 收集算法是内存回收的方法论, 收集器则是实现了
	# jvm规范对收集器怎么去实现, 没有任何规定, 所以不同厂家, 不同版本的可能不一样

	# GC收集器目前主要的有
		* 新生代收集器
			Serial
			ParNew
			Parallel Scavenge

		* 老年代收集器
			Concurrent Mark Sweep(CMS)
			Parallel Old
			Serial Old(MSC)
		
		* 全堆收集器
			Garbage First(G1)

		* 不同的收集器可以共存, 组合使用
		* 它们之间没有绝对的最完美的收集器,(如果有, 也不用实现那么多出来)
	
	
	# 可能涉及到的名词解释
		* 并行
			* 多个GC收集器线程在同时的工作, 但是应用线程处于终止状态

		* 并发
			* 应用线程和GC收集线程同时(因为CPU核心数的问题,可能会交替执行)执行
		
		* Minor GC
			* 从年轻代空间(包括 Eden 和 Survivor 区域)回收内存
			* Minor GC 都会触发 stop-the-world

		* Major GC
			* 清理老年代

		* Full GC
			* 清理整个堆空间—包括年轻代和老年代

		* Concurrent Mode Failure 
			* Concurrent Mode Failure 并发执行收集的时候, 不能腾出内存给正在运行的业务线程
			* 此时会临时启动:Serial Old 收集器来重新对老年代进行垃圾收集

	
------------------------
Gc收集器关系图			|
------------------------
	+-------------------------年轻代回收------------------------------------+
	|[Serial]	  [ParNew]		[Parallel Scavenge]							|
	|--------------------------老年代回收-------------------------------[G1]|
	|[Concurrent Mark Sweep(CMS)] [Serial Old(MSC)]	[Parallel Old]			|
	+-----------------------------------------------------------------------+
	
	# 可以组合的GC收集器
		[Serial] + Concurrent Mark Sweep(CMS)]

		[Serial] + [Serial Old(MSC)]

		[ParNew] + [Concurrent Mark Sweep(CMS)]

		[ParNew] + [Serial Old(MSC)]
		
		[Parallel Scavenge] + [Serial Old(MSC)]

		[Parallel Scavenge] + [Parallel Old]

		[Serial Old(MSC)] + [Concurrent Mark Sweep(CMS)]

	
------------------------
Serial					|
------------------------
	# 最基本的, 历史最悠久的收集器, 在JDK1.3.1之前是虚拟机新生代的唯一选择
	# 这个收集器是一个单线程的收集器
	# 它在执行GC的时候, 会暂停所有的工作线程, 直到它收集结束

	# 它在Client模式下的虚拟机来说, 是一个很好的选择
		* Client模式(桌面环境), 一般分配给jvm管理的内存不是很大
		* GC导致的停顿时间, 完全可以控制在几十毫秒 - 100毫秒以内, 这是可以接收的
		* 单线程, 免去了多线程的切换, 可以专注的进行收集工作, 效率更高
	
	# 工作模式(采用复制算法)
		1. (第一阶段标记)暂停业务线程, 单线程收集(新生代采用复制算法)	
		2. 唤醒业务线程
		3. (第二阶段标记)暂停业务线程, 单线程收集(老年代采用标记整理算法)
	
------------------------
ParNew					|
------------------------
	# 它其实就是 Serial 的多线程版本
	# 还可以通过一系列的JVM参数对它进行控制
		-XX:SurvivorRatio
		-XX:PretenureSizeThreshold
		-XX:HandlePromotionFailure
	
	# 他的工作流程
		1. (第一阶段标记)暂停业务线程, 多线程收集(新生代采用复制算法)
		2. 唤醒业务线程
		3. (第二阶段标记)暂停业务线程, 单线程收集(老年代采用标记整理算法)
	

	# 它相对于 Serial 并没太多的创新之处, 甚至连部分代码都是共用的, 但它却是很多在运行Server模式的JVM的'新生代'首选收集器
		* 因为一个与性能无关的原因: 除了 Serial 以外, 只有它能与Concurrent Mark Sweep(CMS)收集器配合工作
	
	# 在单核的CPU环境中, 它的效果不一定比 Serial 好
		* 甚至由于多线程交互的开销, 可能不如 Serial
		* 这个收集器, 在通过超线程技术实现的两个CPU核心环境中, 都不能100%的 保证超越 Serial 

	# 随着CPU核心数量的增加, 它对于GC时系统资源的有效利用还是很有好处的
		* 默认开启的收集线程数量与CPU的核心数量相同
		* 在核心数量非常多的情况下, 可以通过参数来限制垃圾收集的线程的数量
			-XX:ParallelGCThreads=10


	# 指定使用ParNew收集器
		-XX:+UseConMarkSweepGC
			* 使用Concurrent Mark Sweep(CMS)作为老年代收集器
			* 如果使用该参数, 默认就会使用: ParNew 作为新生代的收集器

		-XX:+UseParNewGC
			* 强制系统使用 ParNew 作为新生代的收集器
		
------------------------
Parallel Scavenge		|
------------------------
	# 新生代收集器, 使用复制算法, 也是可以并行收集的

	# 看似与 ParNew 一样, 但是它的目的则是达到一个可控制的吞吐量
		吞吐量 = CPU用于业务线程的时间 / (CPU用于业务线程的时间 + CPU用于垃圾收集的时间)

		* 虚拟机运行了 100分钟, 垃圾回收花费了1 分钟, 则吞吐量就是: 99%
		* 因为与吞吐量相关,也被称为:吞吐量优先的收集器
	
	# 提供了参数用于精准的控制吞吐量
		-XX:MaxGCPauseMillis
			* 置最大垃圾收集停顿时间, 它的值是一个大于 0 的整数
			* 收集器在工作时, 会调整 Java 堆大小或者其他一些参数,尽可能地把停顿时间控制在 MaxGCPauseMillis 以内
			* 停顿时间的缩短, 是牺牲了吞吐量(以前10s一次100ms的GC, 现在5s一次70ms的GC)和新生代空间(对体积小的内存收集比较快)换来的, 这也导致GC发生得更加的频繁
			* 过小的话, GC停顿时间确实下降了,　但是吞吐量也下降了


		-XX:GCTimeRatio
			* 设置吞吐量大小, 它的值是一个大于 0 小于 100 之间的整数
			* 可以理解为: 垃圾收集时间占总时间的比例
			* 默认 GCTimeRatio 的值为 99, 那么系统将花费不超过 1 / (1 + 99) = 1% 的时间用于垃圾收集
		
		-XX:+UseAdaptiveSizePolicy
			* 打开自适应 GC 策略, 在这种模式下, 其他的一些属性不需要自己去设置, 参数会被自动调整, 以达到在堆大小, 吞吐量和停顿时间之间的平衡点
				-Xmn(新生代大小)
				-XX:+SuivivorRatio(Eden和Survivor区的比例)
				-XX:+PretenureSizeThreshold(晋升老年代对象年龄)
			* 使用自适应GC策略, 只需要把基本的内存数据设置好,例如堆内存大小值
			* 然后仅仅关注/设置最大停顿时间:-XX:MaxGCPauseMillis 
			* 或者给JVM设置一个最大吞吐量 -XX:GCTimeRatio 的优化目标, 具体的工作细节就由jvm完成
	
------------------------
Serial Old				|
------------------------
	# 它是 Serial 收集器的老年代版本, 也是一个单线程的收集器
	# 它存在的意义, 也是给 Client 模式的JVM使用
	# 如果在Server模式下使用, 它有两个用途
		* 在JDK1.5之前与 Parallel Scavenge  搭配使用

		* 作为 Concurrent Mark Sweep(CMS) 收集器的后备预案, 在并发收集发生 Concurrent Mode Failure 时使用
		* 出现此现象的原因主要有两个:一个是在年老代被用完之前不能完成对无引用对象的回收 , 一个是当新空间分配请求在年老代的剩余空间中得到满足
		
	# Serial 配合 Serial Old工作模式
		1. (第一阶段标记)暂停业务线程, 单线程收集(新生代采用复制算法)
		2. 唤醒业务线程
		3. (第二阶段标记)暂停业务线程, 单线程收集(老年代采用标记-整理算法)

------------------------
Parallel Old			|
------------------------
	# 它其实是 Parallel Scavenge 收集器的老年代版本, 使用标记清理算法, 在JDK1.6以后才提供

	# 在此之前, 新生代的Parallel Scavenge收集器存在一个尴尬的问题
		* 如果选择了Parallel Scavenge收集器, 那么老年代除了Serial Old收集器外别无选择
		* 由于老年代收集器Serial Old在服务端性能上的拖累, 就算使用 Parallel Scavenge 也未必能获得最大的吞吐量效果
		* 这种组合的吞吐量, 不一定比使用 ParNew + Serial Old 更好

	# Parallel Old 收集器出现后, 就有了和Parallel Scavenge收集器的一个优良组合
		* 在注重吞吐量和CPU资源敏感的场合, 都可以优先考虑 Parallel Scavenge + Parallel Old

	
	# Parallel Scavenge 配合 Parallel Old 工作原理
		1. (第一阶段标记)暂停业务线程, 多线程收集
		2. 唤醒业务线程
		3. (第二阶段标记)暂停业务线程, 多线程收集

--------------------------
Concurrent Mark Sweep(CMS)|
--------------------------
	# 以获取最短停顿时间为目标的收集器, 基于标记-清除的算法实现 
	# 目前很大一部分Java都是应用在B/S环境下的, 尤其重视响应速度, 希望系统停顿时间短, CMS就非常适合
	# 它的执行过程稍微要复杂一点
		1. 初始标记(Initial Mark)
			* 停止业务线程
			* 标记一下GC Roots能直接关联的对象, 速度很快

		2. 并发标记(Concurrent Mark)
		3. 重新标记(Remark)
			* 停止业务线程
			* 修正并发标记期间, 因为程序逻辑导致标记产生变动的对象记录

		4. 并发清除(Concurrent Sweeo)

		* 整个过程中, 最耗时的并发标记和并发清除过程不会停止业务线程
	
	# 它是优秀的收集器, 并发收集, 低停顿, 但是也有一些缺点
		1, 对CPU资源敏感
			* 事实上, 面向并发设计的程序都对CPU资源敏感
			* 并发阶段, 虽然不会停止业务线程, 但是因为占用了一部分CPU资源, 从而会导致应用程序变慢, 导致总吞吐量变低

			* CMS默认启动的回收线程数:(CPU数量 + 3) / 4, 也就是说在CPU核心数 >= 4的时候, 并发回收时, 占用 25% d CPU资源
			* 如果CPU核心数不足 4, 业务线程受到的影响就比较大了, 因为需要付出一半的算力去执行GC

			* 为了解决这个问题, JVM提供了一种"增量式并发收集器" , 没用,已经被标识为过时
		
		
		2, 无法处理浮动垃圾, 可能出现:Concurrent Mode Failure 失败而导致另一次Full GC的产生
			* 浮动垃圾就是, 在执行并发清理的时候, 因为是并发,业务线程不会停止, 在此期间产生的垃圾, 只能在下一次GC处理
			* 也就是说要留足内存空间给业务线程使用, 因此CMS不能跟其他的收集器一样要等到老年代几乎被完全填满了后再进行收集,因为需要预留空间给并发业务线程使用

			* JDK1.5环境下,默认老年代内存使用了 68%后, 就会触发该收集器, 这个设置比较保守
			* 如果中老年代增长不是很快, 可以适当的调高参数(0 - 100 百分比), 降低GC次数
				-XX:CMSInitiatingOccupancyFraction
			
			* 如果CMS运行期间,预留的内存不足以业务线程的使用, 就会出现一次:Concurrent Mode Failure 失败
			* 这是JVM会启动预案, 临时启动:Serial Old 收集器来重新对老年代进行垃圾收集
			* 也就是说-XX:CMSInitiatingOccupancyFraction设置太高, 可能会导致大量的Concurrent Mode Failure 失败, 性能反而降低
		
		3, 因为使用标记清除算法,导致大量的内存碎片
			* 碎片过多, 这会给内存分配带来很大的麻烦
			* 往往老年代还有足够的内存, 但是因为找不到连续的空间不得不触发一次 Full GC

			* 为了解决这个问题,CMS提供了一个参数(默认已经开启)
				-XX:+UseCMSCompactAtFullCollection

				* 用于在CMS顶不住要进行Full GC的时候, 开启内存碎片的合并整理过程
				* 内存整理的国产没法并发, 空间随便问题解决了, 但是业务线程停顿时间不得不变长了

			* 设置执行多少次不压缩的FullGC后, 跟着来一次带压缩的
				-XX:CMSFullGCsBeforeCompaction

				* 默认为0, 表示每次进入Full GC时都进行碎片整理


			
			
--------------------------
G1收集器				  |
--------------------------
	# Garbage-First, 收集器是当前收集器技术发展的最前沿成果之一
		* 从JDK7开始
		* 设计的目标就是为了替代掉:Concurrent Mark Sweep(CMS) 收集器
	
	# 它是全堆收集器, 老年代, 新生代都行
	
	# 因为是全堆收集器, 所以使用G1的时候, 内存布局就有点儿与其他的收集器不同
		* 它把整个堆划分为多个相等大小的独立区域(Region)
		* 还是保留了新生代, 老年代的概念,　但是新生代和老年代不再是物理隔离了, 它们都是一部分 Region 的集合

		* 引入了分区的概念, 弱到了分代的概念
	
	# 比较重要, 单独写


