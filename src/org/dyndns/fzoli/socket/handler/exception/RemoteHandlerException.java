package org.dyndns.fzoli.socket.handler.exception;

/**
 * A távoli eszközön keletkezett kivétel.
 * @author zoli
 */
public class RemoteHandlerException extends HandlerException {

    /**
     * Tárolja, hogy fontos-e a hiba a naplózás szempontjából.
     */
    private final boolean IMPORTANT;
    
    /**
     * Saját kivétel létrehozása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem a rendben jelzés: {@code HandlerException.VAL_OK}
     */
    public RemoteHandlerException(String message, boolean important) {
        super(message);
        IMPORTANT = important;
    }
    
    /**
     * Saját kivétel létrehozása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem a rendben jelzés: {@code HandlerException.VAL_OK}
     */
    public RemoteHandlerException(String message) {
        super(message);
        IMPORTANT = false;
    }

    /**
     * Már létező kivétel felhasználása.
     */
    public RemoteHandlerException(Throwable cause) {
        super(cause);
        IMPORTANT = false;
    }

    /**
     * Már létező kivétel felhasználása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem rendben jelzés.
     */
    public RemoteHandlerException(String message, Throwable cause) {
        super(message, cause);
        IMPORTANT = false;
    }

    /**
     * Megadja, hogy fontos-e a hiba a naplózás szempontjából.
     */
    public boolean isImportant() {
        return IMPORTANT;
    }
    
}
