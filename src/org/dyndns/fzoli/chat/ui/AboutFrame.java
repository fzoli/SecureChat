package org.dyndns.fzoli.chat.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import static javax.swing.UIManager.getString;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.ui.RelocalizableWindow;

/**
 * Az alkalmazás névjegye.
 * @author zoli
 */
public class AboutFrame extends JFrame implements RelocalizableWindow {

    /**
     * A programcsomag neve.
     */
    private static final String APP_NAME = "Secure Chat";
    
    /**
     * Az aktuális verziószám.
     */
    private static final String VERSION = "0.3.0.13";
    
    /**
     * A program logóját, nevét és verzióját megjelenítő panel.
     */
    private class LogoPanel extends JPanel {

        /**
         * A panel eredeti színe vonal rajzolásához.
         */
        private final Color LINE_COLOR;
        
        /**
         * Konstruktor.
         * @param icon a program logója
         */
        public LogoPanel(BufferedImage icon) {
            LINE_COLOR = getBackground();
            setBackground(Color.WHITE);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            
            JLabel lbApp = new JLabel(APP_NAME);
            lbApp.setFont(new Font(getFont().getName(), Font.PLAIN, 26));
            c.insets = new Insets(5, 5, 0, 0);
            add(lbApp, c);
            
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(0, 5, 0, 0);
            JLabel lbVer = new JLabel(VERSION);
            lbVer.setVerticalAlignment(SwingConstants.TOP);
            add(lbVer, c);
            
            c.gridx = 1;
            c.gridy = 0;
            c.gridheight = 2;
            c.insets = new Insets(5, 5, 5, 5);
            add(new JLabel(new ImageIcon(R.resize(icon, 64))), c);
        }

        /**
         * A komponens kirajzolása után egy
         * elválasztó-vonalat is rajzol a panel aljára.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(LINE_COLOR.brighter());
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g.setColor(LINE_COLOR.darker());
            g.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
        }
        
    }
    
    /**
     * A szerző adatait megjelenítő panel.
     */
    private class TextPanel extends JPanel {

        /**
         * A szerző adatait tartalmazó label.
         */
        private JLabel lb = new JLabel(getHtmlText());
        
        /**
         * Konstruktor.
         */
        public TextPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(5, 5, 5, 5);
            c.anchor = GridBagConstraints.LINE_START;
            add(lb, c);
        }
        
        /**
         * Megadja a lokalizált HTML-szöveget.
         */
        private String getHtmlText() {
            return "<html>" + getString(UIUtil.KEY_AUTHOR) + ": Farkas Zolt&aacute;n<br>E-mail: f.zoli@mailbox.hu</html>";
        }

        /**
         * Az aktuális nyelv alapján frissíti a szerző adatait.
         */
        public void relocalize() {
            lb.setText(getHtmlText());
        }
        
    }
    
    /**
     * OK gomb, ami megszünteti a névjegyet amikor kattintanak rajta.
     */
    private class OkButton extends JButton implements ActionListener {

        /**
         * A névjegy referenciája a megszüntetéshez.
         */
        private final AboutFrame FRAME;
        
        /**
         * Konstruktor.
         * OK szöveg és eseménykezelő inicializálás.
         */
        public OkButton(AboutFrame frame) {
            super("OK");
            FRAME = frame;
            addActionListener(this);
        }
        
        /**
         * A minimális méretnél 10 pixellel szélesebb méretet használ.
         */
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 10, d.height);
        }

        /**
         * A gombra kattintáskor lefutó metódus.
         * Meghívja a {@link JFrame#dispose()} metódust.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            FRAME.dispose();
        }
        
    }
    
    /**
     * A szöveget tartalmazó panel referenciája az újralokalizáláshoz.
     */
    private final TextPanel PANEL = new TextPanel();
    
    /**
     * Konstruktor.
     * @param icon az ablak ikonja és a felület logója
     */
    public AboutFrame(BufferedImage icon) throws HeadlessException {
        super(getString(UIUtil.KEY_ABOUT));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(icon);
        setLayout(new GridBagLayout());
        setResizable(false);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        add(new LogoPanel(icon), c);
        c.gridy = 1;
        c.gridwidth = 1;
        add(PANEL, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 15, 0, 5);
        c.anchor = GridBagConstraints.LINE_END;
        add(new OkButton(this), c);
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * A szerző adatait frissíti az új nyelv alapján.
     */
    @Override
    public void relocalize() {
        PANEL.relocalize();
        setTitle(getString(UIUtil.KEY_AUTHOR));
        if (getPreferredSize().width > getWidth() || getPreferredSize().height > getHeight()) pack();
    }
    
    /**
     * Teszt.
     */
    public static void main(String[] args) {
        UIUtil.setSystemLookAndFeel();
        UIUtil.createResource("org.dyndns.fzoli.chat.l10n.server", Locale.getDefault());
        AboutFrame fr = new AboutFrame(R.getServerImage());
        fr.setDefaultCloseOperation(EXIT_ON_CLOSE);
        fr.setVisible(true);
    }
    
}
