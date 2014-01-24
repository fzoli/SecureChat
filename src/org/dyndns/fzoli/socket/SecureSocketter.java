package org.dyndns.fzoli.socket;

import javax.net.ssl.SSLSocket;

/**
 * SecureHandler és SecureProcess közös őse.
 * @author zoli
 */
public interface SecureSocketter extends Socketter {
    
    /**
     * @return SSLSocket, amin keresztül folyik a kommunikáció.
     */
    @Override
    public SSLSocket getSocket();
    
    /**
     * A titkosított kommunikáció ezen oldalán álló gép tanúsítványának CN mezőjét adja vissza.
     */
    public String getLocalCommonName();
    
    /**
     * A titkosított kommunikáció másik oldalán álló gép tanúsítványának CN mezőjét adja vissza.
     */
    public String getRemoteCommonName();
    
    /**
     * A titkosított kommunikáció ezen oldalán álló gép tanúsítványának Name mezőjét adja vissza.
     */
    public String getLocalFullName();
    
    /**
     * A titkosított kommunikáció másik oldalán álló gép tanúsítványának Name mezőjét adja vissza.
     */
    public String getRemoteFullName();
    
}
