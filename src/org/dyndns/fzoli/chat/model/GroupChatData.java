package org.dyndns.fzoli.chat.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Egy csoportos beszélgetés adatait tartalmazza (online felhasználók, elküldött üzenetek).
 * @author zoli
 */
public class GroupChatData extends Data<GroupChatData, GroupChatPartialData> {
    
    private final List<UserData> USERS;
    private final List<ChatMessage> MESSAGES;

    public GroupChatData(List<UserData> users, List<ChatMessage> messages) {
        USERS = users;
        MESSAGES = messages;
    }
    
    public GroupChatData(GroupChatData d) {
        this(new ArrayList<UserData>(d.getUsers()), new ArrayList<ChatMessage>(d.getMessages()));
    }
    
    public List<UserData> getUsers() {
        return USERS;
    }

    public List<ChatMessage> getMessages() {
        return MESSAGES;
    }

    @Override
    public void update(GroupChatData d) {
        if (d != null) {
            replace(USERS, d.USERS);
            replace(MESSAGES, d.MESSAGES);
        }
    }
    
    @Override
    public void clear() {
        USERS.clear();
        MESSAGES.clear();
    }
    
    public static <T> void replace(List<T> to, List<T> from) {
        if (to != null) {
            to.clear();
            if (from != null) {
                for (T o : from) {
                    to.add(o);
                }
            }
        }
    }
    
//    public static <T> void replace(Class<? super T> clazz, List<T> to, List<?> from) {
//        if (to != null) {
//            to.clear();
//            if (clazz != null && from != null) {
//                for (Object o : from) {
//                    if (o != null && clazz.isInstance(o)) {
//                        to.add((T) o);
//                    }
//                }
//            }
//        }
//    }
    
    public static ChatMessage findChatMsg(List<ChatMessage> c, Integer id) {
        for (ChatMessage m : c) {
            if (ChatMessage.equals(m, id)) return m;
        }
        return null;
    }
    
    public static UserData findUser(List<UserData> c, String s) {
        for (UserData d : c) {
            if (UserData.equals(d, s)) return d;
        }
        return null;
    }
    
    public static int indexOf(List<UserData> c, UserData d) {
        for (int i = 0; i < c.size(); i++) {
            if (UserData.equals(c.get(i), d)) return i;
        }
        return -1;
    }
    
}
