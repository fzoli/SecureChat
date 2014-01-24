package org.dyndns.fzoli.socket.process;

import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.socket.handler.SecureHandler;

/**
 * A socketen át biztonságos adatfeldolgozást végző osztály alapja.
 * @author zoli
 */
public abstract class AbstractSecureProcess extends AbstractProcess implements SecureProcess {

    /**
     * Biztonságos adatfeldolgozó inicializálása.
     * @param handler Biztonságos kapcsolatfeldolgozó, ami létrehozza ezt az adatfeldolgozót.
     * @throws NullPointerException ha handler null
     */
    public AbstractSecureProcess(SecureHandler handler) {
        super(handler);
    }

    /**
     * @return Biztonságos kapcsolatfeldolgozó, ami létrehozta ezt az adatfeldolgozót.
     */
    @Override
    public SecureHandler getHandler() {
        return (SecureHandler) super.getHandler();
    }

    /**
     * @return SSLSocket, amin keresztül folyik a kommunikáció.
     */
    @Override
    public SSLSocket getSocket() {
        return getHandler().getSocket();
    }

    /**
     * A titkosított kommunikáció ezen oldalán álló gép tanúsítványának CN mezőjét adja vissza.
     */
    @Override
    public String getLocalCommonName() {
        return getHandler().getLocalCommonName();
    }

    /**
     * A titkosított kommunikáció másik oldalán álló gép tanúsítványának CN mezőjét adja vissza.
     */
    @Override
    public String getRemoteCommonName() {
        return getHandler().getRemoteCommonName();
    }

    /**
     * A titkosított kommunikáció ezen oldalán álló gép tanúsítványának Name mezőjét adja vissza.
     */
    @Override
    public String getLocalFullName() {
        return getHandler().getLocalFullName();
    }

    /**
     * A titkosított kommunikáció másik oldalán álló gép tanúsítványának Name mezőjét adja vissza.
     */
    @Override
    public String getRemoteFullName() {
        return getHandler().getRemoteFullName();
    }

    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     */
    @Override
    public void closeProcesses() {
        getHandler().closeProcesses(this);
    }
    
}
