package com.pjs.ui;

import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Swing HTML editor backed by JCEF + Quill.
 *
 * Public API intentionally matches the old editor style:
 * - getText()
 * - setText(String)
 * - dispose()
 *
 * Notes:
 * - This class uses a local temporary HTML file.
 * - Quill is loaded from CDN. For offline use, bundle Quill locally.
 * - getText() blocks briefly while asking the browser for current HTML.
 */
public class JcefHTMLEditor extends JPanel {

    private static final String HTML = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Editor</title>

              <link href="https://cdn.jsdelivr.net/npm/quill@2/dist/quill.snow.css" rel="stylesheet"/>

              <style>
                html, body {
                  margin: 0;
                  padding: 0;
                  height: 100%;
                  overflow: hidden;
                  font-family: Arial, sans-serif;
                  background: #ffffff;
                }

                .wrapper {
                  height: 100vh;
                  display: flex;
                  flex-direction: column;
                }

                #editor {
                  flex: 1;
                  min-height: 0;
                }

                .ql-toolbar.ql-snow {
                  border-left: 0;
                  border-right: 0;
                  border-top: 0;
                }

                .ql-container.ql-snow {
                  border-left: 0;
                  border-right: 0;
                  border-bottom: 0;
                  font-size: 14px;
                }

                .ql-editor {
                  min-height: calc(100vh - 42px);
                  box-sizing: border-box;
                }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div id="editor"></div>
              </div>

              <script src="https://cdn.jsdelivr.net/npm/quill@2/dist/quill.js"></script>
              <script>
                let quill;

                function initEditor() {
                  quill = new Quill('#editor', {
                    theme: 'snow',
                    modules: {
                      toolbar: [
                        [{ header: [1, 2, 3, false] }],
                        ['bold', 'italic', 'underline', 'strike'],
                        [{ color: [] }, { background: [] }],
                        [{ list: 'ordered' }, { list: 'bullet' }],
                        [{ indent: '-1' }, { indent: '+1' }],
                        [{ align: [] }],
                        ['blockquote', 'code-block'],
                        ['link', 'image'],
                        ['clean']
                      ]
                    }
                  });
                }

                function editorGetHtml() {
                  return quill.root.innerHTML;
                }

                function editorSetHtml(html) {
                  quill.root.innerHTML = html || '';
                }

                function editorFocus() {
                  quill.focus();
                }

                function sendHtmlToJava() {
                  if (!window.cefQuery) {
                    return;
                  }
                  window.cefQuery({
                    request: 'GET_HTML:' + editorGetHtml(),
                    onSuccess: function() {},
                    onFailure: function(code, message) {
                      console.error('cefQuery failed', code, message);
                    }
                  });
                }

                window.addEventListener('load', function() {
                  initEditor();

                  if (window.cefQuery) {
                    window.cefQuery({
                      request: 'EDITOR_READY',
                      onSuccess: function() {},
                      onFailure: function() {}
                    });
                  }
                });
              </script>
            </body>
            </html>
            """;

    private CefApp cefApp;
    private CefClient client;
    private CefBrowser browser;
    private CefMessageRouter messageRouter;

    private volatile boolean ready;
    private volatile String pendingHtml = "";
    private final Object initLock = new Object();

    public JcefHTMLEditor() {
        super(new BorderLayout());
        initBrowser();
    }

    private void initBrowser() {
        try {
            CefAppBuilder builder = new CefAppBuilder();
            builder.setInstallDir(new File("jcef-bundle"));
            builder.getCefSettings().windowless_rendering_enabled = false;

            cefApp = builder.build();
            client = cefApp.createClient();

            messageRouter = CefMessageRouter.create();
            messageRouter.addHandler(new CefMessageRouterHandlerAdapter() {
                @Override
                public boolean onQuery(CefBrowser browser,
                                       CefFrame frame,
                                       long queryId,
                                       String request,
                                       boolean persistent,
                                       CefQueryCallback callback) {

                    if ("EDITOR_READY".equals(request)) {
                        ready = true;

                        String initialHtml = pendingHtml;
                        if (initialHtml != null && !initialHtml.isEmpty()) {
                            SwingUtilities.invokeLater(() ->
                                    runJs("editorSetHtml(" + toJsString(initialHtml) + ");"));
                        }

                        callback.success("OK");
                        return true;
                    }

                    if (request != null && request.startsWith("GET_HTML:")) {
                        pendingHtml = request.substring("GET_HTML:".length());
                        callback.success("OK");
                        return true;
                    }

                    return false;
                }
            }, true);
            client.addMessageRouter(messageRouter);

            Path htmlFile = writeHtmlToTempFile();
            browser = client.createBrowser(htmlFile.toUri().toString(), false, false);

            add((Component) browser.getUIComponent(), BorderLayout.CENTER);

        } catch (Exception e) {
            removeAll();
            JTextArea errorArea = new JTextArea();
            errorArea.setEditable(false);
            errorArea.setText("Failed to initialize JCEF editor.\n\n" + e);
            add(new JScrollPane(errorArea), BorderLayout.CENTER);
            revalidate();
            repaint();
            throw new RuntimeException("Failed to initialize JcefHTMLEditor", e);
        }
    }

    private Path writeHtmlToTempFile() throws Exception {
        Path file = Files.createTempFile("jcef-editor-", ".html");
        Files.writeString(file, HTML, StandardCharsets.UTF_8);
        file.toFile().deleteOnExit();
        return file;
    }

    public void setText(String html) {
        String safeHtml = html == null ? "" : html;
        pendingHtml = safeHtml;

        if (ready) {
            runJs("editorSetHtml(" + toJsString(safeHtml) + ");");
        }
    }

    public String getText() {
        if (browser == null || !ready) {
            return pendingHtml == null ? "" : pendingHtml;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>(pendingHtml == null ? "" : pendingHtml);

        String callbackId = "javaHtmlCallback_" + System.nanoTime();

        synchronized (initLock) {
            runJs("""
                    (function() {
                        var html = editorGetHtml();
                        window.cefQuery({
                            request: 'GET_HTML:' + html,
                            onSuccess: function() {},
                            onFailure: function() {}
                        });
                    })();
                    """);
        }

        // Give JCEF a short moment to deliver the updated HTML through the router.
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(150);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            result.set(pendingHtml == null ? "" : pendingHtml);
            latch.countDown();
        }, callbackId).start();

        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return pendingHtml == null ? "" : pendingHtml;
        }

        return result.get();
    }

    private void runJs(String js) {
        if (browser == null) {
            return;
        }
        browser.getMainFrame().executeJavaScript(js, browser.getURL(), 0);
    }

    private static String toJsString(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("</", "<\\/");
        return "'" + escaped + "'";
    }

    public void dispose() {
        try {
            if (browser != null) {
                browser.close(true);
            }
        } catch (Exception ignored) {
        }

        try {
            if (client != null && messageRouter != null) {
                client.removeMessageRouter(messageRouter);
            }
        } catch (Exception ignored) {
        }

        try {
            if (cefApp != null) {
                cefApp.dispose();
            }
        } catch (Exception ignored) {
        }

        browser = null;
        client = null;
        cefApp = null;
        messageRouter = null;
        ready = false;
    }
}