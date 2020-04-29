package com.wh.swing.tools.adapter;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

public class AlternateRowColorTableRender extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	Color oddRowColor = Color.WHITE, evenRowColor = Color.LIGHT_GRAY;

	public AlternateRowColorTableRender() {
		super();
	}

	public AlternateRowColorTableRender(Color oddRowColor, Color evenRowColor) {
		super();
		this.oddRowColor = oddRowColor;
		this.evenRowColor = evenRowColor;
	}

	public void setOddRowColor(Color color) {
		if (color != null)
			this.oddRowColor = color;
	}

	public void setEvenRowColor(Color color) {
		if (color != null)
			this.evenRowColor = color;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Color color = row % 2 == 0 ? evenRowColor : oddRowColor;
		label.setBackground(color);

		return label;
	}

	public static void config(JTable table) {
		config(table, null, null);
	}

	public static void config(JTable table, Color oddRowColor, Color evenRowColor) {
		AlternateRowColorTableRender render = new AlternateRowColorTableRender();
		render.setOddRowColor(oddRowColor);
		render.setEvenRowColor(evenRowColor);

		TableColumnModel model = table.getTableHeader().getColumnModel();
		for (int i = 0; i < model.getColumnCount(); i++) {
			model.getColumn(i).setCellRenderer(render);
		}
	}

}
