package com.wh.gaea.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;

import com.wh.gaea.control.CreateAppInfo;
import com.wh.gaea.control.MenuItemInfo;
import com.wh.gaea.control.MenuItemInfo.IGetTreeKey;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.swing.tools.tree.TreeHelp;
import com.wh.swing.tools.tree.checknode.CheckBoxNode;
import com.wh.swing.tools.tree.checknode.CheckBoxNode.ISelection;
import com.wh.swing.tools.tree.checknode.CheckBoxNodeConfig;
import com.wh.swing.tools.tree.checknode.TreeCheckBoxNodeRender;
import com.wh.swing.tools.tree.checknode.TreeCheckBoxNodeRender.IGetIcon;

public class CreateClientAppDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField content;

	boolean isok = false;
	boolean isFile = true;

	private JTextField saveEditor;
	private JTree menuTree;
	private JTree pluginTree;
	private JCheckBox includeuiView;

	/**
	 * Create the dialog.
	 * 
	 * @throws Exception
	 */
	public CreateClientAppDialog(JMenuBar menuBar, File file) {
		createAppInfo = new CreateAppInfo(file);

		setTitle("生成Gaea终端");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(CreateClientAppDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1064, 689);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		contentPanel.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		JLabel label = new JLabel(" 程序名称 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(label, BorderLayout.WEST);
		content = new JTextField();
		content.setText("Gaea终端");
		content.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		content.setPreferredSize(new Dimension(200, 21));
		content.setMinimumSize(new Dimension(200, 21));
		panel.add(content);
		content.setColumns(10);
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		includeuiView = new JCheckBox("包含UI设计器");
		panel_1.add(includeuiView);
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));
		panel = new JPanel();
		buttonPane.add(panel, BorderLayout.EAST);
		JButton button = new JButton("选择目录");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = saveEditor.getText();

				File file = SwingTools.selectSaveDir(null, "选择目录", "请选择你要保存的目录位置", text);
				if (file == null)
					return;
				saveEditor.setText(file.getAbsolutePath());
			}
		});
		panel.add(button);
		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveEditor.getText().isEmpty()) {
					MsgHelper.showMessage("请选择保存文件！");
					return;
				}
				if (content.getText().isEmpty()) {
					MsgHelper.showMessage("请填写程序名称！");
					return;
				}
				isok = true;
				setVisible(false);
			}
		});
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		label = new JLabel(" 输出目录 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonPane.add(label, BorderLayout.WEST);
		saveEditor = new JTextField();
		saveEditor.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		saveEditor.setEnabled(false);
		buttonPane.add(saveEditor, BorderLayout.CENTER);
		saveEditor.setColumns(10);
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPanel.add(splitPane, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);

		pluginTree = new JTree();
		pluginTree.setRootVisible(false);
		pluginTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(pluginTree);
		scrollPane = new JScrollPane();
		splitPane.setRightComponent(scrollPane);
		menuTree = new JTree();
		menuTree.setRootVisible(false);
		menuTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(menuTree);

		initMenuBar(menuBar, menuTree, false);

		initMenuBar(menuBar, pluginTree, true);

		setLocationRelativeTo(null);
	}

	protected String getMenuTitle(Object item) {
		if (item instanceof JPopupMenu) {
			JPopupMenu menu = (JPopupMenu) item;
			return menu.getLabel();
		} else if (item instanceof JMenuItem) {
			JMenuItem menu = (JMenuItem) item;
			return menu.getText();
		} else {
			return null;
		}
	}

	protected void initMenu(Object menuElement, CheckBoxNode parent, String[] parents, boolean isPlugin) {

		boolean isMenuBar = menuElement instanceof JMenuBar;
		int count = isMenuBar ? ((JMenuBar) menuElement).getMenuCount() : ((JMenu) menuElement).getItemCount();
		for (int i = 0; i < count; i++) {
			JMenuItem item = isMenuBar ? ((JMenuBar) menuElement).getMenu(i) : ((JMenu) menuElement).getItem(i);
			if (item == null)
				continue;

			String title = getMenuTitle(item);
			if (title == null || title.isEmpty())
				continue;

			if (item instanceof JMenu) {
				boolean isPluginMenu = false;
				isPluginMenu = title.equalsIgnoreCase("插件")
						|| parents != null && Arrays.asList(parents).indexOf("插件") != -1;
				if (isPlugin) {
					if (!isPluginMenu)
						continue;
				} else {
					if (isPluginMenu)
						continue;
				}

			}

			MenuItemInfo info = new MenuItemInfo();
			info.title = title;
			info.icon = item.getIcon();
			info.parents = parents == null ? null : Arrays.copyOf(parents, parents.length);

			CheckBoxNode childNode = new CheckBoxNode(info);
			parent.add(childNode);

			if (item instanceof JMenu) {
				String[] childParents = new String[parents == null ? 1 : parents.length + 1];
				for (int j = 0; j < childParents.length - 1; j++) {
					childParents[j] = parents[j];
				}
				childParents[childParents.length - 1] = info.title;
				initMenu(item, childNode, childParents, isPlugin);
			}
		}
	}

	protected void initMenuBar(JMenuBar menuBar, JTree tree, boolean isPlugin) {
		MenuItemInfo info = new MenuItemInfo();
		info.title = isPlugin ? "插件" : "Gaea主菜单";
		info.parents = null;

		DefaultTreeModel model = new DefaultTreeModel(new CheckBoxNode(info));

		initMenu(menuBar, (CheckBoxNode) model.getRoot(), null, isPlugin);
		tree.setCellRenderer(new TreeCheckBoxNodeRender());

		tree.setModel(model);

		CheckBoxNodeConfig.config(tree, new ISelection() {

			@Override
			public void onSelected(CheckBoxNode selectNode) {
				selectNode.autoCheckChilds = true;
				selectNode.setSelected(selectNode.isSelected());
			}
		}, new IGetIcon() {

			@Override
			public Icon onIcon(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {

				MenuItemInfo info = (MenuItemInfo) ((DefaultMutableTreeNode) value).getUserObject();
				return info.icon;
			}
		});
	}

	protected JSONArray saveTree(DefaultTreeModel model, DefaultMutableTreeNode parent, JSONArray data) {
		for (int i = 0; i < model.getChildCount(parent); i++) {
			CheckBoxNode child = (CheckBoxNode) model.getChild(parent, i);
			if (child.isSelected()) {
				MenuItemInfo info = (MenuItemInfo) child.getUserObject();
				data.put(info.toJson());
			}
			if (child.getChildCount() > 0)
				saveTree(model, child, data);
		}

		return data;
	}

	CreateAppInfo createAppInfo;

	public interface ISetVisible<T> {
		void onVisible(T control, boolean visible);
	}

	public static <T> void setCheck(Map<String, MenuItemInfo> infoMap, String controlKey, T control,
			ISetVisible<T> setVisible) {
		boolean visible = infoMap.containsKey(controlKey);
		setVisible.onVisible(control, visible);
	}

	protected String getKey(DefaultTreeModel model, DefaultMutableTreeNode node) {
		return MenuItemInfo.getKey(node, new IGetTreeKey<DefaultMutableTreeNode>() {

			@Override
			public String getKey(DefaultMutableTreeNode node) {
				return ((MenuItemInfo) node.getUserObject()).title;
			}

			@Override
			public DefaultMutableTreeNode getParent(DefaultMutableTreeNode node) {
				return (DefaultMutableTreeNode) node.getParent();
			}

			@Override
			public boolean checkRoot(DefaultMutableTreeNode node) {
				return node == model.getRoot();
			}
		});
	}

	protected void loadTree(DefaultTreeModel model, DefaultMutableTreeNode parent, JSONArray data) {
		Map<String, MenuItemInfo> infoMap = MenuItemInfo.jsonarrayToMap(data);

		for (int i = 0; i < model.getChildCount(parent); i++) {
			CheckBoxNode child = (CheckBoxNode) model.getChild(parent, i);
			setCheck(infoMap, getKey(model, child), child, new ISetVisible<CheckBoxNode>() {

				@Override
				public void onVisible(CheckBoxNode control, boolean visible) {
					control.setSelected(visible);
				}
			});
			if (child.getChildCount() > 0)
				loadTree(model, child, data);
		}

	}

	protected void load() throws Exception {
		createAppInfo.load();
		content.setText(createAppInfo.title);
		includeuiView.setSelected(createAppInfo.includeUI);
		saveEditor.setText(createAppInfo.publishDir);
		loadTree((DefaultTreeModel) menuTree.getModel(), (DefaultMutableTreeNode) menuTree.getModel().getRoot(),
				createAppInfo.menus);
		loadTree((DefaultTreeModel) pluginTree.getModel(), (DefaultMutableTreeNode) pluginTree.getModel().getRoot(),
				createAppInfo.plugins);

		if (menuTree.getModel().getRoot() != null)
			TreeHelp.expandOrCollapse(menuTree, (DefaultMutableTreeNode) menuTree.getModel().getRoot(), true);
		if (pluginTree.getModel().getRoot() != null)
			TreeHelp.expandOrCollapse(pluginTree, (DefaultMutableTreeNode) pluginTree.getModel().getRoot(), true);
	}

	protected void save() throws Exception {
		createAppInfo.reset();
		createAppInfo.title = content.getText();
		createAppInfo.includeUI = includeuiView.isSelected();
		createAppInfo.publishDir = saveEditor.getText();
		saveTree((DefaultTreeModel) pluginTree.getModel(), (DefaultMutableTreeNode) menuTree.getModel().getRoot(),
				createAppInfo.menus);
		saveTree((DefaultTreeModel) pluginTree.getModel(), (DefaultMutableTreeNode) pluginTree.getModel().getRoot(),
				createAppInfo.plugins);
		createAppInfo.save();
	}

	public static void showDialog(JMenuBar menuBar, File file) throws Exception {
		CreateClientAppDialog dialog = new CreateClientAppDialog(menuBar, file);
		dialog.load();
		dialog.setModal(true);
		dialog.setVisible(true);
		if (dialog.isok)
			dialog.save();

		dialog.dispose();
	}

}
