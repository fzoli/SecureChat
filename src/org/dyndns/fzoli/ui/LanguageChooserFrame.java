package org.dyndns.fzoli.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Nyelvkiválasztó ablak.
 * A {@code ResourceBundle} osztályhoz használt {@code properties} fájlok listája alapján megjeleníti az elérhető nyelveket egy lenyíló listában.
 * Ha a nyelvet módosították, a {@link #onLanguageSelected(java.util.Locale)} metódus hívódik meg, amit implementálni kell az utódokban.
 * @author zoli
 */
public abstract class LanguageChooserFrame extends JFrame {

    /**
     * A lenyíló listához gyorskereső szöveg.
     */
    private String text = "";
    
    /**
     * Az utóljára kijelölt nyelv.
     */
    private Object lastSelection;public static final String salala = "asas";
    
    /**
     * Megadja, hogy inicializálódott-e már az ablak.
     */
    private boolean loaded = false;
    
    /**
     * Az elérhető nyelvek és a hozzájuk tartozó feliratok.
     */
    private final Map<Locale, String> MAP_LOCALES;
    
    /**
     * A legördülő lista modelje.
     */
    public final DefaultComboBoxModel<Locale> MODEL_LOCALES = new DefaultComboBoxModel<Locale>();
    
    /**
     * A legördülő lista kinézetét szabályozó objektum.
     */
    public final DefaultListCellRenderer LCR_LOCALES = new DefaultListCellRenderer() {

        /**
         * A felsorolás elemei nagy betűvel kezdődnek és a {@link Locale} nyelvén jelennek meg.
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, getLocaleDisplayLanguage((Locale) value), index, isSelected, cellHasFocus);
        }
        
    };
    
    /**
     * A legördülő lista gyorskeresés támogatással.
     */
    private final JComboBox<Locale> CB_LOCALES = new JComboBox<Locale>(MODEL_LOCALES) {
        {
            setRenderer(LCR_LOCALES);
            setKeySelectionManager(createKeySelectionManager(this));
            addActionListener(new ActionListener() {

                /**
                 * Ha kijelölés történt, meghívja az eseményfeldolgozó metódust,
                 * de csak akkor, ha nem ugyan azt jelölték ki, mint ami előtte ki volt jelölve
                 * és ha az inicializálás befejeződött.
                 */
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!loaded) return;
                    Locale selection = (Locale) CB_LOCALES.getSelectedItem();
                    if (lastSelection == null || selection != lastSelection) {
                        onLanguageSelected(selection);
                        lastSelection = selection;
                    }
                }
                
            });
        }
    };
    
    /**
     * Konstruktor.
     * A {@code class} fájlok gyökérkönyvtárában keresi a {@code properties} fájlokat.
     * @param icon az ablak ikonja
     * @param name a {@code properties} fájlok kezdőneve (a ResourceBundle baseName paramétere)
     */
    public LanguageChooserFrame(Image icon, String name) {
        this(icon, "", name);
    }
    
    /**
     * Konstruktor.
     * A {@code class} fájlok gyökérkönyvtárában keresi a {@code properties} fájlokat.
     * @param icon az ablak ikonja
     * @param name a {@code properties} fájlok kezdőneve (a ResourceBundle baseName paramétere)
     * @param def az alapértelmezett nyelv
     * @param locales az elérhető nyelvek listája (ami bővülhet, ha talál még több nyelvet a program)
     */
    public LanguageChooserFrame(Image icon, String name, Locale def, Locale... locales) {
        this(icon, "", name, def, locales);
    }
    
    /**
     * Konstruktor.
     * A rendszer nyelve az alapértelmezett nyelv.
     * @param icon az ablak ikonja
     * @param pkg a csomag neve, ahol a {@code properties} fájlok vannak
     * @param name a {@code properties} fájlok kezdőneve (a ResourceBundle baseName paramétere)
     */
    public LanguageChooserFrame(Image icon, String pkg, String name) {
        this(icon, pkg, name, null);
    }
    
    /**
     * Konstruktor.
     * @param icon az ablak ikonja
     * @param pkg a csomag neve, ahol a {@code properties} fájlok vannak
     * @param name a {@code properties} fájlok kezdőneve (a ResourceBundle baseName paramétere)
     * @param def az alapértelmezett nyelv
     * @param locales az elérhető nyelvek listája (ami bővülhet, ha talál még több nyelvet a program)
     */
    public LanguageChooserFrame(Image icon, String pkg, String name, Locale def, Locale... locales) {
        super("Languages");
        
        // ha nincs megadva alapértelmezett nyelv, rendszernyelv használata
        def = def == null ? Locale.getDefault() : def;
        
        // nyelvek betöltése egy listába
        ArrayList<Locale> l = new ArrayList<Locale>();
        MAP_LOCALES = getBundleLanguages(pkg, name, locales);
        Iterator<Locale> it = MAP_LOCALES.keySet().iterator();
        while (it.hasNext()) {
            l.add(it.next());
        }
        
        // a feltöltött lista rendezése a megjelenő felirat szerint
        Collections.sort(l, new Comparator<Locale>() {

            @Override
            public int compare(Locale o1, Locale o2) {
                return getLocaleDisplayLanguage(o1).compareTo(getLocaleDisplayLanguage(o2));
            }
            
        });
        
        // a legördülő lista feltöltése
        for (Locale locale : l) {
            MODEL_LOCALES.addElement(locale);
        }
        
        // a lista kiürítése
        l.clear();
        
        // ha üres a lista, disabled lesz a komponens
        CB_LOCALES.setEnabled(MODEL_LOCALES.getSize() > 0);
        
        // ha a listában nem szerepelt a megadott érték, akkor a rendszernyelv használata
        if (indexOf(def) == -1) {
            def = Locale.getDefault();
            // ha a rendszernyelv se szerepel a listában, akkor az angol lesz a kezdőnyelv
            if (locales.length > 0 && indexOf(def) == -1) {
                def = MODEL_LOCALES.getElementAt(indexOf(Locale.ENGLISH));
            }
        }
        
        // a kezdőnyelv kijelölése
        setLanguage(def);
        
        // felület inicializálása
        setIconImage(icon);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 3, 3, 3);
        add(new JLabel("Available languages:"), c);
        c.gridy = 1;
        c.insets = new Insets(0, 3, 3, 3);
        add(CB_LOCALES, c);
        pack();
        if (getWidth() < 200) setSize(200, getHeight());
        setLocationRelativeTo(this);
        setResizable(false);
        
        // jelzés, hogy befejeződött az inicializálás
        loaded = true;
    }
    
    /**
     * Megadja a kiválasztott nyelvet.
     */
    public Locale getLanguage() {
        return (Locale) CB_LOCALES.getSelectedItem();
    }
    
    /**
     * A legördülőlista új értékét állítja be.
     * @param l a kért nyelv
     */
    public void setLanguage(Locale l) {
        CB_LOCALES.setSelectedIndex(indexOf(l));
        lastSelection = CB_LOCALES.getSelectedItem();
    }
    
    /**
     * A komponens modelében megkeresi a kért nyelv indexét.
     * @param l a kért nyelv
     */
    private int indexOf(Locale l) {
        int index = -1;
        for (int i = 0; i < MODEL_LOCALES.getSize(); i++) {
            if (MODEL_LOCALES.getElementAt(i).getLanguage().equals(l.getLanguage())) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    /**
     * Akkor hívódik meg, ha megváltozott a kijelölt nyelv.
     * @param l az új nyelv
     */
    protected abstract void onLanguageSelected(Locale l);
    
    /**
     * Az összes létező {@link RelocalizableWindow} interfészt implementáló
     * ablak {@link RelocalizableWindow#relocalize()} metódusát hívja meg.
     */
    public static void relocalizeWindows() {
        for (Window w : Window.getWindows()) {
            if (w instanceof RelocalizableWindow) {
                ((RelocalizableWindow) w).relocalize();
            }
        }
    }
    
    /**
     * Gyorskereső menedzser.
     * @param cb a legördülő lista
     */
    public JComboBox.KeySelectionManager createKeySelectionManager(final JComboBox<Locale> cb) {
        cb.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                text = ""; // ha a komponensre kattintanak, a gyorskeresés újrakezdődik
            }
            
        });
        return new JComboBox.KeySelectionManager() {
            
            /**
             * A leütött karaktert hozzáadja a gyorskereső szöveghez,
             * ha back space vagy delete gomb lett lenyomva, a gyorskereső szöveget kiüríti, végül
             * megkeresi a listában azt a nyelvet, amire illeszkedik a gyorskereső szöveg.
             * Ha a gyorskereső szöveg üres, akkor a lista első elemét jelöli ki.
             * Ha a gyorskereső szövegre nincs illeszkedés, nem változik a kijelölés.
             * Ha nem karaktert ütöttek le (pl. F1, Ctrl, Shift), nem módosul a gyorskereső szöveg.
             */
            @Override
            public int selectionForKey(char aKey, ComboBoxModel aModel) {
                switch (getExtendedKeyCodeForChar(aKey)) {
                    case KeyEvent.VK_DELETE:
                        text = "";
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        text = "";
                        break;
                    default:
                        if (Character.isLetter(aKey)) text += aKey;
                }
                if (text.isEmpty()) return 0;
                Iterator<Entry<Locale, String>> it = MAP_LOCALES.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Locale, String> e = it.next();
                    if (e.getValue().toLowerCase().startsWith(text.toLowerCase())) {
                        return MODEL_LOCALES.getIndexOf(e.getKey());
                    }
                }
                return cb.getSelectedIndex();
            }

            /**
             * A {@link KeyEvent#getExtendedKeyCodeForChar(int)} metódus,
             * csak az Oracle JRE-ben található meg.
             * Oracle JRE alatt az eredeti metódus értékével tér vissza;
             * Apple JRE alatt vagy más egyéb JRE alatt a
             * paraméterben átadott érték nem módosul.
             */
            private int getExtendedKeyCodeForChar(int c) {
                try {
                    return KeyEvent.getExtendedKeyCodeForChar(c);
                }
                catch (Throwable t) {
                    return c;
                }
            }
            
        };
    }
    
    /**
     * Megadja az elérhető nyelvek kódjait és neveit.
     * @param bundlepackage a csomag neve, amiben a resource fájlok vannak
     * @param bundlename a ResourceBundle baseName paramétere
     */
    public static Map<Locale, String> getBundleLanguages(String bundlepackage, String bundlename, Locale... def) {
        String[] defKeys = new String[def.length];
        for (int i = 0; i < def.length; i++) {
            defKeys[i] = def[i].getLanguage();
        }
        Set<String> lngs = getBundleLngs(bundlepackage, bundlename, defKeys);
        Map<Locale, String> res = new HashMap<Locale, String>();
        for (Locale l : Locale.getAvailableLocales()) {
            for (String lng : lngs) {
                if (lng.equalsIgnoreCase(l.getLanguage()) && !containsLanguage(res, l)) {
                    res.put(l, getLocaleDisplayLanguage(l));
                    break;
                }
            }
        }
        return res;
    }
    
    /**
     * Megadja, hogy a nyelvek között szerepel-e a megadott nyelv.
     * A metódus nem tesz különbséget kis- és nagybetű között.
     * @param m a nyelvek megfeleltetése
     * @param l a keresett nyelv
     */
    private static boolean containsLanguage(Map<Locale, String> m, Locale l) {
        Iterator<Locale> it = m.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().getLanguage().equalsIgnoreCase(l.getLanguage())) return true;
        }
        return false;
    }
    
    /**
     * Megadja a Locale objektumhoz tartozó nyelvet.
     * A szöveg anyanyelvű és az első karaktere nagy karakter.
     */
    private static String getLocaleDisplayLanguage(Locale l) {
        if (l == null) return "";
        String name = l.getDisplayLanguage();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * Megadja az elérhető nyelvek kódjait.
     * Forrás alap: http://stackoverflow.com/questions/2685907/list-all-available-resourcebundle-files
     * @param bundlepackage a csomag neve, amiben a resource fájlok vannak
     * @param bundlename a ResourceBundle baseName paramétere
     * @param def az alapértelmezett nyelvek kódjai, amik keresés nélkül hozzáadódnak a felsoroláshoz
     */
    private static Set<String> getBundleLngs(final String bundlepackage, final String bundlename, String... def) {
        Set<String> languages = new TreeSet<String>(Arrays.asList(def));
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            File root = new File(loader.getResource(bundlepackage.replace('.', '/')).getFile());

            File[] files = root.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("^" + bundlename + "(_\\w{2}(_\\w{2})?)?\\.properties$");
                }

            });

            for (File file : files) {
                languages.add(file.getName().replaceAll("^" + bundlename + "(_)?|\\.properties$", ""));
            }
        }
        catch (Exception ex) {
            ;
        }
        return languages;
    }
    
}
