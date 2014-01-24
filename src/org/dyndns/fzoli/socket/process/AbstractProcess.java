package org.dyndns.fzoli.socket.process;

import java.net.Socket;
import org.dyndns.fzoli.socket.handler.Handler;

/**
 * A socketen át adatfeldolgozást végző osztály alapja.
 * @author zoli
 */
public abstract class AbstractProcess implements Process {
    
    private final Handler handler;
    
    /**
     * Adatfeldolgozó inicializálása.
     * @param handler Kapcsolatfeldolgozó, ami létrehozza ezt az adatfeldolgozót.
     * @throws NullPointerException ha handler null
     */
    public AbstractProcess(Handler handler) {
        this.handler = handler;
    }
    
    /**
     * A Socket bezárása és a Process törlése a nyilvántartásból.
     */
    @Override
    public void dispose() {
        try {
            getSocket().close();
        }
        catch (Exception ex) {
            ;
        }
        getHandler().getProcesses().remove(this);
    }
    
    /**
     * @return Kapcsolatfeldolgozó, ami létrehozta ezt az adatfeldolgozót.
     */
    @Override
    public Handler getHandler() {
        return handler;
    }
    
    /**
     * @return Socket, amin keresztül folyik a kommunikáció.
     */
    @Override
    public Socket getSocket() {
        return getHandler().getSocket();
    }

    /**
     * @return Kapcsolatazonosító, ami segítségével megtudható a kapcsolatteremtés célja.
     */
    @Override
    public Integer getConnectionId() {
        return getHandler().getConnectionId();
    }

    /**
     * @return Eszközazonosító, ami segítségével megtudható a kliens típusa.
     */
    @Override
    public Integer getDeviceId() {
        return getHandler().getDeviceId();
    }
    
    /**
     * Szünetet tart a szál.
     * Az utód osztályok használhatják kivételkezelés nélküli kódrövidítéshez.
     * @param delay ezredmásodpercben megadott idő
     */
    protected static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        }
        catch (Exception ex) {
            ;
        }
    }
    
}
