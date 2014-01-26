package org.dyndns.fzoli.socket.handler;

import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.socket.handler.exception.MultipleCertificateException;
import org.dyndns.fzoli.socket.process.SecureProcess;

/**
 * Biztonságos kapcsolatkezelő szerver oldalra.
 * A socket feldolgozása előtt az adatok alapján kiválasztja, melyik feldolgozót kell indítani.
 * @author zoli
 */
public abstract class AbstractSecureServerHandler extends AbstractServerHandler implements SecureHandler {

    private String localCommonName, remoteCommonName, localFullName, remoteFullName;
    
    /**
     * A szerver oldali biztonságos kapcsolatkezelő konstruktora.
     * @param socket SSLSocket, amin keresztül folyik a kommunikáció.
     */
    public AbstractSecureServerHandler(SSLSocket socket) {
        super(socket);
    }

    /**
     * Azokat a biztonságos adatfeldolgozókat adja vissza, melyek még dolgoznak.
     */
    @Override
    public List<SecureProcess> getSecureProcesses() {
        return SecureHandlerUtil.getSecureProcesses(getProcesses());
    }
    
    /**
     * Igaz, ha ugyan azzal a tanúsítvánnyal és azonosítókkal rendelkezik a megadott feldolgozó.
     * @param handler a másik feldolgozó
     */
    @Override
    public boolean isCertEqual(SecureHandler handler) {
        return SecureHandlerUtil.isCertEqual(this, handler);
    }

    /**
     * Igaz, ha ugyan azzal a tanúsítvánnyal és azonosítókkal rendelkezik a feldolgozó, mint a paraméterben megadottak.
     * @param remoteName tanúsítvány common name
     * @param deviceId eszközazonosító
     * @param connectionId kapcsolatazonosító
     */
    @Override
    public boolean isCertEqual(String remoteName, int deviceId, int connectionId) {
        return SecureHandlerUtil.isCertEqual(this, remoteName, deviceId, connectionId);
    }
    
    /**
     * Ez a metódus fut le a szálban.
     * Az eszköz- és kapcsolatazonosító klienstől való fogadása után eldől, melyik kapcsolatfeldolgozót
     * kell használni a szerver oldalon és a konkrét feldolgozás kezdődik meg.
     * Ha a feldolgozás végetér, az erőforrások felszabadulnak.
     * @throws HandlerException ha bármi hiba történik
     * @throws SecureHandlerException ha nem megbízható vagy hibás bármelyik tanúsítvány
     * @throws MultipleCertificateException ha ugyan azzal a tanúsítvánnyal több kliens is kapcsolódik
     */
    @Override
    public void run() {
        super.run();
    }

    /**
     * Annak érdekében, hogy az azonos időben kapcsolódó kliensek (azonos felhasználónévvel) ne kapjanak egyező kapcsolatazonosítót,
     * nem lehet egy időben megállapítani mindkét kliens azonosítóját.
     */
    private static final Object COUNTER_LOCK = new Object();
    
    /**
     * Megszerzi a helyi és távoli tanúsítvány Common Name és Name mezőjét és ellenőrzi a tanúsítványt.
     * @throws SecureHandlerException ha nem megbízható vagy hibás bármelyik tanúsítvány
     * @throws MultipleCertificateException ha ugyan azzal a tanúsítvánnyal több kliens is kapcsolódik
     */
    @Override
    protected int init() {
        localCommonName = SecureHandlerUtil.getLocalCommonName(getSocket());
        remoteCommonName = SecureHandlerUtil.getRemoteCommonName(getSocket());
        if (localCommonName.equals(remoteCommonName)) throw new MultipleCertificateException("The client uses the server's name");
        int id = -1;
        synchronized (COUNTER_LOCK) {
            List<Integer> ids = new ArrayList<Integer>();
            List<SecureProcess> procs = getSecureProcesses();
            for (SecureProcess proc : procs) {
                if (proc.getHandler().isCertEqual(this) && !proc.getSocket().isClosed()) {
                    denyMultipleCerts();
                    Integer i = getConnectionCounter(proc);
                    if (i != null) ids.add(i);
                }
            }
//            Integer maxId = ids.isEmpty() ? 0 : Collections.max(ids);
//            id = maxId + 1;
            for (int i = 0; i <= Integer.MAX_VALUE; i++) {
                if (!ids.contains(i)) {
                    id = i;
                    break;
                }
            }
        }
        localFullName = SecureHandlerUtil.getLocalFullName(getSocket());
        remoteFullName = SecureHandlerUtil.getRemoteFullName(getSocket());
        return id;
    }

    protected void denyMultipleCerts() {
        throw new MultipleCertificateException("Duplicated certificate");
    }
    
    /**
     * Visszatér egy listával, ami tartalmazza az aktuálisan kialakított kapcsolatok azonosítóit.
     * A szerver oldalon hasznos, ha szükség van az aktuálisan kialakított kapcsolatok sorrendi ellenőrzésére.
     */
    protected List<Integer> getActiveConnectionIds() {
        List<SecureProcess> l = getSecureProcesses();
        List<Integer> ids = new ArrayList<Integer>();
        for (SecureProcess p : l) {
            if (p.getDeviceId() == null || p.getDeviceId() != getDeviceId()) continue;
            if (!p.getRemoteCommonName().equals(getRemoteCommonName())) continue;
            ids.add(p.getConnectionId());
        }
        return ids;
    }
    
    /**
     * Ha kivétel képződik, fel kell dolgozni.
     * @param ex a kivétel
     * @throws HandlerException ha nem RuntimeException a kivétel
     * @throws SecureHandlerException ha nem sikerül az SSL kézfogás
     * @throws RuntimeException ha RuntimeException a kivétel
     */
    @Override
    protected void onException(Exception ex) {
        SecureHandlerUtil.onException(ex);
        super.onException(ex);
    }

    /**
     * Ha a kiválasztott Process null, fel kell dolgozni.
     * Bezárja az összes többi kapcsolatot, ami már létre lett hozva az adott klienssel.
     */
    @Override
    protected void onProcessNull() {
        super.onProcessNull();
        SecureHandlerUtil.onProcessNull(this);
    }

    /**
     * Kiválasztja a biztonságos kapcsolatfeldolgozó objektumot az adatok alapján és elindítja.
     * A metódus csak akkor hívható meg, amikor már ismert a kapcsolatazonosító és eszközazonosító.
     */
    @Override
    protected abstract SecureProcess selectProcess();

    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     */
    @Override
    public void closeProcesses() {
        closeProcesses(null);
    }
    
    /**
     * Bezárja a kapcsolatkezelőhöz tartozó kapcsolatfeldolgozók kapcsolatait.
     */
    @Override
    public void closeProcesses(SecureProcess asker) {
        SecureHandlerUtil.closeProcesses(this, asker);
    }
    
    /**
     * @return SSLSocket, amin keresztül folyik a titkosított kommunikáció.
     */
    @Override
    public SSLSocket getSocket() {
        return (SSLSocket) super.getSocket();
    }

    /**
     * A titkosított kommunikáció ezen oldalán álló szerver tanúsítványának CN mezőjét adja vissza.
     */
    @Override
    public String getLocalCommonName() {
        return localCommonName;
    }

    /**
     * A titkosított kommunikáció másik oldalán álló kliens tanúsítványának CN mezőjét adja vissza.
     */
    @Override
    public String getRemoteCommonName() {
        return remoteCommonName;
    }
    
    /**
     * A titkosított kommunikáció ezen oldalán álló kliens tanúsítványának Name mezőjét adja vissza.
     */
    @Override
    public String getLocalFullName() {
        return localFullName;
    }

    /**
     * A titkosított kommunikáció másik oldalán álló szerver tanúsítványának Name mezőjét adja vissza.
     */
    @Override
    public String getRemoteFullName() {
        return remoteFullName;
    }
    
}
