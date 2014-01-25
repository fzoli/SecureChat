package org.dyndns.fzoli.chat.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.dyndns.fzoli.chat.model.ChatMessage;
import org.dyndns.fzoli.chat.model.GroupChatData;
import org.dyndns.fzoli.chat.model.GroupChatPartialListData;
import org.dyndns.fzoli.chat.model.UserData;
import org.dyndns.fzoli.chat.model.UserPartialDateData;
import org.dyndns.fzoli.chat.model.UserPartialIntData;
import org.dyndns.fzoli.socket.ServerProcesses;
import org.dyndns.fzoli.socket.process.impl.MessageProcess;

/**
 *
 * @author zoli
 */
public class ServerSideGroupChatData extends GroupChatData {

    public enum CommandMessage {
        
        CLEAR("/clear");
        
        private final String MSG;
        
        private CommandMessage(String msg) {
            MSG = msg;
        }

        public String getMessage() {
            return MSG;
        }
        
        public static CommandMessage findMessage(String msg) {
            for (CommandMessage cm : values()) {
                if (cm.getMessage().equalsIgnoreCase(msg)) return cm;
            }
            return null;
        }
        
    }
    
    public static class ServerSideUserData extends UserData {

        public ServerSideUserData(String userName, String fullName) {
            super(userName, fullName);
        }

        @Override
        public void setClientCount(Integer clientCount) {
            super.setClientCount(clientCount);
            sendMessage(new UserPartialIntData(getUserName(), UserPartialIntData.Type.CLIENT_COUNT, clientCount));
        }

        @Override
        public void setSignInDate(Date signInDate) {
            super.setSignInDate(signInDate);
            sendMessage(new UserPartialDateData(getUserName(), UserPartialDateData.Type.SIGN_IN, signInDate));
        }

        @Override
        public void setSignOutDate(Date signOutDate) {
            super.setSignOutDate(signOutDate);
            sendMessage(new UserPartialDateData(getUserName(), UserPartialDateData.Type.SIGN_OUT, signOutDate));
        }
        
    }
    
    public static class ServerSideChatMessage extends ChatMessage {
        
        public ServerSideChatMessage(String sender, String fullName, String msg) {
            super(sender, fullName, msg);
        }

        @Override
        public void setMessage(String msg) {
            super.setMessage(msg);
            sendMessage(new ChatMessage(this, msg));
        }
        
    }
    
    private static class UserDataList extends ArrayList<UserData> {

        @Override
        public boolean add(UserData e) {
//            if (!contains(e)) {
                super.add(e);
                sendMessage(new UserData(e)); // szerver oldali példány alapján másolat készítése kliens oldalra és küldés
                return true;
//            }
//            return false;
        }

        /**
         * A {@link ArrayList#contains(java.lang.Object)} metódus erre a metódusra támaszkodik, ezért praktikusabb volt egy csapással kettőt újradefiniálni.
         * De... mivel a ChatServerDisconnectProcess nem ad hozzá a listához már benne lévő felhasználókat, ez csak egy extra biztonság.
         * Mindenesetre felhasználónév és UserData objektum alapján is lehet kerestetni ezzel a metódussal; erre még jó.
         */
        @Override
        public int indexOf(Object o) {
            if (o instanceof UserData) return GroupChatData.indexOf(this, (UserData) o);
            if (o instanceof String) return indexOf(GroupChatData.findUser(this, (String) o));
            return super.indexOf(o);
        }
        
    }
    
    private static class ChatMessageList extends ArrayList<ChatMessage> {

        @Override
        public boolean add(ChatMessage e) {
            sendMessage(new ChatMessage(e));
            return super.add(e);
        }

        @Override
        public void clear() {
            super.clear();
            sendMessage(new GroupChatPartialListData(ChatMessage.class, this));
        }
        
    }
    
    public ServerSideGroupChatData() {
        super(Collections.synchronizedList(new UserDataList()), Collections.synchronizedList(new ChatMessageList()));
    }
    
    private static void sendMessage(Serializable msg) {
        List<MessageProcess> ls = ServerProcesses.getProcesses(MessageProcess.class);
        MessageProcess.sendMessage(ls, msg);
    }
    
}
