package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;

public class AlignRightCommand implements Command {

    @Override
    public String getActionMapKey() {
        return "right-justify";
    }

    @Override
    public String getText() {
        return "Left";
    }

    @Override
    public String getTooltipText() {
        return "Right Alignment";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/align-end.png");
    }

}
