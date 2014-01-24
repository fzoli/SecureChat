package org.dyndns.fzoli.ui.systemtray;

/**
 * Menüelem.
 * @author zoli
 */
public interface MenuItem {
    
    /**
     * Megadja, hogy a menüelem aktív-e.
     * @return true, ha aktív; egyébként false
     */
    public boolean isEnabled();
    
    /**
     * Beállítja a menüelemet aktívra vagy inaktívra.
     * @param enabled true esetén aktív, egyébként inaktív
     */
    public void setEnabled(boolean enabled);
    
}
