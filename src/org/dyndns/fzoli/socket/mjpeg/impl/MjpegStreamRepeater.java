package org.dyndns.fzoli.socket.mjpeg.impl;

import java.io.InputStream;
import java.io.OutputStream;
import org.dyndns.fzoli.socket.mjpeg.JpegProvider;
import org.dyndns.fzoli.socket.mjpeg.jipcam.MjpegInputStream;

/**
 * MJPEG-folyam továbbító.
 * @author zoli
 */
public class MjpegStreamRepeater extends JpegProvider {

    /**
     * A továbbítandó MJPEG-folyam.
     */
    private final MjpegInputStream in;
    
    /**
     * Megadja, hogy be kell-e fejezni a folyam továbbítását.
     */
    private boolean interrupted;
    
    /**
     * Konstruktor.
     * @param in a továbbítandó folyam
     * @param out a kimenet, amire a továbbítás történik
     */
    public MjpegStreamRepeater(InputStream in, OutputStream out) {
        this(in, out, true);
    }
    
    /**
     * Konstruktor.
     * @param in a továbbítandó folyam
     * @param out a kimenet, amire a továbbítás történik
     * @param sendHeader false esetén nem küldi ki a szerver- és boundary adatokat
     */
    public MjpegStreamRepeater(InputStream in, OutputStream out, boolean sendHeader) {
        super(out);
        this.in = new MjpegInputStream(in);
        setSendInfoHeader(sendHeader);
    }

    /**
     * Megadja, kell-e streamelni továbbra is.
     */
    @Override
    protected boolean isInterrupted() {
        return interrupted;
    }

    /**
     * Beállítja, kell-e streamelni továbbra is.
     * @param interrupted true esetén befejeződik a streamelés
     */
    protected void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    /**
     * Kiolvassa a következő képkockát a streamből.
     */
    @Override
    protected byte[] getFrame() {
        try {
            return in.readMjpegFrame().getJpegBytes();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * A képkocka beállítására nincs szükség, mivel a bejövő folyamból olvasódik ki.
     */
    @Override
    protected final void setFrame(byte[] frame) {
        ;
    }

    /**
     * Elkezdi a stream továbbítását.
     */
    @Override
    public void handleConnection() {
        setInterrupted(false);
        super.handleConnection();
    }
    
}
