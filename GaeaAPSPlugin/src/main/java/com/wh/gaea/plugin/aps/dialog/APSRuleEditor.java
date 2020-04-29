package com.wh.gaea.plugin.aps.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;

import com.wh.gaea.industry.builder.DeviceBuilder;
import com.wh.gaea.industry.builder.DynamicParamInfoBuilder;
import com.wh.gaea.industry.builder.DynamicParamInfoBuilder.GroupType;
import com.wh.gaea.industry.builder.MaterialInfoBuilder;
import com.wh.gaea.industry.info.DeviceInfo;
import com.wh.gaea.industry.interfaces.DynamicGroupInfo;
import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.gaea.industry.interfaces.IDInfo;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.interfaces.IRuleBase;
import com.wh.gaea.plugin.aps.rule.Rule;
import com.wh.gaea.plugin.aps.rule.RuleExpr;
import com.wh.gaea.plugin.aps.rule.RuleExprLogicType;
import com.wh.gaea.plugin.aps.rule.RuleExprOperation;
import com.wh.gaea.plugin.aps.rule.RulePart;
import com.wh.gaea.plugin.aps.rule.RulePeriod;
import com.wh.gaea.plugin.aps.rule.RuleScope;
import com.wh.gaea.plugin.aps.rule.RuleType;
import com.wh.gaea.plugin.aps.rule.connector.RuleTarget;
import com.wh.gaea.plugin.datasource.dialog.UrlDataSourceConfig;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.adapter.AlternateRowColorTableRender;
import com.wh.swing.tools.celleditor.ButtonCellEditor;
import com.wh.swing.tools.celleditor.ButtonCellEditor.ActionResult;
import com.wh.swing.tools.celleditor.ButtonCellEditor.ButtonActionListener;
import com.wh.swing.tools.celleditor.ButtonCellEditor.EditorType;
import com.wh.swing.tools.celleditor.NumberCellEditor;
import com.wh.swing.tools.dialog.ItemSwitcher;
import com.wh.tools.IEnum;
import com.wh.tools.JsonHelp;
import java.awt.Color;

public class APSRuleEditor extends JDialog {

	private static final long serialVersionUID = 1L;

	static final int TABLE_LEFT_LOGIC_INDEX = 0;
	static final int TABLE_TYPE_INDEX = 1;
	static final int TABLE_ITEM_INDEX = 2;
	static final int TABLE_ATTR_INDEX = 3;
	static final int TABLE_OPERATION_INDEX = 4;
	static final int TABLE_VALUE_INDEX = 5;
	static final int TABLE_RIGHT_LOGIC_INDEX = 6;

	private JPanel panel;
	private JTextField nameView;
	private JLabel label_5;
	private JComboBox<RuleScope> scopeView;
	private JLabel label_6;
	private JComboBox<RuleType> typeView;
	private JLabel label_8;
	private JTextArea descView;
	private JTextArea exprView;
	private JComboBox<RulePart> partView;

	Rule rule = new Rule();
	String bomId, bommxId;

	DeviceBuilder deviceQuery = new DeviceBuilder();
	MaterialInfoBuilder materialQuery = new MaterialInfoBuilder();

	protected JComboBox<Object> itemComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
	protected JComboBox<Object> attrComboBox = new JComboBox<>(new DefaultComboBoxModel<>());
	protected JComboBox<Object> typeComboBox = new JComboBox<>(
			new DefaultComboBoxModel<>(IRuleBase.msgs(RuleTarget.class)));
	protected JComboBox<Object> logicComboBox = new JComboBox<>(
			new DefaultComboBoxModel<>(IRuleBase.msgs(RuleExprLogicType.class)));

	protected boolean isok = false;
	private JLabel label_11;
	private JScrollPane scrollPane_2;
	private JTable filterTable;
	private JLabel label;
	private JComboBox<RulePeriod> periodView;

	protected <T> void clearComboBox(JComboBox<T> comboBox) {
		comboBox.setModel(new DefaultComboBoxModel<T>());
		comboBox.setSelectedIndex(-1);
	}

	protected void clear() {
		clearComboBox(itemComboBox);
		clearComboBox(attrComboBox);

		int row = filterTable.convertRowIndexToModel(filterTable.getSelectedRow());
		if (row == -1)
			return;

		DefaultTableModel model = (DefaultTableModel) filterTable.getModel();
		model.setValueAt(null, row, 0);
		model.setValueAt(null, row, 2);
		model.setValueAt(null, row, 3);
		model.setValueAt(null, row, 4);
		model.setValueAt(null, row, 5);
		model.setValueAt(null, row, 6);
	}

	public void configFilterTable() {
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
						configComboBox(itemComboBox, DynamicParamInfoBuilder.queryGroups(APSConfigure.getConfigDBName(),
								GroupType.gtDevice));
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}
					break;
				case ftMaterial:
					try {
						configComboBox(itemComboBox, DynamicParamInfoBuilder.queryGroups(APSConfigure.getConfigDBName(),
								GroupType.gtMaterial));
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}
					break;
				case ftModule:
					try {
						configComboBox(itemComboBox, DynamicParamInfoBuilder.queryGroups(APSConfigure.getConfigDBName(),
								GroupType.gtDevice));
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

		filterTable.getColumnModel().getColumn(7)
				.setCellEditor(new NumberCellEditor(new SpinnerNumberModel(0, 0, 100, 1)));
		filterTable.getColumnModel().getColumn(TABLE_RIGHT_LOGIC_INDEX)
				.setCellEditor(new DefaultCellEditor(logicComboBox));
		filterTable.getColumnModel().getColumn(TABLE_LEFT_LOGIC_INDEX)
				.setCellEditor(new DefaultCellEditor(logicComboBox));
		filterTable.getColumnModel().getColumn(TABLE_TYPE_INDEX).setCellEditor(new DefaultCellEditor(typeComboBox));
		filterTable.getColumnModel().getColumn(TABLE_ITEM_INDEX).setCellEditor(new DefaultCellEditor(itemComboBox));
		filterTable.getColumnModel().getColumn(TABLE_ATTR_INDEX).setCellEditor(new DefaultCellEditor(attrComboBox));
		filterTable.getColumnModel().getColumn(TABLE_OPERATION_INDEX).setCellEditor(new DefaultCellEditor(
				new JComboBox<>(new DefaultComboBoxModel<>(IRuleBase.msgs(RuleExprOperation.class)))));
		filterTable.getColumnModel().getColumn(TABLE_VALUE_INDEX)
				.setCellEditor(new ButtonCellEditor(new ButtonActionListener() {

					protected void showEditor(int row, ActionResult actionResult, boolean isMaterial) {
						String tmp = (String) filterTable.getValueAt(row, 5);
						IDInfo[] selects = null;

						if (tmp != null && !tmp.isEmpty()) {
							JSONArray idArray = (JSONArray) JsonHelp.parseJson(tmp);
							if (idArray != null && !idArray.isEmpty()) {
								try {
									if (isMaterial) {
										selects = materialQuery.builder(APSConfigure.getConfigDBName(), idArray.toList(), true);
									} else
										selects = (IDInfo[]) deviceQuery.queryEquipments(idArray.toList(), true);
								} catch (Exception e1) {
									e1.printStackTrace();
									MsgHelper.showException(e1);
									return;
								}
							}
						}

						try {
							ItemSwitcher.Result<IDInfo> result = null;
							if (!isMaterial)
								result = ItemSwitcher.show((DeviceInfo[]) deviceQuery.queryEquipments(), selects);
							else {
								result = ItemSwitcher.show(materialQuery.builder(APSConfigure.getConfigDBName()), selects);
							}
							actionResult.isOk = result.isok;
							if (result.isok) {
								JSONArray data = new JSONArray();
								for (IDInfo info : result.datas) {
									data.put(info.id());
								}
								actionResult.data = data;
							}
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
					}

					@Override
					public void actionPerformed(ActionEvent e, EditorType editorType, ActionResult actionResult) {
						switch (editorType) {
						case etButton:
							int row = filterTable.convertRowIndexToModel(filterTable.getSelectedRow());
							if (row == -1)
								return;

							if (attrComboBox.getSelectedItem() != null) {
								return;
							}

							RuleTarget filterType = (RuleTarget) typeComboBox.getSelectedItem();
							if (filterType == null)
								return;

							switch (filterType) {
							case ftDevice:
							case ftModule:
								showEditor(row, actionResult, false);
								break;
							case ftMaterial:
								showEditor(row, actionResult, true);
								break;
							}
							break;
						case etText:
							break;
						}
					}
				}));
	}

	public void init(Rule rule) {

		if (rule != null) {
			this.rule = rule;
		}

		load();

	}

	public static class Result {
		public boolean isok = false;

		public Rule rule;
	}

	protected RuleExpr toRuleExpr(DefaultTableModel model, int row) {
		RuleExpr expr = new RuleExpr();
		expr.leftLogic = (RuleExprLogicType) model.getValueAt(row, TABLE_LEFT_LOGIC_INDEX);
		expr.rightLogic = (RuleExprLogicType) model.getValueAt(row, TABLE_RIGHT_LOGIC_INDEX);
		expr.type = (RuleTarget) model.getValueAt(row, TABLE_TYPE_INDEX);
		expr.item = (DynamicGroupInfo) model.getValueAt(row, TABLE_ITEM_INDEX);
		expr.attr = (DynamicParamInfo) model.getValueAt(row, TABLE_ATTR_INDEX);
		expr.operation = (RuleExprOperation) model.getValueAt(row, TABLE_OPERATION_INDEX);
		expr.value = model.getValueAt(row, TABLE_VALUE_INDEX);
		return expr;
	}

	protected void toModel(DefaultTableModel model, int row, RuleExpr expr) {
		model.setValueAt(expr.leftLogic, row, TABLE_LEFT_LOGIC_INDEX);
		model.setValueAt(expr.rightLogic, row, TABLE_RIGHT_LOGIC_INDEX);
		model.setValueAt(expr.type, row, TABLE_TYPE_INDEX);
		model.setValueAt(expr.item, row, TABLE_ITEM_INDEX);
		model.setValueAt(expr.attr, row, TABLE_ATTR_INDEX);
		model.setValueAt(expr.operation, row, TABLE_OPERATION_INDEX);
		model.setValueAt(expr.value, row, TABLE_VALUE_INDEX);
	}

	protected void save() {
		rule.name = nameView.getText();
		if (rule.name == null || rule.name.trim().isEmpty()) {
			MsgHelper.showWarn("规则名称没有填写！");
			return;
		}
		rule.name = rule.name.trim();
		if (rule.id == null || rule.id.isEmpty())
			rule.id = rule.name;
		
		rule.scope = (RuleScope) scopeView.getSelectedItem();
		rule.type = (RuleType) typeView.getSelectedItem();
		rule.part = (RulePart) partView.getSelectedItem();
		rule.period = (RulePeriod) periodView.getSelectedItem();
		rule.expr = exprView.getText();
		rule.desc = descView.getText();

		rule.exprs.clear();
		DefaultTableModel model = (DefaultTableModel) filterTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			RuleExpr expr = toRuleExpr(model, i);
			rule.exprs.add(expr);
		}
		isok = true;
		setVisible(false);
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
		nameView.setText(rule.name);
		scopeView.setSelectedItem(rule.scope);
		typeView.setSelectedItem(rule.type);
		partView.setSelectedItem(rule.part);
		periodView.setSelectedItem(rule.period);
		exprView.setText(rule.expr);
		descView.setText(rule.desc);

		DefaultTableModel model = makeDefaultTableModel(filterTable);
		model.setRowCount(rule.exprs.size());
		int index = 0;
		for (RuleExpr expr : rule.exprs) {
			toModel(model, index++, expr);
		}

		filterTable.setModel(model);
		
		configFilterTable();
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
	public APSRuleEditor() {
		setResizable(false);

		setTitle("规则编辑");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(10, -45, 1257, 816);
		getContentPane().setLayout(new BorderLayout());

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		nameView = new JTextField();
		nameView.setPreferredSize(new Dimension(200, 27));
		nameView.setMinimumSize(new Dimension(200, 27));
		nameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		nameView.setColumns(10);
		nameView.setBounds(133, 15, 1037, 27);
		panel.add(nameView);

		JLabel label_2 = new JLabel(" 规则名称 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_2.setBounds(71, 20, 62, 17);
		panel.add(label_2);

		JLabel label_4 = new JLabel(" 处理过程 ");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_4.setBounds(71, 62, 56, 17);
		panel.add(label_4);

		partView = new JComboBox<>();
		partView.setModel(new DefaultComboBoxModel<>(IRuleBase.msgs(RulePart.class)));
		partView.setSelectedIndex(0);
		partView.setMaximumSize(new Dimension(200, 32767));
		partView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		partView.setBounds(133, 57, 167, 27);
		panel.add(partView);

		label_5 = new JLabel(" 作用域 ");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_5.setBounds(647, 62, 56, 17);
		panel.add(label_5);

		scopeView = new JComboBox<>();
		scopeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RuleScope scope = (RuleScope) scopeView.getSelectedItem();
				if (scope == RuleScope.rsBOM && (bomId == null || bomId.isEmpty())) {
					MsgHelper.showMessage("当前未选择BOM，不能设置为BOM作用域！");
					scopeView.setSelectedIndex(0);
					return;
				}
				if (scope == RuleScope.rsNode
						&& (bomId == null || bomId.isEmpty() || bommxId == null || bommxId.isEmpty())) {
					MsgHelper.showMessage("当前未选择BOM节点，不能设置为节点作用域！");
					scopeView.setSelectedIndex(0);
					return;
				}
			}
		});
		scopeView.setModel(new DefaultComboBoxModel<>(IRuleBase.msgs(RuleScope.class)));
		scopeView.setSelectedIndex(0);
		scopeView.setMaximumSize(new Dimension(200, 32767));
		scopeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scopeView.setBounds(709, 57, 167, 27);
		panel.add(scopeView);

		label_6 = new JLabel(" 规则类别 ");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_6.setBounds(359, 62, 56, 17);
		panel.add(label_6);

		typeView = new JComboBox<>();
		typeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		typeView.setModel(new DefaultComboBoxModel<>(IRuleBase.msgs(RuleType.class)));
		typeView.setSelectedIndex(0);
		typeView.setMaximumSize(new Dimension(200, 32767));
		typeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		typeView.setBounds(420, 57, 167, 27);
		panel.add(typeView);

		label_8 = new JLabel(" 高级规则表达式 ");
		label_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_8.setBounds(71, 429, 119, 17);
		panel.add(label_8);

		JLabel label_9 = new JLabel(" 规则说明 ");
		label_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_9.setBounds(71, 608, 119, 17);
		panel.add(label_9);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton("确定");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		btnNewButton.setMargin(new Insets(2, 10, 2, 10));
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(btnNewButton);

		JButton button = new JButton("取消");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		button.setMargin(new Insets(2, 10, 2, 10));
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(71, 448, 1100, 145);
		panel.add(scrollPane);

		exprView = new JTextArea();
		scrollPane.setViewportView(exprView);
		exprView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(71, 628, 1100, 98);
		panel.add(scrollPane_1);

		descView = new JTextArea();
		scrollPane_1.setViewportView(descView);
		descView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_11 = new JLabel(" 简单过滤条件 ");
		label_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_11.setBounds(71, 108, 119, 17);
		panel.add(label_11);

		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(71, 127, 1099, 287);
		panel.add(scrollPane_2);

		filterTable = new JTable();
		filterTable.setSelectionForeground(Color.PINK);
		filterTable.setFillsViewportHeight(true);
		filterTable.setCellSelectionEnabled(true);
		filterTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"\u5DE6\u903B\u8F91", "\u7C7B\u578B", "\u9879\u76EE", "\u5C5E\u6027", "\u64CD\u4F5C", "\u503C", "\u53F3\u903B\u8F91", "\u4F18\u5148\u7EA7"
			}
		));
		filterTable.getColumnModel().getColumn(0).setMaxWidth(100);
		filterTable.getColumnModel().getColumn(1).setMaxWidth(100);
		filterTable.getColumnModel().getColumn(6).setMaxWidth(100);
		filterTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		filterTable.setRowHeight(30);
		scrollPane_2.setViewportView(filterTable);
		AlternateRowColorTableRender.config(filterTable);

		JButton btnNewButton_1 = new JButton("+");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) filterTable.getModel();
				model.insertRow(model.getRowCount(), new Object[model.getColumnCount()]);
				int row = filterTable.convertRowIndexToView(model.getRowCount() - 1);
				filterTable.getSelectionModel().setSelectionInterval(row, row);
				filterTable.setRowHeight(row, 30);
			}
		});
		btnNewButton_1.setMargin(new Insets(2, 0, 2, 0));
		btnNewButton_1.setFont(new Font("微软雅黑", Font.BOLD, 12));
		btnNewButton_1.setBounds(1185, 136, 28, 28);
		panel.add(btnNewButton_1);

		JButton button_1 = new JButton("-");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = filterTable.convertRowIndexToModel(filterTable.getSelectedRow());
				if (row == -1)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的过滤条件？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				DefaultTableModel model = (DefaultTableModel) filterTable.getModel();

				if (filterTable.isEditing())
					filterTable.getCellEditor().cancelCellEditing();
				model.removeRow(row);
			}
		});
		button_1.setMargin(new Insets(2, 0, 2, 0));
		button_1.setFont(new Font("微软雅黑", Font.BOLD, 12));
		button_1.setBounds(1185, 169, 28, 28);
		panel.add(button_1);

		label = new JLabel(" 应用范围 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(940, 62, 56, 17);
		panel.add(label);

		periodView = new JComboBox<>(new DefaultComboBoxModel<>(IEnum.msgs(RulePeriod.class)));
		periodView.setToolTipText("当前规则如何应用到任务目标");
		periodView.setSelectedIndex(1);
		periodView.setMaximumSize(new Dimension(200, 32767));
		periodView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		periodView.setBounds(1002, 57, 167, 27);
		panel.add(periodView);

		setLocationRelativeTo(null);

	}

	public static Result showDialog(Rule rule, String bomId, String bommxId) {
		APSRuleEditor config = new APSRuleEditor();
		config.bomId = bomId;
		config.bommxId = bommxId;
		config.init(rule);

		config.setModal(true);
		config.setVisible(true);

		Result result = new Result();
		result.isok = config.isok;
		if (config.isok) {
			config.rule.id = config.rule.name;
			result.rule = config.rule;
		}

		config.dispose();

		return result;
	}
}
