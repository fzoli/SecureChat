package org.dyndns.fzoli.ui;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.PrintStream;
import javax.swing.UIManager;

/**
 * Általános UI metódusok, amik kellhetnek több helyen is.
 * @author zoli
 */
public class UIUtil {
    
    /**
     * Általános rendszerváltozók.
     */
    private static final String LS = System.getProperty("line.separator");
    
    /**
     * Beállítja a kulcshoz tartozó lokalizált szöveget, de csak akkor, ha nincs még beállítva.
     */
    public static void init(String key, String value) {
        try {
            if (UIManager.get(key) == null) UIManager.put(key, value);
        }
        catch (Error e) {
            System.err.println("Could not initialize the GUI. Exiting.");
            System.exit(1);
        }
    }
    
    /**
     * Egy tályékoztató szöveget jelenít meg a felhasználónak.
     * Ha a grafikus felület elérhető, modális ablakban jelenik meg az üzenet,
     * különben a kimenet streamre megy ki a fejléc és a szöveg.
     * Ha a kimeneti stream System.err, akkor hibaüzenetes ablakikon,
     * egyébként figyelmeztetőikon kerül az ablakra.
     * @param title a fejléc
     * @param text a megjelenő szöveg
     * @param out a kimenet stream
     */
    public static void alert(String title, String text, PrintStream out) {
        alert(title, text, out, false);
    }
    
    /**
     * Egy tályékoztató szöveget jelenít meg a felhasználónak.
     * Ha a grafikus felület elérhető, modális ablakban jelenik meg az üzenet,
     * különben a kimenet streamre megy ki a fejléc és a szöveg.
     * Ha a kimeneti stream System.err, akkor hibaüzenetes ablakikon,
     * egyébként figyelmeztetőikon kerül az ablakra.
     * @param title a fejléc
     * @param text a megjelenő szöveg
     * @param out a kimenet stream
     * @param showOnTaskbar true esetén megjelenik a dialógus a tálcán is
     */
    public static void alert(String title, String text, PrintStream out, boolean showOnTaskbar) {
        alert(title, text, out, null, showOnTaskbar);
    }
    
    /**
     * Egy tályékoztató szöveget jelenít meg a felhasználónak.
     * Ha a grafikus felület elérhető, modális ablakban jelenik meg az üzenet,
     * különben a kimenet streamre megy ki a fejléc és a szöveg.
     * Ha a kimeneti stream System.err, akkor hibaüzenetes ablakikon,
     * egyébként figyelmeztetőikon kerül az ablakra.
     * @param title a fejléc
     * @param text a megjelenő szöveg
     * @param out a kimenet stream
     * @param icon a megjelenő ablak fejlécében megjelenő ikon
     */
    public static void alert(String title, String text, PrintStream out, Image icon) {
        alert(title, text, out, icon, false);
    }
    
    /**
     * Egy tályékoztató szöveget jelenít meg a felhasználónak.
     * Ha a grafikus felület elérhető, modális ablakban jelenik meg az üzenet,
     * különben a kimenet streamre megy ki a fejléc és a szöveg.
     * Ha a kimeneti stream System.err, akkor hibaüzenetes ablakikon,
     * egyébként figyelmeztetőikon kerül az ablakra.
     * @param title a fejléc
     * @param text a megjelenő szöveg
     * @param out a kimenet stream
     * @param icon a megjelenő ablak fejlécében megjelenő ikon
     * @param showOnTaskbar true esetén megjelenik a dialógus a tálcán is
     */
    public static void alert(String title, String text, PrintStream out, Image icon, boolean showOnTaskbar) {
        if (GraphicsEnvironment.isHeadless()) { // ha nem elérhető a grafikus felület
            print(title, text, out);
        }
        else { // ha van grafikus felület
            OptionPane.showMessageDialog(icon, text, title, System.err == out ? OptionPane.ERROR_MESSAGE : OptionPane.WARNING_MESSAGE, showOnTaskbar); // dialógus ablak megjelenítése
        }
    }
    
    /**
     * Paraméter alapján előállítja a kimenetet, és a megadott kimenetre kiírja.
     * @param title a fejléc
     * @param text az üzenet
     * @patam out a kimenet
     */
    public static void print(String title, String text, PrintStream out) {
        out.println(LS + title + ':'); // fejléc, aztán ...
        out.println(text + LS); // ... kimenet streamre írás
    }
    
    /**
     * Az adott rendszer alapértelmezett kinézetét alkalmazza feltéve, ha elérhető a grafikus felület.
     */
    public static void setSystemLookAndFeel() {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                // Linuxra GTK LAF beállítása
                // az OS ellenőrzés szükséges, mert Mac-en is van GTK LAF
                if (System.getProperty("os.name").toLowerCase().contains("nux")) {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                }
                // nem szép dolog, de így legalább nem kell a catch blokkot külön metódusba tenni
                else {
                    throw new Exception();
                }
            }
            catch (Exception ex) {
                // Ha nem Linuxon fut a program, rendszer LAF beállítása
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception e) {
                    ;
                }
            }
        }
    }
    
}
