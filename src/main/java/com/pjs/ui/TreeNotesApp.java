package com.pjs.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjs.Config;
import com.pjs.model.TreeHierarchyData;
import com.pjs.model.TreeItemData;
import com.pjs.ui.htmleditor.SwingHTMLEditor;
import com.pjs.util.FileSystemManager;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Optional;

public class TreeNotesApp extends JFrame {

    private static final String ARTICLE_IMAGE = "/icons/sticky.png";
    private static final String ROOT_IMAGE = "/icons/lock.png";

    private final JTree tree;
    private final SwingHTMLEditor editor;
    private final JLabel statusBar;
    private Config config = new Config();
    private final FileSystemManager fileSystemManager;

    @SneakyThrows
    public TreeNotesApp() {
        super("Tree Notes");
        fileSystemManager = new FileSystemManager(config);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        ObjectMapper mapper = new ObjectMapper();
        TreeHierarchyData rootPageNode = mapper.readValue(Path.of(
                config.getRootPath(),
                config.getTreeFileName()).toFile(), TreeHierarchyData.class);

        // Build Swing TreeModel
        TreeModel treeModel = buildTreeModel(rootPageNode);

        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this::onTreeSelection);
        tree.addMouseListener(buildMouseAdaptor());
        tree.setCellRenderer(getCellRenderer());

        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setBorder(BorderFactory.createTitledBorder("Notes Tree"));

        editor = new SwingHTMLEditor();

        JScrollPane editorScrollPane = new JScrollPane(editor);
        editorScrollPane.setBorder(BorderFactory.createTitledBorder("Editor"));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScrollPane,
                editorScrollPane
        );
        splitPane.setDividerLocation(250);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);

        statusBar = new JLabel(" Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }

    private TreeCellRenderer getCellRenderer() {
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value,
                    boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof TreeItemData) {
                    TreeItemData treeItemData = (TreeItemData) userObject;
                    setText(treeItemData.getNodeName());
                    if (node.getParent() == null) {
                        Optional.ofNullable(getClass().getResource(ROOT_IMAGE))
                                .ifPresent(url -> setIcon(new ImageIcon(url)));
                    } else {
                        Optional.ofNullable(getClass().getResource(ARTICLE_IMAGE))
                                .ifPresent(url -> setIcon(new ImageIcon(url)));
                    }
                }
                return this;
            }
        };
    }

    //@Override
    private MouseAdapter buildMouseAdaptor() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                JTree tree = (JTree)e.getSource();
                TreePath path = tree.getPathForLocation(x, y);
                if (path == null) {
                    return;
                }

                tree.setSelectionPath(path);

                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

                TreeContextMenu treeContextMenu = new TreeContextMenu(
                        fileSystemManager,
                        TreeNotesApp.this,
                        tree,
                        selectedNode
                        );
                treeContextMenu.show(tree, e.getX(), e.getY());

            }
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) myPopupEvent(e);
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) myPopupEvent(e);
            }
        };

        return mouseAdapter;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("New"));
        fileMenu.add(new JMenuItem("Open"));
        fileMenu.add(new JMenuItem("Save"));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exit"));

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("Cut"));
        editMenu.add(new JMenuItem("Copy"));
        editMenu.add(new JMenuItem("Paste"));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new JMenuItem("About"));

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void onTreeSelection(TreeSelectionEvent event) {

        TreePath oldPath = event.getOldLeadSelectionPath();
        TreePath newPath = event.getNewLeadSelectionPath();

        if (oldPath != null) {
            DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) oldPath.getLastPathComponent();
            TreeItemData oldUserObject = (TreeItemData) oldNode.getUserObject();

            fileSystemManager.saveTreeItem(oldUserObject.getFileName(), editor.getText());
        }

        if (newPath != null) {
            DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) newPath.getLastPathComponent();
            TreeItemData newUserObject = (TreeItemData) newNode.getUserObject();

            statusBar.setText(" Selected: " + newUserObject.getNodeName());
            editor.setText(fileSystemManager.loadData(newUserObject.getFileName()));
        }

    }

    public static TreeModel buildTreeModel(TreeHierarchyData rootPageNode) {
        DefaultMutableTreeNode swingRoot = buildSwingNode(rootPageNode);
        return new DefaultTreeModel(swingRoot);
    }

    private static DefaultMutableTreeNode buildSwingNode(TreeHierarchyData pageNode) {
        TreeItemData treeItemData = TreeItemData.builder()
                .nodeName(pageNode.getNodeName())
                .fileName(pageNode.getFileName())
                .build();

        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(treeItemData);

        if (pageNode.getChildren() != null) {
            for (TreeHierarchyData child : pageNode.getChildren()) {
                treeNode.add(buildSwingNode(child));
            }
        }

        return treeNode;
    }
}