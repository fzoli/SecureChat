package org.dyndns.fzoli.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * OS-függő információkat szolgáltató osztály.
 * @author zoli
 */
public class OSUtils {
    
    private static class HANDLE extends PointerType {}

    private static class HWND extends HANDLE {}
    
    /**
     * Windows alatt a Shell32.dll alapján lehet kideríteni az alkalmazáskönyvtár helyét.
     */
    private static interface Shell32 extends Library {

        public static final int MAX_PATH = 260;
        public static final int CSIDL_LOCAL_APPDATA = 0x001c;
        public static final int SHGFP_TYPE_CURRENT = 0;
        public static final int SHGFP_TYPE_DEFAULT = 1;
        public static final int S_OK = 0;

        /**
         * see http://msdn.microsoft.com/en-us/library/bb762181(VS.85).aspx
         * HRESULT SHGetFolderPath(HWND hwndOwner, int nFolder, HANDLE hToken, DWORD dwFlags, LPTSTR pszPath);
         */
        public int SHGetFolderPath(final HWND hwndOwner, final int nFolder, final HANDLE hToken, final int dwFlags, final char pszPath[]);
        
    }
    
    /**
     * Megadja a felhasználóhoz tartozó, adattárolásra használható könyvtárat.
     * @param name a kért könyvtár neve
     */
    public static String getUserDataFolder(String name) {
        String path;
        if (isOS(OS.WINDOWS)) {
            try {
                // az SWT-appkit API-ból mintázott Shell32 hívás
                // https://github.com/fab1an/appkit/blob/master/src/org/appkit/osdependant/OSUtils.java
                Map<String, Object> options = new HashMap<String, Object>();
                options.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                options.put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
                HWND hwndOwner = null;
                int nFolder = Shell32.CSIDL_LOCAL_APPDATA;
                HANDLE hToken = null;
                int dwFlags = Shell32.SHGFP_TYPE_CURRENT;
                char pszPath[] = new char[Shell32.MAX_PATH];
                Shell32 instance = (Shell32) Native.loadLibrary("shell32", Shell32.class, options);
                int hResult = instance.SHGetFolderPath(hwndOwner, nFolder, hToken, dwFlags, pszPath);
                if (Shell32.S_OK == hResult) {
                    path = new String(pszPath);
                    path = path.substring(0, path.indexOf('\0'));
                }
                else {
                    // ha a Shell32 nem működne, az alapértelmezett útvonal használata
                    path = System.getProperty("user.dir");
                }
            }
            catch (Throwable t) {
                // ha a Shell32 nem működne, az alapértelmezett útvonal használata
                path = System.getProperty("user.dir");
            }
        }
        else if (isOS(OS.MAC)) {
            path = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support";
        }
        else if (isOS(OS.LINUX)) {
            String cfg = System.getenv("XDG_CONFIG_HOME");
            if (cfg == null || cfg.trim().isEmpty()) cfg = System.getProperty("user.home") + File.separator + ".config";
            path = cfg;
        }
        else {
            path = System.getProperty("user.dir");
        }
        if (name == null || name.trim().isEmpty()) return path + File.separator;
        else return path + File.separator + name + File.separator;
    }
    
    /**
     * Beállítja az alkalmazás nevét.
     * A tálcán jelenik meg, amikor már elég sok ablak nyitva van és egy csoportba kerülnek az ablakok.
     * @param text az alkalmazás neve
     */
    public static void setApplicationName(String text) {
        try {
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, text);
        }
        catch (Throwable t) {
            ;
        }
        
//        try {
//            org.eclipse.swt.widgets.Display.setAppName(text);
//        }
//        catch (Throwable t) {
//            ;
//        }
        
        try {
            if (!isOS(OS.MAC)) return;
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", text);
        }
        catch (Exception ex) {
            ;
        }
    }
    
    /**
     * Operációsrendszerek felsorolása.
     */
    public static enum OS {
        
        WINDOWS("win"), LINUX("nux"), MAC("mac");
        
        final String NAME;
        
        private OS(String name) {
            NAME = name;
        }
        
    }
    
    /**
     * Megadja, hogy a paraméterben átadott operációsrendszer alatt fut-e az alkalmazás.
     * @param os a vizsgálandó operációsrendszer
     */
    public static boolean isOS(OS os) {
        if (os == null) return false;
        return System.getProperty("os.name").toLowerCase().contains(os.NAME.toLowerCase());
    }
    
    /**
     * Mac rendszeren megadja, hogy az alkalmazás az első szálból lett-e indítva.
     * Alapötlet: http://stackoverflow.com/a/15229307
     * @param mainClass az alkalmazást indító osztály, amiben a main metódus van
     */
    public static boolean startedOnFirstThread(Class mainClass) {
        String pid = null;
        if (mainClass == null) return false;
        Map<String, String> envs = System.getenv();
        Iterator<Map.Entry<String, String>> it = envs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            if (e.getKey().startsWith("JAVA_MAIN_CLASS_")) {
                if (e.getValue().contains(mainClass.getName())) {
                    pid = e.getKey().substring(16);
                    break;
                }
            }
        }
        if (pid != null) {
            String val = envs.get("JAVA_STARTED_ON_FIRST_THREAD_" + pid);
            if (val != null && val.equals("1")) return true;
        }
        return false;
    }
    
}
