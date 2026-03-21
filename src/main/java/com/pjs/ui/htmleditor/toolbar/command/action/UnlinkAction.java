package com.pjs.ui.htmleditor.toolbar.command.action;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;

public class UnlinkAction extends HTMLEditorKit.HTMLTextAction {
    private static final long serialVersionUID = 42L;

    public UnlinkAction() {
        super("unlink");
    }

    public void actionPerformed(final ActionEvent e) {
        final JTextPane editor = (JTextPane) getEditor(e);
        if (editor != null) {
            final int pss = editor.getSelectionStart();
            final int pse = editor.getSelectionEnd();
            final HTMLDocument doc = getHTMLDocument(editor);
            for (int i = pss; i <= pse; i++) {
                final Element elem = doc.getCharacterElement(i);
                final AttributeSet elemAttr = elem.getAttributes();
                final AttributeSet tagAttr = (AttributeSet) elemAttr.getAttribute(HTML.Tag.A);
                if (tagAttr != null) {
                    final SimpleAttributeSet newAttr = new SimpleAttributeSet(elem.getAttributes());
                    newAttr.removeAttribute(HTML.Tag.A);
                    doc.setCharacterAttributes(elem.getStartOffset(),
                            elem.getEndOffset() - elem.getStartOffset(), newAttr, true);
                }
                i = elem.getEndOffset() - 1; // fast-forward
            }
        }
    }
}
