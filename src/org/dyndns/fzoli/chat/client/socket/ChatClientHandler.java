package org.dyndns.fzoli.chat.client.socket;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.chat.ConnectionKeys;
import org.dyndns.fzoli.chat.client.view.ConnectionProgressFrame.Status;
import org.dyndns.fzoli.socket.handler.AbstractSecureClientHandler;
import org.dyndns.fzoli.socket.handler.BufferedStreamDeviceHandler;
import org.dyndns.fzoli.socket.handler.DeviceHandler;
import org.dyndns.fzoli.socket.handler.exception.RemoteHandlerException;
import org.dyndns.fzoli.socket.process.SecureProcess;
import static org.dyndns.fzoli.chat.client.Main.showConnectionStatus;

/**
 *
 * @author zoli
 */
public class ChatClientHandler extends AbstractSecureClientHandler implements ConnectionKeys {
    
    public ChatClientHandler(SSLSocket socket, int devId, int connId) {
        super(socket, devId, connId);
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
        try {
            throw ex;
        }
        catch (RemoteHandlerException e) {
            showConnectionStatus(Status.CONNECTION_REFUSED);
        }
        catch (SocketTimeoutException e) {
            showConnectionStatus(Status.CONNECTION_TIMEOUT);
        }
        catch (SSLHandshakeException e) {
            showConnectionStatus(e.getMessage().contains("Extended key usage") ? Status.SERVER_IS_NOT_CLIENT : Status.HANDSHAKE_ERROR);
        }
        catch (SocketException e) {
            showConnectionStatus(Status.CONNECTION_ERROR);
        }
        catch (EOFException e) {
            showConnectionStatus(Status.DISCONNECTED);
        }
        catch (Exception e) {
            showConnectionStatus(Status.UNKNOWN_CONNECTION_ERROR);
        }
    }
    
}
