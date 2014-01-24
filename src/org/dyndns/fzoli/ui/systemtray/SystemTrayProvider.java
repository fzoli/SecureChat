package org.dyndns.fzoli.ui.systemtray;

/**
 * Rendszerikont biztosító osztály.
 * Ha az SWT elérhető, SWT rendszerikon, egyébként AWT rendszerikon jön létre.
 * @author zoli
 */
public final class SystemTrayProvider {

    /**
     * A létrehozott rendszerikon referenciája.
     */
    private static SystemTray st;
    
    private SystemTrayProvider() {
    }
    
    /**
     * A rendszerikon referenciáját adja vissza.
     * Ha még nincs létrehozva, létrejön a rendszerikon.
     */
    public static SystemTray getSystemTray() {
        return getSystemTray(false);
    }
    
    /**
     * A rendszerikon referenciáját adja vissza.
     * Ha még nincs létrehozva, létrejön a rendszerikon.
     * @param awt ha true, akkor AWT rendszerikon lesz használva, egyébként ha elérhető, SWT
     */
    public static SystemTray getSystemTray(boolean awt) {
        return getSystemTray(false, awt);
    }
    
    /**
     * A rendszerikon referenciáját adja vissza.
     * Ha még nincs létrehozva, létrejön a rendszerikon.
     * @param recreate true esetén újrainicializálás, az előző rendszerikonok megsemmisítése
     * @param awt ha true, akkor AWT rendszerikon lesz használva, egyébként ha elérhető, SWT
     */
    public static SystemTray getSystemTray(boolean recreate, boolean awt) {
        if (st == null || recreate) {
            if (recreate && st != null) st.dispose();
            if (!awt && isSwtTrayAvailable()) st = new AwtSystemTray();
            else st = new AwtSystemTray();
        }
        return st;
    }
    
    /**
     * Megadja, hogy használható-e az SWT rendszerikon.
     */
    private static boolean isSwtTrayAvailable() {
        try {
            Class.forName("chrriis.dj.nativeswing.swtimpl.components.JTray", false, SystemTrayProvider.class.getClassLoader());
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
}
