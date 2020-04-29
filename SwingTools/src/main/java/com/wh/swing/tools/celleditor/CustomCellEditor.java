package com.wh.swing.tools.celleditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

public abstract class CustomCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {

	private static final long serialVersionUID = 1L;
	protected EditorDelegate delegate;
	protected int clickCountToStart = 1;

	/**
	 * Returns a reference to the editor component.
	 *
	 * @return the editor <code>Component</code>
	 */
	public abstract Component getComponent();

	/**
	 * Specifies the number of clicks needed to start editing.
	 *
	 * @param count an int specifying the number of clicks needed to start editing
	 * @see #getClickCountToStart
	 */
	public void setClickCountToStart(int count) {
		clickCountToStart = count;
	}

	/**
	 * Returns the number of clicks needed to start editing.
	 * 
	 * @return the number of clicks needed to start editing
	 */
	public int getClickCountToStart() {
		return clickCountToStart;
	}

	public Object getCellEditorValue() {
		return delegate.getCellEditorValue();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#isCellEditable(EventObject)
	 */
	public boolean isCellEditable(EventObject anEvent) {
		return delegate.isCellEditable(anEvent);
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#shouldSelectCell(EventObject)
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		return delegate.shouldSelectCell(anEvent);
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#stopCellEditing
	 */
	public boolean stopCellEditing() {
		return delegate.stopCellEditing();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#cancelCellEditing
	 */
	public void cancelCellEditing() {
		delegate.cancelCellEditing();
	}

//
//  Implementing the TreeCellEditor Interface
//

	/** Implements the <code>TreeCellEditor</code> interface. */
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row) {
		String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, false);

		delegate.setValue(stringValue);
		return getComponent();
	}

//
//  Implementing the CellEditor Interface
//
	/** Implements the <code>TableCellEditor</code> interface. */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		delegate.setValue(value);

		return getComponent();
	}

//
//  Protected EditorDelegate class
//

	/**
	 * The protected <code>EditorDelegate</code> class.
	 */
	protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

		private static final long serialVersionUID = 1L;
		/** The value of this cell. */
		protected Object value;

		/**
		 * Returns the value of this cell.
		 * 
		 * @return the value of this cell
		 */
		public Object getCellEditorValue() {
			return value;
		}

		/**
		 * Sets the value of this cell.
		 * 
		 * @param value the new value of this cell
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Returns true if <code>anEvent</code> is <b>not</b> a <code>MouseEvent</code>.
		 * Otherwise, it returns true if the necessary number of clicks have occurred,
		 * and returns false otherwise.
		 *
		 * @param anEvent the event
		 * @return true if cell is ready for editing, false otherwise
		 * @see #setClickCountToStart
		 * @see #shouldSelectCell
		 */
		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
			}
			return true;
		}

		/**
		 * Returns true to indicate that the editing cell may be selected.
		 *
		 * @param anEvent the event
		 * @return true
		 * @see #isCellEditable
		 */
		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		/**
		 * Returns true to indicate that editing has begun.
		 *
		 * @param anEvent the event
		 */
		public boolean startCellEditing(EventObject anEvent) {
			return true;
		}

		/**
		 * Stops editing and returns true to indicate that editing has stopped. This
		 * method calls <code>fireEditingStopped</code>.
		 *
		 * @return true
		 */
		public boolean stopCellEditing() {
			fireEditingStopped();
			return true;
		}

		/**
		 * Cancels editing. This method calls <code>fireEditingCanceled</code>.
		 */
		public void cancelCellEditing() {
			fireEditingCanceled();
		}

		/**
		 * When an action is performed, editing is ended.
		 * 
		 * @param e the action event
		 * @see #stopCellEditing
		 */
		public void actionPerformed(ActionEvent e) {
			CustomCellEditor.this.stopCellEditing();
		}

		/**
		 * When an item's state changes, editing is ended.
		 * 
		 * @param e the action event
		 * @see #stopCellEditing
		 */
		public void itemStateChanged(ItemEvent e) {
			CustomCellEditor.this.stopCellEditing();
		}
	}

}
