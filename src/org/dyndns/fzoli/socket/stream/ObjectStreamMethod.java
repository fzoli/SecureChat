package org.dyndns.fzoli.socket.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.dyndns.fzoli.socket.compress.CompressedBlockInputStream;
import org.dyndns.fzoli.socket.compress.CompressedBlockOutputStream;

/**
 * ObjectInputStream és ObjectOutputStream létrehozó.
 * @see StreamMethod
 * @author zoli
 */
public class ObjectStreamMethod implements StreamMethod {

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectOutput createObjectOutput(OutputStream out) throws IOException {
        return new ObjectOutputStream(new CompressedBlockOutputStream(out, 2000));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInput createObjectInput(InputStream in) throws IOException {
        return new ObjectInputStream(new CompressedBlockInputStream(in));
    }
    
}
