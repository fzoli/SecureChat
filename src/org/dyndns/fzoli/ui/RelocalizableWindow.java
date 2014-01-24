package org.dyndns.fzoli.ui;

/**
 * Újralokalizálható ablak.
 * @author zoli
 */
public interface RelocalizableWindow {
    
    /**
     * A felület feliratait újra beállítja.
     * Ha a nyelvet megváltoztatja a felhasználó, ez a metódus hívódik meg.
     */
    public void relocalize();
    
}
