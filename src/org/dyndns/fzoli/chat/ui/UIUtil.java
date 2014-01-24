package org.dyndns.fzoli.chat.ui;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import static javax.swing.UIManager.getString;
import static javax.swing.UIManager.put;
import org.dyndns.fzoli.ui.FilePanel;
import org.dyndns.fzoli.ui.OptionPane;
import org.dyndns.fzoli.ui.systemtray.MenuItemReference;

/**
 * Általános UI metódusok, amik kellhetnek több helyen is.
 * @author zoli
 */
public class UIUtil extends org.dyndns.fzoli.ui.UIUtil {
    
    /**
     * Kulcs a lokalizált szöveghez.
     */
    public static final String KEY_CERT_LOAD_ERROR = "SecureChat.certLoadError",
                               KEY_CERT_ENTER_PASSWORD = "SecureChat.certEnterPassword",
                               KEY_ABOUT = "SecureChat.about", KEY_AUTHOR = "SecureChat.author";
    
    /**
     * Az alapértelmezett szövegek beállítása.
     */
    static {
        init(KEY_CERT_LOAD_ERROR, "Failed to load the certification.");
        init(KEY_CERT_ENTER_PASSWORD, "Enter the password of the certification:");
        init(KEY_AUTHOR, "Author");
        init(KEY_ABOUT, "About");
    }
    
    /**
     * Létrehoz egy szótárat a kért nyelvhez.
     * A default szótár létrehozása nem szükséges, mert az angol nyelvű szótárban keres, ha nincs a kért nyelvhez szöveg.
     */
    public static ResourceBundle createResource(String baseName, Locale locale) {
        try {
            final ResourceBundle lng = ResourceBundle.getBundle(baseName, locale);
            final ResourceBundle def = locale == Locale.ENGLISH ? null : ResourceBundle.getBundle(baseName, Locale.ENGLISH);
            final ResourceBundle res = def == null ? lng : new ResourceBundle() {

                @Override
                protected Object handleGetObject(String key) {
                    try {
                        return lng.getObject(key);
                    }
                    catch (Exception ex) {
                        try {
                            return def.getObject(key);
                        }
                        catch (Exception e) {
                            return null;
                        }
                    }
                }

                @Override
                public Enumeration<String> getKeys() {
                    return def.getKeys();
                }

            };
            return res;
        }
        catch (RuntimeException ex) {
            if (locale == Locale.ENGLISH) throw ex;
            return createResource(baseName, Locale.ENGLISH);
        }
    }
    
    /**
     * Létrehoz egy szótárat a kért nyelvhez és az UIManager-ben megadott, több helyen is használt szövegeket beállítja.
     * A default szótár létrehozása nem szükséges, mert az angol nyelvű szótárban keres, ha nincs a kért nyelvhez szöveg.
     */
    public static ResourceBundle createResource(String baseName, Locale locale, boolean fileChooser) {
        ResourceBundle res = createResource(baseName, locale);
        put(UIUtil.KEY_ABOUT, res.getString("about"));
        put(UIUtil.KEY_AUTHOR, res.getString("author"));
        put(UIUtil.KEY_CERT_LOAD_ERROR, res.getString("cert_load_error"));
        put(UIUtil.KEY_CERT_ENTER_PASSWORD, res.getString("cert_enter_password"));
        put(UncaughtExceptionHandler.KEY_UNEXPECTED_ERROR, res.getString("unexpected_error"));
        put(UncaughtExceptionHandler.KEY_UNEXPECTED_ERROR_MSG, res.getString("unexpected_error_msg"));
        put(UncaughtExceptionHandler.KEY_CLICK_FOR_DETAILS, res.getString("click_for_details"));
        put(UncaughtExceptionHandler.KEY_CLOSE, res.getString("close"));
        put(UncaughtExceptionHandler.KEY_COPY, res.getString("copy"));
        put(UncaughtExceptionHandler.KEY_DETAILS, res.getString("details"));
        put(UncaughtExceptionHandler.KEY_EXIT, res.getString("exit"));
        put(UncaughtExceptionHandler.KEY_SELECT_ALL, res.getString("select_all"));
        put(OptionPane.KEY_ERROR, res.getString("error"));
        put(OptionPane.KEY_EXIT, res.getString("exit"));
        put(OptionPane.KEY_PASSWORD, res.getString("password"));
        put(OptionPane.KEY_SAVE_PASSWORD, res.getString("save_password"));
        put(OptionPane.KEY_INPUT_NOT_POSSIBLE, res.getString("input_not_possible"));
        put(OptionPane.KEY_YES, res.getString("yes"));
        put(OptionPane.KEY_NO, res.getString("no"));
        put(OptionPane.KEY_OK, res.getString("ok"));
        if (fileChooser) FilePanel.setResource(res);
        return res;
    }
    
    /**
     * Információt jelenít meg a program szerzőjéről.
     * A dialógust meghívó menüelem inaktív, míg a dialógus látható.
     * @param mi a menüelem, amire kattintva ez a metódus meghívódott
     * @param icon a dialógus ikonja
     */
    public static void showAuthorDialog(final MenuItemReference mi, BufferedImage icon) {
        if (GraphicsEnvironment.isHeadless()) return;
        AboutFrame aboutFrame = new AboutFrame(icon);
        aboutFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                mi.getMenuItem().setEnabled(false);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                mi.getMenuItem().setEnabled(true);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                mi.getMenuItem().setEnabled(true);
            }
            
        });
        aboutFrame.setVisible(true);
    }
    
    /**
     * Bekéri a tanúsítvány jelszavát a felhasználótól.
     * Ha a grafikus felület elérhető, dialógus ablakban kéri be a jelszót,
     * egyébként megpróbálja konzolról bekérni a jelszót.
     * Ha nincs se konzol, se grafikus felület, a program kilép.
     * Ha a dialógus ablakon nem az OK-ra kattintottak, a program kilép.
     * @param icon a címsorban megjelenő ikon
     * @param saveEnabled true esetén engedélyezve van a jelszó mentése
     * @param showOnTaskbar true esetén megjelenik a tálcán
     */
    public static OptionPane.PasswordData showPasswordInput(Image icon, boolean saveEnabled, boolean showOnTaskbar) {
        return showPasswordInput(icon, saveEnabled, showOnTaskbar, null, null);
    }
    
    /**
     * Bekéri a tanúsítvány jelszavát a felhasználótól.
     * Ha a grafikus felület elérhető, dialógus ablakban kéri be a jelszót,
     * egyébként megpróbálja konzolról bekérni a jelszót.
     * Ha nincs se konzol, se grafikus felület, a program kilép.
     * Ha a dialógus ablakon nem az OK-ra kattintottak, a program kilép.
     * @param icon a címsorban megjelenő ikon
     * @param saveEnabled true esetén engedélyezve van a jelszó mentése
     * @param showOnTaskbar true esetén megjelenik a tálcán
     * @param extraText a középső gomb felirata
     * @param extraCallback a középső gomb kattintására lefutó eseménykezelő
     */
    public static OptionPane.PasswordData showPasswordInput(Image icon, boolean saveEnabled, boolean showOnTaskbar, String extraText, Runnable extraCallback) {
        return OptionPane.showPasswordInput(getString(KEY_CERT_LOAD_ERROR), getString(KEY_CERT_ENTER_PASSWORD), icon, saveEnabled, showOnTaskbar, extraText, extraCallback);
    }
    
}
