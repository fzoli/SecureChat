package org.dyndns.fzoli.ui.systemtray;

import java.awt.CheckboxMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

/**
 * Az alapértelmezett AWT PopupMenu adaptere.
 * @author zoli
 */
class AwtPopupMenu implements PopupMenu {

    private final java.awt.PopupMenu menu;
    private final java.awt.TrayIcon icon;
    
    private boolean visible = true;
    
    public AwtPopupMenu(java.awt.TrayIcon icon, java.awt.PopupMenu menu) {
        this.menu = menu;
        this.icon = icon;
    }

    @Override
    public void dispose() {
        setVisible(false);
    }

    @Override
    public void setVisible(boolean b) {
        if (visible ^ b) {
            if (b) icon.setPopupMenu(menu);
            else icon.setPopupMenu(null);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }
    
    @Override
    public void addSeparator() {
        menu.addSeparator();
    }

    @Override
    public MenuItem addMenuItem(String text, final Runnable r) {
        return addMenuItem(text, null, r);
    }
    
    @Override
    public MenuItem addMenuItem(String text, BufferedImage img, final Runnable r) {
        java.awt.MenuItem item = new java.awt.MenuItem(text);
        if (r != null) {
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    r.run();
                }

            });
        }
        menu.add(item);
        return new AwtMenuItem(item);
    }

    @Override
    public void addCheckboxMenuItem(String text, boolean checked, final Runnable r) {
        final java.awt.CheckboxMenuItem item = new CheckboxMenuItem(text, checked);
        if (r != null) {
            item.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    r.run();
                }

            });
        }
        menu.add(item);
    }
    
}
