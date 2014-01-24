package org.dyndns.fzoli.ui.systemtray;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Az alapértelmezett AWT SystemTray adaptere.
 * @author zoli
 */
class AwtSystemTray implements SystemTray {

    private final java.awt.SystemTray tray;
    
    private final List<java.awt.TrayIcon> icons = new ArrayList<java.awt.TrayIcon>();
    
    private boolean disposed = false;
    
    public AwtSystemTray() {
        if (java.awt.SystemTray.isSupported()) {
             tray = java.awt.SystemTray.getSystemTray();
        }
        else {
            tray = null;
        }
    }

    @Override
    public boolean isSupported() {
        return tray != null;
    }

    @Override
    public TrayIcon addTrayIcon() {
        if (!isSupported() || disposed) return null;
        java.awt.TrayIcon icon = new java.awt.TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        try {
            tray.add(icon);
        }
        catch (AWTException ex) {
            icon = null;
            return null;
        }
        synchronized(icons) {
            icons.add(icon);
        }
        return new AwtTrayIcon(this, tray, icon);
    }

    @Override
    public void dispose() {
        synchronized(icons) {
            disposed = true;
            for (java.awt.TrayIcon icon : icons) {
                tray.remove(icon);
            }
        }
    }
    
}
