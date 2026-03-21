package com.pjs.ui.htmleditor.component;

import com.pjs.ui.htmleditor.component.icon.ColorIcon;
import com.pjs.ui.htmleditor.palette.Palette;
import com.pjs.ui.htmleditor.palette.PaletteItem;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ColorSelectorButton extends JButton {
    private static final long serialVersionUID = 42L;

    private final transient SetColorActionFactory colorFactory;
    private final transient Palette palette;
    private final JPopupMenu colorMenu;

    public ColorSelectorButton(Palette palette) {
        this.colorFactory = new SetColorActionFactory();
        this.palette = palette;

        this.colorMenu = createPopup();

        setSelectedColor(palette.getDefault());

        addActionListener(actionEvent ->
                colorMenu.show(ColorSelectorButton.this, 0,
                        ColorSelectorButton.this.getHeight()));

        setIcons();
    }

    private void setIcons() {
        setText(null);
        setIcon(new ImageIcon(getClass().getResource("/icons/palette.png")));
    }

    protected final JPopupMenu createPopup() {
        JPopupMenu result = new JPopupMenu();

        result.setRequestFocusEnabled(false);
        result.setLayout(new GridLayout(8, 2));

        for (PaletteItem paletteItem : palette.getItems()) {
            JMenuItem menuItem = new JMenuItem(paletteItem.getName());
            menuItem.addActionListener(e -> setSelectedColor(paletteItem, e));

            Icon colorIcon = new ColorIcon(paletteItem.getColor(), 12);
            menuItem.setIcon(colorIcon);

            result.add(menuItem);
        }

        return result;
    }

    public void setSelectedColor(PaletteItem paletteItem) {
        final Icon icon = getIcon();
        final String text = getText();

        setAction(colorFactory.create(paletteItem));

        // Reset original text and icon
        setIcon(icon);
        setText(text);
    }

    public void setSelectedColor( PaletteItem c, ActionEvent e) {
        setSelectedColor(c);
        getAction().actionPerformed(e);
    }

    public static class SetColorActionFactory {
        public StyledEditorKit.StyledTextAction create(PaletteItem newColor) {
            return new StyledEditorKit.ForegroundAction(newColor.getName(), newColor.getColor());
        }
    }
}
