package org.solovyev.android.messenger.users;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:17 PM
 */
public final class Users {

	private Users() {
	}

	@Nonnull
	public static String getDisplayNameFor(@Nonnull Entity user) {
		return App.getUserService().getUserById(user).getDisplayName();
	}

	@Nonnull
	public static User newUser(@Nonnull String realmId,
							   @Nonnull String accountUserId,
							   @Nonnull UserSyncData userSyncData,
							   @Nonnull List<AProperty> properties) {
		final Entity entity = EntityImpl.newInstance(realmId, accountUserId);
		return newUser(entity, userSyncData, properties);
	}

	@Nonnull
	public static User newEmptyUser(@Nonnull Entity realmUser) {
		return newUser(realmUser, Users.newNeverSyncedUserSyncData(), Collections.<AProperty>emptyList());
	}

	@Nonnull
	public static User newEmptyUser(@Nonnull String userId) {
		return newEmptyUser(EntityImpl.fromEntityId(userId));
	}

	@Nonnull
	public static User newUser(@Nonnull Entity entity,
							   @Nonnull UserSyncData userSyncData,
							   @Nonnull Collection<AProperty> properties) {
		return UserImpl.newInstance(entity, userSyncData, properties);
	}

	@Nonnull
	public static UserSyncData newNeverSyncedUserSyncData() {
		return UserSyncDataImpl.newNeverSyncedInstance();
	}

	@Nonnull
	public static UserSyncData newUserSyncData(@Nullable DateTime lastPropertiesSyncDate,
											   @Nullable DateTime lastContactsSyncDate,
											   @Nullable DateTime lastChatsSyncDate,
											   @Nullable DateTime lastUserIconsSyncDate) {
		return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	@Nonnull
	public static UserSyncData newUserSyncData(@Nullable String lastPropertiesSyncDate,
											   @Nullable String lastContactsSyncDate,
											   @Nullable String lastChatsSyncDate,
											   @Nullable String lastUserIconsSyncDate) {
		return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	public static void setUserOnlineProperty(@Nonnull List<AProperty> properties, boolean online) {
		Iterables.removeIf(properties, new Predicate<AProperty>() {
			@Override
			public boolean apply(@Nullable AProperty property) {
				return property != null && property.getName().equals(User.PROPERTY_ONLINE);
			}
		});
		properties.add(Properties.newProperty(User.PROPERTY_ONLINE, String.valueOf(online)));
	}

	public static void tryParseNameProperties(@Nonnull List<AProperty> properties, @Nullable String fullName) {
		if (fullName != null) {
			int firstSpaceSymbolIndex = fullName.indexOf(' ');
			int lastSpaceSymbolIndex = fullName.lastIndexOf(' ');
			if (firstSpaceSymbolIndex != -1 && firstSpaceSymbolIndex == lastSpaceSymbolIndex) {
				// only one space in the string
				// Proof:
				// 1. if no spaces => both return -1
				// 2. if more than one spaces => both return different
				final String firstName = fullName.substring(0, firstSpaceSymbolIndex);
				final String lastName = fullName.substring(firstSpaceSymbolIndex + 1);
				properties.add(Properties.newProperty(User.PROPERTY_FIRST_NAME, firstName));
				properties.add(Properties.newProperty(User.PROPERTY_LAST_NAME, lastName));
			} else {
				// just store full name in first name field
				properties.add(Properties.newProperty(User.PROPERTY_FIRST_NAME, fullName));
			}
		}
	}
}
