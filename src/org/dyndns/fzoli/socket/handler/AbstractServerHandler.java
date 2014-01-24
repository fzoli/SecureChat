package org.dyndns.fzoli.socket.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dyndns.fzoli.socket.ServerProcesses;
import org.dyndns.fzoli.socket.handler.exception.HandlerException;
import org.dyndns.fzoli.socket.process.Process;

/**
 * Kapcsolatkezelő szerver oldalra.
 * A socket feldolgozása előtt az adatok alapján kiválasztja, melyik feldolgozót kell indítani.
 * @author zoli
 */
public abstract class AbstractServerHandler extends AbstractHandler {

    /**
     * Segédváltozó szálkezeléshez.
     * Arra kell, hogy biztosítva legyen, hogy egy időben egyszerre
     * csak egy Process objektum inicializálódik a szerver oldalon.
     * (A kliens oldalon nem szükséges ennek figyelése.)
     */
    private static final Object INIT_LOCK = new Object();
    
    private static final Map<Process, Integer> CONN_COUNTS = new HashMap<Process, Integer>();
    
    /**
     * Azonosító.
     */
    private Integer deviceId, connectionId;
    
    /**
     * A szerver oldali kapcsolatkezelő konstruktora.
     * @param socket Socket, amin keresztül folyik a kommunikáció.
     */
    public AbstractServerHandler(Socket socket) {
        super(socket);
    }

    /**
     * Azokat az adatfeldolgozókat adja vissza, melyek még dolgoznak.
     */
    @Override
    public List<Process> getProcesses() {
        return ServerProcesses.getProcesses();
    }
    
    /**
     * A kapcsolatazonosító a szerver oldalon addig nem ismert, míg a kliens nem közli.
     * Ha a kapcsolat létrejön, a második bejövő bájt tartalmazza a kapcsolatazonosítót,
     * ameddig ez nem jön át, a kapcsolatazonosító null értékű marad.
     * @return Kapcsolatazonosító, ami segítségével megtudható a kapcsolatteremtés célja.
     */
    @Override
    public Integer getConnectionId() {
        return connectionId;
    }

    /**
     * Az eszközazonosító a szerver oldalon addig nem ismert, míg a kliens nem közli.
     * Ha a kapcsolat létrejön, az első bejövő bájt tartalmazza az eszközazonosítót,
     * ameddig ez nem jön át, az eszközazonosító null értékű marad.
     * @return Eszközazonosító, ami segítségével megtudható a kliens típusa.
     */
    @Override
    public Integer getDeviceId() {
        return deviceId;
    }
    
    /**
     * Beállítja az eszközazonosítót.
     * Amint a metódus lefutott, már biztonságosan elkérhető az adat.
     */
    private void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    
    /**
     * Beállítja a kapcsolatazonosítót.
     * Amint a metódus lefutott, már biztonságosan elkérhető az adat.
     */
    private void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * Ha kivétel képződik a szálban, fel kell dolgozni.
     * @param ex a kivétel
     * @throws HandlerException ha nem RuntimeException a kivétel
     * @throws RuntimeException ha RuntimeException a kivétel
     */
    protected void onException(Exception ex) {
        if (ex instanceof RuntimeException) throw (RuntimeException) ex;
        throw new HandlerException(ex);
    }

    /**
     * Ha a kiválasztott Process null, fel kell dolgozni.
     */
    protected void onProcessNull() {
        ;
    }
    
    /**
     * Megpróbálja az üzenetet fogadni a klienstől.
     * Ha a kliens oldalon hiba keletkezett, kivételt dob.
     * @throws IOException ha nem sikerült a fogadás
     * @throws RemoteHandlerException ha a kliens oldalon hiba keletkezett
     */
    private void readStatus(DeviceHandler dh) throws IOException {
        HandlerUtil.readStatus(dh);
    }
    
    /**
     * Az inicializáló metódust kivételkezelten meghívja és közli a klienssel az eredményt.
     * @throws Exception ha inicializálás közben kivétel történt
     * @throws IOException ha nem sikerült a kimenetre írni
     */
    private int runInit(DeviceHandler dh) throws IOException, Exception {
        return HandlerUtil.runInit(this, dh);
    }
    
    /**
     * A kliens-kapcsolat létrejötte után az azonosító cserék és inicializálás folyamatához használt időtúllépés.
     * Alapértelmezés: 3 mp
     */
    protected int getPrefixSoTimeout() {
        return 3000;
    }
    
    /**
     * Az inicializálás után a processnek adódik át a socket; a process ezen időtúllépéssel kapja meg a socketet.
     * Alapértelmezés: végtelen
     */
    protected int getPostfixSoTimeout() {
        return 0;
    }
    
    /**
     * Ez a metódus fut le a szálban.
     * Az eszköz- és kapcsolatazonosító klienstől való fogadása után eldől, melyik kapcsolatfeldolgozót
     * kell használni a szerver oldalon és a konkrét feldolgozás kezdődik meg.
     * Ha a feldolgozás végetér, az erőforrások felszabadulnak.
     * @throws HandlerException ha bármi hiba történik
     */
    @Override
    public void run() {
        try {
            
            // stream referenciák megszerzése
            InputStream in = getSocket().getInputStream();
            OutputStream out = getSocket().getOutputStream();
            
            // maximum 3 másodperc van a két bájt olvasására és az inicializálásra
            getSocket().setSoTimeout(getPrefixSoTimeout());
            
            // eszközazonosító elkérése a klienstől
            setDeviceId(in.read());
            // kapcsolatazonosító elkérése a klienstől
            setConnectionId(in.read());
            
            // létrehozza az eszközazonosító alapján a kapcsolatkialakító metódusokat definiáló objektumot
            DeviceHandler dh = createDeviceHandler(in, out);
            if (dh == null) throw new NullHandlerException();
            
            // adatfeldolgozó referencia
            Process proc;
            
            // egy időben egyszerre csak egy Process inicializálódhat
            synchronized (INIT_LOCK) {
            
                // inicializálás és eredményközlés a kliensnek
                int id = runInit(dh);

                // eredmény fogadása a klienstől és kivételdobás hiba esetén
                readStatus(dh);

                // időtúllépés eredeti állapota kikapcsolva
                getSocket().setSoTimeout(getPostfixSoTimeout());

                // adatfeldolgozó kiválasztása
                proc = selectProcess();

                if (proc != null) {
                    // process id tárolása
                    setConnectionCounter(proc, id);
                    
                    // jelzés, hogy kiválasztódott a Process
                    fireProcessSelected();

                    // adatfeldolgozó hozzáadása a listához
                    getProcesses().add(proc);

                }
                else {
                    // ha nem lett kiválasztva Process, jelzés
                    onProcessNull();
                }
            
            }
            
            // ha sikerült az adatfeldolgozó létrehozása
            if (proc != null) {
                try {
                    // adatfeldolgozó futtatása
                    proc.run();
                    // adatfeldolgozó eltávolítása a listából
                    getProcesses().remove(proc);
                    // id törlése
                    setConnectionCounter(proc, null);
                }
                catch (RuntimeException ex) {
                    // adatfeldolgozó eltávolítása a listából hiba esetén is
                    getProcesses().remove(proc);
                    // id törlése hiba esetén is
                    setConnectionCounter(proc, null);
                    throw ex;
                }
            }
            
            // kapcsolat bezárása
            in.close();
            out.flush();
            out.close();
            
        }
        catch (Exception ex) {
            // nem várt hiba esetén socket bezárása...
            try {
                getSocket().close();
            }
            catch (Exception e) {
                ;
            }
            // ... és esemény jelzése
            onException(ex);
        }
    }
    
    public static Integer getConnectionCounter(Process proc) {
        return CONN_COUNTS.get(proc);
    }
    
    private static void setConnectionCounter(Process proc, Integer count) {
        if (count != null) CONN_COUNTS.put(proc, count);
        else CONN_COUNTS.remove(proc);
    }
    
}
