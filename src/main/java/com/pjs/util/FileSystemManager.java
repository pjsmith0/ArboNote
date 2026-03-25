package com.pjs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pjs.Config;
import com.pjs.model.SearchItemData;
import com.pjs.model.TreeItemData;
import com.pjs.model.TreeHierarchyData;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class FileSystemManager {

    private final ObjectMapper mapper;
    private final Config config;

    public FileSystemManager(Config config) {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);  // Pretty print

        this.config = config;

        File baseDir = new File(config.getRootPath());
        if (!baseDir.exists()) {
            System.out.printf("Base path [%s] does not exist...%n", config.getRootPath());
            System.exit(1);
        }

        // check if tree file exists
        Path treefilePath = Paths.get(config.getRootPath(), config.getTreeFileName());
        if (!Files.exists(treefilePath)) {
            saveTreeFile(TreeHierarchyData.builder()
                    .fileName("Root.html")
                    .nodeName("Home")
                    .build());
        }
    }

    public void saveTreeItem(String itemLocation, String htmlText) {
        Path path = getAbsoluteFileLocation(itemLocation);

        try {
            File file = path.toFile();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(htmlText);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public String loadData(String fileName) {
        File file = getAbsoluteFileLocation(fileName).toFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        return FileUtils.readFileToString(file, "UTF-8");
    }

    @SneakyThrows
    public String createLocation() {
        Path path = Paths.get(config.getRootPath(), UUID.randomUUID() + config.getFileExtention());
        File newFile = path.toFile();
        newFile.createNewFile();
        return newFile.getName();
    }

    private Path getAbsoluteFileLocation(String fullItemName) {
        return Paths.get(config.getRootPath(), fullItemName);
    }

    public void saveTreeToJson(javax.swing.tree.TreeNode root) {
        TreeHierarchyData rootNode = treeItemToNode(root);
        saveTreeFile(rootNode);
    }

    private void saveTreeFile(TreeHierarchyData rootNode) {
        try {
            mapper.writeValue(new FileOutputStream(Path.of(config.getRootPath(), config.getTreeFileName()).toFile()), rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TreeHierarchyData treeItemToNode(javax.swing.tree.TreeNode treeNode) {
        if (treeNode == null) return null;

        DefaultMutableTreeNode treeNodeNode = (DefaultMutableTreeNode) treeNode;

        TreeHierarchyData node = TreeHierarchyData.builder()
                .nodeName(((TreeItemData)treeNodeNode.getUserObject()).getNodeName())
                .fileName(((TreeItemData)treeNodeNode.getUserObject()).getFileName())
                .build();

        Enumeration<javax.swing.tree.TreeNode> children = treeNodeNode.children();
        while (children.hasMoreElements()) {
            javax.swing.tree.TreeNode childTreeNode = children.nextElement();
            node.getChildren().add(treeItemToNode(childTreeNode));
        }

        return node;
    }

    @SneakyThrows
    public void deleteFile(String fileName) {
        FileUtils.delete(getAbsoluteFileLocation(fileName).toFile());
    }

    public List<SearchItemData> searchForTextInsideList(List<SearchItemData> locationsToSearch, String searchText) {
        return locationsToSearch.stream().filter(
                searchItemData -> ChunkedFileSearch.searchInChunksIgnoreCase(
                        Path.of(config.getRootPath(), searchItemData.getFileName()),
                        searchText))
                .toList();
    }

}
