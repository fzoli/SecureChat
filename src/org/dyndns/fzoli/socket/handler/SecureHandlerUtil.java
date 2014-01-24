package org.dyndns.fzoli.socket.handler;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.CertificateException;
import org.dyndns.fzoli.socket.Processes;
import org.dyndns.fzoli.socket.handler.exception.SecureHandlerException;
import org.dyndns.fzoli.socket.process.Process;
import org.dyndns.fzoli.socket.process.SecureProcess;

/**
 * Segédosztály a SecureHandler interfész implementálásához.
 * @author zoli
 */
class SecureHandlerUtil {
    
    /**
     * Ha kivétel képződik, fel kell dolgozni.
     * A SecureHandlert implementáló osztályban elsőként ezt a metódust, majd az ős metódusát kell meghívni.
     * @param ex a kivétel
     * @throws SecureHandlerException ha nem sikerül az SSL kézfogás
     */
    public static void onException(Exception ex) {
        if (ex instanceof SSLHandshakeException) throw new SecureHandlerException(ex);
    }
    
    /**
     * Ha a kiválasztott Process null, fel kell dolgozni.
     * Bezárja az összes többi kapcsolatot, ami már létre lett hozva a másik oldallal.
     * @param h a kapcsolatkezelő
     */
    public static void onProcessNull(SecureHandler h) {
        h.closeProcesses();
    }
    
    /**
     * Igaz, ha ugyan azzal a tanúsítvánnyal és azonosítókkal rendelkezik a két feldolgozó.
     * @param h1 az egyik kapcsolatkezelő
     * @param h2 a másik kapcsolatkezelő
     */
    public static boolean isCertEqual(SecureHandler h1, SecureHandler h2) {
        return isCertEqual(h1, h2.getRemoteCommonName(), h2.getDeviceId(), h2.getConnectionId());
    }
    
    /**
     * Igaz, ha ugyan azzal a tanúsítvánnyal és azonosítókkal rendelkezik a feldolgozó, mint a paraméterben megadottak.
     * @param h a kapcsolatkezelő
     * @param remoteName tanúsítvány common name
     * @param deviceId eszközazonosító
     * @param connectionId kapcsolatazonosító
     */
    public static boolean isCertEqual(SecureHandler h, String remoteName, int deviceId, int connectionId) {
        return h.getRemoteCommonName().equals(remoteName) && h.getDeviceId().equals(deviceId) && h.getConnectionId().equals(connectionId);
    }
    
    /**
     * A paraméterben átadott listát leszűri.
     */
    public static List<SecureProcess> getSecureProcesses(List<Process> processes) {
        return Processes.getProcesses(processes, SecureProcess.class);
    }
    
    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     * @param h a kapcsolatkezelő
     * @param asker opcionális paraméter, ha meg van adva akkor szerver oldalon a kapcsolatszámlálót is figyelembe veszi a metódus
     */
    public static void closeProcesses(SecureHandler h, SecureProcess asker) {
        boolean secureServer = h instanceof AbstractSecureServerHandler;
        List<SecureProcess> procs = h.getSecureProcesses();
        for (SecureProcess prc : procs) { // végigmegy a biztonságos kapcsolatfeldolgozókon ...
            try {
                // ... és ha megegyező eszközazonosítóval és Common Name mezővel rendelkeznek ...
                if (prc.getDeviceId().equals(h.getDeviceId()) && prc.getRemoteCommonName().equals(h.getRemoteCommonName())) {
                    if (secureServer && asker != null) { // ha kell ellenőrzi a szerver oldali ID-ket is
                        Integer id1 = AbstractSecureServerHandler.getConnectionCounter(prc);
                        Integer id2 = AbstractSecureServerHandler.getConnectionCounter(asker);
                        if (id1 == null || id2 == null || id1 == id2) prc.dispose(); // ha egyező ID vagy nem definiált ...
                    }
                    else {
                        prc.dispose(); // ... bezárja a kapcsolatukat és azonnal törli őket a nyilvántartásból
                    }
                }
            }
            catch (Exception ex) { // ha nem sikerült bezárni a socketet, akkor már zárva volt
                ;
            }
        }
    }
    
    /**
     * A titkosított kommunikáció ezen oldalán álló gép tanúsítványának CN mezőjét adja vissza.
     * @throws SecureHandlerException ha a tanúsítvány hibás
     */
    public static String getLocalCommonName(SSLSocket socket) {
        try {
            checkSession(socket);
            return getCommonName(socket.getSession().getLocalCertificates()[0]);
        }
        catch (CertificateException ex) {
            throw new SecureHandlerException(ex);
        }
        catch (CertificateEncodingException ex) {
            throw new SecureHandlerException(ex);
        }
    }
    
    /**
     * A titkosított kommunikáció másik oldalán álló gép tanúsítványának CN mezőjét adja vissza.
     * @throws SecureHandlerException ha nem megbízható a kapcsolat vagy a tanúsítvány hibás
     */
    public static String getRemoteCommonName(SSLSocket socket) {
        try {
            checkSession(socket);
            return getCommonName(socket.getSession().getPeerCertificates()[0]);
        }
        catch (SSLPeerUnverifiedException ex) {
            throw new SecureHandlerException(ex);
        }
        catch (CertificateException ex) {
            throw new SecureHandlerException(ex);
        }
        catch (CertificateEncodingException ex) {
            throw new SecureHandlerException(ex);
        }
    }
    
    /**
     * A titkosított kommunikáció ezen oldalán álló gép tanúsítványának Name mezőjét adja vissza.
     * @throws SecureHandlerException ha a tanúsítvány hibás
     * @return ha definiálva van, akkor a teljes név; egyébként az általános név
     */
    public static String getLocalFullName(SSLSocket socket) {
        return getFullName(socket, false);
    }
    
    /**
     * A titkosított kommunikáció másik oldalán álló gép tanúsítványának Name mezőjét adja vissza.
     * @throws SecureHandlerException ha nem megbízható a kapcsolat vagy a tanúsítvány hibás
     * @return ha definiálva van, akkor a teljes név; egyébként az általános név
     */
    public static String getRemoteFullName(SSLSocket socket) {
        return getFullName(socket, true);
    }
    
    /**
     * A titkosított kommunikáció egyik tanúsítványának Name mezőjét adja vissza.
     * @param remote ha true, akkor a távoli oldalon álló gép, egyébként a helyi gép tanúsítványával dolgozik
     * @throws SecureHandlerException ha nem megbízható a kapcsolat vagy a tanúsítvány hibás
     * @return ha definiálva van, akkor a teljes név; egyébként az általános név
     */
    private static String getFullName(SSLSocket socket, boolean remote) {
        try {
            checkSession(socket);
            CDNMap props = getCertificateSubjectDNMap(remote ? socket.getSession().getPeerCertificates()[0] : socket.getSession().getLocalCertificates()[0]);
            if (props == null) return remote ? getRemoteCommonName(socket) : getLocalCommonName(socket);
            String name = props.get(CDNMap.NAME);
            if (name == null || name.isEmpty()) return remote ? getRemoteCommonName(socket) : getLocalCommonName(socket);
            return name;
        }
        catch (SSLPeerUnverifiedException ex) {
            throw new SecureHandlerException(ex);
        }
        catch (CertificateException ex) {
            throw new SecureHandlerException(ex);
        }
        catch (CertificateEncodingException ex) {
            throw new SecureHandlerException(ex);
        }
    }
    
    /**
     * Az SSLSocket munkamenetének ellenőrzése.
     * @throws SecureHandlerException ha a munkamenet nem érvényes
     */
    private static void checkSession(SSLSocket socket) {
        if (socket == null || socket.getSession() == null || socket.getSession().getLocalCertificates() == null || (!socket.getSession().isValid())) throw new SecureHandlerException("Invalid certificate. Please, check your CA.");
    }
    
    /**
     * A tanúsítvány CN mezőjét adja vissza.
     * @throws CertificateException ha a tanúsítvány nem X509 szabvány szerint kódolt
     * @throws CertificateEncodingException ha a tanúsítvány nem X509 szabvány szerint kódolt
     */
    private static String getCommonName(Certificate cert) throws CertificateException, CertificateEncodingException {
        String certdata = getCertificateSubjectDN(cert); // RFC2253 formátumú adatok megszerzése
        CDNMap props = getCertificateSubjectDNMap(certdata); // megpróbálja értelmezni a tanúsítvány adatait
        if (props != null) { // ha sikerült legenerálni a tanúsítvány tulajdonságait, akkor ...
            return props.get(CDNMap.COMMON_NAME); // ... a CN tulajdonsággal tér vissza
        }
        else { // ha nem sikerült értelmezni a stringet, akkor ...
            int cnstart = certdata.indexOf("CN=") + 3; // "CN=" résztől ...
            int cnstop = certdata.indexOf(',', cnstart); // ... a vesszőig ...
            if (cnstop == -1) cnstop = certdata.length(); // ... vagy ha nincs vessző, a végéig ...
            return certdata.substring(cnstart, cnstop); // ... kérem a string tartalmát, ami a tanúsítványban szereplő Common Name (CN)
        }
    }
    
    /**
     * A tanúsítványok tulajdonságainak tárolására használt oszály.
     */
    public static class CDNMap extends HashMap<String, String> {

        /**
         * A CDNMap egyik kulcsa.
         */
        public static final String
                COUNTRY_CODE = "C",
                STATE_NAME = "ST",
                LOCALITY_NAME = "L",
                ORGANIZATION_NAME = "O",
                ORGANIZATION_UNIT_NAME = "OU",
                COMMON_NAME = "CN",
                NAME = "Name",
                EMAIL_ADDR = "Email";
        
    }
    
    /**
     * Egy felsorolás ami tartalmazza a gyakran használt X.500 tulajdonságok azonosítóit és azok jelentését a CDNMap konstans-kulcsainak megfelelően.
     * A {@link #getCertificateSubjectDNMap(java.security.cert.Certificate)} metódus használja fel.
     * @see CDNMap
     */
    private static Map<String, String> OIDS = new HashMap<String, String>() {
        
        {
            put("2.5.4.41", CDNMap.NAME);
            put("1.2.840.113549.1.9.1", CDNMap.EMAIL_ADDR);
        }
        
    };
    
    /**
     * A tanúsítvány alapadatait adja vissza UTF-8 kódolással.
     * @param cert a tanúsítvány, aminek az adatait szeretnénk megkapni
     * @throws CertificateException ha a tanúsítvány nem X509 szabvány szerint kódolt
     * @throws CertificateEncodingException ha a tanúsítvány nem X509 szabvány szerint kódolt
     * @return egy CDNMap objektum, ami tartalmazza a tanúsítvány összes tulajdonságát, vagy null, ha hiba történt a feldolgozásban
     */
    private static CDNMap getCertificateSubjectDNMap(Certificate cert) throws CertificateException, CertificateEncodingException {
        return getCertificateSubjectDNMap(getCertificateSubjectDN(cert));
    }
    
    /**
     * A tanúsítvány alapadatait adja vissza UTF-8 kódolással.
     * @param dn RFC2253 kódolású szöveges formátum, ami a tanúsítvány adatait tartalmazza
     * @throws CertificateException ha a tanúsítvány nem X509 szabvány szerint kódolt
     * @throws CertificateEncodingException ha a tanúsítvány nem X509 szabvány szerint kódolt
     * @return egy CDNMap objektum, ami tartalmazza a tanúsítvány összes tulajdonságát, vagy null, ha hiba történt a feldolgozásban
     */
    private static CDNMap getCertificateSubjectDNMap(String dn) throws CertificateException, CertificateEncodingException {
        try {
            LdapName ldapDN = new LdapName(dn);
            CDNMap map = new CDNMap();
            for (Rdn rdn : ldapDN.getRdns()) {
                String t = rdn.getType();
                Object o = rdn.getValue();
                String v = o instanceof byte[] ? new String((byte[]) o, "utf8").trim() : o.toString();
                if (OIDS.containsKey(t)) t = OIDS.get(t);
                map.put(t, v);
            }
            return map;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * A tanúsítvány alapadatait adja vissza RFC2253 kódolású szöveges formátumban.
     * @param cert a tanúsítvány, aminek az adatait szeretnénk megkapni
     * @throws CertificateException ha a tanúsítvány nem X509 szabvány szerint kódolt
     * @throws CertificateEncodingException ha a tanúsítvány nem X509 szabvány szerint kódolt
     */
    private static String getCertificateSubjectDN(Certificate cert) throws CertificateException, CertificateEncodingException {
        try {
            X509Certificate crt;
            if (cert instanceof X509Certificate) crt = (X509Certificate) cert;
            else crt = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
            X500Principal pr = crt.getSubjectX500Principal();
            return pr.getName(X500Principal.RFC2253);
        }
        catch (Exception ex) {
            return javax.security.cert.X509Certificate.getInstance(cert.getEncoded()).getSubjectDN().getName();
        }
    }
    
//    /**
//     * Ékezetes név tesztelése.
//     */
//    public static void main(String[] args) throws Exception {
//        JOptionPane.showMessageDialog(null, getCertificateSubjectDNMap("2.5.4.41=#140e4661726b6173205a6f6c74c3a16e,CN=fzoli,OU=Chat certificate,O=fzoli.dyndns.org,L=Budapest,ST=Pest,C=HU"));
//    }
    
}
