package com.pjs.ui.htmleditor.toolbar.command.action;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import java.awt.event.ActionEvent;

public class StrikethroughAction extends StyledEditorKit.StyledTextAction {
    private static final long serialVersionUID = 42L;

    public StrikethroughAction() {
        super("font-strike");
    }

    public void actionPerformed(final ActionEvent e) {
        final JEditorPane editor = getEditor(e);
        if (editor != null) {
            final StyledEditorKit kit = getStyledEditorKit(editor);
            final MutableAttributeSet attr = kit.getInputAttributes();
            final boolean strike = !StyleConstants.isStrikeThrough(attr);
            final SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setStrikeThrough(sas, strike);
            setCharacterAttributes(editor, sas, false);
        }
    }
}
