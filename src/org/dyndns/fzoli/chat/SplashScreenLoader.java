package org.dyndns.fzoli.chat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.SplashScreen;

/**
 * Nyitóképernyővel kapcsolatos metódusok.
 * @author zoli
 */
public class SplashScreenLoader {
    
    /**
     * A nyitóképernyő.
     * Null, ha nincs nyitóképernyő-kép megadva.
     */
    private static final SplashScreen splash;
    
    /**
     * A nyitóképernyő rajzterülete.
     * Null, ha nincs nyitóképernyő-kép megadva.
     */
    private static final Graphics2D g;
    
    /**
     * Inicializálás.
     * Ha nincs grafikus felület, nincs mit tenni.
     * Ha van grafikus felület és nyitóképernyő, a "Mobile-RC" felirat megjelenik,
     * és a {@code setSplashMessage} metódus boldogan teljesíti a kéréseket.
     */
    static {
        if (GraphicsEnvironment.isHeadless()) {
            splash = null;
            g = null;
        }
        else {
            SplashScreen spl;
            try {
                spl = SplashScreen.getSplashScreen();
            }
            catch (Throwable t) {
                // csak az OpenJDK és Oracle JRE tartalmazza a SplashScreen osztályt
                // más JRE alatt error képződik, amit kezelni kell
                spl = null;
            }
            splash = spl;
            if (splash != null) {
                g = splash.createGraphics();
                if (g != null) {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.PLAIN, 12));
                    updateSplash();
                }
            }
            else {
                g = null;
            }
        }
    }
    
    /**
     * Megmondja, látható-e még a nyitóképernyő.
     */
    public static boolean isVisible() {
        if (g == null) return false;
        return splash.isVisible();
    }
    
    /**
     * Bezárja a nyitóképernyőt, ha az megjelent.
     * Ha nincs mit bezárni, nem jelez hibát.
     */
    public static void closeSplashScreen() {
        try {
            splash.close();
        }
        catch (Exception ex) {
            ;
        }
    }
    
    /**
     * A nyitóképernyő tályékoztatószövegét állatja be.
     * Ha nincs megadva nyitóképernyő, nem tesz semmit.
     * @param s a kirajzolandó szöveg
     */
    public static void setSplashMessage(String s) {
        if (g != null && s != null && splash.isVisible()) {
            int y = 192;
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(1, y - 10, splash.getSize().width - 2, 20);
            g.setPaintMode();
            printString(s + (s.isEmpty() ? "" : "..."), y);
            updateSplash();
        }
    }
    
    /**
     * A felületre középre igazítva írja ki a megadott szöveget.
     * Ellenőrizetlen metódus, ezért private.
     * @param g a felület, amire kirajzolódik a szöveg
     * @param s a kirajzolandó szöveg
     * @param width a felület szélessége pixelben
     * @param y a magasság koordináta
     */
    private static void printString(String s, int y) {
        int len = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
        int start = splash.getSize().width / 2 - len / 2;
        g.drawString(s, start, y);
    }
    
    /**
     * Frissíti a splash képernyőt, ha tudja.
     * Semmi kivételt nem dob.
     */
    private static void updateSplash() {
        try {
            splash.update();
        }
        catch (Exception ex) {
            ;
        }
    }
    
}
