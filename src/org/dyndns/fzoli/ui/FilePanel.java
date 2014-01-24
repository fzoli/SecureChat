package org.dyndns.fzoli.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

/**
 * Fájl megjelenítő és kiválasztó panel.
 * @author zoli
 */
public class FilePanel extends JPanel {

    /**
     * Kulcs a lokalizált szöveghez.
     */
    private static final String KEY_SEARCH = "FilePanel.searchButtonText";
    
    /**
     * A lokalizáció beállítása után frissíteni kell a már létrejött panelek feliratait,
     * erre a célra szolgál ez a lista.
     */
    private static final List<FilePanel> PANELS = new ArrayList<FilePanel>();
    
    /**
     * Beállítja az angol elnevezéseket a fájl tallózóhoz, ha még nincs beállítva.
     */
    static {
        UIUtil.init(KEY_SEARCH, "Search");
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
    }

    /**
     * Beállítja az erőforrás alapján a tallózó dialógus szövegeit.
     */
    public static void setResource(ResourceBundle b) {
        Enumeration<String> keys = b.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if ((key.startsWith("FileChooser.") || key.startsWith("FilePanel.")) && !key.equals("FileChooser.readOnly")) {
                String value = b.getString(key);
                if (value != null) UIManager.put(key, value);
            }
        }
        for (FilePanel panel : PANELS) {
            panel.btSearch.setText(UIManager.getString(KEY_SEARCH));
        }
    }

    /**
     * Fájl tallózás eseményfigyelő.
     * Ha a tallózásra kattintottak, fájlkereső ablak jelenik meg.
     * A fájl kiválasztása után, beállítja a kiválasztott fájlt.
     * Ha van már fájl beállítva, a keresőablak kijelöli a fájlt megjelenésekor.
     * Ha nincs fájl beállítva még, de már más panelen állítottak be, akkor
     * az utolsó kiválasztott fájl könyvtárából indul a keresés.
     */
    private final ActionListener alSearch = new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            String openText = UIManager.getString("FileChooser.openDialogTitleText");
            fc.setDialogTitle(openText == null ? "Open" : openText);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setAcceptAllFileFilterUsed(fileFilter == null);
            fc.setMultiSelectionEnabled(false);
            fc.setFileHidingEnabled(true);
            if (file == null) {
                if (lastFile != null) {
                    fc.setCurrentDirectory(lastFile);
                }
            }
            else {
                fc.setSelectedFile(file);
                fc.setCurrentDirectory(file);
            }
            if (fileFilter != null) {
                fc.setFileFilter(fileFilter);
            }
            if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(PARENT)) {
                setFile(fc.getSelectedFile());
            }
        }

    };
    
    /**
     * Szülő komponens.
     */
    private final Component PARENT;

    /**
     * A szöveget megjelenítő címke.
     */
    private final JLabel LB_TEXT;
    
    /**
     * Tallózás gomb.
     * Megjeleníti a fájlkeresőt.
     */
    private final JButton btSearch = new JButton(UIManager.getString(KEY_SEARCH));
    
    /**
     * A kiválasztott fájl útvonalát jeleníti meg.
     */
    private final JTextField tfFile = new JTextField(10);

    /**
     * A panelen megjelenített fájl.
     */
    private File file;
    
    /**
     * Az utóljára kiválasztott fájl bármelyik panelen.
     */
    private static File lastFile;
    
    /**
     * Fájl szűrő.
     * A megjelenő fájlkereső ablakok szűrője.
     */
    private FileFilter fileFilter;
    
    /**
     * Konstruktor.
     * Megjeleníti a fejlécet, a fájlútvonal-mutatót és a tallózó gombot.
     * @param text a fájlválasztó fejléce
     */
    public FilePanel(Component parent, String text) {
        super(new GridBagLayout());
        PANELS.add(this);
        PARENT = parent;
        setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 2;
        LB_TEXT = new JLabel("<html>" + text + ":</html>");
        add(LB_TEXT, c);
        c.weightx = 1;
        c.gridy = 1;
        add(tfFile, c);
        c.gridx = 1;
        add(btSearch, c);
        tfFile.setEditable(false);
        tfFile.setFocusable(false);
        btSearch.addActionListener(alSearch);
    }
    
    /**
     * A szöveget cseréli le.
     */
    public void setText(String text) {
        LB_TEXT.setText("<html>" + text + ":</html>");
    }
    
    /**
     * A megjelenített/beállított fájlt adja vissza.
     */
    public File getFile() {
        return file;
    }

    /**
     * Beállítja a fájlt és megjeleníti.
     * A fájl csak akkor változik, ha a megadott fájl létezik és nem könyvtár,
     * valamint ha van fájlszűrő, megfelel a szűrőfeltételnek.
     */
    public void setFile(File file) {
        if (file != null && !file.isFile()) return;
        lastFile = file;
        if (fileFilter != null && !fileFilter.accept(file)) return;
        this.file = file;
        tfFile.setText(file == null ? "" : file.getName());
    }

    /**
     * A tallózáskor megjelenő fájlkereső ablak szűrőjét állítja be.
     */
    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

}