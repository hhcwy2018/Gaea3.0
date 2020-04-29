package com.wh.gaea.install.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class InstallFailForm extends BaseForm {

	private static final long serialVersionUID = -3469632872672185703L;
	private JPanel contentView;

	public InstallFailForm() {
		getContentPane().setBackground(Color.WHITE);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setTitle("安装失败");
		setBounds(100, 100, 821, 273);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_1 = new JLabel("安装没有成功完成，请重新执行！");
		lblNewLabel_1.setForeground(Color.RED);
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 40));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblNewLabel_1, BorderLayout.CENTER);
		
		JLabel lblNewLabel = new JLabel("   ");
		lblNewLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setIcon(new ImageIcon(InstallFailForm.class.getResource("/image/alert.png")));
		getContentPane().add(lblNewLabel, BorderLayout.WEST);

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
		return "prepare_setup_form";
	}

	@Override
	public CheckResult check() {
		return new CheckResult(true);
	}

	@Override
	public void save() throws Exception {
	}
	
	@Override
	public String getFormTitle() {
		return "安装失败";
	}
}
