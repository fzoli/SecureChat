package org.dyndns.fzoli.socket.handler;

import java.io.IOException;
import org.dyndns.fzoli.socket.handler.exception.HandlerException;
import org.dyndns.fzoli.socket.handler.exception.RemoteHandlerException;

/**
 * AbstractClientHandler és AbstractServerHandler implementálásához.
 * @author zoli
 */
class HandlerUtil {
    
    /**
     * Az inicializáló metódust kivételkezelten meghívja és közli a távoli eszköznek az eredményt.
     * @throws Exception ha inicializálás közben kivétel történt
     * @throws IOException ha nem sikerült a kimenetre írni
     */
    public static int runInit(AbstractHandler handler, DeviceHandler dh) throws IOException, Exception {
        try {
            // inicializáló metódus futtatása
            int id = handler.init();
            // rendben jelzés küldése a távoli eszköznek
            dh.sendStatus(HandlerException.VAL_OK);
            return id;
        }
        catch (IOException ex) {
            // nem sikerült a rendben jelzés küldése, ezért nem próbál üzenetet küldeni
            throw ex;
        }
        catch (HandlerException ex) {
            // a kivétel üzenetét távoli eszköznek is
            dh.sendStatus(ex.getMessage());
            // a kivétel megy tovább, mint ha semmi nem történt volna
            throw ex;
        }
        catch (Exception ex) {
            // olyan kivétel keletkezett, mely nem várt hiba
            dh.sendStatus("unexpected error");
            // a kivétel megy tovább...
            throw ex;
        }
    }
    
    /**
     * Megpróbálja az üzenetet fogadni a távoli géptől.
     * Ha a másik oldalon hiba keletkezett, kivételt dob.
     * @throws IOException ha nem sikerült a fogadás
     * @throws RemoteHandlerException ha a másik oldalon hiba keletkezett
     */
    public static void readStatus(DeviceHandler dh) throws IOException {
        String status = dh.readStatus();
        if (status == null) {
            throw new RemoteHandlerException("unknown error");
        }
        else if (!status.equals(HandlerException.VAL_OK)) {
            throw new RemoteHandlerException(status);
        }
    }
    
}
