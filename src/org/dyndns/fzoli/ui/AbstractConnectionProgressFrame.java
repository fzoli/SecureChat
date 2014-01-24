package org.dyndns.fzoli.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Kapcsolódásjelző- és kezelő ablak.
 * Csak akkor jelenik meg, ha valamiért nem sikerült első alkalommal kapcsolódni a szerverhez.
 * A felhasználó lehetőségei:
 * - beállítja a konfigurációt: az ablak nem látható, míg be nem zárja a beállításokat
 * - újra próbálkozik kapcsolódni: indikátor jelzi a folyamatot és amíg tart, nem lehet újra próbálkozni
 * - kilép a programból: végetér a program futása
 * @author zoli
 */
public abstract class AbstractConnectionProgressFrame extends JFrame implements OkCancelPanel.OkCancelWindow {
    
    /**
     * Újra gomb.
     * Meghívja az {@code onAgain} metódust, ha kiválasztják.
     */
    private final JButton btAgain = new JButton() {
        {
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    onAgain();
                }
                
            });
        }
    };
    
    /**
     * Kapcsolatbeállítás gomb.
     * Meghívja az {@code onSettings} metódust, ha kiválasztják.
     */
    private final JButton btSettings = new JButton() {
        {
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    onSettings();
                }
                
            });
        }
    };
    
    /**
     * Kilépés gomb.
     * Rákattintva az {@link #onExit()} metódus fut le.
     */
    private final JButton btExit = new JButton() {
        {
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    onExit();
                }
                
            });
        }
    };
    
    /**
     * Az ablakon megjeleníthető paneleket tartalmazza.
     */
    private final IconTextPanel[] PANELS;
    
    /**
     * Konstruktor.
     * Alapértelmezetten hibaüzenetet mutat a panel.
     */
    public AbstractConnectionProgressFrame(IconTextPanel[] panels) {
        this("Connection handler", "Try again", "Connection settings", "Exit", panels);
    }
    
    /**
     * Konstruktor.
     * Alapértelmezetten hibaüzenetet mutat a panel.
     */
    public AbstractConnectionProgressFrame(String title, String again, String connSettings, String exit, IconTextPanel[] panels) {
        super(title);
        PANELS = panels;
        btAgain.setText(again);
        btSettings.setText(connSettings);
        btExit.setText(exit);
        
        setLayout(new GridBagLayout()); // kedvenc elrendezésmenedzserem alkalmazása
        setDefaultCloseOperation(EXIT_ON_CLOSE); // X-re kattintva vége a programnak
        setResizable(false); // átméretezés tiltása
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; // teljes helykitöltés, ...
        c.weightx = 1; // ... hogy az ikon balra rendeződjön
        
        for (IconTextPanel panel : panels) {
            add(panel, c); // az összes panelt felfűzöm az ablakra
            panel.setParent(this); // beállítom az ablak referenciáját szülőnek
        }
        IconTextPanel.resizeComponents(this); // átméretezem a komponenseket
        
        setIconTextPanel(0); // az első panel lesz látható csak
        
        c.gridy = 1; // következő sorba mennek a gombok
        JPanel pButtons = new OkCancelPanel(this, btAgain, btSettings, btExit, 5);
        pButtons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5)); // felső margó kivételével mind 5 pixel
        add(pButtons, c);
        
        pack(); // ablak méretének minimalizálása
        setLocationRelativeTo(this); // középre igazítás
        btAgain.requestFocus(); // alapértelmezett opció az Újra gomb
    }
    
    /**
     * A kilépés gombra kattintva ez a metódus fut le.
     * Alapértelmezés szerint a program végetér.
     */
    protected void onExit() {
        System.exit(0);
    }
    
    /**
     * Megadja, hogy a gombok szövegének módosulása után legyen-e ablak újraméretezés.
     * @return true
     */
    @Override
    public boolean needRepack(Rectangle r) {
        return getPreferredSize().width > r.width;
    }
    
    /**
     * Megadja, hogy az átméretezés után a magasság legyen-e újra a régi.
     * @return false
     */
    @Override
    public boolean restoreHeight(Rectangle r) {
        return false;
    }
    
    /**
     * Megadja, hogy az átméretezés után legyen-e ablak újrapozícionálás.
     * @return ha 15 pixel pontossággal a képernyő közepén van az ablak, akkor true, egyébként false
     */
    @Override
    public boolean needReloc(Rectangle r) {
        return OkCancelPanel.isNearCenter(r);
    }
    
    /**
     * Beállítja a látható panelt.
     * @param index a konstruktorban átadott paneleket tartalmazó tömb indexe
     */
    public void setIconTextPanel(int index) {
        for (int i = 0; i < PANELS.length; i++) {
            PANELS[i].setVisible(i == index);
        }
    }
    
    /**
     * Újra próbálkozás szöveg beállítása a gombon.
     */
    public void setTryAgainText(String text) {
        btAgain.setText(text);
    }
    
    /**
     * Kapcsolatbeállítások szöveg beállítása a gombon.
     */
    public void setConnectionSettingsText(String text) {
        btSettings.setText(text);
    }
    
    /**
     * Kilépés szöveg beállítása a gombon.
     */
    public void setExitText(String text) {
        btExit.setText(text);
    }
    
    /**
     * Beállítja az Újra gombot.
     * @param enabled true esetén engedélyezett és fókuszált, false esetén nem engedélyezett
     */
    protected void setAgainButtonEnabled(boolean enabled) {
        btAgain.setEnabled(enabled);
        if (enabled ^ !btAgain.isEnabled()) btAgain.requestFocus(); // fókusz vissza, ha tiltva volt
    }
    
    /**
     * Beállítja az Beállítások gombot.
     * @param enabled true esetén engedélyezett, false esetén nem engedélyezett
     */
    protected void setSettingsButtonEnabled(boolean enabled) {
        btSettings.setEnabled(enabled);
    }
    
    /**
     * Akkor hívódik meg, amikor az Újra gombot kiválasztják.
     */
    protected abstract void onAgain();
    
    /**
     * Akkor hívódik meg, amikor az Beállítások gombot kiválasztják.
     */
    protected abstract void onSettings();
    
}
