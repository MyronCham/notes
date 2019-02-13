--------------------
Buffer				|
--------------------
	# 涉及的类库
		ReferenceCounted
			|-ByteBuf
				|-CompositeByteBuf
			|-ByteBufHolder
			|-FileRegion
				|-DefaultFileRegion
		ByteBufProcessor
		ByteBufAllocator
		Unpooled
		ByteBufUtil
	
	# 设计
		* Netty 使用 reference-counting(引用计数)来判断何时可以释放 ByteBuf 或 ByteBufHolder 和其他相关资源
		* 从而可以利用池和其他技巧来提高性能和降低内存的消耗,这一点上不需要开发人员做任何事情
		* 但是在开发 Netty 应用程序时,尤其是使用 ByteBuf 和 ByteBufHolder 时,应该尽可能早地释放池资源
	
	# Netty 缓冲 API 提供了几个优势
		* 可以自定义缓冲类型
		* 通过一个内置的复合缓冲类型实现零拷贝
		* 扩展性好,比如 StringBuilder
		* 不需要调用 flip() 来切换读/写模式
		* 读取和写入索引分开
		* 方法链
		* 引用计数
		* Pooling(池)
	
	# Netty 使用 reference-counting(引用计数)的时候知道安全释放 Buf 和其他资源
	
	# ByteBuf 的默认最大容量限制是 Integer.MAX_VALUE

	# ByteBuf 的属性
		capacity	总大小
		readindex	读角标
		wirteindex	写角标
	
	# Buffer的分配
		ByteBufAllocator
			ByteBufAllocator.heapBuffer()
			ByteBufAllocator.directBuffer()
			ByteBufAllocator.compositeBuffer()

		Unpooled (非池化)缓存
			Unpooled.buffer();
			Unpooled.directBuffer();
			Unpooled.compositeBuffer();

--------------------
Heap Buffer(堆缓冲区)|
--------------------
	# 最常用的类型是 ByteBuf 将数据存储在 JVM 的堆空间
		* 提供了直接访问数组的方法,通过 ByteBuf.array()来获取 byte[]数据
	
	# 访问非堆缓冲区 ByteBuf 的数组会导致 UnsupportedOperationException
		* 可以使用 ByteBuf.hasArray()来检查是否支持访问数组
	
------------------------
Direct Buffer(直接缓冲区)|
------------------------
	# 在堆之外直接分配内存,直接缓冲区不会占用堆空间容量
	# 直接缓冲区的缺点是在分配内存空间和释放内存时比堆缓冲区更复杂,而 Netty 使用内存池来解决这样的问题,这也是 Netty 使用内存池的原因之一
	# 直接缓冲区不支持数组访问数据,但是可以间接的访问数据数组
		ByteBuf directBuf = Unpooled.directBuffer(16);
		// 直接缓冲区
		if(!directBuf.hasArray()){
			// 可读的数据长度
			int len = directBuf.readableBytes();
			// 创建相同长度的数组
			byte[] arr = new byte[len];
			// 把缓冲区的数据读取到数组
			directBuf.getBytes(0, arr);
		}

----------------------------
Composite Buffer(复合缓冲区)|
----------------------------
	# 其实就是把多个Buffer柔和成一个Buffer
	# CompositeByteBuf.hasArray()总是返回 false,因为它可能包含一些直接或间接的不同类型的 ByteBuf
	# 它也实现了 ByteBuf ,而且它不会有内存泄漏的问题
	# 简单的使用
		// 创建复合缓冲区
		CompositeByteBuf compBuf = Unpooled.compositeBuffer();

		// 创建俩buffer
		ByteBuf heapBuf = Unpooled.buffer(8);
		ByteBuf directBuf = Unpooled.directBuffer(16);

		//添加 ByteBuf 到 CompositeByteBuf
		compBuf.addComponents(heapBuf,directBuf);

		//删除第一个 ByteBuf
		compBuf.removeComponent(0);

		// 可以迭代复合缓冲区中的每一个缓冲区
		Iterator<ByteBuf> iter = compBuf.iterator();
		while(iter.hasNext()){
			System.out.println(iter.next().toString());
		}

		//使用数组访问数据
		if(!compBuf.hasArray()){
			int len = compBuf.readableBytes();
			byte[] arr = new byte[len];
			compBuf.getBytes(0, arr);
		}

----------------------------
ByteBufUtil					|
----------------------------
	# 提供了很多的静态API可以操作buf

	String hexDump(ByteBuf buffer)
		* 返回buffer的16进制字符串,会根据rindex去读取
	
----------------------------
Unpooled					|
----------------------------
	ByteBuf copiedBuffer(CharSequence string, Charset charset)
		* 把指定的string编码为ByteBuff


