package org.dyndns.fzoli.socket.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import org.dyndns.fzoli.socket.ClientProcesses;
import org.dyndns.fzoli.socket.handler.exception.HandlerException;
import org.dyndns.fzoli.socket.process.Process;

/**
 * Kapcsolatkezelő kliens oldalra.
 * A socket feldolgozása előtt az adatok alapján kiválasztja, melyik feldolgozót kell indítani.
 * @author zoli
 */
public abstract class AbstractClientHandler extends AbstractHandler {

    private final Integer deviceId, connectionId;
    
    /**
     * A kliens oldali kapcsolatkezelő konstruktora.
     * @param socket Socket, amin keresztül folyik a kommunikáció.
     * @param deviceId eszközazonosító, ami alapján a szerver tudja, mivel kommunikál
     * @param connectionId kapcsolatazonosító, ami alapján a szerver tudja, mi a kérés
     * @throws IllegalArgumentException ha az eszközazonosító vagy a kapcsolatazonosító mérete nagyobb egy bájtnál vagy negatív értékű
     */
    public AbstractClientHandler(Socket socket, int deviceId, int connectionId) {
        super(socket);
        checkId("Device", deviceId);
        checkId("Connection", connectionId);
        this.deviceId = deviceId;
        this.connectionId = connectionId;
    }

    /**
     * Ellenőrzi az azonosítót, és ha mérete nagyobb egy bájtnál vagy negatív értékű, kivételt dob.
     * @throws IllegalArgumentException ha az azonosító értéke nem megengedett
     */
    private void checkId(String name, int id) {
        if (id < 0 || id > 255) throw new IllegalArgumentException(name + " ID needs to be between 1 and 255");
    }

    /**
     * Azokat az adatfeldolgozókat adja vissza, melyek még dolgoznak.
     */
    @Override
    public List<Process> getProcesses() {
        return ClientProcesses.getProcesses();
    }
    
    /**
     * A kapcsolatazonosítót a kliens generálja, ezért soha nem null.
     * @return Kapcsolatazonosító, ami segítségével megtudható a kapcsolatteremtés célja.
     */
    @Override
    public Integer getConnectionId() {
        return connectionId;
    }

    /**
     * Az eszközazonosítót a kliens generálja, ezért soha nem null.
     * @return Eszközazonosító, ami segítségével megtudható a kliens típusa.
     */
    @Override
    public Integer getDeviceId() {
        return deviceId;
    }
    
    /**
     * Ha kivétel képződik, fel kell dolgozni.
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
     * Megpróbálja az üzenetet fogadni a szervertől.
     * Ha a szerver oldalon hiba keletkezett, kivételt dob.
     * @throws IOException ha nem sikerült a fogadás
     * @throws RemoteHandlerException ha a szerver oldalon hiba keletkezett
     */
    private void readStatus(DeviceHandler dh) throws IOException {
        HandlerUtil.readStatus(dh);
    }
    
    /**
     * Az inicializáló metódust kivételkezelten meghívja és közli a szerverrel az eredményt.
     * @throws Exception ha inicializálás közben kivétel történt
     * @throws IOException ha nem sikerült a kimenetre írni
     */
    private void runInit(DeviceHandler dh) throws IOException, Exception {
        HandlerUtil.runInit(this, dh);
    }
    
    /**
     * A szerver-kapcsolat létrejötte után az azonosító cserék és inicializálás folyamatához használt időtúllépés.
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
     * Az eszköz- és kapcsolatazonosító szervernek való elküldése után eldől, melyik kapcsolatfeldolgozót
     * kell használni a kliens oldalon és a konkrét feldolgozás kezdődik meg.
     * Ha a feldolgozás végetér, az erőforrások felszabadulnak.
     * @throws HandlerException ha bármi hiba történik
     */
    @Override
    public void run() {
        try {
            // stream referenciák megszerzése
            InputStream in = getSocket().getInputStream();
            OutputStream out = getSocket().getOutputStream();
            
            // létrehozza az eszközazonosító alapján a kapcsolatkialakító metódusokat definiáló objektumot
            DeviceHandler dh = createDeviceHandler(in, out);
            if (dh == null) throw new NullHandlerException();
            
            // maximum 3 másodperc van a két bájt küldésére és az inicializálásra
            getSocket().setSoTimeout(getPrefixSoTimeout());
            
            // eszközazonosító küldése a szervernek
            out.write(getDeviceId());
            // kapcsolatazonosító küldése a szervernek
            out.write(getConnectionId());
            
            // eredmény fogadása a szervertől és kivételdobás hiba esetén
            readStatus(dh);
            
            // inicializálás és eredményközlés a szervernek
            runInit(dh);
            
            // időtúllépés eredeti állapota kikapcsolva
            getSocket().setSoTimeout(getPostfixSoTimeout());
            
            // adatfeldolgozó kiválasztása
            Process proc = selectProcess();
            
            if (proc != null) {
                // jelzés, hogy kiválasztódott a Process
                fireProcessSelected();

                // adatfeldolgozó hozzáadása a listához
                getProcesses().add(proc);

                try {
                    // adatfeldolgozó futtatása
                    proc.run();
                    // adatfeldolgozó eltávolítása a listából
                    getProcesses().remove(proc);
                }
                catch (RuntimeException ex) {
                    // adatfeldolgozó eltávolítása a listából hiba esetén is
                    getProcesses().remove(proc);
                    throw ex;
                }
                
            }
            else {
                // ha nem lett kiválasztva Process, jelzés
                onProcessNull();
            }
            
            // kapcsolat bezárása
            in.close();
            out.flush();
            out.close();
        }
        catch (Exception ex) {
            try {
                getSocket().close();
            }
            catch (Exception e) {
                ;
            }
            onException(ex);
        }
    }
    
}
