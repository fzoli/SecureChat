package org.dyndns.fzoli.chat.model;

import java.util.Date;

/**
 * Egy konkrét felhasználó adatait tároló model.
 * Egy felhasználónak van felhasználóneve, teljes neve és státusza (szabad, elfoglalt, nincs a gépnél).
 * A felhasználónév és a teljes név fix, mivel az a tanúsítványban van tárolva;
 * egyedül a státusz változhat meg, amire részadat is lett írva: {@link UserPartialStatusData}
 * @author zoli
 */
public class UserData extends Data<UserData, UserPartialStatusData> {
    
    public enum Status {
        FREE,
        BUSY,
        AWAY
    }
    
    private String userName, fullName;
    private Date signInDate, signOutDate;
    
    private Status status = Status.FREE;
    private Integer clientCount = 1;

    public UserData() {
    }
    
    public UserData(String userName, String fullName) {
        this.userName = userName;
        this.fullName = fullName;
    }

    public UserData(UserData d) {
        clientCount = d.getClientCount();
        userName = d.getUserName();
        fullName = d.getFullName();
        status = d.getStatus();
        signInDate = d.getSignInDate();
        signOutDate = d.getSignOutDate();
    }
    
    public Integer getClientCount() {
        return clientCount;
    }

    public String getUserName() {
        return userName;
    }

    public String getFullName() {
        return fullName;
    }

    public Status getStatus() {
        return status;
    }

    public Date getSignInDate() {
        return signInDate;
    }

    public Date getSignOutDate() {
        return signOutDate;
    }

    public void setClientCount(Integer clientCount) {
        this.clientCount = clientCount;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setSignInDate(Date signInDate) {
        this.signInDate = signInDate;
    }

    public void setSignOutDate(Date signOutDate) {
        this.signOutDate = signOutDate;
    }
    
    @Override
    public void update(UserData d) {
        if (d != null) {
            setClientCount(d.getClientCount());
            setUserName(d.getUserName());
            setFullName(d.getFullName());
            setStatus(d.getStatus());
            setSignInDate(d.getSignInDate());
            setSignOutDate(d.getSignOutDate());
        }
    }

    @Override
    public void clear() {
        clientCount = null;
        userName = null;
        fullName = null;
        status = null;
        signInDate = null;
        signOutDate = null;
    }
    
    /**
     * Megadja, hogy a két felhasználó objektum felhasználóneve megegyezik-e.
     * @return ha az egyik objektum null, akkor false; egyébként ha a felhasználónevük definiált és egyezik, akkor true
     */
    public static boolean equals(UserData user1, UserData user2) {
        return equals(user1, user2 == null ? null : user2.getUserName());
    }
    
    /**
     * Megadja, hogy a megadott felhasználó objektum illik-e a megadott felhasználónévhez.
     * @return ha az objektum vagy a felhasználónév nincs definiálva az objektumban, hamissal tér vissza; egyébként ha egyezik a felhasználónév, igazzal
     */
    public static boolean equals(UserData user, String userName) {
        return user != null && user.getUserName() != null && user.getUserName().equals(userName);
    }
    
}
