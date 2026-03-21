package com.pjs.ui.htmleditor.toolbar;

import com.pjs.ui.htmleditor.component.ColorSelectorButton;
import com.pjs.ui.htmleditor.palette.Palette;
import com.pjs.ui.htmleditor.toolbar.command.Command;
import com.pjs.ui.htmleditor.toolbar.command.CommandGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;

public class ToolBar {

    JTextPane editorPane;
    ToolBarCommands toolBarCommands;
    Palette palette;

    public ToolBar(JTextPane editorPane, ToolBarCommands toolBarCommands, Palette palette) {
        this.editorPane = editorPane;
        this.toolBarCommands = toolBarCommands;
        this.palette = palette;
    }

    public JToolBar asJToolbar() {

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Create toolbar buttons
        createToolBarButtons(toolBar);

        // Focus gain/lost enable/disable buttons
        editorPane.addFocusListener(createFocusListener(toolBar));

        return toolBar;
    }


    private void createToolBarButtons(JToolBar toolBar) {

        ActionMap editorActionMap = editorPane.getActionMap();

        for (CommandGroup commandGroup : toolBarCommands.getCommandGroups()) {

            for (Command command : commandGroup.getCommands()) {

                command.getAction().ifPresent(a -> editorActionMap.put(command.getActionMapKey(), a));
                command.getKeyEvent().ifPresent(keyEvent -> mapKey(editorPane, keyEvent, command.getActionMapKey()));

                JButton button = new JButton(editorActionMap.get(command.getActionMapKey()));
                button.setText(command.getText());
                button.setRequestFocusEnabled(command.isRequestFocusEnabled());
                button.setToolTipText(command.getTooltipText());
                assignIcon(command.getIcon(), button);

                command.getActionListener().ifPresent(button::addActionListener);

                toolBar.add(button);
            }

            toolBar.addSeparator();
        }

        // FONT COLOR
        ColorSelectorButton btnFontColor = new ColorSelectorButton(palette);
        btnFontColor.setRequestFocusEnabled(false);
        btnFontColor.setToolTipText("Font Color");
        toolBar.add(btnFontColor);
    }

    private void assignIcon(ImageIcon icon, AbstractButton comp) {
        comp.setText(null);
        comp.setIcon(icon);
    }

    private FocusListener createFocusListener(JToolBar toolBar) {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                enableComponents(toolBar, true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                enableComponents(toolBar, false);
            }
        };
    }

    private void enableComponents(JToolBar container, boolean enable) {
        for (Component comp : container.getComponents()) {
            comp.setEnabled(enable);
        }
    }

    protected void mapKey(final JTextPane editor, final int keyCode, final String actionMapKey) {
        final InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
        //TODO find correct java 8 version Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        final int mask = InputEvent.CTRL_DOWN_MASK;
        im.put(KeyStroke.getKeyStroke(keyCode, mask), actionMapKey);
    }


}
