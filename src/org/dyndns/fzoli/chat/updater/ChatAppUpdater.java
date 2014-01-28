package org.dyndns.fzoli.chat.updater;

import java.io.File;
import org.dyndns.fzoli.chat.SplashScreenLoader;
import org.dyndns.fzoli.ui.updater.Updater;

/**
 *
 * @author zoli
 */
public class ChatAppUpdater extends Updater {

    @Override
    protected void printGUI(File f, int count, int size) {
        if (SplashScreenLoader.isVisible()) {
            SplashScreenLoader.setSplashMessage("Upgrading the application");
        }
    }
    
    public static void main(String[] args) {
        main(args, new ChatAppUpdater());
    }
    
}
