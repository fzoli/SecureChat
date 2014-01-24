package org.dyndns.fzoli.chat.server.config;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.dyndns.fzoli.socket.ServerProcesses;
import org.dyndns.fzoli.socket.process.SecureProcess;

/**
* A szerver jogosultságok adatbázisa.
* A jogosultság konfiguráció naprakészen tartása a feladata.
* Az osztály betöltésekor inicializálódik a konfiguráció.
* Ez után elindul egy időzítő, ami 5 másodpercenként megnézi, hogy módosult-e a konfiuráció és ha módosult, frissíti azt.
* Miután az új konfig életbe lépett, a már online klienseken végigmegy és ha tiltásra került egy felhasználó, akkor bontja a kapcsolatot vele.
* @author zoli
*/
public final class Permissions {
    
    /**
     * Az aktuális konfiguráció.
     */
    private static final PermissionConfig CONFIG = new PermissionConfig();

    /**
     * Konstruktor.
     * Elindít egy időzítőt, ami 5 másodpercenként megnézi, módosult-e a konfiguráció és ha igen,
     * meghívja a frissítő metódust a régi konfigurációt átadva neki, hogy össze lehessen hasonlítani az eltéréseket.
     */
    static {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                PermissionConfig prev = CONFIG.refresh();
                if (prev != null) onRefresh(prev);
            }
            
        }, 0, 5000);
    }
    
    /**
     * Az osztályt nem kell példányosítani.
     */
    private Permissions() {
    }

    /**
     * Az aktuális konfigurációt adja vissza.
     */
    public static PermissionConfig getConfig() {
        return CONFIG;
    }
    
    /**
     * Ha a konfiguráció módosul, ez a metódus fut le.
     * @param previous az előző konfiguráció
     */
    private static void onRefresh(PermissionConfig previous) {
        List<SecureProcess> procs = ServerProcesses.getProcesses(SecureProcess.class);
        for (SecureProcess p : procs) {
            String userName = p.getRemoteCommonName();
            if (userName != null && getConfig().isBlocked(userName)) p.getHandler().closeProcesses();
        }
    }
    
}
