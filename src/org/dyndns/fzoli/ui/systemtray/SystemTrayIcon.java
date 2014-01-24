package org.dyndns.fzoli.ui.systemtray;

import java.awt.image.BufferedImage;
import org.dyndns.fzoli.ui.UIUtil;
import org.dyndns.fzoli.ui.systemtray.TrayIcon.IconType;

/**
 * Ha van grafikus felület, egy konkrét rendszerikont jelenít meg, melyet a programon belül könnyen el lehet érni.
 * Az osztály egyetlen rendszerikont jelenít meg, nem tud többet megjeleníteni és megszüntetni sem azt az egyet.
 * Több funkciója is van az ikonnak:
 * - a felhasználó látja, hogy a program fut még akkor is, ha háttérben van
 * - a felhasználó ha nem konzolból indította a programot, csak itt képes leállítani, ha a háttérben fut
 * - nem kezelt kivétel esetén buborékablak tályékoztatja a felhasználót, amire kattintva megtekintheti a hibát
 * @author zoli
 */
public class SystemTrayIcon {

    /**
     * A rendszerikon működését szabályzó objektumok.
     */
    private static SystemTray tray;
    private static TrayIcon icon;
    private static PopupMenu menu;
    
    /**
     * Nincs szükség az osztály példányosítására.
     */
    private SystemTrayIcon() {
    }
    
    /**
     * Inicializálás.
     * Ha az SWT elérhető, SWT alapú, egyébként meg AWT alapú rendszerikon jön létre.
     * @return true, ha sikerült az inicializálás, egyébként false
     */
    public static boolean init() {
        return init(false);
    }
    
    /**
     * Inicializálás.
     * Ha az SWT elérhető, SWT alapú, egyébként meg AWT alapú rendszerikon jön létre.
     * Ha már egyszer hiba nélkül lefutott az inicializálás, az újbóli hívásnak nincs hatása.
     * @param awt ha true, akkor biztosan AWT alapú rendszerikon jön létre
     * @return true, ha sikerült az inicializálás, egyébként false
     */
    public static boolean init(boolean awt) {
        try {
            synchronized (SystemTrayIcon.class) {
                if (tray != null && tray.isSupported()) {
                    icon.setVisible(true);
                    menu = icon.createPopupMenu();
                    return true;
                }
                tray = SystemTrayProvider.getSystemTray(true, awt);
                if (tray.isSupported()) {
                    icon = tray.addTrayIcon();
                    menu = icon.createPopupMenu();
                    return true;
                }
                else if (!awt) {
                    return init(true);
                }
                return false;
            }
        }
        catch (Throwable t) {
            if (!awt) return init(true);
            return false;
        }
    }
    
    /**
     * Az összes rendszerikon megszüntetése.
     */
    public static void dispose() {
        if (isSupported()) {
            try {
                synchronized (SystemTrayIcon.class) {
                    tray.dispose();
                    tray = null;
                    icon = null;
                    menu = null;
                }
            }
            catch (Exception ex) {
                ;
            }
        }
    }
    
    /**
     * Értéke megadja, hogy támogatott-e a rendszerikon az adott rendszeren.
     * Ha az {@link #init(boolean)} metódus még nem lett meghívva, addig hamissal tér vissza.
     * @return true, ha támogatott a rendszerikon
     */
    public static boolean isSupported() {
        return icon != null;
    }
    
    /**
     * Értéke megadja, hogy látható-e a rendszerikon.
     * A rendszerikon nem tehető láthatóvá olyan rendszeren, ahol nem támogatott.
     */
    public static boolean isVisible() {
        return isSupported() && icon.isVisible();
    }
    
    /**
     * Megadja a rendszerikon képének ajánlott magasságát, ha az támogatva van.
     * @return ha az init() már lefutott és van támogatás, az ajánlott képméret; egyébként 16
     */
    public static int getIconWidth() {
        if (java.awt.SystemTray.isSupported()) return java.awt.SystemTray.getSystemTray().getTrayIconSize().width;
        return 16;
    }
    
    /**
     * Menüelemet ad hozzá a rendszerikon menüjéhez, ha az támogatott.
     * @param label a menüben megjelenő szöveg
     * @param callback eseménykezelő
     */
    public static MenuItem addMenuItem(String label, Runnable callback) {
        return addMenuItem(label, null, callback);
    }
    
    /**
     * Menüelemet ad hozzá a rendszerikon menüjéhez, ha az támogatott.
     * @param label a menüben megjelenő szöveg
     * @param img SWT menü esetén megjelenő kép
     * @param callback eseménykezelő
     */
    public static MenuItem addMenuItem(String label, BufferedImage img, Runnable callback) {
        if (isSupported()) {
            return menu.addMenuItem(label, img, callback);
        }
        return null;
    }
    
    /**
     * Checkbox Menüelemet ad hozzá a rendszerikon menüjéhez, ha az támogatott.
     * @param label a menüben megjelenő szöveg
     * @param checked kezdőérték
     * @param callback eseménykezelő
     */
    public static void addCheckboxMenuItem(String label, boolean checked, Runnable callback) {
        if (isSupported()) {
            menu.addCheckboxMenuItem(label, checked, callback);
        }
    }
    
    /**
     * A menühöz szeparátort ad hozzá, ha az támogatott.
     */
    public static void addMenuSeparator() {
        if (isSupported()) {
            menu.addSeparator();
        }
    }
    
    /**
     * Beállítja a paraméterben átadott szöveget és ikont, ha támogatva vannak a rendszerikonok.
     * @param tooltip a megjelenő szöveg, amikor az egér az ikon felett van
     * @param img az a kép, ami megjelenik az ikonban
     * @throws NullPointerException ha a kép null
     */
    public static void setIcon(String tooltip, BufferedImage img) {
        if (isSupported()) {
            icon.setToolTip(tooltip);
            icon.setImage(img);
        }
    }
    
    /**
     * Kérésre megjeleníti, vagy elrejti a rendszerikont.
     * A kérés csak akkor teljesül, ha támogatott a rendszerikonok használata,
     * valamint ha látható ikont eltűnésre vagy nem látható ikont megjelenésre kérnek.
     */
    public static void setVisible(boolean visible) {
        if (isSupported()) {
            icon.setVisible(visible);
        }
    }
    
    /**
     * Üzenetet jelenít meg buborékablakban a felhasználónak.
     * Az üzenet ikonja információközlő.
     * Ha a rendszerikon nem támogatott vagy nem látható, konzolra megy az üzenet.
     * @param title az üzenet lényege pár szóban
     * @param text a teljes üzenet
     * @return ha az üzenet a rendszerikonban jelent meg, true, ha a konzolon, false
     */
    public static boolean showMessage(String title, String text) {
        return showMessage(title, text, IconType.INFO);
    }
    
    /**
     * Üzenetet jelenít meg buborékablakban a felhasználónak.
     * Ha a rendszerikon nem támogatott vagy nem látható, konzolra megy az üzenet.
     * @param title az üzenet lényege pár szóban
     * @param text a teljes üzenet
     * @param type az ikon típusa
     * @return ha az üzenet a rendszerikonban jelent meg, true, ha a konzolon, false
     */
    public static boolean showMessage(String title, String text, IconType type) {
        return showMessage(title, text, type, null);
    }
    
    /**
     * Üzenetet jelenít meg buborékablakban a felhasználónak.
     * Ha a rendszerikon nem támogatott vagy nem látható, konzolra megy az üzenet.
     * @param title az üzenet lényege pár szóban
     * @param text a teljes üzenet
     * @param type az ikon típusa
     * @param callback opcionális eseménykezelő, ami akkor fut le, ha az üzenetre kattintanak
     * @return ha az üzenet a rendszerikonban jelent meg, true, ha a konzolon, false
     */
    public static boolean showMessage(String title, String text, IconType type, Runnable callback) {
        if (isVisible()) {
            icon.displayMessage(title, text, type, callback);
            return true;
        }
        else { // ha az ikon nem támogatott vagy nem látható, konzolra megy az üzenet
            UIUtil.print(title, text, IconType.ERROR == type ? System.err : System.out);
            return false;
        }
    }
    
}
