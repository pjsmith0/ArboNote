package com.pjs.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pjs.Config;
import com.pjs.model.TreeHierarchyData;
import com.pjs.model.TreeItemData;
import com.pjs.util.FileSystemManager;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.Optional;

public class TreeNotesApp extends JFrame {

    private static final String ROOT_IMAGE = "/icons/home_16dp.png";
    private static final String ARTICLE_IMAGE = "/icons/sticky_note_16dp.png";

    private final JTree tree;
    private final BasicTextEditor editor;
    private final JLabel statusBar;
    private Config config = new Config();
    private final FileSystemManager fileSystemManager;

    @SneakyThrows
    public TreeNotesApp() {
        super("ArboNote");

        setIconImage(new ImageIcon(getClass().getResource("/icons/arbonote.png")).getImage());

        fileSystemManager = new FileSystemManager(config);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        ObjectMapper mapper = new ObjectMapper();
        TreeHierarchyData rootPageNode = mapper.readValue(
                Path.of(config.getRootPath(), config.getTreeFileName()).toFile(),
                TreeHierarchyData.class
        );

        TreeModel treeModel = buildTreeModel(rootPageNode);

        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this::onTreeSelection);
        tree.addMouseListener(buildMouseAdaptor());
        tree.setCellRenderer(getCellRenderer());
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler(fileSystemManager));

        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setBorder(BorderFactory.createTitledBorder("Notes Tree"));

        editor = new BasicTextEditor();

        JPanel editorContainer = new JPanel(new BorderLayout());
        editorContainer.setBorder(BorderFactory.createTitledBorder("Editor"));
        editorContainer.add(editor, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                treeScrollPane,
                editorContainer
        );
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);

        statusBar = new JLabel(" Ready");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                TreePath path = tree.getSelectionPath();
                Optional.ofNullable(path)
                        .map(TreePath::getLastPathComponent)
                        .ifPresent(o -> {
                            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) o;
                            TreeItemData userObject = (TreeItemData) selectedNode.getUserObject();
                            fileSystemManager.saveTreeItem(userObject.getFileName(), editor.getHtml());
                        });
            }
        });
    }

    private TreeCellRenderer getCellRenderer() {
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean selected,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) {

                super.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus
                );

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof TreeItemData treeItemData) {
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

    private MouseAdapter buildMouseAdaptor() {
        return new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                JTree tree = (JTree) e.getSource();
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
                        editor,
                        selectedNode
                );
                treeContextMenu.show(tree, e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }
        };
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(exitMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> {
            showAboutDialog(TreeNotesApp.this);
        });
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void onTreeSelection(TreeSelectionEvent event) {
        TreePath oldPath = event.getOldLeadSelectionPath();
        TreePath newPath = event.getNewLeadSelectionPath();

        if (oldPath != null) {
            DefaultMutableTreeNode oldNode =
                    (DefaultMutableTreeNode) oldPath.getLastPathComponent();
            TreeItemData oldUserObject = (TreeItemData) oldNode.getUserObject();

            String html = editor.getHtml();
            fileSystemManager.saveTreeItem(oldUserObject.getFileName(), html);
        }

        if (newPath != null) {
            DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) newPath.getLastPathComponent();
            TreeItemData newUserObject = (TreeItemData) newNode.getUserObject();

            statusBar.setText(" Selected: " + newUserObject.getNodeName());
            String htmlToSet = fileSystemManager.loadData(newUserObject.getFileName());
            editor.setHtml(htmlToSet);
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

    @SneakyThrows
    public static void showAboutDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "About", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setPreferredSize(new Dimension(100, 100));
        iconPanel.setOpaque(false);

        JLabel picLabel;
        java.net.URL imageUrl = TreeNotesApp.class.getResource("/icons/arbonote.png");

        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image scaled = originalIcon.getImage().getScaledInstance(72, 72, Image.SCALE_SMOOTH);
            picLabel = new JLabel(new ImageIcon(scaled));
        } else {
            picLabel = new JLabel("No Image");
            picLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        picLabel.setHorizontalAlignment(SwingConstants.CENTER);
        picLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconPanel.add(picLabel, BorderLayout.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("ArboNote");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel version = new JLabel("Version 1.0.0");
        version.setFont(new Font("SansSerif", Font.PLAIN, 14));
        version.setForeground(new Color(90, 90, 90));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea description = new JTextArea(
                "A clean and modern Swing application.\n\n" +
                        "Built with Java Swing for desktop use.\n" +
                        "© 2026"
        );
        description.setFont(new Font("SansSerif", Font.PLAIN, 13));
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(version);
        textPanel.add(Box.createVerticalStrut(14));
        textPanel.add(description);

        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(iconPanel, BorderLayout.WEST);
        centerPanel.add(textPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        content.add(centerPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setSize(420, 240);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}