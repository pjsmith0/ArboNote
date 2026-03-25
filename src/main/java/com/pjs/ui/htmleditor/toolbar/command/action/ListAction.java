package com.pjs.ui.htmleditor.toolbar.command.action;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ListAction extends StyledEditorKit.StyledTextAction {

    public ListAction() {
        super("insert-unordered-list");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        final JTextPane editor = (JTextPane) getEditor(actionEvent);
        if (editor == null) return;
        if (!(editor.getDocument() instanceof HTMLDocument doc)) return;
        if (!(editor.getEditorKit() instanceof HTMLEditorKit)) return;

        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();

        try {
            if (start == end) {
                insertSingleEmptyListItem(doc, editor.getCaretPosition());
            } else {
                replaceSelectedParagraphsWithList(doc, start, end);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void insertSingleEmptyListItem(HTMLDocument doc, int pos) throws Exception {
        Element paragraph = doc.getParagraphElement(pos);
        if (paragraph == null) return;

        doc.setOuterHTML(paragraph, "<ul><li>&nbsp;</li></ul>");
    }

    private void replaceSelectedParagraphsWithList(HTMLDocument doc, int start, int end) throws Exception {
        List<Element> paragraphs = collectParagraphs(doc, start, end);
        if (paragraphs.isEmpty()) return;

        StringBuilder html = new StringBuilder("<ul>");
        for (Element paragraph : paragraphs) {
            String text = extractParagraphText(doc, paragraph);
            html.append("<li>")
                    .append(text.isBlank() ? "&nbsp;" : escapeHtml(text.strip()))
                    .append("</li>");
        }
        html.append("</ul>");

        Element first = paragraphs.get(0);

        // Replace the first selected paragraph with the whole list.
        doc.setOuterHTML(first, html.toString());

        // Remove the remaining paragraphs, which are now stale leftovers.
        for (int i = 1; i < paragraphs.size(); i++) {
            Element p = paragraphs.get(i);
            try {
                doc.setOuterHTML(p, "");
            } catch (Exception ignored) {
                // The DOM may already have shifted after the first replacement.
                // It's safe to ignore failures here if the paragraph is already gone.
            }
        }
    }

    private List<Element> collectParagraphs(HTMLDocument doc, int start, int end) {
        List<Element> paragraphs = new ArrayList<>();

        Element current = doc.getParagraphElement(start);
        while (current != null) {
            if (isParagraph(current)) {
                paragraphs.add(current);
            }

            if (current.getEndOffset() >= end) {
                break;
            }

            int nextOffset = current.getEndOffset();
            if (nextOffset <= current.getStartOffset()) {
                break;
            }

            Element next = doc.getParagraphElement(nextOffset);
            if (next == current) {
                break;
            }
            current = next;
        }

        return paragraphs;
    }

    private boolean isParagraph(Element element) {
        AttributeSet attrs = element.getAttributes();
        Object name = attrs.getAttribute(StyleConstants.NameAttribute);
        return HTML.Tag.P.equals(name) || HTML.Tag.IMPLIED.equals(name);
    }

    private String extractParagraphText(HTMLDocument doc, Element paragraph) throws Exception {
        int start = paragraph.getStartOffset();
        int end = paragraph.getEndOffset();
        String text = doc.getText(start, Math.max(0, end - start));

        return text == null
                ? ""
                : text.replace("\u00A0", " ")
                  .replace("\r", "")
                  .replace("\n", "")
                  .strip();
    }

    private String escapeHtml(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}