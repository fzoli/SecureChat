package org.dyndns.fzoli.chat;

/**
 * Kapcsolat- és eszközazonosítók és segédváltozók.
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
     * A kliens a kapcsolat bontása után ennyi időt vár míg újra nem kapcsolódik.
     * Várakozási idő: 1 másodperc
     * Ajánlott érték: 500
     */
    int RECONN_DELAY = 1000;
    
    /**
     * A kliensnek a szerverrel való kapcsolat kialakítására (host feloldás stb.) szánt idő.
     * Időkorlát: 1 perc
     * Ajánlott érték: 10 másodperc
     */
    int CONN_TIMEOUT = 60000;
    
    /**
     * A kapcsolat kialakítása után a kapcsolatazonosító cserékre szánt idő.
     * Időkorlát: 15 másodperc
     * Ajánlott érték: 3 másodperc
     */
    int READ_TIMEOUT = 15000;
    
    /**
     * Az első időtúllépés a kapcsolatban.
     * Időkorlát: 5 másodperc
     * Ajánlott érték: 1 másodperc
     */
    int DC_TIMEOUT1 = 5000;
    
    /**
     * A második időtúllépés.
     * A szerver ekkor az összes kapcsolatot bezárja.
     * Időkorlát: 20 másodperc
     * Ajánlott érték: 10 másodperc
     */
    int DC_TIMEOUT2 = 20000;
    
    /**
     * Két üzenetváltás között eltelt idő, amit mindkét oldalon ki kell várni a pontos eredmény érdekében.
     * Várakozási idő: fél másodperc
     * Ajánlott érték: 250 ezredmásodperc
     */
    int DC_DELAY = 500;
    
}
