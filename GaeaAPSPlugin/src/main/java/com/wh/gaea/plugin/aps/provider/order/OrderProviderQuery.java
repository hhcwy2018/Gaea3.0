package com.wh.gaea.plugin.aps.provider.order;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.wh.gaea.plugin.aps.dialog.ApsDefine;
import com.wh.gaea.plugin.aps.info.OrderProviderInfo;
import com.wh.tools.DynamicLoadJar;
import com.wh.tools.FileHelp;

public class OrderProviderQuery {
	public static File APS_ENGINE_PATH = new File(ApsDefine.APS_ROOT_DIR, "provider");

	public static OrderProviderInfo[] getInfos() throws Exception {

		DynamicLoadJar.addClassLoaderUrl(new File[] { APS_ENGINE_PATH });
		List<OrderProviderInfo> infos = new ArrayList<>();
		for (File file : APS_ENGINE_PATH.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileHelp.GetExt(f.getName()).equalsIgnoreCase("jar");
			}
		})) {
			OrderProviderInfo info = new OrderProviderInfo(file);
			infos.add(info);
		}

		return infos.toArray(new OrderProviderInfo[infos.size()]);
	}

}
