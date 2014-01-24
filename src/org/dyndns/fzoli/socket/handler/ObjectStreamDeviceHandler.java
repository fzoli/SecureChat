package org.dyndns.fzoli.socket.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Eszközkezelő I/O segédosztály.
 * {@code Object(I/O)Stream} osztályokon alapszik, ezért csak Java alkalmazásokkal képes kommunikálni.
 * @author zoli
 */
public class ObjectStreamDeviceHandler extends DeviceHandler {

    public ObjectStreamDeviceHandler(InputStream in, OutputStream out) {
        super(in, out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendStatus(String s) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeUTF(s);
        oos.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readStatus() throws IOException {
        ObjectInputStream oin = new ObjectInputStream(in);
        return oin.readUTF();
    }
    
}
