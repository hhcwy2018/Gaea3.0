package com.wh.swing.tools.celleditor;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.ConstructorProperties;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

public class NumberCellEditor extends CustomCellEditor {

	private static final long serialVersionUID = 1L;

	JSpinner editorComponent;

	@ConstructorProperties({ "component" })
	public NumberCellEditor(SpinnerModel model) {
		editorComponent = new JSpinner(model);

		this.clickCountToStart = 1;
		delegate = new EditorDelegate() {
			private static final long serialVersionUID = 1L;

			public void setValue(Object value) {
				if (value == null)
					return;
				
				editorComponent.setValue(value);
			}

			public Object getCellEditorValue() {
				return editorComponent.getValue();
			}
		};
		
		editorComponent.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				delegate.stopCellEditing();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
			
		});
	}

	public Component getComponent() {
		return editorComponent;
	}
} 

