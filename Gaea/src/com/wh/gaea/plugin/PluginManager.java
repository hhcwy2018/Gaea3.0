package com.wh.gaea.plugin;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.selector.IDataSourceSelector;
import com.wh.gaea.interfaces.selector.IExcelSelector;
import com.wh.gaea.interfaces.selector.IRoleSelector;
import com.wh.gaea.interfaces.selector.IWorkflowSelector;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.DynamicLoadJar;
import com.wh.tools.FileHelp;

public class PluginManager {
	Map<IGaeaPlugin, File> plugins = new ConcurrentHashMap<>();
	IGaeaDBPlugin dbPlugin;
	static File pluginPath = FileHelp.GetPath("plugins");
	
	List<IGaeaPlugin> sortPlugins = Collections.synchronizedList(new ArrayList<>());
	static {
		try {
			DynamicLoadJar.addClassLoaderUrl(new File[] { pluginPath });
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void load(JMenu root) throws Exception {
		File[] plugFiles = pluginPath.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && FileHelp.GetExt(pathname.getName()).equalsIgnoreCase("jar");
			}
		});

		if (plugFiles == null || plugFiles.length == 0)
			return;

		TreeMap<Integer, IGaeaPlugin> pluginSortMap = new TreeMap<>();
		
		for (File file : plugFiles) {
			String name = FileHelp.removeExt(file.getName());
			IGaeaPlugin plugin = DynamicLoadJar.instance("com.wh.gaea.plugin." + name);
			plugins.put(plugin, file);

			switch (plugin.getType()) {
			case ptDataSource:
				if (plugin instanceof IDataSourceSelector) {
					GlobalInstance.instance().setDataSourceSelector((IDataSourceSelector) plugin);
				}
				break;
			case ptDb:
				if (plugin instanceof IGaeaDBPlugin) {
					dbPlugin = (IGaeaDBPlugin) plugin;
				}
				break;
			case ptExcel:
				if (plugin instanceof IExcelSelector) {
					GlobalInstance.instance().setExcelSelector((IExcelSelector) plugin);
				}
				break;
			case ptRole:
				if (plugin instanceof IRoleSelector) {
					GlobalInstance.instance().setRoleSelector((IRoleSelector) plugin);
				}
				break;
			case ptWorkflow:
				if (plugin instanceof IWorkflowSelector) {
					GlobalInstance.instance().setWorkflowSelector((IWorkflowSelector) plugin);
				}
				break;
			case ptFunction:
			default:
				break;
			}

			int sort = plugin.getLoadOrder();
			if (pluginSortMap.containsKey(sort))
				sort = pluginSortMap.lastKey() + 1;
			pluginSortMap.put(sort, plugin);
		}

		sortPlugins.clear();
		sortPlugins.addAll(pluginSortMap.values());
		
		for (IGaeaPlugin plugin : pluginSortMap.values()) {
			plugin.setMenu(root);
			setClientProperty(plugin, plugin.getRootMenu(root));
		}
	}

	protected void setClientProperty(IGaeaPlugin plugin, JMenu menu) {
		menu.putClientProperty("IGaeaPlugin", plugin);
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem menuItem = menu.getItem(i);
			if (menuItem == null)
				continue;
			
			try {
				if (menuItem instanceof JMenu) {
					setClientProperty(plugin, (JMenu) menuItem);
				}else {
					menuItem.putClientProperty("IGaeaPlugin", plugin);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setDb() {
		if (dbPlugin != null)
			GlobalInstance.instance().getMainControl().setDB(dbPlugin.getDB());
	}

	static PluginManager pluginManager = new PluginManager();

	public static void init(JMenu root) {
		try {
			pluginManager.load(root);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public static void reset() {
		pluginManager.setDb();
		for (IGaeaPlugin gaeaPlugin : pluginManager.plugins.keySet()) {
			gaeaPlugin.reset();
		}
	}

	public static List<IGaeaPlugin> getPlugins(){
		return new ArrayList<>(pluginManager.sortPlugins);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getPlugin(Class<T> t) {
		for (IGaeaPlugin plugin : pluginManager.plugins.keySet()) {
			if (t == plugin.getClass())
				return (T) plugin;
		}

		return null;
	}

	public static IGaeaPlugin getPlugin(JComponent component) {
		return (IGaeaPlugin) component.getClientProperty("IGaeaPlugin");
	}
}
