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
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.List;

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

        // Add CSS rules
        StyleSheet base = htmlKit.getStyleSheet();
        StyleSheet custom = new StyleSheet();
        custom.addStyleSheet(base);
        custom.addRule("""
                p {
                    margin-top: 2px;
                }
            """);
        custom.addRule("""
                pre {
                    font-family: monospace;
                    white-space: pre-wrap;
                    margin: 6px 0;
                }
        """);

        // Attach stylesheet to kit
        htmlKit.setStyleSheet(custom);

        editor.setEditorKit(htmlKit);
        editor.setDocument(htmlDocument);
        editor.setContentType("text/html");
        editor.setBorder(new EmptyBorder(16, 16, 16, 16));
        editor.setBackground(Color.WHITE);
        editor.setFocusTraversalKeysEnabled(false);
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

        installShortcuts();

        setEditorActive(false);
    }

    private JToolBar createToolbarRowOne() {
        JToolBar bar = baseToolbar();

        JComboBox<String> fontFamily = new JComboBox<>(new String[] {
                "SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput", "Arial", "Courier New", "Times New Roman"
        });
        fontFamily.setSelectedItem("SansSerif");
        fixComboSize(fontFamily);
        fontFamily.addActionListener(e -> applyFontFamily(Objects.toString(fontFamily.getSelectedItem(), "SansSerif")));
        bar.add(fontFamily);

        JComboBox<String> formattingComboBox = new JComboBox<>(new String[] {
                FORMATTING, PARAGRAPH, HEADING_1, HEADING_2, HEADING_3, PREFORMATTED
        });
        fixComboSize(formattingComboBox);
        formattingComboBox.addActionListener(e -> {
            String choice = Objects.toString(formattingComboBox.getSelectedItem(), FORMATTING);
            if (!FORMATTING.equals(choice)) {
                applyBlockTag(choice);
                formattingComboBox.setSelectedIndex(0);
            }
        });
        bar.add(formattingComboBox);

        JComboBox<Integer> fontSize = new JComboBox<>(new Integer[] {
                8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36
        });
        fontSize.setSelectedItem(14);
        fixComboSize(fontSize);
        fontSize.addActionListener(e -> applyFontSize((Integer) fontSize.getSelectedItem()));
        bar.add(fontSize);

        bar.addSeparator();
        bar.add(button(null, "/icons/table_16dp.png", e -> insertTable(), "Add table (Ctrl+Alt+T)", null));
        bar.add(button(null, "/icons/table_edit_16dp.png", e -> editTablePreserveContent(), "Edit table", null));
        bar.add(button(null, "/icons/table_remove_16dp.png", e -> removeTable(), "Remove table", null));
        bar.addSeparator();
        bar.add(button(null, "/icons/add_row_below_16dp.png", e -> addTableRow(), "Add row", null));
        bar.add(button(null, "/icons/remove_row_below_16dp.png", e -> removeTableRow(), "Remove row", null));
        bar.addSeparator();
        bar.add(button(null, "/icons/add_column_right_16dp.png", e -> addTableColumn(), "Add column", null));
        bar.add(button(null, "/icons/remove_column_right_16dp.png", e -> removeTableColumn(), "Remove column", null));

        bar.addSeparator();
        bar.add(button(null, "/icons/undo_16dp.png", e -> undo(), "Undo (Ctrl+Z)", null));
        bar.add(button(null, "/icons/redo_16dp.png", e -> redo(), "Redo (Ctrl+Y / Ctrl+Shift+Z)", null));

        bar.addSeparator();
        bar.add(button(null, "/icons/chat_paste_go_16dp.png", e -> pasteAsPreformatted(), "Paste unformatted as <pre> (Ctrl+Shift+V)", null));
        bar.add(button(null, "/icons/code_16dp.png", e -> insertUnformattedText(), "Enter unformatted text as <pre>", null));

        return bar;
    }

    private JToolBar createToolbarRowTwo() {
        JToolBar bar = baseToolbar();

        bar.add(button(null, "/icons/format_bold_16dp.png", e -> new StyledEditorKit.BoldAction().actionPerformed(e), "Bold (Ctrl+B)", null)).setFont(bar.getFont().deriveFont(Font.BOLD));
        bar.add(button(null, "/icons/format_italic_16dp.png", e -> new StyledEditorKit.ItalicAction().actionPerformed(e), "Italic (Ctrl+I)", null));
        bar.add(button(null, "/icons/format_underlined_16dp.png", e -> new StyledEditorKit.UnderlineAction().actionPerformed(e), "Underline (Ctrl+U)", null));
        bar.add(button(null, "/icons/colors_16dp.png", e -> chooseTextColor(), "Text color", null));

        bar.addSeparator();
        bar.add(button(null, "/icons/format_indent_decrease_16dp.png", e -> insertIndentHtml(false), "Outdent", null));
        bar.add(button(null, "/icons/format_indent_increase_16dp.png", e -> insertIndentHtml(true), "Indent", null));
        bar.add(button(null, "/icons/format_quote_16dp.png", e -> wrapSelection("<blockquote>", "</blockquote>"), "Quote", null));
        bar.add(button(null, "/icons/format_list_numbered_16dp.png", e -> insertList(true), "Numbered list", null));
        bar.add(button(null, "/icons/list_16dp.png", e -> insertList(false), "Bulleted list", null));

        bar.addSeparator();
        bar.add(button(null, "/icons/link_16dp.png", e -> insertLink(), "Insert link (Ctrl+K)", null));
        bar.add(button(null, "/icons/link_off_16dp.png", e -> unwrapAnchor(), "Remove link", null));
        bar.add(button(null, "/icons/image_16dp.png", e -> insertImage(), "Insert image", null));
        bar.add(button(null, "/icons/horizontal_rule_16dp.png", e -> insertHtml("<hr/>"), "Horizontal rule", null));

        bar.addSeparator();
        bar.add(button(null, "/icons/format_align_left_16dp.png", e -> new StyledEditorKit.AlignmentAction("Left", StyleConstants.ALIGN_LEFT).actionPerformed(e), "Align left", null));
        bar.add(button(null, "/icons/format_align_center_16dp.png", e -> new StyledEditorKit.AlignmentAction("Center", StyleConstants.ALIGN_CENTER).actionPerformed(e), "Align center", null));
        bar.add(button(null, "/icons/format_align_right_16dp.png", e -> new StyledEditorKit.AlignmentAction("Right", StyleConstants.ALIGN_RIGHT).actionPerformed(e), "Align right", null));
        bar.add(button(null, "/icons/format_align_justify_16.png", e -> new StyledEditorKit.AlignmentAction("Justify", StyleConstants.ALIGN_JUSTIFIED).actionPerformed(e), "Justify", null));
        bar.add(button(null, "/icons/html_16dp.png", e -> toggleSourceMode(), "Toggle HTML source (Ctrl+Shift+H)", null));

        return bar;
    }

    private void installShortcuts() {
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_B, menuMask), "bold", new StyledEditorKit.BoldAction());
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_I, menuMask), "italic", new StyledEditorKit.ItalicAction());
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_U, menuMask), "underline", new StyledEditorKit.UnderlineAction());

        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuMask), "undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Y, menuMask), "redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuMask | InputEvent.SHIFT_DOWN_MASK), "redoShift", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });

        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_K, menuMask), "insertLink", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertLink();
            }
        });
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_H, menuMask | InputEvent.SHIFT_DOWN_MASK), "toggleHtmlSource", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSourceMode();
            }
        });
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuMask | InputEvent.ALT_DOWN_MASK), "insertTable", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertTable();
            }
        });
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask | InputEvent.SHIFT_DOWN_MASK), "pasteAsPreformatted", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteAsPreformatted();
            }
        });
        bindShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuMask | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), "insertUnformattedText", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertUnformattedText();
            }
        });
    }

    private void bindShortcut(KeyStroke keyStroke, String actionKey, Action action) {
        InputMap inputMap = editor.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = editor.getActionMap();
        inputMap.put(keyStroke, actionKey);
        actionMap.put(actionKey, action);
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

    private JButton button(String text, String iconPath, java.awt.event.ActionListener action, String tooltip, Character mnemonic) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        Optional.ofNullable(iconPath).ifPresent(s -> button.setIcon(new ImageIcon(getClass().getResource(s))));
        Optional.ofNullable(mnemonic).ifPresent(c -> button.setMnemonic(c.charValue()));
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

    private void pasteAsPreformatted() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable == null || !transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            String text = (String) transferable.getTransferData(DataFlavor.stringFlavor);
            if (text == null || text.isEmpty()) {
                return;
            }

            if (sourceMode) {
                editor.replaceSelection("<pre>" + escapeHtml(text) + "</pre>");
                return;
            }

            replaceSelectionWithHtml("<pre>" + escapeHtml(normalizeLineEndings(text)) + "</pre>");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not paste plain text from clipboard.\n" + ex.getMessage(),
                    "Paste failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private void insertUnformattedText() {
        JTextArea textArea = new JTextArea(12, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Enter Unformatted Text",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String text = textArea.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        replaceSelectionWithHtml("<pre>" + escapeHtml(normalizeLineEndings(text)) + "</pre>");
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

        insertHtml(buildTableHtml(rows, cols));
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

    private Element findEnclosingElement(int offset, HTML.Tag tag) {
        if (sourceMode) {
            return null;
        }

        offset = Math.max(0, Math.min(offset, htmlDocument.getLength()));
        Element element = htmlDocument.getCharacterElement(Math.max(0, offset - 1));

        while (element != null) {
            Object name = element.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (tag.equals(name)) {
                return element;
            }
            element = element.getParentElement();
        }

        return null;
    }

    private void collectElements(Element root, HTML.Tag tag, List<Element> result) {
        if (root == null) {
            return;
        }

        Object name = root.getAttributes().getAttribute(StyleConstants.NameAttribute);
        if (tag.equals(name)) {
            result.add(root);
        }

        for (int i = 0; i < root.getElementCount(); i++) {
            collectElements(root.getElement(i), tag, result);
        }
    }

    private int getTableRowCount(Element tableElement) {
        List<Element> rows = new ArrayList<>();
        collectElements(tableElement, HTML.Tag.TR, rows);
        return Math.max(1, rows.size());
    }

    private int getTableColumnCount(Element tableElement) {
        List<Element> rows = new ArrayList<>();
        collectElements(tableElement, HTML.Tag.TR, rows);

        int maxCols = 1;
        for (Element row : rows) {
            int cols = 0;
            for (int i = 0; i < row.getElementCount(); i++) {
                Element cell = row.getElement(i);
                Object name = cell.getAttributes().getAttribute(StyleConstants.NameAttribute);
                if (HTML.Tag.TD.equals(name) || HTML.Tag.TH.equals(name)) {
                    cols++;
                }
            }
            maxCols = Math.max(maxCols, cols);
        }

        return maxCols;
    }

    private List<Element> getTableRows(Element tableElement) {
        List<Element> rows = new ArrayList<>();

        for (int i = 0; i < tableElement.getElementCount(); i++) {
            Element child = tableElement.getElement(i);
            Object name = child.getAttributes().getAttribute(StyleConstants.NameAttribute);

            if (HTML.Tag.TR.equals(name)) {
                rows.add(child);
            } else if ("tbody".equalsIgnoreCase(String.valueOf(name))
                    || "thead".equalsIgnoreCase(String.valueOf(name))
                    || "tfoot".equalsIgnoreCase(String.valueOf(name))) {
                for (int j = 0; j < child.getElementCount(); j++) {
                    Element nested = child.getElement(j);
                    Object nestedName = nested.getAttributes().getAttribute(StyleConstants.NameAttribute);
                    if (HTML.Tag.TR.equals(nestedName)) {
                        rows.add(nested);
                    }
                }
            }
        }

        return rows;
    }

    private List<Element> getRowCells(Element rowElement) {
        List<Element> cells = new ArrayList<>();

        for (int i = 0; i < rowElement.getElementCount(); i++) {
            Element child = rowElement.getElement(i);
            Object name = child.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (HTML.Tag.TD.equals(name) || HTML.Tag.TH.equals(name)) {
                cells.add(child);
            }
        }

        return cells;
    }

    private String getElementText(Element element) {
        try {
            int start = element.getStartOffset();
            int end = element.getEndOffset();
            int length = Math.max(0, end - start);
            String text = htmlDocument.getText(start, length);

            if (text == null) {
                return "";
            }

            text = text.replace("\n", " ").replace("\r", " ").trim();
            return text;
        } catch (BadLocationException ex) {
            throw new IllegalStateException("Could not read table cell text.", ex);
        }
    }

    private List<List<String>> extractTableData(Element tableElement) {
        List<List<String>> data = new ArrayList<>();
        List<Element> rows = getTableRows(tableElement);

        for (Element row : rows) {
            List<Element> cells = getRowCells(row);
            List<String> rowData = new ArrayList<>();

            for (Element cell : cells) {
                rowData.add(getElementText(cell));
            }

            data.add(rowData);
        }

        return data;
    }

    private String safeCellText(String text) {
        return text == null || text.isBlank() ? " " : escapeHtml(text);
    }

    private String buildTableHtml(int rows, int cols) {
        List<List<String>> data = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            List<String> row = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                row.add(" ");
            }
            data.add(row);
        }
        return buildTableHtml(data);
    }

    private String buildTableHtml(List<List<String>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"6\">");

        for (List<String> row : data) {
            sb.append("<tr>");
            for (String cell : row) {
                sb.append("<td>").append(safeCellText(cell)).append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table><p></p>");
        return sb.toString();
    }

    private List<List<String>> resizeTableData(List<List<String>> original, int targetRows, int targetCols) {
        List<List<String>> resized = new ArrayList<>();

        for (int r = 0; r < targetRows; r++) {
            List<String> newRow = new ArrayList<>();
            List<String> oldRow = r < original.size() ? original.get(r) : List.of();

            for (int c = 0; c < targetCols; c++) {
                String value = c < oldRow.size() ? oldRow.get(c) : " ";
                newRow.add(value == null || value.isBlank() ? " " : value);
            }

            resized.add(newRow);
        }

        return resized;
    }

    private void replaceTable(Element tableElement, String newTableHtml) {
        try {
            int start = tableElement.getStartOffset();
            int length = tableElement.getEndOffset() - start;

            htmlDocument.remove(start, length);
            htmlKit.insertHTML(htmlDocument, start, newTableHtml, 0, 0, HTML.Tag.TABLE);
        } catch (BadLocationException | IOException ex) {
            throw new IllegalStateException("Could not replace table.", ex);
        }
    }

    private Element requireCurrentTable() {
        if (sourceMode) {
            JOptionPane.showMessageDialog(
                    this,
                    "Table editing is only available in rendered mode.",
                    "Not available",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }

        Element table = findEnclosingElement(editor.getCaretPosition(), HTML.Tag.TABLE);
        if (table == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Place the caret inside a table first.",
                    "No table found",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }

        return table;
    }

    private void editTable() {
        if (sourceMode) {
            JOptionPane.showMessageDialog(
                    this,
                    "Edit table is only available in rendered mode.",
                    "Not available",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Element table = findEnclosingElement(editor.getCaretPosition(), HTML.Tag.TABLE);
        if (table == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Place the caret inside a table first.",
                    "No table found",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int currentRows = getTableRowCount(table);
        int currentCols = getTableColumnCount(table);

        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        JTextField rowsField = new JTextField(String.valueOf(currentRows));
        JTextField colsField = new JTextField(String.valueOf(currentCols));

        panel.add(new JLabel("Rows:"));
        panel.add(rowsField);
        panel.add(new JLabel("Columns:"));
        panel.add(colsField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Table",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int rows;
        int cols;
        try {
            rows = Math.max(1, Integer.parseInt(rowsField.getText().trim()));
            cols = Math.max(1, Integer.parseInt(colsField.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Rows and columns must be numeric.",
                    "Invalid table",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            int start = table.getStartOffset();
            int length = table.getEndOffset() - start;

            htmlDocument.remove(start, length);
            htmlKit.insertHTML(htmlDocument, start, buildTableHtml(rows, cols), 0, 0, HTML.Tag.TABLE);
        } catch (BadLocationException | IOException ex) {
            throw new IllegalStateException("Could not edit table.", ex);
        }
    }

    private void editTablePreserveContent() {
        Element table = requireCurrentTable();
        if (table == null) {
            return;
        }

        List<List<String>> currentData = extractTableData(table);
        int currentRows = Math.max(1, currentData.size());
        int currentCols = 1;
        for (List<String> row : currentData) {
            currentCols = Math.max(currentCols, row.size());
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        JTextField rowsField = new JTextField(String.valueOf(currentRows));
        JTextField colsField = new JTextField(String.valueOf(currentCols));

        panel.add(new JLabel("Rows:"));
        panel.add(rowsField);
        panel.add(new JLabel("Columns:"));
        panel.add(colsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Table", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int rows;
        int cols;
        try {
            rows = Math.max(1, Integer.parseInt(rowsField.getText().trim()));
            cols = Math.max(1, Integer.parseInt(colsField.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Rows and columns must be numeric.",
                    "Invalid table",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        List<List<String>> resized = resizeTableData(currentData, rows, cols);
        replaceTable(table, buildTableHtml(resized));
    }

    private void addTableRow() {
        Element table = requireCurrentTable();
        if (table == null) {
            return;
        }

        List<List<String>> data = extractTableData(table);

        int cols = 1;
        for (List<String> row : data) {
            cols = Math.max(cols, row.size());
        }

        List<String> newRow = new ArrayList<>();
        for (int c = 0; c < cols; c++) {
            newRow.add(" ");
        }

        data.add(newRow);
        replaceTable(table, buildTableHtml(data));
    }

    private void removeTableRow() {
        Element table = requireCurrentTable();
        if (table == null) {
            return;
        }

        List<List<String>> data = extractTableData(table);
        if (data.size() <= 1) {
            JOptionPane.showMessageDialog(
                    this,
                    "A table must have at least one row.",
                    "Cannot remove row",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        data.remove(data.size() - 1);
        replaceTable(table, buildTableHtml(data));
    }

    private void addTableColumn() {
        Element table = requireCurrentTable();
        if (table == null) {
            return;
        }

        List<List<String>> data = extractTableData(table);
        if (data.isEmpty()) {
            data.add(new ArrayList<>());
        }

        for (List<String> row : data) {
            row.add(" ");
        }

        replaceTable(table, buildTableHtml(data));
    }

    private void removeTableColumn() {
        Element table = requireCurrentTable();
        if (table == null) {
            return;
        }

        List<List<String>> data = extractTableData(table);

        int cols = 1;
        for (List<String> row : data) {
            cols = Math.max(cols, row.size());
        }

        if (cols <= 1) {
            JOptionPane.showMessageDialog(
                    this,
                    "A table must have at least one column.",
                    "Cannot remove column",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        for (List<String> row : data) {
            if (!row.isEmpty()) {
                row.remove(row.size() - 1);
            }
        }

        replaceTable(table, buildTableHtml(data));
    }

    private void removeTable() {
        Element table = requireCurrentTable();
        if (table == null) {
            return;
        }

        try {
            int start = table.getStartOffset();
            int length = table.getEndOffset() - start;
            htmlDocument.remove(start, length);
        } catch (BadLocationException ex) {
            throw new IllegalStateException("Could not remove table.", ex);
        }
    }

    public void setCaretPosition(int length) {
        editor.setCaretPosition(length);
    }

    public HTMLDocument getDocument() {
        return htmlDocument;
    }

    private void fixComboSize(JComboBox<?> combo) {
        Dimension size = combo.getPreferredSize();
        size.width += 10;
        combo.setPreferredSize(size);
        combo.setMaximumSize(size);
    }

    public void setEditorActive(boolean active) {
        editor.setEditable(active);
        editor.setEnabled(active);

        // Optional: visual feedback
        editor.setBackground(active ? Color.WHITE : new Color(240, 240, 240));
    }

    public static String defaultHtml() {
        return defaultHtml(null);
    }

    public static String defaultHtml(String header) {
        if (header == null) {
            return """
            <html>
              <body>
                <p>&nbsp;</p>
              </body>
            </html>
            """;
        } else {
            return """
            <html>
              <body>
                <h1>%s</h1>
                <hr>
                <p>&nbsp;</p>
              </body>
            </html>
            """.formatted(header);
        }
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
