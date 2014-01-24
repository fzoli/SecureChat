package org.dyndns.fzoli.socket.handler.exception;

/**
 * Akkor keletkezik ez a kivétel, ha ugyan azzal a tanúsítvánnyal többen is kapcsolódnak a szerverhez.
 * @author zoli
 */
public class MultipleCertificateException extends SecureHandlerException {

    /**
     * Saját kivétel létrehozása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem a rendben jelzés: {@code HandlerException.VAL_OK}
     */
    public MultipleCertificateException(String message) {
        super(message);
    }

    /**
     * Már létező kivétel felhasználása.
     */
    public MultipleCertificateException(Throwable cause) {
        super(cause);
    }

    /**
     * Már létező kivétel felhasználása saját üzenettel.
     * Az üzenet tartalma bármi lehet, ami nem rendben jelzés.
     */
    public MultipleCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
