package org.dyndns.fzoli.ui.updater;

import com.google.gson.Gson;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
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
    static final String TMP_ERR_NAME = "update-crash";

    protected abstract boolean hasNewVersion();
    
    protected abstract boolean isEnabled();
    
    protected abstract Map<String, String> getUpdateMap();

    private final Image icon;
    private final String updaterJarPathInJar;
    private final int delay;
    private final String[] args;
    private final boolean silent;
    
    private boolean crashed = false;
    
    public static final String KEY_TITLE = "Updater.title", KEY_MESSAGE = "Updater.message", KEY_ERR_JAVA = "Updater.javaError", KEY_ERR_EXTRCT = "Updater.extractError", KEY_UPGRADING = "Updater.upgrading", KEY_UPDATE_ERR_A = "Updater.updateErrorA", KEY_UPDATE_ERR_B = "Updater.updateErrorB";
    
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
    
    public UpdateFinder(boolean silent, Image icon, int delay, String updaterJarPathInJar, String[] args) {
        this.silent = silent;
        this.icon = icon;
        this.delay = delay;
        this.updaterJarPathInJar = updaterJarPathInJar;
        this.args = args;
        UIUtil.init(KEY_TITLE, "Updater");
        UIUtil.init(KEY_MESSAGE, "New version is available.\nWould you like to update the application?");
        UIUtil.init(KEY_ERR_EXTRCT, "The updater could not be extracted.");
        UIUtil.init(KEY_ERR_JAVA, "The Java binary could not be found.");
        UIUtil.init(KEY_UPGRADING, "Upgrading the application");
        UIUtil.init(KEY_UPDATE_ERR_A, "There were");
        UIUtil.init(KEY_UPDATE_ERR_B, "errors while the updating process was running!");
        if (getBinaryPath() != null) {
            File crashFile = new File(new File(getBinaryPath()).getParent(), TMP_ERR_NAME);
            if (crashFile.exists()) {
                crashed = true;
                try { crashFile.delete(); } catch (Exception ex) { }
            }
        }
    }
    
    public static boolean isSupported() {
        try {
            String version = System.getProperty("java.version");
            double ver = Double.parseDouble(version.substring(0, version.indexOf('.', version.indexOf('.') + 1)));
            return ver > 1.6;
        }
        catch (Exception ex) {
            return true;
        }
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

    public boolean isCrashed() {
        return crashed;
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
                FileOutputStream fos = new FileOutputStream(tmpBin, false);
                InputStream is = jar.getInputStream(e);
                ReadableByteChannel rbc = Channels.newChannel(is);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.flush();
                fos.close();
                is.close();
                updaterPath = tmpBin.getAbsolutePath();
            }
        }
        catch (Throwable t) {
            ;
        }
        if (updaterPath != null) {
            UpdateModel model = new UpdateModel(getUpdateMap(), inJarbundler(), getBinaryPath(), args, silent, (String) UIManager.get(KEY_TITLE), (String) UIManager.get(KEY_UPGRADING), (String) UIManager.get(KEY_UPDATE_ERR_A), (String) UIManager.get(KEY_UPDATE_ERR_B));
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
                crashed = true;
                if (!silent) OptionPane.showWarningDialog(icon, (String) UIManager.get(KEY_ERR_JAVA), (String) UIManager.get(KEY_TITLE));
            }
        }
        else {
            crashed = true;
            if (!silent) OptionPane.showWarningDialog(icon, (String) UIManager.get(KEY_ERR_EXTRCT), (String) UIManager.get(KEY_TITLE));
        }
    }
    
    @Override
    public void run() {
        if (GraphicsEnvironment.isHeadless()) return;
        while (isSupported() && isEnabled() && !crashed) {
            if (hasNewVersion() && (silent || OptionPane.showYesNoDialog(icon, (String) UIManager.get(KEY_MESSAGE), (String) UIManager.get(KEY_TITLE)) == 0)) {
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
    
}
