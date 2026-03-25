package com.pjs.ui.htmleditor.html;

import org.apache.commons.io.IOUtils;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomHTMLEditorKit extends HTMLEditorKit {

    private static final PolicyFactory HTML_POLICY_DEFINITION_BASIC = new HtmlPolicyBuilder()
            .allowUrlProtocols("http", "https", "data", "mailto") //
            .allowAttributes("id").globally() //
            .allowAttributes("class").globally() //
            .allowAttributes("align").onElements("p", "hr")
            .allowStyling(CssSchema.DEFAULT) //
            .allowAttributes("src").onElements("img") //
            .allowAttributes("alt").onElements("img") //
            .allowAttributes("height", "width").onElements("img") //
            .allowAttributes("href").onElements("a") //
            .allowAttributes("color").onElements("font") //
            .allowAttributes("size", "width").onElements("hr") //
            .allowElements("html", "head", "body", "style", "p", "div", "span", "br", "b", "i", "u", "strike", "font", "a", "img", "ul", "ol", "li", "hr") //
            .toFactory();

    private static final long serialVersionUID = 42L;
    private final AtomicBoolean init = new AtomicBoolean();

    private final transient ViewFactory factory = new HTMLFactory();

    @Override
    public ViewFactory getViewFactory() {
        return factory;
    }

    @Override
    public void read(final InputStream in, final Document doc, final int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final OutputStream out, final Document doc, final int pos, final int len) {
        throw new UnsupportedOperationException();
    }

    public String sanitizeHTML(String input) {
        return HTML_POLICY_DEFINITION_BASIC.sanitize(input);
    }

    @Override
    public void read(final Reader in, final Document doc, final int pos) //
            throws IOException, BadLocationException {
        if (init.getAndSet(true)) {
            final String text = sanitizeHTML(IOUtils.toString(in));
            super.read(new StringReader(text), doc, pos);
        } else {
            super.read(in, doc, pos);
        }
    }

    @Override
    public void write(final Writer out, final Document doc, final int pos, final int len) //
            throws IOException, BadLocationException {

        CustomHTMLWriter w = new CustomHTMLWriter(out, (HTMLDocument) doc, pos, len);
        w.write();
        out.flush();
    }

}
