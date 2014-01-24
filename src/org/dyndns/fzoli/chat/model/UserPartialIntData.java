package org.dyndns.fzoli.chat.model;

/**
 * Ha egy felhasználó több helyről jelentkezik be, növelni kell a clientCount tulajdonságát.
 * @author zoli
 */
public class UserPartialIntData extends UserPartialData<Integer> {
    
    public enum Type {
        CLIENT_COUNT
    }
    
    private final Type TYPE;
    
    public UserPartialIntData(String userName, Type t, Integer i) {
        super(userName, i);
        TYPE = t;
    }

    @Override
    public void apply(UserData d) {
        if (d != null && TYPE != null) {
            switch (TYPE) {
                case CLIENT_COUNT:
                    d.setClientCount(data);
                    break;
            }
        }
    }
    
}
