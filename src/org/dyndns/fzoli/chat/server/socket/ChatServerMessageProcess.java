package org.dyndns.fzoli.chat.server.socket;

import java.io.Serializable;
import org.dyndns.fzoli.chat.model.ChatMessage;
import org.dyndns.fzoli.chat.model.GroupChatData;
import org.dyndns.fzoli.chat.model.GroupChatPartialData;
import org.dyndns.fzoli.chat.model.UserPartialData;
import org.dyndns.fzoli.socket.handler.SecureHandler;
import org.dyndns.fzoli.socket.process.impl.MessageProcess;
import org.dyndns.fzoli.socket.stream.JsonStreamMethod;
import org.dyndns.fzoli.socket.stream.StreamMethod;
import static org.dyndns.fzoli.chat.server.Main.DATA;
import org.dyndns.fzoli.chat.server.ServerSideGroupChatData;

/**
 *
 * @author zoli
 */
public class ChatServerMessageProcess extends MessageProcess {
    
    public ChatServerMessageProcess(SecureHandler handler) {
        super(handler);
    }

    @Override
    protected StreamMethod createStreamMethod(Integer deviceId) {
        return new JsonStreamMethod();
    }

    @Override
    protected void onStart() {
        sendMessage(new GroupChatData(DATA));
    }

    @Override
    protected void onException(Exception ex) {
        closeProcesses();
    }

    @Override
    protected void onMessage(Serializable msg) {
        if (msg instanceof GroupChatPartialData) { // csak az ilyen típusú üzenetek érdeklik a chat szervert
             if (msg instanceof ChatMessage) { // chat üzenetek szerver oldali kezelése
                 ChatMessage cm = (ChatMessage) msg;
                 if (cm.getMessage() != null) {
                     ServerSideGroupChatData.CommandMessage cmd = ServerSideGroupChatData.CommandMessage.findMessage(cm.getMessage());
                     if (cmd != null) {
                         switch (cmd) {
                             case CLEAR:
                                 DATA.getMessages().clear();
                                 break;
                         }
                     }
                     else {
                         if (cm.getID() != null) { // chat üzenet szövegének módosítását szeretné a kliens
                             ChatMessage scm = GroupChatData.findChatMsg(DATA.getMessages(), cm.getID()); // megkeresi a szerver oldalon tárolt üzenetet
                             if (scm != null && getRemoteCommonName().equals(scm.getSender())) { // ha létezik az üzenet és a küldő a kliens felhasználója, akkor ...
                                 scm.setMessage(cm.getMessage()); // ... üzenet módosítása szerver oldalon és üzenet küldése a klienseknek (a kliens onnan tudja, hogy ez nem új üzenet, hogy az üzenet ID <= a lista méreténél a hozzáadás előtt)
                             }
                         }
                         else { // új chat üzenetet küld a kliens
                             DATA.getMessages().add(new ServerSideGroupChatData.ServerSideChatMessage(getRemoteCommonName(), cm.getMessage())); // szerver oldali üzenet létrehozása és küldése a klienseknek
                         }
                     }
                 }
             }
             else if (msg instanceof UserPartialData) { // felhasználói adat változott
                 UserPartialData<?> upd = (UserPartialData) msg;
                 String uname = upd.getUserName();
                 if (uname == null) uname = getRemoteCommonName(); // ha a username nincs megadva, akkor a kliens önmagát érti alatta
                 if (getRemoteCommonName().equals(uname)) { // a kliens csak a saját felhasználójának adatát módosíthatja
                     upd.apply(DATA); // az üzenet kezelése megegyezik a kliens oldaléval
                 }
             }
        }
    }
    
}
