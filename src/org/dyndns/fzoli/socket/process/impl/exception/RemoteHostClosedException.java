package org.dyndns.fzoli.socket.process.impl.exception;

/**
 * Azt jelzi, hogy a távoli fél bezárta a kapcsolatot önszántából.
 * @author zoli
 */
public class RemoteHostClosedException extends Exception {

    public RemoteHostClosedException() {
        super("Remote host closed the connection");
    }
    
}
