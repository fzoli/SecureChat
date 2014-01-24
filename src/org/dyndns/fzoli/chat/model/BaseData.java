package org.dyndns.fzoli.chat.model;

import java.io.Serializable;

/**
 *
 * @author zoli
 */
public class BaseData<D extends BaseData> implements Serializable {
    
    /**
     * Nem példányosítható kintről, csak örökölhető.
     */
    protected BaseData() {
    }
    
    /**
     * Két objektumról állapítja meg, hogy egyenlőek-e.
     * @return True, ha a két objektum egyenlő vagy mindkét paraméter null, egyébként false.
     */
    protected boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null ^ o2 == null) return false;
        return o1.equals(o2);
    }
    
}
