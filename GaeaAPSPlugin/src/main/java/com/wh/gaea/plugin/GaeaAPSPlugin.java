package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.dialog.APSConfigureDialog;
import com.wh.gaea.plugin.aps.dialog.APSDialog;
import com.wh.swing.tools.MsgHelper;

public class GaeaAPSPlugin extends BaseGaeaPlugin implements IGaeaPlugin {

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/aps菜单.png")));
		
		JMenuItem configDBMenuItem = new JMenuItem("设置配置库名称");
		configDBMenuItem.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/aps/APS设置配置库.png")));
		configDBMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				String name = MsgHelper.showInputDialog("请输入配置库的名称", "");
				if (name == null || name.isEmpty())
					return;
				
				try {
					APSConfigure.setDBConfigDBName(name);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		rootMenu.add(configDBMenuItem);

		JMenuItem apsConfigMenuItem = new JMenuItem("排程配置");
		apsConfigMenuItem.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/aps/APS配置.png")));
		apsConfigMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					APSConfigureDialog.show(GlobalInstance.instance().getMainControl());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		rootMenu.add(apsConfigMenuItem);

		JMenuItem apsRunMenuItem = new JMenuItem("运行");
		apsRunMenuItem.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/aps/APS运行.png")));
		apsRunMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				APSDialog.showDialog();
			}
		});
		rootMenu.add(apsRunMenuItem);
	}

	@Override
	public void reset() {

	}

	@Override
	public int getLoadOrder() {
		return 1;
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptDb;
	}

	@Override
	protected String getMenuRootName() {
		return "APS";
	}

}
