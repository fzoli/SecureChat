package org.dyndns.fzoli.chat.client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import static org.dyndns.fzoli.chat.client.Main.getString;
import org.dyndns.fzoli.ui.RelocalizableWindow;

/**
 * A kapcsolatbeállító ablak súgója.
 * @author zoli
 */
public class ConfigHelpDialog extends JDialog implements RelocalizableWindow {

    /**
     * A szöveg egyszerű HTML kódja.
     */
    private final JLabel taHelp = new JLabel(getLabelText()) {
        
        /**
         * A preferált méret 400 pixel széles és hosszban az eredeti.
         * Így az ablakméret hosszúságban az 1 soros szövegekhez áll be.
         */
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(400, d.height);
        }
        
    };
    
    /**
     * Konstruktor.
     */
    public ConfigHelpDialog(Window owner) {
        super(owner, getString("help")); // szülő és címsor szöveg beállítása
        setDefaultCloseOperation(HIDE_ON_CLOSE); // bezáráskor elrejtődés
        setModalityType(ModalityType.MODELESS); // nem modális dialógus
        
        taHelp.setOpaque(true); // ne legyen átlátszó
        taHelp.setBackground(Color.WHITE); // fehér háttérszín
        taHelp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 5x5-ös margó
        
        JScrollPane sp = new JScrollPane(taHelp); // a szöveg scrollozható ...
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // ... de csak vertikálisan
        sp.setBorder(null); // mindkét border eltüntetése
        sp.setViewportBorder(null);
        add(sp); // scrollpane hozzáadása az ablakhoz
        
        pack(); // minimális méret beállítása és ...
        setMinimumSize(new Dimension(400, 50)); // ... az ablakmagasság csak ennél nagyobb lehet
        setLocationRelativeTo(owner); // szülő ablak szerint középre igazítás
    }
    
    /**
     * Megadja a súgó HTML kódját.
     */
    private String getLabelText() {
        return "<html><span style=\"font-size: 11px\"><p style=\"margin: 5px 0px 5px 0px\"><b>" + getString("path") + "</b></p>" + getString("help_path") + "<p style=\"margin: 5px 0px 5px 0px\"><b>" + getString("certificate") + "</b></p><p style=\"margin: 0px 0px 5px 0px\">" + getString("help_certificate1") + "</p>" + getString("help_certificate2") + ":<br>- " + getString("certifier") + " <i>(" + getString("ca_certificate") + ")</i><br>- " + getString("certificate") + " <i>(" + getString("public_key") + ")</i><br>- " + getString("key") + " <i>(" + getString("private_key") + ")</i><p style=\"margin: 5px 0px 5px 0px\"><b>" + getString("what_is_this") + "</b></p>" + getString("help_what_is_this") + "</span></html>";
    }
    
    /**
     * A felület feliratait újra beállítja.
     * Ha a nyelvet megváltoztatja a felhasználó, ez a metódus hívódik meg.
     */
    @Override
    public void relocalize() {
        setTitle(getString("help"));
        taHelp.setText(getLabelText());
    }
    
}
