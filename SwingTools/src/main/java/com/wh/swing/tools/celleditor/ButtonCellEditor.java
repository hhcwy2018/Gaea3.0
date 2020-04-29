package com.wh.swing.tools.celleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.ConstructorProperties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ButtonCellEditor extends CustomCellEditor {

	private static final long serialVersionUID = 1L;

	public enum EditorType {
		etText, etButton
	}

	JPanel editorComponent;
	JTextField editor;
	JButton button;

	public static class ActionResult {
		public boolean isOk = false;
		public Object data;
	}

	public interface ButtonActionListener {
		void actionPerformed(ActionEvent e, EditorType editorType, ActionResult actionResult);
	}

	@ConstructorProperties({ "component" })
	public ButtonCellEditor(ButtonActionListener buttonActionListener) {
		editorComponent = new JPanel();
		editorComponent.setLayout(new BorderLayout());
		editor = new JTextField();
		editorComponent.add(editor, BorderLayout.CENTER);
		button = new JButton();
		editorComponent.add(button, BorderLayout.EAST);

		this.clickCountToStart = 1;
		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
				editor.setText((value != null) ? value.toString() : "");
			}

			public Object getCellEditorValue() {
				return editor.getText();
			}
		};
		editor.addActionListener(delegate);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ActionResult result = new ActionResult();
				buttonActionListener.actionPerformed(e,
						e.getSource() instanceof JButton ? EditorType.etButton : EditorType.etText, result);
				if (!result.isOk)
					return;

				delegate.setValue(result.data);
				delegate.stopCellEditing();
			}
		});
	}

	public Component getComponent() {
		return editorComponent;
	}

} 
