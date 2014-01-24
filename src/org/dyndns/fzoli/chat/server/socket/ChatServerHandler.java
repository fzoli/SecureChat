package org.dyndns.fzoli.chat.server.socket;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import org.dyndns.fzoli.chat.ConnectionKeys;
import static org.dyndns.fzoli.chat.server.ConnectionAlert.logMessage;
import org.dyndns.fzoli.chat.server.Main;
import org.dyndns.fzoli.chat.server.config.Permissions;
import org.dyndns.fzoli.socket.handler.AbstractSecureServerHandler;
import org.dyndns.fzoli.socket.handler.BufferedStreamDeviceHandler;
import org.dyndns.fzoli.socket.handler.DeviceHandler;
import org.dyndns.fzoli.socket.process.SecureProcess;
import static org.dyndns.fzoli.chat.server.Main.getString;
import org.dyndns.fzoli.socket.handler.exception.MultipleCertificateException;
import org.dyndns.fzoli.socket.handler.exception.RemoteHandlerException;
import org.dyndns.fzoli.socket.handler.exception.SecureHandlerException;
import org.dyndns.fzoli.ui.systemtray.TrayIcon.IconType;

/**
 * ChatServer Process példányosító.
 * @author zoli
 */
public class ChatServerHandler extends AbstractSecureServerHandler implements ConnectionKeys {

    /**
     * Alapértelmezetten a figyelmeztetések be vannak kapcsolva.
     */
    private static boolean show = true;
    
    /**
     * Kapcsolatazonosítók, melyek kapcsolataiban keletkező hibák nem érdekesek.
     */
    private static final List<Integer> HIDDEN_WARN_KEYS = createHiddenWarningKeys();
    
    /**
     * A magyar nyelv magánhangzóit tartalmazza.
     * Arra kell, hogy el lehessen dönteni, a vagy az névelő kerüljön-e a cím elé.
     */
    private static final char[] mgh = {'a', 'á', 'e', 'é', 'i', 'í', 'o', 'ó', 'ö', 'ő', 'u', 'ú', 'ü', 'ű'};
    
    public ChatServerHandler(SSLSocket socket) {
        super(socket);
    }
    
    @Override
    protected DeviceHandler createDeviceHandler(InputStream in, OutputStream out) {
        return new BufferedStreamDeviceHandler(in, out);
    }
    
    @Override
    protected SecureProcess selectProcess() {
        if (getDeviceId() == KEY_DEV_CLIENT) {
            switch (getConnectionId()) {
                case KEY_CONN_DISCONNECT:
                    return new ChatServerDisconnectProcess(this);
                case KEY_CONN_MESSAGE:
                    return new ChatServerMessageProcess(this);
            }
        }
        return null;
    }
    
    /**
     * Miután inicializálódtak az adatok, a szerver megnézi, hogy a kapcsolódott kliens tanúsítványneve blokkolva van-e,
     * és ha blokkolva van, kivételt dob, tehát a kapcsolatot elutasítja.
     * A disconnect process felelőssége a klienseknek való jelzés a felhasználó kapcsolódásáról illetve lekapcsolódásáról,
     * ezért a kapcsolat akkor is tiltásra kerül, ha az első kapcsolatfelvételkor nem disconnect process jön létre, ezzel elkerülve az észrevétlen lehallgatást.
     */
    @Override
    protected int init() {
        int id = super.init();
        if (Permissions.getConfig().isBlocked(getRemoteCommonName())) throw new BlockedCommonNameException();
        if (getConnectionId() != KEY_CONN_DISCONNECT && !getActiveConnectionIds().contains(KEY_CONN_DISCONNECT)) throw new RemoteHandlerException("Wrong order", true);
        return id;
    }

    /**
     * Alapértelmezésként nem keres online tanúsítványokat ugyan azzal a CN mezővel, hogy a szerverre több helyről is fel tudjanak csatlakozni egyazon felhasználónévvel.
     * Ha a konfigurációban a single paraméter true, akkor lefut a keresés.
     * FONTOS! A kliensnek jeleznie kell azt ha egyazon névvel többször kapcsolódtak fel a szerverre, hogy a beszélgető felhasználók lássák,
     * ha esetleg valaki megszerezte egy online felhasználó titkos kulcsát és kémkedik.
     */
    @Override
    protected void denyMultipleCerts() {
        if (Main.CONFIG.isSingle()) super.denyMultipleCerts();
    }
    
    /**
     * Ha kivétel képződik a szálban, fel kell dolgozni.
     * Duplázott tanúsítvány esetén figyelmezteti a felhasználót.
     * Más kivétel nem várt hibát eredményeznek.
     * @param ex a kivétel
     * @throws Exception a paraméterben átadott kivételt, ha nem várt hiba történt
     */
    @Override
    protected void onException(Exception ex) {
        try {
            throw ex;
        }
        catch (BlockedCommonNameException e) {
            showWarning(getString("warn_conn1")); // tiltott kapcsolódás
        }
        catch (MultipleCertificateException e) {
            showWarning(getString("warn_conn2")); // duplázott tanúsítvány
        }
        catch (SecureHandlerException e) {
            showWarning(getString("warn_conn5"), e.getMessage()); // tanúsítvány hiba
        }
        catch (SSLHandshakeException e) {
            // szerverhez való tanúsítvány érkezett vagy nem megbízható kapcsolódás
            showWarning(getString("warn_conn" + (e.getMessage().contains("Extended key usage") ? '3' : '4')));
        }
        catch (SSLException e) {
            showWarning(getString("warn_conn6"), e.getMessage()); // SSL hiba
        }
        catch (RemoteHandlerException e) {
            showWarning(getString("warn_conn7"), e.getMessage(), e.isImportant()); // távoli hiba
        }
        catch (SocketException e) {
            showWarning(getString("warn_conn8"), e.getMessage()); // socket hiba
        }
        catch (SocketTimeoutException e) {
            showWarning(getString("warn_conn9"), e.getMessage()); // socket időtúllépés
        }
        catch (EOFException e) {
            showWarning(getString("warn_conn10"), e.getMessage()); // váratlan socket bezárás
        }
        catch (Exception e) {
            showWarning(getString("warn_conn11"), e.getMessage()); // nem várt hiba
        }
    }
    
    /**
     * Ha adott klienstől az első kapcsolatfelvétel közben hiba keletkezik, jelzi a felhasználónak és naplózza a hibát.
     * @param message a kijelzendő üzenet
     */
    private void showWarning(String message) {
        showWarning(message, false);
    }
    
    /**
     * Ha adott klienstől az első kapcsolatfelvétel közben hiba keletkezik, jelzi a felhasználónak.
     * @param message a kijelzendő üzenet
     * @param details további részlet az üzenet mellé
     */
    private void showWarning(String message, String details) {
        showWarning(message, details, false);
    }
    
    /**
     * Ha adott klienstől az első kapcsolatfelvétel közben hiba keletkezik, jelzi a felhasználónak és naplózza a hibát.
     * @param message a kijelzendő üzenet
     * @param important megjeleníti a hibát akkor is, ha az nem a disconnect socket
     */
    private void showWarning(String message, boolean important) {
        if ((important || getConnectionId() == null || !HIDDEN_WARN_KEYS.contains(getConnectionId())) && getSocket() != null) {
            String name = getSocket().getInetAddress().getHostName();
            String close = getString("warn_addr_prep2");
            logMessage(Main.VAL_WARNING, message + ' ' + getString("warn_addr_prep1" + (Arrays.binarySearch(mgh, name.charAt(0)) >= 0 ? 'b' : 'a')) + ' ' + name + " [" + getSocket().getInetAddress().getHostAddress() + "]" + (close.trim().isEmpty() ? "" : " ") + close + '.', IconType.WARNING, isWarnEnabled());
        }
    }
    
    /**
     * Ha adott klienstől az első kapcsolatfelvétel közben hiba keletkezik, jelzi a felhasználónak.
     * @param message a kijelzendő üzenet
     * @param details további részlet az üzenet mellé
     * @param important megjeleníti a hibát akkor is, ha az nem a disconnect socket
     */
    private void showWarning(String message, String details, boolean important) {
        if (details == null || details.isEmpty()) showWarning(message, important);
        else showWarning(message + " (" + details + ")", important);
    }

    /**
     * Ha a kiválasztott Process null, fel kell dolgozni.
     * Jelzi a felhasználónak, hogy ismeretlen kérést kapott a szerver, majd
     * bezárja az összes többi kapcsolatot, ami már létre lett hozva az adott klienssel.
     */
    @Override
    protected void onProcessNull() {
        showWarning(getString("warn_conn12")); // ismeretlen kérés
        super.onProcessNull();
    }
    
    /**
     * Megadja, hogy a figyelmeztetések megjelenhetnek-e.
     */
    public static boolean isWarnEnabled() {
        return show;
    }
    
    /**
     * Bekapcsolja vagy kikapcsolja a figyelmeztetéseket.
     */
    public static void setWarnEnabled(boolean enabled) {
        show = enabled;
    }
    
    /**
     * Létrehoz egy listát, melyben azok a kapcsolatazonosítók szerepelnek,
     * melyek kapcsolatainak kezelésekor keletkező hibák figyelmen kívül lesznek hagyva.
     */
    private static List<Integer> createHiddenWarningKeys() {
        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(KEY_CONN_MESSAGE);
        return l;
    }
    
}
