package org.dyndns.fzoli.chat.client.view;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import org.dyndns.fzoli.chat.client.Main;
import org.dyndns.fzoli.chat.resource.R;
import static org.dyndns.fzoli.chat.client.Main.getString;
import org.dyndns.fzoli.ui.AbstractConnectionProgressFrame;
import org.dyndns.fzoli.ui.IconTextPanel;
import org.dyndns.fzoli.ui.RelocalizableWindow;
import org.dyndns.fzoli.ui.systemtray.MenuItem;

/**
 *
 * @author zoli
 */
public class ConnectionProgressFrame extends AbstractConnectionProgressFrame implements RelocalizableWindow {
    
    /**
     * A kapcsolatok állapotai.
     */
    public static enum Status {
        CONNECTING(R.getIndicatorIcon()),
        CONNECTION_ERROR,
        UNKNOWN_CONNECTION_ERROR,
        DISCONNECTED(R.getWarningIcon()),
        CONNECTION_REFUSED(R.getWarningIcon()),
        UNKNOWN_HOST,
        CONNECTION_TIMEOUT,
        HANDSHAKE_ERROR,
        KEYSTORE_ERROR,
        SERVER_IS_NOT_CLIENT;

        /**
         * Konstruktor.
         * Az alapértelmezett ikon a hibát jelző ikon.
         */
        private Status() {
            this(R.getErrorIcon());
        }

        /**
         * Konstruktor.
         * @param icon az állapothoz tartozó ikon
         */
        private Status(Icon icon) {
            ICON = icon;
        }

        /**
         * Az állapothoz tartozó ikon,
         * ami a kaocsolódáskezelő ablakon jelenik meg.
         */
        private final Icon ICON;
        
        /**
         * A szótár alapján adja meg a szöveget.
         */
        public String text() {
            return getString(name().toLowerCase());
        }
        
        /**
         * Legyártja a kapcsolódáskezelő ablakhoz a paneleket.
         */
        private static IconTextPanel[] createPanels() {
            Status[] values = Status.values();
            IconTextPanel[] panels = new IconTextPanel[values.length];
            for (int i = 0; i < panels.length; i++) {
                panels[i] = new ConnProgPanel(values[i].ICON, values[i].text());
            }
            return panels;
        }
        
    }
    
    /**
     * Az ablakon megjelenő panelek belőle származnak.
     */
    private static class ConnProgPanel extends IconTextPanel {

        public ConnProgPanel(Icon icon, String text) {
            super(icon, text);
            setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // alsó és felső margó 5 pixel
        }
        
    }
    
    /**
     * Az ablakon ezek a panelek jelenhetnek meg.
     */
    private static final IconTextPanel[] PANELS = Status.createPanels();
    
    /**
     * Megadja, mi történjen a kilépés gombra kattintva.
     */
    private final Runnable ON_EXIT;
    
    /**
     * Beállítja a kis autó ikont és az indikátor animációt.
     * @param onExit megadja, mi történjen a kilépés gombra kattintva
     */
    public ConnectionProgressFrame(Runnable onExit) {
        super(getString("connection_handler"), getString("reconnect"), getString("connection_settings"), getString("exit"), PANELS);
        setIconImage(R.getClientImage());
        ON_EXIT = onExit;
    }

    /**
     * A kilépéskor lefuttatja a konstruktorban átadott Runnable objektumot.
     */
    @Override
    protected void onExit() {
        if (ON_EXIT != null) ON_EXIT.run();
        else super.onExit();
    }

    /**
     * A felület feliratait újra beállítja.
     * Ha a nyelvet megváltoztatja a felhasználó, ez a metódus hívódik meg.
     */
    @Override
    public void relocalize() {
        setTitle(getString("connection_handler"));
        setExitText(getString("exit"));
        setTryAgainText(getString("reconnect"));
        setConnectionSettingsText(getString("connection_settings"));
        Status[] sa = Status.values();
        for (int i = 0; i < PANELS.length; i++) {
            PANELS[i].setText(sa[i].text());
        }
    }
    
    /**
     * Beállítja a megjelenő panelt és az Újra gombot.
     * Az Újra gomb tiltva lesz {@code Status.CONNECTING} státusz esetén.
     * Ha nincs megadva státusz, az ablak elrejtődik.
     * @param status a kapcsolat egyik állapota
     * @param miReconn újrakapcsolódás opció a rendszerikon menüjében
     */
    public void setStatus(Status status, MenuItem miReconn) {
        if (status != null) {
            boolean enabled = status != Status.CONNECTING;
            if (miReconn != null) miReconn.setEnabled(enabled);
            setAgainButtonEnabled(enabled);
            setIconTextPanel(status.ordinal());
        }
        else {
            if (miReconn != null) miReconn.setEnabled(true);
        }
        setVisible(status != null);
    }
    
    /**
     * Akkor hívódik meg, amikor az Újra gombot kiválasztják.
     */
    @Override
    protected void onAgain() {
        Main.runClient(true);
    }

    /**
     * Akkor hívódik meg, amikor az Beállítások gombot kiválasztják.
     */
    @Override
    protected void onSettings() {
        Main.showSettingFrame(false, 0);
    }
    
}
