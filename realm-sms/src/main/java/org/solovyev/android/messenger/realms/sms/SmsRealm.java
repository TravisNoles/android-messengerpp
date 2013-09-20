package org.solovyev.android.messenger.realms.sms;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmState;
import org.solovyev.android.messenger.users.CompositeUserChoice;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.Properties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;

import com.google.common.base.Splitter;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:43 PM
 */
final class SmsRealm extends AbstractRealm<SmsAccountConfiguration> {

	public SmsRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull SmsAccountConfiguration configuration, @Nonnull RealmState state) {
		super(id, realmDef, user, configuration, state);
	}

	@Nonnull
	@Override
	protected RealmConnection newRealmConnection0(@Nonnull Context context) {
		return new SmsRealmConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		return context.getString(getRealmDef().getNameResId());
	}

	@Nonnull
	@Override
	public RealmUserService getRealmUserService() {
		return new SmsRealmUserService(this);
	}

	@Nonnull
	@Override
	public RealmChatService getRealmChatService() {
		return new SmsRealmChatService();
	}

	@Override
	public boolean isCompositeUser(@Nonnull User user) {
		return true;
	}

	@Override
	public boolean isCompositeUserDefined(@Nonnull User user) {
		final String phoneNumber = user.getPropertyValueByName(User.PROPERTY_PHONE);
		return !Strings.isEmpty(phoneNumber);
	}

	@Nonnull
	@Override
	public List<CompositeUserChoice> getCompositeUserChoices(@Nonnull User user) {
		final String phoneNumbers = user.getPropertyValueByName(User.PROPERTY_PHONES);
		if (!Strings.isEmpty(phoneNumbers)) {
			final List<CompositeUserChoice> choices = new ArrayList<CompositeUserChoice>();

			int index = 0;
			for (String phoneNumber : Splitter.on(User.PROPERTY_PHONES_SEPARATOR).omitEmptyStrings().split(phoneNumbers)) {
				choices.add(CompositeUserChoice.newInstance(phoneNumber, index));
				index++;
			}

			return choices;
		} else {
			return Collections.emptyList();
		}
	}

	@Nonnull
	@Override
	public User applyCompositeChoice(@Nonnull CompositeUserChoice compositeUserChoice, @Nonnull User user) {
		return user.cloneWithNewProperty(Properties.newProperty(User.PROPERTY_PHONE, compositeUserChoice.getName().toString()));
	}

	@Override
	public boolean isCompositeUserChoicePersisted() {
		return true;
	}

	@Override
	public int getCompositeDialogTitleResId() {
		return R.string.mpp_sms_realm_composite_dialog_title;
	}
}