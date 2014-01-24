package org.dyndns.fzoli.chat.client;

import org.dyndns.fzoli.chat.SplashScreenLoader;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import org.dyndns.fzoli.chat.ConnectionKeys;
import static org.dyndns.fzoli.chat.SplashScreenLoader.setSplashMessage;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.chat.client.socket.ConnectionHelper;
import org.dyndns.fzoli.chat.client.view.ChatFrame;
import org.dyndns.fzoli.chat.client.view.ConnectionProgressFrame;
import org.dyndns.fzoli.chat.client.view.ConnectionProgressFrame.Status;
import org.dyndns.fzoli.chat.client.view.ConfigEditorFrame;
import org.dyndns.fzoli.chat.model.GroupChatData;
import org.dyndns.fzoli.chat.ui.UIUtil;
import org.dyndns.fzoli.chat.ui.UncaughtExceptionHandler;
import org.dyndns.fzoli.ui.LanguageChooserFrame;
import org.dyndns.fzoli.ui.OptionPane;
import org.dyndns.fzoli.ui.OptionPane.PasswordData;
import static org.dyndns.fzoli.chat.ui.UIUtil.setSystemLookAndFeel;
import org.dyndns.fzoli.ui.systemtray.SystemTrayIcon;
import static org.dyndns.fzoli.ui.systemtray.SystemTrayIcon.showMessage;
import org.dyndns.fzoli.ui.systemtray.TrayIcon.IconType;
import static org.dyndns.fzoli.util.OSUtils.setApplicationName;
import org.dyndns.fzoli.ui.systemtray.MenuItem;
import org.dyndns.fzoli.ui.systemtray.MenuItemReference;
import static org.dyndns.fzoli.util.MacApplication.setDockIcon;
import org.dyndns.fzoli.util.OSUtils;

/**
 *
 * @author zoli
 */
public class Main {
    
    public static ChatFrame CHAT_FRAME;
    
    public static final GroupChatData DATA = new ClientSideGroupChatData();
    
    /**
     * Új sor jel.
     */
    public static final String LS = System.getProperty("line.separator");
    
    /**
     * Callback, ami megjeleníti a kapcsolatbeállító ablakot.
     */
    private static final Runnable CALLBACK_SETTING = new Runnable() {

        @Override
        public void run() {
            // kapcsolatbeállító ablak megjelenítése
            showSettingFrame(false, null);
        }

    };
    
    /**
     * Callback, ami akkor fut le, ha ki szeretnének lépni a programból.
     */
    private static final Runnable CALLBACK_EXIT = new Runnable() {
        
        /**
         * Ha a kilépésre kattintottak.
         * A program azonnal végetér, ha nincs kiépítve kapcsolat,
         * egyébként megkérdezi a felhasználót, hogy biztos ki akar-e lépni
         * és csak akkor lép ki, ha Igen a válasza.
         * Mac-en nem teszi fel a kérdést, egyből kilép, mivel a dialógusablakok
         * nem mindig kerülnek előtérbe és zavaróak.
         */
        @Override
        public void run() {
            if (CONN.isConnected() && !OSUtils.isOS(OSUtils.OS.MAC)) { // ha van kiépített kapcsolat
                // megkérdi, biztos-e a kilépésben
                int opt = OptionPane.showYesNoDialog(CHAT_FRAME, getString("confirm_exit"), getString("confirmation"));
                // ha igen, akkor a program kilép
                if (opt == 0) {
                    exit();
                }
            }
            else { // ha nincs kiépített kapcsolat, a program kilép
                exit();
            }
        }

    };
    
    /**
     * Eseményfigyelő, ami akkor fut le, ha ki szeretnének lépni a programból.
     */
    private static final ActionListener AL_EXIT = new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            CALLBACK_EXIT.run();
        }

    };
    
    /**
     * A konfiguráció beállító ablak bezárásakor figyelmeztetést jelentít meg, ha kell.
     */
    private static final WindowAdapter WL_CFG = new WindowAdapter() {

        @Override
        public void windowClosed(WindowEvent e) {
            configAlert(false);
        }

    };
    
    /**
     * A konfigurációt tartalmazó objektum.
     */
    private static final Config CONFIG = Config.getInstance();
    
    /**
     * A szótár.
     */
    private static ResourceBundle STRINGS = createResource(CONFIG.getLanguage());
    
    /**
     * A szerverrel építi ki a kapcsolatot.
     */
    private static final ConnectionHelper CONN = new ConnectionHelper(CONFIG);
    
    /**
     * Időzítő a szerverhez való kapcsolódás késleltetéséhez.
     */
    private static final Timer TIMER_CONN = new Timer();
    
    /**
     * Ha az értéke true, akkor a program leállítás alatt van.
     * Ez esetben a kapcsolódáskezelő ablak nem jelenik meg, amikor megszakad a szerverrel a kapcsolat.
     */
    private static boolean exiting = false;
    
    /**
     * Nyelvkiválasztó ablak.
     */
    public static LanguageChooserFrame LNG_FRAME;
    
    /**
     * Konfiguráció-szerkesztő ablak.
     */
    private static ConfigEditorFrame CONFIG_EDITOR;
    
    /**
     * Kapcsolódásjelző- és kezelő ablak.
     */
    private static ConnectionProgressFrame PROGRESS_FRAME;
    
    /**
     * A szerző dialógust megjelenítő menüelem, ami inaktív,
     * míg a dialógus látható.
     */
    private static MenuItem MI_ABOUT;
    
    /**
     * Újrakapcsolódás menüopció.
     */
    private static MenuItem MI_RECONNECT;
    
    /**
     * Segédváltozó kapcsolódás kérés detektálására.
     */
    private static boolean connecting = false;
    
    /**
     * A program leállítása.
     * Akkor fut le, amikor a felhasználó ki szeretne lépni a programból.
     */
    private static void exit() {
        exiting = true;
        SystemTrayIcon.dispose();
        UncaughtExceptionHandler.setDisabled(true);
        System.exit(0);
    }
    
    /**
     * Beállítja a kliens kivételkezelő metódusát.
     * Ha a rendszerikonok támogatva vannak, dialógusablak jeleníti meg a nem kezelt kivételeket,
     * egyébként nem változik az eredeti kivételkezelés.
     */
    private static void setExceptionHandler() {
        UncaughtExceptionHandler.apply(R.getClientImage());
    }
    
    /**
     * Beállítja a rendszerikont.
     */
    private static void setSystemTrayIcon() {
        if (SystemTrayIcon.init(true) && SystemTrayIcon.isSupported()) {
            // az ikon beállítása
            SystemTrayIcon.setIcon(getString("app_name"), R.resize(R.getClientImage(), SystemTrayIcon.getIconWidth()), new Runnable() {

                @Override
                public void run() {
                    if (CONN.isConnected() && CHAT_FRAME != null) {
                        CHAT_FRAME.setVisible(!CHAT_FRAME.isVisible());
                    }
                }
                
            });
            // nyelv választó opció hozzáadása
            String lngText = getString("language");
            if (!lngText.equalsIgnoreCase("language")) lngText += " (language)";
            SystemTrayIcon.addMenuItem(lngText, R.getImage("languages.png"), new Runnable() {

                @Override
                public void run() {
                    if (LNG_FRAME != null) LNG_FRAME.setVisible(true);
                }

            });

            //szeparátor hozzáadása
            SystemTrayIcon.addMenuSeparator();

            // kapcsolatbeállítás opció hozzáadása
            SystemTrayIcon.addMenuItem(getString("connection_settings"), R.getImage("preferences.png"), CALLBACK_SETTING);

            // újrakapcsolódás opció hozzáadása
            MI_RECONNECT = SystemTrayIcon.addMenuItem(getString("reconnect"), R.getImage("connect.png"), new Runnable() {

                @Override
                public void run() {
                    reconnect();
                }

            });

            //szeparátor hozzáadása
            SystemTrayIcon.addMenuSeparator();

            // megadja, hogy engedélyezett-e a névjegy megjelenítése
            boolean showAboutEnabled = true;
            if (MI_ABOUT != null) showAboutEnabled = MI_ABOUT.isEnabled();

            // az aktuális névjegy-megjelenítő opció referenciáját adja meg
            final MenuItemReference mirAuthor = new MenuItemReference() {

                @Override
                public synchronized MenuItem getMenuItem() {
                    return MI_ABOUT;
                }

            };

            // névjegy opció hozzáadása
            MI_ABOUT = SystemTrayIcon.addMenuItem(getString("about"), R.getQuestionImage(), new Runnable() {

                @Override
                public void run() {
                    UIUtil.showAuthorDialog(mirAuthor, R.getClientImage());
                }

            });

            // csak egyetlen névjegy ablak lehet látható egyazon időben
            MI_ABOUT.setEnabled(showAboutEnabled);
            
            // kilépés opció hozzáadása
            SystemTrayIcon.addMenuItem(getString("exit"), R.getExitImage(), CALLBACK_EXIT);
        }
    }
    
    /**
     * A beállításkezelő ablakot jeleníti meg.
     * Ha a helyes konfiguráció kényszerített, az ablak bezárása után figyelmeztetés jelenhet meg a beállításokkal kapcsolatban.
     * @param force kényszerítve legyen-e a felhasználó helyes konfiguráció megadására
     * @param tabIndex a megjelenő lapfül
     */
    public static void showSettingFrame(boolean force, Integer tabIndex) {
        if (!CONN.isConnected()) {
            CONN.disconnect();
        }
        if (PROGRESS_FRAME != null) {
            PROGRESS_FRAME.setVisible(false);
        }
        if (CONFIG_EDITOR != null) {
            if (CHAT_FRAME != null && CHAT_FRAME.isVisible()) {
                CONFIG_EDITOR.setLocationRelativeTo(CHAT_FRAME);
            }
            CONFIG_EDITOR.setTabIndex(tabIndex);
            CONFIG_EDITOR.setForce(force);
            CONFIG_EDITOR.setVisible(true);
        }
    }
    
    /**
     * Hibaüzenetet küld a felhasználónak modális dialógusablakban.
     * @param text a megjelenő szöveg
     */
    private static void showSettingError(String text) {
        UIUtil.alert(getString("error"), text, System.err, R.getClientImage());
    }
    
    /**
     * Figyelmeztetést küld a felhasználónak a buborékablakra.
     * Az üzenetre kattintva a kapcsolatbeállító ablak jelenik meg.
     * @param text a megjelenő szöveg
     * @param showSettings jelenjen-e meg a beállítások ablak kattintás esetén
     */
    private static void showSettingWarning(String text, boolean showSettings) {
        showMessage(getString("warning"), text, IconType.WARNING, showSettings ? CALLBACK_SETTING : null);
    }
    
    /**
     * Közli a felhasználóval, hogy a kapcsolódás folyamatban van.
     * Ha a nyitóképernyő még látható, akkor azon történik a jelzés, egyébként
     * a kapcsolódásjelző ablak jelenik meg.
     */
    private static void showConnecting() {
        if (SplashScreenLoader.isVisible()) {
            SplashScreenLoader.setSplashMessage(getString("connect_to_server"));
        }
        else {
            showConnectionStatus(Status.CONNECTING);
        }
    }
    
    /**
     * Beállítja a kapcsolatjelző ablakon a látható ikont és szöveget, majd elrejti a többi ablakot.
     * Ha nincs megadva státusz, akkor az ablak eltűnik, egyébként a megadott státusz jelenik meg.
     * Ha éppen kapcsolódás van, csak a kapcsolódás státusz állítható be.
     * Ha a kapcsolatbeállító ablak látható vagy a program leállítás alatt van,
     * nem jelenik meg a kapcsolatjelző ablak.
     * @param status a kapcsolat státusza
     */
    public static void showConnectionStatus(Status status) {
        if (exiting || CONFIG_EDITOR.isVisible()) return;
        if (connecting && status != Status.CONNECTING) return;
        PROGRESS_FRAME.setStatus(status, MI_RECONNECT);
        CHAT_FRAME.setVisible(false);
    }
    
    /**
     * Megjeleníti a jelszóbekérő dialógust és ha megadták, elmenti a jelszót a memóriába vagy a konfig fájlba.
     * Ha a kapcsolódásjelző ablak nem látható, akkor a jelszókérő dialógus megjelenik a tálcán.
     * @return a megadott jelszó adat, ami akkor null, ha a Beállítások gombra kattintottak
     */
    public static PasswordData showPasswordDialog() {
        PasswordData data = UIUtil.showPasswordInput(R.getClientImage(), true, !PROGRESS_FRAME.isVisible(), getString("settings"), new Runnable() {

            @Override
            public void run() {
                showSettingFrame(false, 1);
            }

        });
        if (data != null) {
            CONFIG.setPassword(data.getPassword(), data.isSave());
            if (data.isSave()) Config.save(CONFIG);
        }
        return data;
    }
    
    /**
     * Ha van kiépítve kapcsolat, bontja azt és új kapcsolatot alakít ki.
     */
    public static void reconnect() {
        if (CONN.isConnecting()) return;
        CONN.disconnect();
        runClient(true, false);
    }
    
    /**
     * A program értelme.
     * Kijelzi, hogy elkezdődött a kapcsolódás és kapcsolódik a szerverhez (ha még nem történt meg).
     * Innentől kezdve már a kommunikációtól függ, hogyan folytatódik a program futása.
     * @param reloadMap legyen-e újratöltve a térkép dialóus
     */
    public static void runClient(boolean reloadMap) {
        runClient(false, reloadMap);
    }
    
    /**
     * A program értelme.
     * Kijelzi, hogy elkezdődött a kapcsolódás és kapcsolódik a szerverhez (ha még nem történt meg).
     * Fél másodperc késleltetés van beállítva, hogy legyen ideje a felhasználónak észlelni a folyamatot,
     * és így a szervernek is van ideje bezárni a régi kapcsolatokat és frissíteni az adatokat.
     * Innentől kezdve már a kommunikációtól függ, hogyan folytatódik a program futása.
     * @param delay legyen-e késleltetés
     * @param reloadMap legyen-e újratöltve a térkép dialóus
     */
    public static void runClient(boolean delay, final boolean reloadMap) {
        if (connecting || CONN.isConnected()) return;
        connecting = true;
        showConnecting();
        TIMER_CONN.schedule(new TimerTask() {

            @Override
            public void run() {
                connecting = false;
                CONN.connect();
            }
            
        }, delay ? ConnectionKeys.RECONN_DELAY : 0);
    }
    
    /**
     * A szótárból kikeresi a megadott kulcshoz tartozó szót.
     */
    public static String getString(String key) {
        return STRINGS.getString(key);
    }
    
    /**
     * Létrehoz egy szótárat a kért nyelvhez és az UIManager-ben megadott, több helyen is használt szövegeket beállítja.
     */
    private static ResourceBundle createResource(Locale locale) {
        return UIUtil.createResource("org.dyndns.fzoli.chat.l10n.client", locale, true);
    }
    
    /**
     * Figyelmeztetést jelenít meg a konfigurációval kapcsolatban, ha az alapértelmezett tanúsítvány van használatban.
     * @param help true esetén megjelenik, hogy hol érhetőek el a beállítások, ha alapértelmezett a konfig; false esetén meg figyelmeztetés
     */
    private static void configAlert(boolean help) {
        if (help) {
            if (CONFIG.isDefault()) {
                showSettingWarning(getString("msg_config_hint"), true);
            }
        }
        if (!help || !CONFIG.isDefault()) {
            if (CONFIG.isReplacedCerts()) {
                showSettingWarning(getString("msg_config_replaced"), false);
            }
        }
    }
    
    /**
     * Megadja, hogy a kliens kapcsolódva van-e a szerverhez.
     */
    public static boolean isConnected() {
        return CONN.isConnected();
    }
    
    /**
     * Beállítja a kért nyelvet.
     * Ha a nyelv megváltozott, szótár cseréje,
     * feliratok lecserélése és ha a konfigszerkesztő-ablak nem látható,
     * az új nyelv elmentése.
     */
    public static void setLanguage(Locale l) {
        if (CONFIG.getLanguage().equals(l)) return;
        STRINGS = createResource(l);
        LanguageChooserFrame.relocalizeWindows();
        setSystemTrayIcon();
        synchronized (CONFIG) {
            CONFIG.setLanguage(l);
            if (!CONFIG_EDITOR.isVisible()) Config.save(CONFIG);
            else LNG_FRAME.setLanguage(l);
        }
    }
    
    /**
     * A kliens main metódusa.
     * A nyitóképernyő szövege megjelenik és a rendszer LAF valamint a kivételkezelő beállítódik, majd:
     * Ha a grafikus felület nem érhető el, konzolra írja a szomorú tényt és a program végetér.
     * Ha a konfigurációban megadott tanúsítványfájlok nem léteznek, közli a hibát és kényszeríti a kijavítását úgy,
     * hogy feldobja a konfiguráció beállító ablakot és addig nem lehet elhagyni, míg nincs létező fájl beállítva.
     * Ezek után megnézi a program, hogy a publikus teszt tanúsítványok vannak-e használva és ha igen, figyelmezteti a felhasználót.
     * Ha a konfiguráció teljes egészében megegyezik az eredeti beállításokkal, a program közli, hol állítható át.
     * Végül a kliens program elkezdi futását.
     */
    public static void main(String[] args) {
        setApplicationName("Secure Chat"); // alkalmazásnév beállítása
        setSplashMessage(getString("please_wait")); // jelzés a felhasználónak, hogy tölt a program
        setSystemLookAndFeel(); // rendszer LAF beállítása
        setDockIcon(R.getClientImage()); // Mac OS X-en dock ikon beállítása az alkalmazásválasztó ikonjára
        setExceptionHandler(); // kivételkezelő átállítása a kivételt megjelenítő dialógusra
        if (GraphicsEnvironment.isHeadless()) { // ha a grafikus felület nem érhető el
            System.err.println(getString("msg_need_gui") + LS + getString("msg_exit"));
            System.exit(1); // hibakóddal lép ki
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setSystemTrayIcon(); // rendszerikon létrehozása
                
                if (!Config.STORE_FILE.exists()) { // ha a konfig fájl nem létezik
                    try {
                        if (!Config.ROOT.exists()) Config.ROOT.mkdirs(); // könyvtár létrehozása, ha nem létezik még
                        Config.STORE_FILE.createNewFile(); // megpróbálja létrehozni
                        Config.STORE_FILE.delete(); // és törli, ha sikerült a létrehozás
                    }
                    catch (IOException ex) { // ha nem lehet létrehozni a fájlt: jogosultság gond
                        showSettingError(getString("msg_need_dir_permission") + LS + getString("msg_dir_path") + ": " + Config.ROOT.getAbsolutePath() + LS + getString("msg_exit"));
                        System.exit(1);
                    }
                }
                if (Config.STORE_FILE.exists() && (!Config.STORE_FILE.canRead() || !Config.STORE_FILE.canWrite())) {
                    showSettingError(getString("msg_need_cfg_permission") + LS + getString("msg_file_path") + ": " + Config.STORE_FILE.getAbsolutePath() + LS + getString("msg_exit"));
                    System.exit(1);
                }
                
                // Előinicializálom az ablakokat, míg a nyitóképernyő fent van,
                // hogy később ne menjen el ezzel a hasznos idő.
                // A nyelvkiválasztó ablakkal kezdem, mivel a konfig-szerkesztő
                // ablak használja a referenciáját.
                
                LNG_FRAME = new LanguageChooserFrame(R.getClientImage(), "org.dyndns.fzoli.chat.l10n", "client", CONFIG.getLanguage(), Locale.ENGLISH, new Locale("hu")) {

                    /**
                     * Ha a nyelv megváltozott, kért nyelv beállítása.
                     */
                    @Override
                    protected void onLanguageSelected(Locale l) {
                        Main.setLanguage(l);
                    }
                    
                };
                PROGRESS_FRAME = new ConnectionProgressFrame(CALLBACK_EXIT);
                CONFIG_EDITOR = new ConfigEditorFrame(CONFIG, WL_CFG);
                if (OSUtils.isOS(OSUtils.OS.MAC) && CONFIG.needInfo()) {
                    // Mac alatt az ablakok előtérbe kerülése nem mindig sikerül
                    // Figyelmeztetem a felhasználót, hogy a modális dialógusok blokkolják a felületet
                    // és ha úgy tűnik, nem válaszol a program, az ablakok elmozdításával egy háttérbe
                    // került modális dialógusablakot találhatnak. A pozícionálási hiba oka ismeretlen...
                    OptionPane.showMessageDialog(R.getClientImage(),
                            getString("warn_testing1") + LS + LS +
                            getString("warn_testing2") + LS + getString("warn_testing3") + LS +
                            getString("warn_testing4") + LS + LS +
                            getString("warn_testing5"), getString("warn_testing1"),
                            OptionPane.INFORMATION_MESSAGE, true);
                }
                if (!CONFIG.isCorrect()) { // ha a tanúsítvány fájlok egyike nem létezik
                    showSettingError(getString("warn_config_error1" + (CONFIG.isDefault() ? 'b' : 'a')) + ' ' + getString("warn_config_error2") + LS + getString("warn_config_error3"));
                    showSettingFrame(true, 1); // kényszerített beállítás és tanúsítvány lapfül előtérbe hozása
                }
                
                CHAT_FRAME = new ChatFrame();
                Rectangle r = CONFIG.getFrameBounds();
                if (r != null) CHAT_FRAME.setBounds(r);
                if (!SystemTrayIcon.isSupported()) {
                    CHAT_FRAME.setDefaultCloseOperation(ChatFrame.DO_NOTHING_ON_CLOSE);
                    CHAT_FRAME.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            AL_EXIT.actionPerformed(null);
                        }

                    });
                }
                CHAT_FRAME.addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentResized(ComponentEvent e) {
                        saveState();
                    }

                    @Override
                    public void componentMoved(ComponentEvent e) {
                        saveState();
                    }
                    
                    private void saveState() {
                        CONFIG.setFrameBounds(CHAT_FRAME.getBounds());
                        Config.save(CONFIG);
                    }
                    
                });
                
                Toolkit.getDefaultToolkit().sync(); // addig nem indul a kliens, míg a felület készen nem áll
                if (CONFIG.isCorrect()) { // ha a konfiguráció megfelelő, kliens indítása
                    configAlert(true); // súgó figyelmeztetés, ha kell
                    runClient(false); // és végül a lényeg
                }
            }

        });
    }
    
}
