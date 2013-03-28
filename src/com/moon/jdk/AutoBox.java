package com.moon.jdk;

import junit.framework.Assert;

import org.junit.Test;

/**
 * <br>自动装箱和拆箱
 * <br>
 * <br>享元模式
 * <br>例如word里面的英文字符，预先初始化26个对象，以共享池获取
 * <br>这样即使有很多的字符使用，但是实际上只有26个对象和引用，共享这些引用
 * 
 * <br>例如windows的文件夹图标
 * <br>预先初始化，全系统共享这些图标的引用
 * 
 * @author liuzhao
 * 
 */
public class AutoBox {

	@Test
	public void test(){
		Integer obj = 3; // 自动装箱
		System.out.println(obj+12); // 自动拆箱
		
		/** 
		 *  使用的值是共享的
		 *  共享池存在，范围 -128 ~ 127，一个字节之内
		 *  创建一个字节之内的数值，先看是否在共享池里有引用，有就直接返回该引用
		 */
		Integer i1 = 13;
		Integer i2 = 13;
		Assert.assertTrue(i1 == i2);
		
		/**
		 *  不在共享池内
		 */
		Integer i3 = 137;
		Integer i4 = 137;
		Assert.assertFalse(i3 == i4);
		
		Integer i5 = Integer.valueOf(13);
		Integer i6 = Integer.valueOf(13);
		Assert.assertTrue(i5 == i6);
	}
	
}
