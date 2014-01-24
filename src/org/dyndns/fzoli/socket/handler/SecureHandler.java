package org.dyndns.fzoli.socket.handler;

import java.util.List;
import org.dyndns.fzoli.socket.SecureSocketter;
import org.dyndns.fzoli.socket.process.SecureProcess;

/**
 * Biztonságos socketfeldolgozó implementálásához kliens és szerver oldalra.
 * A socket feldolgozása előtt az adatok alapján kiválasztja, melyik feldolgozót kell indítani.
 * @author zoli
 */
public interface SecureHandler extends Handler, SecureSocketter {
    
    /**
     * Azokat a biztonságos adatfeldolgozókat adja vissza, melyek még dolgoznak.
     */
    public List<SecureProcess> getSecureProcesses();
    
    /**
     * Igaz, ha ugyan azzal a tanúsítvánnyal és azonosítókkal rendelkezik a megadott feldolgozó.
     * @param handler a másik feldolgozó
     */
    public boolean isCertEqual(SecureHandler handler);
    
    /**
     * Igaz, ha ugyan azzal a tanúsítvánnyal és azonosítókkal rendelkezik a feldolgozó, mint a paraméterben megadottak.
     * @param remoteName tanúsítvány common name
     * @param deviceId eszközazonosító
     * @param connectionId kapcsolatazonosító
     */
    public boolean isCertEqual(String remoteName, int deviceId, int connectionId);
    
    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     */
    public void closeProcesses();
    
    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     */
    public void closeProcesses(SecureProcess asker);
    
}
