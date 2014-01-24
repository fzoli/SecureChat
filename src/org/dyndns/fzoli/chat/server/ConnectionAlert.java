package org.dyndns.fzoli.chat.server;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import static org.dyndns.fzoli.chat.server.Main.VAL_CONN_LOG;
import org.dyndns.fzoli.ui.systemtray.SystemTrayIcon;
import org.dyndns.fzoli.ui.systemtray.TrayIcon.IconType;

/**
 * Sockettel kapcsolatos figyelmeztetésjelző osztály.
 * @author zoli
 */
public class ConnectionAlert {
    
    /**
     * Alapértelmezetten a kapcsolódás és lekapcsolódás nincs jelezve.
     */
    private static boolean show = false;
    
    /**
     * Logger ahhoz, hogy naplózni lehessen a kommunikációval kapcsolatos eseményeket.
     */
    private static final Logger logger;
    
    /**
     * Inicializálja a naplózáshoz szükséges objektumot.
     * Az inicializálás közben fellépő kivételek nem jelennek meg a konzolon.
     */
    static {
        LogLog.setQuietMode(true);
        logger = Logger.getLogger(ConnectionAlert.class);
    }
    
    /**
     * Megadja, hogy be van-e kapcsolva az üzenetjelzés.
     */
    public static boolean isMsgEnabled() {
        return show;
    }
    
    /**
     * Bekapcsolja vagy kikapcsolja az üzenetjelzést.
     */
    public static void setMsgEnabled(boolean enabled) {
        show = enabled;
    }
    
    /**
     * Jelez a felhasználónak, kapcsolódást illetve lekapcsolódást, ha kérik,
     * valamint elvégzi a naplózást is.
     */
    public static void log(String text) {
        logMessage(VAL_CONN_LOG, text, IconType.INFO, show);
    }
    
    /**
     * Naplózza az átadott üzenetet és ha kérik, meg is jeleníti azt a felhasználónak.
     * @param title az üzenet címsora
     * @param text a naplózandó üzenet
     * @param type az üzenet típusa
     * @param show true esetén az üzenet megjelenik a naplózás után
     * @see SystemTrayIcon#showMessage(java.lang.String, java.lang.String, org.dyndns.fzoli.ui.systemtray.TrayIcon.IconType) 
     */
    public static void logMessage(String title, String text, IconType type, boolean show) {
        switch (type) {
            case INFO:
                logger.info(text);
                break;
            case WARNING:
                logger.warn(text);
                break;
            case ERROR:
                logger.error(text);
        }
        if (show) {
            SystemTrayIcon.showMessage(title, text, type);
        }
    }
    
}
