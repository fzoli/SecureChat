package org.dyndns.fzoli.ui.updater;

import java.util.Map;

/**
 *
 * @author zoli
 */
class UpdateModel {

    public final boolean JARBUNDLER;
    public final String BINARY;
    public final Map<String, String> UPDATE_MAP;
    public final String[] ARGS;

    UpdateModel(Map<String, String> updateMap, boolean inJarbundler, String binaryPath, String[] args) {
        this.UPDATE_MAP = updateMap;
        this.JARBUNDLER = inJarbundler;
        this.BINARY = binaryPath;
        this.ARGS = args;
    }
    
}
