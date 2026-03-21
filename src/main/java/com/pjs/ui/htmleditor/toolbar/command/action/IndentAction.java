package com.pjs.ui.htmleditor.toolbar.command.action;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import java.awt.event.ActionEvent;

public class IndentAction extends StyledEditorKit.StyledTextAction {
    private static final long serialVersionUID = 42L;
    private final float step;

    public IndentAction(final String name, final float step) {
        super(name);
        this.step = step; // +20f
    }

    public void actionPerformed(final ActionEvent e) {
        final JTextPane editor = (JTextPane) getEditor(e);
        if (editor != null) {
            final AttributeSet attr = editor.getParagraphAttributes();
            float leftIndent = StyleConstants.getLeftIndent(attr);
            final SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setLeftIndent(sas, Math.max(leftIndent + step, 0f));
            setParagraphAttributes(editor, sas, false);
        }
    }
}
