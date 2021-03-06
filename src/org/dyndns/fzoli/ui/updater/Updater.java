package org.dyndns.fzoli.ui.updater;

import com.google.gson.Gson;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.dyndns.fzoli.resource.Base64;
import org.dyndns.fzoli.ui.OptionPane;
import org.dyndns.fzoli.ui.UIUtil;
import org.dyndns.fzoli.util.Folders;

/**
 *
 * @author zoli
 */
public abstract class Updater implements Runnable {

    private UpdateModel model;

    private void setModel(UpdateModel model) {
        this.model = model;
    }
    
    protected abstract void printGUI(String message, File f, int count, int size);
    
    @Override
    public void run() {
        Iterator<Map.Entry<String, String>> it = model.UPDATE_MAP.entrySet().iterator();
        int errors = 0, count = 0;
        while (it.hasNext()) {
            count++;
            File tmpFile = null;
            try {
                Map.Entry<String, String> e = it.next();
                File origFile = new File(e.getValue());
                printGUI(model.LOADING_MESSAGE, origFile, count, model.UPDATE_MAP.size());
                URL url = new URL(e.getKey());
                InputStream in = url.openStream();
                ReadableByteChannel rbc = Channels.newChannel(in);
                tmpFile = new File(e.getValue() + ".tmp");
                tmpFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(tmpFile, false);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.flush();
                fos.close();
                in.close();
                if (origFile.isFile()) origFile.delete();
                tmpFile.renameTo(origFile);
            }
            catch (Exception e) {
                errors++;
                if (tmpFile != null && tmpFile.isFile()) tmpFile.delete();
            }
        }
        ArrayList<String> ls = new ArrayList<String>();
        if (model.JARBUNDLER) {
            ls.add("open");
            ls.add("-n");
            ls.add(model.BINARY);
            if (model.ARGS != null) {
                ls.add("--args");
            }
        }
        else {
            ls.add("java");
            ls.add("-jar");
            ls.add(model.BINARY);
        }
        if (model.ARGS != null) for (String arg : model.ARGS) {
            ls.add(arg);
        }
        if (errors > 0) {
            if (!model.SILENT) OptionPane.showWarningDialog((Window) null, model.ERR_MESSAGE_A + " " + errors + " " + model.ERR_MESSAGE_B, model.TITLE);
            try {
                new File(new File(model.BINARY).getParent(), UpdateFinder.TMP_ERR_NAME).createNewFile();
            }
            catch (Exception ex) {
                ;
            }
        }
        try {
            new ProcessBuilder(ls).start();
        }
        catch (Exception ex) {
            ;
        }
        System.exit(0);
    }
    
    public static void main(String[] args, Updater updater) {
        File srcFile = Folders.getSourceFile();
        String runningFrom = srcFile.getAbsolutePath();
        boolean updaterBinary = runningFrom.endsWith(UpdateFinder.EXT_JAR_NAME) && runningFrom.contains(UpdateFinder.TMP_DIR_NAME);
        if (updaterBinary) {
            final File tmpDir = srcFile.getParentFile();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    Folders.delete(tmpDir);
                }
                
            }));
        }
        if (args.length == 1) {
            UpdateModel model;
            try {
                String json = new String(Base64.decode(args[0]));
                model = new Gson().fromJson(json, UpdateModel.class);
            }
            catch (Exception ex) {
                return;
            }
            UIUtil.setSystemLookAndFeel();
            updater.setModel(model);
            updater.run();
        }
    }
    
}
