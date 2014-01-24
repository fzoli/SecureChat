package org.dyndns.fzoli.chat;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import static org.dyndns.fzoli.util.OSUtils.setApplicationName;
import static org.dyndns.fzoli.ui.UIUtil.setSystemLookAndFeel;
import static org.dyndns.fzoli.chat.SplashScreenLoader.setSplashMessage;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.chat.ui.UIUtil;
import static org.dyndns.fzoli.util.MacApplication.setDockIcon;

/**
 * Chat-alkalmazás választó
 * @author zoli
 */
public class Main {
    
    /**
     * Az alkalmazásválasztó szótára.
     */
    static ResourceBundle res;
    
    /**
     * Szótár betöltése.
     * A szótár a rendszer nyelvén töltődik be, de ha a rendszernyelvhez nem tartozik szótár,
     * akkor az angol nyelvű szótár töltődik be.
     */
    private static void loadLanguage() {
        res = UIUtil.createResource("org.dyndns.fzoli.chat.l10n.chooser", Locale.getDefault());
    }
    
    /**
     * A megadott alkalmazás indítása.
     * @param app az alkalmazás: <code>client</code> vagy <code>server</code>
     * @param args az alkalmazásnak átadandó paraméterek
     */
    private static boolean runApp(String app, String[] args) {
        // ha a kliens indítását kérték, kliens indítása paraméterátadással
        if ("client".equalsIgnoreCase(app)) {
            org.dyndns.fzoli.chat.client.Main.main(args);
            return true;
        }
        // ha a szerver indítását kérték, szerver indítása paraméterátadással
        if ("server".equalsIgnoreCase(app)) {
            org.dyndns.fzoli.chat.server.Main.main(args);
            return true;
        }
        return false;
    }
    
    /**
     * Alkalmazásválasztó ablak.
     * Csak akkor jön létre, ha van grafikus környezet.
     */
    private static AppChooserFrame frame;
    
    /**
     * A Kliens- vagy a szerver-alkalmazást elindító metódus.
     * Az ablak megjelenítése előtt a szótár,
     * az alkalmazásnév és a Look and Feel beállítódik.
     * @see Main
     */
    public static void main(final String[] args) {
        setApplicationName("Secure Chat"); // alkalmazásnév beállítása mielőtt AWT vagy SWT komponensek kerülnek használatra
        loadLanguage(); // szótár betöltése
        
        // ha legalább 1 paraméter meg lett adva
        if (args.length > 0) {
            // az első paraméter kivételével minden más
            // paramétert tartalmazó tömb létrehozása
            String[] arg = new String[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                arg[i - 1] = args[i];
            }
            // paraméterben kért alkalmazás futtatása paraméterátadással és
            // ha az alkalmazás lefutott, kilépés a main metódusból
            if (runApp(args[0], arg)) {
                return;
            }
        }
        
        // ha nem lett paraméter megadva, vagy az első paraméter nem alkalmazás-paraméter
        if (GraphicsEnvironment.isHeadless()) { // ha csak a konzol érhető el
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // konzol bemenet olvasásához
            try {
                String answer = null; // kezdetben nincs válasz
                // addig kérdez, míg nincs helyes válasz megadva
                while (answer == null || !(answer.isEmpty() || answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("n"))) {
                    // megkérdi, induljon-e el a szerver alkalmazás
                    System.err.print(res.getString("server-run") + " (y/N) ");
                    answer = reader.readLine().trim(); // a megadott válasz tárolása
                }
                reader.close(); // bemenet felszabadítása
                // ha 'Y' a válasz, szerver alkalmazás indítása
                if (answer.equalsIgnoreCase("y")) runApp("server", args);
            }
            catch (IOException ex) {
                System.err.println(res.getString("input-error")); // jelzi, ha hiba történt a bemenet olvasásakor
            }
        }
        else { // ha a grafikus felület elérhető
            // jelzés a felhasználónak, hogy alkalmazásválasztás következik
            setSplashMessage(res.getString("loading"));
            // rendszer LAF beállítása
            setSystemLookAndFeel();
            // Mac OS X-en dock ikon beállítása az alkalmazásválasztó ikonjára
            setDockIcon(R.getServerImage());
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // várakozás az alkalmazásválasztó-ablak bezárására
                    while (frame == null || frame.isVisible()) {
                        try {
                            Thread.sleep(100);
                        }
                        catch (Exception ex) {
                            ;
                        }
                    }
                    String app = frame.getSelectedApp(); // a kiválasztott alkalmazás lekérése
                    frame = null; // az ablak referenciájának megszüntetése, hogy a GC törölhesse
                    // ha ki lett választva az alkalmazás, alkalmazás indítása
                    if (app != null) {
                        runApp(app, args);
                    }
                }
                
            }).start();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // alkalmazásválasztó ablak megjelenítése
                    frame = new AppChooserFrame();
                }
                
            });
        }
    }
    
}
