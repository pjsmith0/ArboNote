package com.pjs.ui;

import com.pjs.util.FileSystemManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class TreeTransferHandler extends TransferHandler {
    private final FileSystemManager fileSystemManager;
    private final DataFlavor nodesFlavor;
    private DefaultMutableTreeNode[] nodesToRemove;

    TreeTransferHandler(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
        try {
            String mimeType = "%s;class=\"%s\"".formatted(
                    DataFlavor.javaJVMLocalObjectMimeType,
                    DefaultMutableTreeNode[].class.getName());
            nodesFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null || paths.length == 0) {
            return null;
        }

        DefaultMutableTreeNode[] copies = new DefaultMutableTreeNode[paths.length];
        nodesToRemove = new DefaultMutableTreeNode[paths.length];

        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            copies[i] = copy(node);
            nodesToRemove[i] = node;
        }

        return new NodesTransferable(copies);
    }

    private DefaultMutableTreeNode copy(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node.getUserObject());
        for (int i = 0; i < node.getChildCount(); i++) {
            copy.add(copy((DefaultMutableTreeNode) node.getChildAt(i)));
        }
        return copy;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) return false;
        if (!support.isDataFlavorSupported(nodesFlavor)) return false;

        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        if (destPath == null) return false;

        DefaultMutableTreeNode target =
                (DefaultMutableTreeNode) destPath.getLastPathComponent();

        // Don't allow dropping onto itself or into its own subtree
        if (nodesToRemove != null) {
            for (DefaultMutableTreeNode dragged : nodesToRemove) {
                if (dragged == target || dragged.isNodeDescendant(target)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        try {
            DefaultMutableTreeNode[] nodes =
                    (DefaultMutableTreeNode[]) support.getTransferable()
                            .getTransferData(nodesFlavor);

            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            JTree tree = (JTree) support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

            TreePath destPath = dl.getPath();
            DefaultMutableTreeNode parent =
                    (DefaultMutableTreeNode) destPath.getLastPathComponent();

            int index = dl.getChildIndex();
            if (index == -1) {
                index = parent.getChildCount();
            }

            for (DefaultMutableTreeNode node : nodes) {
                model.insertNodeInto(node, parent, index++);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if ((action & MOVE) == MOVE && nodesToRemove != null) {
            JTree tree = (JTree) source;
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

            for (DefaultMutableTreeNode node : nodesToRemove) {
                if (node.getParent() != null) {
                    model.removeNodeFromParent(node);
                }
            }

            onDropComplete((TreeNode) model.getRoot());
        }
    }

    private void onDropComplete(TreeNode root) {
        fileSystemManager.saveTreeToJson(root);
    }

    class NodesTransferable implements Transferable {
        private final DefaultMutableTreeNode[] nodes;

        NodesTransferable(DefaultMutableTreeNode[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{nodesFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedOperationException("Unsupported flavor");
            }
            return nodes;
        }
    }
}
