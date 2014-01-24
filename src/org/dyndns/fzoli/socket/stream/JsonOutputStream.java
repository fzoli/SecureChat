package org.dyndns.fzoli.socket.stream;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * JSON kimenő folyam a Google Gson API-ra alapozva.
 * Kizárólag objektumok küldésére használható, a többi metódus nincs implementálva!
 * @see #writeObject(Object)
 * @see JsonInputStream
 * @author zoli
 */
public class JsonOutputStream extends OutputStream implements ObjectOutput {

    private final static Gson gson = new Gson();
    
    private final OutputStream out;
    
    public JsonOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }
    
    /**
     * Objektum szerializálása és küldése.
     * @param obj a küldendő objektum
     */
    @Override
    public void writeObject(Object obj) throws IOException {
        write((obj.getClass().getName() + "\r\n").getBytes());
        write((gson.toJson(obj) + "\r\n\r\n").getBytes());
    }
    
//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        JsonOutputStream jout = new JsonOutputStream(out);
//        Date date = new Date();
//        jout.writeObject(date);
//        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
//        JsonInputStream jin = new JsonInputStream(in);
//        date = (Date) jin.readObject();
//        System.out.println(date);
//    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        ;
    }

    @Override
    public void writeByte(int v) throws IOException {
        ;
    }

    @Override
    public void writeShort(int v) throws IOException {
        ;
    }

    @Override
    public void writeChar(int v) throws IOException {
        ;
    }

    @Override
    public void writeInt(int v) throws IOException {
        ;
    }

    @Override
    public void writeLong(long v) throws IOException {
        ;
    }

    @Override
    public void writeFloat(float v) throws IOException {
        ;
    }

    @Override
    public void writeDouble(double v) throws IOException {
        ;
    }

    @Override
    public void writeBytes(String s) throws IOException {
        ;
    }

    @Override
    public void writeChars(String s) throws IOException {
        ;
    }

    @Override
    public void writeUTF(String s) throws IOException {
        ;
    }
    
}
