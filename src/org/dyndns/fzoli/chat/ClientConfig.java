package org.dyndns.fzoli.chat;

import org.dyndns.fzoli.chat.Config;

/**
 * Konfiguráció SSL Socket létrehozására kliens oldalon.
 * @author zoli
 */
public interface ClientConfig extends Config {
    
    /**
     * A szerver címét adja meg.
     * @return null, ha nincs beállítva
     */
    public String getAddress();
    
}
