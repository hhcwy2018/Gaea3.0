package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.plugin.authorization.AuthorizationDialog;
import com.wh.gaea.plugin.authorization.GenRegisterFileDialog;
import com.wh.swing.tools.MsgHelper;

public class GaeaAuthorizationPlugin extends BaseGaeaPlugin implements IGaeaPlugin {

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/产品授权.png")));

		JMenuItem menu = new JMenuItem("授权管理");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/auth/授权管理.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					AuthorizationDialog.showDialog(GlobalInstance.instance().getMainControl());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		rootMenu.add(menu);

		menu = new JMenuItem("生成注册申请文件");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/auth/生成注册申请文件.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				GenRegisterFileDialog.showDialog();
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
		return PlugInType.ptAuth;
	}

	@Override
	protected String getMenuRootName() {
		return "产品授权";
	}

}
