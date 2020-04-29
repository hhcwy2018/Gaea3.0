package com.wh.gaea.plugin.aps.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import com.wh.gaea.industry.builder.DynamicParamInfoBuilder;
import com.wh.gaea.industry.builder.DynamicParamInfoBuilder.GroupType;
import com.wh.gaea.industry.interfaces.DynamicGroupInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.info.RuleMapInfo;
import com.wh.gaea.plugin.aps.info.RuleMapInfos;
import com.wh.gaea.plugin.aps.interfaces.IRuleBase;
import com.wh.gaea.plugin.aps.rule.RuleMapType;
import com.wh.gaea.plugin.aps.rule.connector.RuleTarget;
import com.wh.gaea.plugin.datasource.dialog.UrlDataSourceConfig;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.adapter.AlternateRowColorTableRender;
import com.wh.swing.tools.celleditor.NumberCellEditor;
import com.wh.tools.IEnum;
import java.awt.Color;

public class APSRuleRelationEditor extends JDialog {

	private static final long serialVersionUID = 1L;

	static final int TABLE_TYPE_INDEX = 0;
	static final int TABLE_ITEM_INDEX = 1;
	static final int TABLE_ATTR_INDEX = 2;
	static final int TABLE_OPERATION_INDEX = 3;
	static final int TABLE_DEST_TYPE_INDEX = 4;
	static final int TABLE_DEST_ITEM_INDEX = 5;
	static final int TABLE_DEST_ATTR_INDEX = 6;
	static final int TABLE_LEVEL_INDEX = 7;

	private JPanel panel;

	RuleMapInfos ruleMapInfos = new RuleMapInfos();

	private JScrollPane scrollPane_2;
	private JTable ruleMapInfosTable;

	class TableControl {
		protected JComboBox<Object> itemComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
		protected JComboBox<Object> attrComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
		protected JComboBox<Object> typeComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(IRuleBase.msgs(RuleTarget.class)));

		protected <T> void clearComboBox(JComboBox<T> comboBox) {
			comboBox.setModel(new DefaultComboBoxModel<T>());
			comboBox.setSelectedIndex(-1);
		}

		protected void clear() {
			clearComboBox(itemComboBox);
			clearComboBox(attrComboBox);

			int row = ruleMapInfosTable.convertRowIndexToModel(ruleMapInfosTable.getSelectedRow());
			if (row == -1)
				return;

			DefaultTableModel model = (DefaultTableModel) ruleMapInfosTable.getModel();
			if (clearCols != null) {
				for (int index : clearCols) {
					model.setValueAt(null, row, index);
				}
			}
		}

		int[] clearCols;
		
		public TableControl(int[] clearCols) {
			this.clearCols = clearCols;
			typeComboBox.addActionListener(new ActionListener() {
				RuleTarget oldFilterType = null;

				@Override
				public void actionPerformed(ActionEvent e) {

					RuleTarget filterType = (RuleTarget) typeComboBox.getSelectedItem();
					if (filterType == null)
						return;

					if (oldFilterType != null && (RuleTarget) typeComboBox.getSelectedItem() == oldFilterType)
						return;

					clear();

					oldFilterType = filterType;
					switch (filterType) {
					case ftDevice:
						try {
							configComboBox(itemComboBox, DynamicParamInfoBuilder
									.queryGroups(APSConfigure.getConfigDBName(), GroupType.gtDevice));
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
						break;
					case ftMaterial:
						try {
							configComboBox(itemComboBox, DynamicParamInfoBuilder
									.queryGroups(APSConfigure.getConfigDBName(), GroupType.gtMaterial));
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
						break;
					case ftModule:
						try {
							configComboBox(itemComboBox, DynamicParamInfoBuilder
									.queryGroups(APSConfigure.getConfigDBName(), GroupType.gtDevice));
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
						break;
					}

					if (itemComboBox.getModel().getSize() > 0)
						itemComboBox.setSelectedIndex(0);
					else {
						itemComboBox.setSelectedIndex(-1);
					}
				}
			});

			itemComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					DynamicGroupInfo groupInfo = (DynamicGroupInfo) itemComboBox.getSelectedItem();
					if (groupInfo == null) {
						attrComboBox.setModel(new DefaultComboBoxModel<>());
						return;
					}
					try {
						configComboBox(attrComboBox,
								DynamicParamInfoBuilder.queryGroupParams(APSConfigure.getConfigDBName(), groupInfo.id));
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}

					attrComboBox.insertItemAt(null, 0);
					attrComboBox.setSelectedIndex(0);
				}
			});

		}
	}

	TableControl sourceTableControl = new TableControl(new int[] {TABLE_ITEM_INDEX, TABLE_ATTR_INDEX});
	TableControl destTableControl = new TableControl(new int[] {TABLE_DEST_ITEM_INDEX, TABLE_DEST_ATTR_INDEX});

	public void configRuleMapInfosTable() {

		ruleMapInfosTable.getColumnModel().getColumn(7)
				.setCellEditor(new NumberCellEditor(new SpinnerNumberModel(0, 0, 100, 1)));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_TYPE_INDEX)
				.setCellEditor(new DefaultCellEditor(sourceTableControl.typeComboBox));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_ITEM_INDEX)
				.setCellEditor(new DefaultCellEditor(sourceTableControl.itemComboBox));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_ATTR_INDEX)
				.setCellEditor(new DefaultCellEditor(sourceTableControl.attrComboBox));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_OPERATION_INDEX).setCellEditor(
				new DefaultCellEditor(new JComboBox<>(new DefaultComboBoxModel<>(IEnum.msgs(RuleMapType.class)))));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_DEST_TYPE_INDEX)
				.setCellEditor(new DefaultCellEditor(destTableControl.typeComboBox));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_DEST_ITEM_INDEX)
				.setCellEditor(new DefaultCellEditor(destTableControl.itemComboBox));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_DEST_ATTR_INDEX)
				.setCellEditor(new DefaultCellEditor(destTableControl.attrComboBox));
		ruleMapInfosTable.getColumnModel().getColumn(TABLE_LEVEL_INDEX)
				.setCellEditor(new NumberCellEditor(new SpinnerNumberModel(0, 0, 100, 1)));
	}

	public void init(RuleMapInfos ruleMapInfos) {

		if (ruleMapInfos != null) {
			this.ruleMapInfos = ruleMapInfos;
		}

		load();

	}

	protected RuleMapInfo toRuleMapInfo(DefaultTableModel model, int row) {
		RuleMapInfo info = new RuleMapInfo();
		info.type = (RuleTarget) model.getValueAt(row, TABLE_TYPE_INDEX);
		info.item = (DynamicGroupInfo) model.getValueAt(row, TABLE_ITEM_INDEX);
		info.attr = (DynamicParamInfo) model.getValueAt(row, TABLE_ATTR_INDEX);
		info.dest_type = (RuleTarget) model.getValueAt(row, TABLE_DEST_TYPE_INDEX);
		info.dest_item = (DynamicGroupInfo) model.getValueAt(row, TABLE_DEST_ITEM_INDEX);
		info.dest_attr = (DynamicParamInfo) model.getValueAt(row, TABLE_DEST_ATTR_INDEX);
		info.mapType = (RuleMapType) model.getValueAt(row, TABLE_OPERATION_INDEX);
		info.level = (int) model.getValueAt(row, TABLE_LEVEL_INDEX);
		return info;
	}

	protected void toModel(DefaultTableModel model, int row, RuleMapInfo info) {
		model.setValueAt(info.type, row, TABLE_TYPE_INDEX);
		model.setValueAt(info.item, row, TABLE_ITEM_INDEX);
		model.setValueAt(info.attr, row, TABLE_ATTR_INDEX);
		model.setValueAt(info.dest_type, row, TABLE_DEST_TYPE_INDEX);
		model.setValueAt(info.dest_item, row, TABLE_DEST_ITEM_INDEX);
		model.setValueAt(info.dest_attr, row, TABLE_DEST_ATTR_INDEX);
		model.setValueAt(info.mapType, row, TABLE_OPERATION_INDEX);
		model.setValueAt(info.level, row, TABLE_LEVEL_INDEX);
	}

	protected void save() {
		ruleMapInfos.clear();
		DefaultTableModel model = (DefaultTableModel) ruleMapInfosTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			RuleMapInfo info = toRuleMapInfo(model, i);
			ruleMapInfos.add(info);
		}
	}

	protected DefaultTableModel makeDefaultTableModel(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] headers = new Object[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			headers[i] = model.getColumnName(i);
		}
		return new DefaultTableModel(headers, 0);
	}

	protected void load() {
		DefaultTableModel model = makeDefaultTableModel(ruleMapInfosTable);
		for (RuleMapInfo info : ruleMapInfos) {
			model.addRow(new Vector<>());
			toModel(model, model.getRowCount() - 1, info);
		}
		
		ruleMapInfosTable.setModel(model);

		configRuleMapInfosTable();
	}

	protected <E> void configComboBox(JComboBox<E> comboBox, E[] data) {
		if (data == null || data.length == 0)
			comboBox.setModel(new DefaultComboBoxModel<>());
		else {
			comboBox.setModel(new DefaultComboBoxModel<>(data));
		}
	}

	/**
	 * Create the dialog.
	 */
	public APSRuleRelationEditor() {
		setResizable(false);

		setTitle("规则映射");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(10, 10, 1079, 700);
		getContentPane().setLayout(new BorderLayout());

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton("保存");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		btnNewButton.setMargin(new Insets(2, 10, 2, 10));
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(btnNewButton);

		JButton button = new JButton("装载");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				load();
			}
		});
		button.setMargin(new Insets(2, 10, 2, 10));
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button);

		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(28, 26, 974, 587);
		panel.add(scrollPane_2);

		ruleMapInfosTable = new JTable();
		ruleMapInfosTable.setSelectionForeground(Color.PINK);
		ruleMapInfosTable.setFillsViewportHeight(true);
		ruleMapInfosTable.setCellSelectionEnabled(true);
		ruleMapInfosTable.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "\u7C7B\u578B", "\u9879\u76EE", "\u5C5E\u6027", "\u64CD\u4F5C",
						"\u76EE\u6807\u7C7B\u578B", "\u76EE\u6807\u9879\u76EE", "\u76EE\u6807\u5C5E\u6027",
						"\u4F18\u5148\u7EA7" }));
		ruleMapInfosTable.getColumnModel().getColumn(0).setMaxWidth(100);
		ruleMapInfosTable.getColumnModel().getColumn(3).setMaxWidth(100);
		ruleMapInfosTable.getColumnModel().getColumn(4).setMaxWidth(100);
		ruleMapInfosTable.getColumnModel().getColumn(6).setMaxWidth(100);
		ruleMapInfosTable.getColumnModel().getColumn(7).setMaxWidth(100);
		ruleMapInfosTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		ruleMapInfosTable.setRowHeight(30);
		scrollPane_2.setViewportView(ruleMapInfosTable);
		AlternateRowColorTableRender.config(ruleMapInfosTable);

		JButton btnNewButton_1 = new JButton("+");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) ruleMapInfosTable.getModel();
				model.insertRow(model.getRowCount(), new Object[model.getColumnCount()]);
				int row = ruleMapInfosTable.convertRowIndexToView(model.getRowCount() - 1);
				ruleMapInfosTable.getSelectionModel().setSelectionInterval(row, row);
				ruleMapInfosTable.setRowHeight(row, 30);
			}
		});
		btnNewButton_1.setMargin(new Insets(2, 0, 2, 0));
		btnNewButton_1.setFont(new Font("微软雅黑", Font.BOLD, 12));
		btnNewButton_1.setBounds(1012, 38, 28, 28);
		panel.add(btnNewButton_1);

		JButton button_1 = new JButton("-");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = ruleMapInfosTable.convertRowIndexToModel(ruleMapInfosTable.getSelectedRow());
				if (row == -1)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的过滤条件？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				DefaultTableModel model = (DefaultTableModel) ruleMapInfosTable.getModel();
				if (ruleMapInfosTable.isEditing())
					ruleMapInfosTable.getCellEditor().cancelCellEditing();
				model.removeRow(row);
			}
		});
		button_1.setMargin(new Insets(2, 0, 2, 0));
		button_1.setFont(new Font("微软雅黑", Font.BOLD, 12));
		button_1.setBounds(1012, 71, 28, 28);
		panel.add(button_1);

		setLocationRelativeTo(null);

	}

	public static void showDialog(RuleMapInfos ruleMapInfos) {
		APSRuleRelationEditor config = new APSRuleRelationEditor();
		config.init(ruleMapInfos);

		config.setModal(true);
		config.setVisible(true);
	}
}
