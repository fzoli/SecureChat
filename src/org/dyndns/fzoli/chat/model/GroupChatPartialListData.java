package org.dyndns.fzoli.chat.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zoli
 */
public class GroupChatPartialListData<T> extends GroupChatPartialData<GroupChatData, ArrayList<T>> {
    
    private final String CLASS;
    
    public GroupChatPartialListData(Class<T> clazz, List<T> data) {
        super(new ArrayList<T>(data));
        CLASS = clazz.getCanonicalName();
    }

    @Override
    public void apply(GroupChatData gcd) {
        if (gcd != null) {
            try {
                Class<?> clazz = Class.forName(CLASS);
                if (ChatMessage.class == clazz) {
                    GroupChatData.replace(gcd.getMessages(), (List<ChatMessage>) data);
                }
                else if (UserData.class == clazz) {
                    GroupChatData.replace(gcd.getUsers(), (List<UserData>) data);
                }
            }
            catch (ClassNotFoundException ex) {
                ;
            }
        }
    }

    @Override
    protected boolean equals(GroupChatData d) {
        return false;
    }
    
}
