package com.wh.gaea.install.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.swing.tools.SwingTools;

public class SelectInstallDirForm extends BaseForm {

	private static final long serialVersionUID = -3469632872672185703L;
	private JPanel contentView;

	InstallConfigureInfo installInfo;
	private JTextField dirView;

	public SelectInstallDirForm(InstallConfigureInfo installInfo) {
		this.installInfo = installInfo;

		getContentPane().setBackground(Color.WHITE);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setTitle(getFormTitle());
		setBounds(100, 100, 821, 273);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.LINE_AXIS));

		JLabel lblNewLabel_1 = new JLabel(" 安装目录 ");
		panel_1.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);

		dirView = new JTextField();
		if (installInfo.installDir != null)
			dirView.setText(installInfo.installDir.getAbsolutePath());
		panel_1.add(dirView);
		dirView.setColumns(10);

		JButton btnNewButton = new JButton("选择");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = SwingTools.selectOpenDir(null, "安装路径选择",
						"请选择要安装到的目录", dirView.getText());
				if (dir == null)
					return;

				dirView.setText(dir.getAbsolutePath());
			}
		});
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton.setMargin(new Insets(2, 2, 2, 2));
		panel_1.add(btnNewButton);

		JLabel lblNewLabel = new JLabel("欢迎您使用本系统，请选择一个目录用于安装系统");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblNewLabel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentView = new JPanel();
		scrollPane.setViewportView(contentView);
		GridBagLayout gbl_contentView = new GridBagLayout();
		gbl_contentView.columnWeights = new double[]{};
		gbl_contentView.rowWeights = new double[]{};
		contentView.setLayout(gbl_contentView);

	}

	@Override
	public String getId() {
		return pageInfo.id;
	}

	@Override
	public CheckResult check() {
		return new CheckResult(true);
	}

	@Override
	public void save() throws Exception {
		String dirPath = dirView.getText().trim();
		if (dirPath == null || dirPath.isEmpty())
			throw new Exception("请设置安装路径！");

		File dir = new File(dirPath);
		if (!dir.exists())
			if (!dir.mkdirs())
				throw new FileNotFoundException("创建目录【" + dirPath + "】失败！");

		installInfo.installDir = dir;
	}

	@Override
	public String getFormTitle() {
		return "安装路径选择";
	}

}
