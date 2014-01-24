package org.dyndns.fzoli.ui.systemtray;

/**
 * Rendszerikon támogatás.
 * @author zoli
 */
public interface SystemTray {
    
    /**
     * Megadja, támogatott-e a rendszerikon.
     */
    public boolean isSupported();
    
    /**
     * Ha támogatott a rendszerikon, létrehozz egyet és megjeleníti.
     */
    public TrayIcon addTrayIcon();
    
    /**
     * Az összes rendszerikon megszüntetése.
     */
    public void dispose();
    
}
