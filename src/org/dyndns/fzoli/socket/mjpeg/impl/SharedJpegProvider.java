package org.dyndns.fzoli.socket.mjpeg.impl;

import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dyndns.fzoli.socket.mjpeg.JpegProvider;

/**
 * Megosztott MJPEG streamelő.
 * Mindegyik objektum ugyan azt a képkocka objektumot használja.
 * @author zoli
 */
public abstract class SharedJpegProvider extends JpegProvider {

    /**
     * A megosztott képkockákat tároló map.
     */
    private static final Map<String, byte[]> FRAMES = Collections.synchronizedMap(new HashMap<String, byte[]>());
    
    /**
     * Konstruktor.
     * @param out az MJPEG kimenő folyam
     */
    public SharedJpegProvider(OutputStream out) {
        super(out);
    }

    /**
     * Folyam-azonosító, ami eldönti, hogy melyik képkockát adja vissza a {@link #getFrame()} metódus.
     */
    public abstract String getKey();
    
    /**
     * Hogy ha a kulcs null, nincs mit olvasni, tehát nem olvasható ki a következő képkocka.
     */
    @Override
    protected boolean isUnreadable() {
        return getKey() == null;
    }
    
    /**
     * A JPEG képkocka adatát adja vissza bájt tömbben.
     * Mindegyik objektumhoz ugyan az a képkocka referencia tartozik.
     * @param key a folyam azonosító
     */
    @Override
    protected final byte[] getFrame() {
        return FRAMES.get(getKey());
    }

    /**
     * A JPEG képkocka adatát állítja be.
     * Mindegyik objektumhoz ugyan az a képkocka referencia tartozik.
     * @param key a folyam azonosító
     * @param frame a képkocka adata bájt tömbben
     */
    @Override
    protected final void setFrame(byte[] frame) {
        setSharedFrame(getKey(), frame);
    }
    
    /**
     * A JPEG képkocka adatát állítja be.
     * @param key a folyam-azonosító
     * @param frame a képkocka adata bájt tömbben
     */
    public static void setSharedFrame(String key, byte[] frame) {
        if (key != null) FRAMES.put(key, frame);
    }
    
}
