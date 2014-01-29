package org.dyndns.fzoli.util;

import java.io.File;
import org.dyndns.fzoli.ui.UIUtil;

/**
 * Könyvtárakkal kapcsolatos metódusok.
 * @author zoli
 */
public class Folders {
    
    /**
     * Rekurzívan törli a megadott fájlt.
     */
    public static boolean delete(File f) {
        boolean b = true;
        if (f.isDirectory()) { // ha könyvtár ...
            File[] files = f.listFiles();
            if (files != null) {
                for (File c : files) { // ... a benne lévő összes fájl...
                    b &= delete(c); // ... rekurzív törlése
                }
            }
        }
        if (f.exists()) { // ha a fájlnak már nincs gyermeke és még létezik...
            try { f.delete(); } catch (Exception ex) { b = false; } // ... a fájl törlése
        }
        return b;
    }
    
    /**
     * Meadja, hogy az alkalmazás melyik könyvtárban van.
     * @return az útvonal vagy null, ha nem sikerült lekérni
     */
    public static File getSourceDir() {
        try {
            return getSourceFile().getParentFile();
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Meadja, hogy az alkalmazás honnan fut.
     * @return az útvonal vagy null, ha nem sikerült lekérni
     */
    public static File getSourceFile() {
        try {
            return new File(UIUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Elkészíti a kért fájlnévre mutató fájl-objektumot.
     * Ha az aktuális könyvtárban nem található a megadott fájl, a forrás könyvtárban is megnézi.
     * @return a megtalált fájl vagy az aktuális könyvtárba mutató fájl ill. az alapértelmezés, ha meg van adva
     */
    public static File createFile(String fileName, File def) {
        File f = new File(System.getProperty("user.dir"), fileName);
        if (!f.exists()) {
            try {
                File oldFile = f;
                File srcFile = getSourceFile();
                f = new File(srcFile.getParentFile(), fileName);
                if (!f.exists()) f = def == null ? oldFile : def;
            }
            catch (Exception ex) {
                f = null;
            }
        }
        return f;
    }
    
    /**
     * Elkészíti a kért fájlnévre mutató fájl-objektumot.
     * Ha az aktuális könyvtárban nem található a megadott fájl, a forrás könyvtárban is megnézi.
     * @return a megtalált fájl vagy az aktuális könyvtárba mutató fájl
     */
    public static File createFile(String fileName) {
        return createFile(fileName, null);
    }
    
    /**
     * Megadja a jelenlegi munkakönyvtár helyét.
     */
    public static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Megpróbálja beállítani a munkakönyvtárat a paraméterben megadott útvonalra.
     * Forrás: http://stackoverflow.com/a/13981910
     * @param directory_name a könyvtár relatív vagy teljes útvonala
     * @return true, ha sikerült a beállítás, egyébként false
     */
    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false;  // Boolean indicating whether directory was set
        File    directory;       // Desired current working directory
        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs()) {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }
        return result;
    }
    
}
