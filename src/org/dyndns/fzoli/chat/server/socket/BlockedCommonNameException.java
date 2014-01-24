package org.dyndns.fzoli.chat.server.socket;

import org.dyndns.fzoli.socket.handler.exception.HandlerException;

/**
 * Ha a kliens tanúsítvány-neve szerepel a tiltólistában,
 * a szerver nem engedi kapcsolódni a kliens gépet.
 * Ez esetben keletkezik ez a kivétel.
 * @author zoli
 */
public class BlockedCommonNameException extends HandlerException {

    public BlockedCommonNameException() {
        super("The client's Common Name is blocked");
    }
    
}
