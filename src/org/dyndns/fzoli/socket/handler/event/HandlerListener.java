package org.dyndns.fzoli.socket.handler.event;

import org.dyndns.fzoli.socket.handler.Handler;

/**
 * Handler eseménykezelő.
 * @author zoli
 */
public interface HandlerListener {
    
    /**
     * Ez a metódus hívódik meg, ha a Handler kiválasztotta, melyik Process legyen használva.
     */
    public void onProcessSelected(Handler handler);
    
}
