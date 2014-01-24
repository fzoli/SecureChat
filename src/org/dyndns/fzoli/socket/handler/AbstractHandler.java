package org.dyndns.fzoli.socket.handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dyndns.fzoli.socket.handler.event.HandlerListener;
import org.dyndns.fzoli.socket.process.Process;

/**
 * Kapcsolatkezelő kliens és szerver oldalra.
 * A socket feldolgozása előtt az adatok alapján kiválasztja, melyik feldolgozót kell indítani és azt új szálban elindítja.
 * @author zoli
 */
public abstract class AbstractHandler implements Handler {
    
    private final Socket SOCKET;
    
    private final List<HandlerListener> LISTENERS = Collections.synchronizedList(new ArrayList<HandlerListener>());
    
    /**
     * @param socket Socket, amin keresztül folyik a kommunikáció.
     */
    public AbstractHandler(Socket socket) {
        SOCKET = socket;
    }
    
    /**
     * A kapcsolatkezelő eseményfigyelőit adja vissza.
     */
    @Override
    public List<HandlerListener> getHandlerListeners() {
        return LISTENERS;
    }

    /**
     * A kapcsolatkezelőhöz eseményfigyelőt ad hozzá.
     */
    @Override
    public void addHandlerListener(HandlerListener listener) {
        synchronized(LISTENERS) {
            LISTENERS.add(listener);
        }
    }

    /**
     * A kapcsolatkezelőből eseményfigyelőt távolít el.
     */
    @Override
    public void removeHandlerListener(HandlerListener listener) {
        synchronized(LISTENERS) {
            LISTENERS.remove(listener);
        }
    }

    /**
     * Kiválasztja a kapcsolatfeldolgozó objektumot az adatok alapján és elindítja.
     * A metódus csak akkor hívható meg, amikor már ismert a kapcsolatazonosító és eszközazonosító,
     */
    protected abstract Process selectProcess();
    
    /**
     * Létrehoz egy kapcsolatinicializáló segédet, ami a státuszüzenet küldését és fogadását intézi.
     * Alapértelmezés szerint {@link ObjectStreamDeviceHandler} objektum jön létre.
     * @return egy kapcsolatinicializáló segéd
     * @see DeviceHandler
     */
    protected DeviceHandler createDeviceHandler(InputStream in, OutputStream out) {
        return new ObjectStreamDeviceHandler(in, out);
    }
    
    /**
     * Miután az eszközazonosító és a kapcsolatazonosító közlése megtörtént,
     * lefut ez az inicializáló metódus, ami után a konkrét feldolgozás történik meg.
     * Ez a metódus az utód osztályoknak lett létrehozva inicializálás céljára.
     * Ebben a metódusban nem célszerű socketen át adatot küldeni vagy fogadni.
     * Inicializálás alatt igény szerint lehet kivételt is dobni, melyet mindkét oldal
     * megkap a Handler inicializálása közben, és le lehet kezelni a dobott kivételt.
     * @throws HandlerException ha a helyi eszközön kivétel keletkezett
     * @throws RemoteHandlerException ha a távoli eszközön kivétel keletkezett
     */
    protected int init() {
        return -1;
    }

    /**
     * A kapcsolatfeldolgozó kiválasztása után hívódik meg.
     * Meghívja az összes eseménykezelő onProcessSelected metódusát külön szálakban.
     */
    protected void fireProcessSelected() {
        List<HandlerListener> ls = getHandlerListeners();
        for (final HandlerListener hl : ls) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    hl.onProcessSelected(AbstractHandler.this);
                }
                
            }).start();
        }
    }
    
    /**
     * @return Socket, amin keresztül folyik a kommunikáció.
     */
    @Override
    public Socket getSocket() {
        return SOCKET;
    }
    
}
