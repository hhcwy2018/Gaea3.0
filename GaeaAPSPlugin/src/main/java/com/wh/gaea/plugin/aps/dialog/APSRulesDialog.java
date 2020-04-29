package com.wh.gaea.plugin.aps.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.control.modelsearch.ModelSearchView.TableModelSearchView;
import com.wh.gaea.industry.builder.BOMBuilder;
import com.wh.gaea.industry.builder.BOMBuilder.TreeInfo;
import com.wh.gaea.industry.builder.BomOper;
import com.wh.gaea.industry.builder.BomOper.ISetBomList;
import com.wh.gaea.industry.builder.CustomerBuilder;
import com.wh.gaea.industry.builder.CustomerBuilder.ISetUserList;
import com.wh.gaea.industry.info.BomInfo;
import com.wh.gaea.industry.info.Customer;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.interfaces.IRuleBase;
import com.wh.gaea.plugin.aps.rule.Rule;
import com.wh.gaea.plugin.aps.rule.RulePart;
import com.wh.gaea.plugin.aps.rule.RuleScope;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;
import com.wh.gaea.plugin.aps.rule.RuleType;
import com.wh.gaea.plugin.aps.rule.connector.BomConnector;
import com.wh.gaea.plugin.aps.rule.connector.BommxConnector;
import com.wh.gaea.plugin.aps.rule.connector.RuleConnector;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.tree.TreeHelp;

public class APSRulesDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	BomOper bomOper = new BomOper();
	RuleStructInfo structInfo;

	class RuleAttatchInfo {
		public JTable table;

		/***
		 * 
		 * 仅可以在一次调用中使用，跨调用边界的使用，索引可能无效
		 */
		public int row;

		public RuleAttatchInfo(JTable table, int row) {
			this.table = table;
			this.row = row;
		}
	}

	protected RulePart getSelectRulePart() {
		return (RulePart) rulepartView.getSelectedItem();
	}

	public void queryRules(JTable table, RuleScope scope, RuleType type, String ...keys) throws Exception {

		List<Rule> rules = structInfo.queryRules(getSelectRulePart(), scope, type, keys);

		Object[] columns = Rule.names();
		Object[][] rows = rules.size() == 0 ? null : new Object[rules.size()][];
		if (rows != null)
			for (int i = 0; i < rows.length; i++) {
				rows[i] = rules.get(i).values();
			}
		DefaultTableModel model = new DefaultTableModel((Object[][]) rows, columns);
		table.setModel(model);
	}

	public void queryBomRules() throws Exception {
		queryRules(bomGrid, RuleScope.rsBOM, null);
	}

	public void queryGlobalRules() throws Exception {
		queryRules(globalGrid, RuleScope.rsGlobal, null);
	}

	public void queryBomNodeRules() throws Exception {
		queryRules(bomnodeGrid, RuleScope.rsNode, null);
	}

	public Rule getGridSelectedRule(JTable table) {
		if (table.getSelectedRow() == -1)
			return null;

		int row = table.convertRowIndexToModel(table.getSelectedRow());
		Rule rule = RuleStructInfo.createRule((DefaultTableModel) table.getModel(), row);
		rule.attatchObject = new RuleAttatchInfo(table, row);
		return rule;
	}

	public JTable getActiveTable() {
		switch (tabbedPane.getSelectedIndex()) {
		case 1:
			return bomGrid;
		case 2:
			return bomnodeGrid;
		default:
			return globalGrid;
		}
	}

	public void updateRuleModel(Rule rule) {
		JTable table = getActiveTable();
		boolean isAdd = rule.attatchObject == null;
		if (isAdd) {
			rule.attatchObject = new RuleAttatchInfo(table, 0);
		}

		RuleAttatchInfo info = (RuleAttatchInfo) rule.attatchObject;
		DefaultTableModel model = (DefaultTableModel) info.table.getModel();
		if (model == null) {
			model = new DefaultTableModel(Rule.names(), 0);
		}

		int row = info.row;
		Object[] values = rule.values();
		if (isAdd) {
			model.insertRow(row, new Vector<Object>());
		}
		
		for (int i = 0; i < model.getColumnCount(); i++) {
			model.setValueAt(values[i], row, i);
		}

		table.updateUI();
	}

	public Rule getSelectRule() {
		switch (tabbedPane.getSelectedIndex()) {
		case 0:
			return getGridSelectedRule(globalGrid);
		case 1:
			return getGridSelectedRule(bomGrid);
		default:
			return getGridSelectedRule(bomnodeGrid);
		}
	}

	public RuleStructInfo getRuleStructInfo() {
		return structInfo;
	}

	public <T extends RuleConnector> void loadRules(Class<T> c, JTable table, String filter) throws Exception {
		loadRules(getRuleStructInfo().id, c, table, filter);
	}

	public <T extends RuleConnector> void loadRules(String struct_id, Class<T> c, JTable table, String filter)
			throws Exception {

		List<Rule> rules = new ArrayList<>();
		Map<RuleConnector, Rule> connectorMap = getRuleStructInfo().getRules(c);

		for (Entry<RuleConnector, Rule> entry : connectorMap.entrySet()) {
			if (filter != null && !filter.isEmpty()) {
				if (entry.getKey().getFilter().equalsIgnoreCase(filter))
					rules.add(entry.getValue());
			} else {
				rules.add(entry.getValue());
			}
		}

		loadRawRules(struct_id, rules, table);
	}

	public void loadRawRules(String struct_id, Collection<Rule> connectors, JTable table) throws Exception {

		DefaultTableModel model = null;

		model = new DefaultTableModel(Rule.names(), 0);
		for (Rule rule : connectors) {
			model.addRow(rule.values());
		}

		table.setModel(model);
		table.updateUI();
	}

	IMainControl mainControl;

	private JToolBar toolBar_1;
	private JPanel panel_3;
	private JButton button_1;
	private JButton button_5;
	private JButton button;
	private JScrollPane scrollPane_1;
	private JTree bomTree;
	private JButton btnbom;
	private JLabel lblBom;
	private JLabel label_3;
	private JComboBox<RulePart> rulepartView;
	private JButton button_2;
	private JPanel panel;
	private JTabbedPane tabbedPane;
	private JPanel panel_1;
	private JScrollPane scrollPane;

	private TableModelSearchView bomGrid;
	private JPanel panel_4;
	private JScrollPane scrollPane_2;

	private TableModelSearchView globalGrid;
	private JPanel panel_5;
	private JScrollPane scrollPane_3;

	private TableModelSearchView bomnodeGrid;
	private JComboBox<BomInfo> bomcodeView;
	private JLabel label_5;
	private JComboBox<Customer> customerView;
	private JSplitPane splitPane;

	protected static void configRuleGrid(TableModelSearchView grid) {
		grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		grid.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		grid.setFillsViewportHeight(true);
		grid.getTableHeader().setReorderingAllowed(false);
		grid.setRowHeight(30);
		grid.setFont(new Font("微软雅黑", Font.PLAIN, 12));

	}

	public Rule saveRule(Rule cur) throws Exception {
		RuleStructInfo info = getRuleStructInfo();

		String bomId = null;
		String bommxId = null;
		if (tabbedPane.getSelectedIndex() == 2) {
			if (bomTree.getSelectionPath() == null) {
				MsgHelper.showMessage("请先选择一个BOM节点后再试！");
				return null;
			}

			TreeInfo treeInfo = (TreeInfo) ((DefaultMutableTreeNode) bomTree.getSelectionPath().getLastPathComponent())
					.getUserObject();
			if (treeInfo != null) {
				bomId = treeInfo.bom_id;
				bommxId = treeInfo.bommx_id;
			}
		}

		if (cur == null) {
			cur = new Rule();
			cur.name = "新建规则";
			cur.id = cur.name;
			cur.part = (RulePart) rulepartView.getSelectedItem();
			switch (tabbedPane.getSelectedIndex()) {
			case 0:
				cur.scope = RuleScope.rsGlobal;
				break;
			case 1:
				cur.scope = RuleScope.rsBOM;
				break;
			case 2:
				cur.scope = RuleScope.rsNode;
				break;
			}
		}
		
		APSRuleEditor.Result result = APSRuleEditor.showDialog(cur, bomId, bommxId);
		if (!result.isok)
			return null;

		info.saveRule(result.rule);
		info.saveConnector(result.rule, bomId, bommxId);
		result.rule.attatchObject = cur == null ? null : cur.attatchObject;
		
		APSConfigure.saveRuleStruct(structInfo);
		APSConfigure.saveCurrentRuleStruct(structInfo);
		
		return result.rule;

	}

	/**
	 * Create the dialog.
	 */
	public APSRulesDialog(IMainControl mainControl) {
		this.mainControl = mainControl;

		setTitle("排程规则设置");
		setIconImage(Toolkit.getDefaultToolkit().getImage(APSRulesDialog.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1354, 881);
		getContentPane().setLayout(new BorderLayout());
		panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));
		toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		toolBar_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_3.add(toolBar_1, BorderLayout.NORTH);

		lblBom = new JLabel(" BOM ");
		toolBar_1.add(lblBom);
		lblBom.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		bomcodeView = new JComboBox<>();
		bomcodeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		toolBar_1.add(bomcodeView);
		bomcodeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		label_5 = new JLabel(" 客户 ");
		toolBar_1.add(label_5);
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		customerView = new JComboBox<>();
		toolBar_1.add(customerView);
		customerView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (customerView.getSelectedIndex() == -1)
					return;

				Customer customer = (Customer) customerView.getSelectedItem();
				try {
					bomOper.initBomList(customer.bomids.toArray(new String[customer.bomids.size()]), new ISetBomList() {
						DefaultComboBoxModel<BomInfo> model = new DefaultComboBoxModel<>();

						@Override
						public void onBomList() {
							bomcodeView.setModel(model);
							if (bomcodeView.getItemCount() > 0)
								bomcodeView.setSelectedIndex(0);
						}

						@Override
						public void addNode(BomInfo info) {
							model.addElement(info);
						}
					});

				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		customerView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		btnbom = new JButton(" 打开 ");
		toolBar_1.add(btnbom);
		btnbom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BomInfo bomInfo = (BomInfo) bomcodeView.getSelectedItem();
				if (bomInfo == null) {
					MsgHelper.showMessage("请先选择BOM！");
					return;
				}

				try {
					loadRules(BomConnector.class, bomGrid, bomInfo.id);
					new BOMBuilder().builder(bomInfo.id, bomTree);
					TreeHelp.expandOrCollapse(bomTree, (DefaultMutableTreeNode) bomTree.getModel().getRoot(), true);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		btnbom.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		toolBar_1.addSeparator();

		label_3 = new JLabel(" 处理过程 ");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label_3);

		rulepartView = new JComboBox<>();
		rulepartView.setModel(new DefaultComboBoxModel<>(IRuleBase.msgs(RulePart.class)));
		rulepartView.setSelectedIndex(0);
		rulepartView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(rulepartView);

		button_2 = new JButton(" 查询 ");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					queryGlobalRules();
					queryBomRules();
					queryBomNodeRules();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button_2);

		toolBar_1.addSeparator();

		button_5 = new JButton(" 添加 ");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Rule rule = saveRule(null);
					if (rule != null) {
						updateRuleModel(rule);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}

		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button_5);

		button_1 = new JButton(" 编辑 ");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Rule rule = getSelectRule();
				if (rule == null) {
					MsgHelper.showMessage("请选择一项规则后重试！");
					return;
				}
				try {
					saveRule(rule);
					updateRuleModel(rule);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		button = new JButton(" 删除 ");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Rule rule = getSelectRule();
				if (rule == null)
					return;

				RuleStructInfo structInfo = getRuleStructInfo();
				try {

					structInfo.removeRule(rule);
					APSConfigure.saveCurrentRuleStruct(structInfo);
					APSConfigure.saveRuleStruct(structInfo);

					if (rule.attatchObject instanceof RuleAttatchInfo) {
						RuleAttatchInfo info = (RuleAttatchInfo) rule.attatchObject;
						((DefaultTableModel) info.table.getModel()).removeRow(info.row);
						info.table.updateUI();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}

			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button);
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button_1);

		panel = new JPanel();
		panel_3.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(tabbedPane);

		panel_4 = new JPanel();
		tabbedPane.addTab("全局规则", null, panel_4, null);
		panel_4.setLayout(new BorderLayout(0, 0));

		scrollPane_2 = new JScrollPane();
		panel_4.add(scrollPane_2, BorderLayout.CENTER);
		globalGrid = new ModelSearchView.TableModelSearchView();
		scrollPane_2.setViewportView(globalGrid);
		configRuleGrid(globalGrid);

		panel_1 = new JPanel();
		tabbedPane.addTab("BOM规则", null, panel_1, null);
		panel_1.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		bomGrid = new ModelSearchView.TableModelSearchView();
		scrollPane.setViewportView(bomGrid);
		configRuleGrid(bomGrid);

		panel_5 = new JPanel();
		tabbedPane.addTab("节点规则", null, panel_5, null);
		panel_5.setLayout(new BorderLayout(0, 0));

		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		panel_5.add(splitPane, BorderLayout.CENTER);

		scrollPane_3 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_3);
		bomnodeGrid = new ModelSearchView.TableModelSearchView();
		scrollPane_3.setViewportView(bomnodeGrid);
		configRuleGrid(bomnodeGrid);

		scrollPane_1 = new JScrollPane();
		splitPane.setLeftComponent(scrollPane_1);

		bomTree = new JTree((TreeModel)null);
		bomTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (bomTree.getSelectionPath() == null || bomTree.getSelectionPath().getLastPathComponent() == null)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) bomTree.getSelectionPath()
						.getLastPathComponent();

				if (node.getParent() != bomTree.getModel().getRoot()) {
					return;
				}

				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					if (MsgHelper.showConfirmDialog("是否从列表中删除选定的BOM，仅从列表中删除不会删除BOM信息，是否继续？",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						return;
					}

					((DefaultMutableTreeNode) node.getParent()).remove(node);
					bomTree.updateUI();
				}
			}
		});

		bomTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {

				if (bomTree.getSelectionPath() == null) {
					return;
				}

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) bomTree.getSelectionPath()
						.getLastPathComponent();
				if (!(node.getUserObject() instanceof TreeInfo)) {
					return;
				}

				TreeInfo info = (TreeInfo) node.getUserObject();

				try {
					loadRules(BommxConnector.class, bomnodeGrid, BommxConnector.getFilter(info.bom_id, info.bommx_id));
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		bomTree.setRowHeight(28);
		bomTree.setRootVisible(false);
		bomTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_1.setViewportView(bomTree);

		tabbedPane.setSelectedIndex(0);

		setLocationRelativeTo(null);
	}

	protected void init() {
		try {
			queryGlobalRules();
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}

		initCustomers();
	}

	private void initCustomers() {
		try {
			CustomerBuilder.builder(new ISetUserList() {

				DefaultComboBoxModel<Customer> model = new DefaultComboBoxModel<>();

				@Override
				public void onEnd() {
					customerView.setModel(model);
					if (model.getSize() > 0)
						customerView.setSelectedIndex(0);
				}

				@Override
				public void addNode(Customer info) {
					model.addElement(info);
				}
			});
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	public static void show(IMainControl mainControl, RuleStructInfo info) throws Exception {
		APSRulesDialog config = new APSRulesDialog(mainControl);
		if (info == null)
			throw new Exception("未提供架构信息！");

		config.structInfo = info;
		config.setTitle("编辑结构规则：" + info.id);

		config.init();
		config.setModal(true);
		config.setVisible(true);
		config.dispose();
	}
}
