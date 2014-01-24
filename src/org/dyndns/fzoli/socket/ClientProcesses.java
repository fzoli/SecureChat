package org.dyndns.fzoli.socket;

import java.net.Socket;
import java.util.List;
import org.dyndns.fzoli.socket.process.Process;

/**
 * Az aktív kapcsolatfeldolgozókat tároló osztály kliens oldalra.
 * @author zoli
 */
public class ClientProcesses extends Processes {
    
    /**
     * Aktív kapcsolatfeldolgozók.
     */
    private static final List<Process> PROCESSES = createList();
    
    /**
     * Azokat az adatfeldolgozókat adja vissza, melyek még dolgoznak.
     */
    public static List<Process> getProcesses() {
        return PROCESSES;
    }
    
    /**
     * A paraméterben átadott listát leszűri.
     * @param clazz a szűrőfeltétel
     */
    public static <T extends Process> List<T> getProcesses(Class<T> clazz) {
        return getProcesses(getProcesses(), clazz);
    }
    
    /**
     * Kapcsolatazonosító alapján megkeresi az adatfeldolgozót.
     * @param connectionId kapcsolatazonosító
     * @return null, ha nincs találat, egyébként adatfeldolgozó objektum
     */
    public static Process findProcess(int connectionId) {
        return findProcess(connectionId, Process.class);
    }
    
    /**
     * Kapcsolatazonosító alapján megkeresi az adatfeldolgozót.
     * @param connectionId kapcsolatazonosító
     * @param clazz az adatfeldolgozó típusa
     * @return null, ha nincs találat, egyébként adatfeldolgozó objektum
     */
    public static <T extends Process> T findProcess(int connectionId, Class<T> clazz) {
        if (clazz == null) return null;
        List<Process> ls = getProcesses();
        for (Process p : ls) {
            if (p.getConnectionId().equals(connectionId)) {
                try {
                    return clazz.cast(p);
                }
                catch (ClassCastException ex) {
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * Megadja, hogy a kliens oldali listában benne van-e az az adatfeldoglozó,
     * mely socket kapcsolata megegyezik a megadott socket kapcsolattal.
     * @param socket a keresendő socket
     */
    public static boolean contains(Socket socket) {
        return contains(getProcesses(), socket);
    }
    
}
