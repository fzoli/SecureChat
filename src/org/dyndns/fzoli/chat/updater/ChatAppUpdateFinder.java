package org.dyndns.fzoli.chat.updater;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.chat.ui.AboutFrame;
import org.dyndns.fzoli.chat.ui.UIUtil;
import org.dyndns.fzoli.ui.updater.UpdateFinder;

/**
 *
 * @author zoli
 */
public class ChatAppUpdateFinder extends UpdateFinder {

    private boolean newVersion = false;
    
    private final boolean client;
    
    static {
        ResourceBundle res = UIUtil.createResource("org.dyndns.fzoli.chat.l10n.updater", Locale.getDefault());
        UIUtil.init(KEY_TITLE, res.getString("title"));
        UIUtil.init(KEY_MESSAGE, res.getString("message"));
        UIUtil.init(KEY_ERR_EXTRCT, res.getString("err_extrct"));
        UIUtil.init(KEY_ERR_JAVA, res.getString("err_java"));
        UIUtil.init(KEY_UPGRADING, res.getString("upgrading"));
        UIUtil.init(KEY_UPDATE_ERR_A, res.getString("update_err_a"));
        UIUtil.init(KEY_UPDATE_ERR_B, res.getString("update_err_b"));
    }
    
    public ChatAppUpdateFinder() {
        this(true);
    }
    
    public ChatAppUpdateFinder(boolean client) {
        super(true, R.getClientImage(), 14400000, "updater.jar", null);
        this.client = client;
    }
    
    private int getCommitVersion(String ver) {
        return Integer.parseInt(ver.substring(ver.lastIndexOf('.') + 1));
    }
    
    @Override
    public boolean hasNewVersion() {
        if (newVersion) return true;
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
            if (different) newVersion = getCommitVersion(gitVersion) > getCommitVersion(AboutFrame.VERSION);
        }
        catch (Exception ex) {
            ;
        }
        return newVersion;
    }

    @Override
    public boolean isEnabled() {
        return isRunnable();
    }

    @Override
    protected Map<String, String> getUpdateMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("https://github.com/fzoli/SecureChat/raw/master/" + (client ? "bin" : "bin-server") + "/SecureChat.jar", getBinaryPath());
        return map;
    }
    
}
