package org.dyndns.fzoli.chat.model;

/**
 * Az adatmodellek alapja.
 * Mindegyik adatmodellnek vannak részhalmazai.
 * Az adatmodell képes frissíteni magát önmagával vagy a részhalmazaival.
 * A generikus paraméterek segítségével megadható, hogy a frissítés
 * mely konkrét típusokkal mehessen végbe.
 * @author zoli
 */
public abstract class Data<D extends Data, PD extends PartialData> extends BaseData<D> {

    /**
     * Nem példányosítható kintről, csak örökölhető.
     */
    protected Data() {
    }
    
    /**
     * Adatmodell létrehozása egy másik adatmodell objektum adataival.
     * @param data a másik adatmodell
     */
    protected Data(D data) {
        if (data != null) update(data);
    }
    
    /**
     * Egy adat frissítése részhalmaz segítségével.
     */
    public void update(PD pd) {
        if (pd != null) pd.apply(this);
    }
    
    /**
     * Az összes adat frissítése adatmodell segítségével.
     */
    public abstract void update(D d);
    
    /**
     * Kinullázza az adatokat, így felszabadulhat a memória.
     */
    public abstract void clear();
    
}
