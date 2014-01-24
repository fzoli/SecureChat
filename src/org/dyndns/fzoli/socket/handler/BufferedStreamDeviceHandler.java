package org.dyndns.fzoli.socket.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.dyndns.fzoli.socket.handler.exception.RemoteHandlerException;

/**
 * Eszközkezelő I/O segédosztály.
 * Közönséges szöveget küld státuszüzenetnek újsorral a végén,
 * hogy a státuszüzenet olvasása a {@link BufferedReader#readLine()} metódussal végbemehessen.
 * A nyers üzenetküldés miatt bármely a Javan kívül bármely más nyelvvel képes kommunikálni.
 * @author zoli
 */
public class BufferedStreamDeviceHandler extends DeviceHandler {

    public BufferedStreamDeviceHandler(InputStream in, OutputStream out) {
        super(in, out);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendStatus(String s) throws IOException {
        out.write((s.replace("\r", "").replace("\n", "") + "\r\n").getBytes());
        out.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readStatus() throws IOException {
        return new BufferedReader(new InputStreamReader(in) {

            private int count = 0;

            @Override
            public int read(char[] cbuf, int offset, int length) throws IOException {
                if (count >= 100) throw new RemoteHandlerException("long message", true);
                int bytes = super.read(cbuf, offset, length);
                count += bytes;
                return bytes;
            }

        }, 100).readLine().trim();
    }

}
