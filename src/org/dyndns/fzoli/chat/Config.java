package org.dyndns.fzoli.chat;

import java.io.File;

/**
 * Konfiguráció SSL Socket létrehozására.
 * @author zoli
 */
public interface Config {
    
    /**
     * Megadja, hogy a konfiguráció helyes-e.
     * @return true esetén használható a konfiguráció
     */
    public boolean isCorrect();
    
    /**
     * Megadja, hogy mely port legyen használva a kapcsolat kialakítására.
     * @return null, ha nincs beállítva
     */
    public Integer getPort();
    
    /**
     * A tanúsítvány jelszavát adja vissza.
     * @return null vagy üres string, ha nem kell jelszó
     */
    public char[] getPassword();
    
    /**
     * A tanúsítvány kiállító kulcsfájlt adja vissza.
     * @return null, ha a fájl nem létezik, vagy nincs beállítva
     */
    public File getCAFile();
    
    /**
     * A tanúsítvány publikus kulcsfájlt adja vissza.
     * @return null, ha a fájl nem létezik, vagy nincs beállítva
     */
    public File getCertFile();
    
    /**
     * A tanúsítvány privát kulcsfájlt adja vissza.
     * @return null, ha a fájl nem létezik, vagy nincs beállítva
     */
    public File getKeyFile();
    
}
