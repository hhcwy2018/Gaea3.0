package com.wh.swing.tools.tree.drag;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.TreePath;

public class TransferableTreeNode implements Transferable {

	public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");

	DataFlavor flavors[] = { TREE_PATH_FLAVOR };

	TreePath path;//tree的每个节点的userdata必须实现序列化接口，否则不会被自动复制到剪切板

	public TransferableTreeNode(TreePath tp) {
		path = tp;
	}

	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.getRepresentationClass() == TreePath.class);
	}

	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			return path;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}
