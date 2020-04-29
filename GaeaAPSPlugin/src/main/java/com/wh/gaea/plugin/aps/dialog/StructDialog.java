package com.wh.gaea.plugin.aps.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.wh.swing.tools.MsgHelper;

public class StructDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public boolean isok = false;
	
	private JPanel panel_3;
	private JTextField nameView;
	private JTextArea memoView;

	/**
	 * Create the dialog.
	 */
	public StructDialog() {

		setTitle("架构添加");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(StructDialog.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 573, 453);
		getContentPane().setLayout(new BorderLayout());
		panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(null);
		
		JLabel label = new JLabel("架构名称");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(39, 29, 81, 21);
		panel_3.add(label);
		
		nameView = new JTextField();
		nameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		nameView.setBounds(120, 26, 384, 27);
		panel_3.add(nameView);
		nameView.setColumns(10);
		
		JLabel label_1 = new JLabel("架构说明");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(39, 91, 81, 21);
		panel_3.add(label_1);
		
		memoView = new JTextArea();
		memoView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		memoView.setLineWrap(true);
		memoView.setBounds(120, 92, 384, 239);
		panel_3.add(memoView);
		
		JButton button = new JButton("确定");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String tmp = nameView.getText();
				if (tmp == null || tmp.isEmpty()) {
					MsgHelper.showMessage("架构名称不能为空，请填写后再试！");
					return;
				}
				
				isok = true;
				setVisible(false);
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBounds(356, 346, 70, 29);
		panel_3.add(button);
		
		JButton button_1 = new JButton("取消");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.setMargin(new Insets(0, 0, 0, 0));
		button_1.setBounds(434, 346, 70, 29);
		panel_3.add(button_1);

		setLocationRelativeTo(null);
	}

	public static class Result{
		public boolean isok;
		public String name;
		public String desc;
	}
	
	public static Result showDialog() {
		StructDialog config = new StructDialog();
		config.setModal(true);
		config.setVisible(true);
		
		Result result = new Result();
		result.isok = config.isok;
		result.name = config.nameView.getText();
		result.desc = config.memoView.getText();
		 
		config.dispose();
		
		return result;
	}
}
