package org.dyndns.fzoli.chat.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.dyndns.fzoli.chat.model.ChatMessage;
import org.dyndns.fzoli.chat.model.GroupChatData;
import org.dyndns.fzoli.chat.model.UserData;
import org.dyndns.fzoli.socket.ClientProcesses;
import org.dyndns.fzoli.socket.process.impl.MessageProcess;
import static org.dyndns.fzoli.chat.client.Main.CHAT_FRAME;

/**
 * TODO:
 * - szerver oldalon van egy bug, ami miatt több kliens is disconnectálódik amikor egy kliens lekapcsolódik a szerverről
 * - ha az átírt üzenet egyezik a régivel, ne küldje el a kliens
 * - az üzenetek átírásakor a rendszerüzenetek eltünnek, mert azok nem tényleges üzenetek:
 *   a kliensnek tárolni kéne a rendszerüzeneteket és amikor a listát tölti fel idő alapján rendezve a rendszerüzeneteket is hozzáadni
 * - ha rejtve van az ablak és üzenet érkezik, jelezze a rendszerikon
 * - ha rejtve van az ablak és megszakad a kapcsolat, ne jöjjön fel a kapcsolódáskezelő ablak, de
 *   ha a kapcsolat megszakadt vagy folyamatban van, jelezze a rendszerikon
 * - ha a chat ablak látható és disconnect történik, ne tűnjön el, csak legyen disabled az üzenet írására szolgáló input mező
 *   (lehet nincs is szükség a kapcsolódáskezelő ablakra, csak akkor ha a system tray nincs támogatva)
 * - beállításokba minimalizált indítás opció: ez esetben semmi ablak nem jelenik meg induláskor és a splash screen is eltűnik amint a system tray ikon megjelenik
 * @author zoli
 */
public class ClientSideGroupChatData extends GroupChatData {
    
    public static class ClientSideUserData extends UserData {
        
        public ClientSideUserData(UserData ud) {
            super(ud);
        }

        @Override
        public void setClientCount(Integer clientCount) {
            super.setClientCount(clientCount);
            CHAT_FRAME.setUserVisible(this, true, false);
        }

        @Override
        public void setSignInDate(Date signInDate) {
            super.setSignInDate(signInDate);
            CHAT_FRAME.setUserVisible(this, true, true);
        }

        @Override
        public void setSignOutDate(Date signOutDate) {
            super.setSignOutDate(signOutDate);
            CHAT_FRAME.setUserVisible(this, false, true);
            if (getClientCount() != null && getClientCount() > 0) CHAT_FRAME.setUserVisible(this, true, false);
            else Main.DATA.getUsers().remove(this);
        }
        
    }
    
    public static class ClientSideChatMessage extends ChatMessage {
        
        public ClientSideChatMessage(ChatMessage msg) {
            super(msg);
        }
        
        public ClientSideChatMessage(String msg) {
            super(msg);
        }

        @Override
        public void setMessage(String msg) {
            super.setMessage(msg);
            CHAT_FRAME.replaceChatMessages(Main.DATA.getMessages());
        }
        
    }
    
    private static class UserDataList extends ArrayList<UserData> {

        @Override
        public boolean add(UserData e) {
            boolean b = super.add(new ClientSideUserData(e));
            if (b) CHAT_FRAME.setUserVisible(e, true, false);
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            CHAT_FRAME.removeUsers();
        }
        
    }
    
    private static class ChatMessageList extends ArrayList<ChatMessage> {

        @Override
        public boolean add(ChatMessage e) {
            boolean b = super.add(new ClientSideChatMessage(e));
            if (b) CHAT_FRAME.addChatMessage(e);
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            CHAT_FRAME.removeChatMessages();
        }
        
    }
    
    public ClientSideGroupChatData() {
        super(Collections.synchronizedList(new UserDataList()), Collections.synchronizedList(new ChatMessageList()));
    }

    @Override
    public void update(GroupChatData d) {
        super.update(d);
        if (d != null) {
            CHAT_FRAME.setVisible(true);
        }
    }
    
    public static void sendMessage(Serializable msg) {
        List<MessageProcess> ls = ClientProcesses.getProcesses(MessageProcess.class);
        MessageProcess.sendMessage(ls, msg);
    }
    
}
