package org.dyndns.fzoli.socket.mjpeg;

import java.io.OutputStream;
import java.util.Arrays;

/**
 * MJPEG streamelő.
 * @author zoli
 */
public abstract class JpegProvider {

    /**
     * Az MJPEG folyamba küldött szerver paraméter.
     */
    private static final String STR_SERVER = System.getProperty("os.name") + "-" + System.getProperty("os.version");
    
    /**
     * MIME Boundary: két JPEG képet elválasztó jel.
     */
    private static final String STR_BOUNDARY = "--Ba4oTvQMY8ew04N8dcnM";
    
    /**
     * Hibakód a kivételkezeléshez.
     */
    protected static final int ERR_HEADER_WRITE = 0,
                               ERR_FIRST_READ = 1,
                               ERR_READ = 2,
                               ERR_WRITE = 3;
    
    /**
     * MJPEG kimenő folyam.
     */
    private final OutputStream out;
    
    /**
     * Segédváltozó az aktuális képkocka újraküldéséhez.
     */
    private boolean resend = false;
    
    /**
     * Megadja, hogy szükség van-e a header adatok
     * elküldésére a streamelés kezdete előtt.
     */
    private boolean sendInfoHeader = true;
    
    /**
     * Konstruktor.
     * @param out az MJPEG kimenő folyam
     */
    public JpegProvider(OutputStream out) {
        if (out == null) throw new NullPointerException("Out parameter of JpegProvider can not be null");
        this.out = out;
    }

    /**
     * Beállítja, hogy szükség van-e a header adatok
     * elküldésére a streamelés kezdete előtt.
     */
    protected void setSendInfoHeader(boolean sendInfoHeader) {
        this.sendInfoHeader = sendInfoHeader;
    }
    
    /**
     * Megadja, hogy meg kell-e szakítani a streamelést.
     * @return true esetén megszakad a streamelés, egyébként folytatódik tovább
     */
    protected boolean isInterrupted() {
        return false;
    }
    
    /**
     * Megadja, hogy a következő képkocka kiolvasható-e.
     * Ha a metódus igazzal tér vissza, az összes meghívott
     * {@link #nextFrame(boolean)} metódus null referenciával lép ki.
     * @return false esetén kiolvasható, egyébként nem
     */
    protected boolean isUnreadable() {
        return false;
    }
    
    /**
     * Megadja, hogy az aktuális képkockát ki lehet-e írni a kimenő folyamra.
     * @return false esetén kiküldhető, egyébként nem
     */
    protected boolean isUnwriteable() {
        return false;
    }
    
    /**
     * Újraküldi az aktuális képkockát, ha van képkocka és fut a kapcsolatkezelés.
     */
    public void resend() {
        resend = true;
    }
    
    /**
     * A JPEG képkocka adatát adja vissza bájt tömbben.
     * Az utód osztály eldöntheti, hogy honnan szerzi meg ezt az adatot.
     */
    protected abstract byte[] getFrame();
    
    /**
     * A JPEG képkocka adatát állítja be.
     * Ezzel a {@code getFrame(boolean)} metódus befejezi a várakozást.
     * Az utód osztály eldöntheti, hogy hol tárolja ezt az adatot.
     * @param frame a képkocka adata bájt tömbben
     */
    protected abstract void setFrame(byte[] frame);
    
    /**
     * Egy képkockát ad vissza.
     * Ha még nincs egy képkocka se beállítva, mindenképpen megvárja.
     * Ha már van képkocka beállítva, paramétertől függ, hogy vár-e a következőre.
     * Ezzel a megoldással az esetleg lassú kapcsolattal rendelkező kliensek nem húzzák
     * vissza a gyorsabb kapcsolattal rendelkezőket.
     * Ennek ára az, hogy van egy FPS limit a {@code Thread.sleep(int)} metódus miatt.
     * Ha az újraküldés aktiválva van és van képkocka,
     * akkor azonnal visszatér az aktuális képkockával és kikapcsolja az újraküldést.
     * @param wait várja-e meg a következő képkockát létező adat esetén
     */
    private byte[] nextFrame(boolean wait) throws InterruptedException {
        if (isUnreadable() || isInterrupted()) return null;
        byte[] frame;
        byte[] tmp = frame = getFrame();
        if ((wait && !resend) || tmp == null) {
//            EQU RES OUT
//            1   1   0
//            1   0   1
//            0   1   0
//            0   0   0
//            A képlet: !((EQU && RES) || !EQU) illetve (!EQU || !RES) && EQU
//            Levezetés:
//            EQU ↛ RES = !(EQU → RES)
//            EQU → RES = (EQU && RES) || !EQU
//            A két egyenlet alapján kijön a bal oldali fenti képlet.
//            A jobb oldali képlet is ugyan azt a kimenetet adja.
            boolean equ;
            while (!isInterrupted() && !isUnreadable() && ((frame = getFrame()) == null || (tmp != null && !(((equ = Arrays.equals(tmp, frame)) && resend) || !equ)))) {
                Thread.sleep(20);
            }
        }
        resend = false; 
        return frame;
    }
    
    /**
     * Kivétel keletkezett a ciklusban a kimenetre írás közben.
     * A metódus eredetileg false értékkel tér vissza, ezzel a ciklus végetér.
     * @param ex a keletkezett kivétel
     * @param err a kivételhez tartozó hibakód
     * @return true esetén folytatódik a ciklus, egyébként kilép a ciklusból
     */
    protected boolean onException(Exception ex, int err) {
        return false;
    }
    
    /**
     * MJPEG folyamot küld a kimenetre.
     * Forrás: http://www.damonkohler.com/2010/10/mjpeg-streaming-protocol.html
     */
    public void handleConnection() {
        if (sendInfoHeader) { // ha ki kell küldeni a headert
            try {
                out.write(( // header generálása és küldése
                    "HTTP/1.0 200 OK\r\n" +
                    "Server: " + STR_SERVER + "\r\n" +
                    "Connection: close\r\n" +
                    "Max-Age: 0\r\n" +
                    "Expires: 0\r\n" +
                    "Cache-Control: no-cache, private\r\n" + 
                    "Pragma: no-cache\r\n" + 
                    "Content-Type: multipart/x-mixed-replace; " +
                    "boundary=" + STR_BOUNDARY + "\r\n\r\n").getBytes());
            }
            catch (Exception ex) { // ha a header küldése közben hiba történt
                if (!onException(ex, ERR_HEADER_WRITE)) return; // kilépés, ha azt kéri a kivételkezelő
            }
        }
        byte[] frame;
        try {
            frame = nextFrame(false); // első képkocka kiolvasása (várakozás nélkül)
        }
        catch (Exception ex) {
            if (!onException(ex, ERR_FIRST_READ)) return; // ha nem sikerült kiolvasni, kilépés, ha azt kéri a kivételkezelő
            frame = null; // egyébként nincs első képkocka
        }
        while (!isInterrupted()) { // amíg nincs megszakítva a streamelés
            if (frame != null) { // ha van képkocka ...
                if (!isUnwriteable()) try { // ... és kiküldhető a képkocka, akkor kiküldés
                    out.write((
                        STR_BOUNDARY + "\r\n" +
                        "Content-type: image/jpg\r\n" +
                        "Content-Length: " +
                        frame.length +
                        "\r\n\r\n").getBytes());
                    out.write(frame);
                    out.write("\r\n\r\n".getBytes());
                    out.flush();
                }
                catch (Exception ex) { // ha a küldés közben hiba történt
                    if (!onException(ex, ERR_WRITE)) break; // kilépés, ha azt kéri a kivételkezelő
                }
            }
            else { // ha nincs képkocka
                try {
                    Thread.sleep(20); // vár egy kicsit
                }
                catch (Exception ex) {
                    ;
                }
            }
            try {
                frame = nextFrame(true); // várakozás a következő képkockára, ami a ciklus elején kerül elküldésre, ha lehetséges
            }
            catch (Exception ex) {
                if (!onException(ex, ERR_READ)) break; // ha nem sikerült a képkocka megszerzése, kilépés, ha azt kéri a kivételkezelő
            }
        }
    }

}
