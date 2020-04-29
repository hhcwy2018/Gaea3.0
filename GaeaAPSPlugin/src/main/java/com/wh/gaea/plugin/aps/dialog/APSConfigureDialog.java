package com.wh.gaea.plugin.aps.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.industry.builder.BomOper;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.dialog.StructDialog.Result;
import com.wh.gaea.plugin.aps.engine.EngineQuery;
import com.wh.gaea.plugin.aps.info.EngineInfo;
import com.wh.gaea.plugin.aps.info.RuleMapInfos;
import com.wh.gaea.plugin.aps.interfaces.IRuleBase;
import com.wh.gaea.plugin.aps.rule.OrderAllotType;
import com.wh.gaea.plugin.aps.rule.OrderTarget;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;
import com.wh.gaea.plugin.aps.rule.RuleStructInfos;
import com.wh.gaea.plugin.datasource.dialog.UrlDataSourceConfig;
import com.wh.swing.tools.MsgHelper;

public class APSConfigureDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	BomOper bomOper = new BomOper();
	RuleStructInfos structInfos;

	IMainControl mainControl;
	private final JToolBar toolBar = new JToolBar();

	protected static JSONArray tableToJson(JTable view) {

		List<String> fieldInfos = new ArrayList<>();
		for (int i = 0; i < view.getColumnModel().getColumnCount(); i++) {
			TableColumn tableColumn = view.getColumnModel().getColumn(i);
			fieldInfos.add((String) tableColumn.getHeaderValue());
		}
		JSONArray rows = new JSONArray();
		DefaultTableModel model = (DefaultTableModel) view.getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			JSONObject rowData = new JSONObject();
			for (int j = 0; j < model.getColumnCount(); j++) {
				rowData.put(fieldInfos.get(j), model.getValueAt(i, j));
			}
			rows.put(rowData);
		}
		return rows;
	}

	Map<String, Integer> materialMap = new HashMap<>();
	private JButton button;
	private JLabel label_2;
	private JComboBox<EngineInfo> engineView;
	private JComboBox<RuleStructInfo> structView;
	private JLabel label_4;
	private JButton button_3;
	private JPanel panel;
	private JButton button_1;
	private JButton button_5;
	private JButton button_6;
	private JComboBox<OrderAllotType> ruleallottypeView;
	private JComboBox<OrderTarget> ruletargetView;
	private JButton button_7;

	protected void save() throws Exception {
		EngineInfo engineInfo = (EngineInfo) engineView.getSelectedItem();
		RuleStructInfo structInfo = (RuleStructInfo) structView.getSelectedItem();

		if (engineInfo == null)
			throw new Exception("排程引擎未选择！");

		if (structInfo == null)
			throw new Exception("排程架构未选择！");

		APSConfigure.saveEngine(engineInfo);
		APSConfigure.saveCurrentRuleStruct(structInfo);
		APSConfigure.saveCurrentOrderAllotType((OrderAllotType) ruleallottypeView.getSelectedItem());
		APSConfigure.saveCurrentOrderTarget((OrderTarget) ruletargetView.getSelectedItem());
	}

	protected void load() throws Exception {

		RuleStructInfo structInfo = APSConfigure.loadCurrentRuleStruct();
		if (structInfo != null) {
			for (int i = 0; i < structView.getItemCount(); i++) {
				RuleStructInfo info = structView.getItemAt(i);
				if (info.toString().equalsIgnoreCase(structInfo.id)) {
					structView.setSelectedIndex(i);
					break;
				}
			}
		}

		EngineInfo engineInfo = APSConfigure.loadEngine();
		if (engineInfo != null) {
			for (int i = 0; i < engineView.getItemCount(); i++) {
				EngineInfo info = engineView.getItemAt(i);
				if (info.name.equalsIgnoreCase(engineInfo.name)) {
					engineView.setSelectedIndex(i);
					break;
				}
			}
		}

		ruleallottypeView.setSelectedItem(APSConfigure.loadCurrentOrderAllotType());
		ruletargetView.setSelectedItem(APSConfigure.loadCurrentOrderTarget());
		
	}

	/**
	 * Create the dialog.
	 */
	public APSConfigureDialog(IMainControl mainControl) {
		setResizable(false);
		this.mainControl = mainControl;

		setTitle("排程配置");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setSize(525, 300);
		getContentPane().setLayout(new BorderLayout());

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		label_2 = new JLabel(" 排程引擎 ");
		label_2.setBounds(33, 28, 62, 17);
		panel.add(label_2);
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		engineView = new JComboBox<>();
		engineView.setBounds(105, 25, 312, 23);
		panel.add(engineView);
		engineView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		label_4 = new JLabel(" 排程架构 ");
		label_4.setBounds(33, 76, 62, 17);
		panel.add(label_4);
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		structView = new JComboBox<>();
		structView.setBounds(105, 73, 312, 23);
		panel.add(structView);
		structView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton btnNewButton = new JButton("+");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Result result = StructDialog.showDialog();
				if (result.isok) {
					RuleStructInfo info = new RuleStructInfo();
					info.date = new Date();
					info.desc = result.desc;
					info.id = result.name;
					try {
						APSConfigure.saveRuleStruct(info);
						structInfos.addStructInfo(info);
						structView.addItem(info);
						structView.setSelectedIndex(structView.getItemCount() - 1);
						structView.updateUI();
					} catch (Exception e1) {
						MsgHelper.showException(e1);
						e1.printStackTrace();
					}

				}
			}
		});
		btnNewButton.setMargin(new Insets(2, 0, 2, 0));
		btnNewButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
		btnNewButton.setBounds(427, 70, 28, 28);
		panel.add(btnNewButton);

		button_6 = new JButton("-");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (structView.getSelectedItem() == null)
					return;

				if (MsgHelper.showConfirmDialog("删除选定的架构将解绑所有与此架构相关的规则，是否继续？", "警告",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				RuleStructInfo info = (RuleStructInfo) structView.getSelectedItem();
				try {
					structInfos.removeStructInfo(info.id);
					APSConfigure.removeRuleStruct(info);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_6.setMargin(new Insets(2, 0, 2, 0));
		button_6.setFont(new Font("微软雅黑", Font.BOLD, 12));
		button_6.setBounds(457, 70, 28, 28);
		panel.add(button_6);
		
		JLabel label = new JLabel(" 分配方式 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(33, 123, 62, 17);
		panel.add(label);
		
		ruleallottypeView = new JComboBox<>(new DefaultComboBoxModel<>(IRuleBase.msgs(OrderAllotType.class)));
		ruleallottypeView.setSelectedIndex(0);
		ruleallottypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		ruleallottypeView.setBounds(105, 120, 312, 23);
		panel.add(ruleallottypeView);
		
		JLabel label_1 = new JLabel(" 排程目标 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(33, 168, 62, 17);
		panel.add(label_1);
		
		ruletargetView = new JComboBox<>(new DefaultComboBoxModel<>(IRuleBase.msgs(OrderTarget.class)));
		ruletargetView.setSelectedIndex(0);
		ruletargetView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		ruletargetView.setBounds(105, 165, 312, 23);
		panel.add(ruletargetView);

		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		button = new JButton(" 排程测试");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});

		button_3 = new JButton(" 规则编辑 ");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (structView.getSelectedItem() == null) {
						MsgHelper.showMessage("请先选择一个架构！");
						return;
					}

					APSRulesDialog.show(mainControl, (RuleStructInfo) structView.getSelectedItem());
					refreshRuleStructs();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		button_1 = new JButton(" 刷新引擎 ");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshEngines();
			}
		});
		
		button_7 = new JButton(" 规则映射 ");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (structView.getSelectedItem() == null) {
					MsgHelper.showMessage("请先选择一个架构后再试！");
					return;
				}
				
				try {
					RuleStructInfo structInfo = (RuleStructInfo)structView.getSelectedItem();
					RuleMapInfos infos = structInfo.ruleMapInfos;
					APSRuleRelationEditor.showDialog(infos);
					APSConfigure.saveCurrentRuleStruct(structInfo);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_7);
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_1);

		button_5 = new JButton(" 刷新架构 ");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshRuleStructs();
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		toolBar.addSeparator();
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton button_2 = new JButton("确定");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					save();
					setVisible(false);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button_2);

		JButton button_4 = new JButton("取消");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(button_4);

		refreshRuleStructs();
		refreshEngines();
		setLocationRelativeTo(null);

	}

	private void refreshRuleStructs() {
		try {
			structInfos = APSConfigure.getRuleStructInfos();
			RuleStructInfo[] infos = structInfos.toArray();
			structView.setModel(infos == null ? new DefaultComboBoxModel<>() : new DefaultComboBoxModel<>(infos));
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	private void refreshEngines() {
		try {
			EngineInfo[] infos = EngineQuery.getEngines();

			engineView.setModel(infos.length == 0 ? new DefaultComboBoxModel<>() : new DefaultComboBoxModel<>(infos));
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	public static void show(IMainControl mainControl) throws Exception {
		APSConfigureDialog config = new APSConfigureDialog(mainControl);
		config.load();
		config.setModal(true);
		config.setVisible(true);
		config.dispose();
	}
}
