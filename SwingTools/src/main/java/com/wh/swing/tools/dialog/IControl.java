package com.wh.swing.tools.dialog;

public interface IControl {
	public static final String Tag_Object = "Tag_Object";

	public Object getTag(String key);

	public void setTag(String key, Object value);
}
