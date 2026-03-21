package com.pjs;

import com.pjs.ui.TreeNotesApp;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new TreeNotesApp().setVisible(true);
        });
    }
}
