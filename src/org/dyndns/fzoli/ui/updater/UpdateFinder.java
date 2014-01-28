package org.dyndns.fzoli.ui.updater;

import com.google.gson.Gson;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.swing.UIManager;
import org.dyndns.fzoli.resource.Base64;
import org.dyndns.fzoli.ui.OptionPane;
import org.dyndns.fzoli.ui.UIUtil;
import org.dyndns.fzoli.util.Folders;
import org.dyndns.fzoli.util.OSUtils;

/**
 *
 * @author zoli
 */
public abstract class UpdateFinder implements Runnable {
    
    static final String EXT_JAR_NAME = "updater.jar";
    static final String TMP_DIR_NAME = "java-app-updater";

    protected abstract boolean hasNewVersion();
    
    protected abstract boolean isEnabled();
    
    protected abstract Map<String, String> getUpdateMap();

    private final Image icon;
    private final String updaterJarPathInJar;
    private final int delay;
    
    public static final String KEY_TITLE = "Updater.title", KEY_MESSAGE = "Updater.message", KEY_ERR_JAVA = "Updater.javaError", KEY_ERR_EXTRCT = "Updater.extractError";
    
    private static final File SRC_FILE = Folders.getSourceFile() == null ? new File(".") : Folders.getSourceFile();
    
    /**
     * Megadja, hogy jar-ból fut-e az alkalmazás.
     */
    private static final boolean IN_JAR;
    
    private static final boolean IN_JARBUNDLER;
    
    private static final String MAC_APP_DIR;
    
    private static final String JAR_FILE;
    
    private static final Gson GSON = new Gson();
    
    static {
        final String srcPath = SRC_FILE.getAbsolutePath();
        IN_JAR = SRC_FILE.getName().endsWith(".jar");
        JAR_FILE = IN_JAR ? srcPath : null;
        boolean inJarbundler = OSUtils.isOS(OSUtils.OS.MAC) && srcPath.contains(".app/Contents/Resources/Java/");
        String macAppDir = null;
        if (inJarbundler) {
            String dir = srcPath.substring(0, srcPath.indexOf(".app") + 4);
            if (dir.endsWith(".app")) macAppDir = dir;
            else inJarbundler = false;
        }
        MAC_APP_DIR = macAppDir;
        IN_JARBUNDLER = inJarbundler;
    }
    
    public UpdateFinder(Image icon, int delay, String updaterJarPathInJar) {
        this.icon = icon;
        this.delay = delay;
        this.updaterJarPathInJar = updaterJarPathInJar;
        UIUtil.init(KEY_TITLE, "Updater");
        UIUtil.init(KEY_MESSAGE, "New version is available.\nWould you like to update the application?");
        UIUtil.init(KEY_ERR_EXTRCT, "The updater could not be extracted.");
        UIUtil.init(KEY_ERR_JAVA, "The Java binary could not be found.");
    }
    
    public static boolean isRunnable() {
        return IN_JAR || IN_JARBUNDLER;
    }
    
    public static String getBinaryPath() {
        if (IN_JARBUNDLER) return MAC_APP_DIR;
        if (IN_JAR) return JAR_FILE;
        return null;
    }
    
    public static boolean inJarbundler() {
        return IN_JARBUNDLER;
    }
    
    private void startUpdate() {
        String updaterPath = null;
        try {
            JarFile jar = new JarFile(JAR_FILE);
            ZipEntry e = jar.getEntry(updaterJarPathInJar);
            if (e != null) {
                File tmpDir = Files.createTempDirectory(TMP_DIR_NAME).toFile();
                File tmpBin = new File(tmpDir, EXT_JAR_NAME);
                tmpBin.createNewFile();
                OutputStream os = new FileOutputStream(tmpBin, false);
                InputStream is = jar.getInputStream(e);
                while (is.available() > 0) {
                    os.write(is.read());
                }
                os.flush();
                os.close();
                is.close();
                updaterPath = tmpBin.getAbsolutePath();
            }
        }
        catch (Throwable t) {
            ;
        }
        if (updaterPath != null) {
            UpdateModel model = new UpdateModel(getUpdateMap(), inJarbundler(), getBinaryPath());
            String json = GSON.toJson(model);
            String b64 = Base64.encode(json.getBytes());
            ArrayList<String> procParams = new ArrayList<String>();
            procParams.add("java");
            procParams.add("-jar");
            procParams.add(updaterPath);
            procParams.add(b64);
            try {
                new ProcessBuilder(procParams).start();
                System.exit(0);
            }
            catch (Exception ex) {
                OptionPane.showWarningDialog(icon, (String) UIManager.get(KEY_ERR_JAVA), (String) UIManager.get(KEY_TITLE));
            }
        }
        else {
            OptionPane.showWarningDialog(icon, (String) UIManager.get(KEY_ERR_EXTRCT), (String) UIManager.get(KEY_TITLE));
        }
    }
    
    @Override
    public void run() {
        if (GraphicsEnvironment.isHeadless()) return;
        while (isEnabled()) {
            if (hasNewVersion() && OptionPane.showYesNoDialog(icon, (String) UIManager.get(KEY_MESSAGE), (String) UIManager.get(KEY_TITLE)) == 0) {
                startUpdate();
                break;
            }
            else {
                Date now;
                Date lastRun = new Date();
                do {
                    try { Thread.sleep(1000); } catch (Exception ex) {}
                    now = new Date();
                }
                while (now.getTime() - lastRun.getTime() < delay);
            }
        }
    }
    
    public static void main(String[] args) {
        UIUtil.setSystemLookAndFeel();
        new UpdateFinder(null, 3000, "updater.jar") {
            
            @Override
            protected boolean hasNewVersion() {
                return true;
            }
            
            @Override
            protected boolean isEnabled() {
                return true;
            }

            @Override
            protected Map<String, String> getUpdateMap() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("https://github.com/fzoli/SecureChat/raw/master/bin/SecureChat.jar", SRC_FILE.getAbsolutePath());
                return map;
            }
            
        }.run();
    }
    
}
