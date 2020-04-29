package com.wh.gaea.plugin.authorization;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.hardware.register.HintType;
import com.wh.hardware.register.ServerCore;
import com.wh.hardware.register.ServerCore.AuthInfo;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.FileHelp;
import com.wh.tools.JsonHelp;

public class AuthorizationDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	IMainControl mainControl;

	class IdInfo {
		String id;
		int row;

		public IdInfo(String id, int row) {
			this.id = id;
			this.row = row;
		}
	}

	protected IdInfo getIdInfo() {
		return getIdInfo(true);
	}

	protected File rootPath() {
		return GlobalInstance.instance().getProjectBasePath();
	}

	protected IdInfo getIdInfo(boolean hint) {
		int row = table.getSelectedRow();
		if (row == -1) {
			if (hint)
				MsgHelper.showMessage("请先选择一条授权记录！");
			return null;
		}
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		String id = (String) model.getValueAt(row, AuthHelper.ID_COL_INDEX);
		if (id == null || id.trim().isEmpty()) {
			if (hint)
				MsgHelper.showMessage("未设置授权编号！");
			return null;
		}
		return new IdInfo(id, row);
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

	protected AuthInfo gridToAuthInfo(int row) throws IOException, ParseException {
		if (row == -1)
			return null;

		DefaultTableModel model = (DefaultTableModel) table.getModel();

		AuthInfo authInfo = new AuthInfo();
		authInfo.aesKey = (String) model.getValueAt(row, AuthHelper.AES_COL_INDEX);
		authInfo.clientTag = (String) model.getValueAt(row, AuthHelper.NAME_COL_INDEX);
		authInfo.hintType = (HintType) model.getValueAt(row, AuthHelper.HINT_COL_INDEX);
		authInfo.genAuthKey = genauthView.isSelected();
		authInfo.days = (int) model.getValueAt(row, AuthHelper.DAY_COL_INDEX);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		authInfo.start = format.parse((String) model.getValueAt(row, AuthHelper.START_COL_INDEX));

		return authInfo;
	}

	protected void publish() throws IOException {
		IdInfo info = getIdInfo();
		if (info == null) {
			return;
		}

		int row = info.row;
		String id = info.id;

		if (MsgHelper.showConfirmDialog("发布回覆盖已有的授权信息及加密代码文件，是否继续？",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		File requestRegisterFile = AuthHelper.getAuthRequestFile(id);
		if (requestRegisterFile == null || !requestRegisterFile.exists()) {
			MsgHelper.showWarn("未设置【客户注册申请文件】或此文件无法找到，发布失败！");
			return;
		}

		File sourceCodeFile = AuthHelper.getSourceCodeFile(id);
		if (sourceCodeFile == null || !sourceCodeFile.exists()) {
			MsgHelper.showWarn("未设置【代码文件】或此文件无法找到，发布失败！");
			return;
		}

		try {
			if (!AuthHelper.getPublicKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id).exists()) {
				try {
					AuthHelper.genKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id);
					AuthHelper.genKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
					return;
				}
			}

			AuthInfo authInfo = gridToAuthInfo(row);

			File serverPrivateKeyFile = AuthHelper.getPrivateKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id);
			File clientPublicKeyFile = AuthHelper.getPublicKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id);

			ServerCore serverCore = new ServerCore(serverPrivateKeyFile, clientPublicKeyFile);
			serverCore.genAuthFile(requestRegisterFile, AuthHelper.getAuthFile(id), authInfo);
			serverCore.encodeCode(sourceCodeFile, AuthHelper.getEncryptCodeFile(id), authInfo.aesKey);
			MsgHelper.showMessage("恭喜，发布授权成功！");
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}

	}

	interface IPublish {
		File[] getFiles(String id) throws IOException;
	}

	protected void publishFiles(IPublish publish, String title, String succMsg) {
		IdInfo info = getIdInfo();
		if (info == null) {
			return;
		}

		String id = info.id;

		File path = SwingTools.selectSaveDir(null, title, "选择要发布到的目录", null);
		if (path == null)
			return;

		try {
			File[] publishFiles = publish.getFiles(id);
			for (File file : publishFiles) {
				if (!file.exists()) {
					MsgHelper.showException("服务端公钥文件【" + file.getAbsolutePath() + "】不存在！");
					return;
				}

			}

			for (File file : publishFiles) {
				FileHelp.copyFileTo(file, new File(path, file.getName()));
			}
			MsgHelper.showMessage(succMsg);
		} catch (IOException e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	protected void publishAuth() {
		publishFiles(new IPublish() {

			@Override
			public File[] getFiles(String id) throws IOException {
				return new File[] { AuthHelper.getPublicKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id),
						AuthHelper.getPrivateKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id),
						AuthHelper.getEncryptCodeFile(id), AuthHelper.getAuthFile(id), };
			}
		}, "发布到客户端", "恭喜，发布客户端成功！");

	}

	protected void addRow() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] data = new Object[10];
		data[AuthHelper.GENAUTH_COL_INDEX] = false;
		data[AuthHelper.ID_COL_INDEX] = "";
		data[AuthHelper.NAME_COL_INDEX] = "无";
		data[AuthHelper.CONTACT_COL_INDEX] = "无";
		data[AuthHelper.TEL_COL_INDEX] = "无";
		data[AuthHelper.START_COL_INDEX] = convertDate(new Date());
		data[AuthHelper.DAY_COL_INDEX] = 30;
		data[AuthHelper.HINT_COL_INDEX] = HintType.htHalf;
		data[AuthHelper.AES_COL_INDEX] = UUID.randomUUID().toString().substring(0, 16);

		model.addRow(data);
		int row = model.getRowCount() - 1;
		table.getSelectionModel().setSelectionInterval(row, row);
		table.scrollRectToVisible(table.getCellRect(row, 0, true));
	}

	protected void removeRow() {
		int row = table.getSelectedRow();
		if (row == -1)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的授权信息？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(row);
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
			JComboBox<HintType> comboBox = (JComboBox<HintType>) component;
			return (T) comboBox.getSelectedItem();
		}
		return null;
	}

	protected void openAuthDir() {
		IdInfo info = getIdInfo();
		if (info == null) {
			return;
		}

		String id = info.id;

		try {
			Desktop.getDesktop().open(AuthHelper.getRootPath(id));
		} catch (IOException e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	protected void save() throws Exception {
		JSONArray data = new JSONArray();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			JSONObject row = new JSONObject();
			for (int j = 0; j < model.getColumnCount(); j++) {
				switch (j) {
				case AuthHelper.HINT_COL_INDEX:
					HintType hintType = (HintType) model.getValueAt(i, j);
					row.put(String.valueOf(j), hintType.name());
					break;
				default:
					Object v = model.getValueAt(i, j);
					row.put(String.valueOf(j), v == null ? "" : v);
					break;
				}
			}
			data.put(row);
		}

		File saveFile = AuthHelper.getDataFile();
		JsonHelp.saveJson(saveFile, data, null);
	}

	protected void load() throws Exception {
		File dataFile = AuthHelper.getDataFile();
		if (!dataFile.exists())
			return;

		JSONArray data = (JSONArray) JsonHelp.parseCacheJson(dataFile, null);

		DefaultTableModel model = makeDefaultTableModel();
		for (Object object : data) {
			JSONObject row = (JSONObject) object;
			JSONArray names = row.names();
			Object[] rowData = new Object[names.length()];
			for (int i = 0; i < names.length(); i++) {
				switch (i) {
				case AuthHelper.HINT_COL_INDEX:
					rowData[i] = HintType.valueOf(row.getString(names.getString(i)));
					break;

				default:
					rowData[i] = row.get(names.getString(i));
					break;
				}
			}
			model.addRow(rowData);
		}

		table.setModel(model);

		table.updateUI();
	}

	protected void saveRow() throws Exception {
		int row = table.getSelectedRow();
		if (row == -1) {
			MsgHelper.showMessage("请先选择一条授权记录！");
			return;
		}

		String id = getViewText(idView);
		String name = getViewText(nameView);
		String start = getViewText(startView);
		int day = getViewText(dayView);
		String contact = getViewText(contactView);
		String tel = getViewText(telView);
		boolean used = getViewText(genauthView);
		HintType hintType = getViewText(hintTypeView);
		String aes = getViewText(aesView);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setValueAt(used, row, AuthHelper.GENAUTH_COL_INDEX);
		model.setValueAt(id, row, AuthHelper.ID_COL_INDEX);
		model.setValueAt(name, row, AuthHelper.NAME_COL_INDEX);
		model.setValueAt(start, row, AuthHelper.START_COL_INDEX);
		model.setValueAt(day, row, AuthHelper.DAY_COL_INDEX);
		model.setValueAt(contact, row, AuthHelper.CONTACT_COL_INDEX);
		model.setValueAt(tel, row, AuthHelper.TEL_COL_INDEX);
		model.setValueAt(hintType, row, AuthHelper.HINT_COL_INDEX);
		model.setValueAt(aes, row, AuthHelper.AES_COL_INDEX);

		table.updateUI();

	}

	protected void genKeyFiles() {
		IdInfo info = getIdInfo();
		if (info == null) {
			return;
		}

		String id = info.id;

		try {
			AuthHelper.genKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id);
			AuthHelper.genKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id);
			MsgHelper.showMessage("恭喜，成功创建秘钥文件！");
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	protected void delete(File file) throws IOException {
		if (file.exists() && !file.delete())
			throw new IOException("删除文件【" + file.getAbsolutePath() + "】失败！");
	}

	public void deleteKeyFiles() {
		IdInfo info = getIdInfo();
		if (info == null) {
			return;
		}

		String id = info.id;

		if (MsgHelper.showConfirmDialog("是否删除选定授权记录的秘钥信息，如果删除回导致所有授权信息无效，包括授权码、加密代码文件等，是否继续？",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		try {
			File file = AuthHelper.getPublicKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id);
			delete(file);
			file = AuthHelper.getPrivateKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id);
			delete(file);

			file = AuthHelper.getPublicKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id);
			delete(file);
			file = AuthHelper.getPrivateKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id);
			delete(file);
		} catch (IOException e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	protected void selectFile(File destFile, String ext, JLabel view) {
		IdInfo info = getIdInfo();
		if (info == null)
			return;

		try {
			File file = SwingTools.selectOpenFile(null, AuthHelper.getRootPath(info.id).getAbsolutePath(), null, ext);

			if (file == null)
				return;

			FileHelp.copyFileTo(file, destFile);

			view.setText(destFile.getName());

		} catch (IOException e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	protected DefaultTableModel makeDefaultTableModel() {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] headers = new Object[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			headers[i] = model.getColumnName(i);
		}
		return new DefaultTableModel(new Object[][] {},
				headers);
	}

	public AuthorizationDialog(IMainControl mainControl) throws Exception {
		super();
		setTitle("授权管理");
		setIconImage(Toolkit.getDefaultToolkit().getImage(AuthorizationDialog.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		this.mainControl = mainControl;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1232, 777);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);

		toolBar.addSeparator(new Dimension(5, 0));
		JButton button_2 = new JButton("添加");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRow();
			}

		});

		JLabel lblNewLabel_1 = new JLabel("授权信息： ");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblNewLabel_1);
		toolBar.add(button_2);

		JButton button_3 = new JButton("删除");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeRow();
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		JButton button_8 = new JButton("保存");
		button_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveRow();
					save();
					MsgHelper.showMessage("保存成功！");
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_8);

		toolBar.addSeparator();

		JButton button_4 = new JButton("生成秘钥(1)");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				genKeyFiles();
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		JButton btnNull = new JButton("删除秘钥");
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteKeyFiles();
			}
		});
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(btnNull);

		JButton button_7 = new JButton("选择申请文件[2]");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IdInfo info = getIdInfo();
				try {
					selectFile(AuthHelper.getAuthRequestFile(info.id), "客户申请注册文件=apy", requestregView);
				} catch (IOException e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_7);

		JButton button_1 = new JButton("选择代码文件(3)");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IdInfo info = getIdInfo();
				try {
					selectFile(AuthHelper.getSourceCodeFile(info.id), "JAVA源文件=java", codeView);
				} catch (IOException e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_1);

		toolBar.addSeparator();

		JButton button = new JButton("生成(4)");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					publish();
				} catch (IOException e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);

		toolBar.addSeparator();

		JButton button_6 = new JButton("发布");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				publishAuth();
			}
		});
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_6);

		JButton button_10 = new JButton("仅发布秘钥");
		button_10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				publishFiles(new IPublish() {

					@Override
					public File[] getFiles(String id) throws IOException {
						return new File[] { AuthHelper.getPublicKeyFile(AuthHelper.SERVER_AUTH_NAME_PREX, id),
								AuthHelper.getPrivateKeyFile(AuthHelper.CLIENT_AUTH_NAME_PREX, id) };
					}
				}, "仅发布秘钥", "恭喜，发布秘钥成功！");

			}
		});
		button_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_10);

		JButton button_5 = new JButton("打开授权目录");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAuthDir();
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		JButton button_9 = new JButton("测试授权");
		button_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IdInfo info = getIdInfo();
				if (info == null)
					return;

				try {
					AuthHelper.testAuth(info.id, gridToAuthInfo(info.row));
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_9);

		JPanel splitPane = new JPanel();
		splitPane.setLayout(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		splitPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		table = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;// 表格不允许被编辑
			}
		};

		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"\u6388\u6743", "\u6388\u6743\u7F16\u53F7", "\u516C\u53F8\u540D\u79F0", "\u8BD5\u7528\u8D77\u59CB\u65E5\u671F", "\u8BD5\u7528\u65F6\u95F4", "\u8054\u7CFB\u4EBA", "\u8054\u7CFB\u7535\u8BDD", "\u6388\u6743\u672A\u901A\u8FC7", "Aes\u79D8\u94A5"
			}
		));
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setMaxWidth(75);
		table.getColumnModel().getColumn(3).setPreferredWidth(114);
		table.getColumnModel().getColumn(7).setResizable(false);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				if (row == -1)
					return;

				DefaultTableModel model = (DefaultTableModel) table.getModel();

				String id = (String) model.getValueAt(row, AuthHelper.ID_COL_INDEX);
				idView.setText(id);
				nameView.setText((String) model.getValueAt(row, AuthHelper.NAME_COL_INDEX));
				contactView.setText((String) model.getValueAt(row, AuthHelper.CONTACT_COL_INDEX));
				telView.setText((String) model.getValueAt(row, AuthHelper.TEL_COL_INDEX));

				startView.setValue(toDate((String) model.getValueAt(row, AuthHelper.START_COL_INDEX)));
				dayView.setValue((int) model.getValueAt(row, AuthHelper.DAY_COL_INDEX));
				genauthView.setSelected((boolean) model.getValueAt(row, AuthHelper.GENAUTH_COL_INDEX));
				hintTypeView.setSelectedItem(model.getValueAt(row, AuthHelper.HINT_COL_INDEX));
				aesView.setText((String) model.getValueAt(row, AuthHelper.AES_COL_INDEX));

				requestregView.setText("未设置授权申请文件");
				codeView.setText("未设置代码文件");
				if (id != null && !id.isEmpty()) {
					try {
						File requestRegisterFile = AuthHelper.getAuthRequestFile(id);
						if (requestRegisterFile != null && requestRegisterFile.exists())
							requestregView.setText(requestRegisterFile.getName());

						File sourceCodeFile = AuthHelper.getSourceCodeFile(id);
						if (sourceCodeFile != null && sourceCodeFile.exists())
							codeView.setText(sourceCodeFile.getName());
					} catch (IOException e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}
				}

			}
		});
		scrollPane.setViewportView(table);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 200));
		panel.setMinimumSize(new Dimension(10, 200));
		splitPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("授权编号");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblNewLabel.setBounds(49, 22, 54, 15);
		panel.add(lblNewLabel);

		idView = new JTextField();
		idView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		idView.setBounds(113, 19, 384, 21);
		panel.add(idView);
		idView.setColumns(10);

		nameView = new JTextField();
		nameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		nameView.setColumns(10);
		nameView.setBounds(618, 19, 535, 21);
		panel.add(nameView);

		JLabel label = new JLabel("公司名称");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(554, 22, 54, 15);
		panel.add(label);

		JLabel label_2 = new JLabel("起始日期");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_2.setBounds(49, 80, 54, 15);
		panel.add(label_2);

		JLabel label_3 = new JLabel("联系人");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_3.setBounds(555, 80, 54, 15);
		panel.add(label_3);

		contactView = new JTextField();
		contactView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contactView.setColumns(10);
		contactView.setBounds(619, 77, 184, 21);
		panel.add(contactView);

		JLabel label_4 = new JLabel("联系电话");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_4.setBounds(835, 80, 54, 15);
		panel.add(label_4);

		telView = new JTextField();
		telView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		telView.setColumns(10);
		telView.setBounds(899, 77, 184, 21);
		panel.add(telView);

		startView = new JSpinner();
		startView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		startView.setModel(new SpinnerDateModel(new Date(1581868800000L), null, null, Calendar.DAY_OF_YEAR));
		startView.setEditor(new JSpinner.DateEditor(startView, "yyyy-MM-dd"));
		startView.setBounds(113, 77, 159, 22);
		panel.add(startView);

		JLabel label_5 = new JLabel("试用天数");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_5.setBounds(302, 80, 54, 15);
		panel.add(label_5);

		dayView = new JSpinner();
		dayView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dayView.setBounds(366, 77, 74, 22);
		panel.add(dayView);

		genauthView = new JCheckBox("授权");
		genauthView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		genauthView.setBounds(46, 171, 50, 23);
		panel.add(genauthView);

		codeView = new JLabel("未设置代码文件");
		codeView.setHorizontalAlignment(SwingConstants.CENTER);
		codeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		codeView.setBounds(219, 175, 275, 15);
		panel.add(codeView);

		requestregView = new JLabel("未设置授权申请文件");
		requestregView.setHorizontalAlignment(SwingConstants.CENTER);
		requestregView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		requestregView.setBounds(625, 172, 264, 15);
		panel.add(requestregView);

		JLabel label_7 = new JLabel("未通过");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_7.setBounds(363, 138, 50, 15);
		panel.add(label_7);

		hintTypeView = new JComboBox<>();
		hintTypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		hintTypeView.setModel(new DefaultComboBoxModel<>(new HintType[] { HintType.htMsg, HintType.htHalf }));
		hintTypeView.setSelectedIndex(1);
		hintTypeView.setBounds(410, 135, 87, 21);
		panel.add(hintTypeView);

		JLabel label_8 = new JLabel("代码文件： ");
		label_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_8.setBounds(145, 174, 64, 17);
		panel.add(label_8);

		JLabel label_9 = new JLabel("申请文件： ");
		label_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_9.setBounds(551, 171, 64, 17);
		panel.add(label_9);

		JLabel lblAes = new JLabel("AES秘钥");
		lblAes.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblAes.setBounds(49, 135, 54, 15);
		panel.add(lblAes);

		aesView = new JTextField();
		aesView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		aesView.setColumns(10);
		aesView.setBounds(113, 132, 184, 21);
		panel.add(aesView);

		JButton btnNewButton = new JButton("。。。");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aesView.setText(UUID.randomUUID().toString().substring(0, 16));
			}
		});
		btnNewButton.setBounds(307, 131, 29, 23);
		panel.add(btnNewButton);

		setLocationRelativeTo(null);
	}

	private JTable table;
	private JTextField idView;
	private JTextField nameView;
	private JTextField contactView;
	private JTextField telView;
	private JLabel codeView;
	private JSpinner startView;
	private JSpinner dayView;
	private JCheckBox genauthView;
	private JLabel requestregView;
	private JComboBox<HintType> hintTypeView;
	private JTextField aesView;

	public static void showDialog(IMainControl mainControl) {
		AuthorizationDialog dialog;
		try {
			dialog = new AuthorizationDialog(mainControl);
			dialog.load();
			dialog.setModal(true);
			dialog.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}
}