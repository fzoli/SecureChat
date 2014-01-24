package org.dyndns.fzoli.socket.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DeviceHandler kivétel.
 * Akkor keletkezik, ha az {@link AbstractHandler#createDeviceHandler(InputStream, OutputStream)} null referenciával tér vissza.
 */
class NullHandlerException extends NullPointerException {

    public NullHandlerException() {
        super("DeviceHandler is null");
    }
    
}

/**
 * Eszközkezelő I/O segédosztály.
 * Előfordulhat, hogy a szerver nem csak Java nyelven lesz megírva.
 * Nem Java nyelveken nincs Object(I/O)Stream, ezért az írás és olvasás eltérhet.
 * Ez az osztály definiálja, hogyan kell a {@link Handler} inicializálásakor végbemenő olvasást és írást végrehajtani.
 * @author zoli
 */
public abstract class DeviceHandler {

    /**
     * A bejövő folyam, amiről olvas.
     */
    protected final InputStream in;
    
    /**
     * A kimenő folyam, amire ír.
     */
    protected final OutputStream out;
    
    /**
     * Konstruktor.
     * @param in a bejövő folyam, amiről olvas
     * @param out a kimenő folyam, amire ír
     */
    public DeviceHandler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    /**
     * Elküldi a státuszüzenetet.
     * @param s az üzenet
     * @throws IOException ha az írás nem sikerült
     */
    public abstract void sendStatus(String s) throws IOException;

    /**
     * Fogadja a státuszüzenetet.
     * @throws IOException ha az olvasás nem sikerült
     * @return a kapott üzenet
     */
    public abstract String readStatus() throws IOException;
    
}
