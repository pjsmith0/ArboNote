package com.pjs.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;

public class BasicTextEditor extends JPanel {
    public static final String FORMATTING = "Formatting";
    public static final String PARAGRAPH = "Paragraph";
    public static final String HEADING_1 = "Heading 1";
    public static final String HEADING_2 = "Heading 2";
    public static final String HEADING_3 = "Heading 3";
    public static final String PREFORMATTED = "Preformatted";
    private final JEditorPane editor = new JEditorPane();
    private final HTMLEditorKit htmlKit = new HTMLEditorKit();
    private HTMLDocument htmlDocument = (HTMLDocument) htmlKit.createDefaultDocument();
    private final UndoManager undoManager = new UndoManager();

    private boolean sourceMode = false;
    private String lastRenderedHtml = "";

    public BasicTextEditor() {
        super(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        htmlDocument.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

        editor.setEditorKit(htmlKit);
        editor.setDocument(htmlDocument);
        editor.setContentType("text/html");
        editor.setBorder(new EmptyBorder(16, 16, 16, 16));
        editor.setBackground(Color.WHITE);
        lastRenderedHtml = getHtml();

        htmlDocument.addUndoableEditListener(undoManager);

        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));
        toolbarPanel.setBackground(new Color(239, 239, 239));
        toolbarPanel.add(createToolbarRowOne());
        toolbarPanel.add(createToolbarRowTwo());

        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

    }

    private JToolBar createToolbarRowOne() {
        JToolBar bar = baseToolbar();

        JComboBox<String> fontFamily = new JComboBox<>(new String[] {
                "SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput", "Arial", "Courier New", "Times New Roman"
        });
        fontFamily.setSelectedItem("SansSerif");
        fontFamily.addActionListener(e -> applyFontFamily(Objects.toString(fontFamily.getSelectedItem(), "SansSerif")));
        bar.add(fontFamily);

        JComboBox<String> formatting = new JComboBox<>(new String[] {
                FORMATTING, PARAGRAPH, HEADING_1, HEADING_2, HEADING_3, PREFORMATTED
        });
        formatting.addActionListener(e -> {
            String choice = Objects.toString(formatting.getSelectedItem(), FORMATTING);
            if (!FORMATTING.equals(choice)) {
                applyBlockTag(choice);
                formatting.setSelectedIndex(0);
            }
        });
        bar.add(formatting);

        JComboBox<Integer> fontSize = new JComboBox<>(new Integer[] {
                8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36
        });
        fontSize.setSelectedItem(14);
        fontSize.addActionListener(e -> applyFontSize((Integer) fontSize.getSelectedItem()));
        bar.add(fontSize);

        bar.addSeparator();
        bar.add(button(null, "/icons/undo_16dp.png", e -> undo(), "Undo"));
        bar.add(button(null, "/icons/redo_16dp.png", e -> redo(), "Redo"));

        return bar;
    }

    private JToolBar createToolbarRowTwo() {
        JToolBar bar = baseToolbar();

        bar.add(button(null, "/icons/format_bold_16dp.png", e -> new StyledEditorKit.BoldAction().actionPerformed(e), "Bold")).setFont(bar.getFont().deriveFont(Font.BOLD));
        bar.add(button(null, "/icons/format_italic_16dp.png", e -> new StyledEditorKit.ItalicAction().actionPerformed(e), "Italic"));
        bar.add(button(null, "/icons/format_underlined_16dp.png", e -> new StyledEditorKit.UnderlineAction().actionPerformed(e), "Underline"));
        bar.add(button(null, "/icons/colors_16dp.png", e -> chooseTextColor(), "Text color"));

        bar.addSeparator();
        bar.add(button(null, "/icons/format_indent_decrease_16dp.png", e -> insertIndentHtml(false), "Outdent"));
        bar.add(button(null, "/icons/format_indent_increase_16dp.png", e -> insertIndentHtml(true), "Indent"));
        bar.add(button(null, "/icons/format_quote_16dp.png", e -> wrapSelection("<blockquote>", "</blockquote>"), "Quote"));
        bar.add(button(null, "/icons/format_list_numbered_16dp.png", e -> insertList(true), "Numbered list"));
        bar.add(button(null, "/icons/list_16dp.png", e -> insertList(false), "Bulleted list"));

        bar.addSeparator();
        bar.add(button(null, "/icons/link_16dp.png", e -> insertLink(), "Insert link"));
        bar.add(button(null, "/icons/link_off_16dp.png", e -> unwrapAnchor(), "Remove link"));
        bar.add(button(null, "/icons/image_16dp.png", e -> insertImage(), "Insert image"));
        bar.add(button(null, "/icons/hide_image_16dp.png", e -> insertTable(), "Insert table"));
        bar.add(button(null, "/icons/horizontal_rule_16dp.png", e -> insertHtml("<hr/>"), "Horizontal rule"));

        bar.addSeparator();
        bar.add(button(null, "/icons/format_align_left_16dp.png", e -> new StyledEditorKit.AlignmentAction("Left", StyleConstants.ALIGN_LEFT).actionPerformed(e), "Align left"));
        bar.add(button(null, "/icons/format_align_center_16dp.png", e -> new StyledEditorKit.AlignmentAction("Center", StyleConstants.ALIGN_CENTER).actionPerformed(e), "Align center"));
        bar.add(button(null, "/icons/format_align_right_16dp.png", e -> new StyledEditorKit.AlignmentAction("Right", StyleConstants.ALIGN_RIGHT).actionPerformed(e), "Align right"));
        bar.add(button(null, "/icons/format_align_justify_16.png", e -> new StyledEditorKit.AlignmentAction("Justify", StyleConstants.ALIGN_JUSTIFIED).actionPerformed(e), "Justify"));
        bar.add(button(null, "/icons/code_16dp.png", e -> toggleSourceMode(), "Toggle HTML source"));

        return bar;
    }

    private JToolBar baseToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setBorder(new EmptyBorder(6, 8, 4, 8));
        bar.setBackground(new Color(239, 239, 239));
        return bar;
    }

    private JButton button(String text, String iconPath, java.awt.event.ActionListener action, String tooltip) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        Optional.ofNullable(iconPath).ifPresent(s -> button.setIcon(new ImageIcon(getClass().getResource(s))));
        button.setMargin(new Insets(4, 8, 4, 8));
        return button;
    }

    private void applyFontFamily(String family) {
        if (sourceMode) {
            return;
        }
        MutableAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributes, family);
        new StyledEditorKit.FontFamilyAction("font-family", family).actionPerformed(
                new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, family)
        );
    }

    private void applyFontSize(Integer size) {
        if (sourceMode || size == null) {
            return;
        }
        new StyledEditorKit.FontSizeAction("font-size", htmlFontSize(size)).actionPerformed(
                new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, String.valueOf(size))
        );
    }

    private int htmlFontSize(int px) {
        if (px <= 9) return 1;
        if (px <= 11) return 2;
        if (px <= 14) return 3;
        if (px <= 18) return 4;
        if (px <= 24) return 5;
        if (px <= 32) return 6;
        return 7;
    }

    private void applyBlockTag(String choice) {
        if (sourceMode) {
            return;
        }
        System.out.println(choice);
        switch (choice) {
            case PARAGRAPH -> wrapSelection("<p>", "</p>");
            case HEADING_1 -> wrapSelection("<h1>", "</h1>");
            case HEADING_2 -> wrapSelection("<h2>", "</h2>");
            case HEADING_3 -> wrapSelection("<h3>", "</h3>");
            case PREFORMATTED -> wrapSelection("<pre>", "</pre>");
            default -> { }
        }
    }

    private void chooseTextColor() {
        if (sourceMode) {
            return;
        }
        Color color = JColorChooser.showDialog(this, "Choose Text Color", Color.BLACK);
        if (color != null) {
            String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            wrapSelection("<span style=\"color:" + hex + ";\">", "</span>");
        }
    }

    private void insertList(boolean ordered) {
        String tag = ordered ? "ol" : "ul";
        String selected = editor.getSelectedText();
        if (selected == null || selected.isBlank()) {
            insertHtml("<" + tag + "><li>List item</li></" + tag + ">");
            return;
        }
        String[] lines = selected.split("\\R+");
        StringBuilder sb = new StringBuilder("<").append(tag).append(">");
        for (String line : lines) {
            if (!line.isBlank()) {
                sb.append("<li>").append(escapeHtml(line.trim())).append("</li>");
            }
        }
        sb.append("</").append(tag).append(">");
        replaceSelectionWithHtml(sb.toString());
    }

    private void insertIndentHtml(boolean indent) {
        String style = indent ? "margin-left:40px;" : "margin-left:0;";
        wrapSelection("<div style=\"" + style + "\">", "</div>");
    }

    private void insertLink() {
        String url = JOptionPane.showInputDialog(this, "Enter URL:", "https://");
        if (url == null || url.isBlank()) {
            return;
        }
        String text = editor.getSelectedText();
        if (text == null || text.isBlank()) {
            text = url;
        } else {
            text = escapeHtml(text);
        }
        replaceSelectionWithHtml("<a href=\"" + escapeAttribute(url.trim()) + "\">" + text + "</a>");
    }

    private void unwrapAnchor() {
        String selected = editor.getSelectedText();
        if (selected != null && !selected.isBlank()) {
            replaceSelectionWithHtml(escapeHtml(selected));
        }
    }

    private void insertImage() {
        String pathOrUrl = JOptionPane.showInputDialog(this, "Enter image URL or path:");
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return;
        }
        insertHtml("<img src=\"" + escapeAttribute(pathOrUrl.trim()) + "\" alt=\"image\"/>");
    }

    private void insertTable() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        JTextField rowsField = new JTextField("2");
        JTextField colsField = new JTextField("2");
        panel.add(new JLabel("Rows:"));
        panel.add(rowsField);
        panel.add(new JLabel("Columns:"));
        panel.add(colsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert Table", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int rows;
        int cols;
        try {
            rows = Math.max(1, Integer.parseInt(rowsField.getText().trim()));
            cols = Math.max(1, Integer.parseInt(colsField.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Rows and columns must be numeric.", "Invalid table", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\">");
        for (int r = 0; r < rows; r++) {
            sb.append("<tr>");
            for (int c = 0; c < cols; c++) {
                sb.append("<td> </td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table><p></p>");
        insertHtml(sb.toString());
    }

    private void wrapSelection(String prefix, String suffix) {
        if (sourceMode) {
            return;
        }
        String selected = editor.getSelectedText();
        if (selected == null || selected.isEmpty()) {
            insertHtml(prefix + suffix);
            return;
        }
        replaceSelectionWithHtml(prefix + escapeHtml(selected) + suffix);
    }

    private void replaceSelectionWithHtml(String html) {
        if (sourceMode) {
            editor.replaceSelection(html);
            return;
        }

        int start = editor.getSelectionStart();
        int length = editor.getSelectionEnd() - start;
        try {
            if (length > 0) {
                htmlDocument.remove(start, length);
            }
            insertHtmlAtCaret(html, start);
        } catch (BadLocationException ex) {
            throw new IllegalStateException("Could not replace selection.", ex);
        }
    }

    private void insertHtml(String html) {
        insertHtmlAtCaret(html, editor.getCaretPosition());
    }

    private void insertHtmlAtCaret(String html, int offset) {
        if (sourceMode) {
            editor.replaceSelection(html);
            return;
        }
        try {
            htmlKit.insertHTML(htmlDocument, offset, html, 0, 0, null);
        } catch (BadLocationException | IOException ex) {
            throw new IllegalStateException("Could not insert HTML.", ex);
        }
    }

    private void undo() {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException ignored) {
        }
    }

    private void redo() {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotRedoException ignored) {
        }
    }

    private void toggleSourceMode() {
        try {
            if (!sourceMode) {
                lastRenderedHtml = getHtml();
                editor.setContentType("text/plain");
                editor.setText(lastRenderedHtml);
                sourceMode = true;
            } else {
                String html = editor.getText();
                setHtml(html);
                lastRenderedHtml = html;
                sourceMode = false;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not switch editor mode:\n" + ex.getMessage(),
                    "Mode switch failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getHtml() {
        if (sourceMode) {
            return editor.getText();
        }
        try (Writer writer = new StringWriter()) {
            HTMLDocument currentDoc = (HTMLDocument) editor.getDocument();
            htmlKit.write(writer, currentDoc, 0, currentDoc.getLength());
            return writer.toString();
        } catch (IOException | BadLocationException ex) {
            throw new IllegalStateException("Could not read HTML from editor.", ex);
        }
    }

    public void setHtml(String html) {
        try {
            editor.setContentType("text/html");

            HTMLDocument newDoc = (HTMLDocument) htmlKit.createDefaultDocument();
            newDoc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

            undoManager.discardAllEdits();
            newDoc.addUndoableEditListener(undoManager);

            try (Reader reader = new StringReader(
                    (html == null || html.isBlank()) ? defaultHtml() : html)) {
                htmlKit.read(reader, newDoc, 0);
            }

            htmlDocument = newDoc;

            editor.setDocument(htmlDocument);

            sourceMode = false;

        } catch (IOException | BadLocationException ex) {
            throw new IllegalArgumentException("Invalid HTML content.", ex);
        }
    }

    private String defaultHtml() {
        return """
                <html>
                  <body>
                  </body>
                </html>
                """;
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escapeAttribute(String text) {
        return escapeHtml(text).replace("'", "&#39;");
    }
}
