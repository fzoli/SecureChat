package org.dyndns.fzoli.socket.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * JsonInputStream és JsonOutputStream létrehozó.
 * @see StreamMethod
 * @author zoli
 */
public class JsonStreamMethod implements StreamMethod {

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectOutput createObjectOutput(OutputStream out) throws IOException {
        return new JsonOutputStream(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInput createObjectInput(InputStream in) throws IOException {
        return new JsonInputStream(in);
    }
    
}
