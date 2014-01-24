package org.dyndns.fzoli.socket.process.impl;

import java.io.InputStream;
import java.io.OutputStream;
import org.dyndns.fzoli.socket.handler.SecureHandler;

/**
 * Az osztály a klienssel kiépített kapcsolatot arra használja, hogy
 * másodpercenként ellenőrizze, hogy megszakadt-e a kapcsolat.
 * @author zoli
 */
public class ServerDisconnectProcess extends DisconnectProcess {
    
    /**
     * Szerver oldalra időtúllépés detektáló.
     * @param handler Biztonságos kapcsolatfeldolgozó, ami létrehozza ezt az adatfeldolgozót.
     * @param timeout1 az első időtúllépés ideje ezredmásodpercben (nem végzetes korlát)
     * @param timeout2 a második időtúllépés ideje ezredmásodpercben (végzetes korlát)
     * @param waiting két ellenőrzés között eltelt idő
     * @throws NullPointerException ha handler null
     */
    public ServerDisconnectProcess(SecureHandler handler, int timeout1, int timeout2, int waiting) {
        super(handler, timeout1, timeout2, waiting);
    }

    /**
     * Kommunikáció a két fél között.
     * Mindkét fél olvas és ír is a streamekre, de fordított sorrendben.
     * A szerver előbb ír, aztán olvas, ebből adódik, hogy a kliens előbb olvas, aztán ír.
     * @param in bemenet
     * @param out kimenet
     * @throws Exception ha az olvasás vagy írás közben bármi hiba történik
     */
    @Override
    protected final void loop(InputStream in, OutputStream out) throws Exception {
        write(out); // írás
        read(in); // olvasás
    }
    
}
