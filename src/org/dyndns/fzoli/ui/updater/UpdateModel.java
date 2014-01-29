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
    public final boolean SILENT;
    public final String TITLE, LOADING_MESSAGE, ERR_MESSAGE_A, ERR_MESSAGE_B;
    
    UpdateModel(Map<String, String> updateMap, boolean inJarbundler, String binaryPath, String[] args, boolean silent, String title, String loadingMsg, String errMsgA, String errMsgB) {
        this.UPDATE_MAP = updateMap;
        this.JARBUNDLER = inJarbundler;
        this.BINARY = binaryPath;
        this.ARGS = args;
        this.SILENT = silent;
        this.TITLE = title;
        this.LOADING_MESSAGE = loadingMsg;
        this.ERR_MESSAGE_A = errMsgA;
        this.ERR_MESSAGE_B = errMsgB;
    }
    
}
