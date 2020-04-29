package com.wh.swing.tools.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JScrollPane;
import java.awt.Insets;
import javax.swing.JList;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ItemSwitcher<T> extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JList<T> sourceView;
	private JList<T> destView;

	protected boolean isok = false;
	
	protected List<T> getListSelects(JList<T> list, boolean isAll) {
		List<T> result = new ArrayList<>();
		if (isAll) {
			DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				result.add(model.getElementAt(i));
			} 
		}else {
			result.addAll(list.getSelectedValuesList());
		}
		
		return result;
	}
	
	protected void sortList(JList<T> list, DefaultListModel<T> model) {
		TreeMap<String, T> sortMap = new TreeMap<>();
		for (int i = 0; i < model.getSize(); i++) {
			T t = model.getElementAt(i);
			sortMap.put(t.toString(), t);
		}
		
		model = new DefaultListModel<>();
		for (T t : sortMap.values()) {
			model.addElement(t);
		}
		
		list.setModel(model);
	}

	protected void addToModel(JList<T> source, JList<T> dest, boolean isAll) {
		if (!isAll)
			if (source.getSelectedValue() == null) {
				return;
			}
		
		List<T> list = getListSelects(source, isAll);
		DefaultListModel<T> sourceModel = (DefaultListModel<T>) source.getModel();
		DefaultListModel<T> destModel = (DefaultListModel<T>) dest.getModel();
		for (T t : list) {
			sourceModel.removeElement(t);
			if (destModel.indexOf(t) == -1)
				destModel.addElement(t);
		}
		
		sortList(dest, (DefaultListModel<T>) dest.getModel());
	}

	/**
	 * Create the dialog.
	 */
	public ItemSwitcher() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ItemSwitcher.class.getResource("/image/browser.png")));
		setTitle("项目选择");
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 930, 636);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("源");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		lblNewLabel.setBounds(143, 15, 81, 21);
		contentPanel.add(lblNewLabel);
		
		JLabel label = new JLabel("目标");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		label.setBounds(678, 15, 81, 21);
		contentPanel.add(label);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(33, 51, 399, 491);
		contentPanel.add(scrollPane);
		
		sourceView = new JList<>();
		sourceView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					e.consume();
					addToModel(sourceView, destView, false);				
				}
			}
		});
		sourceView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(sourceView);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(487, 51, 399, 491);
		contentPanel.add(scrollPane_1);
		
		destView = new JList<>();
		destView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					e.consume();
					addToModel(destView, sourceView, false);				
				}
			}
		});
		destView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_1.setViewportView(destView);
		
		JButton button = new JButton("->");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToModel(sourceView, destView, false);				
			}
		});
		button.setFont(new Font("黑体", Font.BOLD, 12));
		button.setMargin(new Insets(2, 2, 2, 2));
		button.setBounds(439, 88, 39, 29);
		contentPanel.add(button);
		
		JButton button_1 = new JButton("->>");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToModel(sourceView, destView, true);				
			}
		});
		button_1.setFont(new Font("黑体", Font.BOLD, 12));
		button_1.setMargin(new Insets(2, 2, 2, 2));
		button_1.setBounds(439, 205, 39, 29);
		contentPanel.add(button_1);
		
		JButton button_2 = new JButton("<-");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToModel(destView, sourceView, false);				
			}
		});
		button_2.setFont(new Font("黑体", Font.BOLD, 12));
		button_2.setMargin(new Insets(2, 2, 2, 2));
		button_2.setBounds(439, 322, 39, 29);
		contentPanel.add(button_2);
		
		JButton button_3 = new JButton("<<-");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToModel(destView, sourceView, true);				
			}
		});
		button_3.setFont(new Font("黑体", Font.BOLD, 12));
		button_3.setMargin(new Insets(2, 2, 2, 2));
		button_3.setBounds(439, 439, 39, 29);
		contentPanel.add(button_3);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = true;
				setVisible(false);
			}
		});
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		setLocationRelativeTo(null);
	}
	
	public static class Result<T>{
		public boolean isok = false;
		public List<T> datas = new ArrayList<>();
	}
	
	public static <T> Result<T> show(T[] sources, T[] selects){
		ItemSwitcher<T> dialog = new ItemSwitcher<>();
		DefaultListModel<T> sourceModel = new DefaultListModel<>();
		for (T t : sources) {
			sourceModel.addElement(t);
		}
		
		DefaultListModel<T> destModel = new DefaultListModel<>();
		if (selects != null) {
			for (T t : selects) {
				destModel.addElement(t);
			}
		}
		
		dialog.sortList(dialog.sourceView, sourceModel);
		dialog.sortList(dialog.destView, destModel);
		
		dialog.setVisible(true);
		
		Result<T> result = new Result<>();
		result.isok = dialog.isok;
		if (result.isok) {
			for (int i = 0; i < destModel.getSize(); i++) {
				result.datas.add(destModel.getElementAt(i));
			}
		}
		
		dialog.dispose();
		
		return result;
	}
}
