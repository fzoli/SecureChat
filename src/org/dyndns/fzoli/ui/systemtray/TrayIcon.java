package org.dyndns.fzoli.ui.systemtray;

import java.awt.image.BufferedImage;

/**
 * Rendszerikon.
 * @author zoli
 */
public interface TrayIcon extends Visibility {
    
    /**
     * Buborékablak ikonjának a típusa.
     */
    public enum IconType {
        INFO,
        WARNING,
        ERROR
    }
    
    /**
     * Az ikonhoz tartozó SystemTray.
     */
    public SystemTray getSystemTray();
    
    /**
     * Létrehozza a menüt, ami egyből felkerül a rendszerikonra.
     */
    public PopupMenu createPopupMenu();
    
    /**
     * Eltávolítja a menüt, ha létezik.
     */
    public void removePopupMenu();
    
    /**
     * Ha az ikonra duplán kattintanak, a paraméterben átadott objektum run metódusa fut le.
     * @param r callback
     */
    public void setOnClickListener(final Runnable r);
    
    /**
     * Buborékablakot jelenít meg.
     * @param title rövid üzenet
     * @param msg hosszú üzenet
     * @param icon a buborékablak ikonjának típusa
     */
    public void displayMessage(String title, String msg, IconType icon);
    
    /**
     * Buborékablakot jelenít meg.
     * @param title rövid üzenet
     * @param msg hosszú üzenet
     * @param icon a buborékablak ikonjának típusa
     * @param onClick callback, ami akkor hívódik meg, ha a buborékablakra kattintanak és ha nem null
     */
    public void displayMessage(String title, String msg, IconType icon, final Runnable onClick);
    
    /**
     * Az ikon képének beállítása.
     */
    public void setImage(BufferedImage img);
    
    /**
     * Tool tip üzenet beállítása.
     */
    public void setToolTip(String text);
    
}
