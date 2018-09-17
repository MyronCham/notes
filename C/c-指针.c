--------------------------------
指针							|
--------------------------------
	# 指针是一个变量,其值为另一个变量的地址
	# 指针变量的声明
		[合法的C语言类型] *变量名称

		int    *ip;    /* 一个整型的指针 */
		double *dp;    /* 一个 double 型的指针 */
		float  *fp;    /* 一个浮点型的指针 */
		char   *ch;     /* 一个字符型的指针 */

	# 所有指针的值的实际数据类型,不管是整型,浮点型,字符型,还是其他的数据类型,都是一样的,都是一个代表内存地址的长的十六进制数
		* 不同数据类型的指针之间唯一的不同是,指针所指向的变量或常量的数据类型不同
		* 32位编译器,用32位大小保存地址(4字节)
		* 64位编译器,用64位大小保存地址(8字节)

		printf("%d",sizeof(int *));		//4


	# 指针的简单使用
			int number = 10;
			int *pointer = &number;
			printf("number的内存地址是:%p\n",pointer);
			printf("通过指针访问值是:%d\n",*pointer);
	
	# 野指针
		* 值栈保存了一个毫无意义的地址(非法的地址)
			int *p;
			p = 0x00000FFF;
			printf("%p",p);     //00000FFF

		* 只有定义了变量后,此变量的地址才是合法地址
		* 操作野指针本身不会有任何问题
		* 操作野指针指向的内存,这种直接操作系统未授权的内存,是非法的,就会有问题

	#  NULL 指针
		* 赋为 NULL 值的指针被称为空指针
		* 它可以尽量的避免野指针
		*  NULL 指针是一个定义在标准库中的值为零的常量: #define NULL((void *)0)
			int  *pointer = NULL;
			printf("pointer 的地址是 %p\n", pointer);		//00000000
		
		* 在大多数的操作系统上,程序不允许访问地址为 0 的内存,因为该内存是操作系统保留的
		* 然而,内存地址 0 有特别重要的意义,它表明该指针不指向一个可访问的内存位置,但按照惯例,如果指针包含空值(零值),则假定它不指向任何东西
			if(pointer)     /* 如果 p 非空,则完成 */
			if(!pointer)    /* 如果 p 为空,则完成 */


--------------------------------
多级指针						|
--------------------------------
	# 指向指针的指针
		int x = 15;
		int *p1 = &x;			//p1 -> x
		int **p2 = &p1;			//p2 -> p1
		int ***p3 = &p2;		//p3 -> p2
		printf("x=%d",***p3);		//15

--------------------------------
指针数组						|
--------------------------------
	# 专门保存指针的数组
		int arr[5] = {1,2,3,4,5};
		int *ps[5];
		for(int x = 0 ; x < 5;x++ ){
			ps[x] = &arr[x];
		}
		for(int x = 0 ; x < 5;x++ ){
			printf("%d",*ps[x]);		//12345
		}


--------------------------------
指针函数						|
--------------------------------
	# 指向函数的指针
		#include <stdlib.h>
		#include <stdio.h>
		
		//定义函数
		int max(int a,int b){
			return a > b ? a : b;
		}

		int main(void){

			//m 就是函数 max 的指针
			int (* m)(int,int) = &max;

			int a,b,c;

			scanf("%d %d %d",&a,&b,&c);

			//等同于 max(max(a,b),c)
			int r = m(m(a,b),c);

			printf("%d\n",r);
		}
		
		* 定义方法
			[返回值类型] (* 指针变量)(形参类型)
			int (* pointer)(void)
			int (* pointer)(int,int)
			void (* pointer)(void)

	
	# 回调函数 
		* 函数指针作为某个函数的参数
		* 函数指针变量可以作为某个函数的参数来使用的,回调函数就是一个通过函数指针调用的函数
