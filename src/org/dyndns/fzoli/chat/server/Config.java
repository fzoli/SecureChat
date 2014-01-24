package org.dyndns.fzoli.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.util.Folders;

/**
 * A szerver konfigurációját tölti be a bridge.conf fájlból.
 * Ha a konfig fájl nem létezik, létrehozza az alapértelmezett konfiggal.
 * A konfig fájlban # karakterrel lehet kommentezni a sorokban.
 * A fölösleges szóközöket az objektumot létrehozó metódus levágja.
 * @author zoli
 */
public class Config implements org.dyndns.fzoli.chat.Config {
    
    private Map<String, String> values;
    
    /**
     * Az értékeket tartalmazó felsorolás kulcsai, amit a program használ.
     */
    public static final String KEY_PORT = "port",
                               KEY_CA = "ca",
                               KEY_CERT = "cert",
                               KEY_KEY = "key",
                               KEY_PASSWORD = "password",
                               KEY_SINGLE = "single",
                               KEY_QUIET = "quiet",
                               KEY_HIDDEN = "hidden",
                               KEY_LANG = "lang",
                               KEY_APP_MENU = "app-menu";
    
    /**
     * Általános rendszerváltozók.
     */
    public static final String LS = System.getProperty("line.separator");
    
    /**
     * A soronként érvényes megjegyzés karakter.
     */
    private static final String CC = "#";
    
    /**
     * A konfig fájl neve.
     */
    private static final String CFG_NAME = "server.conf";
    
    /**
     * A konfig fájl eléréséhez létrehozott objektum.
     */
    private static final File FILE_CONFIG = R.getConfigFile(CFG_NAME);
    
    /**
     * A konfig fájl objektuma.
     */
    private final File FILE;
    
    /**
     * Az alapértelmezett fájl tartalma.
     */
    private static final String DEFAULT_CONFIG =
            CC + " Ez a fájl a Secure Chat szerverének konfigurációs állománya." + LS +
            KEY_PORT + " 8443 " + CC + " az a TCP port, amin a szerver figyel" + LS +
            KEY_CA + ' ' + new File("test-certs", "ca.crt") + ' ' + CC + " a tanúsítványokat kiállító CA tanúsítvány-fájl" + LS +
            KEY_CERT + ' ' + new File("test-certs", "server.crt") + ' ' + CC + " a szerver tanúsítvány-fájl" + LS +
            KEY_KEY + ' ' + new File("test-certs", "server.key") + ' ' + CC + " a szerver titkos kulcsa" + LS +
            CC + ' ' + KEY_PASSWORD + " optional_cert_password " + CC + " a szerver tanúsítványának jelszava, ha van" + LS +
            CC + ' ' + KEY_SINGLE + " true " + CC + " ha true, a szerver csak egy kapcsolatot engedélyez egyszerre felhasználónként" + LS +
            CC + ' ' + KEY_QUIET + " true " + CC + " ha true, a program indulásakor az összes figyelmeztetés inaktív" + LS +
            CC + ' ' + KEY_HIDDEN + " true " + CC + " ha true, a rendszerikon nem jelenik meg annak ellenére sem, hogy van grafikus felület" + LS +
            CC + ' ' + KEY_APP_MENU + " true " + CC + " ha true, a rendszerikon menüjéből elérhető egy kliens processt indító opció (alapértelmezetten csak Mac-en látható)" + LS +
            CC + ' ' + KEY_LANG + " en " + CC + " a program nyelvét adja meg";
    
    /**
     * Ideiglenes jelszó a memóriában.
     */
    private char[] password = null;
    
    /**
     * Ez az osztály nem példányosítható kívülről és nem származhatnak belőle újabb osztályok.
     */
    private Config(File file) {
        FILE = file;
    }

    /**
     * A konfig fájl objektuma.
     */
    public File getFile() {
        return FILE;
    }
    
    /**
     * A konfigurációs fájl single paramétere.
     * Ha true, a szerver csak egy kapcsolatot engedélyez egyszerre felhasználónként.
     * @return false, ha nincs megadva, és ha a megadott érték "true", akkor true
     */
    public boolean isSingle() {
        return isTrue(KEY_SINGLE);
    }
    
    /**
     * A konfigurációs fájl quiet paramétere.
     * Ha true, akkor a program indulásakor a figyelmeztetések inaktívak.
     * @return false, ha nincs megadva, és ha a megadott érték "true", akkor true
     */
    public boolean isQuiet() {
        return isTrue(KEY_QUIET);
    }
    
    /**
     * A konfigurációs fájl hidden paramétere.
     * Ha true, akkor nem jelenik meg a rendszerikon.
     * @return false, ha nincs megadva, és ha a megadott érték "true", akkor true
     */
    public boolean isHidden() {
        return isTrue(KEY_HIDDEN);
    }
    
    /**
     * A konfigurációs fájl hidden paramétere.
     * Ha true, akkor a rendszertől függetlenül látható
     * a rendszerikon menüjében egy kliens processt indító opció.
     * @return false, ha nincs megadva, és ha a megadott érték "true", akkor true
     */
    public boolean isAppMenu() {
        return isTrue(KEY_APP_MENU);
    }
    
    /**
     * A konfigurációs fájl egyik paraméteréről mondja meg, hogy true-e az értéke.
     * @return false, ha nincs megadva, és ha a megadott érték "true", akkor true
     */
    private boolean isTrue(String key) {
        try {
            return Boolean.parseBoolean(getValues().get(key));
        }
        catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * A program nyelve.
     */
    public Locale getLanguage() {
        Locale def = Locale.getDefault();
        if (getValues() == null) return def;
        String lang = getValues().get(KEY_LANG);
        return new Locale(lang == null ? def.getLanguage() : lang);
    }
    
    /**
     * A konfigurációs fájl port paramétere.
     * A SocketServer ezen a porton figyel.
     * @return port vagy kivétel esetén NULL
     */
    @Override
    public Integer getPort() {
        try {
            int port = Integer.parseInt(getValues().get(KEY_PORT));
            if (port < 1 || port > 65535) return null;
            return port;
        }
        catch (NullPointerException ex) {
            return null;
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }
    
    /**
     * A konfigurációs fájl ca paramétere.
     * Ez az egyelten CA, amit a szerver és kliens socketek elfogadnak.
     * @return fájl vagy NULL, ha a fájl nem létezik
     */
    @Override
    public File getCAFile() {
        return createFile(KEY_CA);
    }
    
    /**
     * A konfigurációs fájl cert paramétere.
     * A CA által kiállított érvényes tanúsítvány helye.
     * @return fájl vagy NULL, ha a fájl nem létezik
     */
    @Override
    public File getCertFile() {
        return createFile(KEY_CERT);
    }
    
    /**
     * A konfigurációs fájl key paramétere.
     * A CA által kiállított érvényes tanúsítvány titkos kulcsának helye.
     * @return fájl vagy NULL, ha a fájl nem létezik
     */
    @Override
    public File getKeyFile() {
        return createFile(KEY_KEY);
    }
    
    /**
     * Az opcionális tanúsítvány jelszó értékét adja vissza.
     * Ha nincs megadva, üres karakterlánccal tér vissza.
     */
    @Override
    public char[] getPassword() {
        if (password != null) return password;
        if (getValues() == null || getValues().get(KEY_PASSWORD) == null) return new char[] {};
        return getValues().get(KEY_PASSWORD).toCharArray();
    }
    
    /**
     * A memóriában tárolja a megadott jelszót.
     */
    public void setPassword(char[] password) {
        this.password = password;
    }
    
    /**
     * A konfigurációs fájl paraméterei és azok értékei.
     * @return A paramétereket tartalmazó felsorolás vagy NULL, ha a konfig fájl nem létezik.
     */
    public Map<String, String> getValues() {
        return values;
    }

    /**
     * @return true, ha minden érték meg van adva és érvényes, egyébként false
     */
    @Override
    public boolean isCorrect() {
        return getPort() != null &&
               getCAFile() != null &&
               getCertFile() != null &&
               getKeyFile() != null;
    }
    
    /**
     * @return true, ha a konfig fájl most lett létrehozva
     */
    public boolean isNew() {
        return getValues() == null;
    }
    
    /**
     * Tesztelési célból értelmes szöveget generál a metódus az objektumnak.
     */
    @Override
    public String toString() {
        return "Port: " + getPort() + LS +
               "CA file: " + getCAFile() + LS +
               "Cert file: " + getCertFile() + LS +
               "Key file:" + getKeyFile() + LS +
               "Password length: " + (getPassword() == null ? -1 : getPassword().length) + LS +
               "Hidden: " + isHidden() + LS +
               "App menu: " + isAppMenu() + LS +
               "Lang: " + getLanguage().getLanguage() + LS +
               "Correct? " + isCorrect();
    }
    
    /**
     * Gyártó metódus.
     * @throws RuntimeException ha bármi hiba történik. Pl. nincs olvasási/írási jog
     */
    public static Config getInstance() {
        File file;
        List<String> conf;
        try {
            // ha a konfigurációs fájl meg van találva vagy Mac OS X rendszeren fut az alkalmazás, a forráskönyvtár kihagyása
            if (FILE_CONFIG.exists() || System.getProperty("os.name").toUpperCase().contains("MAC")) throw new Exception();
            // a forráskönyvtárból olvassa ki a konfigot, vagy ha nem létezik, megpróbálja létrehozni
            conf = read(file = new File(Folders.getSourceDir().getAbsoluteFile(), CFG_NAME), DEFAULT_CONFIG);
        }
        catch (Exception ex) {
            // ha nem a forráskönyvtárat van használva, akkor a "cd" könyvtár használata
            conf = read(file = FILE_CONFIG, DEFAULT_CONFIG);
        }
        // konfig létrehozása és a konfig fájl helyének megadása
        Config config = new Config(file);
        // konfig beállítása, ha sikerült a konfig fájl beolvasása
        if (conf != null) {
            int ind;
            String key, val;
            config.values = new HashMap<String, String>();
            for (String ln : conf) {
                ind = ln.indexOf(" ");
                if (ind == -1) continue;
                key = ln.substring(0, ind);
                val = ln.substring(ind).trim();
                config.values.put(key, val);
            }
        }
        return config;
    }
    
    /**
     * A megadott szövegfájlt beolvasó metódus.
     * @param f a beolvasandó fájl
     * @param def ha a fájl nem létezik, létrejön a fájl a paraméter tartalmával.
     * @return Sorokat tartalmazó lista vagy NULL, ha a fájl nem létezik.
     * @throws RuntimeException ha bármi hiba történik. Pl. nincs olvasási/írási jog
     */
    public static List<String> read(File f, String def) {
        // kísérlet a konfig fájl beolvasására
        if (f != null) try {
            int ind;
            String ln;
            List<String> ls = new ArrayList<String>();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while ((ln = in.readLine()) != null) {
                ln = ln.trim();
                if (ln.startsWith(CC)) continue;
                ind = ln.indexOf(CC);
                if (ind != -1) ln = ln.substring(0, ind);
                if (!ln.isEmpty()) ls.add(ln);
            }
            in.close();
            return ls;
        }
        // ha a konfig fájl nem létezik, létrehozza azt az alapértelmezéssel, ha meg van adva
        catch (FileNotFoundException ex) {
            if (def != null) try {
                if (f.isDirectory()) Folders.delete(f);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
                out.write(def, 0, def.length());
                out.flush();
                out.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }
    
    
    
    /**
     * Útvonal alapján fájl objektumot ad vissza.
     * Ha a fájl nem létezik, vagy nem fájl, akkor NULL értékkel tér vissza.
     */
    private File createFile(String key) {
        if (getValues() == null) return null;
        String path = getValues().get(key);
        if (path == null) return null;
        File f = new File(path);
        if (!f.exists()) f = Folders.createFile(path);
        if (!f.exists() || !f.isFile()) return null;
        return f;
    }
    
}
