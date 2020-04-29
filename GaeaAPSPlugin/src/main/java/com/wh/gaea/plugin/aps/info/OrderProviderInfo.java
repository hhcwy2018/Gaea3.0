package com.wh.gaea.plugin.aps.info;

import java.io.File;

import com.wh.gaea.plugin.aps.interfaces.IOrderProvider;
import com.wh.gaea.plugin.aps.provider.order.OrderProviderQuery;
import com.wh.tools.DynamicLoadJar;
import com.wh.tools.FileHelp;

public class OrderProviderInfo {
	public String name;
	public File providerFile;
	protected IOrderProvider provider;

	public OrderProviderInfo() {
	}

	public OrderProviderInfo(String name) {
		this.name = name;
		providerFile = new File(OrderProviderQuery.APS_ENGINE_PATH, name + ".jar");
	}

	public OrderProviderInfo(File file) {
		this.name = FileHelp.removeExt(file.getName());
		providerFile = file;
	}

	@Override
	public String toString() {
		try {
			getProvider();
			return provider.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return name;
		}
	}

	@SuppressWarnings("unchecked")
	public IOrderProvider getProvider() throws Exception {
		if (provider == null)
			provider = DynamicLoadJar.instance("com.wh.gaea.aps.order.provider." + name);
		return provider;
	}
}