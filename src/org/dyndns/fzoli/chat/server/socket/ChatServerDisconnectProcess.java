package org.dyndns.fzoli.chat.server.socket;

import java.util.Date;
import java.util.List;
import org.dyndns.fzoli.chat.ConnectionKeys;
import org.dyndns.fzoli.chat.model.GroupChatData;
import org.dyndns.fzoli.chat.model.UserData;
import org.dyndns.fzoli.chat.server.ConnectionAlert;
import org.dyndns.fzoli.socket.handler.SecureHandler;
import org.dyndns.fzoli.socket.process.impl.ServerDisconnectProcess;
import static org.dyndns.fzoli.chat.server.Main.getString;
import static org.dyndns.fzoli.chat.server.Main.DATA;
import org.dyndns.fzoli.chat.server.ServerSideGroupChatData.ServerSideUserData;

/**
 *
 * @author zoli
 */
public class ChatServerDisconnectProcess extends ServerDisconnectProcess implements ConnectionKeys {

    private UserData ud;
    
    public ChatServerDisconnectProcess(SecureHandler handler) {
        super(handler, DC_TIMEOUT1, DC_TIMEOUT2, DC_DELAY);
    }
    
    /**
     * Ha a kapcsolat létrejött, jelzi, ha kérik.
     */
    @Override
    protected void onConnect() {
        super.onConnect();
        List<UserData> users = DATA.getUsers(); // szerver oldali felhasználó lista, mely 'add' metódusa broadcastol a kliensek felé)
        ud = GroupChatData.findUser(users, getRemoteCommonName());
        if (ud != null) { // már ismert a felhasználó (több élő belépés)
            int count = ud.getClientCount();
            count++;
            log(true, count);
            ud.setClientCount(count); // kiküldi a klienseknek, hogy újra belépet (hanyadik alkalommal) egy már online user és a szerver tárolja -> kliens oldalon a felhasználólistában a felhasználónév után megjelenik egy szám (>1)
        }
        else { // első online kapcsolat
            log(true, 1);
            ud = new ServerSideUserData(getRemoteCommonName(), getRemoteFullName()); // felhasználó létrehozása (szerver oldali, mely setterei broadcastolnak a kliensek felé)
            users.add(ud); // kiküldi a klienseknek a bejelentkezett felhasználó adatait és a szerver nyilvántartásba veszi -> kliens oldalon bővül a felhasználólista
        }
        ud.setSignInDate(new Date()); // kiküldi az online felhasználóknak a bejelentkezés tényét (dátumot) és a szerver tárolja -> rendszerüzenetben jelenik meg kliens oldalon
    }
    
    /**
     * Ha a kapcsolat végetért, jelzi, ha kérik.
     */
    @Override
    protected void onDisconnect(Exception ex) {
        int count = ud.getClientCount();
        count--;
        log(false, count);
        ud.setClientCount(count); // kiküldi a klienseknek, hogy a felhasználó online kapcsolata csökkent (ha 0, akkor offline lett -> felhasználólista és model ürítése kliens oldalon) és a szerver tárolja
        ud.setSignOutDate(new Date()); // kiküldi a klienseknek a kijelentkezés tényét (dátumot) -> rendszerüzenetben jelenik meg kliens oldalon
        if (count < 1) DATA.getUsers().remove(ud); // szerver oldali RAM takarékosság ...
        ud = null; // ... ez is
        super.onDisconnect(ex);
    }
    
    /**
     * Jelez a felhasználónak, kapcsolódást illetve lekapcsolódást, ha kérik.
     */
    private void log(boolean connect, int count) {
        char c = getDeviceId() == KEY_DEV_CLIENT ? 'a' : 'b';
        String s1 = getString("log_conn1" + c);
        String s2 = getString("log_conn2" + c);
        String sc = count > 1 ? " [" + count + "] " : "";
        boolean e1 = s1.trim().isEmpty();
        boolean e2 = s2.trim().isEmpty();
        ConnectionAlert.log(s1 + (e1 ? "" : " ") + getRemoteCommonName() + sc + (e2 ? "" : " ") + s2 + ' ' + getString(connect ? "log_conn3" : "log_conn4"));
    }
    
}
