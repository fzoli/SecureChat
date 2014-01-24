package org.dyndns.fzoli.chat;

/**
 * Kapcsolat- és eszközazonosítók.
 * @author zoli
 */
public interface ConnectionKeys {
    
    /**
     * Chat-kliensek, több programnyelvhez.
     */
    int KEY_DEV_CLIENT = 0;
    
    /**
     * A kapcsolat megszakadását detektáló szál kapcsolatazonosítója.
     */
    int KEY_CONN_DISCONNECT = 0;
    
    /**
     * Üzenetküldő és fogadó szál kapcsolatazonosítója.
     */
    int KEY_CONN_MESSAGE = 1;
    
    /**
     * Az első időtúllépés a kapcsolatban.
     * Időkorlát: 1 másodperc
     */
    int DC_TIMEOUT1 = 1000;
    
    /**
     * A második időtúllépés.
     * A szerver ekkor az összes kapcsolatot bezárja.
     * Időkorlát: 10 másodperc
     */
    int DC_TIMEOUT2 = 10000;
    
    /**
     * Két üzenetváltás között eltelt idő, amit mindkét oldalon ki kell várni a pontos eredmény érdekében.
     * Várakozási idő: 250 ezredmásodperc
     */
    int DC_DELAY = 250;
    
}
