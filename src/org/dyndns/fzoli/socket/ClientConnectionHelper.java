package org.dyndns.fzoli.socket;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.socket.handler.AbstractSecureClientHandler;
import org.dyndns.fzoli.socket.handler.Handler;
import org.dyndns.fzoli.socket.handler.event.HandlerListener;
import org.dyndns.fzoli.socket.process.SecureProcess;

/**
 * Kliens oldalra egyszerű kapcsolódást megvalósító osztály.
 * @author zoli
 */
public abstract class ClientConnectionHelper {
    
    /**
     * Eszközazonosító.
     */
    private final int deviceId;
    
    /**
     * Kapcsolatazonosítók.
     */
    private final int[] connectionIds;

    /**
     * A kiépített kapcsolatokat tartalmazó lista.
     */
    private final List<SSLSocket> CONNECTIONS = Collections.synchronizedList(new ArrayList<SSLSocket>());
    
    /**
     * Eseménykezelő, ami lefut, ha sikerült az első kapcsolódás.
     * Ha az első kapcsolódás sikerült, létrehozza a többi kapcsolatot is.
     */
    private final HandlerListener listener = new HandlerListener() {

        @Override
        public void onProcessSelected(Handler handler) {
            synchronized(connectionIds) {
                for (int i = 1; i < connectionIds.length; i++) {
                    runHandler(connectionIds[i], false);
                }
            }
        }
        
    };
    
    /**
     * Kapcsolódás megszakítására létrehozott segédváltozó.
     */
    private int state = -1;
    
    /**
     * Egyszerű kapcsolódást megvalósító osztály konstruktora.
     * @param deviceId eszközazonosító
     * @param connectionIds kapcsolatazonosítókat tartalmazó tömb
     */
    public ClientConnectionHelper(int deviceId, int[] connectionIds) {
        if (connectionIds == null || connectionIds.length < 1) throw new IllegalArgumentException("At least one Connection ID needs to be added");
        this.deviceId = deviceId;
        this.connectionIds = connectionIds;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            /**
             * A program leállása előtt minden kapcsolat bezárása.
             */
            @Override
            public void run() {
                disconnect();
            }
            
        }));
    }
    
    /**
     * Megmondja, hány socket van tárolva a listában.
     */
    private int getConnectionsSize() {
        synchronized(CONNECTIONS) {
            return CONNECTIONS.size();
        }
    }
    
    /**
     * Az összes lezárt kapcsolatot eltávolítja a kapcsolatokat tartalmazó listából.
     */
    private void removeClosedConnections() {
        synchronized (CONNECTIONS) {
            Iterator<SSLSocket> it = CONNECTIONS.iterator();
            while (it.hasNext()) {
                SSLSocket conn = it.next();
                if (conn.isClosed()) it.remove();
            }
        }
    }
    
    /**
     * Megmondja, hogy kapcsolódva van-e a kliens a szerverhez.
     * @return true, ha az összes kapcsolat ki van alakítva a szerverrel
     */
    public boolean isConnected() {
        removeClosedConnections();
        return getConnectionsSize() == connectionIds.length;
    }

    /**
     * Megmondja, hogy a kapcsolódás folyamatban van-e.
     * @return true, ha a kapcsolódás folyamatban van
     */
    public boolean isConnecting() {
        return state == 0;
    }
    
    public boolean isCancelled() {
        return state == -2;
    }
    
    /**
     * Socket létrehozása.
     * Kapcsolódás a szerverhez.
     */
    protected abstract SSLSocket createConnection() throws GeneralSecurityException, IOException;
    
    /**
     * Kliens oldali Handler példányosítása.
     * @param socket a kapcsolat a szerverrel
     * @param deviceId az eszközazonosító
     * @param connectionId a kapcsolatazonosító
     */
    protected abstract AbstractSecureClientHandler createHandler(SSLSocket socket, int deviceId, int connectionId);
    
    /**
     * Ha a kapcsolódás végetért, ez a metódus fut le.
     */
    protected void onConnected() {
        ;
    }
    
    /**
     * Ha kivétel keletkezik, ebben a metódusban le lehet kezelni.
     * @param ex a keletkezett kivétel
     * @param connectionId a közben használt kapcsolatazonosító
     */
    protected void onException(Exception ex, int connectionId) {
        throw new RuntimeException(ex.getMessage() + "; connection id: " + connectionId, ex);
    }
    
    /**
     * Az adott kapcsolatazonosítóval rendelkező kapcsolat létrehozása előtt ezzel a metódussal még megszakítható a kapcsolódás.
     * @return true, ha a kapcsolódás folytatódhat; egyébként false
     */
    protected boolean onCreateConnection(int connectionId) {
        return true;
    }
    
    /**
     * Kapcsolódik a szerverhez a megadott kapcsolatazonosítóval.
     * A létrehozott socketet eltárolja a listában.
     * Ha az utolsó kapcsolat is kialakult, {@code onConnect} metódus fut le.
     * Ha bármi hiba történik, {@code onException} metódus fut le.
     * @param connectionId a kapcsolatazonosító
     * @param addListener megadja, kell-e eseményt hozzáadni
     */
    private void runHandler(int connectionId, boolean addListener) {
        try {
            if (!onCreateConnection(connectionId)) {
                disconnect();
                return;
            }
            SSLSocket conn = createConnection();
            synchronized(CONNECTIONS) {
                if (conn != null) CONNECTIONS.add(conn);
            }
            if (conn == null || isCancelled()) {
                disconnect();
                return;
            }
            AbstractSecureClientHandler handler = createHandler(conn, deviceId, connectionId);
            if (addListener) handler.addHandlerListener(listener);
            new Thread(handler).start();
            if (connectionId == connectionIds[connectionIds.length - 1]) {
                state = 1;
                onConnected();
            }
        }
        catch (Exception ex) {
            onException(ex, connectionId);
        }
    }
    
    /**
     * Kapcsolódás a szerverhez.
     * Ha az utolsó kapcsolat is kialakult, {@code onConnect} metódus fut le.
     * Ha bármi hiba történik a kapcsolódások közben, {@code onException} metódus fut le.
     * Ha már van kialakított kapcsolat vagy éppen kapcsolódás folyik, akkor megszakad a kapcsolódás.
     */
    public void connect() {
        connect(false);
    }
    
    /**
     * Kapcsolódás a szerverhez.
     * Ha az utolsó kapcsolat is kialakult, {@code onConnect} metódus fut le.
     * Ha bármi hiba történik a kapcsolódások közben, {@code onException} metódus fut le.
     * @param close ha már van kialakított kapcsolat vagy éppen kapcsolódás folyik, akkor -
     * true esetén a kapcsolódás előtt minden kapcsolatot bezár;
     * false esetén megszakad a kapcsolódás
     */
    public void connect(boolean close) {
        if (isConnecting() || isConnected()) {
            if (close) disconnect();
            else return;
        }
        state = 0;
        new Thread(new Runnable() {

            @Override
            public void run() {
                runHandler(connectionIds[0], true);
            }
            
        }).start();
    }
    
    /**
     * Egyetlen kapcsolat kialakítása a szerverrel.
     * Nem társul a kapcsolat létrejöttéhez eseménykezelő.
     * Ha bármi hiba történik a kapcsolódások közben, {@code onException} metódus fut le.
     */
    public void connect(final int connectionId) {
        state = 0;
        new Thread(new Runnable() {

            @Override
            public void run() {
                runHandler(connectionId, false);
            }
            
        }).start();
    }
    
    /**
     * A megadott kapcsolatfeldolgozó újrapéldányosítása.
     * Az átadott feldolgozó kapcsolatát bezárja,
     * új kapcsolatot alakít ki az azonosítójával a szerverrel
     * és várakozás nélkül újrapéldányosítja a feldolgozót.
     */
    public void recreateProcess(SecureProcess proc) {
        recreateProcess(proc, 0);
    }
    
    /**
     * A megadott kapcsolatfeldolgozó újrapéldányosítása.
     * Az átadott feldolgozó kapcsolatát bezárja,
     * új kapcsolatot alakít ki az azonosítójával a szerverrel
     * és újrapéldányosítja a feldolgozót.
     * @param sleep a kapcsolat bezárása után ennyi ezredmásodpercet vár az új kapcsolat kialakításáig
     */
    public void recreateProcess(SecureProcess proc, long sleep) {
        if (proc != null) {
            try {
                proc.getSocket().close();
                Thread.sleep(sleep);
            }
            catch (Exception ex) {
                ;
            }
            connect(proc.getConnectionId());
        }
    }
    
    /**
     * A kapcsolatok bezárása.
     * A metódus addig nem fejeződik be, míg az összes
     * kapcsolathoz tartozó adatfeldolgozó le nem áll.
     */
    public void disconnect() {
        state = -2;
        synchronized(CONNECTIONS) {
            Iterator<SSLSocket> it = CONNECTIONS.iterator();
            while (it.hasNext()) {
                SSLSocket conn = it.next();
                it.remove();
                try {
                    conn.close();
                    while (ClientProcesses.contains(conn)) {
                        Thread.sleep(10);
                    }
                }
                catch (Exception ex) {
                    ;
                }
            }
        }
    }
    
}
