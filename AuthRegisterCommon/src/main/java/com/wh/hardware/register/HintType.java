package com.wh.hardware.register;

/***
 * 当客户端注册认证失败时系统的处理类型
 * @author wy
 *
 */
public enum HintType{
	/***
	 * 提示用户，信息通过clientTag
	 */
	htMsg("提示"), 
	htHalf("终止运行");
	
	String code;
	private HintType(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		return code;
	}
}