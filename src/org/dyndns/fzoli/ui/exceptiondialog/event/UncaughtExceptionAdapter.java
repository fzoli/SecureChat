package org.dyndns.fzoli.ui.exceptiondialog.event;

/**
 * A nem kezelt kivételek esetén meghívódó eseménykezelő üres implementációja.
 * @author zoli
 */
public abstract class UncaughtExceptionAdapter implements UncaughtExceptionListener {

    /**
     * A dialógusablak megjelenése előtt lefutó metódus.
     * @param e az esemény
     */
    @Override
    public void exceptionDialogAppears(UncaughtExceptionEvent e) {
        ;
    }

    /**
     * A dialógusablak eltűnése után lefutó metódus.
     */
    @Override
    public void exceptionDialogClosed() {
        ;
    }
    
}