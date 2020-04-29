package com.wh.gaea.install.form;

import javax.swing.JInternalFrame;

import com.wh.gaea.install.interfaces.PageConfigureInfo;

public abstract class BaseForm extends JInternalFrame{

	public static class CheckResult{
		public boolean isok = false;
		public String msg;
		
		public CheckResult() {}
		public CheckResult(boolean isok) {
			this.isok = isok;
		}
	}
	
	private static final long serialVersionUID = 1L;

	public abstract String getId();
	public abstract CheckResult check();
	public String getFormTitle() {
		return pageInfo.title;
	}
	
	public abstract void save() throws Exception;
	
	PageConfigureInfo pageInfo;

	public void load(PageConfigureInfo pageInfo) {		
		this.pageInfo = pageInfo;
	}
	
}
