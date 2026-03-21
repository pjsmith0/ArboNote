package com.pjs.ui.htmleditor.component;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class LinkDialog extends JDialog {
    private static final long serialVersionUID = 42L;
    private static final int V_SPACE = 5;

    private final AtomicBoolean accepted = new AtomicBoolean();
    private final JTextField url;

    public LinkDialog(final Frame parent) {
        super(parent, "Link", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel linkHeaderPanel = createHeaderPanel();
        JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.LINE_AXIS));
        url = new JTextField(20);

        linkPanel.add(url);
        linkHeaderPanel.add(linkPanel);
        panel.add(linkHeaderPanel);
        panel.add(Box.createRigidArea(new Dimension(5, V_SPACE)));

        JPanel buttons = new ButtonPanel();

        panel.add(buttons);
        JPanel padded = new JPanel();
        padded.add(panel);
        getContentPane().add(padded);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void setLink(final String url) {
        this.url.setText(url);
    }

    public String getLink() {
        return this.url.getText();
    }

    public boolean isAccepted() {
        return this.accepted.get();
    }

    private JPanel createHeaderPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        return panel;
    }

    private class ButtonPanel extends JPanel {

        public ButtonPanel(){
            super();
            initComponents();
        }

        private void initComponents() {
            setLayout(new FlowLayout(FlowLayout.CENTER));

            JButton addButton = new JButton(UIManager.getString("OptionPane.okButtonText"));
            addButton.addActionListener(e -> {
                accepted.set(false);

                ValidatedURL validatedURL = new ValidatedURL(url.getText());

                if (!validatedURL.isValid()) {
                    url.putClientProperty("JComponent.outline", "error");
                    url.requestFocusInWindow();
                    return;
                }
                accepted.set(true);
                dispose();
            });

            add(addButton);

            JButton cancelButton = new JButton(UIManager.getString("OptionPane.cancelButtonText"));
            cancelButton.addActionListener(e -> dispose());
            add(cancelButton);

            LinkDialog.this.getRootPane().setDefaultButton(addButton); // Default on INTRO
        }
    }

}