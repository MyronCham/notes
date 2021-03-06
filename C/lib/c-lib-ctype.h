------------------------
ctype.h					|
------------------------
	# 包含了一系列对字符的判断函数,这些函数都使用字符作为参数,如果满足某些条件就返回 true,反之返回 false

------------------------
判断					|
------------------------
	isalpha(int c)
		* 如果该字符是一个字母,返回 true

	isalnum(int c)
		* 字母或者数字

	isblank(int c)
		* 标准空白字符(空格,制表符,换行符),或者任何额其他本地化指定为空白的字符

	iscntrl(int c)
		* 控制字符,如: Ctrl + B

	isdigit(int c)
		* 是否是十进制数字

	isgraph(int c)
		* 除了空格以外的任意可打印字符
		* 该函数检查所传的字符是否有图形表示法

	islower(int c)
		* 小写字母

	isprint(int c)
		* 可打印字符

	ispunct(int c)
		*  标点符号(除了空格或做字母数字字符以外的任何可打印字符)

	isspace(int c)
		* 空白字符(空格,换行符,换页符,回车符,垂直制表符,水平制表符,或者其他本地化定义的字符)

	isupper(int c)
		* 是否是大写字母

	isxdigit(int c)
		* 十六进制字符

------------------------
行为					|
------------------------
	tolower(int c)
		* 如果参数是大写,该函数返回其小写,否则返回原始参数
	
	toupper(int c)
		* 同上,大写转换