package org.dyndns.fzoli.socket;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dyndns.fzoli.socket.process.Process;

/**
 * Az aktív kapcsolatfeldolgozókat tároló osztályok őse.
 * @author zoli
 */
public class Processes {
    
    /**
     * Szálbiztos listát készít.
     */
    protected static List<Process> createList() {
        return Collections.synchronizedList(new ArrayList<Process>());
    }
    
    /**
     * A paraméterben átadott listát leszűri.
     * @param processes a megszűrendő lista
     * @param clazz a szűrőfeltétel
     */
    public static <T extends Process> List<T> getProcesses(List<Process> processes, Class<T> clazz) {
        synchronized (processes) {
            List<T> ls = Collections.synchronizedList(new ArrayList<T>());
            for (Process proc : processes) {
                try {
                    ls.add(clazz.cast(proc)); // ezzel a kasztolás módszerrel dobódik csak kivétel
                }
                catch (ClassCastException ex) {
                    ;
                }
            }
            return ls;
        }
    }
    
    /**
     * Megadja, hogy a listában benne van-e az az adatfeldoglozó,
     * mely socket kapcsolata megegyezik a megadott socket kapcsolattal.
     * @param processes a lista, melyben keres
     * @param socket a keresendő socket
     */
    public static boolean contains(List<Process> processes, Socket socket) {
        for (Process proc : processes) {
            if (proc == null || proc.getSocket() == null) continue;
            if (proc.getSocket() == socket) return true;
        }
        return false;
    }
    
}
