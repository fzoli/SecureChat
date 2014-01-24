package org.dyndns.fzoli.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
* Olyan panel, melyen bal oldalt egy ikon található és az mellett egy szöveg.
* Az ikon és szöveg mérete a többi panel ikonjától függ és mindnek ugyan akkora a mérete.
* A legnagyobb ikon- illetve szövegcímke érvényesül a többi panel címkéire.
* @author zoli
*/
public class IconTextPanel extends JPanel {

    /**
     * A paneleket tartalmazó lista.
     */
    private static final List<IconTextPanel> panels = Collections.synchronizedList(new ArrayList<IconTextPanel>());

    /**
     * A panelen megjelenő komponensek.
     */
    private final JLabel lbIcon, lbText;

    /**
     * A szülő komponens.
     */
    private Component parent;
    
    /**
     * Konstruktor.
     * @param owner az a komponens, melyre rákerül a panel
     * @param icon az ikon, ami a panel bal szélén jelenik meg
     * @param text a szöveg, ami az ikon mellé kerül
     */
    public IconTextPanel(Icon icon, String text) {
        super(new GridBagLayout());
        panels.add(this); // a panel nyílvántartásba vétele

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START; // balra szélre horganyozva
        c.insets = new Insets(5, 5, 5, 5); // 5x5 pixeles margóval

        c.weightx = 1; // minimális térkitöltés
        c.fill = GridBagConstraints.NONE; // méretet nem váltóztatva
        lbIcon = new JLabel(icon); // ikon label létrehozása ...
        add(lbIcon, c); // ... és panelhez adás

        c.weightx = Integer.MAX_VALUE; // teljes teret kihasználva ...
        c.fill = GridBagConstraints.HORIZONTAL; // ... horizontálisan
        lbText = new JLabel(text); // üzenet label létrehozása
        lbText.setHorizontalAlignment(SwingConstants.LEFT); // szöveg balra igazítva
        add(lbText, c); // panelhez adás

    }

    /**
     * A szülő komponens beállítása.
     */
    public void setParent(Component parent) {
        this.parent = parent;
    }
    
    /**
     * A panel szövegét állítja be.
     */
    public void setText(String text) {
        lbText.setText(text);
        IconTextPanel.resizeComponents(parent);
    }
    
    /**
     * Átméretezi a panelek címkéit.
     * Megkeresi a legnagyobb méretű címként és alkalmazza a többire a méretét.
     * Csak azokat a paneleket veszi figyelembe, melyek tulajdonosa megegyezik az övével.
     * @param parent csak azok lesznek átmeretezve, melyek szülője megegyezik a paraméterrel
     */
    public static void resizeComponents(Component parent) {
        for (IconTextPanel panel : panels) {
            if (!isParent(parent, panel)) continue;
            panel.lbIcon.setPreferredSize(null);
            panel.lbText.setPreferredSize(null);
        }
        Dimension iconSize = new Dimension(1, 1);
        Dimension panelSize = new Dimension(1, 1);
        for (IconTextPanel panel : panels) {
            if (!isParent(parent, panel)) continue;
            setMaxSize(panel.lbIcon, iconSize);
            setMaxSize(panel.lbText, panelSize);
        }
        for (IconTextPanel panel : panels) {
            if (!isParent(parent, panel)) continue;
            panel.lbIcon.setPreferredSize(iconSize);
            panel.lbText.setPreferredSize(panelSize);
        }
    }

    /**
     * Igazzal tér vissza, ha a panelnek ugyan az az objektum a szülője.
     * @param parent a szülő mellyel összehasonlítja
     * @param panel a vizsgált panel
     */
    private static boolean isParent(Component parent, IconTextPanel panel) {
        return !(parent == null || panel.parent == null || parent != panel.parent);
    }
    
    /**
     * Beállítja a maximum értéket.
     * @param component a komponens, amit vizsgál
     * @param size az aktuális méret, ami megnő, ha a vizsgált komponens mérete nagyobb
     */
    private static void setMaxSize(Component component, Dimension size) {
        Dimension d = component.getPreferredSize();
        if (d.getWidth() > size.getWidth()) size.width = (int) d.getWidth();
        if (d.getHeight() > size.getHeight()) size.height = (int) d.getHeight();
    }

};
