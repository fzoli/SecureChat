package org.dyndns.fzoli.socket.handler;

import java.util.List;
import org.dyndns.fzoli.socket.Socketter;
import org.dyndns.fzoli.socket.handler.event.HandlerListener;
import org.dyndns.fzoli.socket.process.Process;

/**
 * Kapcsolatkezelő implementálásához kliens és szerver oldalra.
 * A socket feldolgozása előtt az adatok alapján kiválasztja, melyik feldolgozót kell indítani és azt új szálban elindítja.
 * @author zoli
 */
public interface Handler extends Socketter {
    
    /**
     * Azokat az adatfeldolgozókat adja vissza, melyek még dolgoznak.
     */
    public List<Process> getProcesses();
    
    /**
     * A kapcsolatkezelő eseményfigyelőit adja vissza.
     */
    public List<HandlerListener> getHandlerListeners();
    
    /**
     * A kapcsolatkezelőhöz eseményfigyelőt ad hozzá.
     */
    public void addHandlerListener(HandlerListener listener);
    
    /**
     * A kapcsolatkezelőből eseményfigyelőt távolít el.
     */
    public void removeHandlerListener(HandlerListener listener);
    
    /**
     * Ez a metódus fut a külön szálban, és ebben választódik ki és indul el az adatfeldolgozó.
     */
    @Override
    public void run();
    
}
