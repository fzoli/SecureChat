package org.dyndns.fzoli.socket.process;

import org.dyndns.fzoli.socket.SecureSocketter;
import org.dyndns.fzoli.socket.handler.SecureHandler;

/**
 * Interfész az SSL socketen át adatfeldolgozást végző osztály írására.
 * @author zoli
 */
public interface SecureProcess extends Process, SecureSocketter {
    
    /**
     * @return Biztonságos kapcsolatfeldolgozó, ami létrehozta ezt az adatfeldolgozót.
     */
    @Override
    public SecureHandler getHandler();
    
    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     */
    public void closeProcesses();
    
}
