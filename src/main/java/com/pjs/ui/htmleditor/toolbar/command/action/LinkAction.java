package com.pjs.ui.htmleditor.toolbar.command.action;

import com.pjs.ui.htmleditor.component.LinkDialog;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;

public class LinkAction extends HTMLEditorKit.HTMLTextAction {
    private static final long serialVersionUID = 42L;

    public LinkAction() {
        super("link-broken");
    }

    private void insertLink(JTextPane editor, String href) {
        SimpleAttributeSet sasTag = new SimpleAttributeSet();
        SimpleAttributeSet sasAttr = new SimpleAttributeSet();

        sasAttr.addAttribute(HTML.Attribute.HREF, href);
        sasTag.addAttribute(HTML.Tag.A, sasAttr);

        int pss = editor.getSelectionStart();
        int pse = editor.getSelectionEnd();

        if (pss != pse) {
            HTMLDocument doc = getHTMLDocument(editor);
            doc.setCharacterAttributes(pss, pse - pss, sasTag, false);
        }
    }

    private String findLink(final JTextPane editor) {
        HTMLDocument doc = getHTMLDocument(editor);

        for (int i = editor.getSelectionStart(); i <= editor.getSelectionEnd(); i++) {
            Element elem = doc.getCharacterElement(i);
            AttributeSet elemAttr = elem.getAttributes();
            AttributeSet tagAttr = (AttributeSet) elemAttr.getAttribute(HTML.Tag.A);

            if (tagAttr != null) {
                String href = (String) tagAttr.getAttribute(HTML.Attribute.HREF);

                if ((href != null) && !href.isEmpty()) {
                    return href;
                }
            }

            i = elem.getEndOffset() - 1; // fast-forward
        }

        return null;
    }

    public void actionPerformed(final ActionEvent e) {
        JTextPane editor = (JTextPane) getEditor(e);

        if (editor != null) {
            String href = findLink(editor);
            LinkDialog d = new LinkDialog(null);

            if ((href != null) && !href.isEmpty()) {
                d.setLink(href);
            }

            d.setVisible(true);

            if (!d.isAccepted()) { // Cancel
                return;
            }

            insertLink(editor, d.getLink());
        }
    }
}
