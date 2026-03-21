package com.pjs.ui;

import com.formdev.flatlaf.util.StringUtils;
import com.pjs.model.TreeItemData;
import com.pjs.util.FileSystemManager;
import com.pjs.util.Utils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeContextMenu extends JPopupMenu {

    private final JFrame frame;
    private final JTree tree;
    private final FileSystemManager fileSystemManager;
    private final DefaultMutableTreeNode selectedNode;

    private JMenuItem create;
    private JMenuItem rename;
    private JMenuItem delete;
    private JMenuItem search;

    private JTextComponent textComponent;

    public TreeContextMenu(FileSystemManager fileSystemManager, JFrame frame, JTree tree, DefaultMutableTreeNode selectedNode) {
        this.frame = frame;
        this.tree = tree;
        this.fileSystemManager = fileSystemManager;
        this.selectedNode = selectedNode;
        addPopupMenuItems();
    }

    private void addPopupMenuItems() {
        create = new JMenuItem("Create...");
        //create.setEnabled(false);
        create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        create.addActionListener(event -> {
            String rawTreeItemName = JOptionPane.showInputDialog(
                    frame,
                    "Enter a name..."
            );
            if (!StringUtils.isEmpty(rawTreeItemName)) {
                String location = fileSystemManager.createLocation();
                String treeItemName = Utils.sanitize(rawTreeItemName);

                TreeItemData treeItemData = TreeItemData.builder()
                        .nodeName(treeItemName)
                        .fileName(location)
                        .build();

                //DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(treeItemData);
                //selectedNode.add(newNode);
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(treeItemData);
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());

                TreePath path = new TreePath(newNode.getPath());
                tree.expandPath(path);
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);

                fileSystemManager.saveTreeToJson(selectedNode.getRoot());
            }
        });
        add(create);

        rename = new JMenuItem("Rename");
        //rename.setEnabled(false);
        rename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        rename.addActionListener(event -> {
            String rawTreeItemName = JOptionPane.showInputDialog(
                    frame,
                    "Enter a new name..."
            );
            if (!StringUtils.isEmpty(rawTreeItemName)) {
                String treeItemName = Utils.sanitize(rawTreeItemName);
                TreeItemData treeItemData = (TreeItemData) selectedNode.getUserObject();
                treeItemData.setNodeName(treeItemName);

                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.nodeChanged(selectedNode);

                fileSystemManager.saveTreeToJson(selectedNode.getRoot());
            }
        });
        add(rename);

        add(new JSeparator());

        delete = new JMenuItem("Delete");
        //delete.setEnabled(false);
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        delete.addActionListener(event -> {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
            if (parent != null) {

                int input = JOptionPane.showConfirmDialog(frame,
                        "Do you want to proceed?",
                        "Select an Option...",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                // 0=yes, 1=no, 2=cancel

                if (input == JOptionPane.YES_OPTION) {
                    TreePath parentPath = new TreePath(parent.getPath());
                    TreeNode root = selectedNode.getRoot();

                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    model.removeNodeFromParent(selectedNode);

                    tree.setSelectionPath(parentPath);
                    tree.scrollPathToVisible(parentPath);

                    TreeItemData treeItemData = (TreeItemData) selectedNode.getUserObject();
                    fileSystemManager.deleteFile(treeItemData.getFileName());
                    fileSystemManager.saveTreeToJson(root);
                }
            }
        });
        add(delete);

        search = new JMenuItem("Search");
        //search.setEnabled(false);
        search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        search.addActionListener(event -> {
            SearchPopup popup = new SearchPopup(fileSystemManager, tree, selectedNode, frame);
            popup.show();
        });
        add(search);

    }

    private void addTo(JTextComponent textComponent) {
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent pressedEvent) {
                if ((pressedEvent.getKeyCode() == KeyEvent.VK_Z) && ((pressedEvent.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)) {
//                    if (undoManager.canUndo()) {
//                        undoManager.undo();
//                    }
                }

                if ((pressedEvent.getKeyCode() == KeyEvent.VK_Y)
                        && ((pressedEvent.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)) {
//                    if (undoManager.canRedo()) {
//                        undoManager.redo();
//                    }
                }
            }
        });

        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent releasedEvent) {
                handleContextMenu(releasedEvent);
            }

            @Override
            public void mouseReleased(MouseEvent releasedEvent) {
                handleContextMenu(releasedEvent);
            }
        });

//        textComponent.getDocument().addUndoableEditListener(event ->
//                undoManager.addEdit(event.getEdit())
//        );
    }

    private void handleContextMenu(MouseEvent releasedEvent) {
        if (releasedEvent.getButton() == MouseEvent.BUTTON3) {
            processClick(releasedEvent);
        }
    }

    private void processClick(MouseEvent event) {
        textComponent = (JTextComponent) event.getSource();
        textComponent.requestFocus();

//        boolean enableUndo = undoManager.canUndo();
//        boolean enableRedo = undoManager.canRedo();
        boolean enableCut = false;
        boolean enableCopy = false;
        boolean enablePaste = false;
        boolean enableDelete = false;
        boolean enableSelectAll = false;

        String selectedText = textComponent.getSelectedText();
        String text = textComponent.getText();

        if (text != null) {
            if (text.length() > 0) {
                enableSelectAll = true;
            }
        }

        if (selectedText != null) {
            if (selectedText.length() > 0) {
                enableCut = true;
                enableCopy = true;
                enableDelete = true;
            }
        }

//        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor) && textComponent.isEnabled()) {
//            enablePaste = true;
//        }

//        undo.setEnabled(enableUndo);
//        redo.setEnabled(enableRedo);
//        cut.setEnabled(enableCut);
//        copy.setEnabled(enableCopy);
//        paste.setEnabled(enablePaste);
//        delete.setEnabled(enableDelete);
//        selectAll.setEnabled(enableSelectAll);

        // Shows the popup menu
        show(textComponent, event.getX(), event.getY());
    }

    public static void addDefaultContextMenu(JTextComponent component) {
//        DefaultContextMenu defaultContextMenu = new DefaultContextMenu();
//        defaultContextMenu.addTo(component);
    }
}

