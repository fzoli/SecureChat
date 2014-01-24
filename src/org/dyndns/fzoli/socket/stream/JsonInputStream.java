package org.dyndns.fzoli.socket.stream;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;

/**
 * JSON bejövő folyam a Google Gson API-ra alapozva.
 * Kizárólag objektumok fogadására használható, a többi metódus nincs implementálva!
 * @see #readObject()
 * @see JsonOutputStream
 * @author zoli
 */
public class JsonInputStream extends InputStream implements ObjectInput {

    private final static Gson gson = new Gson();
    
    private final BufferedReader in;
    
    public JsonInputStream(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));
    }
    
    @Override
    public int read() throws IOException {
        return in.read();
    }

    /**
     * Objektum fogadása és deszerializálása.
     * @return a fogadott objektum
     */
    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        Class<?> classType = Class.forName(in.readLine());
        String line;
        StringBuffer sb = new StringBuffer();
        while (!(line = in.readLine()).isEmpty()) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), classType);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        ;
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        ;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return 0;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return false;
    }

    @Override
    public byte readByte() throws IOException {
        return 0;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return 0;
    }

    @Override
    public short readShort() throws IOException {
        return 0;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return 0;
    }

    @Override
    public char readChar() throws IOException {
        return '\0';
    }

    @Override
    public int readInt() throws IOException {
        return 0;
    }

    @Override
    public long readLong() throws IOException {
        return 0;
    }

    @Override
    public float readFloat() throws IOException {
        return 0;
    }

    @Override
    public double readDouble() throws IOException {
        return 0;
    }

    @Override
    public String readLine() throws IOException {
        return "";
    }

    @Override
    public String readUTF() throws IOException {
        return "";
    }
    
}
