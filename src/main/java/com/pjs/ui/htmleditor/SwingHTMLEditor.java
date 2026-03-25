package com.pjs.ui.htmleditor;

import com.pjs.ui.htmleditor.html.CustomHTMLEditorKit;
import com.pjs.ui.htmleditor.html.HTMLBody;
import com.pjs.ui.htmleditor.palette.Palette;
import com.pjs.ui.htmleditor.palette.WebPalette;
import com.pjs.ui.htmleditor.toolbar.ToolBar;
import com.pjs.ui.htmleditor.toolbar.ToolBarCommands;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SwingHTMLEditor extends JPanel {

    final JTextPane editorPane;

    public SwingHTMLEditor() {
        this(new WebPalette());
    }

    public SwingHTMLEditor(Palette palette) {
        super();
        this.editorPane = new JTextPane();
        initComponents(palette);
    }

    private void initComponents(Palette palette) {

        setLayout(new BorderLayout());

        editorPane.setContentType("text/html");
        editorPane.setBackground(Color.WHITE);

        JScrollPane editorScrollPane = new JScrollPane();
        editorScrollPane.setViewportView(editorPane);

        CustomHTMLEditorKit kit = new CustomHTMLEditorKit();
        editorPane.setEditorKit(kit);
        fixEditorCssRules(kit);
        kit.setAutoFormSubmission(false);

        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        doc.setPreservesUnknownTags(false);
        doc.putProperty("IgnoreCharsetDirective", true);
        editorPane.setDocument(doc);
        editorPane.setText(createEmptyDocument());

        installBackspaceHandler();

        ToolBarCommands toolBarCommands = new ToolBarCommands(doc);

        ToolBar toolbar = new ToolBar(editorPane, toolBarCommands, palette);

        add(toolbar.asJToolbar(), BorderLayout.NORTH);
        add(editorScrollPane, BorderLayout.CENTER);
        afterLoad(editorPane);
    }

    private void installBackspaceHandler() {
        KeyStroke backspace = KeyStroke.getKeyStroke("BACK_SPACE");

        InputMap inputMap = editorPane.getInputMap();
        ActionMap actionMap = editorPane.getActionMap();

        Object previousKey = inputMap.get(backspace);
        final Action previousAction =
                previousKey == null ? null : actionMap.get(previousKey);

        inputMap.put(backspace, "custom-html-backspace");
        actionMap.put("custom-html-backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!handleEmptyListItemBackspace()) {
                    if (previousAction != null) {
                        previousAction.actionPerformed(e);
                    }
                }
            }
        });
    }

    private boolean handleEmptyListItemBackspace() {
        if (!(editorPane.getDocument() instanceof HTMLDocument doc)) {
            return false;
        }

        if (editorPane.getSelectionStart() != editorPane.getSelectionEnd()) {
            return false;
        }

        int caret = editorPane.getCaretPosition();
        if (caret < 0 || caret > doc.getLength()) {
            return false;
        }

        Element li = findParentElement(doc.getCharacterElement(Math.max(0, caret - 1)), HTML.Tag.LI);
        if (li == null) {
            li = findParentElement(doc.getCharacterElement(caret), HTML.Tag.LI);
        }
        if (li == null) {
            return false;
        }

        if (!isVisuallyEmpty(doc, li)) {
            return false;
        }

        Element ul = findParentElement(li, HTML.Tag.UL);
        Element ol = findParentElement(li, HTML.Tag.OL);
        Element list = ul != null ? ul : ol;

        if (list == null) {
            return false;
        }

        try {
            if (countChildElements(list, HTML.Tag.LI) <= 1) {
                int caretAfter = Math.max(0, list.getStartOffset() - 1);
                doc.setOuterHTML(list, "<p></p>");
                setSafeCaretPosition(caretAfter);
            } else {
                int caretAfter = Math.max(0, li.getStartOffset() - 1);
                doc.setOuterHTML(li, "");
                setSafeCaretPosition(caretAfter);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void setSafeCaretPosition(int pos) {
        SwingUtilities.invokeLater(() -> {
            int safe = Math.max(0, Math.min(pos, editorPane.getDocument().getLength()));
            editorPane.setCaretPosition(safe);
        });
    }

    private Element findParentElement(Element start, HTML.Tag tag) {
        Element current = start;
        while (current != null) {
            Object name = current.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (tag.equals(name)) {
                return current;
            }
            current = current.getParentElement();
        }
        return null;
    }

    private int countChildElements(Element parent, HTML.Tag tag) {
        int count = 0;
        for (int i = 0; i < parent.getElementCount(); i++) {
            Element child = parent.getElement(i);
            Object name = child.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (tag.equals(name)) {
                count++;
            }
        }
        return count;
    }

    private boolean isVisuallyEmpty(HTMLDocument doc, Element element) {
        try {
            int start = element.getStartOffset();
            int end = element.getEndOffset();
            String text = doc.getText(start, Math.max(0, end - start));

            if (text == null) {
                return true;
            }

            text = text
                    .replace("\u00A0", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .trim();

            return text.isEmpty();
        } catch (BadLocationException e) {
            return false;
        }
    }

    public void addDocumentListener(DocumentListener listener) {
        editorPane.getDocument().addDocumentListener(listener);
    }

    public void setText(String text) {
        editorPane.setText(text);
    }

    public String getText() {
        HTMLDocument document = (HTMLDocument) editorPane.getDocument();
        CustomHTMLEditorKit editorKit = (CustomHTMLEditorKit) editorPane.getEditorKit();

        try {
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            editorKit.write(stringWriter, document, 0, document.getLength());
            return stringWriter.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize editor document", e);
        }
    }

    protected void afterLoad(final JTextPane editor) {

        StyledDocument doc = editor.getStyledDocument();

        for (int i = 0; i <= doc.getLength(); i++) {
            Element elem = doc.getParagraphElement(i);
            AttributeSet attr = elem.getAttributes();
            Object o = attr.getAttribute(StyleConstants.NameAttribute);

            if (o == HTML.Tag.P) {
                editor.setCaretPosition(elem.getStartOffset());
                break;
            }

            i = elem.getEndOffset() - 1;
        }

        editor.requestFocus();
    }

    protected void fixEditorCssRules(final HTMLEditorKit kit) {
        StyleSheet css = kit.getStyleSheet();
        css.addRule("body, p, li { font-size: 1.1em; }");
    }

    protected String createEmptyDocument() {
        return """
                <html><head>
                <style type=\"text/css\">
                body { color: black; background-color: white; font-family: \"Verdana\"; font-size: 12pt; font-weight: normal; font-style: normal; }
                p { margin-top: 2px; margin-bottom: 2px; }
                hr { border-top: 1px solid gray; }
                ol, ul { margin-left: 40px; margin-top: 2px; }
                </style></head><body>
                <p></p>
                </body></html>
        """;
    }
}