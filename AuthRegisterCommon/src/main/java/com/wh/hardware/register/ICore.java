package com.wh.hardware.register;

/**
 * 此接口的实现代码必须为IDynamicCoreClass。getCryptCode返回的加密代码
 * 用户实现的加密库核心函数接口，用来提示加密失败，以及当成功后调用核心函数完成业务功能
 * 
 * @author wy
 *
 * @param <T> 程序的业务数据
 */
public interface ICore<T> {
	/**
	 * 核心业务功能调用，此功能一定要与一个具体的核心的业务数据或者功能片段相关，尽量调用实现不要包含资源的试用，而是通过context的数据做一些基本变换操作即可
	 * 
	 * @param context 程序上下文，用调用者传入
	 * @return 对于此业务系统有意义的值
	 */
	T call(Object context);

	/**
	 * 当注册校验失败，包括超期，并且注册文件内的hint设置为htMsg时调用，用以向客户显示未注册成功的提示信息
	 * 
	 * @param msg 显示的信息，由注册信息设置
	 */
	void hint();
}