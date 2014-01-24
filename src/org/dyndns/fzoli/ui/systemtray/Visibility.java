package org.dyndns.fzoli.ui.systemtray;

/**
 * Azok az osztályok, melyek láthatósága állítható,
 * implementálják ezt az interfészt.
 * @author zoli
 */
interface Visibility {
    
    /**
     * Láthatóság állítása.
     * @param b true esetén látható, egyébként nem látható
     */
    public void setVisible(boolean b);
    
    /**
     * Megadja, látható-e a komponens.
     */
    public boolean isVisible();
    
}
