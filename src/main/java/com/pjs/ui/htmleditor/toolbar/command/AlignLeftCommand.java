package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;

public class AlignLeftCommand implements Command {

    @Override
    public String getActionMapKey() {
        return "left-justify";
    }

    @Override
    public String getText() {
        return "Left";
    }


    @Override
    public String getTooltipText() {
        return "Left Alignment";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/align-start.png");
    }
}

