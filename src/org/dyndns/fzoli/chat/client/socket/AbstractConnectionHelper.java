package org.dyndns.fzoli.chat.client.socket;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.chat.ClientConfig;
import org.dyndns.fzoli.chat.ConnectionKeys;
import org.dyndns.fzoli.socket.ClientConnectionHelper;
import org.dyndns.fzoli.socket.SSLSocketUtil;
import org.dyndns.fzoli.socket.SSLSocketUtil.Callback;

/**
 * Kliens oldalra egyszerű kapcsolódást megvalósító osztály.
 * Kliens oldali konfiguráció alapján hozza létre a socketet.
 * @author zoli
 */
public abstract class AbstractConnectionHelper extends ClientConnectionHelper implements ConnectionKeys {

    /**
     * Konfiguráció a kapcsolódáshoz.
     */
    protected final ClientConfig CONFIG;
    
    /**
     * Egyszerű kapcsolódást megvalósító osztály konstruktora.
     * @param config konfiguráció a kapcsolódás megvalósításához
     * @param deviceId eszközazonosító
     * @param connectionIds kapcsolatazonosítókat tartalmazó tömb
     */
    public AbstractConnectionHelper(ClientConfig config, int deviceId, int[] connectionIds) {
        super(deviceId, connectionIds);
        CONFIG = config;
    }

    /**
     * Socket létrehozása.
     * Kapcsolódás a híd szerverhez a konfiguráció alapján.
     */
    @Override
    protected SSLSocket createConnection() throws GeneralSecurityException, IOException {
        return SSLSocketUtil.createClientSocket(CONFIG.getAddress(), CONFIG.getPort(), CONFIG.getCAFile(), CONFIG.getCertFile(), CONFIG.getKeyFile(), CONFIG.getPassword(), CONFIG.getConnTimeout(), new Callback() {

            @Override
            public boolean onConnect() {
                return !isCancelled();
            }
            
        });
    }
    
}
