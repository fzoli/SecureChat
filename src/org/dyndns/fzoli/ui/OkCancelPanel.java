package org.dyndns.fzoli.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Súgó, Mégse, Ok gombokat tartalmazó panel.
 * @author zoli
 */
public class OkCancelPanel extends JPanel {

    /**
     * Egy ablak, amin a gombpanel megjelenhet és képes szövegmódosulást kezelni.
     */
    public static interface OkCancelWindow {
        
        /**
         * Megadja, hogy a gombok szövegének módosulása után legyen-e ablak újraméretezés.
         * @param r a szövegmódosulás előtti "bounds"
         */
        public boolean needRepack(Rectangle r);

        /**
         * Megadja, hogy az átméretezés után a magasság legyen-e újra a régi.
         * @param r a szövegmódosulás előtti "bounds"
         */
        public boolean restoreHeight(Rectangle r);
        
        /**
         * Megadja, hogy az átméretezés után legyen-e ablak újrapozícionálás.
         * @param r a szövegmódosulás előtti "bounds"
         */
        public boolean needReloc(Rectangle r);
        
    }
    
    private final Window OWNER;
    
    /**
     * Az egyik gomb a penelen.
     */
    private final JButton BT_OK, BT_CANCEL, BT_HELP;
    
    /**
     * Ha az egyik gomb szövege módosul, meghívja a gombátméretező metódust.
     */
    private final PropertyChangeListener LR = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            resizeButtons(true);
        }
        
    };
    
    /**
     * Láthatatlan gomb a súgó gomb helyére, ha nincs súgó gomb megadva.
     */
    private static final JButton INVISIBLE_BUTTON = new JButton() {

        /**
         * Nem rajzol semmit, tehát nem látható.
         */
        @Override
        protected void paintComponent(Graphics g) {
            ;
        }
        
    };

    /**
     * Konstruktor.
     * @param btOk Oké gomb
     * @param btCancel Mégse gomb
     * @param gap az Oké és Mégse gomb közötti rés
     * @throws NullPointerException ha bármelyik gomb null
     */
    public OkCancelPanel(Window owner, JButton btOk, JButton btCancel, int gap) {
        this(owner, btOk, btCancel, null, gap);
    }
    
    /**
     * Konstruktor.
     * @param btOk Oké gomb
     * @param btCancel Mégse gomb
     * @param btHelp Súgó gomb
     * @param gap az Oké és Mégse gomb közötti rés
     * @throws NullPointerException ha az OK vagy Mégse gomb null
     */
    public OkCancelPanel(Window owner, JButton btOk, JButton btCancel, JButton btHelp, int gap) {
        super(new GridBagLayout());
        OWNER = owner;
        BT_OK = btOk;
        BT_CANCEL = btCancel;
        BT_HELP = btHelp;
        
        GridBagConstraints pc = new GridBagConstraints();
        pc.fill = GridBagConstraints.NONE; // a gombok mérete ne változzon
        pc.gridwidth = 1; // a teljes szélesség kitöltése
        
        pc.insets = new Insets(0, 0, 0, gap); // jobb oldali margó beállítása
        
        pc.weightx = Integer.MAX_VALUE; // a Súgó gomb tölti ki a nagy részt ...
        pc.anchor = GridBagConstraints.LINE_START; // ... és bal szélre kerül
        add(btHelp == null ? INVISIBLE_BUTTON : btHelp, pc);
        
        pc.weightx = 1; // a másik két gomb minimális helyet foglal el ...
        pc.anchor = GridBagConstraints.LINE_END; // ... a jobb szélen
        
        add(btCancel, pc);
        
        pc.insets = new Insets(0, 0, 0, 0); // margó vissza eredeti állapotba (nincs margó)
        add(btOk, pc);
        
        resizeButtons(false);
        btOk.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, LR);
        btCancel.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, LR);
        if (btHelp != null) btHelp.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, LR);
    }
    
    /**
     * Átméretezi a gombokat.
     * Az összes gombnak azonos szélességet állít be.
     */
    private void resizeButtons(boolean setWin) {
        JButton[] buttons = createButtonArray();
        
        final Rectangle r = OWNER == null ? null : OWNER.getBounds(); // az átméretezés előtti ablakméret és pozíció
        
        // az esetleg előzőleg beállított méret törlése
        for (JButton bt : buttons) {
            bt.setPreferredSize(null);
        }
        
        // közönséges maximum kiválasztás
        double size = buttons[0].getPreferredSize().getWidth();
        for (int i = 1; i < buttons.length; i++) {
            double d = buttons[i].getPreferredSize().getWidth();
            if (d > size) size = d;
        }
        
        // kis méretnövelés
        size += 5;
        
        // új méret beállítása a gombokra
        for (JButton bt : buttons) {
            bt.setPreferredSize(new Dimension((int) size, bt.getPreferredSize().height));
        }
        
        OkCancelWindow owner = getOwner();
        if (setWin && owner != null) { // ha van kompatibilis ablak megadva és be kell állítani
            if (owner.needRepack(r)) { // átméretezi, ha kéri azt
                int h = OWNER.getHeight();
                OWNER.pack(); 
                if (owner.restoreHeight(r)) {
                    OWNER.setSize(OWNER.getWidth(), h);
                }
                if (owner.needReloc(r)) {
                    OWNER.setLocationRelativeTo(null); // középre helyezi, ha kéri azt
                }
            }
        }
    }
    
    /**
     * Megadja a birtokló ablakot, ha meg lett adva és támogatja a panelt.
     */
    private OkCancelWindow getOwner() {
        if (OWNER != null && OWNER instanceof OkCancelWindow) return (OkCancelWindow) OWNER;
        return null;
    }
    
    /**
     * Létrehoz egy tömböt a gombokkal, hogy iterálni lehessen őket.
     */
    private JButton[] createButtonArray() {
        final JButton[] buttons = new JButton[BT_HELP == null ? 2 : 3];
        buttons[0] = BT_OK;
        buttons[1] = BT_CANCEL;
        if (BT_HELP != null) buttons[2] = BT_HELP;
        return buttons;
    }
    
    /**
     * Megadja, hogy a képernyő közepén van-e a téglalap.
     * @param r a téglalap
     * @return ha 15 pixel pontossággal a képernyő közepén van a téglalap, akkor true, egyébként false
     */
    public static boolean isNearCenter(Rectangle r) {
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        int x = p.x - r.width / 2;
        int y = p.y - r.height / 2;
        boolean bX = Math.abs(x - r.x) <= 15;
        boolean bY = Math.abs(y - r.y) <= 15;
        return bX && bY;
    }
    
}
