package org.dyndns.fzoli.chat.model;

import java.io.Serializable;

/**
 * Egy konkrét felhasználó nevét és annak egyik tulajdonságának változását tartalmazza.
 * @author zoli
 */
public abstract class UserPartialData<T extends Serializable> extends GroupChatPartialData<UserData, T> {

    /**
     * Annak a felhasználónak a neve, melynek egyik tulajdonsága megváltozott.
     */
    private final String USER_NAME;
    
    /**
     * Konstruktor szerver oldalra.
     * @param userName annak a felhasználónak a neve, melynek egyik tulajdonsága megváltozott
     * @param data a megváltozott adat új értéke
     */
    public UserPartialData(String userName, T data) {
        super(data);
        USER_NAME = userName;
    }

    /**
     * Konstruktor kliens oldalra.
     */
    public UserPartialData(T data) {
        this(null, data);
    }

    public final String getUserName() {
        return USER_NAME;
    }
    
    /**
     * A csoportos beszélgetésben résztvevő felhasználókon végigmegy és ha kell, frissíti az adatukat.
     */
    @Override
    public void apply(GroupChatData gcd) {
        if (gcd != null) {
            apply(gcd.getUsers());
        }
    }
    
    /**
     * Felhasználónév alapján megadja, hogy a részadat a paraméterben megadott modelhez tartozik-e.
     */
    @Override
    protected boolean equals(UserData user) {
        return UserData.equals(user, USER_NAME);
    }
    
}
