package com.pjs.ui.htmleditor.html;

import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.io.StringWriter;

public class HTMLBody {

    private final HTMLDocument document;

    private final CustomHTMLEditorKit editorKit;

    public HTMLBody(HTMLDocument document, CustomHTMLEditorKit editorKit){
        this.document = document;
        this.editorKit = editorKit;
    }

    public String asString(){

        try {
            Element rootElement = document.getDefaultRootElement();
            Element element = findElementByTagName(rootElement, "body");

            if (element != null) {
                StringWriter stringWriter = new StringWriter();
                editorKit.write(stringWriter, element.getDocument(), element.getStartOffset(), element.getEndOffset() - element.getStartOffset());
                return editorKit.sanitizeHTML(stringWriter.toString());
            }

            return null;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private Element findElementByTagName(Element element, String tagName) {
        if (element.getName() != null && element.getName().equals(tagName)) {
            return element;
        }

        for (int i = 0; i < element.getElementCount(); i++) {
            Element child = element.getElement(i);
            Element found = findElementByTagName(child, tagName);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

}
