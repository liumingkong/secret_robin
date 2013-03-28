package com.moon.jdk;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 测试可变参数
 * @author liuzhao
 *
 */
public class VariableParameter {

	@Test
	public void testVariable(){
		System.out.println(add(1,2,3,4,5));
		Assert.assertTrue(15 == add(1,2,3,4,5));
		Assert.assertTrue(10 == add(1,2,3,4));
	}
	
	/** 
	 * <BR>编译器为该可变参数隐含创建一个数组
	 * <br>在方法体中以数组的形式访问可变参数
	 * <BR>普通参数应该直接访问，不在可变参数数组内，例如x，y
	 */
	public int add(int x,int y,int ... args){
		int sum = x + y;
		for(int i:args){
			sum = sum + i;
		}
		return sum;
	}
}
