package com.wh.gaea.plugin.aps.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;

import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.industry.builder.BomOper;
import com.wh.gaea.industry.builder.MaterialInfoBuilder;
import com.wh.gaea.industry.interfaces.IMaterial;
import com.wh.gaea.industry.interfaces.IOrder;
import com.wh.gaea.industry.interfaces.IProduct;
import com.wh.gaea.industry.interfaces.IScheduleTask;
import com.wh.gaea.plugin.aps.configure.APSConfigure;
import com.wh.gaea.plugin.aps.info.EngineInfo;
import com.wh.gaea.plugin.aps.info.OrderProviderInfo;
import com.wh.gaea.plugin.aps.interfaces.IEngine.ScheduleResult;
import com.wh.gaea.plugin.aps.interfaces.IOrderProvider;
import com.wh.gaea.plugin.aps.provider.order.OrderProviderQuery;
import com.wh.gaea.plugin.aps.rule.RuleStructInfo;
import com.wh.gaea.plugin.datasource.dialog.UrlDataSourceConfig;
import com.wh.swing.tools.MsgHelper;

public class APSDialog extends JDialog {

	private static final int ORDER_ID_INDEX = 0;
	private static final int ORDER_CUSTOMERID_INDEX = 1;
	private static final int ORDER_COUNT_INDEX = 2;
	private static final int ORDER_WORKHOURS_INDEX = 3;
	private static final int ORDER_ENDTIME_INDEX = 4;
	private static final int ORDER_OBJECT_INDEX = 5;

	protected static final int SCHEDULE_ORDDERID_INDEX = 0;
	protected static final int SCHEDULE_MATERIALID_INDEX = 1;
	protected static final int SCHEDULE_DEVICEID_INDEX = 2;
	protected static final int SCHEDULE_STATIONID_INDEX = 3;
	protected static final int SCHEDULE_TASKID_INDEX = 4;
	protected static final int SCHEDULE_WORKERID_INDEX = 5;
	protected static final int SCHEDULE_MATERIALCOUNT_INDEX = 6;
	protected static final int SCHEDULE_STARTTIME_INDEX = 7;
	protected static final int SCHEDULE_ENDTIME_INDEX = 8;
	protected static final int SCHEDULE_WORKHOURS_INDEX = 9;

	protected static final int MATERIAL_CODE_INDEX = 0;
	protected static final int MATERIAL_NAME_INDEX = 1;
	protected static final int MATERIAL_COUNT_INDEX = 2;
	protected static final int MATERIAL_TYPE_INDEX = 3;
	protected static final int MATERIAL_MODEL_INDEX = 4;
	protected static final int MATERIAL_ID_INDEX = 5;
	protected static final int MATERIAL_ORDERIDS_INDEX = 6;

	private static final long serialVersionUID = 1L;
	private JTable productsTable;

	private JScrollPane scrollPane_2;
	private JSplitPane splitPane;
	private JPanel panel;
	private JPanel panel_1;
	private JScrollPane scrollPane;
	private final JToolBar toolBar = new JToolBar();

	private JLabel label;
	private JComboBox<IOrder> ordersView;
	private JButton button;
	private JButton button_1;
	private JTable ordersTable;
	private JButton button_2;
	private JComboBox<OrderProviderInfo> providerView;
	private JLabel label_1;
	private JButton button_4;

	BomOper bomOper = new BomOper();
	Map<String, IMaterial> materialMap = new HashMap<>();

	private JTabbedPane tabbedPane;
	private JPanel panel_2;
	private JPanel panel_3;
	private JScrollPane scrollPane_3;
	private JTable scheduledTable;
	private JButton button_3;
	private JTabbedPane tabbedPane_1;
	private JPanel panel_4;
	private JScrollPane scrollPane_1;
	private JTable curproductsTable;
	private JButton button_5;

	protected IOrderProvider getOrderProvider() throws Exception {
		if (providerView.getSelectedItem() == null)
			throw new Exception("未设置订单提供者！");
		return ((OrderProviderInfo) providerView.getSelectedItem()).getProvider();
	}

	protected IOrder[] getOrders() throws Exception {
		DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
		IOrder[] orders = new IOrder[model.getRowCount()];
		for (int i = 0; i < model.getRowCount(); i++) {
			orders[i] = (IOrder) model.getValueAt(i, ORDER_OBJECT_INDEX);
		}

		return orders;
	}

	protected void resetTableModel(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] columns = new Object[model.getColumnCount()];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = model.getColumnName(i);
		}

		table.setModel(new DefaultTableModel(columns, 0));
	}

	protected void saveSchedule() throws Exception {
		EngineInfo engineInfo = APSConfigure.loadEngine();
		engineInfo.save();
	}

	protected void scheduleOrders() throws Throwable {
		EngineInfo engineInfo = APSConfigure.loadEngine();
		if (engineInfo == null) {
			MsgHelper.showMessage("当前未设置默认排程引擎，请前往设置后再试！");
			return;
		}

		RuleStructInfo structInfo = APSConfigure.loadCurrentRuleStruct();
		if (structInfo == null) {
			MsgHelper.showMessage("当前未设置默认排程架构，请前往设置后再试！");
			return;
		}

		ScheduleResult result = engineInfo.schedule(APSConfigure.loadCurrentOrderAllotType(),
				APSConfigure.loadCurrentOrderTarget(), structInfo, getOrders(), null);
		if (result.ret != ScheduleResult.RET_SUCC)
			throw new Exception(
					result.exception != null ? result.exception.toString() : result.errMsg + "[+result.ret+]");

		Object[] columnNames = new Object[] { "订单编号列表", "物料编号", "设备编号", "工位编号", "任务编号", "操作者编号", "任务数量", "任务起始时间",
				"任务结束时间", "有效工时" };

		DefaultTableModel model = new DefaultTableModel(columnNames, 0);
		for (IScheduleTask task : result.metaSources.tasks) {
			Object[] row = new Object[columnNames.length];
			JSONArray ids = new JSONArray();
			for (String id : task.orderId()) {
				ids.put(id);
			}

			row[SCHEDULE_ORDDERID_INDEX] = ids.toString();
			row[SCHEDULE_MATERIALID_INDEX] = task.materialId();
			row[SCHEDULE_DEVICEID_INDEX] = task.deviceId();
			row[SCHEDULE_STATIONID_INDEX] = task.stationId();
			row[SCHEDULE_TASKID_INDEX] = task.taskId();
			row[SCHEDULE_WORKERID_INDEX] = task.workerId();
			row[SCHEDULE_MATERIALCOUNT_INDEX] = task.count();
			row[SCHEDULE_STARTTIME_INDEX] = task.startTime();
			row[SCHEDULE_ENDTIME_INDEX] = task.endTime();
			row[SCHEDULE_WORKHOURS_INDEX] = task.workhours();

			model.addRow(row);
		}

		scheduledTable.setModel(model);
		scheduledTable.updateUI();

		tabbedPane.setSelectedIndex(1);
	}

	protected void addOrders() throws Exception {

		Map<String, String> orderIdMap = new HashMap<>();
		DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			String id = (String) model.getValueAt(i, ORDER_ID_INDEX);
			orderIdMap.put(id, id);
		}

		for (int i = 0; i < ordersView.getItemCount(); i++) {
			IOrder order = (IOrder) ordersView.getItemAt(i);			
			if (order == null) {
				continue;
			}
			
			if (orderIdMap.containsKey(order.orderId()))
				continue;
			addOrder(order);
		}

	}
	
	protected void addOrder() throws Exception {
		IOrder order = (IOrder) ordersView.getSelectedItem();
		if (order == null) {
			return;
		}

		DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			String id = (String) model.getValueAt(i, ORDER_ID_INDEX);
			if (id.equalsIgnoreCase(order.orderId())) {
				MsgHelper.showMessage("此订单已经在列表中，请勿重复添加！");
				ordersTable.getSelectionModel().setSelectionInterval(i, i);
				ordersTable.scrollRectToVisible(ordersTable.getCellRect(i, ORDER_ID_INDEX, true));
				return;
			}
		}

		addOrder(order);
	}

	/**
	 * Create the dialog.
	 * 
	 * @throws Exception
	 */
	public APSDialog() throws Exception {
		setTitle("排程");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1419, 867);
		getContentPane().setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		panel_2 = new JPanel();
		panel_2.setMinimumSize(new Dimension(10, 200));
		tabbedPane.addTab("订单信息", null, panel_2, null);
		panel_2.setLayout(new BorderLayout(0, 0));

		splitPane = new JSplitPane();
		panel_2.add(splitPane);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});

		splitPane.setResizeWeight(0.3);

		panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabbedPane_1, BorderLayout.CENTER);

		panel_4 = new JPanel();
		tabbedPane_1.addTab("当前订单任务", null, panel_4, null);
		panel_4.setLayout(new BorderLayout(0, 0));

		scrollPane_1 = new JScrollPane();
		panel_4.add(scrollPane_1, BorderLayout.CENTER);

		curproductsTable = new ModelSearchView.TableModelSearchView();
		curproductsTable.setFillsViewportHeight(true);
		curproductsTable.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "\u7269\u6599\u4EE3\u7801", "\u7269\u6599\u540D\u79F0", "\u4EFB\u52A1\u6570\u91CF",
						"\u7269\u6599\u7C7B\u578B", "\u7269\u6599\u578B\u53F7", "\u7269\u6599\u7F16\u53F7" }));
		curproductsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		curproductsTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		curproductsTable.setRowHeight(30);
		curproductsTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		scrollPane_1.setViewportView(curproductsTable);

		scrollPane_2 = new JScrollPane();
		tabbedPane_1.addTab("总任务", scrollPane_2);

		productsTable = new ModelSearchView.TableModelSearchView();
		productsTable.setFillsViewportHeight(true);
		productsTable.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "\u7269\u6599\u4EE3\u7801", "\u7269\u6599\u540D\u79F0", "\u4EFB\u52A1\u6570\u91CF",
						"\u7269\u6599\u7C7B\u578B", "\u7269\u6599\u578B\u53F7", "\u7269\u6599\u7F16\u53F7",
						"\u5BA2\u6237" }) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8495345698740157025L;
			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		productsTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		// DataGridHelp.FitTableColumns(orderTable);
		productsTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (productsTable.getSelectedRow() == -1 || productsTable.getSelectedColumn() == -1)
					return;

				if (productsTable.isEditing())
					return;

				productsTable.editCellAt(productsTable.getSelectedRow(), productsTable.getSelectedColumn());
				if (productsTable.isEditing())
					productsTable.getEditorComponent().requestFocus();
			}
		});
		productsTable.setRowHeight(30);
		productsTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(productsTable);

		panel_1 = new JPanel();
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		ordersTable = new JTable();
		ordersTable.setFillsViewportHeight(true);
		ordersTable.setModel(new DefaultTableModel(null, new String[] { "\u8BA2\u5355\u53F7", "\u5BA2\u6237",
				"\u4EFB\u52A1\u603B\u6570", "\u5DE5\u65F6", "\u4EA4\u4ED8\u65F6\u95F4", "\u6570\u636E" }) {
			private static final long serialVersionUID = 1L;
			boolean[] columnEditables = new boolean[] { true, true, true, true, true, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});

		ordersTable.getColumnModel().getColumn(5).setResizable(false);
		ordersTable.getColumnModel().getColumn(5).setMaxWidth(75);
		ordersTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				label.setText("【数据】");
				return label;
			}
		});

		ordersTable.setRowHeight(30);
		ordersTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		ordersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = ordersTable.getSelectedRow();
				if (row == -1) {
					return;
				}

				DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
				Vector<Object> rowData = (Vector<Object>) model.getDataVector().get(row);
				try {
					updateOrderProductModel((IOrder) rowData.get(ORDER_OBJECT_INDEX));
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		scrollPane.setViewportView(ordersTable);

		panel_3 = new JPanel();
		tabbedPane.addTab("排程信息", null, panel_3, null);
		panel_3.setLayout(new BorderLayout(0, 0));

		scrollPane_3 = new JScrollPane();
		panel_3.add(scrollPane_3, BorderLayout.CENTER);

		scheduledTable = new JTable();
		scheduledTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scheduledTable.setFillsViewportHeight(true);
		scrollPane_3.setViewportView(scheduledTable);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		label = new JLabel(" 订单编号  ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		ordersView = new JComboBox<>();
		ordersView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(ordersView);
		
		button_5 = new JButton(" 一键加入 ");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addOrders();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		JButton btnexcel = new JButton(" 加入订单 ");
		toolBar.add(btnexcel);
		btnexcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addOrder();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		btnexcel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		button_2 = new JButton(" 删除订单 ");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					delOrder();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_2);

		button_4 = new JButton("重置订单 ");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetTableModel(ordersTable);
				resetTableModel(productsTable);
				resetTableModel(curproductsTable);
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		toolBar.addSeparator();

		label_1 = new JLabel(" 订单提供者  ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);

		providerView = new JComboBox<>();
		providerView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					refreshOrders();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		providerView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(providerView);

		button = new JButton(" 预排程 ");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					scheduleOrders();
				} catch (Throwable e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);

		button_1 = new JButton(" 排程 ");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					scheduleOrders();
					saveSchedule();
				} catch (Throwable e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}

			}
		});

		button_3 = new JButton(" 保存 ");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveSchedule();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		toolBar.addSeparator();
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_1);

		setLocationRelativeTo(null);

	}

	protected void init() throws Exception {
		IMaterial[] materials = new MaterialInfoBuilder().builder(APSConfigure.getConfigDBName());
		if (materials != null && materials.length > 0) {
			for (IMaterial iMaterial : materials) {
				materialMap.put(iMaterial.id(), iMaterial);
			}
		}
		refreshProviders();
	}

	interface ISetMaterialInfo{
		void setRow(Object[] row);
	}
	
	protected void setMaterialInfo(DefaultTableModel model, Object[] row, IProduct product, ISetMaterialInfo setMaterialInfo) throws Exception {
		String materalId = product.materalId();
		
		IMaterial material = materialMap.get(materalId);
		if (material == null) {
			throw new Exception("物料【" + materalId + "】不存在！");
		}

		row[MATERIAL_ID_INDEX] = material.id();
		row[MATERIAL_NAME_INDEX] = material.name();
		row[MATERIAL_COUNT_INDEX] = product.count();
		row[MATERIAL_TYPE_INDEX] = material.type();
		row[MATERIAL_MODEL_INDEX] = material.model();
		row[MATERIAL_CODE_INDEX] = material.code();
		if (setMaterialInfo != null)
			setMaterialInfo.setRow(row);
		
		model.addRow(row);
	}
	
	protected void updateAllProductModel(IOrder order, boolean isPlug) throws Exception {
		class Tool {
			String toOrderKey() {
				return order.customerId() + "." + order.orderId();
			}

			void updateOrderIds(DefaultTableModel productModel, int row) {
				String id = toOrderKey();
				JSONArray ids = (JSONArray) productModel.getValueAt(row, MATERIAL_ORDERIDS_INDEX);

				if (ids != null) {
					for (Object object : ids) {
						if (object.toString().equalsIgnoreCase(id)) {
							return;
						}
					}
				} else {
					ids = new JSONArray();
				}

				ids.put(id);
				productModel.setValueAt(ids, row, MATERIAL_ORDERIDS_INDEX);
			}

		}

		Tool tool = new Tool();

		Map<String, IProduct> productMap = new HashMap<>();
		for (IProduct product : order.products()) {
			productMap.put(product.materalId(), product);
		}

		DefaultTableModel productModel = (DefaultTableModel) productsTable.getModel();
		for (int i = productModel.getRowCount() - 1; i >= 0; i--) {
			String materialId = (String) productModel.getValueAt(i, MATERIAL_ID_INDEX);
			IProduct product = productMap.remove(materialId);
			if (product != null) {
				int count = (int) productModel.getValueAt(i, MATERIAL_COUNT_INDEX);
				count = isPlug ? count + product.count() : count - product.count();
				if (count <= 0)
					productModel.removeRow(i);
				else {
					productModel.setValueAt(count, i, MATERIAL_COUNT_INDEX);
					tool.updateOrderIds(productModel, i);
				}
			}

		}

		if (isPlug && productMap.size() > 0) {
			for (IProduct product : productMap.values()) {
				Object[] row = new Object[7];
				
				setMaterialInfo(productModel, row, product, new ISetMaterialInfo() {
					
					@Override
					public void setRow(Object[] row) {
						row[MATERIAL_ORDERIDS_INDEX] = new JSONArray("[" + tool.toOrderKey() + "]");
					}
				});
			}
		}

		productsTable.updateUI();

	}

	protected DefaultTableModel makeDefaultTableModel(JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] headers = new Object[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			headers[i] = model.getColumnName(i);
		}
		return new DefaultTableModel(headers, 0);
	}

	protected void updateOrderProductModel(IOrder order) throws Exception {

		DefaultTableModel productModel = makeDefaultTableModel(curproductsTable);
		for (IProduct product : order.products()) {
			Object[] row = new Object[6];
			
			setMaterialInfo(productModel, row, product, null);
		}

		curproductsTable.setModel(productModel);

	}

	private void refreshProviders() throws Exception {
		OrderProviderInfo[] providers = OrderProviderQuery.getInfos();
		DefaultComboBoxModel<OrderProviderInfo> model = new DefaultComboBoxModel<>();
		if (providers != null && providers.length > 0) {
			model = new DefaultComboBoxModel<OrderProviderInfo>(providers);
		}
		providerView.setModel(model);

		if (providerView.getItemCount() > 0)
			providerView.setSelectedIndex(0);
	}

	private void refreshOrders() throws Exception {
		IOrderProvider provider = getOrderProvider();
		DefaultComboBoxModel<IOrder> model = new DefaultComboBoxModel<>();
		if (provider != null) {
			IOrder[] orders = provider.queryOrders();
			if (orders != null && orders.length > 0)
				model = new DefaultComboBoxModel<IOrder>(orders);
		}
		ordersView.setModel(model);
		if (ordersView.getItemCount() > 0)
			ordersView.setSelectedIndex(0);
	}

	protected void addOrder(IOrder order) throws Exception {
		int count = 0;
		for (IProduct product : order.products()) {
			count += product.count();
		}

		DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
		Object[] row = new Object[6];
		row[ORDER_ID_INDEX] = order.orderId();
		row[ORDER_CUSTOMERID_INDEX] = order.customerId();
		row[ORDER_COUNT_INDEX] = count;
		row[ORDER_WORKHOURS_INDEX] = order.workhours();
		row[ORDER_ENDTIME_INDEX] = order.endTime();
		row[ORDER_OBJECT_INDEX] = order;
		model.addRow(row);
		ordersTable.updateUI();

		int rowIndex = model.getRowCount() - 1;
		ordersTable.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
		
		updateAllProductModel(order, true);

	}

	protected void delOrder() throws Exception {
		int row = ordersTable.getSelectedRow();
		if (row == -1) {
			MsgHelper.showMessage("请先选择一个订单！");
			return;
		}

		IOrderProvider provider = getOrderProvider();

		DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
		String id = (String) model.getValueAt(row, ORDER_ID_INDEX);

		IOrder order = (IOrder) model.getValueAt(row, ORDER_OBJECT_INDEX);
		if (order == null) {
			order = provider.queryOrder(id);
		}

		if (order == null) {
			MsgHelper.showMessage("未发现订单【】，此订单可能已经被删除，请复位后重新添加所有订单！");
			return;
		}

		model.removeRow(row);

		updateAllProductModel(order, false);
	}

	public static void showDialog() {
		try {
			APSDialog config = new APSDialog();
			config.init();
			config.setModal(true);
			config.setVisible(true);
			config.dispose();
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}
}
