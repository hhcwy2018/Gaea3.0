package com.wh.gaea.install.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.wh.gaea.install.control.GridBagConstraintsEx;
import com.wh.gaea.install.interfaces.ItemConfigureInfo;
import com.wh.gaea.install.interfaces.ItemConfigureInfo.ComboboxConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;

public class ConfigureForm extends BaseForm {

	private static final long serialVersionUID = -3469632872672185703L;
	private JPanel contentView;

	public ConfigureForm() {
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setTitle("消息队列服务器设置");
		setBounds(100, 100, 821, 273);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(null);
		panel.setPreferredSize(new Dimension(10, 50));
		panel.setMinimumSize(new Dimension(10, 50));
		panel.setMaximumSize(new Dimension(32767, 50));
		getContentPane().add(panel, BorderLayout.NORTH);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel_1.setPreferredSize(new Dimension(10, 50));
		panel_1.setMinimumSize(new Dimension(10, 50));
		panel_1.setMaximumSize(new Dimension(32767, 50));
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(null);
		panel_2.setMinimumSize(new Dimension(50, 10));
		panel_2.setMaximumSize(new Dimension(50, 32767));
		panel_2.setPreferredSize(new Dimension(50, 10));
		getContentPane().add(panel_2, BorderLayout.WEST);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(null);
		panel_3.setPreferredSize(new Dimension(50, 10));
		panel_3.setMinimumSize(new Dimension(50, 10));
		panel_3.setMaximumSize(new Dimension(50, 32767));
		getContentPane().add(panel_3, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		contentView = new JPanel();
		scrollPane.getViewport().add(contentView);
		GridBagLayout gbl_contentView = new GridBagLayout();
		gbl_contentView.columnWeights = new double[]{};
		gbl_contentView.rowWeights = new double[]{};
		contentView.setLayout(gbl_contentView);

	}

	int dy = 0;

	protected JComponent createComponent(ItemConfigureInfo info) {
		JComponent valueView = null;
		Class<?> c = info.getType();
		if (ComboboxConfigureInfo.class.isAssignableFrom(info.getClass())) {
			JComboBox<Object> comboBox = new JComboBox<Object>(
					((ComboboxConfigureInfo) info).getItems());
			if (info.value != null)
				comboBox.setSelectedItem(info.value);
			valueView = comboBox;
		} else if (Date.class.isAssignableFrom(c)) {
			JSpinner spinner = new JSpinner(new SpinnerDateModel());
			spinner.setValue(info.value == null ? "" : info.value);
			valueView = spinner;
		} else if (Boolean.class.isAssignableFrom(c)) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setText("");
			valueView = checkBox;
		} else if (Number.class.isAssignableFrom(c)) {
			JSpinner spinner = new JSpinner();
			valueView = spinner;
			SpinnerModel model = null;
			if (Integer.class.isAssignableFrom(c)) {
				model = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
			} else if (Byte.class.isAssignableFrom(c)) {
				model = new SpinnerNumberModel(0, 0, Byte.MAX_VALUE, 1);
			} else if (Short.class.isAssignableFrom(c)) {
				model = new SpinnerNumberModel(0, 0, Short.MAX_VALUE, 1);
			} else if (Long.class.isAssignableFrom(c)) {
				model = new SpinnerNumberModel(0, 0, Long.MAX_VALUE, 1);
			} else {
				model = new SpinnerNumberModel(0F, 0F, Float.MAX_VALUE, 1);
			}
			spinner.setModel(model);
		} else {
			JTextField textView = new JTextField();
			textView.setText(info.value == null ? "" : info.value.toString());
			valueView = textView;
		}

		return valueView;
	}

	public void load(PageConfigureInfo pageInfo) {
		super.load(pageInfo);
		for (ItemConfigureInfo configureItemInfo : pageInfo) {
			add(configureItemInfo);
		}
	}

	protected void add(ItemConfigureInfo info) {
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraintsEx(0, dy, 2, 1);
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.weightx = 15;
		
		JLabel lblNewLabel = new JLabel(info.title);
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentView.add(lblNewLabel, gbc_lblNewLabel);

		GridBagConstraints gbc_hostView = new GridBagConstraintsEx(3, dy++, 5, 1);
		gbc_hostView.insets = new Insets(0, 0, 5, 5);
		gbc_hostView.fill = GridBagConstraints.HORIZONTAL;
		gbc_hostView.weightx = 70;

		JComponent valueView = createComponent(info);
		valueView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentView.add(valueView, gbc_hostView);

		gbc_lblNewLabel = new GridBagConstraintsEx(8, dy, 2, 1);
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.weightx = 15;
		
		lblNewLabel = new JLabel();
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentView.add(lblNewLabel, gbc_lblNewLabel);
		
		info.tag = valueView;
	}

	@SuppressWarnings("rawtypes")
	protected Object getValue(ItemConfigureInfo info) {
		if (info.tag == null)
			return null;

		if (JComboBox.class.isAssignableFrom(info.tag.getClass())) {
			return ((JComboBox) info.tag).getSelectedItem();
		} else if (JSpinner.class.isAssignableFrom(info.tag.getClass())) {
			return ((JSpinner) info.tag).getValue();
		} else if (JCheckBox.class.isAssignableFrom(info.tag.getClass())) {
			return ((JCheckBox) info.tag).isSelected();
		} else {
			return ((JTextField) info.tag).getText();
		}
	}

	@Override
	public String getId() {
		return pageInfo.id;
	}

	@Override
	public CheckResult check() {
		CheckResult result = new CheckResult();
		for (ItemConfigureInfo info : pageInfo) {
			Object value = getValue(info);
			if (info.allowNull)
				continue;

			if (value == null || (value instanceof String
					&& value.toString().trim().isEmpty())) {
				result.msg = info.msg == null || info.msg.trim().isEmpty()
						? info.title + "不能为空，请填写后再试！"
						: info.msg;
				return result;
			}
		}

		result.isok = true;
		return result;
	}

	@Override
	public void save() throws Exception {
		for (ItemConfigureInfo info : pageInfo) {
			info.value = getValue(info);
		}
	}

}
