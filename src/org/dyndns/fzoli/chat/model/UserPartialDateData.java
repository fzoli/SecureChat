package org.dyndns.fzoli.chat.model;

import java.util.Date;

/**
 *
 * @author zoli
 */
public class UserPartialDateData extends UserPartialData<Date> {
    
    public enum Type {
        SIGN_IN,
        SIGN_OUT
    }
    
    private final Type TYPE;
    
    public UserPartialDateData(String userName, Type t, Date d) {
        super(userName, d);
        TYPE = t;
    }

    @Override
    public void apply(UserData d) {
        if (d != null && TYPE != null) {
            switch (TYPE) {
                case SIGN_IN:
                    d.setSignInDate(data);
                    break;
                case SIGN_OUT:
                    d.setSignOutDate(data);
                    break;
            }
        }
    }
    
}
