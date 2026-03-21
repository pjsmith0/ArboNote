package com.pjs.ui;

import com.pjs.model.SearchItemData;
import com.pjs.model.TreeItemData;
import com.pjs.util.FileSystemManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SearchPopup {

    private final FileSystemManager fileSystemManager;
    private final JTree pagesTree;
    private final DefaultMutableTreeNode selectedItem;
    private final JDialog popupDialog;
    private final JTextField searchField;
    private final JList<SearchItemData> resultsList;
    private final DefaultListModel<SearchItemData> resultsListModel;
    private final JButton searchButton;

    public SearchPopup(FileSystemManager fileSystemManager,
                       JTree pagesTree,
                       DefaultMutableTreeNode selectedItem,
                       Frame parentFrame) {
        this.fileSystemManager = fileSystemManager;
        this.pagesTree = pagesTree;
        this.selectedItem = selectedItem;

        popupDialog = new JDialog(parentFrame, "Search Files", true);
        popupDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        searchField = new JTextField();
        searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(80, searchButton.getPreferredSize().height));

        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(searchButton, BorderLayout.EAST);

        resultsListModel = new DefaultListModel<>();
        resultsList = new JList<>(resultsListModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        resultsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof SearchItemData item) {
                    setText(item.getNodeName());
                } else {
                    setText("");
                }

                return component;
            }
        });

        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SearchItemData clickedItem = resultsList.getSelectedValue();
                    if (clickedItem != null && clickedItem.getTreeItem() instanceof DefaultMutableTreeNode treeNode) {
                        TreePath path = new TreePath(treeNode.getPath());
                        pagesTree.setSelectionPath(path);
                        pagesTree.scrollPathToVisible(path);
                        popupDialog.dispose();
                    }
                }
            }
        });

        JScrollPane resultsScrollPane = new JScrollPane(resultsList);
        resultsScrollPane.setPreferredSize(new Dimension(350, 200));

        JPanel layout = new JPanel();
        layout.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        JLabel searchLabel = new JLabel("Search for text in files:");
        JLabel resultsLabel = new JLabel("Files containing search text:");

        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        layout.add(searchLabel);
        layout.add(Box.createVerticalStrut(10));
        layout.add(searchBox);
        layout.add(Box.createVerticalStrut(10));
        layout.add(resultsLabel);
        layout.add(Box.createVerticalStrut(10));
        layout.add(resultsScrollPane);

        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        popupDialog.setContentPane(layout);
        popupDialog.setSize(400, 350);
        popupDialog.setLocationRelativeTo(parentFrame);
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }

        resultsListModel.clear();

        List<SearchItemData> locationsToSearch = new ArrayList<>();
        gatherFileNames(locationsToSearch, selectedItem);

        List<SearchItemData> foundItems =
                fileSystemManager.searchForTextInsideList(locationsToSearch, searchText);

        for (SearchItemData item : foundItems) {
            resultsListModel.addElement(item);
        }
    }

    private void gatherFileNames(List<SearchItemData> locationsToSearch, DefaultMutableTreeNode selectedItem) {
        Object userObject = selectedItem.getUserObject();
        if (!(userObject instanceof TreeItemData data)) {
            return;
        }

        locationsToSearch.add(SearchItemData.builder()
                .fileName(data.getFileName())
                .nodeName(data.getNodeName())
                .treeItem(selectedItem)
                .build());

        Enumeration<?> children = selectedItem.children();
        while (children.hasMoreElements()) {
            Object childObj = children.nextElement();
            if (childObj instanceof DefaultMutableTreeNode childNode) {
                Object childUserObject = childNode.getUserObject();
                if (childUserObject instanceof TreeItemData childData) {
                    locationsToSearch.add(SearchItemData.builder()
                            .fileName(childData.getFileName())
                            .nodeName(childData.getNodeName())
                            .treeItem(childNode)
                            .build());
                }

                if (!childNode.isLeaf()) {
                    gatherFileNames(locationsToSearch, childNode);
                }
            }
        }
    }

    public void show() {
        popupDialog.setVisible(true);
    }

    public SearchItemData getSelectedFile() {
        return resultsList.getSelectedValue();
    }
}