package com.wh.gaea.industry.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.grid.DataGridHelp;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.industry.builder.BOMBuilder;
import com.wh.gaea.industry.builder.BomOper;
import com.wh.gaea.industry.builder.CustomerBuilder;
import com.wh.gaea.industry.builder.BOMBuilder.TreeInfo;
import com.wh.gaea.industry.builder.BomOper.ISetBomList;
import com.wh.gaea.industry.builder.CustomerBuilder.ISetUserList;
import com.wh.gaea.industry.info.BomInfo;
import com.wh.gaea.industry.info.Customer;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.swing.tools.MsgHelper;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IColumn;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;

public class BomQueryDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable dataProviderGrid;

	BomOper bomOper = new BomOper();

	IMainControl mainControl;
	private JComboBox<Customer> customersView;
	private JScrollPane scrollPane_2;
	private JTextField bcodeView;
	private JLabel lblBom_1;
	private JSplitPane splitPane;
	private JPanel panel;
	private JPanel panel_1;
	private JScrollPane scrollPane;
	private JTree bomTree;
	private final JToolBar toolBar = new JToolBar();
	private JLabel lblBom_2;
	private JComboBox<BomInfo> bomsView;

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

	protected void initTableProvider() throws Exception {

		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("material");
		sqlBuilder.addWhere("used", Operation.otEqual, new Object[] { 1 });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("deleted", Operation.otEqual, new Object[] { 0 });
		IDataset dataset = db.query(sqlBuilder);

		Object[] columns = new Object[dataset.getColumnCount()];
		int index = 0;
		for (IColumn column : dataset.getColumns()) {
			columns[index++] = column.getName();
		}

		Object[][] rows = new Object[dataset.getRowCount()][dataset.getColumnCount()];
		for (int i = 0; i < dataset.getRowCount(); i++) {
			IRow row = dataset.getRow(i);
			materialMap.put(row.getValue("material_id").toString(), i);
			for (int j = 0; j < dataset.getColumnCount(); j++) {
				rows[i][j] = row.getValue(j);
			}

		}

		dataProviderGrid.setModel(new DefaultTableModel(rows, columns));

		dataProviderGrid.repaint();
	}

	/**
	 * Create the dialog.
	 */
	public BomQueryDialog(IMainControl mainControl) {
		this.mainControl = mainControl;

		setTitle("BOM查询");
//		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1419, 867);
		getContentPane().setLayout(new BorderLayout());

		splitPane = new JSplitPane();
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});
		getContentPane().add(toolBar, BorderLayout.NORTH);

		JLabel lblBom = new JLabel(" BOM编码  ");
		toolBar.add(lblBom);
		lblBom.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		bcodeView = new JTextField();
		toolBar.add(bcodeView);
		bcodeView.setMinimumSize(new Dimension(200, 27));
		bcodeView.setPreferredSize(new Dimension(200, 27));
		bcodeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		bcodeView.setColumns(10);

		lblBom_2 = new JLabel(" BOM  ");
		lblBom_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblBom_2);

		bomsView = new JComboBox<>();
		bomsView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(bomsView);

		lblBom_1 = new JLabel(" BOM绑定客户  ");
		toolBar.add(lblBom_1);
		lblBom_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		customersView = new JComboBox<>();
		customersView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (customersView.getSelectedIndex() == -1)
					return;

				Customer customer = (Customer) customersView.getSelectedItem();
				try {
					bomOper.initBomList(customer.bomids.toArray(new String[customer.bomids.size()]), new ISetBomList() {
						DefaultComboBoxModel<BomInfo> model = new DefaultComboBoxModel<>();
						@Override
						public void onBomList() {
							bomsView.setModel(model);
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
		toolBar.add(customersView);
		customersView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton btnexcel = new JButton(" 查询 ");
		toolBar.add(btnexcel);
		btnexcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String bomCode = bcodeView.getText();
				String bomid;
				if (bomCode != null)
					bomCode = bomCode.trim();

				if (bomCode.isEmpty()) {
					if (bomsView.getSelectedIndex() == -1) {
						MsgHelper.showMessage("请先选择一个bom或填写bom代码！");
						return;
					}

					bomid = ((BomInfo) bomsView.getSelectedItem()).id;
				} else {
					try {
						bomid = bomOper.getBomId(bomCode);
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
						return;
					}
				}

				try {
					new BOMBuilder().builder(bomid, bomTree);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		btnexcel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		splitPane.setResizeWeight(0.3);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		scrollPane_2 = new JScrollPane();
		panel.add(scrollPane_2, BorderLayout.CENTER);
		dataProviderGrid = new ModelSearchView.TableModelSearchView();
		dataProviderGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		dataProviderGrid.setRowHeight(30);
		dataProviderGrid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(dataProviderGrid);

		panel_1 = new JPanel();
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		bomTree = new JTree(new DefaultTreeModel(null));
		bomTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (bomTree.getSelectionPath() == null || bomTree.getSelectionPath().getLastPathComponent() == null) {
					dataProviderGrid.setRowSelectionInterval(-1, -1);
					return;
				}

				TreeInfo info = (TreeInfo) ((DefaultMutableTreeNode) bomTree.getSelectionPath().getLastPathComponent())
						.getUserObject();

				int index = materialMap.get(info.materialId);
				dataProviderGrid.setRowSelectionInterval(index, index);
				if (index != -1)
					dataProviderGrid.scrollRectToVisible(dataProviderGrid.getCellRect(index, 0, true));
			}
		});
		bomTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		bomTree.setRowHeight(28);
		bomTree.setRootVisible(false);
		scrollPane.setViewportView(bomTree);

		try {
			CustomerBuilder.builder(new ISetUserList() {
				
				DefaultComboBoxModel<Customer> model = new DefaultComboBoxModel<>();
				@Override
				public void onEnd() {
					customersView.setModel(model);
					if (model.getSize() > 0)
						customersView.setSelectedIndex(0);
				}
				
				@Override
				public void addNode(Customer info) {
					model.addElement(info);
				}
			});
			initTableProvider();
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}

		setLocationRelativeTo(null);

	}

	public static void show(IMainControl mainControl) {
		BomQueryDialog config = new BomQueryDialog(mainControl);
		config.setModal(true);
		config.setVisible(true);
		config.dispose();
	}
}
