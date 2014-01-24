package org.dyndns.fzoli.chat.model;

import java.util.Collection;
import java.util.Date;

/**
 * Chat üzenet ami egyben adat és részadat is.
 * @author zoli
 */
public class ChatMessage extends GroupChatPartialData<ChatMessage, String> {
    
    /**
     * Szerver oldalra egyedi azonosítót generál.
     */
    private static int ID_INCREMENTER = 1;
    
    /**
     * Egyedi azonosító auto increment alapokon.
     */
    private final Integer ID;
    
    /**
     * Az üzenet küldőjének felhasználóneve.
     */
    private final String SENDER;
    
    /**
     * Az üzenet első küldésének dátuma.
     * Csak a szerver állíthatja be az egységes dátumozás érdekében.
     */
    private final Date DATE;
    
    /**
     * Kliens oldal - új üzenet küldéséhez.
     */
    public ChatMessage(String msg) {
        this(msg, (Integer) null);
    }
    
    /**
     * Kliens oldal - üzenet módosítására.
     */
    public ChatMessage(String msg, Integer id) {
        super(msg);
        SENDER = null;
        DATE = null;
        ID = id;
    }
    
    /**
     * Szerver oldal - kliens által fogadott új üzenet.
     */
    public ChatMessage(String sender, String msg) {
        super(msg);
        SENDER = sender;
        DATE = new Date();
        ID = ID_INCREMENTER++;
    }

    /**
     * Szerver oldal - kliens által fogadott átírt üzenet.
     */
    public ChatMessage(ChatMessage cm, String msg) {
        super(msg);
        SENDER = cm.getSender();
        DATE = cm.getDate();
        ID = cm.getID();
    }
    
    /**
     * Szerver és kliens oldal - másolat készítése.
     */
    public ChatMessage(ChatMessage cm) {
        this(cm, cm.getMessage());
    }
    
    public Integer getID() {
        return ID;
    }

    public String getSender() {
        return SENDER;
    }

    public Date getDate() {
        return DATE;
    }

    public String getMessage() {
        return data == null ? "" : data;
    }

    public void setMessage(String msg) {
        data = msg;
    }

    @Override
    public void apply(ChatMessage d) {
        if (equals(d)) {
            setMessage(d.getMessage());
        }
    }

    /**
     * A részadat kollekcióban való feldolgozása.
     * Ha az üzenet már benne van a kollekcióban (ID alapján), akkor átírásra kerül; ha nincs, a kollekcióhoz lesz adva.
     * Ha az üzenetnek az azonosítója nincs meghatározva, akkor a metódusnak nem lesz hatása a kollekcióra.
     */
    @Override
    public int apply(Collection<ChatMessage> datas) {
        int count = getID() == null ? -1 : super.apply(datas);
        if (count == 0) {
            datas.add(this);
            count = 1;
        }
        return count;
    }
    
    @Override
    public void apply(GroupChatData gcd) {
        if (gcd != null) {
            apply(gcd.getMessages());
        }
    }

    /**
     * Megadja, hogy a két üzenetnek egyezik-e az azonosítója.
     */
    @Override
    public boolean equals(ChatMessage d) {
        return equals(d, getID());
    }

    /**
     * Megadja, hogy az üzenetnek egyezik-e az azonosítója a paraméterben átadottal.
     */
    public static boolean equals(ChatMessage d, Integer id) {
        return d != null && d.getID() != null && d.getID().equals(id);
    }
    
}