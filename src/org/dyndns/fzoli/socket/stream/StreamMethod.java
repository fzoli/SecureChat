package org.dyndns.fzoli.socket.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * ObjectInput és ObjectOutput folyam létrehozó.
 * A Java Object(I/O)Stream osztályai tökéletesen megfelelnek
 * objektumok továbbítására két Java alkalmazás között,
 * de ha csak az egyik program Java alapú, más megoldásra van szükség.
 * Erre lett létrehozva ez a segédosztály, ami létrehozza a megfelelő folyamokat.
 * Az Object(I/O)Stream tömörítéssel megspékelt használatát
 * a {@link MessageProcessObjectMethod} biztosítja, és a
 * JSON alapú szerializációt a {@link MessageProcessJsonMethod} biztosítja.
 * @author zoli
 */
public interface StreamMethod {

    /**
     * Az objektumok küldésére használható kimenő folyamot létrehozó metódus.
     */
    public ObjectOutput createObjectOutput(OutputStream out) throws IOException;
    
    /**
     * Az objektumok fogadására használható bejövő folyamot létrehozó metódus.
     */
    public ObjectInput createObjectInput(InputStream in) throws IOException;
    
}
