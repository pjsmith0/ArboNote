package com.pjs.ui.htmleditor.palette;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class WebPalette implements Palette {

    final List<PaletteItem> items;

    public WebPalette() {
        this.items = initColors();
    }

    List<PaletteItem> initColors() {

        return Arrays.asList(
                new PaletteItem(new Color(0xFF, 0xFF, 0xFF), "white"),
                new PaletteItem(new Color(0xC0, 0xC0, 0xC0), "silver"),
                new PaletteItem(new Color(0x80, 0x80, 0x80), "gray"),
                new PaletteItem(new Color(0x00, 0x00, 0x00), "black"),
                new PaletteItem(new Color(0xFF, 0x00, 0x00), "red"),
                new PaletteItem(new Color(0x80, 0x00, 0x00), "maroon"),
                new PaletteItem(new Color(0xFF, 0xFF, 0x00), "yellow"),
                new PaletteItem(new Color(0x80, 0x80, 0x00), "olive"),
                new PaletteItem(new Color(0x00, 0xFF, 0x00), "lime"),
                new PaletteItem(new Color(0x00, 0x80, 0x00), "green"),
                new PaletteItem(new Color(0x00, 0xFF, 0xFF), "aqua"),
                new PaletteItem(new Color(0x00, 0x80, 0x80), "teal"),
                new PaletteItem(new Color(0x00, 0x00, 0xFF), "blue"),
                new PaletteItem(new Color(0x00, 0x00, 0x80), "navy"),
                new PaletteItem(new Color(0xFF, 0x00, 0xFF), "fuchsia"),
                new PaletteItem(new Color(0x80, 0x00, 0x80), "purple")
        );
    }

    @Override
    public PaletteItem getDefault() {
        return new PaletteItem(new Color(0x00, 0x00, 0x00), "black");
    }

    @Override
    public List<PaletteItem> getItems() {
        return this.items;
    }
}
