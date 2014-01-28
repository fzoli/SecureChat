package org.dyndns.fzoli.chat.updater;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.chat.ui.AboutFrame;
import org.dyndns.fzoli.ui.updater.UpdateFinder;

/**
 *
 * @author zoli
 */
public class ChatAppUpdateFinder extends UpdateFinder {

    public ChatAppUpdateFinder() {
        super(true, R.getClientImage(), 14400000, "updater.jar", null);
    }
    
    private int getCommitVersion(String ver) {
        return Integer.parseInt(ver.substring(ver.lastIndexOf('.') + 1));
    }
    
    @Override
    protected boolean hasNewVersion() {
        boolean b = false;
        try {
            URL url = new URL("https://raw2.github.com/fzoli/SecureChat/master/src/org/dyndns/fzoli/chat/ui/AboutFrame.java");
            InputStream in = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("static final String VERSION")) {
                    break;
                }
            }
            String gitVersion = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
            boolean different = !AboutFrame.VERSION.equals(gitVersion);
            if (different) b = getCommitVersion(gitVersion) > getCommitVersion(AboutFrame.VERSION);
        }
        catch (Exception ex) {
            ;
        }
        return b;
    }

    @Override
    protected boolean isEnabled() {
        return isRunnable();
    }

    @Override
    protected Map<String, String> getUpdateMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("https://github.com/fzoli/SecureChat/raw/master/bin/SecureChat.jar", getBinaryPath());
        return map;
    }
    
}
