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
    private final String SENDER, FULL_NAME;
    
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
        this(id, null, null, null, msg);
    }
    
    /**
     * Szerver oldal - kliens által fogadott új üzenet.
     */
    public ChatMessage(String sender, String fullName, String msg) {
        this(ID_INCREMENTER++, new Date(), sender, fullName, msg);
    }

    /**
     * Szerver oldal - kliens által fogadott átírt üzenet.
     */
    public ChatMessage(ChatMessage cm, String msg) {
        this(cm.getID(), cm.getDate(), cm.getSender(), cm.getFullName(), cm.getMessage());
    }
    
    /**
     * Szerver és kliens oldal - másolat készítése.
     */
    public ChatMessage(ChatMessage cm) {
        this(cm, cm.getMessage());
    }
    
    /**
     * Egyéb esetekre.
     */
    public ChatMessage(Integer id, Date date, String sender, String fullName, String msg) {
        super(msg);
        SENDER = sender;
        FULL_NAME = fullName;
        DATE = date;
        ID = id;
    }
    
    public Integer getID() {
        return ID;
    }

    public String getSender() {
        return SENDER;
    }

    public String getFullName() {
        return FULL_NAME == null ? SENDER : FULL_NAME;
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
            d.setMessage(getMessage());
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
