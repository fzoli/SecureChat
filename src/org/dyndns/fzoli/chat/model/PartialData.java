package org.dyndns.fzoli.chat.model;

import java.io.Serializable;

/**
 * A Data osztály részadata (részhalmaza).
 * Egy PartialData objektumot átadva a Data objektumnak, egyszerű frissítést lehet végrehajtani.
 * Generikus paraméterben adható meg, milyen típusú adatot tartalmaz az objektum, és
 * hogy az osztály melyik osztálynak a részhalmaza, melyen alkalmaznia kell a módosítást.
 */
public abstract class PartialData<D extends BaseData, T extends Serializable> extends BaseData<D> implements Serializable {

    /**
     * Az adat.
     */
    protected T data;

    /**
     * Részadat inicializálása és beállítása.
     * @param data az adat
     */
    protected PartialData(T data) {
        this.data = data;
    }

    /**
     * A részadatot alkalmazza a paraméterben megadott adaton.
     * @param d a teljes adat, amin a módosítást alkalmazni kell
     */
    public abstract void apply(D d);

}
