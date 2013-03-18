package org.solovyev.android.messenger.chats;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;

/**
* User: serso
* Date: 3/10/13
* Time: 2:41 PM
*/
public class ChatEvent extends AbstractTypedJEvent<Chat, ChatEventType> {

    ChatEvent(@Nonnull Chat chat, @Nonnull ChatEventType type, Object data) {
        super(chat, type, data);
    }

    @Nonnull
    public Chat getChat() {
        return getEventObject();
    }

    @Nonnull
    public ChatMessage getDataAsChatMessage() {
        return (ChatMessage) getData();
    }
}