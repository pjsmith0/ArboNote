package com.pjs.ui.htmleditor.html;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;
import java.io.IOException;
import java.io.Writer;

public class CustomHTMLWriter extends HTMLWriter {
    private boolean inBody = false;
    private boolean inParagraph = false;
    private int paragraphText = 0;

    public CustomHTMLWriter(final Writer w, final HTMLDocument doc, final int pos, final int len) {
        super(w, doc, pos, len);
    }

    @Override
    protected boolean synthesizedElement(Element elem) {
        return false;
    }

    @Override
    protected void startTag(final Element elem) throws IOException, BadLocationException {
        if (matchNameAttribute(elem.getAttributes(), HTML.Tag.P)) {
            inParagraph = true;
            paragraphText = 0;
        } else if (matchNameAttribute(elem.getAttributes(), HTML.Tag.DIV)) {
            return;
        } else if (matchNameAttribute(elem.getAttributes(), HTML.Tag.IMPLIED)) {
            if (!inBody) {
                return;
            }
            indent();
            write('<');
            write("div");
            writeAttributes(elem.getAttributes());
            write('>');
            writeLineSeparator();
            return;
        } else if (matchNameAttribute(elem.getAttributes(), HTML.Tag.BODY)) {
            inBody = true;
        }
        super.startTag(elem);
    }

    @Override
    protected void text(final Element elem) throws BadLocationException, IOException {
        if (inParagraph) {
            int start = Math.max(getStartOffset(), elem.getStartOffset());
            int end = Math.min(getEndOffset(), elem.getEndOffset());
            if (start < end) {
                final String text = getDocument().getText(start, end - start);
                final boolean isBlank = text.chars().allMatch(Character::isWhitespace);
                if (!isBlank) {
                    paragraphText++;
                }
            }
        }
        super.text(elem);
    }

    @Override
    protected void endTag(final Element elem) throws IOException {
        if (matchNameAttribute(elem.getAttributes(), HTML.Tag.P)) {
            inParagraph = false;
            if (paragraphText == 0) {
                indent();
                write("&nbsp;");
            }
        } else if (matchNameAttribute(elem.getAttributes(), HTML.Tag.DIV)) {
            return;
        } else if (matchNameAttribute(elem.getAttributes(), HTML.Tag.IMPLIED)) {
            if (!inBody) {
                return;
            }
            indent();
            write("</div>");
            writeLineSeparator();
            return;
        } else if (matchNameAttribute(elem.getAttributes(), HTML.Tag.BODY)) {
            inBody = false;
        }
        super.endTag(elem);
    }
}
