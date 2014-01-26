package org.dyndns.fzoli.socket.process.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import org.dyndns.fzoli.socket.handler.SecureHandler;
import org.dyndns.fzoli.socket.process.AbstractSecureProcess;
import org.dyndns.fzoli.socket.process.impl.exception.RemoteHostClosedException;

/**
 * Az utód osztályok a másik oldallal kiépített kapcsolatot arra használják, hogy
 * másodpercenként ellenőrzik, hogy megszakadt-e a kapcsolat a másik oldallal.
 * @author zoli
 */
abstract class DisconnectProcess extends AbstractSecureProcess {
    
    /**
     * Konstruktorban beállított konstansok.
     */
    private final int timeout1, timeout2, waiting;

    /**
     * Időzítő a második időtúllépés hívására.
     */
    private Timer timer;
    
    /**
     * Megadja, hogy meg lett-e hívva már az {@code onDisconnect} metódus.
     */
    private boolean disconnected = false;
    
    /**
     * Megadja, hogy meg kell-e hívni a válasz érkezése előtt a timeout vége metódust.
     */
    private boolean timeout = false;
    
    /**
     * Időtúllépés detektáló konstruktora.
     * @param handler Biztonságos kapcsolatfeldolgozó, ami létrehozza ezt az adatfeldolgozót.
     * @param timeout1 az első időtúllépés ideje ezredmásodpercben (nem végzetes korlát)
     * @param timeout2 a második időtúllépés ideje ezredmásodpercben (végzetes korlát)
     * @param waiting két ellenőrzés között eltelt idő
     * @throws NullPointerException ha handler null
     */
    public DisconnectProcess(SecureHandler handler, int timeout1, int timeout2, int waiting) {
        super(handler);
        this.timeout1 = timeout1;
        this.timeout2 = timeout2;
        this.waiting = waiting;
    }
    
    /**
     * Az első időtúllépés ideje.
     */
    public int getFirstTimeout() {
        return timeout1;
    }
    
    /**
     * A második időtúllépés ideje.
     */
    public int getSecondTimeout() {
        return timeout2;
    }
    
    /**
     * Két ellenőrzés között eltelt idő.
     */
    public int getWaiting() {
        return waiting;
    }
    
    /**
     * Ez a metódus hívódik meg, amikor létrejön a kapcsolat.
     */
    protected void onConnect() {
        ;
    }
    
    /**
     * A válaszkérés előtt hívódik meg.
     * @throws Exception az {@code onTimeout} metódusnak átadott kivétel
     */
    protected void beforeAnswer() throws Exception {
        ;
    }
    
    /**
     * Akkor hívódik meg, amikor sikeresen válasz érkezett a távoli géptől.
     * @throws Exception az {@code onTimeout} metódusnak átadott kivétel
     */
    protected void afterAnswer() throws Exception {
        ;
    }
    
    /**
     * Időtúllépés esetén hívódik meg.
     * Az első időtúllépés történt meg, ami még nem végzetes.
     * A metódus ha kivételt dob, az {@code onDisconnect} metódus hívódik meg.
     * @param ex a hibát okozó kivétel
     * @throws Exception az {@code onDisconnect} metódusnak átadott kivétel
     */
    protected void onTimeout(final Exception ex) throws Exception {
        ;
    }
    
    /**
     * Időtúllépés után az első válaszüzenet megérkezésekor hívódik meg.
     * A metódus ha kivételt dob, az {@code onDisconnect} metódus hívódik meg.
     */
    protected void afterTimeout() throws Exception {
        ;
    }
    
    /**
     * Ez a metódus hívódik meg, amikor megszakad a kapcsolat.
     * A második időtúllépés történt meg, ami végzetes hiba.
     * Az összes aktív kapcsolatfeldolgozót leállítja, mely ugyan ahhoz az eszközhöz tartozik.
     * @param ex a hibát okozó kivétel
     */
    protected void onDisconnect(Exception ex) {
        closeProcesses();
    }

    /**
     * A kapcsolat bezárása előtt az időzítő leállítása, hogy a Handler szál végetérhessen.
     */
    @Override
    public void dispose() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        super.dispose();
    }
    
    /**
     * A socket bementének olvasására be lehet állítani időtúllépést.
     * Erre alapozva megtudható, hogy él-e még a kapcsolat a távoli géppel.
     */
    @Override
    public final void run() {
        onConnect(); // onConnect eseménykezelő hívása, hogy a kapcsolat létrejött
        try {
            InputStream in = getSocket().getInputStream(); // bemenet
            OutputStream out = getSocket().getOutputStream(); // kimenet
            getSocket().setSoTimeout(getFirstTimeout()); // in.read() metódusnak az 1. időtúllépés beállítása
            while(true) { // végtelen ciklus, amit SocketException zár be a kapcsolat végén
                try {
                    loop(in, out); // kommunikáció a két fél között
                }
                catch (SocketTimeoutException ex) { // ha az in.read() az 1. időkorláton belül nem kapott bájtot
                    setTimeoutActive(true, ex); // 2. időtúllépés aktiválása, ha kell
                    callOnTimeout(ex); // időtúllépés eseménykezelő hívása
                }
                sleep(getWaiting()); // várakozik egy kicsit, hogy a sávszélességet ne terhelje, és hogy szinkronban legyen a másik oldallal
            }
        }
        catch (Exception ex) { // ha bármilyen hiba történt
            callOnDisconnect(ex); // disconnect eseménykezelő hívása, ha kell
        }
    }
    
    /**
     * Kommunikáció a két fél között.
     * Mindkét fél olvas és ír is a streamekre, de fordított sorrendben.
     * @param in bemenet
     * @param out kimenet
     * @throws Exception ha az olvasás vagy írás közben bármi hiba történik
     */
    protected abstract void loop(InputStream in, OutputStream out) throws Exception;
    
    /**
     * Olvasás a bemeneti streamről és eseményjelzés.
     * Ez egy segédmetódus az utód osztály loop metódusának implementálásához.
     * Az olvasás előtt és után esemény jelzést ad le.
     * Sikeres olvasás esetén inaktiválja a 2. időtúllépést, ha az aktív, hogy
     * ne okozzon téves időtúllépés hívást.
     * @param in bemenet
     * @throws Exception ha az olvasás közben bármi hiba történik
     */
    protected final void read(InputStream in) throws Exception {
        beforeAnswer(); // olvasás előtti eseménykezelő hívása
        if (in.read() != -1) { // válasz a másik oldaltól
            setTimeoutActive(false, null); // 2. időtúllépés inaktiválása, ha kell
            callAfterAnswer(); // olvasás utáni eseménykezelő hívása
        }
        else {
            callOnDisconnect(new RemoteHostClosedException());
        }
    }
    
    /**
     * Írás a kimeneti streamre és stream kiürítése.
     * Ez egy segédmetódus az utód osztály loop metódusának implementálásához.
     * @param out kimenet
     * @throws Exception ha az írás közben bármi hiba történik
     */
    protected final void write(OutputStream out) throws Exception {
        out.write((int)(Math.random() * 256 - 128)); // üzenés a másik oldalnak ...
        out.flush(); // ... azonnal (véletlen bájtot, hogy az esetleges lehallgatót megtévessze)
    }
    
    /**
     * Aktiválja vagy inaktiválja az időzítőt, ami meghívja a második végzetes időtúllépést.
     */
    private void setTimeoutActive(boolean b, final Exception ex) throws SocketException {
        if (b) {
            if (timer == null) { // ha aktiválni kell, csak akkor aktiválódik, ha még nem aktív
                timer = new Timer(); // időzítő létrehozása, ami a 2. időtúllépést hívja meg
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() { // ha letelt az idő
                        callOnDisconnect(ex); // disconnect esemény hívása, ha még nem hívták meg
                    }
                    
                }, getSecondTimeout());
            }
        }
        else {
            if (timer != null) { // ha deaktiválni kell, csak akkor deaktiválódik, ha még aktív
                timer.cancel(); // időzítő leállítása
                timer = null; // Garbage Collector végezheti a dolgát
            }
        }
    }
    
    /**
     * Ha még nem lett meghívva, meghívódik az {@code onDisconnect} metódus.
     */
    private void callOnDisconnect(Exception ex) {
        if (!disconnected) { // ha még nem volt disconnect
            disconnected = true; // jelzé, hogy nem kell többé hívni
            onDisconnect(ex); // eseménykezelő hívása, hogy vége a kapcsolatnak
        }
    }
    
    /**
     * Beállítja az időtúllépés jelzést és meghívja az {@code onTimeout} metódust.
     */
    private void callOnTimeout(Exception ex) throws Exception {
        timeout = true; // timeout jelzés aktiválása
        onTimeout(ex); // eseménykezelő hívása, hogy időtúllépés történt
    }
    
    /**
     * Ha a válasz előtt időtúllépés volt, meghívja az {@code afterTimeout} metódust,
     * majd meghívja az {@code afterTimeout} metódust.
     */
    private void callAfterAnswer() throws Exception {
        if (timeout) { // ha van timeout jelzés
            timeout = false; // jelzés inaktiválása, hogy újra ne fusson ez az ág le
            afterTimeout(); // eseménykezelő hívása, hogy még él a kapcsolat
        }
        afterAnswer(); // eseménykezelő hívása, hogy válasz érkezett
    }
    
}
