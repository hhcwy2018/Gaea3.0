package com.wh.swing.tools.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.wh.swing.tools.MsgHelper;

public class ListSelecter<T> extends JDialog{

	private static final long serialVersionUID = 1L;
	private JList<T> list;

	protected boolean isok = false;
	
	public ListSelecter() {
		setAlwaysOnTop(true);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setTitle("选择");
		setSize(753,  563);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		list = new JList<>();
		list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(list);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("确定");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedValue() == null) {
					MsgHelper.showMessage("请先选择一项后再试！");
					return;
				}
				isok = true;
				setVisible(false);
			}
		});
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(btnNewButton);
		
		JButton button = new JButton("取消");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(button);
		
		setLocationRelativeTo(null);
	}

	public static <T> T showDialog(String title, List<T> datas){
		ListSelecter<T> dialog = new ListSelecter<>();
		DefaultListModel<T> model = new DefaultListModel<>();
		for (T t : datas) {
			model.addElement(t);			
		}
		if (title != null && !title.isEmpty())
			dialog.setTitle(title);
		
		dialog.list.setModel(model);
		dialog.setModal(true);
		dialog.setVisible(true);
		
		if (!dialog.isok)
			return null;
		
		T result = dialog.list.getSelectedValue();
		dialog.dispose();
		
		return result;
	}
	
}
