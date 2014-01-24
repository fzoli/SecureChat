package org.dyndns.fzoli.socket.handler.exception;

/**
 * A Handler adatfeldolgozása közben fellépő kivétel.
 * @author zoli
 */
public class HandlerException extends RuntimeException {

    /**
     * A szerver elküldi a kliensnek a kivétel üzenetét.
     * Ha nem keletkezett kivétel a Handler inicializálásakor,
     * akkor a szerver jelzi, hogy minden rendben.
     * Ehhez kell egy előre megállapodás, hogy mi a renben jelzés.
     * Ezt az értéket tárolja ez a konstans.
     */
    public static final String VAL_OK = "OK";
    
    /**
     * Saját kivétel létrehozása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem a rendben jelzés: {@code HandlerException.VAL_OK}
     */
    public HandlerException(String message) {
        super(createMessage(message));
    }
    
    /**
     * Már létező kivétel felhasználása.
     */
    public HandlerException(Throwable cause) {
        super(createMessage(cause == null ? null : cause.getMessage()), cause);
    }

    /**
     * Már létező kivétel felhasználása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem rendben jelzés.
     */
    public HandlerException(String message, Throwable cause) {
        super(createMessage(message), cause);
    }
    
    /**
     * Az üzenetet ellenőrzi.
     * Az üzenet nem lehet OK vagy null, ez esetben üres lesz az üzenet.
     * Ha az üzenet megfelel, nem változik tartalma.
     */
    private static String createMessage(String message) {
        if (message == null || message.toUpperCase().equals(VAL_OK)) return "";
        else return message;
    }
    
}
