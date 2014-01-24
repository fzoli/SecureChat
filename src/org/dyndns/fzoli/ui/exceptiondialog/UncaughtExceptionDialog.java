package org.dyndns.fzoli.ui.exceptiondialog;

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.dyndns.fzoli.ui.LookAndFeelIcon;
import org.dyndns.fzoli.ui.exceptiondialog.event.UncaughtExceptionEvent;
import org.dyndns.fzoli.ui.exceptiondialog.event.UncaughtExceptionListener;
import org.dyndns.fzoli.ui.exceptiondialog.event.UncaughtExceptionSource;

/**
 * Exception megjelenítő dialógusablak.
 * Megjegyzés: Minden osztály, ami ebben a csomagban található egy régebbi projektem eredménye.
 * Amikor ez a kód íródott, még nem írtam megjegyzést a kódjaimhoz, ezért utólag a fontosabb metódusokat kommenteztem,
 * de a nem fontosakat már nem írtam meg, ezért hiányos a kommentezés.
 * @author zoli
 */
public final class UncaughtExceptionDialog extends JDialog {
    
    private JPanel pc = new JPanel(new GridLayout());
    
    private UncaughtExceptionListener uel;
    
    /**
     * A dialógusablakot nem lehet kívülről példányosítani.
     */
    private UncaughtExceptionDialog(Dialog.ModalityType modalityType, String s, UncaughtExceptionParameters params, UncaughtExceptionListener uel) {
        this.uel = uel;
        setResizable(false);
        setTitle(params.getTitle());
        setIconImage(params.getDialogIconImage());
        setModalityType(modalityType);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        final JTextArea ta = new JTextArea(s);
        ta.setOpaque(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        final JScrollPane sp = new JScrollPane(ta);
        ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(sp, gbc);
        final JToggleButton btDetails = new JToggleButton("« " + params.getDetails());
        btDetails.setOpaque(false);
        btDetails.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setDetailsVisible(sp, btDetails.isSelected());
            }
            
        });
        JButton btOk = new JButton(params.getOk());
        btOk.setOpaque(false);
        btOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
            
        });
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 5, 10);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        p.add(btDetails);
        p.add(btOk);
        add(p, gbc);
        final JPanel pi = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        final JLabel li = createMessageIconLabel(params.getMessageIconImage());
        if (li != null) {
            li.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            pi.add(li);
        }
        final JLabel lb = new JLabel(params.getInfo()) {

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(400 - li.getPreferredSize().width, d.height);
            }
            
        };
        lb.setOpaque(false);
        MouseAdapter lbma = params.getMessageListener();
        if (lbma != null) {
            lb.addMouseListener(lbma);
            lb.addMouseMotionListener(lbma);
            lb.addMouseWheelListener(lbma);
        }
        JPanel pci = new JPanel(new GridBagLayout());
        pci.setOpaque(false);
        GridBagConstraints pcc = new GridBagConstraints();
        pcc.anchor = GridBagConstraints.LINE_START;
        pci.add(lb, pcc);
        pi.add(pci);
        setComponent(params.getComponent());
        pcc.gridy = 1;
        pcc.fill = GridBagConstraints.BOTH;
        pcc.insets = new Insets(5, 0, 0, 0);
        pci.add(pc, pcc);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 0, 10);
        add(pi, gbc);
        Dimension d = new Dimension(400, 150);
        sp.setMinimumSize(d);
        sp.setPreferredSize(d);
        pack();
        btOk.setPreferredSize(btDetails.getSize());
        int w = sp.getSize().width;
        p.setPreferredSize(new Dimension(w, p.getSize().height));
        pi.setPreferredSize(new Dimension(w, pi.getSize().height));
        setLocationRelativeTo(this);
        ta.setPreferredSize(ta.getSize());
        ta.setEditable(false);
        setDetailsVisible(sp, false);
        final JPopupMenu pm = new JPopupMenu();
        final JMenuItem mic = new JMenuItem(params.getCopy());
        final JMenuItem misa = new JMenuItem(params.getSelectAll());
        ActionListener al = new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == mic) {
                    ta.copy();
                }
                else if (e.getSource() == misa) {
                    ta.requestFocus();
                    ta.selectAll();
                }
            }
            
        };
        mic.addActionListener(al);
        pm.add(mic);
        misa.addActionListener(al);
        pm.add(misa);
        ta.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                JTextComponent tc = (JTextComponent) e.getSource();
                if (tc.isEnabled()) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                        tc.selectAll();
                    }
                    else if (e.getButton() == MouseEvent.BUTTON3) {
                        mic.setEnabled(tc.getSelectedText() != null);
                        pm.show(tc, e.getX(), e.getY());
                    }
                }
            }
            
        });
        btOk.requestFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (uel != null) uel.exceptionDialogClosed();
    }
    
    private void setComponent(Component c) {
        pc.setVisible(false);
        pc.removeAll();
        if (c != null) {
            pc.add(c);
            pc.setVisible(true);
        }
    }
    
    private void setDetailsVisible(JScrollPane sp, boolean visible) {
        sp.setVisible(visible);
        pack();
    }
    
    private JLabel createMessageIconLabel(Image img) {
        return new JLabel(LookAndFeelIcon.createIcon(this, "OptionPane.errorIcon", img));
    }
    
    /**
     * A virtuális gép kivételkezelő metódusát cseréli le, és a konzol helyett dialógusablakban jelenik meg a kivétel.
     * A dialógus ablak modális, ezért addig nem fut tovább a kód, míg az ablakot be nem zárják.
     * Ha a grafikus felület nem elérhető, akkor az eredeti kivételkezelés marad meg.
     * A dialógus ablak alapértelmezett beállításai kerülnek alkalmazásra.
     */
    public static void applyHandler() {
        applyHandler(null);
    }
    
    /**
     * A virtuális gép kivételkezelő metódusát cseréli le, és a konzol helyett dialógusablakban jelenik meg a kivétel.
     * A dialógus ablak modális, ezért addig nem fut tovább a kód, míg az ablakot be nem zárják.
     * Ha a grafikus felület nem elérhető, akkor az eredeti kivételkezelés marad meg.
     * @param params a dialógusablak megjelenését és működését szabályozó paraméterek
     */
    public static void applyHandler(UncaughtExceptionParameters params) {
        applyHandler(params, null);
    }
    
    /**
     * A virtuális gép kivételkezelő metódusát cseréli le, és a konzol helyett dialógusablakban jelenik meg a kivétel.
     * A dialógus ablak modális, ezért addig nem fut tovább a kód, míg az ablakot be nem zárják.
     * Ha a grafikus felület nem elérhető, akkor az eredeti kivételkezelés marad meg.
     * @param params a dialógusablak megjelenését és működését szabályozó paraméterek
     * @param listener eseménykezelő, ami akkor hívódik meg, ha nem kezelt kivétel keletkezik
     */
    public static void applyHandler(UncaughtExceptionParameters params, UncaughtExceptionListener listener) {
        applyHandler(Dialog.ModalityType.APPLICATION_MODAL, params, listener);
    }
    
    /**
     * A virtuális gép kivételkezelő metódusát cseréli le, és a konzol helyett dialógusablakban jelenik meg a kivétel.
     * Ha a grafikus felület nem elérhető, akkor az eredeti kivételkezelés marad meg.
     * @param modalityType a dialógus ablak modalitását szabályozza
     * @param params a dialógusablak megjelenését és működését szabályozó paraméterek
     * @param listener eseménykezelő, ami akkor hívódik meg, ha nem kezelt kivétel keletkezik
     */
    public static void applyHandler(final Dialog.ModalityType modalityType, final UncaughtExceptionParameters params, final UncaughtExceptionListener listener) {
        if (!GraphicsEnvironment.isHeadless()) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    showException(t, e, modalityType, params, listener);
                }

            });
        }
    }
    
    /**
     * Megjeleníti a kivételmegjelenítő dialógusablakot.
     * Ha a grafikus felület nem érhető el, akkor a konzolra íródik a kivétel.
     * @param t a szál, melyben a kivétel képződött
     * @param e a megjelenítendő kivétel
     * @param modalityType a dialógus ablak modalitását szabályozza
     * @param params a dialógusablak megjelenését és működését szabályozó paraméterek
     * @param uel eseménykezelő, ami akkor hívódik meg, ha nem kezelt kivétel keletkezik
     */
    public static void showException(Thread t, Throwable e, Dialog.ModalityType modalityType, UncaughtExceptionParameters params, UncaughtExceptionListener uel) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                e.printStackTrace(System.err);
            }
            else {
                String s = createDetails(e);
                params = params == null ? new UncaughtExceptionParameters() : params;
                modalityType = modalityType == null ? Dialog.ModalityType.APPLICATION_MODAL : modalityType;
                final UncaughtExceptionDialog d = new UncaughtExceptionDialog(modalityType, s, params, uel);
                UncaughtExceptionSource src = new UncaughtExceptionSource() {

                    @Override
                    public void setComponent(Component c) {
                        d.setComponent(c);
                    }

                    @Override
                    public void addWindowListener(WindowListener listener) {
                        d.addWindowListener(listener);
                    }

                };
                if (uel != null) uel.exceptionDialogAppears(new UncaughtExceptionEvent(src, s, t, e));
                d.setVisible(true);
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    
    private static String createDetails(Throwable e) {
        if (e == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        String s = new String(baos.toByteArray());
        s = s.substring(0, s.length() - 1);
        return s;
    }
    
}