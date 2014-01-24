package org.dyndns.fzoli.chat.server.config;

/**
 * A tiltólista, a fehérlista és a feketelista alapján működő jogosultság-konfiguráció.
 * A három lista összesítése, ami konfig frissítéshez, rangsor létrehozáshoz
 * és jogosultság olvasáshoz használható fel.
 * @see BlocklistConfig
 * @see BlacklistConfig
 * @see WhitelistConfig
 * @author zoli
 */
public class PermissionConfig {
    
    /**
     * Tiltólista.
     */
    private BlocklistConfig blocklist;

    /**
     * Konstruktor, amit a {@link Permissions} osztály is használ.
     * Friss konfiguráció létrehozására használható fel.
     */
    protected PermissionConfig() {
        this(new BlocklistConfig());
    }
    
    /**
     * Konstruktor, amit csak ez az osztály használ.
     * Paraméterben megadható, milyen konfigurációkat használjon, így új és régi konfig is megadható.
     * @param blocklist a tiltólista
     * @param blacklist a feketelista
     * @param whitelist a fehérlista
     */
    private PermissionConfig(BlocklistConfig blocklist) {
        this.blocklist = blocklist;
    }
    
    /**
     * Frissíti a konfigurációt, ha módosult és a régi konfigurációval tér vissza.
     * @return null, ha a konfiguráció nem módosult, egyébként a frissítés előtti konfiguráció
     */
    protected PermissionConfig refresh() {
        PermissionConfig old = null;
        if (blocklist.isOutdated()) {
            old = new PermissionConfig(blocklist);
            if (blocklist.isOutdated()) blocklist = new BlocklistConfig();
        }
        return old;
    }
    
    /**
     * Megadja, hogy a konfigurációt tartalmazó fájlok olvashatóak-e.
     */
    public boolean canRead() {
        return blocklist.canRead();
    }
    
    /**
     * Megadja, hogy a tanúsítvány-név szerepel-e a tiltólistán.
     * @param name a tanúsítványnév
     */
    public boolean isBlocked(String name) {
        return blocklist.getValues().contains(name);
    }
    
}
