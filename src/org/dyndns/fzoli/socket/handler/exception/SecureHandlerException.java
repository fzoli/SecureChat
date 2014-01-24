package org.dyndns.fzoli.socket.handler.exception;

/**
 * A SecureHandler SSL műveletei közben fellépő kivétel.
 * @author zoli
 */
public class SecureHandlerException extends HandlerException {

    /**
     * Saját kivétel létrehozása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem a rendben jelzés: {@code HandlerException.VAL_OK}
     */
    public SecureHandlerException(String message) {
        super(message);
    }

    /**
     * Már létező kivétel felhasználása.
     */
    public SecureHandlerException(Throwable cause) {
        super(cause);
    }

    /**
     * Már létező kivétel felhasználása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem rendben jelzés.
     */
    public SecureHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
