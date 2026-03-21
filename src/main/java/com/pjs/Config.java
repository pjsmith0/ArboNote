package com.pjs;

import lombok.Data;

@Data
public class Config {
    private String rootPath = "/home/pjs/jnotes_data";
    private String fileExtention = ".html";
    private String treeFileName = "tree.json";

    public void init() {

    }
}
