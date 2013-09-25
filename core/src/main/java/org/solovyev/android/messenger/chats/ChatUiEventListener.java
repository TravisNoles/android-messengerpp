package org.solovyev.android.messenger.chats;

import android.support.v4.app.Fragment;
import roboguice.event.EventListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.fragments.MultiPaneFragmentManager;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.messages.MessengerMessagesFragment;
import org.solovyev.android.messenger.users.ContactFragmentReuseCondition;
import org.solovyev.android.messenger.users.MessengerContactFragment;
import org.solovyev.android.messenger.users.MessengerContactsInfoFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Builder;

import com.actionbarsherlock.app.ActionBar;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:59 PM
 */
public class ChatUiEventListener implements EventListener<ChatUiEvent> {

	@Nonnull
	private static final String TAG = ChatUiEventListener.class.getSimpleName();

	@Nonnull
	private final MessengerFragmentActivity activity;

	@Nonnull
	private final ChatService chatService;

	public ChatUiEventListener(@Nonnull MessengerFragmentActivity activity, @Nonnull ChatService chatService) {
		this.activity = activity;
		this.chatService = chatService;
	}

	@Override
	public void onEvent(ChatUiEvent event) {
		final Chat chat = event.getChat();
		final ChatUiEventType type = event.getType();

		switch (type) {
			case chat_open_requested:
				handleChatOpenRequestedEvent(chat);
				break;
			case chat_clicked:
				handleChatClickedEvent(chat);
				break;
			case chat_message_read:
				handleMessageReadEvent(chat, event.getDataAsChatMessage());
				break;
		}
	}

	private void handleChatOpenRequestedEvent(@Nonnull final Chat chat) {
		final MultiPaneFragmentManager fragmentService = activity.getMultiPaneFragmentManager();
		if (activity.getMultiPaneManager().isDualPane(activity)) {
			if (!fragmentService.isFragmentShown(MessengerChatsFragment.FRAGMENT_TAG)) {
				final ActionBar.Tab tab = activity.findTabByTag(MessengerChatsFragment.FRAGMENT_TAG);
				if (tab != null) {
					tab.select();
				}
			}

			final MessengerChatsFragment fragment = fragmentService.getFragment(MessengerChatsFragment.FRAGMENT_TAG);
			if (fragment != null) {
				fragment.selectListItem(chat.getId());
			}
		} else {
			fragmentService.goBackTillStart();
			fragmentService.setMainFragment(MultiPaneFragmentDef.newInstance(MessengerMessagesFragment.FRAGMENT_TAG, true, new Builder<Fragment>() {
				@Nonnull
				@Override
				public Fragment build() {
					return new MessengerMessagesFragment(chat);
				}
			}, MessagesFragmentReuseCondition.forChat(chat)));
		}
	}

	private void handleMessageReadEvent(@Nonnull Chat chat, @Nonnull ChatMessage message) {
		chatService.onChatMessageRead(chat, message);
	}

	private void handleChatClickedEvent(@Nonnull final Chat chat) {
		final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();

		if (activity.isDualPane()) {
			fm.setSecondFragment(new Builder<Fragment>() {
				@Nonnull
				@Override
				public Fragment build() {
					return new MessengerMessagesFragment(chat);
				}
			}, MessagesFragmentReuseCondition.forChat(chat), MessengerMessagesFragment.FRAGMENT_TAG);

			if (activity.isTriplePane()) {
				if (chat.isPrivate()) {
					fm.setThirdFragment(new Builder<Fragment>() {
						@Nonnull
						@Override
						public Fragment build() {
							return MessengerContactFragment.newForContact(chat.getSecondUser());
						}
					}, ContactFragmentReuseCondition.forContact(chat.getSecondUser()), MessengerContactFragment.FRAGMENT_TAG);
				} else {
					fm.setThirdFragment(new Builder<Fragment>() {
						@Nonnull
						@Override
						public Fragment build() {
							final List<User> participants = new ArrayList<User>();
							try {
								final Account account = activity.getAccountService().getAccountByEntity(chat.getEntity());
								participants.addAll(activity.getChatService().getParticipantsExcept(chat.getEntity(), account.getUser().getEntity()));
							} catch (UnsupportedAccountException e) {
								App.getExceptionHandler().handleException(e);
							}
							return new MessengerContactsInfoFragment(participants);
						}
					}, null, MessengerContactsInfoFragment.FRAGMENT_TAG);
				}
			}

		} else {
			fm.setMainFragment(new Builder<Fragment>() {
				@Nonnull
				@Override
				public Fragment build() {
					return new MessengerMessagesFragment(chat);
				}
			}, MessagesFragmentReuseCondition.forChat(chat), MessengerMessagesFragment.FRAGMENT_TAG, true);
		}
	}
}