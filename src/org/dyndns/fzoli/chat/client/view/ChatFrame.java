package org.dyndns.fzoli.chat.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.metal.MetalToolTipUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.dyndns.fzoli.chat.client.ClientSideGroupChatData;
import org.dyndns.fzoli.chat.client.Main;
import org.dyndns.fzoli.chat.model.UserData;
import org.dyndns.fzoli.ui.FixedStyledEditorKit;
import org.dyndns.fzoli.ui.ScrollingDocumentListener;
import org.dyndns.fzoli.ui.UIUtil;
import sun.swing.SwingUtilities2;
import static org.dyndns.fzoli.chat.client.Main.getString;
import org.dyndns.fzoli.chat.model.ChatMessage;
import org.dyndns.fzoli.chat.resource.R;
import org.dyndns.fzoli.ui.RelocalizableWindow;

/**
 * Chatablak.
 * @author zoli
 */
public class ChatFrame extends JFrame implements RelocalizableWindow {
    
    /**
     * A felhasználókat megjelenítő listának a modelje.
     */
    private static class UserListModel extends DefaultListModel<UserData> {

        /**
         * Megadja, hogy a user benne van-e a listában.
         * A keresést usernév alapján teszi meg.
         */
        @Override
        public boolean contains(Object elem) {
            return findUserData(elem) != null;
        }

        @Override
        public boolean removeElement(Object obj) {
            return super.removeElement(findUserData(obj));
        }
        
        public UserData findUserData(Object elem) {
            if (elem instanceof UserData) {
                UserData ud = (UserData) elem;
                Enumeration<UserData> e = elements();
                while (e.hasMoreElements()) {
                    UserData d = e.nextElement();
                    if (UserData.equals(d, ud)) return d;
                }
            }
            if (elem instanceof String) {
                String un = (String) elem;
                Enumeration<UserData> e = elements();
                while (e.hasMoreElements()) {
                    UserData d = e.nextElement();
                    if (UserData.equals(d, un)) return d;
                }
            }
            return null;
        }
        
    }
    
    /**
     * Az elválasztóvonalak szélessége.
     */
    private static final int DIVIDER_SIZE = 5, MARGIN = 2, TTM = 2;
    
    /**
     * A szövegeket megjelenítő panelek háttérszíne.
     */
    private static final Color COLOR_BG = Color.WHITE;
    
    /**
     * A "Jelenlévők" szöveget tartalmazó címke.
     */
    private final JLabel LB_ATTENDEES = new JLabel(getString("attendees"));
    
    /**
     * Két felhasználót hasonlít össze a megjelenő nevük alapján.
     */
    private static final Comparator<UserData> CMP_CNTRLS = new Comparator<UserData>() {

        @Override
        public int compare(UserData o1, UserData o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getFullName(), o2.getFullName());
        }
        
    };
    
    /**
     * A felhasználók listája.
     */
    private final JList<UserData> LIST_USERS = new JList<UserData>(new UserListModel()) {
        {
            setBackground(COLOR_BG);
            setDragEnabled(true);
            setCellRenderer(new DefaultListCellRenderer() {

                /**
                 * A lista elemei látszólag nem választhatóak ki és nem soha nincs rajtuk fókusz.
                 * A saját név dőlt betűvel jelenik meg, ha JLabel alapú a lista felsorolása.
                 */
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, false, false);
                    try {
                        JLabel lb = (JLabel) c;
                        UserData ud = (UserData) value;
                        lb.setText(createUserText(ud));
                        if (isSenderName(ud.getUserName())) lb.setFont(new Font(lb.getFont().getName(), Font.ITALIC, lb.getFont().getSize()));
                        return lb;
                    }
                    catch (Exception ex) {
                        return c;
                    }
                }
                
            });
        }

        @Override
        public void paint(Graphics g) {
            if (!freezeUsr) super.paint(g);
        }
        
        /**
         * Legyárt egy komponenst, ami a megadott sorban lévő komponenssel egyezik méretileg.
         * @param row a sorindex
         */
        private Component createComponent(int row) {
            return getCellRenderer().getListCellRendererComponent(this, getModel().getElementAt(row), row, false, false);
        }
        
        /**
         * Megadja, hogy az adott sorban lévő elemnek szüksége van-e ToolTip szövegre.
         * @param row a sorindex
         * @return true ha nem fér ki és három pontra végződik, egyébként false
         */
        private boolean needToolTip(int row) {
            Component c = createComponent(row);
            return c.getPreferredSize().width > getSize().width;
        }
        
        /**
         * A felhasználó teljes nevét és online kapcsolatainak számát tartalmazó szöveg előállítása.
         */
        private String createUserText(UserData ud) {
            return ud.getFullName() + (ud.getClientCount() == null || ud.getClientCount() < 2 ? "" : " [" + ud.getClientCount() + "]");
        }
        
        /**
         * Azok a nevek, melyek nem férnek ki, három pontra végződnek és az egeret rajtuk tartva a teljes szövegük jelenik meg előttük ToolTip-ben.
         * Ez a metódus adja meg a ToolTip szövegét.
         * @see #getToolTipLocation(java.awt.event.MouseEvent)
         */
        @Override
        public String getToolTipText(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if (!needToolTip(row)) return null;
            UserData o = (UserData) getModel().getElementAt(row);
            return o == null ? null : createUserText(o);
        }
        
        /**
         * Azok a nevek, melyek nem férnek ki, három pontra végződnek és az egeret rajtuk tartva a teljes szövegük jelenik meg előttük ToolTip-ben.
         * Ez a metódus adja meg a ToolTip helyét.
         * @see #getToolTipText(java.awt.event.MouseEvent)
         */
        @Override
        public Point getToolTipLocation(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if (!needToolTip(row)) return null;
            Rectangle r = getCellBounds(row, row);
            return new Point(r.x - TTM, r.y - TTM);
        }
        
        /**
         * Azok a nevek, melyek nem férnek ki, három pontra végződnek és az egeret rajtuk tartva a teljes szövegük jelenik meg előttük ToolTip-ben.
         * Ahhoz, hogy úgy tűnjön, mint ha a felirat kiérne a panelből, ugyan olyan betűtípussal, háttérszínnel és betűszínnel kell kirajzolódnia
         * a ToolTip-nek, mint a JLabel-nek. Ez a metódus létrehoz egy olyan ToolTip objektumot, ami pontosan így néz ki.
         * A ToolTip szövegének margója a {@link #TTM} alapján állítódik be, amit a pozíció generálásakor is figyelembe kell venni:
         * @see #getToolTipLocation(java.awt.event.MouseEvent)
         */
        @Override
        public JToolTip createToolTip() {
            return new JToolTip() {
                {
                    setComponent(LIST_USERS);
                    setUI(new MetalToolTipUI() {

                        {
                            setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                            setBackground(COLOR_BG);
                        }

                        private Font findFontAndSetColors(String text) {
                            int index = 0;
                            DefaultListModel<UserData> model = (DefaultListModel) getModel();
                            Enumeration<UserData> e = model.elements();
                            while (e.hasMoreElements()) {
                                UserData cs = e.nextElement();
                                if (createUserText(cs).equals(text)) {
                                    Component cmp = createComponent(index);
                                    if (cmp.isOpaque()) setBackground(cmp.getBackground());
                                    foreground = cmp.getForeground();
                                    return cmp.getFont();
                                }
                                index++;
                            }
                            return null;
                        }

                        @Override
                        public void paint(Graphics g, JComponent c) {
                            String text = ((JToolTip) c).getTipText();
                            Insets insets = getInsets();
                            Font font = findFontAndSetColors(text);
                            if (font == null) font = getFont();
                            g.setFont(font);
                            g.setColor(getForeground());
                            int x = insets.left;
                            int y = insets.top + SwingUtilities2.getFontMetrics(c, g, font).getAscent();
                            try {
                                // TODO: kicserélni megbízható osztályra
                                // a JLabel kódja alapján rajzolja ki a tool tip feliratát
                                SwingUtilities2.drawString(getComponent(), g, text, x, y);
                            }
                            catch (Throwable t) {
                                // ha nem sikerült kirajzolni (pl. a SwingUtilities2 osztályt eltávolították)
                                // menti a menthetőt és "rondán" kirajzolja
                                g.drawString(text, x, y);
                            }
                        }

                    });
                }

                private Color foreground;

                @Override
                public Color getForeground() {
                    return foreground == null ? UIManager.getDefaults().getColor("Label.foreground") : foreground;
                }

                @Override
                public Font getFont() {
                    return UIManager.getDefaults().getFont("Label.font");
                }

                @Override
                public Insets getInsets() {
                    Insets ins = ((JLabel)createComponent(0)).getInsets();
                    return new Insets(ins.top + TTM, ins.left + TTM, ins.bottom + TTM, ins.right + TTM);
                }
                
            };
        }
        
    };
    
    /**
     * Az üzenetkijelző és üzenetküldő panel.
     * Az összes szöveget megjelenítő panel háttérszíne azonos.
     * Ehhez egy kis trükkre volt szükség, hogy minden oprendszeren működjön.
     * Windows LAF esetén valamiért a JLabel felett ott maradt egy csík, és a panel
     * háttérszín beállítása se oldotta meg a gondot, ezért a ScrollPane-nek állítottam
     * be a hátteret, de a ScrollBarra is hatott, ezért annak megadtam a ScrollPane hátterét.
     * Így sikeresen eltünt a zavaró csík.
     */
    private final JPanel PANEL_USERS = new JPanel() {
        {
            setBackground(COLOR_BG);
            setLayout(new GridLayout());
            setBorder(null);
            
            JPanel panel = new JPanel(new BorderLayout()) {

                @Override
                public Dimension getPreferredSize() {
                    // a kívánt szélesség 0, hogy a scroll pane teljesen összehúzza, ha azt kérik
                    return new Dimension(0, super.getPreferredSize().height);
                }
                
            };
            panel.setBackground(getBackground());
            
            JScrollPane pane = new JScrollPane(panel);
            pane.getVerticalScrollBar().setOpaque(true);
            Color spbg = (Color) UIManager.getDefaults().get("ScrollPane.background");
            if (spbg != null) pane.getVerticalScrollBar().setBackground(new Color(spbg.getRGB()));
            pane.setBorder(null);
            pane.setOpaque(false);
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            pane.setViewportBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createLineBorder(getBackground(), 4)));
            add(pane);
            
            LB_ATTENDEES.setFont(new Font(LB_ATTENDEES.getFont().getFontName(), Font.BOLD, LB_ATTENDEES.getFont().getSize()));
            LB_ATTENDEES.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            
            panel.add(LB_ATTENDEES, BorderLayout.NORTH);
            panel.add(LIST_USERS, BorderLayout.CENTER);
            
            setPreferredSize(new Dimension(150 - DIVIDER_SIZE - 2 * MARGIN, 200 - 2 * MARGIN));
        }
    };
    
    /**
     * Az üzeneteket megjelenítő komponens.
     */
    private JTextPane tpMessages;
    
    /**
     * Formázott dokumentum az üzenetek megjelenítéséhez.
     */
    private StyledDocument doc;
    
    /**
     * A formázott dokumentum stílusainak kulcsai.
     */
    private static final String KEY_DATE = "date",
                                KEY_NAME = "name",
                                KEY_MYNAME = "myname",
                                KEY_SYSNAME = "sysname",
                                KEY_REGULAR = "regualar",
                                KEY_URL = "url";
    
    /**
     * Dátumformázó a chatüzenetek elküldésének idejének kijelzésére.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    private JScrollPane paneSender;
    
    private Integer msgKey = null;
    
    /**
     * Az üzenetkijelző és üzenetküldő panel.
     */
    private final JPanel PANEL_MESSAGES = new JPanel() {
        {
            setBackground(COLOR_BG);
            setLayout(new BorderLayout());
            
            tpMessages = new JTextPane() {

                @Override
                public void paint(Graphics g) {
                    if (!freezeMsg) super.paint(g);
                }
                
            };
            
            tpMessages.setBackground(getBackground());
            tpMessages.setFocusable(false);
            tpMessages.setEditable(false);
            tpMessages.setEditorKit(new FixedStyledEditorKit());
            
            tpMessages.addMouseListener(new MouseAdapter() {

                private String getUrl(MouseEvent e) {
                    int i = tpMessages.viewToModel(e.getPoint()); // a kattintást szöveg indexre konvertálja
                    String fullText = tpMessages.getText();
                    Matcher matcher = URL_PATTERN.matcher(fullText);
                    String selectedUrl = null;
                    while (matcher.find()) {
                        if (matcher.start() <= i && matcher.end() >= i) {
                            selectedUrl = fullText.substring(matcher.start(), matcher.end());
                            break;
                        }
                    }
                    return selectedUrl;
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    String url = getUrl(e);
                    if (url != null) {
                        openWebpage(url);
                    }
                }
                
//                    String line = "";
//                    int begin = 0, end = 0;
//                    while (true) { // megkeresi azt a sort, amire kattintottak
//                        if (end > 0) begin = tpMessages.getText().indexOf('\n', begin + 1);
//                        if (begin == -1) break;
//                        end = Math.min(tpMessages.getText().length() - 1, tpMessages.getText().indexOf('\n', begin + 1));
//                        if (end == -1) end = tpMessages.getText().length();
//                        if (begin <= i && end >= i) try {
//                            line = doc.getText(begin, end - begin).trim(); // találat
//                            break;
//                        }
//                        catch (Exception ex) {
//                            ;
//                        }
//                    }
                
            });
            
            doc = tpMessages.getStyledDocument();
            Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            Style regular = doc.addStyle(KEY_REGULAR, def);
            
            Style date = doc.addStyle(KEY_DATE, regular);
            StyleConstants.setForeground(date, Color.GRAY);
            
            Style url = doc.addStyle(KEY_URL, regular);
            StyleConstants.setForeground(url, Color.BLUE);
            StyleConstants.setUnderline(url, true);
            
            Style name = doc.addStyle(KEY_NAME, regular);
            StyleConstants.setBold(name, true);
            StyleConstants.setForeground(name, new Color(0, 128, 255));
            
            Style myname = doc.addStyle(KEY_MYNAME, name);
            StyleConstants.setForeground(myname, new Color(0, 100, 205));
            
            Style sysname = doc.addStyle(KEY_SYSNAME, name);
            StyleConstants.setForeground(sysname, Color.BLACK);
            
            final JTextArea tpSender = new JTextArea();
            tpSender.addKeyListener(new KeyAdapter() {

                private Integer prevKey;
                
                @Override
                public void keyPressed(KeyEvent e) {
                    if (isRewriteAction(e, true)) {
                        List<ChatMessage> l = Main.DATA.getMessages();
                        for (int i = l.size() - 1; i >= 0; i--) {
                            ChatMessage msg = l.get(i);
                            if (msg.getID() != null && senderName != null && senderName.equals(msg.getSender())) {
                                loadBack(msg);
                                if (prevKey == null || prevKey > msg.getID()) break;
                            }
                        }
                        prevKey = msgKey;
                    }
                    else if (isRewriteAction(e, false) && msgKey != null) {
                        List<ChatMessage> l = Main.DATA.getMessages();
                        for (int i = 0; i < l.size(); i++) {
                            ChatMessage msg = l.get(i);
                            if (msg.getID() == null || msg.getID() <= msgKey) continue;
                            if (senderName != null && senderName.equals(msg.getSender())) {
                                loadBack(msg);
                                if (prevKey == null || prevKey < msg.getID()) break;
                            }
                        }
                        prevKey = msgKey;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (!isRewriteAction(e, true) && !isRewriteAction(e, false)) {
                        if (tpSender.getText().isEmpty()) {
                            cancelRewrite();
                        }
                    }
                }
                
                private boolean isRewriteAction(KeyEvent e, boolean reverse) {
                    return e.isControlDown() && e.getKeyCode() == (reverse ? KeyEvent.VK_UP : KeyEvent.VK_DOWN);
                }
                
                private void loadBack(ChatMessage msg) {
                    msgKey = msg.getID();
                    tpSender.setText(msg.getMessage());
                    paneSender.setBackground(Color.GREEN);
                }
                
                private void cancelRewrite() {
                    msgKey = prevKey = null;
                    paneSender.setBackground(COLOR_BG);
                }
                
            });
            tpSender.setBackground(getBackground());
            tpSender.setLineWrap(true);
            tpSender.setDocument(new PlainDocument() {

                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    if(str == null || tpSender.getText().length() >= 1000) throw new BadLocationException(str, offs);
                    super.insertString(offs, str, a);
                }
                
            });
            tpSender.setFont(UIManager.getDefaults().getFont("Label.font"));
            tpSender.setBorder(BorderFactory.createLineBorder(getBackground(), 5));
            
            Dimension minSize = new Dimension(200, 32);
            
            final JScrollPane paneMessages = new JScrollPane(tpMessages) {

                @Override
                public void paint(Graphics g) {
                    if (!freezeMsg) super.paint(g);
                }
                
            };
            
            paneMessages.setBorder(null);
            paneMessages.setViewportBorder(BorderFactory.createEtchedBorder());
            paneMessages.setMinimumSize(minSize);
            paneMessages.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            
            ScrollingDocumentListener.apply(tpMessages, paneMessages);
            
            paneSender = new JScrollPane(tpSender);
            paneSender.setBorder(null);
            paneSender.setViewportBorder(BorderFactory.createEtchedBorder());
            paneSender.setMinimumSize(minSize);
            paneSender.setPreferredSize(new Dimension(490, 50));
            paneSender.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            
            final String SUBMIT = "text-submit";
            InputMap input = tpSender.getInputMap();
            KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
            KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
            input.put(shiftEnter, input.get(enter));
            input.put(enter, SUBMIT);
            ActionMap actions = tpSender.getActionMap();
            actions.put(SUBMIT, new AbstractAction() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    ScrollingDocumentListener.scrollToBottom(tpMessages); // scrollozás az üzenetek végére
                    if (!tpSender.getText().trim().isEmpty()) {
                        ClientSideGroupChatData.sendMessage(new ChatMessage(tpSender.getText(), msgKey)); // üzenet elküldése
                        tpSender.setText(""); // üzenetkülső panel kiürítése
                    }
                }
                
            });
            
            add(createSplitPane(JSplitPane.VERTICAL_SPLIT, paneMessages, paneSender));
            
            setPreferredSize(new Dimension(490, 200 - 2 * MARGIN));
        }
    };
    
    private static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            }
            catch (Exception e) {
                ;
            }
        }
    }

    private static void openWebpage(String url) {
        try {
            openWebpage(new URL(url).toURI());
        }
        catch (Exception e) {
            ;
        }
    }
    
    /**
     * Az utolsó üzenetküldő neve.
     */
    private String lastSender;
    
    /**
     * A saját felhasználónév.
     */
    private static String senderName;
    
    /**
     * A használt rendszerüzenetek típusai és szövegük.
     */
    private final Map<Integer, String> sysMessages = Collections.synchronizedMap(new HashMap<Integer, String>());
    
    /**
     * A rendszerüzenetek dátumait tartalmazó lista.
     */
    private final List<Date> sysDates = Collections.synchronizedList(new ArrayList<Date>());
    
    /**
     * Rendszerüzenet típus.
     */
    private static final int SYS_CONNECT = -1, SYS_DISCONNECT = -2;
    
    /**
     * Konstruktor.
     */
    public ChatFrame() {
        super(getString("app_name"));
        setIconImage(R.getClientImage());
        setMinimumSize(new Dimension(420, 125));
        
        JSplitPane pane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, PANEL_MESSAGES, PANEL_USERS);
        pane.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        add(pane);
        
        pack();
    }
    
    /**
     * Beállítja a csetablak címsorának a szövegét.
     * A "Cset" felirat után kötőjellel elválasztva az aktuális jármű neve kerül beállításra.
     * Ha nincs kiválasztva jármű, csak a "Cset" felirat kerül beállításra.
     */
    public void setTitle() {
        setTitle(getString("app_name"));
    }
    
    /**
     * Kicseréli a chatablak szövegeinek egy részét.
     * @param from a szöveg, amit cserélni kell
     * @param to a szöveg, amire cserélni kell
     * @param start a csere kezdőpozíciója
     * @param sys true esetén csak rendszerüzenetet cserél
     */
    private void replace(String from, String to, int start, boolean sys) {
//        Enumeration<UserData> e = ((UserListModel) LIST_USERS.getModel()).elements();
//        while (e.hasMoreElements()) {
//            String fullName = e.nextElement().getFullName();
//            if (fullName.equals(from)) return; // felhasználónév nem írható át soha! (még akkor sem, ha az az üzenet)
//        }
        synchronized (DOC_LOCK) {
            try {
                String s = doc.getText(0, doc.getLength()); // az üzenetek teljes szövege
                int i = s.indexOf(from, start); // az első előfordulás a cserélendő szöveghez
                if (i != -1) { // ha van előfordulás
                    if (sys) { // ha rendszerüzenetet kell csak cserélni
                        int lineStart = 0; // első sor esetén 0 a sor eleje
                        for (int j = i; j > 0; j--) { // megkeresi a sor elejét
                            if (s.charAt(j) == '\n') {
                                lineStart = j + 1; // a sor eleje megtalálva
                                break;
                            }
                        }
                        int starIndex = lineStart + 11; // a csillag karakter pozíciója
                        boolean isSys; // megadja, hogy rendszerüzenet-e
                        try {
                            isSys = s.charAt(starIndex) == '*'; // elsőként meg kell nézni, csillag karakter van-e a megfelelő helyen
                            if (isSys) { // ha csillag karakter van
                                String chk = s.substring(lineStart, starIndex); // a csillag karakter előtti dátum részletnek ...
                                boolean date = false;
                                for (Date d : sysDates) { // ... illeszkednie kell a rendszerüzenetek dátumait tartalmazó listához
                                    if (chk.contains(DATE_FORMAT.format(d))) {
                                        date = true;
                                        break;
                                    }
                                }
                                isSys &= date; // most már lehet tudni, hogy rendszerüzenet-e az adott üzenet
                            }
                        }
                        catch (Exception ex) {
                            isSys = false; // hiba esetén biztos, hogy nem rendszerüzenet
                        }
                        if (!isSys) { // ha a találat nem rendszerüzenet ...
                            int lineEnd = s.indexOf("\n", lineStart);
                            if (lineEnd != -1) replace(from, to, lineEnd, sys); // ugrás a következő sorra, ha van és rekurzív hívás
                            return; // a rekurzív hívás után visszatér a kód, ezért itt ki kell lépni
                        }
                    }
                    // a megtalált sor törölhető, ezért törlés, majd új szöveg beillesztése
                    doc.remove(i, from.length());
                    doc.insertString(i, to, doc.getStyle(KEY_REGULAR));
                    // rekurzív újrahívás, hátha van még cserélni való szöveg
                    replace(from, to, ++i, sys);
                }
            }
            catch (Exception ex) {
                ;
            }
        }
    }
    
    /**
     * A felület feliratait újra beállítja.
     * Ha a nyelvet megváltoztatja a felhasználó, ez a metódus hívódik meg.
     */
    @Override
    public void relocalize() {
        setTitle();
        LB_ATTENDEES.setText(getString("attendees"));
        Iterator<Entry<Integer, String>> it = sysMessages.entrySet().iterator();
        Map<Integer, String> newValues = new HashMap<Integer, String>();
        while (it.hasNext()) { // a használt rendszerüzenetek lecserélése az új nyelv alapján
            Entry<Integer, String> e = it.next();
            String newValue = getSysText(e.getKey(), "");
            replace(e.getValue(), newValue, 0, true);
            newValues.put(e.getKey(), newValue);
        }
        sysMessages.clear(); // az új értékek nyílvántartásba vétele
        sysMessages.putAll(newValues);
    }
    
    /**
     * Az összes chatüzenetet eltávolítja a felületről.
     */
    public void removeChatMessages() {
        synchronized (DOC_LOCK) {
            try {
                lastSender = null;
                sysMessages.clear();
                SYS_MESSAGES.clear();
                doc.remove(0, doc.getLength());
            }
            catch (BadLocationException ex) {
                ;
            }
        }
    }
    
    /**
     * Az összes felhasználó eltávolítása a felületről.
     */
    public void removeUsers() {
        ((DefaultListModel) LIST_USERS.getModel()).clear();
    }
    
    /**
     * A jelenlévők listájához ad hozzá vagy abból vesz el.
     * Ugyan azt a nevet csak egyszer adja hozzá a listához.
     * A hozzáadáskor ABC sorrendbe rendeződik a lista.
     * @param ud a beállítandó user
     * @param visible true esetén hozzáadás, egyébként eltávolítás
     */
    public void setUserVisible(UserData ud, boolean visible, boolean notify) {
        freezeUsr = true;
        DefaultListModel<UserData> model = (DefaultListModel) LIST_USERS.getModel();
        if ((visible && model.contains(ud)) || (!visible && model.contains(ud))) {
            model.removeElement(ud);
        }
        if (visible) {
            List<UserData> l = new ArrayList<UserData>();
            Enumeration<UserData> e = model.elements();
            while (e.hasMoreElements()) {
               l.add(e.nextElement());
            }
            l.add(ud);
            Collections.sort(l, CMP_CNTRLS);
            model.clear();
            for (UserData s : l) {
                model.addElement(s);
            }
        }
        freezeUsr = false;
        LIST_USERS.invalidate();
        LIST_USERS.repaint();
        if (notify) {
            showSysMessage(visible ? ud.getSignInDate() : ud.getSignOutDate(), ud.getFullName(), visible ? SYS_CONNECT : SYS_DISCONNECT);
        }
    }

    /**
     * Megadja, hogy a megadott felhasználónév megegyezik-e a saját felhasználónévvel.
     */
    private static boolean isSenderName(String senderName) {
        if (senderName == null || ChatFrame.senderName == null) return false;
        return ChatFrame.senderName.equals(senderName);
    }
    
    /**
     * Saját felhasználónév beállítása.
     */
    public static void setSenderName(String senderName) {
        ChatFrame.senderName = senderName;
    }
    
    private final List<ChatMessage> SYS_MESSAGES = new ArrayList<ChatMessage>();
    
    /**
     * Megjeleníti a kért rendszerüzenetet.
     * @param d az esemény ideje
     * @param name a módosult felhasználó neve
     * @param type az esemény típusa
     */
    private void showSysMessage(Date d, String name, int type) {
        String s = getSysText(type, null);
        if (s != null) {
            SYS_MESSAGES.add(new ChatMessage(type, d, name, name, null));
            addMessage(d, name, s, true);
            sysMessages.put(type, s);
            sysDates.add(d);
        }
    }
    
    /**
     * Az aktuális nyelv alapján adja meg a kért rendszerüzenet szövegét.
     * @param type a rendszerüzenet típusa
     * @param def ha nincs ilyen típus, ezzel tér vissza
     */
    private String getSysText(int type, String def) {
        String s;
        switch (type) {
            case SYS_CONNECT:
                s = getString("sys_connect");
                break;
            case SYS_DISCONNECT:
                s = getString("sys_disconnect");
                break;
            default:
                s = def;
        }
        return s;
    }
    
    /**
     * Chatüzenetet jelenít meg és a scrollt beállítja.
     * @param date az üzenet elküldésének ideje
     * @param name az üzenet feladója
     * @param message az üzenet tartalma
     */
    public void addMessage(Date date, String name, String message) {
        addMessage(date, name, message, false);
    }
    
    public void addChatMessage(ChatMessage msg) {
        if (msg != null) {
//            UserListModel model = (UserListModel) LIST_USERS.getModel();
//            UserData ud = model.findUserData(msg.getSender());
//            String sender = ud == null ? msg.getSender() : ud.getFullName();
            String sender = msg.getFullName();
            addMessage(msg.getDate(), sender, msg.getMessage());
        }
    }
    
    /**
     * Segédobjektum szálkezeléshez.
     * Annak elkerülése érdekében, hogy egyszerre több szálban is módosulhasson
     * a chatüzenetet megjelenítő panel, synchronized blokkba vannak téve a módosításokat
     * végző metódusok. Az objektumot a synchronized blokk használja.
     */
    private final Object DOC_LOCK = new Object();
    
    private final Pattern URL_PATTERN = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    
    /**
     * Chatüzenetet illetve rendszerüzenetet jelenít meg és a scrollt beállítja.
     * @param date az üzenet elküldésének ideje
     * @param name az üzenet feladója
     * @param message az üzenet tartalma
     * @param sysmsg rendszerüzenet-e az üzenet
     */
    private void addMessage(Date date, String name, String message, boolean sysmsg) {
        synchronized (DOC_LOCK) {
            try {
                if (date == null || name == null || message == null) return;
                boolean me = message.indexOf("/me ") == 0;
                if (me) message = message.substring(4);
                me |= sysmsg;
                boolean startNewline = message.indexOf("\n") == 0; // ha új sorral kezdődik az üzenet, egy újsor jel bent marad
                message = message.trim();
                
                Matcher matcher = URL_PATTERN.matcher(message);
                Map<String, String> messages = new LinkedHashMap<String, String>();
                int end = -1;
                while (matcher.find()) {
                    int start = matcher.start();
                    String bs = message.substring(Math.max(0, end), start);
                    if (!bs.isEmpty()) messages.put(bs, KEY_REGULAR);
                    end = matcher.end();
                    messages.put(message.substring(start, end), KEY_URL);
                }
                if (messages.isEmpty()) {
                    messages.put(message, KEY_REGULAR);
                }
                else if (end < message.length()) {
                    messages.put(message.substring(end), KEY_REGULAR);
                }
                
                Iterator<Entry<String, String>> it = messages.entrySet().iterator();
                Entry<String, String> first = it.next();
                doc.insertString(doc.getLength(), (doc.getLength() > 0 ? "\n" : "") + '[' + DATE_FORMAT.format(date) + "] ", doc.getStyle(KEY_DATE));
                doc.insertString(doc.getLength(), (me ? ("* " + name) : (name.equals(lastSender) ? "..." : (name + ':'))) + ' ', doc.getStyle(sysmsg ? KEY_SYSNAME : isSenderName(name) ? KEY_MYNAME : KEY_NAME));
                doc.insertString(doc.getLength(), (!me && startNewline ? "\n" : "") + first.getKey(), doc.getStyle(first.getValue()));
                while (it.hasNext()) {
                    Entry<String, String> e = it.next();
                    doc.insertString(doc.getLength(), e.getKey(), doc.getStyle(e.getValue()));
                }
                messages.clear();
                
                lastSender = me ? "" : name;
            }
            catch (Exception ex) {
                ;
            }
        }
    }

    boolean freezeMsg = false, freezeUsr = false;
    
    public void replaceChatMessages(List<ChatMessage> l) {
        freezeMsg = true;
        try {
            List<ChatMessage> sysmsgs = new ArrayList<ChatMessage>(SYS_MESSAGES);
            removeChatMessages();
            for (ChatMessage m : l) {
                if (m.getDate() != null) {
                    Iterator<ChatMessage> si = sysmsgs.iterator();
                    while (si.hasNext()) {
                        ChatMessage sm = si.next();
                        if (sm.getDate() != null) {
                            if (sm.getDate().before(m.getDate())) {
                                showSysMessage(sm.getDate(), sm.getFullName(), sm.getID());
                                si.remove();
                            }
                        }
                        else {
                            si.remove();
                        }
                    }
                    addChatMessage(m);
                }
            }
            for (ChatMessage sm : sysmsgs) {
                showSysMessage(sm.getDate(), sm.getFullName(), sm.getID());
            }
            sysmsgs.clear();
        }
        catch (Exception ex) {
            ;
        }
        freezeMsg = false;
        tpMessages.repaint();
    }
    
    /**
     * Két komponens mérete állítható SplitPane segítségével.
     */
    private static JSplitPane createSplitPane(int orientation, Component c1, Component c2) {
        JSplitPane pane = new JSplitPane(orientation, c1, c2);
        ((BasicSplitPaneUI) pane.getUI()).getDivider().setBorder(null);
        pane.setContinuousLayout(true);
        pane.setDividerSize(DIVIDER_SIZE);
        pane.setResizeWeight(1.0);
        pane.setBorder(null);
        return pane;
    }
    
    /**
     * Teszt.
     */
    public static void main(String[] args) {
        ChatFrame.setSenderName("test1");
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                UIUtil.setSystemLookAndFeel();
                ChatFrame d = new ChatFrame();
                UserData u1 = new UserData();
                u1.setUserName("test1");
                u1.setFullName("Első tesztelő");
                u1.setSignInDate(new Date());
                u1.setClientCount(2);
                d.setUserVisible(u1, true, true);
                UserData u2 = new UserData();
                u2.setUserName("test2");
                u2.setFullName("Második tesztelő");
                u2.setSignInDate(new Date());
                d.setUserVisible(u2, true, false);
                d.addMessage(new Date(), "test1", "Ez az üzenet egy http://example.com/asd URL-t tartalmaz, melyre kattintva megjelenik az alapértelmezett böngésző.");
                d.setDefaultCloseOperation(EXIT_ON_CLOSE);
                d.setLocationRelativeTo(d);
                d.setVisible(true);
            }
            
        });
    }
    
}