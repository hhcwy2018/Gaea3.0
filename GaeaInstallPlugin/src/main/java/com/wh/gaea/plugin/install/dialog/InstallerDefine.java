package com.wh.gaea.plugin.install.dialog;

import java.io.File;
import java.util.Date;

import com.wh.gaea.GlobalInstance;
import com.wh.tools.IEnum;

public abstract class InstallerDefine {
	public static final int INSTALLER_GRID_TITLE = 0;
	public static final int INSTALLER_GRID_VALUE = 1;
	public static final int INSTALLER_GRID_MSG = 2;
	public static final int INSTALLER_GRID_ALLOWNULL = 3;
	public static final int INSTALLER_GRID_INPUTTYPE = 4;
	public static final int INSTALLER_GRID_REGULAR = 5;
	public static final int INSTALLER_GRID_SAVEKEY = 6;
	public static final int INSTALLER_GRID_SAVEFILE = 7;
	public static final int INSTALLER_GRID_SAVETYPE = 8;

	public static final int INSTALLER_COMMAND_COMMANDTYPE = 0;
	public static final int INSTALLER_COMMAND_COMMANDINDICATE = 1;
	public static final int INSTALLER_COMMAND_ARGS = 2;
	public static final int INSTALLER_COMMAND_CHECKRESULT = 3;
	public static final int INSTALLER_COMMAND_WAITTIME = 4;

	public enum InputType implements IEnum{
		itString(100, "字符录入框"), itInt(0, "整数录入框"), itShort(1, "短整数录入框"), itByte(2, "单字节整数录入框"), itLong(3, "长整数录入框"), itFloat(4,"浮点数录入框"), itBoolean(5,"布尔录入框"), itDate(6,"日期录入框");
		private int code;
		private String msg;

		private InputType(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
		
		public String getMsg() {
			return msg;
		}
		
		public static InputType fromMsg(String msg) {
			switch (msg) {
			case "整数录入框":
				return itInt;
			case "短整数录入框":
				return itShort;
			case "单字节整数录入框":
				return itByte;
			case "长整数录入框":
				return itLong;
			case "浮点数录入框":
				return itFloat;
			case "布尔录入框":
				return itBoolean;
			case "日期录入框":
				return itDate;
			default:
				return itString;
			}
		}
		
		public static InputType fromClass(Class<?> c) {
			if (Integer.class.isAssignableFrom(c))
				return itInt;
			if (Short.class.isAssignableFrom(c))
				return itShort;
			if (Byte.class.isAssignableFrom(c))
				return itByte;
			if (Long.class.isAssignableFrom(c))
				return itLong;
			if (Float.class.isAssignableFrom(c))
				return itFloat;
			if (Boolean.class.isAssignableFrom(c))
				return itBoolean;
			if (Date.class.isAssignableFrom(c))
				return itDate;
			
			return itString;
		}
		
		public static InputType fromCode(int code) {
			switch (code) {
			case 0:
				return itInt; 
			case 1:
				return itShort; 
			case 2:
				return itByte; 
			case 3:
				return itLong; 
			case 4:
				return itFloat; 
			case 5:
				return itBoolean; 
			case 6:
				return itDate; 
			default:
				return itString;
			}
		}
		
	}
	
	public static File getProjectInstallPath() {
		return GlobalInstance.instance().getProjectPath("install");		
	}
	
	public static File getInstallRootPath(String name) {
		return new File(getProjectInstallPath(), name);
	}
	
	public static File getInstallPath(String rootName, String dirName) {
		return new File(getInstallRootPath(rootName), dirName);
	}
	
	public static File getInstallFile(String rootName, String dirName, String fileName) {
		return new File(getInstallPath(rootName, dirName), fileName);
	}
	
	public static File getInstallEngineFile(String rootName) {
		return new File(getInstallRootPath(rootName), "main_engine.wh");
	}

}
