package com.wh.gaea.install.control;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alee.laf.desktoppane.WebInternalFrameUI;
import com.wh.gaea.install.form.BaseForm;
import com.wh.gaea.install.interfaces.PageConfigureInfo;

public class PageManager {
	public static class UIInfo {
		public BaseForm ui;
		public Class<?> uiClass;
		public Object createParams;

		public UIInfo() {
		}

		public <T extends BaseForm> UIInfo(Class<T> c) {
			this.uiClass = c;
		}

		public <T extends BaseForm> UIInfo(Class<T> c, Object ...createParams) {
			this.uiClass = c;
			this.createParams = createParams;
		}

		public JSONObject toJson() {
			JSONObject data = new JSONObject();
			data.put("class", uiClass.getName());
			if (createParams instanceof PageConfigureInfo)
				data.put("createParams",
						((PageConfigureInfo) createParams).toJson());
			return data;
		}

		public void fromJson(JSONObject data) throws Exception {
			String className = data.getString("class");
			uiClass = Class.forName(className);
			if (data.has("createParams"))
				createParams = new PageConfigureInfo(
						data.getJSONObject("createParams"));
			else {
				createParams = null;
			}
			ui = null;
		}
	}

	int index = -1;
	List<UIInfo> uiInfos = new ArrayList<>();

	public void add(UIInfo uiInfo) {
		uiInfos.add(uiInfo);
	}

	public UIInfo get(int index) {
		if (uiInfos.size() == 0)
			return null;

		if (index < 0)
			index = 0;

		if (index > uiInfos.size() - 1) {
			index = uiInfos.size() - 1;
		}

		return uiInfos.get(index);
	}

	public boolean isFirst() {
		return index <= 0;
	}

	public boolean isLast() {
		return index >= uiInfos.size() - 1;
	}

	public UIInfo current() {
		return uiInfos.get(index);
	}
	
	public UIInfo next() {
		if (uiInfos.size() == 0)
			return null;

		index++;

		if (index > uiInfos.size() - 1) {
			index = uiInfos.size() - 1;
		}

		return uiInfos.get(index);
	}

	public UIInfo prev() {
		if (uiInfos.size() == 0)
			return null;

		index--;

		if (index < 0)
			index = 0;

		return uiInfos.get(index);
	}

	public void save() throws Exception {
		if (index >= 0 && index < uiInfos.size()) {
			UIInfo uiInfo = uiInfos.get(index);
			uiInfo.ui.save();
		}
	}
	
	public static void setUI(JLabel label, JDesktopPane desktop, UIInfo uiInfo)
			throws Exception {
		if (uiInfo.ui == null) {
			if (uiInfo.createParams != null && uiInfo.createParams.getClass().isArray()) {
				Object[] objects = (Object[]) uiInfo.createParams;
				Class<?>[] cs = new Class[objects.length];
				for (int i = 0; i < cs.length; i++) {
					Class<?> c = objects[i].getClass();
					if (c.isAnonymousClass()) {
						c = c.getInterfaces()[0];
					}
					
					cs[i] = c;
				}
				uiInfo.ui = (BaseForm) uiInfo.uiClass.getDeclaredConstructor(cs)
						.newInstance(objects);
			} else
				uiInfo.ui = (BaseForm) uiInfo.uiClass.newInstance();
			if (uiInfo.createParams instanceof PageConfigureInfo)
				uiInfo.ui.load((PageConfigureInfo) uiInfo.createParams);
			else {
				uiInfo.ui.load(null);
			}
			uiInfo.ui.setClosable(false);
			uiInfo.ui.setBounds(0, 0, desktop.getWidth(), desktop.getHeight());
			WebInternalFrameUI ui = (WebInternalFrameUI) uiInfo.ui.getUI();
			ui.setNorthPane(null);

			uiInfo.ui.putClientProperty("JInternalFrame.isPalette",
					Boolean.TRUE);
			uiInfo.ui.getRootPane()
					.setBorder(BorderFactory.createEmptyBorder());
			ui.setWestPane(null);
			ui.setEastPane(null);
			ui.setSouthPane(null);
			desktop.add(uiInfo.ui);
			try {
				uiInfo.ui.setMaximum(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}

		}
		label.setText(uiInfo.ui.getFormTitle());
		uiInfo.ui.setVisible(true);
		uiInfo.ui.toFront();
		uiInfo.ui.requestFocus();
	}

	public void fromJson(JSONArray data) throws Exception {
		uiInfos.clear();
		for (Object object : data) {
			UIInfo uiInfo = new UIInfo();
			uiInfo.fromJson((JSONObject) object);
			uiInfos.add(uiInfo);
		}
	}

	public JSONArray toJson() {
		JSONArray data = new JSONArray();
		for (UIInfo uiInfo : uiInfos) {
			data.put(uiInfo.toJson());
		}
		return data;
	}
}
