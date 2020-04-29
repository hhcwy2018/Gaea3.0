package com.wh.swing.tools.dialog.input;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.wh.swing.tools.MsgHelper;

public class TextInput extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	boolean isok = false;

	protected void ok() {
		String text = editor.getText();
		if (text == null || text.isEmpty()) {
			MsgHelper.showMessage("请输入信息后重试！");
			return;
		}
		isok = true;
		setVisible(false);
	}

	private JTextField editor;

	/**
	 * Create the dialog.
	 */
	public TextInput() {
		setAlwaysOnTop(true);
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(TextInput.class.getResource("/image/browser.png")));
		setBounds(100, 100, 611, 151);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		editor = new JTextField();
		editor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		editor.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		contentPanel.add(editor, BorderLayout.CENTER);
		editor.setColumns(10);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		okButton.setActionCommand("");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		setLocationRelativeTo(null);
	}

	public static String showDialog(String title) {
		return showDialog(title, null);
	}

	public static String showDialog(String title, String defaultValue) {
		TextInput msg = new TextInput();
		if (defaultValue != null)
			msg.editor.setText(defaultValue);
		msg.setTitle(title);
		msg.setModal(true);
		msg.setVisible(true);
		String text = null;
		if (msg.isok) {
			text = msg.editor.getText();
		} else
			text = null;
		msg.dispose();
		return text;
	}
}
