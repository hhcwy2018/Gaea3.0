package com.wh.gaea.plugin.install.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.install.interfaces.ItemConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo.SaveType;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.plugin.install.dialog.InstallerDefine.InputType;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.swing.tools.dialog.WaitDialog;
import com.wh.swing.tools.dialog.WaitDialog.IProcess;
import com.wh.tools.FileHelp;
import com.wh.tools.IEnum;
import com.wh.tools.ImageUtils;
import com.wh.tools.JsonHelp;
import com.wh.tools.TextStreamHelp;
import com.wh.tools.ZipManager;

public class InstallConfigureDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	Map<JTable, PageConfigureInfo> pages = new HashMap<>();

	boolean isEditRow = false;
	boolean isEditPage = false;

	protected File rootPath() {
		return GlobalInstance.instance().getProjectBasePath();
	}

	protected String convertDate(Date dt) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(dt);
	}

	protected Date toDate(String dtString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return format.parse(dtString);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}
	}

	protected ItemConfigureInfo gridRowToConfigureItemInfo(JTable table,
			int row) throws IOException, ParseException {
		if (row == -1)
			return null;

		DefaultTableModel model = (DefaultTableModel) table.getModel();

		InputType inputType = (InputType) model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_INPUTTYPE);
		Class<?> c;

		switch (inputType) {
			case itBoolean :
				c = Boolean.class;
				break;
			case itByte :
				c = Byte.class;
				break;
			case itDate :
				c = Date.class;
				break;
			case itFloat :
				c = Float.class;
				break;
			case itInt :
				c = Integer.class;
				break;
			case itLong :
				c = Long.class;
				break;
			case itShort :
				c = Short.class;
				break;
			default :
				c = String.class;
				break;
		}

		ItemConfigureInfo info = new ItemConfigureInfo(c);
		info.title = (String) model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_TITLE);
		info.value = model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_VALUE);
		info.msg = (String) model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_MSG);
		info.allowNull = (boolean) model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_ALLOWNULL);
		info.regular = (String) model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_REGULAR);
		info.saveKey = (String) model.getValueAt(row,
				InstallerDefine.INSTALLER_GRID_SAVEKEY);

		return info;
	}

	protected void addRow(JTable table, ItemConfigureInfo info,
			boolean needScrollEnd, boolean needAdd)
			throws IOException, ParseException {
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		if (info == null)
			info = new ItemConfigureInfo(String.class);

		Object[] data = new Object[7];
		data[InstallerDefine.INSTALLER_GRID_TITLE] = info.title;
		data[InstallerDefine.INSTALLER_GRID_VALUE] = info.value;
		data[InstallerDefine.INSTALLER_GRID_MSG] = info.msg;
		data[InstallerDefine.INSTALLER_GRID_ALLOWNULL] = info.allowNull;
		data[InstallerDefine.INSTALLER_GRID_REGULAR] = info.regular;
		data[InstallerDefine.INSTALLER_GRID_SAVEKEY] = info.saveKey;
		data[InstallerDefine.INSTALLER_GRID_INPUTTYPE] = InputType
				.fromClass(info.getType());

		model.addRow(data);

		int row = model.getRowCount() - 1;
		if (needAdd) {
			PageConfigureInfo pageInfo = pages.get(table);
			pageInfo.add(gridRowToConfigureItemInfo(table, row));
		}
		if (needScrollEnd) {
			table.getSelectionModel().setSelectionInterval(row, row);
			table.scrollRectToVisible(table.getCellRect(row, 0, true));
		}
	}

	protected void removeRow(JTable table) {
		int row = table.getSelectedRow();
		if (row == -1)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的信息？",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(row);

		PageConfigureInfo pageInfo = pages.get(table);
		pageInfo.remove(row);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getViewText(JComponent component) {
		if (component instanceof JTextField) {
			JTextField field = (JTextField) component;
			String text = field.getText();
			if (text == null)
				return null;
			else {
				text = text.trim();
				if (text.isEmpty())
					return null;

				return (T) text;
			}
		} else if (component instanceof JSpinner) {
			JSpinner spinner = (JSpinner) component;
			SpinnerModel model = spinner.getModel();
			if (model instanceof SpinnerDateModel) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				SpinnerDateModel spinnerDateModel = (SpinnerDateModel) model;
				return (T) format.format(spinnerDateModel.getDate());
			} else if (model instanceof SpinnerNumberModel) {
				SpinnerNumberModel spinnerDateModel = (SpinnerNumberModel) model;
				return (T) spinnerDateModel.getValue();
			}
		} else if (component instanceof JCheckBox) {
			JCheckBox checkBox = (JCheckBox) component;
			return (T) (Boolean) checkBox.isSelected();
		} else if (component instanceof JComboBox) {
			JComboBox<InputType> comboBox = (JComboBox<InputType>) component;
			return (T) comboBox.getSelectedItem();
		}
		return null;
	}

	protected JTable getTableView() {
		JComponent component = (JComponent) tabsView.getSelectedComponent();
		if (component == null)
			return null;

		JScrollPane scrollPane = (JScrollPane) component;
		if (scrollPane.getViewport().getComponentCount() > 0)
			return (JTable) scrollPane.getViewport().getComponent(0);
		else {
			return null;
		}
	}

	protected PageConfigureInfo getPageInfo() {
		JTable table = getTableView();
		if (table == null)
			return null;

		return pages.get(table);
	}

	protected JTable addTab(PageConfigureInfo pageInfo)
			throws IOException, ParseException {
		if (pageInfo == null) {
			String title = MsgHelper.showInputDialog("请输入新页面的标题");
			if (title == null || title.isEmpty())
				return null;

			pageInfo = new PageConfigureInfo();
			pageInfo.title = title;
			installInfo.add(pageInfo);
		}

		JTable table = createTab(pageInfo);

		return table;
	}

	protected void removeTab() {
		JTable table = getTableView();
		if (table == null) {
			return;
		}

		if (MsgHelper.showConfirmDialog("是否删除当前页面配置？",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		tabsView.remove(tabsView.getSelectedIndex());
		PageConfigureInfo pageInfo = pages.remove(table);
		installInfo.remove(pageInfo);
	}

	protected JTable createTab(PageConfigureInfo info)
			throws IOException, ParseException {

		JScrollPane scrollPane = new JScrollPane();
		tabsView.add(info.title, scrollPane);

		JTable table = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;// 表格不允许被编辑
			}
		};

		table.setModel(new DefaultTableModel(new Object[][]{},
				new String[]{"\u6807\u9898", "\u7F3A\u7701\u503C",
						"\u5F02\u5E38\u6D88\u606F", "\u5141\u8BB8\u7A7A\u503C",
						"\u8F93\u5165\u7C7B\u578B",
						"\u68C0\u67E5\u8868\u8FBE\u5F0F",
						"\u4FDD\u5B58\u952E\u503C"}));
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setMaxWidth(75);
		table.getColumnModel().getColumn(3).setPreferredWidth(114);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setFillsViewportHeight(true);
		table.getSelectionModel()
				.addListSelectionListener(new ListSelectionListener() {
					int old = -1;

					@Override
					public void valueChanged(ListSelectionEvent e) {
						int row = table.getSelectedRow();
						if (row == -1)
							return;

						if (old != -1 && isEditRow) {
							if (MsgHelper.showConfirmDialog("项目数据已经改变，是否保存？",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								try {
									saveRow(table, old);
								} catch (Exception e1) {
									e1.printStackTrace();
									MsgHelper.showException(e1);
								}
							}
						}

						try {
							DefaultTableModel model = (DefaultTableModel) table
									.getModel();

							String title = (String) model.getValueAt(row,
									InstallerDefine.INSTALLER_GRID_TITLE);
							titleView.setText(title);
							valueView.setText((String) model.getValueAt(row,
									InstallerDefine.INSTALLER_GRID_VALUE));
							msgView.setText((String) model.getValueAt(row,
									InstallerDefine.INSTALLER_GRID_MSG));
							regularView.setText((String) model.getValueAt(row,
									InstallerDefine.INSTALLER_GRID_REGULAR));
							allownullView
									.setSelected((boolean) model.getValueAt(row,
											InstallerDefine.INSTALLER_GRID_ALLOWNULL));
							savekeyView.setText((String) model.getValueAt(row,
									InstallerDefine.INSTALLER_GRID_SAVEKEY));
							inputtypeView.setSelectedItem(model.getValueAt(row,
									InstallerDefine.INSTALLER_GRID_INPUTTYPE));

							isEditRow = false;

							old = row;
						} catch (Exception e2) {
							e2.printStackTrace();
						}

					}
				});

		pages.put(table, info);

		for (ItemConfigureInfo configureItemInfo : info) {
			addRow(table, configureItemInfo, false, false);
		}

		table.updateUI();

		scrollPane.setViewportView(table);
		tabsView.setSelectedComponent(scrollPane);

		return table;
	}

	protected void load() throws Exception {
		setIconView(installInfo);
		includegaearuntimeView.setSelected(installInfo.includeGaeaRuntime);
		includegaeaView.setSelected(installInfo.includeGaea);
		gaearuntimepathView.setText(installInfo.gaeaRuntimePath == null
				? "c:\\"
				: installInfo.gaeaRuntimePath.getAbsolutePath());
		installdirView.setText(installInfo.installDir == null
				? ""
				: installInfo.installDir.getAbsolutePath());
		publishpathView.setText(installInfo.publishDir == null
				? ""
				: installInfo.publishDir.getAbsolutePath());
		pages.clear();
		tabsView.removeAll();
		for (PageConfigureInfo pageInfo : installInfo) {
			JTable table = addTab(pageInfo);
			if (table == null)
				throw new Exception("添加页面失败！");

		}
	}

	protected void save() throws Exception {
		save(getTableView());
	}

	protected void save(JTable table) throws Exception {
		if (table == null)
			throw new Exception("请先选择一个页面！");

		PageConfigureInfo pageInfo = pages.get(table);
		pageInfo.saveFile = savefileView.getText();
		pageInfo.saveType = (SaveType) savetypeView.getSelectedItem();
		pageInfo.splitChar = splitView.getText();
		if (pageInfo.saveType == SaveType.stKeyValue
				&& (pageInfo.splitChar == null || pageInfo.splitChar.isEmpty()))
			throw new Exception(
					"当文件类型【" + SaveType.stKeyValue.getMsg() + "】时，必须设置【分隔符号】！");

		installInfo.includeGaea = includegaeaView.isSelected();
		installInfo.includeGaeaRuntime = includegaearuntimeView.isSelected();
		installInfo.gaeaRuntimePath = new File(gaearuntimepathView.getText());
		installInfo.installDir = new File(installdirView.getText());
		installInfo.publishDir = new File(publishpathView.getText());
		JsonHelp.saveJson(
				InstallerDefine.getInstallEngineFile(installInfo.name),
				installInfo.toJson(), null);

		isEditPage = false;
	}

	protected void saveRow(JTable table, int row) throws Exception {
		if (row == -1) {
			throw new Exception("请先选择一条授权记录！");
		}

		String title = getViewText(titleView);
		String value = getViewText(valueView);
		String msg = getViewText(msgView);
		String regular = getViewText(regularView);
		String savekey = getViewText(savekeyView);
		boolean used = getViewText(allownullView);
		InputType inputType = getViewText(inputtypeView);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setValueAt(title, row, InstallerDefine.INSTALLER_GRID_TITLE);
		model.setValueAt(value, row, InstallerDefine.INSTALLER_GRID_VALUE);
		model.setValueAt(msg, row, InstallerDefine.INSTALLER_GRID_MSG);
		model.setValueAt(regular, row, InstallerDefine.INSTALLER_GRID_REGULAR);
		model.setValueAt(savekey, row, InstallerDefine.INSTALLER_GRID_SAVEKEY);
		model.setValueAt(used, row, InstallerDefine.INSTALLER_GRID_ALLOWNULL);
		model.setValueAt(inputType, row,
				InstallerDefine.INSTALLER_GRID_INPUTTYPE);

		PageConfigureInfo pageInfo = pages.get(table);
		pageInfo.set(row, gridRowToConfigureItemInfo(table, row));

		table.updateUI();

		isEditRow = false;
	}

	protected void delete(File file) throws IOException {
		if (file.exists() && !file.delete())
			throw new IOException("删除文件【" + file.getAbsolutePath() + "】失败！");
	}

	protected DefaultTableModel makeDefaultTableModel(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] headers = new Object[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			headers[i] = model.getColumnName(i);
		}
		return new DefaultTableModel(new Object[][]{}, headers);
	}

	InstallConfigureInfo installInfo;

	protected File getInstallRoot() {
		return InstallerDefine.getInstallRootPath(installInfo.name);
	}

	protected File getInstallIconIconFile() {
		return new File(InstallerDefine.getInstallRootPath(installInfo.name),
				"icon.png");
	}

	protected File getInstallResourcePath() {
		return new File(getInstallRoot(), "resource");
	}

	protected File importDir() throws Exception {
		File dir = SwingTools.selectOpenDir(null, "导入目录", "请选择要导入的目录", null);
		if (dir == null)
			return null;

		File destPath = new File(getInstallResourcePath(), dir.getName());
		if (destPath.exists()) {
			if (MsgHelper.showConfirmDialog("目录【】已经存在，继续将删除所有已经存在的文件，是否继续？",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return null;

			FileHelp.delDir(destPath);
		}

		if (!destPath.mkdirs())
			throw new IOException(
					"建立目录【" + destPath.getAbsolutePath() + "】失败！");
		FileHelp.copyFilesTo(dir, destPath);

		return destPath;
	}

	protected void setIconView(InstallConfigureInfo info) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (info.iconName == null || info.iconName.isEmpty())
					return;

				File file = new File(getInstallRoot(), info.iconName);
				if (!file.exists())
					return;

				BufferedImage image;
				try {
					image = ImageUtils.loadImage(file);
					image = ImageUtils.zoomImage(image, 32, 32);
					iconView.setIcon(new ImageIcon(image));
					iconView.updateUI();
				} catch (Exception e) {
					e.printStackTrace();
					MsgHelper.showException(e);
				}
			}
		});
	}

	protected void packageGaeaRuntime(File destInstallDir,
			boolean includeGaeaRuntime) throws Exception {
		if (includeGaeaRuntime) {
			File xamppFile = GlobalInstance.instance()
					.getResourceFile("gaea_runtime", "xampp.zip");
			FileHelp.copyFileTo(xamppFile,
					new File(getInstallResourcePath(), xamppFile.getName()));
		}

		try (ZipManager zip = new ZipManager(
				new File(getInstallResourcePath(), "gaea.dat")
						.getAbsolutePath());) {
			zip.ZipFolder(destInstallDir.getAbsolutePath());
		}
	}

	protected void publish(File destDir, boolean includeGaea,
			boolean includeGaeaRuntime) throws Exception {

		File webPath = null;
		if (includeGaea) {
			webPath = GlobalInstance.instance().getPublishWebPath();
			if (webPath == null || !webPath.exists())
				throw new Exception(
						"设置的web根目录【" + webPath.getAbsolutePath() + "】不存在！");
			if (installInfo.gaeaRuntimePath == null
					|| !installInfo.gaeaRuntimePath.exists())
				throw new Exception("请先设置web根目录！");
		}

		File destInstallDir = new File(destDir, "java");
		File runtimeDir = GlobalInstance.instance()
				.getResourcePath(IEditorEnvironment.Install_Dir_Name);

		if (!destInstallDir.exists())
			if (!destInstallDir.mkdirs())
				throw new IOException(
						"建立目录【" + destInstallDir.getAbsolutePath() + "】失败！");

		if (includeGaea)
			packageGaeaRuntime(webPath, includeGaeaRuntime);

		if (!FileHelp.copyFilesTo(runtimeDir, destDir))
			throw new IOException("拷贝目录【" + runtimeDir.getAbsolutePath()
					+ "】 =》 【" + destDir.getAbsolutePath() + "】失败");

		File sourceResourceDir = getInstallResourcePath();
		if (!FileHelp.copyFilesTo(sourceResourceDir, destInstallDir))
			throw new IOException("拷贝目录【" + sourceResourceDir.getAbsolutePath()
					+ "】 =》 【" + destInstallDir.getAbsolutePath() + "】失败");

		File installConfigureFile = InstallerDefine
				.getInstallEngineFile(installInfo.name);
		File destInstallConfigureFile = new File(destInstallDir, "install.wh");
		FileHelp.copyFileTo(installConfigureFile, destInstallConfigureFile);

		File iconFile = getInstallIconIconFile();
		if (iconFile.exists()) {
			File destIconFile = new File(destInstallDir, "icon.png");
			FileHelp.copyFileTo(iconFile, destIconFile);
		}

		File runFile = new File(destInstallDir, "run.bat");
		TextStreamHelp.saveToFile(runFile, "echo off \n javaw -jar .\\GaeaInstaller.jar "
				+ destInstallConfigureFile.getName() + " -XX:+UseG1GC");

		File setupFile = new File(destDir, "setup_install_jdk.bat");
		TextStreamHelp.saveToFile(setupFile,
				"echo off \n JavaInstaller.exe jdk-8u231-windows-x64.exe run.bat \"\" \""
						+ destInstallDir.getAbsolutePath() + "\"");

		setupFile = new File(destDir, "setup_not_install_jdk.bat");
		
		String command = "echo off \n JavaInstaller.exe jdk-8u231-windows-x64.exe run.bat \"\" \""
				+ destInstallDir.getAbsolutePath() + "\" false";
		
		TextStreamHelp.saveToFile(setupFile,
				command);

	}

	protected File selectPublishDir() throws Exception {
		final File dir = SwingTools.selectOpenDir(null, "发布", "请选择要发布到的目录",
				null);
		if (dir == null)
			throw new Exception("用户取消了发布！");

		if (dir.exists() && dir.listFiles().length > 0) {
			if (MsgHelper.showConfirmDialog(
					"目录【" + dir.getAbsolutePath()
							+ "】存在，继续将清空此目录，所有存在文件将丢失，是否继续？",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				throw new Exception("用户取消了发布！");
			}

			FileHelp.delDir(dir);
		}

		publishpathView.setText(dir.getAbsolutePath());

		return dir;
	}

	public void publish(File dir) {
		if (installInfo.includeGaea) {
			try {
				GlobalInstance.instance().completePublish();
				installInfo.gaeaProjectName = GlobalInstance.instance()
						.getCurrentProjectName();
			} catch (Throwable e2) {
				e2.printStackTrace();
				MsgHelper.showException(e2);
				return;
			}
		}

		WaitDialog.Show("发布安装包", "正在创建安装系统，请等待", new IProcess() {

			@Override
			public boolean doProc(WaitDialog waitDialog)
					throws Exception {

				save();

				publish(dir, installInfo.includeGaea,
						installInfo.includeGaeaRuntime);

				return true;
			}

			@Override
			public void closed(boolean isok, Throwable e) {
				if (isok) {
					if (dir != null) {
						try {
							MsgHelper.showMessage("恭喜，发布安装程序成功");
							Desktop.getDesktop().open(dir);
						} catch (IOException e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
					}
				} else {
					MsgHelper.showException(e);
				}
			}
		}, null);
		try {
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}

	}
	
	public InstallConfigureDialog(InstallConfigureInfo installInfo)
			throws Exception {
		super();
		this.installInfo = installInfo;
		setTitle("配置安装【" + installInfo.name + "】");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(InstallConfigureDialog.class
						.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (isEditPage || isEditRow) {
					int hRet = MsgHelper.showConfirmDialog(
							"您当前有未保存的工作，是否保存后退出系统？", "退出系统",
							JOptionPane.YES_NO_CANCEL_OPTION);
					switch (hRet) {
						case JOptionPane.CANCEL_OPTION :
							return;
						case JOptionPane.YES_OPTION :
							JTable table = getTableView();
							try {
								if (table.getSelectedRow() != -1)
									saveRow(table, table.getSelectedRow());
								save();
							} catch (Exception e1) {
								e1.printStackTrace();
								MsgHelper.showException(e1);
							}
							break;
					}
				}
				setVisible(false);
			}

		});

		setBounds(100, 100, 1232, 808);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);

		toolBar.addSeparator(new Dimension());

		JLabel lblNewLabel_1 = new JLabel(" 页面：");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblNewLabel_1);

		JButton button = new JButton("新建");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addTab(null);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);

		JButton button_1 = new JButton("删除");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTab();
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_1);

		JButton button_7 = new JButton("保存");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					save();
					MsgHelper.showMessage("恭喜，保存成功！");
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		JButton button_9 = new JButton("编辑");
		button_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTable table = getTableView();
				if (table == null) {
					MsgHelper.showMessage("请先选择一个页面！");
					return;
				}

				PageConfigureInfo pageInfo = pages.get(table);

				String name = MsgHelper.showInputDialog("请输入新标题",
						pageInfo.title);
				if (name == null || name.isEmpty())
					return;

				pageInfo.title = name;

				try {
					save();
					tabsView.setTitleAt(tabsView.getSelectedIndex(), name);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_9);
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_7);

		toolBar.addSeparator();

		JButton button_2 = new JButton("添加");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addRow(getTableView(), null, true, true);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}

		});

		JLabel label_6 = new JLabel(" 项目： ");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_6);
		toolBar.add(button_2);

		JButton button_3 = new JButton("删除");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeRow(getTableView());
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		toolBar.addSeparator();

		JButton button_4 = new JButton("导入目录");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(importDir());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		JButton button_10 = new JButton("指令编辑");
		button_10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InstallCommandConfigureDialog.showDialog(installInfo);
			}
		});
		button_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_10);
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		JButton button_5 = new JButton("移除目录");
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		JLabel lblNewLabel_2 = new JLabel("  ");
		toolBar.add(lblNewLabel_2);

		iconView = new JLabel("");
		iconView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		iconView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				File file = SwingTools.selectOpenImageFile(null, null, null);
				if (file == null)
					return;

				try {
					File destFile = getInstallIconIconFile();

					BufferedImage image = ImageUtils.loadImage(file);
					image = ImageUtils.zoomImage(image, 128, 128);
					ImageUtils.saveImage(image, destFile);
					installInfo.iconName = destFile.getName();

					setIconView(installInfo);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		iconView.setMinimumSize(new Dimension(32, 32));
		iconView.setPreferredSize(new Dimension(32, 32));
		iconView.setMaximumSize(new Dimension(32, 32));
		iconView.setIcon(new ImageIcon(InstallConfigureDialog.class
				.getResource("/images/defaultIcon.png")));
		toolBar.add(iconView);

		JLabel label_8 = new JLabel("  ");
		toolBar.add(label_8);

		toolBar.addSeparator();

		JButton button_6 = new JButton(" 发布 ");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = installInfo.publishDir;
				try {
					if (dir == null) {
						dir = selectPublishDir();
						isEditPage = true;
					}
				} catch (Exception e3) {
					e3.printStackTrace();
					MsgHelper.showException(e3);
					return;
				}

				if (isEditPage || isEditRow) {
					if (MsgHelper.showConfirmDialog("当前安装脚本已经被改变，是否保存后发布？",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						try {
							save();
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
							return;
						}
					}
				}


				publish(dir);
			}
		});
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_6);

		JPanel splitPane = new JPanel();
		splitPane.setLayout(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		splitPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		tabsView = new JTabbedPane(JTabbedPane.TOP);
		tabsView.addChangeListener(new ChangeListener() {

			JTable old = null;

			@Override
			public void stateChanged(ChangeEvent e) {
				JTable table = getTableView();
				if (table == null)
					return;

				if (old != null && (isEditRow || isEditPage)) {
					if (MsgHelper.showConfirmDialog("数据已经修改，是否保存？",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						try {
							if (old.getSelectedRow() != -1)
								saveRow(old, old.getSelectedRow());
							save(old);
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
					}
				}
				PageConfigureInfo pageInfo = pages.get(table);
				savefileView.setText(pageInfo.saveFile);
				savetypeView.setSelectedItem(pageInfo.saveType);
				splitView.setText(pageInfo.splitChar);

				if (table.getRowCount() > 0) {
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().setSelectionInterval(0, 0);
				}
				old = table;
				isEditPage = false;
				isEditRow = false;
			}
		});

		panel_1.add(tabsView, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 300));
		panel.setMinimumSize(new Dimension(10, 300));
		splitPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("标题");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblNewLabel.setBounds(49, 13, 54, 15);
		panel.add(lblNewLabel);

		titleView = new JTextField();
		titleView.setToolTipText("配置项目的标题");
		titleView.setMinimumSize(new Dimension(6, 24));
		titleView.setPreferredSize(new Dimension(6, 24));
		titleView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditRow = true;
			}
		});
		titleView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		titleView.setBounds(113, 10, 382, 30);
		panel.add(titleView);
		titleView.setColumns(10);

		valueView = new JTextField();
		valueView.setToolTipText("配置项目的缺省值");
		valueView.setMinimumSize(new Dimension(6, 24));
		valueView.setPreferredSize(new Dimension(6, 24));
		valueView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditRow = true;
			}
		});
		valueView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		valueView.setColumns(10);
		valueView.setBounds(618, 10, 535, 30);
		panel.add(valueView);

		JLabel dlabel = new JLabel("缺省值");
		dlabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dlabel.setBounds(542, 13, 54, 15);
		panel.add(dlabel);

		JLabel label_3 = new JLabel("异常提示");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_3.setBounds(49, 49, 54, 15);
		panel.add(label_3);

		msgView = new JTextField();
		msgView.setToolTipText("当配置项目设置失败时向用户显示的提示，可以为空");
		msgView.setMinimumSize(new Dimension(6, 24));
		msgView.setPreferredSize(new Dimension(6, 24));
		msgView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditRow = true;
			}
		});
		msgView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		msgView.setColumns(10);
		msgView.setBounds(113, 45, 382, 30);
		panel.add(msgView);

		JLabel label_4 = new JLabel("录入类型");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_4.setBounds(542, 84, 54, 15);
		panel.add(label_4);

		allownullView = new JCheckBox("允许为空");
		allownullView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		allownullView.setBounds(113, 116, 100, 23);
		panel.add(allownullView);

		JLabel label = new JLabel("检查表达式");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(542, 50, 64, 15);
		panel.add(label);

		regularView = new JTextField();
		regularView.setToolTipText("用于检查用户输入值的格式是否正确，此值必须未有效的JAVA正则表达式");
		regularView.setMinimumSize(new Dimension(6, 24));
		regularView.setPreferredSize(new Dimension(6, 24));
		regularView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditRow = true;
			}
		});
		regularView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		regularView.setColumns(10);
		regularView.setBounds(618, 44, 535, 30);
		panel.add(regularView);

		JLabel label_1 = new JLabel("保存键值");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(49, 84, 54, 15);
		panel.add(label_1);

		savekeyView = new JTextField();
		savekeyView.setToolTipText("此配置项目保存到对象配置文件中的key路径，不同类型的配置文件key格式不同");
		savekeyView.setMinimumSize(new Dimension(6, 24));
		savekeyView.setPreferredSize(new Dimension(6, 24));
		savekeyView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditRow = true;
			}
		});
		savekeyView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		savekeyView.setColumns(10);
		savekeyView.setBounds(113, 80, 382, 30);
		panel.add(savekeyView);

		inputtypeView = new JComboBox<>(
				new DefaultComboBoxModel<>(IEnum.msgs(InputType.class)));
		inputtypeView.setToolTipText("决定生成的配置值录入框得类型");
		inputtypeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditRow = true;
			}
		});
		inputtypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		inputtypeView.setBounds(618, 81, 217, 30);
		panel.add(inputtypeView);

		JLabel label_2 = new JLabel("保存文件");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_2.setBounds(49, 169, 54, 15);
		panel.add(label_2);

		savefileView = new JTextField();
		savefileView.setEditable(false);
		savefileView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditPage = true;
			}
		});
		savefileView.setToolTipText("配置项要保存到得配置文件路径，此文件路径为相对于安装目录的路径");
		savefileView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		savefileView.setColumns(10);
		savefileView.setBounds(113, 165, 382, 30);
		panel.add(savefileView);

		JButton btnNewButton = new JButton("...");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingTools.onlyInitDir(true);
				try {
					String basePath = getInstallResourcePath()
							.getAbsolutePath();
					File file = SwingTools.selectOpenFile(null, basePath, null,
							null);
					if (file == null)
						return;

					savefileView.setText(
							file.getAbsolutePath().replace(basePath, ""));
					isEditPage = true;
				} finally {
					SwingTools.onlyInitDir(false);
				}
			}
		});
		btnNewButton.setBounds(503, 165, 31, 23);
		panel.add(btnNewButton);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(
				new MatteBorder(1, 0, 0, 0, (Color) new Color(0, 0, 0)));
		panel_2.setBounds(49, 145, 1104, 10);
		panel.add(panel_2);

		JLabel label_5 = new JLabel("文件类型");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_5.setBounds(542, 168, 54, 15);
		panel.add(label_5);

		savetypeView = new JComboBox<>(
				new DefaultComboBoxModel<>(IEnum.msgs(SaveType.class)));
		savetypeView.setToolTipText(
				"配置文件类型，YAML对应springboot得配置文件，keyvalue未键值对文件，xml为XML格式文件，json为json格式文件");
		savetypeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditPage = true;
				splitView.setEnabled((SaveType) savetypeView
						.getSelectedItem() == SaveType.stKeyValue);
			}
		});
		savetypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		savetypeView.setBounds(618, 165, 217, 30);
		panel.add(savetypeView);

		JLabel label_7 = new JLabel("分割符号");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_7.setBounds(854, 169, 54, 15);
		panel.add(label_7);

		splitView = new JTextField();
		splitView.setToolTipText("仅用于keyvalue类型文件，用于指定key和value得分隔符号");
		splitView.setEnabled(false);
		splitView.setText("=");
		splitView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		splitView.setColumns(10);
		splitView.setBounds(918, 166, 54, 30);
		panel.add(splitView);

		includegaearuntimeView = new JCheckBox("包含Gaea运行时");
		includegaearuntimeView.setToolTipText("如果选定，则安装文件会包含运行Gaea所必要得运行环境文件");
		includegaearuntimeView.setBounds(49, 212, 115, 25);
		panel.add(includegaearuntimeView);
		includegaearuntimeView.setSelected(true);
		includegaearuntimeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		includegaeaView = new JCheckBox("包含当前Gaea项目");
		includegaeaView.setToolTipText("如果选中，则安装文件会自动包含Gaea设计器中打开得当前项目");
		includegaeaView.setBounds(198, 212, 127, 25);
		panel.add(includegaeaView);
		includegaeaView.setSelected(true);
		includegaeaView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JLabel lblNewLabel_3 = new JLabel("框架安装路径");
		lblNewLabel_3.setBounds(542, 216, 80, 17);
		panel.add(lblNewLabel_3);
		lblNewLabel_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		gaearuntimepathView = new JTextField();
		gaearuntimepathView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditPage = true;
			}
		});
		gaearuntimepathView.setToolTipText("Gaea运行时得安装目录，一般不要修改");
		gaearuntimepathView.setMaximumSize(new Dimension(2147483647, 24));
		gaearuntimepathView.setMinimumSize(new Dimension(6, 24));
		gaearuntimepathView.setPreferredSize(new Dimension(6, 24));
		gaearuntimepathView.setBounds(618, 212, 535, 30);
		panel.add(gaearuntimepathView);
		gaearuntimepathView.setText("c:\\");
		gaearuntimepathView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		gaearuntimepathView.setColumns(10);

		JLabel label_9 = new JLabel("默认安装路径");
		label_9.setBounds(542, 258, 80, 17);
		panel.add(label_9);
		label_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		installdirView = new JTextField();
		installdirView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEditPage = true;
			}
		});
		installdirView.setToolTipText("用于设置用户安装得默认路径，如果安装类型为静默安装必须设置此项目");
		installdirView.setMaximumSize(new Dimension(2147483647, 24));
		installdirView.setMinimumSize(new Dimension(6, 24));
		installdirView.setPreferredSize(new Dimension(6, 24));
		installdirView.setBounds(618, 255, 535, 30);
		panel.add(installdirView);
		installdirView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		installdirView.setColumns(10);

		JLabel label_10 = new JLabel("发布路径");
		label_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_10.setBounds(49, 258, 54, 17);
		panel.add(label_10);

		publishpathView = new JTextField();
		publishpathView.setToolTipText("此安装系统得发布路径");
		publishpathView.setEditable(false);
		publishpathView.setText("x:\\new");
		publishpathView.setPreferredSize(new Dimension(6, 24));
		publishpathView.setMinimumSize(new Dimension(6, 24));
		publishpathView.setMaximumSize(new Dimension(2147483647, 24));
		publishpathView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		publishpathView.setColumns(10);
		publishpathView.setBounds(113, 255, 382, 30);
		panel.add(publishpathView);

		JButton button_8 = new JButton("...");
		button_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					selectPublishDir();
					isEditPage = true;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		button_8.setBounds(503, 255, 31, 23);
		panel.add(button_8);

		setLocationRelativeTo(null);

		load();
	}

	private JTextField titleView;
	private JTextField valueView;
	private JTextField msgView;
	private JCheckBox allownullView;
	private JTextField regularView;
	private JTextField savekeyView;
	private JComboBox<InputType> inputtypeView;
	private JTextField savefileView;
	private JComboBox<SaveType> savetypeView;
	private JTabbedPane tabsView;
	private JTextField splitView;
	private JLabel iconView;
	private JCheckBox includegaeaView;
	private JCheckBox includegaearuntimeView;
	private JTextField gaearuntimepathView;
	private JTextField installdirView;
	private JTextField publishpathView;

	public static void showDialog(InstallConfigureInfo installInfo) {
		InstallConfigureDialog dialog;
		try {
			dialog = new InstallConfigureDialog(installInfo);
			dialog.setModal(true);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}
}