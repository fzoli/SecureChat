package org.dyndns.fzoli.socket;

import java.net.Socket;

/**
 * Handler és Process közös őse.
 * Nem jutott jobb név eszembe. :D
 * @author zoli
 */
public interface Socketter extends Runnable {
    
    /**
     * @return Socket, amin keresztül folyik a kommunikáció.
     */
    public Socket getSocket();
    
    /**
     * @return Kapcsolatazonosító, ami segítségével megtudható a kapcsolatteremtés célja.
     */
    public Integer getConnectionId();
    
    /**
     * @return Eszközazonosító, ami segítségével megtudható a kliens típusa.
     */
    public Integer getDeviceId();
    
}
