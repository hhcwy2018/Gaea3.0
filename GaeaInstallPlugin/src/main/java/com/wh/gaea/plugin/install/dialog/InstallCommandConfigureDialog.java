package com.wh.gaea.plugin.install.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.install.control.CommandRunner.CommandType;
import com.wh.gaea.install.control.CommandRunner.ConfigureCommandIndicate;
import com.wh.gaea.install.control.CommandRunner.CopyCommandIndicate;
import com.wh.gaea.install.control.CommandRunner.DeleteCommandIndicate;
import com.wh.gaea.install.control.CommandRunner.IIndicate;
import com.wh.gaea.install.control.CommandRunner.RunCommandIndicate;
import com.wh.gaea.install.interfaces.CommandConfigureInfo;
import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;
import com.wh.gaea.plugin.install.dialog.InstallerDefine.InputType;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.IEnum;

public class InstallCommandConfigureDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	boolean isEditRow = false;

	protected File rootPath() {
		return GlobalInstance.instance().getProjectBasePath();
	}

	protected File getInstallRoot() {
		return InstallerDefine.getInstallRootPath(installInfo.name);
	}

	protected File getInstallResourcePath() {
		return new File(getInstallRoot(), "resource");
	}

	protected CommandConfigureInfo gridRowToCommandConfigureInfo(int row)
			throws IOException, ParseException {
		if (row == -1)
			return null;

		DefaultTableModel model = (DefaultTableModel) commandsView.getModel();

		CommandType commandType = (CommandType) model.getValueAt(row,
				InstallerDefine.INSTALLER_COMMAND_COMMANDTYPE);
		IIndicate commandIndicate = (IIndicate) model.getValueAt(row,
				InstallerDefine.INSTALLER_COMMAND_COMMANDINDICATE);
		CommandConfigureInfo info = new CommandConfigureInfo(commandType,
				commandIndicate);
		info.datas = (JSONArray) model.getValueAt(row,
				InstallerDefine.INSTALLER_COMMAND_ARGS);
		info.checkResult = (boolean) model.getValueAt(row,
				InstallerDefine.INSTALLER_COMMAND_CHECKRESULT);
		info.waitTime = (int) model.getValueAt(row,
				InstallerDefine.INSTALLER_COMMAND_WAITTIME);
		
		return info;
	}

	protected void addRow(CommandConfigureInfo info, boolean needScrollEnd)
			throws IOException, ParseException {
		DefaultTableModel model = (DefaultTableModel) commandsView.getModel();

		if (info == null)
			info = new CommandConfigureInfo(CommandType.ctConfigure,
					ConfigureCommandIndicate.ciFailAndFail);

		Object[] data = new Object[5];
		data[InstallerDefine.INSTALLER_COMMAND_COMMANDTYPE] = info
				.getCommandType();
		data[InstallerDefine.INSTALLER_COMMAND_COMMANDINDICATE] = info.commandIndicate;
		data[InstallerDefine.INSTALLER_COMMAND_ARGS] = info.datas;
		data[InstallerDefine.INSTALLER_COMMAND_CHECKRESULT] = info.checkResult;
		data[InstallerDefine.INSTALLER_COMMAND_WAITTIME] = info.waitTime;
		model.addRow(data);

		int row = model.getRowCount() - 1;
		if (needScrollEnd) {
			commandsView.getSelectionModel().setSelectionInterval(row, row);
			commandsView.scrollRectToVisible(
					commandsView.getCellRect(row, 0, true));
		}
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
		} else if (component instanceof JTable) {
			DefaultTableModel model = (DefaultTableModel) ((JTable) component)
					.getModel();
			JSONArray data = new JSONArray();
			for (int i = 0; i < model.getRowCount(); i++) {
				Object obj = model.getValueAt(i, 0);
				if (obj instanceof PageConfigureInfo) {
					obj = ((PageConfigureInfo) obj).toJson();
				}

				data.put(obj);
			}
			return (T) data;
		}
		return null;
	}

	protected JTable getTableView() {
		return commandsView;
	}

	protected void load() throws Exception {
		for (CommandConfigureInfo info : installInfo.commands) {
			addRow(info, false);
		}
	}

	protected void saveRow() throws Exception {
		saveRow(commandsView.getSelectedRow());
	}

	protected void saveRow(int row) throws Exception {
		if (row == -1) {
			MsgHelper.showMessage("请先选择一条授权记录！");
			return;
		}

		CommandType commandType = getViewText(commandtypeView);
		Enum<?> indicate = getViewText(commandindicateView);
		JSONArray args = getViewText(argsView);
		boolean checkResult = checkresultView.isSelected();
		int waitTime = (int) waittimeView.getValue();

		DefaultTableModel model = (DefaultTableModel) commandsView.getModel();
		model.setValueAt(commandType, row,
				InstallerDefine.INSTALLER_COMMAND_COMMANDTYPE);
		model.setValueAt(indicate, row,
				InstallerDefine.INSTALLER_COMMAND_COMMANDINDICATE);
		model.setValueAt(args, row, InstallerDefine.INSTALLER_COMMAND_ARGS);
		model.setValueAt(checkResult, row, InstallerDefine.INSTALLER_COMMAND_CHECKRESULT);
		model.setValueAt(waitTime, row, InstallerDefine.INSTALLER_COMMAND_WAITTIME);

		if (row > installInfo.commands.size() - 1) {
			installInfo.commands.add(gridRowToCommandConfigureInfo(row));
		} else {
			installInfo.commands.set(row, gridRowToCommandConfigureInfo(row));
		}

		commandsView.updateUI();

		isEditRow = false;
	}

	InstallConfigureInfo installInfo;

	protected void removeRow() {
		int row = commandsView.getSelectedRow();
		if (row == -1)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的信息？",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		DefaultTableModel model = (DefaultTableModel) commandsView.getModel();
		model.removeRow(row);

		installInfo.commands.remove(row);
	}

	protected void addFileOrDir(String title, boolean isDir) {
		SwingTools.onlyInitDir(true);
		try {
			String basePath = getInstallResourcePath().getAbsolutePath();
			File file;

			if (isDir)
				file = SwingTools.selectOpenDir(null, title, "请选择目录", basePath);
			else
				file = SwingTools.selectOpenFile(title, "请选择文件", null, basePath,
						null, null);
			if (file == null)
				return;

			DefaultTableModel model = (DefaultTableModel) argsView.getModel();
			model.addRow(
					new Object[]{file.getAbsolutePath().replace(basePath, "")});
			argsView.updateUI();
			isEditRow = true;
		} finally {
			SwingTools.onlyInitDir(false);
		}
	}

	protected DefaultTableModel makeDefaultTableModel(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] headers = new Object[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			headers[i] = model.getColumnName(i);
		}
		return new DefaultTableModel(new Object[][]{}, headers);
	}

	protected void updateArgsModel() {
		CommandType commandType = (CommandType) commandtypeView
				.getSelectedItem();
		DefaultTableModel model = (DefaultTableModel) argsView.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			String memo = (String) model.getValueAt(i, 1);

			switch (commandType) {
				case ctConfigure :
					break;
				case ctCopyDir :
					memo = i == 0 ? "源目录" : "目标目录";
					break;
				case ctCopyFile :
					memo = i == 0 ? "源文件" : "目标文件";
					break;
				case ctDelDir :
					memo = "目标目录";
				case ctDelFile :
					memo = "目标文件";
					break;
				case ctRun :
					break;
			}

			model.setValueAt(memo, i, 1);
		}
	}

	protected void addConfigureItem() {
		PageConfigureInfo info = InstallItemConfigureDialog
				.showDialog(installInfo);
		if (info == null)
			return;

		DefaultTableModel model = (DefaultTableModel) argsView.getModel();
		model.addRow(new Object[]{info, info.saveFile});
		isEditRow = true;
	}

	protected void moveRow(JTable table, boolean isDown) {
		int row = table.getSelectedRow();
		if (row == -1)
			return;

		DefaultTableModel model = (DefaultTableModel) table.getModel();

		int newRow = row;
		if (isDown) {
			if (row == table.getRowCount() - 1)
				return;

			newRow = row + 1;
		} else {
			if (row == 0)
				return;

			newRow = row - 1;
		}
		model.moveRow(row, row, newRow);

		table.getSelectionModel().setSelectionInterval(newRow, newRow);
		table.updateUI();
	}

	public InstallCommandConfigureDialog(InstallConfigureInfo installInfo)
			throws Exception {
		super();
		this.installInfo = installInfo;
		setTitle("安装指令编辑");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(InstallCommandConfigureDialog.class
						.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (isEditRow) {
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
									saveRow(table.getSelectedRow());
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

		setBounds(100, 100, 1232, 790);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);

		toolBar.addSeparator(new Dimension());

		JLabel lblNewLabel_1 = new JLabel(" ");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblNewLabel_1);

		JButton button = new JButton("新建");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addRow(new CommandConfigureInfo(CommandType.ctConfigure,
							ConfigureCommandIndicate.ciFailAndFail), true);
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
				removeRow();
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_1);

		JButton button_7 = new JButton("保存");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveRow();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_7);

		toolBar.addSeparator();

		JButton button_3 = new JButton("");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRow(commandsView, false);
			}
		});
		button_3.setIcon(new ImageIcon(InstallCommandConfigureDialog.class
				.getResource("/images/arrow_up_48.png")));
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		JButton button_4 = new JButton("");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRow(commandsView, true);
			}
		});
		button_4.setIcon(new ImageIcon(InstallCommandConfigureDialog.class
				.getResource("/images/arrow_down_48.png")));
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		JPanel splitPane = new JPanel();
		splitPane.setLayout(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		splitPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		commandsView = new JTable();
		commandsView.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"\u6307\u4EE4", "\u6307\u4EE4\u9009\u9879", "\u53C2\u6570\u5217\u8868", "\u68C0\u67E5\u8FD4\u56DE\u7ED3\u679C", "\u7B49\u5F85\u8D85\u65F6"
			}
		));
		commandsView.getColumnModel().getColumn(1).setPreferredWidth(100);
		commandsView.getColumnModel().getColumn(1).setMaxWidth(100);
		commandsView.getColumnModel().getColumn(2).setMaxWidth(75);
		commandsView.getColumnModel().getColumn(3).setPreferredWidth(90);
		commandsView.getColumnModel().getColumn(3).setMaxWidth(90);
		commandsView.getColumnModel().getColumn(4).setMaxWidth(75);
		commandsView.getSelectionModel()
				.addListSelectionListener(new ListSelectionListener() {
					int old = -1;

					@Override
					public void valueChanged(ListSelectionEvent e) {
						int row = commandsView.getSelectedRow();
						if (row == -1)
							return;

						if (old != -1 && isEditRow) {
							if (MsgHelper.showConfirmDialog("项目数据已经改变，是否保存？",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								try {
									saveRow(old);
								} catch (Exception e1) {
									e1.printStackTrace();
									MsgHelper.showException(e1);
								}
							}
						}

						try {
							DefaultTableModel model = (DefaultTableModel) commandsView
									.getModel();

							CommandType commandType = (CommandType) model
									.getValueAt(row,
											InstallerDefine.INSTALLER_COMMAND_COMMANDTYPE);
							commandtypeView.setSelectedItem(commandType);
							commandindicateView.setSelectedItem(
									(Enum<?>) model.getValueAt(row,
											InstallerDefine.INSTALLER_GRID_VALUE));
							JSONArray args = (JSONArray) model.getValueAt(row,
									InstallerDefine.INSTALLER_COMMAND_ARGS);
							
							
							if (args == null)
								args = new JSONArray();

							DefaultTableModel argsModel = makeDefaultTableModel(
									argsView);
							for (Object arg : args) {
								Object[] rowData = new Object[2];
								if (commandType == CommandType.ctConfigure) {
									rowData[0] = installInfo.get(new PageConfigureInfo(
											(JSONObject) arg).id);
								} else
									rowData[0] = arg.toString();
								
								argsModel.addRow(rowData);
							}
							argsView.setModel(argsModel);

							isEditRow = false;
							old = row;
						} catch (Exception e2) {
							e2.printStackTrace();
						}

					}
				});
		commandsView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		commandsView.setFillsViewportHeight(true);
		scrollPane.setViewportView(commandsView);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 200));
		panel.setMinimumSize(new Dimension(10, 200));
		splitPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("指令");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblNewLabel.setBounds(49, 22, 54, 15);
		panel.add(lblNewLabel);

		JLabel dlabel = new JLabel("指令选项");
		dlabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dlabel.setBounds(242, 22, 54, 15);
		panel.add(dlabel);

		JLabel label_3 = new JLabel("异常提示");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_3.setBounds(49, 58, 54, 15);
		panel.add(label_3);

		JButton btnNewButton = new JButton("+");
		btnNewButton.setMinimumSize(new Dimension(48, 48));
		btnNewButton.setMaximumSize(new Dimension(48, 48));
		btnNewButton.setPreferredSize(new Dimension(48, 48));
		btnNewButton.setMargin(new Insets(2, 2, 2, 2));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommandType commandType = (CommandType) commandtypeView
						.getSelectedItem();
				boolean isFirst = argsView.getRowCount() == 0;
				switch (commandType) {
					case ctConfigure :
						addConfigureItem();
						break;
					case ctCopyDir :
						addFileOrDir(!isFirst ? "目标目录选择" : "源目录选择", true);
						break;
					case ctCopyFile :
						addFileOrDir(!isFirst ? "目标文件选择" : "源文件选择", false);
						break;
					case ctDelDir :
					case ctDelFile :
						boolean isDir = commandType == CommandType.ctDelDir;
						addFileOrDir(isDir ? "目标目录选择" : "目标文件选择", isDir);
						break;
					case ctRun :
						DefaultTableModel model = (DefaultTableModel) argsView.getModel();
						model.addRow(new Object[]{"指令", "指令说明"});
						isEditRow = true;
						break;
				}

				updateArgsModel();

			}
		});
		btnNewButton.setBounds(1078, 43, 31, 31);
		panel.add(btnNewButton);

		commandtypeView = new JComboBox<>(
				new DefaultComboBoxModel<>(IEnum.msgs(CommandType.class)));
		commandtypeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				CommandType commandType = (CommandType) commandtypeView.getSelectedItem();
				
				checkresultView.setEnabled(commandType == CommandType.ctRun);
				waittimeView.setEnabled(commandType == CommandType.ctRun);				
				
				switch (commandType) {
					case ctConfigure :
						commandindicateView.setModel(new DefaultComboBoxModel<>(
								IEnum.msgs(ConfigureCommandIndicate.class)));
						break;
					case ctCopyDir :
					case ctCopyFile :
						commandindicateView.setModel(new DefaultComboBoxModel<>(
								IEnum.msgs(CopyCommandIndicate.class)));
						break;
					case ctDelDir :
					case ctDelFile :
						commandindicateView.setModel(new DefaultComboBoxModel<>(
								IEnum.msgs(DeleteCommandIndicate.class)));
						break;
					case ctRun :
						commandindicateView.setModel(new DefaultComboBoxModel<>(
								IEnum.msgs(RunCommandIndicate.class)));
						break;
				}
				
				argsView.setModel(makeDefaultTableModel(argsView));
			}
		});
		commandtypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		commandtypeView.setBounds(82, 19, 150, 21);
		panel.add(commandtypeView);

		commandindicateView = new JComboBox<>();
		commandindicateView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		commandindicateView.setBounds(299, 19, 217, 21);
		panel.add(commandindicateView);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(48, 79, 1108, 110);
		panel.add(scrollPane_1);

		argsView = new JTable();
		argsView.setModel(new DefaultTableModel(
			new Object[][] {
				{null},
			},
			new String[] {
				"\u53C2\u6570"
			}
		));
		argsView.setFillsViewportHeight(true);
		scrollPane_1.setViewportView(argsView);

		JButton button_2 = new JButton("-");
		button_2.setMinimumSize(new Dimension(48, 48));
		button_2.setMaximumSize(new Dimension(48, 48));
		button_2.setPreferredSize(new Dimension(48, 48));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = argsView.getSelectedRow();
				if (row == -1)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的参数项目？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				DefaultTableModel model = (DefaultTableModel) argsView
						.getModel();

				model.removeRow(row);
				updateArgsModel();
			}
		});
		button_2.setMargin(new Insets(2, 2, 2, 2));
		button_2.setBounds(1110, 43, 31, 31);
		panel.add(button_2);

		JButton button_5 = new JButton("");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRow(argsView, false);
				updateArgsModel();
			}
		});
		button_5.setIcon(new ImageIcon(InstallCommandConfigureDialog.class
				.getResource("/images/arrow_up_48.png")));
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_5.setBounds(1006, 42, 31, 31);
		panel.add(button_5);

		JButton button_6 = new JButton("");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRow(argsView, true);
				updateArgsModel();
			}
		});
		button_6.setIcon(new ImageIcon(InstallCommandConfigureDialog.class
				.getResource("/images/arrow_down_48.png")));
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_6.setBounds(1037, 42, 31, 31);
		panel.add(button_6);
		
		checkresultView = new JCheckBox("检查返回结果");
		checkresultView.setEnabled(false);
		checkresultView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		checkresultView.setSelected(true);
		checkresultView.setBounds(535, 18, 103, 23);
		panel.add(checkresultView);
		
		JLabel label = new JLabel("等待返回时间");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(644, 22, 78, 15);
		panel.add(label);
		
		waittimeView = new JSpinner();
		waittimeView.setEnabled(false);
		waittimeView.setModel(new SpinnerNumberModel(new Integer(-1), null, null, new Integer(1)));
		waittimeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		waittimeView.setBounds(724, 18, 54, 22);
		panel.add(waittimeView);

		setLocationRelativeTo(null);

		load();
	}
	private JTable commandsView;
	private JComboBox<CommandType> commandtypeView;
	private JComboBox<Enum<?>> commandindicateView;
	private JTable argsView;
	private JCheckBox checkresultView;
	private JSpinner waittimeView;

	public static void showDialog(InstallConfigureInfo installInfo) {
		InstallCommandConfigureDialog dialog;
		try {
			dialog = new InstallCommandConfigureDialog(installInfo);
			dialog.setModal(true);
			dialog.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}
}