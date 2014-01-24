package org.dyndns.fzoli.socket.process;

import org.dyndns.fzoli.socket.Socketter;
import org.dyndns.fzoli.socket.handler.Handler;

/**
 * Interfész a socketen át adatfeldolgozást végző osztály írására.
 * @author zoli
 */
public interface Process extends Socketter {
    
    /**
     * @return Kapcsolatfeldolgozó, ami létrehozta ezt az adatfeldolgozót.
     */
    public Handler getHandler();
    
    /**
     * A Socket bezárása és a Process törlése a nyilvántartásból.
     */
    public void dispose();
    
    /**
     * Ez a metódus indítja el az adatfeldolgozást.
     */
    @Override
    public void run();
    
}
