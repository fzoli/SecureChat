package org.dyndns.fzoli.ui.systemtray;

/**
 * AWT alapú menüelem.
 * @author zoli
 */
class AwtMenuItem implements MenuItem {

    private final java.awt.MenuItem ITEM;
    
    public AwtMenuItem(java.awt.MenuItem item) {
        ITEM = item;
    }

    @Override
    public boolean isEnabled() {
        return ITEM.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        ITEM.setEnabled(enabled);
    }
    
}
