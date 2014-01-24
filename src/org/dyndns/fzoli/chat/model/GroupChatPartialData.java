package org.dyndns.fzoli.chat.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * A csoportos beszélgetés egyik adatának módosulását tartalmazza.
 * @author zoli
 */
public abstract class GroupChatPartialData<D extends BaseData, T extends Serializable> extends PartialData<D, T> {

    public GroupChatPartialData(T data) {
        super(data);
    }
    
    /**
     * A paraméterben kapott adatokon végigmegy és ha kell, frissíti az adatukat.
     */
    public int apply(Collection<D> datas) {
        int count = 0;
        if (datas != null) {
            for (D data : datas) {
                if (equals(data)) {
                    apply(data);
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * A csoportos beszélgetésben lévő adatokon végigmegy és ha kell, frissíti az adatukat.
     */
    public abstract void apply(GroupChatData gcd);
    
    /**
     * Megadja, hogy a részadat a paraméterben megadott modelhez tartozik-e.
     */
    protected abstract boolean equals(D d);
    
}
