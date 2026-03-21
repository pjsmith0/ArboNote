package com.pjs.ui.htmleditor;

import com.pjs.ui.htmleditor.html.CustomHTMLEditorKit;
import com.pjs.ui.htmleditor.html.HTMLBody;
import com.pjs.ui.htmleditor.palette.Palette;
import com.pjs.ui.htmleditor.palette.WebPalette;
import com.pjs.ui.htmleditor.toolbar.ToolBar;
import com.pjs.ui.htmleditor.toolbar.ToolBarCommands;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;

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

        ToolBarCommands toolBarCommands = new ToolBarCommands(doc);

        ToolBar toolbar = new ToolBar(editorPane, toolBarCommands, palette);

        add(toolbar.asJToolbar(), BorderLayout.NORTH);
        add(editorScrollPane, BorderLayout.CENTER);
        afterLoad(editorPane);
    }

    public void addDocumentListener(DocumentListener listener) {
        editorPane.getDocument().addDocumentListener(listener);
    }

    public void setText(String text) {
        editorPane.setText(text);
    }

    public String getText(){
        return new HTMLBody((HTMLDocument) editorPane.getDocument(), (CustomHTMLEditorKit)editorPane.getEditorKit()).asString();
    }

    protected void afterLoad(final JTextPane editor) {

        // Goto first Paragraph
        StyledDocument doc = editor.getStyledDocument();

        for (int i = 0; i <= doc.getLength(); i++) {
            Element elem = doc.getParagraphElement(i);
            AttributeSet attr = elem.getAttributes();
            Object o = attr.getAttribute(StyleConstants.NameAttribute);

            if (o == HTML.Tag.P) {
                editor.setCaretPosition(elem.getStartOffset());
                break;
            }

            i = elem.getEndOffset() - 1; // fast-forward
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
