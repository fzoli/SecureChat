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
 *
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
        }
        
    }
    
    private static class UserDataList extends ArrayList<UserData> {

        @Override
        public boolean add(UserData e) {
            boolean b = super.add(e);
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
            boolean b = super.add(e);
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
        if (d != null) {
            getUsers().clear();
            getMessages().clear();
            for (UserData ud : d.getUsers()) {
                getUsers().add(new ClientSideUserData(ud));
            }
            for (ChatMessage cm : d.getMessages()) {
                getMessages().add(new ClientSideChatMessage(cm));
            }
            CHAT_FRAME.setVisible(true);
        }
    }
    
    public static void sendMessage(Serializable msg) {
        List<MessageProcess> ls = ClientProcesses.getProcesses(MessageProcess.class);
        MessageProcess.sendMessage(ls, msg);
    }
    
}