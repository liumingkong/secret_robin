package com.moon.jdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import junit.framework.Assert;

import org.junit.Test;

/**
 * <h1>反射:就是将java类中的所有元素映射成类</h1>
 * @author liuzhao
 *
 */
public class ReflectTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testReflect() throws Exception{
		
		String str = "abc";
		Class csz1 = str.getClass();
		Class csz2 = String.class;
		Class csz3 = Class.forName("java.lang.String");
		
		// 三个对象都具有相同的字节码,JVM中只保留一份该字节码，用该字节码可以创建多个对象
		// 一份字节码对应于多个对象
		Assert.assertSame(csz1, csz2);
		Assert.assertSame(csz2, csz3);
		
		Assert.assertFalse(csz1.isPrimitive()); // String不是基本类型，返回false
		Assert.assertTrue(int.class.isPrimitive()); // int是基本原始类型
		Assert.assertFalse(int.class == Integer.class); // 不同类型，很显然
		Assert.assertTrue(int.class == Integer.TYPE); // Integer.TYPE 代表的是包装类所包装的基本类型的字节码
		Assert.assertFalse(int[].class.isPrimitive()); // 并不是基本类型
		Assert.assertTrue(int[].class.isArray()); // 是否是数组
		
		// 表示第一个参数StringBuffer，第二个参数int
		// 事实上，在newInstance的内部实现中，是缓存这个constructor的，因此性能不高
		Constructor<String> constructor = String.class.getConstructor(StringBuffer.class);
		String str1 = (String) constructor.newInstance(new StringBuffer("abc")); // 获得一个实例
		Assert.assertEquals(str1, "abc");
		// 只能调用无参数的构造函数，包含参数的构造方法采用下面的方式是无效的
		String str2 = (String) Class.forName("java.lang.String").newInstance();
		Assert.assertEquals(str2,"");
		
		// Field，属性
		ReflectPoint reflectPoint = new ReflectPoint(1,2);
		Field fieldY = reflectPoint.getClass().getField("y");// y 属性是public
		Assert.assertEquals(fieldY.get(reflectPoint),2);
		Field fieldX = reflectPoint.getClass().getDeclaredField("x");// x 属性是private，要getDeclaredField这个方法
		fieldX.setAccessible(true); // 设置属性为私有 private
		Assert.assertEquals(fieldX.get(reflectPoint),1);
		
		
	}
}

class ReflectPoint{
	private int x;
	public int y;
	
	public ReflectPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
}
