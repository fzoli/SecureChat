package org.dyndns.fzoli.chat.client.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.ParseException;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.dyndns.fzoli.chat.client.Config;
import org.dyndns.fzoli.chat.client.Main;
import static org.dyndns.fzoli.chat.client.Main.getString;
import static org.dyndns.fzoli.chat.client.Main.runClient;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.ui.FilePanel;
import org.dyndns.fzoli.ui.FrontFrame;
import org.dyndns.fzoli.ui.OkCancelPanel;
import org.dyndns.fzoli.ui.OptionPane;
import org.dyndns.fzoli.ui.RegexPatternFormatter;
import org.dyndns.fzoli.ui.RelocalizableWindow;
import org.dyndns.fzoli.util.Folders;

/**
 * A kliens konfigurációját beállító dialógusablak.
 * @author zoli
 */
public class ConfigEditorFrame extends FrontFrame implements RelocalizableWindow, OkCancelPanel.OkCancelWindow {
    
    /**
     * A dialógusablak lapfüleinek tartalma ebbe a panelbe kerül bele.
     * Mindegyik panel átlátszó.
     */
    private static class ConfigPanel extends JPanel {
        
        public ConfigPanel() {
            setOpaque(false);
        }
        
        /**
         * Címkét gyárt a panelhez.
         * @param text a címke szövege
         */
        protected JLabel createLabel(final String text) {
            return new JLabel(createLabelText(text)) {

                /**
                 * A kívánt szélesség generálásában segít.
                 */
                private JLabel lbSizer = createSizer();
                
                /**
                 * Miután módosult a szövege a címkének,
                 * újragenerálja a szélességet megadó címkét.
                 */
                @Override
                public void setText(String text) {
                    super.setText(text);
                    lbSizer = createSizer();
                }
                
                /**
                 * A kívánt szélesség 180 pixel körül van, a magasság változatlan.
                 */
                @Override
                public Dimension getPreferredSize() {
                    Dimension d1 = super.getPreferredSize();
                    Dimension d2 = lbSizer.getPreferredSize();
                    return new Dimension(Math.min(d1.width, d2.width), d1.height);
                }
                
                /**
                 * Gyárt egy címként, ami szélességében limitálva van 180 pixelre.
                 * A címke ezt a panelt használja kívánt szélességének megadására.
                 */
                private JLabel createSizer() {
                    return new JLabel("<html><body style=\"width: 180px\">" + text + "</body></html>");
                }
                
            };
        }
        
    }
    
    /**
     * Az ablakhoz tartozó fájl tallózó panelek osztálya.
     */
    private static class ConfigFilePanel extends FilePanel {

        /**
         * Az a könyvtár, ahonnan a programot indították.
         */
        private final String currentDir = new File(System.getProperty("user.dir")).getAbsolutePath();
        
        /**
         * Konstruktor.
         * Megjeleníti a fejlécet, a fájlútvonal-mutatót és a tallózó gombot.
         * @param text a fájlválasztó fejléce
         */
        public ConfigFilePanel(Component parent, String text) {
            super(parent, text);
        }

        /**
         * A megjelenített/beállított fájlt adja vissza.
         * Ha a fájlra lehet relatív útvonallal is hivatkozni, akkor a relatív útvonallal tér vissza.
         */
        @Override
        public File getFile() {
            File file = super.getFile(); // az eredeti fájl
            try {
                final String absFileName = file.getAbsolutePath(); // az eredeti fájl teljes útvonala
                String root = currentDir; // "cd" könyvtár
                if (!absFileName.startsWith(root) && Folders.getSourceDir() != null) { // ha nem a "cd" könyvtárban van a fájl
                    root = Folders.getSourceDir().getAbsolutePath(); // "cd" helyett forrás könyvtár
                }
                if (absFileName.startsWith(root)) { // ha a fájl a "cd" vagy a forrás könyvtáron belül van
                    final int rootLength = root.length(); // a "cd"/forrás könyvtár hossza
                    final String relFileName = absFileName.substring(rootLength + 1); // a relatív útvonal
                    file = new File(relFileName); // relatív útvonallal gyártott fájl
                }
            }
            catch (Exception ex) { // ha a fájl null vagy fájlrendszer hiba
                ;
            }
            return file;
        }
    }
    
    /**
     * Súgó ablak.
     */
    private final ConfigHelpDialog DIALOG_HELP = new ConfigHelpDialog(this);
    
    /**
     * Crt fájlszűrő.
     */
    private FileNameExtensionFilter fnefCrt = createCertificateFilter("certificate", "crt");
    
    /**
     * Key fájlszűrő.
     */
    private FileNameExtensionFilter fnefKey = createCertificateFilter("certificate_key", "key");
    
    /**
     * IP címre és hosztnévre és egyéb egyedi címekre is egész jól használható reguláris kifejezés.
     */
    private static final Pattern PT_ADDRESS = Pattern.compile("^[a-z\\d]{1}[\\w\\.\\-\\d]{0,18}[a-z\\d]{1}$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Port validálására használt reguláris kifejezés.
     * Minimum 1 és maximum 5 karakter, csak szám.
     */
    private static final Pattern PT_PORT = Pattern.compile("^[1-9]{1}[\\d]{0,4}$", Pattern.CASE_INSENSITIVE);
    
    /**
     * A konfiguráció, amit használ az ablak.
     */
    private final Config CONFIG;
    
    /**
     * Magyarázó szöveget megjelenítő címke.
     */
    private JLabel lbAddressSum, lbPasswordResetSum, lbLanguageChooserSum;
    
    /**
     * Címke egy szerver paraméterhez.
     */
    private JLabel lbServerAddress, lbServerPort;
    
    /**
     * Jelszótörlő gomb.
     */
    private final JButton BT_PASSWORD_RESET = new JButton(getString("delete")) {
        
        {
            // kattintáskor a jelszó törlése a konfigurációból és a gomb letiltása
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    setEnabled(false);
                    CONFIG.setPassword(null, true);
                    Config.save(CONFIG);
                }
                
            });
        }

        @Override
        public Dimension getPreferredSize() {
            // a szükséges helynél 30 pixellel szélesebb méret
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 30, d.height);
        }
        
    };
    
    /**
     * A szerver címe írható át benne.
     */
    private final JTextField TF_ADDRESS = new JFormattedTextField(createAddressFormatter());
    
    /**
     * A szerver portja írható át benne.
     */
    private final JTextField TF_PORT = new JFormattedTextField(createPortFormatter());
    
    /**
     * A kiállító fájl tallózó panele.
     */
    private final FilePanel FP_CA = new ConfigFilePanel(this, getString("certifier")) {
        {
            setFileFilter(fnefCrt);
        }
    };
    
    /**
     * A tanúsítvány fájl tallózó panele.
     */
    private final FilePanel FP_CERT = new ConfigFilePanel(this, getString("certificate")) {
        {
            setFileFilter(fnefCrt);
        }
    };
    
    /**
     * A tanúsítvány kulcs-fájl tallózó panele.
     */
    private final FilePanel FP_KEY = new ConfigFilePanel(this, getString("key")) {
        {
            setFileFilter(fnefKey);
        }
    };
    
    /**
     * Az ablak bezárásakor lefutó eseménykezelő.
     * Meghívja az {@code onClosing} metódust.
     */
    private final WindowAdapter WL_CLOSE = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            onClosing();
        }
        
    };
    
    /**
     * Erre a gombra kattintva a konfiguráció elmentődik és bezárul az ablak.
     * De csak akkor, ha érvényesek a beállítások.
     */
    private final JButton BT_OK = new JButton(getString("ok")) {
        {
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (checkConfig()) saveConfig();
                }
                
            });
        }
    };
    
    /**
     * Erre a gombra kattintva bezárul az ablak, a konfiguráció nem változik.
     */
    private final JButton BT_CANCEL = new JButton(getString("cancel")) {
        {
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    unsaveConfig();
                }
                
            });
        }
    };
    
    /**
     * Erre a gombra kattintva előjön a súgó.
     */
    private final JButton BT_HELP = new JButton(getString("help")) {
        {
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    BT_HELP.setEnabled(false);
                    DIALOG_HELP.setLocationRelativeTo(ConfigEditorFrame.this);
                    DIALOG_HELP.setVisible(true);
                }
                
            });
            DIALOG_HELP.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    BT_HELP.setEnabled(true);
                }
                
            });
        }
    };
    
    /**
     * Ezen a panelen állítható be a szerver elérési útvonala.
     */
    private final JPanel PANEL_ADDRESS = new ConfigPanel() {
        {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weighty = 1; // teljes helylefoglalás hosszúságban
            c.insets = new Insets(5, 5, 5, 5); // 5 pixeles margó
            c.fill = GridBagConstraints.HORIZONTAL; // teljes helykitöltés horizontálisan (sorkitöltés)
            
            c.gridx = 1; // első oszlop
            c.weightx = 0; // csak annyit foglal, amennyit kell
            
            c.gridy = 0; // nulladik sor
            c.gridwidth = 2; // két oszlopot foglal el a magyarázat
            lbAddressSum = createLabel(getString("sum_address"));
            add(lbAddressSum, c);
            c.gridwidth = 1; // a többi elem egy oszlopot foglal el
            
            c.gridy = 1; // első sor (1, 1)
            lbServerAddress = new JLabel(getString("server_address") + ':');
            add(lbServerAddress, c);
            
            c.gridy = 2; // második sor (1, 2)
            lbServerPort = new JLabel(getString("server_port") + ':');
            add(lbServerPort, c);
            
            c.gridx = 2; // második oszlop
            c.weightx = 1; // kitölti a maradék helyet
            
            c.gridy = 1; // első sor (2, 1)
            add(TF_ADDRESS, c);
            
            c.gridy = 2; // második sor (2, 2)
            add(TF_PORT, c);
        }
    };
    
    /**
     * Erről a panelről érhető el a jelszó törlésére használható gomb.
     */
    private final JPanel PANEL_PASSWORD_RESET = new ConfigPanel() {
        {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1; // teljes helyfoglalás szélességében
            c.weighty = 1; // teljes helylefoglalás hosszúságban
            
            c.insets = new Insets(5, 5, 5, 5); // 5 pixeles margó
            c.fill = GridBagConstraints.HORIZONTAL; // teljes helykitöltés horizontálisan (sorkitöltés)
            lbPasswordResetSum = createLabel(getString("sum_password_reset"));
            add(lbPasswordResetSum, c); // üzenet hozzáadása a panelhez
            
            c.gridy = 1; // a szöveg alá kerül a törlés gomb
            c.insets = new Insets(0, 5, 5, 5); // 5 pixeles margó mindenhol, kivéve felül
            c.fill = GridBagConstraints.NONE; // csak akkora helyet foglal, amennyire szüksége van
            add(BT_PASSWORD_RESET, c);
        }
    };
    
    /**
     * Erről a panelről érhető el a nyelv módosítására használható legördülő lista.
     */
    private final JPanel PANEL_LANGUAGE_CHOOSER = new ConfigPanel() {
        {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1; // teljes helyfoglalás szélességében
            c.weighty = 1; // teljes helylefoglalás hosszúságban
            
            c.insets = new Insets(5, 5, 5, 5); // 5 pixeles margó
            c.fill = GridBagConstraints.HORIZONTAL; // teljes helykitöltés horizontálisan (sorkitöltés)
            lbLanguageChooserSum = createLabel(getString("sum_language_chooser"));
            add(lbLanguageChooserSum, c); // üzenet hozzáadása a panelhez
            
            c.gridy = 1; // a szöveg alá kerül a törlés gomb
            c.insets = new Insets(0, 5, 5, 5); // 5 pixeles margó mindenhol, kivéve felül
            c.fill = GridBagConstraints.NONE; // csak akkora helyet foglal, amennyire szüksége van
            add(new JComboBox(Main.LNG_FRAME.MODEL_LOCALES) { // ugyan azzal a modellel jön létre a legördülő lista, amivel a nyelvkiválasztó ablak, így egyszerre módosulnak
                {
                    setRenderer(Main.LNG_FRAME.LCR_LOCALES); // ugyan azt a renderert használja, mint a nyelvkiválasztó ablak
                    setKeySelectionManager(Main.LNG_FRAME.createKeySelectionManager(this)); // gyorskereső létrehozása
                    setPreferredSize(new Dimension(Math.max(getPreferredSize().width, 180), getPreferredSize().height)); // 180 pixel széles (vagy nagyobb, ha kell)
                }
            }, c);
        }
    };
    
    /**
     * Ezen a panelen állítható be a kapcsolathoz használt tanúsítvány.
     */
    private final JPanel PANEL_CERTIFICATE = new ConfigPanel() {
        {
            setLayout(new GridLayout(3, 1));
            add(FP_CA);
            add(FP_CERT);
            add(FP_KEY);
        }
    };
    
    /**
     * Az ablakot teljes egészében kitöltő lapfüles panel.
     */
    private final JTabbedPane TABBED_PANE = new JTabbedPane() {
        {
            addTab(getString("path"), PANEL_ADDRESS);
            addTab(getString("security"), PANEL_CERTIFICATE);
            addTab(getString("password"), PANEL_PASSWORD_RESET);
            addTab(getString("language"), PANEL_LANGUAGE_CHOOSER);
        }
    };
    
    /**
     * Helyes konfiguráció beállítás kényszerítése.
     * Kezdetben inaktív.
     */
    private boolean force = false;
    
    /**
     * Az ablak megjelenésekor aktuális konfiguráció.
     * Ahhoz kell, hogy meg lehessen tudni, módosult-e a konfiguráció az ablak megjelenése óta.
     */
    private Config previousConfig;
    
    /**
     * Konstruktor.
     * @param config konfiguráció, amit használ az ablak.
     * @param wl eseménykezelő, ami előhívja a figyelmeztetéseket az ablak bezárásakor
     */
    public ConfigEditorFrame(Config config, WindowListener wl) {
        CONFIG = config;
        addWindowListener(wl);
        initComponents();
        initFrame();
    }
    
    /**
     * Inicializálja az ablakot.
     */
    private void initFrame() {
        setTitle(getString("connection_settings")); // címsor szöveg beállítása
        setIconImage(R.getClientImage()); // címsor ikon beállítása
        setLayout(new GridBagLayout()); // elrendezésmenedzser megadása
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.BOTH; // mindkét irányban helykitöltés
        c.weightx = 1; // helyfoglalás szélességében ...
        c.weighty = 1; // ... és hosszúságában is
        add(TABBED_PANE, c); // lapfül panel hozzáadása
        
        c.gridy = 1; // következő sor
        c.weighty = 0; // minimális helyfoglalás magasságban ...
        c.fill = GridBagConstraints.HORIZONTAL; // ... és teljes szélesség elfoglalása ...
        JPanel pButton = new OkCancelPanel(this, BT_OK, BT_CANCEL, BT_HELP, 5); // ... a gombokat tartalmazó panelnek
        pButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5)); // margó a gombokat tartalmazó panelre
        add(pButton, c); // gombok hozzáadása az ablakhoz
        
        pack(); // legkisebb méretre állítás ...
        setMinimumSize(getSize()); // ... és ennél a méretnél csak nagyobb lehet az ablak
        setMaximumSize(new Dimension(1024, getSize().height)); // Java 1.7.0_07 még mindig bugos, de egyszer csak menni fog
        setLocationRelativeTo(this); // képernyő közepére igazítás
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // az alapértelmezett bezárás tiltása
        addWindowListener(WL_CLOSE); // bezáráskor saját metódus hívódik meg
    }
    
    /**
     * Inicializálja a komponenseket.
     */
    private void initComponents() {
        TABBED_PANE.setFocusable(false); // zavaró kijelölés jelzés leszedése
        TABBED_PANE.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 5 x 5 pixeles margó
        
        final KeyAdapter klEnter = new KeyAdapter() { // szerkeszthető mezőben enter lenyomására ugyan az történik, mint ha az OK gombra kattintanának, ha jók a beállítások

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
                    if (isConfigValid()) saveConfig(); // ha a konfiguráció helyes, mentés és ablak bezárása
                }
            }

        };
        
        TF_ADDRESS.addKeyListener(klEnter);
        TF_PORT.addKeyListener(klEnter);
    }

    /**
     * A felület feliratait újra beállítja.
     * Ha a nyelvet megváltoztatja a felhasználó, ez a metódus hívódik meg.
     */
    @Override
    public void relocalize() {
        setTitle(getString("connection_settings"));
        TABBED_PANE.setTitleAt(0, getString("path"));
        TABBED_PANE.setTitleAt(1, getString("security"));
        TABBED_PANE.setTitleAt(2, getString("password"));
        TABBED_PANE.setTitleAt(3, getString("language"));
        BT_OK.setText(getString("ok"));
        BT_CANCEL.setText(getString(force ? "exit" : "cancel"));
        BT_HELP.setText(getString("help"));
        BT_PASSWORD_RESET.setText(getString("delete"));
        DIALOG_HELP.relocalize();
        lbServerAddress.setText(getString("server_address") + ':');
        lbServerPort.setText(getString("server_port") + ':');
        fnefCrt = createCertificateFilter("certificate", "crt");
        fnefKey = createCertificateFilter("certificate_key", "key");
        FP_CA.setFileFilter(fnefCrt);
        FP_CERT.setFileFilter(fnefCrt);
        FP_KEY.setFileFilter(fnefKey);
        FP_CA.setText(getString("certifier"));
        FP_CERT.setText(getString("certificate"));
        FP_KEY.setText(getString("key"));
        lbAddressSum.setText(createSummaryText("sum_address"));
        lbPasswordResetSum.setText(createSummaryText("sum_password_reset"));
        lbLanguageChooserSum.setText(createSummaryText("sum_language_chooser"));
        Dimension d = getPreferredSize();
        setMinimumSize(d);
        if (d.height > getHeight()) {
            setSize(getWidth(), d.height);
        }
    }
    
    /**
     * Megadja, hogy a helyes konfiguráció beállítása kényszerítve van-e.
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Beállítja, hogy a helyes konfiguráció beállítása kényszerítve legyen-e.
     * @param force true esetén kényszerített
     */
    public void setForce(boolean force) {
        if (this.force != force) {
            this.force = force;
            BT_CANCEL.setText(getString(force ? "exit" : "cancel"));
        }
    }
    
    /**
     * Betölti a konfigurációt a felület elemeibe.
     */
    private void loadConfig() {
        previousConfig = Config.getInstance();
        TF_ADDRESS.setText(CONFIG.getAddress());
        TF_PORT.setText(Integer.toString(CONFIG.getPort()));
        FP_CA.setFile(CONFIG.getCAFile());
        FP_CERT.setFile(CONFIG.getCertFile());
        FP_KEY.setFile(CONFIG.getKeyFile());
        BT_PASSWORD_RESET.setEnabled(CONFIG.isPasswordStored());
    }
    
    /**
     * Elmenti a konfigurációt.
     * Ha sikerült a mentés, bezárja az ablakot, egyébként figyelmezteti a felhasználót.
     * Ha van már kialakított kapcsolat a szerverrel és sikerült a mentés,
     * megkérdi, akar-e a felhasználó újrakapcsolódni a szerverhez az új beállításokkal.
     */
    private void saveConfig() {
        CONFIG.setAddress(TF_ADDRESS.getText());
        CONFIG.setCAFile(FP_CA.getFile());
        CONFIG.setCertFile(FP_CERT.getFile());
        CONFIG.setKeyFile(FP_KEY.getFile());
        CONFIG.setPort(Integer.parseInt(TF_PORT.getText()));
        if (Config.save(CONFIG)) {
            if (!previousConfig.equals(CONFIG) && Main.isConnected()) {
                int answer = OptionPane.showYesNoDialog(ConfigEditorFrame.this, getString("reconnect_ask"), getString("reconnect"));
                setVisible(false);
                if (answer == 0) Main.reconnect();
            }
            else {
                setVisible(false);
            }
        }
        else {
            OptionPane.showWarningDialog(R.getClientImage(), getString("saveError"), getString("warning"));
        }
    }
    
    /**
     * Bezárja az ablakot a konfiguráció mentése nélkül.
     * Ha nem megfelelő az érvényben lévő konfiguráció, de meg van követelve a helyes konfiguráció, a program leáll.
     * Ha a nyelv módosult miközben az ablak látható volt, az ablak megjelenése előtti nyelvre vált vissza a program.
     */
    private void unsaveConfig() {
        if (isForce() && !CONFIG.isFileExists()) System.exit(0);
        if (!previousConfig.getLanguage().equals(CONFIG.getLanguage())) {
            Main.setLanguage(previousConfig.getLanguage());
        }
        setVisible(false);
    }
    
    /**
     * Megadja, érvényes-e az aktuális beállítás.
     * Érvényes, ha mindhárom fájl be van állítva és mindkét bemenet megfelel a reguláris kifejezésüknek.
     */
    private boolean isConfigValid() {
        return isConfigValid(TF_ADDRESS.getText(), TF_PORT.getText(), FP_CA.getFile(), FP_CERT.getFile(), FP_KEY.getFile());
    }
    
    /**
     * Megadja, érvényes-e a paraméterben megadott beállítás.
     * Érvényes, ha mindhárom fájl be van állítva és mindkét bemenet megfelel a reguláris kifejezésüknek.
     */
    private static boolean isConfigValid(String address, String port, File ca, File cert, File key) {
        return PT_ADDRESS.matcher(address).matches() &&
               PT_PORT.matcher(port).matches() &&
               ca != null &&
               cert != null &&
               key != null;
    }
    
    /**
     * A cím maszkolására hoz létre egy formázó objektumot.
     */
    private AbstractFormatter createAddressFormatter() {
        RegexPatternFormatter fmAddress = new RegexPatternFormatter(PT_ADDRESS) {

            private final String[] WRONG_STRINGS = {"..", "--", "-.", ".-"};
            
            @Override
            public Object stringToValue(String string) throws ParseException {
                // egynél több pont vagy kötőjel ill. kombinációjuk nincs megengedve
                for (String ws : WRONG_STRINGS) {
                    if (string.contains(ws)) throw new ParseException("wrong string", 0);
                }
                // ha a szöveg pontra végződik vagy rövidebb két karakternél, az eredeti szöveg kerül a helyére a szerkesztés befejezésekor
                if (string.length() < 2 || string.endsWith(".") || string.endsWith("-")) return CONFIG.getAddress();
                return ((String)super.stringToValue(string)).toLowerCase(); // a szerkesztés befejezésekor minden karaktert kicsire cserél
            }
            
        };
        fmAddress.setAllowsInvalid(false); // nem engedi meg a nem megfelelő értékek beírását
        return fmAddress;
    }
    
    /**
     * A port maszkolására hoz létre egy formázó objektumot.
     */
    private AbstractFormatter createPortFormatter() {
        RegexPatternFormatter fmPort = new RegexPatternFormatter(PT_PORT) {

            @Override
            public Object stringToValue(String string) throws ParseException {
                try {
                    // ha a szöveg rövidebb 1 karakternél, az eredeti szöveg kerül a helyére a szerkesztés befejezésekor
                    if (string.length() < 1) return CONFIG.getPort();
                    // ha a szöveg nem alakítható egész számmá vagy az intervallumon kívül esik, kivételt keletkezik...
                    int number = Integer.parseInt(string); // ... itt
                    if (number < 1 || number > 65535) throw new Exception(); // ... vagy itt
                }
                catch (Exception ex) {
                    // ParseException kivétel dobása, hogy nem megfelelő az érték
                    throw new ParseException("invalid port", 0);
                }
                // ha eddig nem dobódott kivétel, még a regex kifejezés dobhat kivételt és ha dob, nem frissül a szöveg
                return super.stringToValue(string);
            }
            
        };
        fmPort.setAllowsInvalid(false); // nem engedi meg a nem megfelelő értékek beírását
        return fmPort;
    }

    /**
     * Megjeleníti vagy elrejti az ablakot.
     * Ha megjelenést kértek, előtérbe kerül az ablak.
     * A konfiguráció frissül az ablak megjelenésekor.
     * Az ablak elrejtése után elindul a kliens, ha elindítható és még nem fut.
     */
    @Override
    public void setVisible(boolean b) {
        if (b && !isVisible()) loadConfig();
        if (b) {
            toFront();
        }
        else {
            DIALOG_HELP.setVisible(false);
            BT_HELP.setEnabled(true);
        }
        super.setVisible(b);
        if (!b) runClient(true);
    }

    /**
     * Megadja, hogy a gombok szövegének módosulása után legyen-e ablak újraméretezés.
     * @return ha a régi szélesség nem elég az új szövegeknek, akkor true, egyébként false
     */
    @Override
    public boolean needRepack(Rectangle r) {
        return getPreferredSize().width > r.width;
    }
    
    /**
     * Megadja, hogy az átméretezés után a magasság legyen-e újra a régi.
     * @return ha a régi magasság nagyobb az új magasságnál, akkor true, egyébként false
     */
    @Override
    public boolean restoreHeight(Rectangle r) {
        return r.height > getPreferredSize().height;
    }
    
    /**
     * Megadja, hogy az átméretezés után legyen-e ablak újrapozícionálás.
     * @return ha 15 pixel pontossággal a képernyő közepén van a téglalap, akkor true, egyébként false
     */
    @Override
    public boolean needReloc(Rectangle r) {
        return OkCancelPanel.isNearCenter(r);
    }
    
    /**
     * Beállítja, melyik lapfül legyen előtérben.
     * @param tabIndex az előtérbe kerülő lapfül indexe
     */
    public void setTabIndex(Integer tabIndex) {
        if (tabIndex != null) TABBED_PANE.setSelectedIndex(tabIndex);
    }
    
    /**
     * Ha a beállítások nem érvényesek, figyelmezteti a felhasználót.
     * @return true, ha érvényesek a beállítások, egyébként false
     */
    private boolean checkConfig() {
        if (!isConfigValid()) {
            String[] opts = new String[] {getString("ok"), getString("exit")};
            int sel = OptionPane.showOptionDialog(this, getString("wrong_settings"), getString("warning"), OptionPane.NO_OPTION, OptionPane.WARNING_MESSAGE, null, opts, opts[0]);
            if (sel != 0) unsaveConfig();
            return false;
        }
        return true;
    }

    /**
     * Az ablak bezárásakor ha módosult a konfiguráció és nincs mentve,
     * megkérdi, akarja-e menteni, egyébként biztos, hogy nincs mentés.
     * Ha a konfiguráció nem érvényes, figyelmezteti a felhasználót és nem csinál semmit.
     */
    private void onClosing() {
        if (!checkConfig()) return; // ha a beállítás nem érvényes figyelmeztetés és semmittevés
        getContentPane().requestFocus(); // fókusz átadása az ablaknak, hogy biztosan minden szerkesztés végetérjen
        if (CONFIG.equals(TF_ADDRESS.getText(), Integer.parseInt(TF_PORT.getText()), FP_CA.getFile(), FP_CERT.getFile(), FP_KEY.getFile())) {
            unsaveConfig(); // a beállítások nem változtak, nincs mentés
        }
        else {
            // a beállítások megváltoztak, legyen mentés?
            String[] opts = new String[] {getString("yes"), getString("no"), getString("cancel")}; // az alapértelmezett opció a Mégse
            int sel = OptionPane.showOptionDialog(this, getString("save_ask"), getTitle(), OptionPane.NO_OPTION, OptionPane.QUESTION_MESSAGE, null, opts, opts[2]);
            switch (sel) {
                case 0: // Igen, legyen mentés
                    saveConfig();
                    break;
                case 1: // Nem, ne legyen mentés
                    unsaveConfig();
                    break;
                case 2: // Mégse, semmittevés
                    ;
            }
        }
    }
    
    /**
     * HTML tegek közé teszi a szöveget.
     */
    private static String createLabelText(String text) {
        return "<html>" + text + "</html>";
    }
    
    /**
     * Szöveget gyárt a magyarázat címkékhez a szótár kód alapján.
     */
    private static String createSummaryText(String key) {
        return "<html>" + getString(key) + "</html>";
    }
    
    /**
     * Fájlszűrőt hoz létre.
     * @param textKey a szöveghez tartozó kulcs a szótárban
     * @param ext a kiterjesztés, amire szűrni kell
     */
    private static FileNameExtensionFilter createCertificateFilter(String textKey, String ext) {
        return new FileNameExtensionFilter(getString(textKey) + " (*." + ext + ")", new String[] {ext});
    }
    
}
