package org.dyndns.fzoli.ui;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.io.Console;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import static javax.swing.UIManager.getString;
import static org.dyndns.fzoli.ui.UIUtil.init;

/**
 * Dialógusablakokat hoz létre.
 * @author zoli
 */
public class OptionPane extends JOptionPane {
    
    /**
     * A jelszómegjelenítő dialógus visszatérési értéke.
     */
    public static class PasswordData {
        
        private final char[] password;
        private final boolean save;

        public PasswordData(char[] password, boolean save) {
            this.password = password;
            this.save = save;
        }

        /**
         * A beírt jelszó.
         */
        public char[] getPassword() {
            return password;
        }

        /**
         * Kérnek-e jelszómentést.
         * @return true, ha kérnek jelszómentést (vagyis be lett pipálva a checkbox)
         */
        public boolean isSave() {
            return save;
        }
        
    }
    
    /**
     * Kulcs a lokalizált szöveghez.
     */
    public static final String KEY_OK = "OptionPane.ok",
                               KEY_EXIT = "OptionPane.exit",
                               KEY_YES = "OptionPane.yes",
                               KEY_NO = "OptionPane.no",
                               KEY_ERROR = "OptionPane.error",                   
                               KEY_PASSWORD = "OptionPane.password",                    
                               KEY_SAVE_PASSWORD = "OptionPane.savePassword",
                               KEY_INPUT_NOT_POSSIBLE = "OptionPane.inputNotPossible";
    
    /**
     * Az alapértelmezett szövegek beállítása.
     */
    static {
        init(KEY_OK, "OK");
        init(KEY_EXIT, "Exit");
        init(KEY_NO, "No");
        init(KEY_YES, "Yes");
        init(KEY_ERROR, "Error");
        init(KEY_PASSWORD, "Password");
        init(KEY_SAVE_PASSWORD, "Save password");
        init(KEY_INPUT_NOT_POSSIBLE, "Reading from console is not possible!");
    }
    
    /**
     * OK és Kilépés szöveget tartalmazó tömb.
     */
    private static String[] getOkExitOpts() {
        return new String[] { getString(KEY_OK), getString(KEY_EXIT) };
    }
    
    /**
     * Igen és Nem szöveget tartalmazó tömb.
     */
    private static String[] getYesNoOpts() {
        return new String[] { getString(KEY_YES), getString(KEY_NO) };
    }
    
    /**
     * OK szöveget tartalmazó tömb.
     */
    private static String[] getOkOpt() {
        return new String[] { getString(KEY_OK) };
    }
    
    /**
     * Jelszóbekérő dialógust jelenít meg.
     * Ha a grafikus felület elérhető, dialógus ablakban kéri be a jelszót,
     * egyébként megpróbálja konzolról bekérni a jelszót.
     * Ha nincs se konzol, se grafikus felület, a program kilép.
     * Ha a dialógus ablakon nem az OK-ra kattintottak, a program kilép.
     * @param message az első szöveg, ami információközlő
     * @param request a második szöveg, ami kéri a jelszót, hogy írják be
     * @param icon a címsorban megjelenő ikon
     * @param saveEnabled engedélyezve legyen-e a jelszó mentése checkbox
     * @param showOnTaskbar true esetén megjelenik a tálcán
     * @return a beírt jelszó
     */
    public static PasswordData showPasswordInput(String message, String request, Image icon, boolean saveEnabled, boolean showOnTaskbar) {
        return showPasswordInput(message, request, icon, saveEnabled, showOnTaskbar, null, null);
    }
    
    /**
     * Jelszóbekérő dialógust jelenít meg.
     * Ha a grafikus felület elérhető, dialógus ablakban kéri be a jelszót,
     * egyébként megpróbálja konzolról bekérni a jelszót.
     * Ha nincs se konzol, se grafikus felület, a program kilép.
     * Ha a dialógus ablakon a Kilépésre kattintottak, a program kilép.
     * @param message az első szöveg, ami információközlő
     * @param request a második szöveg, ami kéri a jelszót, hogy írják be
     * @param icon a címsorban megjelenő ikon
     * @param saveEnabled engedélyezve legyen-e a jelszó mentése checkbox
     * @param showOnTaskbar true esetén megjelenik a tálcán
     * @param extraText a középső gomb szövege
     * @param extraCallback a középső gombra kattintás eseménykezelője
     * @return a beírt jelszó, vagy null, ha a középső gombra kattintottak
     */
    public static PasswordData showPasswordInput(String message, String request, Image icon, boolean saveEnabled, boolean showOnTaskbar, String extraText, Runnable extraCallback) {
        if (GraphicsEnvironment.isHeadless()) {
            Console console = System.console();
            if (console == null) {
                UIUtil.alert(getString(KEY_ERROR), getString(KEY_INPUT_NOT_POSSIBLE), System.err);
                System.exit(1);
            }
            console.printf("%s%n", message);
            return new PasswordData(console.readPassword(request), false);
        }
        else {
            JPanel panel = new JPanel(new GridLayout(4, 1));
            JLabel lbMessage = new JLabel(message);
            JLabel lbRequest = new JLabel(request);
            JPasswordField pass = new JPasswordField(10);
            JCheckBox save = new JCheckBox(getString(KEY_SAVE_PASSWORD));
            save.setEnabled(saveEnabled);
            save.setSelected(false);
            panel.add(lbMessage);
            panel.add(lbRequest);
            panel.add(pass);
            panel.add(save);
            final boolean hasExtra = extraText != null && extraCallback != null;
            final String[] opts = hasExtra ? new String[] {getOkExitOpts()[0], extraText, getOkExitOpts()[1]} : getOkExitOpts();
            JFrame dummy = createDummyFrame(icon, showOnTaskbar ? getString(KEY_PASSWORD) : null);
            final int option = showOptionDialog(dummy, panel, getString(KEY_PASSWORD),
                NO_OPTION, QUESTION_MESSAGE,
                null, opts, opts[0]);
            dummy.dispose();
            if (option == 0) {
                return new PasswordData(pass.getPassword(), save.isSelected());
            }
            else {
                if (hasExtra) {
                    if (option == 1) extraCallback.run();
                    else System.exit(0);
                }
                else {
                    System.exit(0);
                }
            }
        }
        return null;
    }
    
    /**
     * Üzenet dialógust jelenít meg és a megadott képet használja címsor ikonnak.
     * @param icon a címsorba kerülő ikon képe
     * @param message az üzenet
     * @param title a címsor szövege
     * @param messageType az üzenet típusa, az üzenet melletti ikont folyásolja be
     * @param showOnTaskbar true esetén megjelenik a dialógus a tálcán is
     */
    public static void showMessageDialog(Image icon, String message, String title, int messageType, boolean showOnTaskbar) {
        JFrame dummy = createDummyFrame(icon, showOnTaskbar ? title : null);
        showMessageDialog(dummy, message, title, messageType);
        if (dummy != null) dummy.dispose();
    }
    
    /**
     * Igen/Nem kérdező dialógust jelenít meg.
     * @param owner az ablak, amihez a dialógus tartozik
     * @param message az üzenet
     * @param title a címsor szövege
     */
    public static int showYesNoDialog(Window owner, String message, String title) {
        return showOptionDialog(owner, message, title, NO_OPTION, QUESTION_MESSAGE, null, getYesNoOpts(), getYesNoOpts()[1]);
    }
    
    /**
     * Igen/Nem kérdező dialógust jelenít meg és a megadott képet használja címsor ikonnak.
     * @param icon a címsorba kerülő ikon képe
     * @param message az üzenet
     * @param title a címsor szövege
     */
    public static int showYesNoDialog(Image icon, String message, String title) {
        return showYesNoDialog(createDummyFrame(icon), message, title);
    }
    
    /**
     * Figyelmeztető dialógust jelenít meg és a megadott képet használja címsor ikonnak.
     * @param owner az ablak, amihez a dialógus tartozik
     * @param message az üzenet
     * @param title a címsor szövege
     */
    public static int showWarningDialog(Window owner, String message, String title) {
        return showOptionDialog(owner, message, title, NO_OPTION, WARNING_MESSAGE, null, getOkOpt(), getOkOpt()[0]);
    }
    
    /**
     * Figyelmeztető dialógust jelenít meg és a megadott képet használja címsor ikonnak.
     * @param icon a címsorba kerülő ikon képe
     * @param message az üzenet
     * @param title a címsor szövege
     */
    public static int showWarningDialog(Image icon, String message, String title) {
        return showWarningDialog(createDummyFrame(icon), message, title);
    }
    
    /**
     * Egy kis csel arra, hogy a címsorban a kért ikon jelenhessen meg.
     * Készít egy ablakot, ami soha nem fog megjelenni, de a dialógus örökli a címsorban lévő ikonját.
     * Teljesen fölösleges lenne, ha lehetne címsor ikont megadni a {@code JOptionPane} metódusainak.
     * @param icon a címsorba kerülő ikon képe
     */
    private static JFrame createDummyFrame(Image icon) {
        return createDummyFrame(icon, null);
    }
    
    /**
     * Egy kis csel arra, hogy a címsorban a kért ikon jelenhessen meg.
     * Készít egy ablakot, ami soha nem fog megjelenni, de a dialógus örökli a címsorban lévő ikonját.
     * Teljesen fölösleges lenne, ha lehetne címsor ikont megadni a {@code JOptionPane} metódusainak.
     * @param icon a címsorba kerülő ikon képe
     * @param title az ablak címsora, amit ha megadnak, akkor a tálcán láthatóvá válik
     */
    private static JFrame createDummyFrame(Image icon, String title) {
        JFrame dummy = null;
        if (icon != null) {
            dummy = new JFrame();
            dummy.setIconImage(icon);
        }
        if (dummy != null && title != null) {
            dummy.setSize(0, 0);
            dummy.setUndecorated(true);
            dummy.setLocationRelativeTo(null);
            dummy.setTitle(title);
            dummy.setVisible(true);
        }
        return dummy;
    }
    
}
