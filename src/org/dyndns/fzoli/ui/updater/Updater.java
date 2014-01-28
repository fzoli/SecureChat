package org.dyndns.fzoli.ui.updater;

import com.google.gson.Gson;
import java.awt.Window;
import java.io.File;
import org.dyndns.fzoli.resource.Base64;
import org.dyndns.fzoli.ui.OptionPane;
import org.dyndns.fzoli.ui.UIUtil;
import org.dyndns.fzoli.util.Folders;

/**
 *
 * @author zoli
 */
public class Updater implements Runnable {

    private final UpdateModel model;
    
    public Updater(UpdateModel model) {
        this.model = model;
    }
    
    @Override
    public void run() {
        OptionPane.showWarningDialog((Window) null, "Update map: " + model.UPDATE_MAP, "Test");
    }
    
    public static void main(String[] args) {
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
            new Updater(model).run();
        }
    }
    
}
