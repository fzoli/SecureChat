package org.dyndns.fzoli.ui;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

/**
 * Based on: http://stackoverflow.com/questions/4045722/how-to-make-jtextpane-autoscroll-only-when-scroll-bar-is-at-bottom-and-scroll-lo
 */
public class ScrollingDocumentListener implements DocumentListener {

    public static ScrollingDocumentListener apply(JTextComponent textArea, JScrollPane scrollPane) {
        ScrollingDocumentListener l;
        textArea.getDocument().addDocumentListener(l = new ScrollingDocumentListener(textArea, scrollPane));
        return l;
    }
    
    public static boolean isScrollBarFullyExtended(JScrollBar vScrollBar) {
        BoundedRangeModel model = vScrollBar.getModel();
        return (model.getExtent() + model.getValue()) == model.getMaximum();
    }
    
    public static void scrollToBottom(final JComponent component) {
        // Push the call to "scrollToBottom" back TWO PLACES on the
        // AWT-EDT queue so that it runs *after* Swing has had an
        // opportunity to "react" to the appending of new text:
        // this ensures that we "scrollToBottom" only after a new
        // bottom has been recalculated during the natural
        // revalidation of the GUI that occurs after having
        // appending new text to the JTextArea.
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                Rectangle visibleRect = component.getVisibleRect();
                visibleRect.y = component.getHeight() - visibleRect.height;
                component.scrollRectToVisible(visibleRect);
            }

        });
    }

    private final JTextComponent TEXT_AREA;
    
    private final JScrollBar VERTICAL_SCROLL_BAR;
    
    private boolean enabled = true;
    
    public ScrollingDocumentListener(JTextComponent textArea, JScrollPane scrollPane) {
        // Configure JTextArea to not update the cursor position after
        // inserting or appending text to the JTextArea. This disables the
        // JTextArea's usual behavior of scrolling automatically whenever
        // inserting or appending text into the JTextArea: we want scrolling
        // to only occur at our discretion, not blindly. NOTE that this
        // breaks normal typing into the JTextArea. This approach assumes
        // that all updates to the ScrollingJTextArea are programmatic.
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        TEXT_AREA = textArea;
        VERTICAL_SCROLL_BAR = scrollPane.getVerticalScrollBar();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        maybeScrollToBottom();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        maybeScrollToBottom();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        maybeScrollToBottom();
    }

    private void maybeScrollToBottom() {
        if (!isEnabled()) return;
        boolean scrollBarAtBottom = isScrollBarFullyExtended(VERTICAL_SCROLL_BAR);
        boolean scrollLock;
        try {
            scrollLock = Toolkit.getDefaultToolkit()
                .getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);
        }
        catch (Exception ex) {
            // On Mac the getLockingKeyState method is not supported.
            scrollLock = false;
        }
        if (scrollBarAtBottom && !scrollLock) {
            scrollToBottom(TEXT_AREA);
        }
    }
    
}
