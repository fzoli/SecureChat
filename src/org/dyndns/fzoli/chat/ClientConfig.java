package org.dyndns.fzoli.chat;

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
    
    /**
     * A kapcsolat kialakítására szánt max idő.
     */
    public int getConnTimeout();
    
}
