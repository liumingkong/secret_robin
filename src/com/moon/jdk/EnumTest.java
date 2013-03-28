package com.moon.jdk;

import org.junit.Test;

public class EnumTest {

	@Test
	public void testEnum(){		
		Request request1 = Request.POST;
		System.out.println(request1.name()); //打出对应的string
		System.out.println(request1.ordinal()); //排行
		System.out.println(Request.valueOf("POST")); // 转换
		Request.values(); // 获取枚举类中所有的元素
	}
	
}

/** 
 * 加上括号部分就会调用带参数的构造函数
 * 把枚举类的每个枚举项都当做对象
 * 因此每个枚举项都要进行初始化，带括号的枚举项初始化，会调用带参数的构造函数
 * 打印出结果:
 * Request has params
 * Request has params
 * Request no params
 */
enum Request{
	POST(1),
	GET(2),
	MUTIPART;
	private Request(){
		System.out.println("Request no params");
	}
	private Request(int code){
		System.out.println("Request has params");
	}
}