package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.plugin.install.dialog.InstallConfigureDialog;
import com.wh.gaea.plugin.install.dialog.InstallerDefine;
import com.wh.gaea.selector.KeyValueSelector;
import com.wh.gaea.selector.KeyValueSelector.RowResult;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.JsonHelp;

public class GaeaInstallPlugin extends BaseGaeaPlugin implements IGaeaPlugin {

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/安装.png")));

		JMenuItem menu = new JMenuItem("安装配置");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/install/安装配置.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {

					File[] dirs = InstallerDefine.getProjectInstallPath().listFiles(new FileFilter() {

						@Override
						public boolean accept(File pathname) {
							return pathname.isDirectory();
						}
					});

					Object[][] datas = new Object[dirs.length][2];
					for (int i = 0; i < dirs.length; i++) {
						File dir = dirs[i];
						datas[i][0] = dir.getName();
						datas[i][1] = dir;
					}
					RowResult result = KeyValueSelector.showForOne(GlobalInstance.instance().getMainControl(), datas,
							new Object[] { "安装名称", "路径" });
					if (!result.isok)
						return;

					String engineName = (String) result.row[0];
					File engineFile = InstallerDefine.getInstallEngineFile(engineName);
					InstallConfigureInfo installInfo = new InstallConfigureInfo();
					if (engineFile.exists()) {
						JSONObject engineData = (JSONObject) JsonHelp.parseJson(engineFile, null);
						installInfo.fromJson(engineData);
					}else {
						installInfo.name = engineName;
					}
					InstallConfigureDialog.showDialog(installInfo);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		rootMenu.add(menu);

		menu = new JMenuItem("安装执行");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/install/安装执行.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

			}
		});
		rootMenu.add(menu);
	}

	@Override
	public void reset() {
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptFunction;
	}

	@Override
	protected String getMenuRootName() {
		return "安装";
	}

}
