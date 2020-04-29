package com.wh.gaea.plugin.authorization;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.hardware.register.ClientCore;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.JsonHelp;

public class GenRegisterFileDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static final String SERVER_AUTH_NAME_PREX = "server";
	public static final String CLIENT_AUTH_NAME_PREX = "client";

	File requestRegisterFile;
	private JTable table_1;

	public GenRegisterFileDialog() throws Exception {
		super();
		setTitle("注册申请文件生成");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(GenRegisterFileDialog.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 959, 629);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);

		toolBar.addSeparator(new Dimension(5, 0));
		JButton button_2 = new JButton("生成");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table_1.getSelectedRow();
				if (row == -1) {
					MsgHelper.showMessage("请先选择一条授权记录！");
					return;
				}
				
				DefaultTableModel model = (DefaultTableModel) table_1.getModel();
				String id = (String) model.getValueAt(row, 0);
				
				try {
					File serverPublicKeyFile = AuthHelper.getPublicKeyFile(SERVER_AUTH_NAME_PREX, id);
					if (!serverPublicKeyFile.exists()) {
						MsgHelper.showWarn("未发现公钥文件【" + serverPublicKeyFile.getAbsolutePath() + "】，请确认后再试！");
						return;
					}
					
					requestRegisterFile = AuthHelper.getAuthRequestFile(id);

					ClientCore clientCore = new ClientCore(serverPublicKeyFile);
					clientCore.genApplyFile(requestRegisterFile);
					MsgHelper.showMessage("恭喜，成功生成注册申请文件，关闭对话框打开目录！");
					Desktop.getDesktop().open(requestRegisterFile.getParentFile());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
				
			}

		});
		toolBar.add(button_2);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(10, 200));
		panel.setMinimumSize(new Dimension(10, 200));
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
		
		table_1 = new JTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;// 表格不允许被编辑
			}
		};

		table_1.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"\u6388\u6743\u7F16\u53F7", "\u4F01\u4E1A\u540D\u79F0", "\u8054\u7CFB\u4EBA", "\u8054\u7CFB\u7535\u8BDD"
			}
		));
		table_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		table_1.setFillsViewportHeight(true);
		scrollPane.setViewportView(table_1);

		setLocationRelativeTo(null);
	}

	protected void load() throws Exception {
		File dataFile = AuthHelper.getDataFile();
		if (!dataFile.exists())
			return;

		JSONArray data = (JSONArray) JsonHelp.parseCacheJson(dataFile, null);

		DefaultTableModel model = new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					"\u6388\u6743\u7F16\u53F7", "\u4F01\u4E1A\u540D\u79F0", "\u8054\u7CFB\u4EBA", "\u8054\u7CFB\u7535\u8BDD"
				}
			);
		for (Object object : data) {
			JSONObject row = (JSONObject) object;

			JSONArray names = row.names();

			String id = (String) row.get(names.getString(AuthHelper.ID_COL_INDEX));
			if (id == null || id.isEmpty())
				continue;
			
			Object[] rowData = new Object[names.length()];
			rowData[0] = id;
			rowData[1] = row.get(names.getString(AuthHelper.NAME_COL_INDEX));
			rowData[2] = row.get(names.getString(AuthHelper.CONTACT_COL_INDEX));
			rowData[3] = row.get(names.getString(AuthHelper.TEL_COL_INDEX));
			model.addRow(rowData);
		}

		table_1.setModel(model);

		table_1.updateUI();
	}
	
	public static void showDialog() {
		GenRegisterFileDialog dialog;
		try {
			dialog = new GenRegisterFileDialog();
			dialog.load();
			
			dialog.setModal(true);
			dialog.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}