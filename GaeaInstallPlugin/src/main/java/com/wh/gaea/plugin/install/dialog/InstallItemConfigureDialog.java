package com.wh.gaea.plugin.install.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;
import com.wh.swing.tools.MsgHelper;

public class InstallItemConfigureDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	boolean isok = false;

	protected void addRow(PageConfigureInfo info, boolean needScrollEnd) {
		DefaultTableModel model = (DefaultTableModel) pagesView.getModel();

		Object[] data = new Object[3];
		data[0] = info.title;
		data[1] = info.saveFile;
		model.addRow(data);

		int row = model.getRowCount() - 1;
		if (needScrollEnd) {
			pagesView.getSelectionModel().setSelectionInterval(row, row);
			pagesView.scrollRectToVisible(pagesView.getCellRect(row, 0, true));
		}
	}

	protected void load() {
		for (PageConfigureInfo info : installInfo) {
			addRow(info, false);
		}
	}

	InstallConfigureInfo installInfo;

	public InstallItemConfigureDialog(InstallConfigureInfo installInfo) {
		super();
		this.installInfo = installInfo;
		setTitle("配置页面选择");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(InstallItemConfigureDialog.class
						.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));

		setBounds(100, 100, 1154, 648);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel splitPane = new JPanel();
		splitPane.setLayout(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		splitPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		pagesView = new JTable();
		pagesView.setModel(new DefaultTableModel(new Object[][]{}, new String[]{
				"\u9875\u9762\u6807\u9898", "\u9875\u9762\u8BF4\u660E"}));
		pagesView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		pagesView.setFillsViewportHeight(true);
		scrollPane.setViewportView(pagesView);

		JPanel panel = new JPanel();
		splitPane.add(panel, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton("确定");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pagesView.getSelectedRow() == -1) {
					MsgHelper.showMessage("请选择一个页面！");
					return;
				}
		
				isok = true;
				setVisible(false);
			}
		});
		
		panel.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("取消");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		panel.add(btnNewButton_1);

		setLocationRelativeTo(null);

		load();
	}
	private JTable pagesView;

	public static PageConfigureInfo showDialog(InstallConfigureInfo installInfo) {
		InstallItemConfigureDialog dialog;

		dialog = new InstallItemConfigureDialog(installInfo);
		dialog.setModal(true);
		dialog.setVisible(true);
		if (!dialog.isok)
			return null;

		return installInfo.get(dialog.pagesView.getSelectedRow());
	}
}