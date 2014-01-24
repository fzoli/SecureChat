package org.dyndns.fzoli.chat.client.socket;

import org.dyndns.fzoli.chat.ConnectionKeys;
import org.dyndns.fzoli.socket.handler.SecureHandler;
import org.dyndns.fzoli.socket.process.impl.ClientDisconnectProcess;
import org.dyndns.fzoli.chat.client.view.ConnectionProgressFrame.Status;
import static org.dyndns.fzoli.chat.client.Main.showConnectionStatus;

/**
 *
 * @author zoli
 */
public class ChatClientDisconnectProcess extends ClientDisconnectProcess implements ConnectionKeys {

    public ChatClientDisconnectProcess(SecureHandler handler) {
        super(handler, DC_TIMEOUT1, DC_TIMEOUT2, DC_DELAY);
    }
    
    @Override
    protected void onConnect() {
        setTimeout(false);
        super.onConnect();
    }
    
    @Override
    protected void onTimeout(Exception ex) throws Exception {
        setTimeout(true);
        super.onTimeout(ex);
    }
    
    @Override
    protected void afterTimeout() throws Exception {
        setTimeout(false);
        super.afterTimeout();
    }
    
    @Override
    protected void onDisconnect(Exception ex) {
        super.onDisconnect(ex);
        showConnectionStatus(Status.DISCONNECTED);
    }
    
    private void setTimeout(boolean b) {
        ;
    }
    
}
