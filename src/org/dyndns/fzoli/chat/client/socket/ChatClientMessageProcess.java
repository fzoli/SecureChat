package org.dyndns.fzoli.chat.client.socket;

import java.io.Serializable;
import org.dyndns.fzoli.chat.client.ClientSideGroupChatData;
import org.dyndns.fzoli.chat.model.GroupChatData;
import org.dyndns.fzoli.chat.model.GroupChatPartialData;
import org.dyndns.fzoli.socket.handler.SecureHandler;
import org.dyndns.fzoli.socket.process.impl.MessageProcess;
import org.dyndns.fzoli.socket.stream.JsonStreamMethod;
import org.dyndns.fzoli.socket.stream.StreamMethod;
import static org.dyndns.fzoli.chat.client.Main.DATA;
import static org.dyndns.fzoli.chat.client.Main.showConnectionStatus;
import org.dyndns.fzoli.chat.client.view.ChatFrame;
import org.dyndns.fzoli.chat.model.UserData;

/**
 *
 * @author zoli
 */
public class ChatClientMessageProcess extends MessageProcess {

    public ChatClientMessageProcess(SecureHandler handler) {
        super(handler);
    }
    
    @Override
    protected StreamMethod createStreamMethod(Integer deviceId) {
        return new JsonStreamMethod();
    }

    @Override
    protected void onException(Exception ex) {
        closeProcesses();
    }

    @Override
    protected void onStart() {
        showConnectionStatus(null);
        ChatFrame.setSenderName(getLocalCommonName());
    }
    
    @Override
    protected void onMessage(Serializable msg) {
        if (msg instanceof UserData) {
            DATA.getUsers().add(new ClientSideGroupChatData.ClientSideUserData((UserData) msg));
        }
        else if (msg instanceof GroupChatData) {
            DATA.update((GroupChatData) msg);
        }
        else if (msg instanceof GroupChatPartialData) {
            ((GroupChatPartialData) msg).apply(DATA);
        }
    }
    
}
