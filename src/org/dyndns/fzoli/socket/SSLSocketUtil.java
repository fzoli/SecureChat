package org.dyndns.fzoli.socket;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.apache.commons.ssl.KeyMaterial;
import org.apache.commons.ssl.SSLClient;
import org.apache.commons.ssl.SSLServer;
import org.apache.commons.ssl.TrustMaterial;
import static org.dyndns.fzoli.resource.MD5Checksum.getMD5Checksum;

/**
 * Segédosztály az SSL Socketek létrehozásához.
 * @author zoli
 */
public class SSLSocketUtil {
    
    /**
     * Callback, amivel megszakítható a kapcsolódás, ha idő közben a kliens meggondolja magát.
     */
    public static interface Callback {
        
        /**
         * A kapcsolódás előtt fut le.
         * @return true, ha a kapcsolódás elindulhat egyébként false
         */
        public boolean onConnect();
        
    }
    
    /**
     * Kliens kapcsolatok létrehozását segítő SSLClient objektumok tárolója.
     * Cache szerepet tölt be, hogy ugyan azokhoz a fájlokhoz ne kelljen újra példányosítást végrehajtani több kapcsolat nyitásakor.
     */
    private static final Map<String, SSLClient> CLIENT_CACHE = new HashMap<String, SSLClient>();
    
    /**
     * SSL Server socket létrehozása.
     * @param port a szerver portja, amin hallgat
     * @param ca annak a CA-nak a tanúsítványa, amely az egyetlen megbízható tanúsítvány kiállító
     * @param crt az egyetlen megbízható CA által kiállított tanúsítvány
     * @param key a tanúsítvány titkos kulcsa
     * @param passwd a használandó tanúsítvány jelszava, ha van, egyébként null
     */
    public static SSLServerSocket createServerSocket(int port, File ca, File crt, File key, char[] passwd) throws GeneralSecurityException, IOException {
        if (passwd == null) passwd = new char[] {}; // ha nincs jelszó megadva, üres jelszó létrehozása
        SSLServer server = new SSLServer(); // SSL szerver socket létrehozására kell
        server.setKeyMaterial(new KeyMaterial(crt, key, passwd)); //publikus és privát kulcs megadása a kapcsolathoz
        server.setTrustMaterial(new TrustMaterial(ca)); // csak a saját CA és az ő általa kiállított tanúsítványok legyenek megbízhatóak
        server.setCheckHostname(false); // a hostname kivételével minden más ellenőrzése, amikor a kliens kapcsolódik
        server.setCheckExpiry(true); // lejárat ellenőrzés
        server.setCheckCRL(true); // visszavonás ellenőrzés
        server.setNeedClientAuth(true); // a kliens is ellenőrizendő
        return (SSLServerSocket) server.createServerSocket(port); // server socket létrehozása
    }
    
    /**
     * SSL kliens socket létrehozása és kapcsolódás a szerverhez.
     * A kapcsolódáskor az eredeti időtúllépés lesz használva.
     * @param port a szerver portja, amin hallgat
     * @param ca annak a CA-nak a tanúsítványa, amely az egyetlen megbízható tanúsítvány kiállító
     * @param crt az egyetlen megbízható CA által kiállított tanúsítvány
     * @param key a tanúsítvány titkos kulcsa
     * @param passwd a használandó tanúsítvány jelszava, ha van, egyébként null
     * @throws NullPointerException ha a jelszó kivételével nincs megadva az egyik paraméter
     */
    public static SSLSocket createClientSocket(String host, int port, File ca, File crt, File key, char[] passwd, Callback callback) throws GeneralSecurityException, IOException {
        return createClientSocket(host, port, ca, crt, key, passwd, null, callback);
    }
    
    /**
     * SSL kliens socket létrehozása és kapcsolódás a szerverhez.
     * @param port a szerver portja, amin hallgat
     * @param ca annak a CA-nak a tanúsítványa, amely az egyetlen megbízható tanúsítvány kiállító
     * @param crt az egyetlen megbízható CA által kiállított tanúsítvány
     * @param key a tanúsítvány titkos kulcsa
     * @param passwd a használandó tanúsítvány jelszava, ha van, egyébként null
     * @param connTimeout kapcsolódáskor használt időtúllépés
     * @throws NullPointerException ha a jelszó kivételével nincs megadva az egyik paraméter
     */
    public static SSLSocket createClientSocket(String host, int port, File ca, File crt, File key, char[] passwd, Integer connTimeout, Callback callback) throws GeneralSecurityException, IOException {
        if (passwd == null) passwd = new char[] {}; // ha nincs jelszó megadva, üres jelszó létrehozása
        String cacheId = getCacheId(ca, crt, key); // cache id generálása
        SSLClient client;
        synchronized (CLIENT_CACHE) {
            client = CLIENT_CACHE.get(cacheId); // a múltban létrehozott SSLClient objektum megszerzése, ha van
            if (client == null) { // ha még nem volt létrehozva, létrehozás és beállítás
                client = new SSLClient(); // SSL kliens socket létrehozására kell
                client.setKeyMaterial(new KeyMaterial(crt, key, passwd)); //publikus és privát kulcs megadása a kapcsolathoz
                client.setTrustMaterial(new TrustMaterial(ca)); // csak a megadott CA és az ő általa kiállított tanusítványok legyenek megbízhatóak
                client.setCheckHostname(false); // hostname ellenőrzés kikapcsolása, minden más engedélyezése
                client.setCheckExpiry(true); // lejárat ellenőrzés
                client.setCheckCRL(true); // visszavonás ellenőrzés
                if (connTimeout != null) client.setConnectTimeout(connTimeout); // kapcsolódás időtúllépés beállítása, ha megadták
                CLIENT_CACHE.put(cacheId, client); // cachelés a memóriába
            }
        }
        if (callback != null && !callback.onConnect()) return null; // a kapcsolódás megszakítása, ha kell
        return (SSLSocket) client.createSocket(host, port); // kliens socket létrehozása és kapcsolódás
    }
    
    /**
     * A tanúsítványfájlok alapján cachelt objektumot ad vissza, ha van.
     */
    public static SSLClient getClientCache(File ca, File crt, File key) {
        synchronized (CLIENT_CACHE) {
            return CLIENT_CACHE.get(getCacheId(ca, crt, key));
        }
    }
    
    /**
     * A tanúsítványfájlok alapján cachelt objektumot állít be.
     */
    public static void setClientCache(File ca, File crt, File key, SSLClient client) {
        synchronized (CLIENT_CACHE) {
            CLIENT_CACHE.put(getCacheId(ca, crt, key), client);
        }
    }
    
    /**
     * Kiüríti a cachet memória-helyfelszabadítás céljából.
     */
    public static void clearClientCache() {
        synchronized (CLIENT_CACHE) {
            CLIENT_CACHE.clear();
        }
    }
    
    /**
     * Cache ID generátor.
     */
    private static String getCacheId(File ca, File crt, File key) {
        try {
            return getMD5Checksum(ca) + getMD5Checksum(crt) + getMD5Checksum(key);
        }
        catch (Exception ex) {
            return ca.getAbsolutePath() + crt.getAbsolutePath() + key.getAbsolutePath();
        }
    }
    
}
