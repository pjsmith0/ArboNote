package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;

public class AlignCenterCommand implements Command {

    @Override
    public String getActionMapKey() {
        return "center-justify";
    }

    @Override
    public String getText() {
        return "Left";
    }

    @Override
    public String getTooltipText() {
        return "Center Alignment";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/align-center.png");
    }

}
