package org.dyndns.fzoli.chat.model;

/**
 * A felhasználó megváltozott státuszát tárolja.
 * @author zoli
 */
public class UserPartialStatusData extends UserPartialData<UserData.Status> {

    public UserPartialStatusData(String userName, UserData.Status status) {
        super(userName, status);
    }
    
    public UserPartialStatusData(UserData.Status status) {
        super(status);
    }
    
    @Override
    public void apply(UserData d) {
        if (equals(d)) d.setStatus(data);
    }
    
}
