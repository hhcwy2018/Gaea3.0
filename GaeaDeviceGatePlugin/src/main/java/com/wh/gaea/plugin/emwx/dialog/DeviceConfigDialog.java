package com.wh.gaea.plugin.emwx.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.grid.DataGridHelp;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.LinkInfo;
import com.wh.gaea.interfaces.selector.IExcelSelector;
import com.wh.gaea.plugin.datasource.dialog.UrlDataSourceConfig;
import com.wh.swing.tools.FieldInfo;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.swing.tools.adapter.ColorTableCellRender;
import com.wh.swing.tools.tree.TreeHelp;
import com.wh.tools.FileHelp;
import com.wh.tools.IdHelper;
import com.wh.tools.JsonHelp;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class DeviceConfigDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable dataProviderGrid;

	File templateFile;
	LinkInfo linkInfo;

	Integer bomIDCol;
	Integer bomPIDCol;
	Integer bomNameCol;
	Integer materialCodeCol;
	Integer materialIDCol;
	Integer masterBomIDCol;
	Integer bomStartTimeCol;
	Integer bomEndTimeCol;

	ColorTableCellRender colorTableCellRender = new ColorTableCellRender();

	File configFile = FileHelp.GetFile("bomimport.config", "emwx");

	protected boolean checkMaterials() throws Exception {
		DefaultTableModel model = (DefaultTableModel) dataProviderGrid.getModel();
		if (model.getRowCount() == 0)
			return false;

		if (materialCodeCol == null) {
			MsgHelper.showWarn("请先设置物料Code列！");
			return false;
		}

		if (materialIDCol == null) {
			MsgHelper.showWarn("请先设置物料ID列！");
			return false;
		}

		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("material_code,material_id");
		sqlBuilder.addTable("material");
		sqlBuilder.addWhere("used", Operation.otEqual, new Object[] { 1 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		IDataset dataset = db.query(sqlBuilder);

		Map<String, String> materials = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			materials.put(row.getValue("material_code").toString(), row.getValue("material_id").toString());
		}

		for (int i = 0; i < model.getRowCount(); i++) {
			String code = model.getValueAt(i, materialCodeCol).toString();
			if (!materials.containsKey(code)) {
				int row = dataProviderGrid.convertRowIndexToView(i);
				dataProviderGrid.setRowSelectionInterval(row, row);
				MsgHelper.showException("物料编码【" + code + "】在当前物料列表中不存在，请修改后再试！");
				return false;
			}
			model.setValueAt(materials.get(code), i, materialIDCol);
		}

		return true;
	}

	protected void initColors() {
		try {
			saveColors();
			loadColors();
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	protected void saveColors() throws Exception {
		JSONObject data = new JSONObject();
		data.put("bomIDCol", bomIDCol);
		data.put("bomPIDCol", bomPIDCol);
		data.put("bomNameCol", bomNameCol);
		data.put("materialCodeCol", materialCodeCol);
		data.put("materialIDCol", materialIDCol);
		data.put("masterBomIDCol", masterBomIDCol);
		data.put("bomStartTimeCol", bomStartTimeCol);
		data.put("bomEndTimeCol", bomEndTimeCol);
		JsonHelp.saveJson(configFile, data, null);
	}

	protected void loadColors() throws Exception {
		if (!configFile.exists())
			return;

		colorTableCellRender.resetColColor();
		
		JSONObject data = (JSONObject) JsonHelp.parseCacheJson(configFile, null);
		if (data.has("bomStartTimeCol")) {
			bomStartTimeCol = data.getInt("bomStartTimeCol");
			colorTableCellRender.setColColor(bomStartTimeCol, starttimeButton.getBackground());
		}

		if (data.has("bomEndTimeCol")) {
			bomEndTimeCol = data.getInt("bomEndTimeCol");
			colorTableCellRender.setColColor(bomEndTimeCol, endtimeButton.getBackground());
		}

		if (data.has("bomIDCol")) {
			bomIDCol = data.getInt("bomIDCol");
			colorTableCellRender.setColColor(bomIDCol, bomidView.getBackground());
		}

		if (data.has("bomPIDCol")) {
			bomPIDCol = data.getInt("bomPIDCol");
			colorTableCellRender.setColColor(bomPIDCol, bompidView.getBackground());
		}

		if (data.has("bomNameCol")) {
			bomNameCol = data.getInt("bomNameCol");
			colorTableCellRender.setColColor(bomNameCol, bomnameView.getBackground());
		}

		if (data.has("materialCodeCol")) {
			materialCodeCol = data.getInt("materialCodeCol");
			colorTableCellRender.setColColor(materialCodeCol, materialcodeView.getBackground());
		}

		if (data.has("materialIDCol")) {
			materialIDCol = data.getInt("materialIDCol");
			colorTableCellRender.setColColor(materialIDCol, materialidView.getBackground());
		}

		if (data.has("masterBomIDCol")) {
			masterBomIDCol = data.getInt("masterBomIDCol");
			colorTableCellRender.setColColor(masterBomIDCol, bommasteridView.getBackground());
		}

		dataProviderGrid.repaint();
	}

	IMainControl mainControl;
	private JToolBar toolBar2;
	private JScrollPane scrollPane_2;
	private JTextField idView;
	private JButton btnbom;
	private JButton mapButton;
	private JButton bomidView;
	private JButton bompidView;
	private JSplitPane splitPane;
	private JPanel panel;
	private JPanel panel_1;
	private JScrollPane scrollPane;
	private JTree modelTree;
	private JButton bomnameView;
	private JLabel lblbom;
	private JTextField nameView;
	private JButton materialidView;
	private final JToolBar toolBar = new JToolBar();
	private JLabel lblBom_2;
	private JComboBox<String> datatypeView;
	private JLabel lblBom_3;
	private JComboBox<String> commandTypeView;
	private JLabel labe1;
	private JComboBox<String> notifytypeView;
	private JLabel lblBom_4;
	private JLabel lblBom_5;
	private JTextField unitView;
	private JButton bommasteridView;
	private JButton button;
	private JButton materialcodeView;
	private JButton button_2;
	private JButton starttimeButton;
	private JButton endtimeButton;
	private JPanel panel_2;
	private JToolBar toolBar_1;
	private JTextField dateformatView;
	private JLabel label;
	private JTextField joinView;

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

	protected void initTableProvider() throws Exception {
		IExcelSelector excelSelector = GlobalInstance.instance().getExcelSelector();
		JSONObject data = excelSelector.importToDataset(templateFile, linkInfo.excelFile);

		if (data == null || !data.has("header") || !data.has("data")) {
			return;
		}

		JSONArray headers = data.getJSONArray("header");
		if (headers.length() == 0)
			return;

		Object[] columns = new Object[headers.length()];
		int index = 0;
		DefaultComboBoxModel<FieldInfo> fieldsModel = new DefaultComboBoxModel<>();
		for (Object obj : headers) {
			JSONObject header = (JSONObject) obj;
			FieldInfo info = new FieldInfo();
			info.field = header.getString("id");
			info.name = header.getString("name");
			info.valueType = Class.forName(header.getString("type"));

			fieldsModel.addElement(info);
			columns[index++] = info;
		}

		JSONArray dataset = data.getJSONArray("data");
		Object[][] rows = null;
		if (dataset.length() > 0)
			rows = new Object[dataset.length()][headers.length()];
		int rowindex = 0;
		for (Object obj : dataset) {
			JSONObject row = (JSONObject) obj;
			int colindex = 0;
			for (Object tmp : columns) {
				FieldInfo col = (FieldInfo) tmp;
				rows[rowindex][colindex++] = row.has(col.field) ? row.get(col.field) : null;
			}
			rowindex++;
		}

		DefaultTableModel model = new DefaultTableModel(rows, columns);
		dataProviderGrid.setModel(model);

		for (int i = 0; i < dataProviderGrid.getColumnCount(); i++) {
			TableColumn column = dataProviderGrid.getColumnModel().getColumn(i);

			column.setCellRenderer(colorTableCellRender);
		}

		for (int i = 0; i < dataProviderGrid.getColumnCount(); i++) {
			TableColumn column = dataProviderGrid.getColumnModel().getColumn(i);
			column.setWidth(200);
			column.setPreferredWidth(column.getWidth());
		}

		dataProviderGrid.repaint();
	}

	class TreeInfos {
		class TreeInfo {
			public Object name;
			public int tableRow;
			public int id;
			public String newId;
			public Integer pid;

			@Override
			public String toString() {
				return name.toString();
			}
		}

		class TableMapInfo {
			public Integer pid;
			public int rowIndex;
		}

		Map<Integer, DefaultMutableTreeNode> existTreeNodeMap = new HashMap<>();

		protected Integer getValue(DefaultTableModel model, int row, int col) {
			Object obj = model.getValueAt(row, col);
			if (obj == null)
				return null;

			if (obj instanceof Integer)
				return (int) obj;
			else {
				String tmp = obj.toString().trim();
				if (tmp.isEmpty())
					return null;
				return Integer.parseInt(tmp);
			}
		}

		protected DefaultMutableTreeNode createTreeNode(int id, Integer pid, Map<Integer, TableMapInfo> idMap,
				DefaultTreeModel model) {
			TreeInfo info = new TreeInfo();
			info.id = id;
			info.pid = pid;
			info.newId = IdHelper.genOrderID((long) info.id, "bom");
			info.tableRow = idMap.containsKey(info.id) ? idMap.get(info.id).rowIndex : 0;
			info.name = dataProviderGrid.getValueAt(info.tableRow, bomNameCol);

			DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
			existTreeNodeMap.put(info.id, node);
			DefaultMutableTreeNode parent = existTreeNodeMap.get(pid);
			if (parent == null)
				parent = (DefaultMutableTreeNode) model.getRoot();
			model.insertNodeInto(node, parent, model.getChildCount(parent));

			return node;
		}

		protected void convertToInteger(DefaultTableModel model, int row, int col) {
			Object obj = model.getValueAt(row, col);
			int intValue = 0;
			if (obj != null) {
				if (obj instanceof Integer) {
					intValue = (Integer) obj;
				} else {
					String value = (String) obj;
					int index = value.lastIndexOf(IdHelper.SPLITCHAR);
					intValue = Integer.parseInt(value.substring(index + 1));
				}
			}

			model.setValueAt(intValue, row, col);
		}

		public void reset() {
			DefaultTableModel oldModel = (DefaultTableModel) dataProviderGrid.getModel();
			if (oldModel.getRowCount() == 0)
				return;

			DefaultTableModel model = new DefaultTableModel();
			model.setRowCount(oldModel.getRowCount());
			model.setColumnCount(oldModel.getColumnCount());
			for (int i = 0; i < oldModel.getRowCount(); i++) {
				for (int j = 0; j < oldModel.getColumnCount(); j++) {
					model.setValueAt(oldModel.getValueAt(i, j), i, j);
				}
				convertToInteger(model, i, bomIDCol);
				convertToInteger(model, i, bomPIDCol);
			}

			Map<Integer, List<Integer>> treeInfoMap = new TreeMap<>();
			Map<Integer, TableMapInfo> idMap = new HashMap<>();
			for (int i = 0; i < model.getRowCount(); i++) {
				Integer id = getValue(model, i, bomIDCol);
				Integer pid = getValue(model, i, bomPIDCol);

				if (idMap.containsKey(id)) {
					MsgHelper.showException("bomID【" + id + "】已经存在，请修正后重试！");
					dataProviderGrid.setRowSelectionInterval(i, i);
					dataProviderGrid.setColumnSelectionInterval(bomIDCol, bomIDCol);
					return;
				}

				if (pid == null) {
					pid = 0;
				}

				TableMapInfo mapInfo = new TableMapInfo();
				mapInfo.pid = pid;
				mapInfo.rowIndex = i;
				idMap.put(id, mapInfo);

				List<Integer> nodes;
				if (!treeInfoMap.containsKey(pid)) {
					nodes = new ArrayList<>();
					treeInfoMap.put(pid, nodes);
				} else {
					nodes = treeInfoMap.get(pid);
				}

				nodes.add(id);
			}

			DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));

			for (Entry<Integer, List<Integer>> entry : treeInfoMap.entrySet()) {
				Integer id = entry.getKey();
				for (Integer child : entry.getValue()) {
					createTreeNode(child, id, idMap, treeModel);
				}
			}

			modelTree.setModel(treeModel);
			TreeHelp.expandOrCollapse(modelTree, (DefaultMutableTreeNode) treeModel.getRoot(), true);
		}
	}

	protected void configButton(JButton button) {
		BufferedImage img = new BufferedImage(button.getPreferredSize().width, button.getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(button.getBackground());
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		button.setIcon(new ImageIcon(img));
		button.setHorizontalTextPosition(JButton.CENTER);
		button.setVerticalTextPosition(JButton.CENTER);
	}
	
	/**
	 * Create the dialog.
	 */
	public DeviceConfigDialog(IMainControl mainControl) {
		this.mainControl = mainControl;

		setTitle("设备对接文件配置助手");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1336, 745);
		getContentPane().setLayout(new BorderLayout());
		toolBar2 = new JToolBar();
		getContentPane().add(toolBar2, BorderLayout.SOUTH);
		toolBar2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.setFloatable(false);
		toolBar2.addSeparator();

		bomidView = new JButton(" ID列 ");
		bomidView.setBackground(Color.ORANGE);
		bomidView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				bomIDCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();
			}
		});
		bomidView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(bomidView);

		bompidView = new JButton(" 父ID列 ");
		bompidView.setBackground(Color.GREEN);
		bompidView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				bomPIDCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();

			}
		});
		bompidView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(bompidView);

		bomnameView = new JButton(" 名称列 ");
		bomnameView.setBackground(Color.CYAN);
		bomnameView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				bomNameCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();

			}
		});
		bomnameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(bomnameView);

		materialidView = new JButton(" 物料ID列 ");
		materialidView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				materialIDCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();
			}
		});
		materialidView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		materialidView.setBackground(Color.PINK);
		toolBar2.add(materialidView);

		bommasteridView = new JButton(" 主BOMID列 ");
		bommasteridView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				masterBomIDCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();
			}
		});

		materialcodeView = new JButton(" 物料Code列 ");
		materialcodeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				materialCodeCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();
			}
		});
		materialcodeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		materialcodeView.setBackground(Color.MAGENTA);
		toolBar2.add(materialcodeView);
		bommasteridView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		bommasteridView.setBackground(Color.YELLOW);
		toolBar2.add(bommasteridView);

		starttimeButton = new JButton(" 有效期起始列 ");
		starttimeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				bomStartTimeCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();
			}
		});
		starttimeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		starttimeButton.setBackground(SystemColor.activeCaption);
		toolBar2.add(starttimeButton);

		endtimeButton = new JButton(" 有效期结束列 ");
		endtimeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviderGrid.getSelectedColumn() == -1) {
					MsgHelper.showMessage("请先选择一列后再试！");
					return;
				}

				bomEndTimeCol = dataProviderGrid.convertColumnIndexToModel(dataProviderGrid.getSelectedColumn());
				initColors();
			}
		});
		endtimeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		endtimeButton.setBackground(SystemColor.info);
		toolBar2.add(endtimeButton);
		
		toolBar2.addSeparator();
		
		btnbom = new JButton(" 建立BOM树 ");
		btnbom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) dataProviderGrid.getModel();
				if (model == null || model.getRowCount() == 0) {
					MsgHelper.showMessage("请先执行【打开】操作以装载数据；或者您打开的数据集合未空，请检查并修正后再试！");
					return;
				}
				TreeInfos infos = new TreeInfos();
				infos.reset();
			}
		});
		btnbom.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(btnbom);

		button = new JButton(" 生成 ");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);

		toolBar2.addSeparator();

		mapButton = new JButton(" 设置映射文件 ");
		mapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.selectOpenFile(null, null, null, "excel数据映射文件=xls;xlsx");
				if (file == null)
					return;
				try {
					templateFile = file;
					setTitle("数据预处理 - 当前映射配置：" + FileHelp.removeExt(templateFile.getName()));
				} catch (Exception e2) {
					SwingTools.showErrorMessage(null, e2);
				}
			}
		});
		mapButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(mapButton);

		JButton btnexcel = new JButton(" 打开 ");
		toolBar2.add(btnexcel);
		btnexcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (templateFile == null) {
					MsgHelper.showMessage("请先【 设置数据映射 】！");
					return;
				}
				
				File file = SwingTools.selectOpenFile(null, null, null, "excel数据文件=xls;xlsx");
				if (file == null)
					return;
				try {
					linkInfo = new LinkInfo(file, null);
					initTableProvider();
				} catch (Exception e2) {
					SwingTools.showErrorMessage(null, e2);
				}
			}
		});

		btnexcel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		JButton btnexcel_1 = new JButton(" 保存 ");
		toolBar2.add(btnexcel_1);
		btnexcel_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnexcel_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		toolBar2.addSeparator();
		
		button_2 = new JButton(" 导入 ");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button_2);

		splitPane = new JSplitPane();
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});

		panel_2 = new JPanel();
		getContentPane().add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel lblBom = new JLabel(" 编码  ");
		toolBar.setFloatable(false);
		panel_2.add(toolBar);
		toolBar.add(lblBom);
		lblBom.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		idView = new JTextField();
		toolBar.add(idView);
		idView.setMinimumSize(new Dimension(80, 27));
		idView.setPreferredSize(new Dimension(80, 27));
		idView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		idView.setColumns(10);

		lblbom = new JLabel(" 名称  ");
		toolBar.add(lblbom);
		lblbom.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		nameView = new JTextField();
		toolBar.add(nameView);
		nameView.setPreferredSize(new Dimension(80, 27));
		nameView.setMinimumSize(new Dimension(80, 27));
		nameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		nameView.setColumns(10);

		lblBom_5 = new JLabel(" 单位  ");
		lblBom_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblBom_5);

		unitView = new JTextField();
		unitView.setPreferredSize(new Dimension(80, 27));
		unitView.setMinimumSize(new Dimension(80, 27));
		unitView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		unitView.setColumns(10);
		toolBar.add(unitView);
										
												labe1 = new JLabel(" 打包类型  ");
												toolBar.add(labe1);
												labe1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
										
												notifytypeView = new JComboBox<>();
												toolBar.add(notifytypeView);
												notifytypeView.setModel(new DefaultComboBoxModel<>(new String[] {"none", "package", "alert", "state"}));
												notifytypeView.setSelectedIndex(0);
												notifytypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
								
										lblBom_2 = new JLabel(" 数据类型  ");
										toolBar.add(lblBom_2);
										lblBom_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
								
										datatypeView = new JComboBox<>();
										toolBar.add(datatypeView);
										datatypeView.setModel(new DefaultComboBoxModel<>(new String[] {"short", "int", "string", "float", "bool"}));
										datatypeView.setSelectedIndex(0);
										datatypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
						
								lblBom_3 = new JLabel(" 指令类型  ");
								toolBar.add(lblBom_3);
								lblBom_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
								
										commandTypeView = new JComboBox<>();
										toolBar.add(commandTypeView);
										commandTypeView.setModel(new DefaultComboBoxModel<>(new String[] {"read", "write", "check", "value", "notify", "cmd", "combine"}));
										commandTypeView.setSelectedIndex(1);
										commandTypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		panel_2.add(toolBar_1);

		lblBom_4 = new JLabel(" 日期格式  ");
		toolBar_1.add(lblBom_4);
		lblBom_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		
		dateformatView = new JTextField();
		dateformatView.setText("yyyy-MM-dd hh:mm:ss");
		dateformatView.setPreferredSize(new Dimension(300, 27));
		dateformatView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dateformatView.setColumns(10);
		toolBar_1.add(dateformatView);
		
		label = new JLabel(" 连接格式  ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label);
		
		joinView = new JTextField();
		joinView.setText("#{dotime_year,4,0}-#{dotime_month,2,0}-#{dotime_day,2,0} #{dotime_hour,2,0}:#{dotime_minute,2,0}:#{dotime_second,2,0}");
		joinView.setPreferredSize(new Dimension(80, 27));
		joinView.setMinimumSize(new Dimension(80, 27));
		joinView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		joinView.setColumns(10);
		toolBar_1.add(joinView);

		splitPane.setResizeWeight(0.3);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		scrollPane_2 = new JScrollPane();
		panel.add(scrollPane_2);
		dataProviderGrid = new ModelSearchView.TableModelSearchView();
		dataProviderGrid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataProviderGrid.setFillsViewportHeight(true);

		DataGridHelp.FitTableColumns(dataProviderGrid);

		dataProviderGrid.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (dataProviderGrid.getSelectedRow() == -1 || dataProviderGrid.getSelectedColumn() == -1)
					return;

				if (dataProviderGrid.isEditing())
					return;

				dataProviderGrid.editCellAt(dataProviderGrid.getSelectedRow(), dataProviderGrid.getSelectedColumn());
				dataProviderGrid.getEditorComponent().requestFocus();
			}
		});
		dataProviderGrid.setColumnSelectionAllowed(true);
		dataProviderGrid.setCellSelectionEnabled(true);
		dataProviderGrid.setRowHeight(30);
		dataProviderGrid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(dataProviderGrid);

		panel_1 = new JPanel();
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		modelTree = new JTree(new DefaultTreeModel(null));
		modelTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		modelTree.setRowHeight(28);
		modelTree.setRootVisible(false);
		scrollPane.setViewportView(modelTree);

		try {
			loadColors();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		configButton(bomidView);
		configButton(bompidView);
		configButton(bomnameView);
		configButton(materialidView);
		configButton(materialcodeView);
		configButton(bommasteridView);
		configButton(starttimeButton);
		configButton(endtimeButton);
		
		SwingTools.showMaxDialog(this);

	}

	public static void show(IMainControl mainControl) {
		DeviceConfigDialog config = new DeviceConfigDialog(mainControl);
		config.setModal(true);
		config.setVisible(true);
		config.dispose();
	}
}
