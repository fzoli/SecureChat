package org.dyndns.fzoli.chat.client.socket;

import java.io.InputStream;
import java.io.OutputStream;
import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.chat.ConnectionKeys;
import org.dyndns.fzoli.socket.handler.AbstractSecureClientHandler;
import org.dyndns.fzoli.socket.handler.BufferedStreamDeviceHandler;
import org.dyndns.fzoli.socket.handler.DeviceHandler;
import org.dyndns.fzoli.socket.process.SecureProcess;
import org.dyndns.fzoli.socket.ClientConnectionHelper;

/**
 *
 * @author zoli
 */
public class ChatClientHandler extends AbstractSecureClientHandler implements ConnectionKeys {
    
    public ChatClientHandler(ClientConnectionHelper helper, SSLSocket socket, int devId, int connId) {
        super(helper, socket, devId, connId);
    }

    @Override
    protected int getPrefixSoTimeout() {
        return READ_TIMEOUT;
    }
    
    @Override
    protected DeviceHandler createDeviceHandler(InputStream in, OutputStream out) {
        return new BufferedStreamDeviceHandler(in, out);
    }
    
    @Override
    protected SecureProcess selectProcess() {
        if (getDeviceId() == KEY_DEV_CLIENT) {
            switch (getConnectionId()) {
                case KEY_CONN_DISCONNECT:
                    return new ChatClientDisconnectProcess(this);
                case KEY_CONN_MESSAGE:
                    return new ChatClientMessageProcess(this);
            }
        }
        return null;
    }
    
    /**
     * A kapcsolatkezelő ablak figyelmezteti a felhasználót, hogy kivétel keletkezett.
     * A keletkezett kivételnek megfelelően változik a kapcsolatkezelő ablak üzenete.
     */
    @Override
    protected void onException(Exception ex) {
        fireException(ex);
    }
    
}
