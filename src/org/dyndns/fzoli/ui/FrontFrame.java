package org.dyndns.fzoli.ui;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import javax.swing.JFrame;

/**
 * Tálcáról is előtérbe hozható frame.
 * @author zoli
 */
public class FrontFrame extends JFrame {
    
    public FrontFrame() throws HeadlessException {
    }

    public FrontFrame(GraphicsConfiguration gc) {
        super(gc);
    }

    public FrontFrame(String title) throws HeadlessException {
        super(title);
    }

    public FrontFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    /**
     * Előtérbe hozza az ablakot akkor is, ha az le van csukva a talcára.
     */
    @Override
    public void toFront() {
        int state = super.getExtendedState();
        state &= ~ICONIFIED;
        super.setExtendedState(state);
        super.setAlwaysOnTop(true);
        super.toFront();
        super.requestFocus();
        super.setAlwaysOnTop(false);
    }
    
}
