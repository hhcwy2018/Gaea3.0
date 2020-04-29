package com.wh.gaea.plugin.aps.engine;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.wh.gaea.plugin.aps.dialog.ApsDefine;
import com.wh.gaea.plugin.aps.info.EngineInfo;
import com.wh.tools.DynamicLoadJar;
import com.wh.tools.FileHelp;

public class EngineQuery {
	public static File APS_ENGINE_PATH = new File(ApsDefine.APS_ROOT_DIR, "engine");

	public static EngineInfo[] getEngines() throws Exception {
		DynamicLoadJar.addClassLoaderUrl(new File[] { APS_ENGINE_PATH });
		List<EngineInfo> infos = new ArrayList<>();
		for (File file : APS_ENGINE_PATH.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileHelp.GetExt(f.getName()).equalsIgnoreCase("jar");
			}
		})) {
			EngineInfo info = new EngineInfo(file);
			infos.add(info);
		}

		return infos.size() == 0 ? null : infos.toArray(new EngineInfo[infos.size()]);
	}
}
