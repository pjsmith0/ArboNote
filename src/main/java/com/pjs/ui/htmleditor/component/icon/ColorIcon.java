package com.pjs.ui.htmleditor.component.icon;

import javax.swing.*;
import java.awt.*;

public class ColorIcon implements Icon {
    private final Color color;
    private final int size;

    public ColorIcon(Color color, int size) {
        this.color = color;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, size, size);

        // optional border so light colors are visible
        g.setColor(Color.BLACK);
        g.drawRect(x, y, size - 1, size - 1);
    }
}
