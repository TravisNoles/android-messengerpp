package org.solovyev.android.messenger.realms;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.solovyev.android.db.*;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Singleton
public class SqliteAccountDao extends AbstractSQLiteHelper implements AccountDao {

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Nullable
	private SecretKey secret;

	@Inject
	public SqliteAccountDao(@Nonnull Application context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	SqliteAccountDao(@Nonnull Context context, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
	}

	@Override
	public void init() {
		secret = MessengerApplication.getServiceLocator().getSecurityService().getSecretKey();
	}

	@Override
	public void insertRealm(@Nonnull Account account) throws AccountException {
		try {
			AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new InsertRealm(account, secret)));
		} catch (RealmRuntimeException e) {
			throw new AccountException(e);
		}
	}

	@Override
	public void deleteRealm(@Nonnull String realmId) {
		AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new DeleteRealm(realmId)));
	}

	@Nonnull
	@Override
	public Collection<Account> loadRealms() {
		try {
			return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadRealm(getContext(), null, getSqliteOpenHelper()));
		} catch (RealmRuntimeException e) {
			MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			return Collections.emptyList();
		}
	}

	@Override
	public void deleteAllRealms() {
		AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(DeleteAllRowsDbExec.newInstance("realms")));
	}

	@Override
	public void updateRealm(@Nonnull Account account) throws AccountException {
		try {
			AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateRealm(account, secret)));
		} catch (RealmRuntimeException e) {
			throw new AccountException(e);
		}
	}

	@Nonnull
	@Override
	public Collection<Account> loadRealmsInState(@Nonnull AccountState state) {
		try {
			return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadRealm(getContext(), state, getSqliteOpenHelper()));
		} catch (RealmRuntimeException e) {
			MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			return Collections.emptyList();
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	private static class InsertRealm extends AbstractObjectDbExec<Account> {

		@Nullable
		private final SecretKey secret;

		public InsertRealm(@Nonnull Account account, @Nullable SecretKey secret) {
			super(account);
			this.secret = secret;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Account account = getNotNullObject();

			final ContentValues values = toContentValues(account, secret);

			return db.insert("realms", null, values);
		}
	}

	private static class UpdateRealm extends AbstractObjectDbExec<Account> {

		@Nullable
		private final SecretKey secret;

		public UpdateRealm(@Nonnull Account account, @Nullable SecretKey secret) {
			super(account);
			this.secret = secret;
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final Account account = getNotNullObject();

			final ContentValues values = toContentValues(account, secret);

			return db.update("realms", values, "id = ?", new String[]{account.getId()});
		}
	}

	@Nonnull
	private static ContentValues toContentValues(@Nonnull Account account, @Nullable SecretKey secret) throws RealmRuntimeException {
		final ContentValues values = new ContentValues();

		values.put("id", account.getId());
		values.put("realm_def_id", account.getRealmDef().getId());
		values.put("user_id", account.getUser().getEntity().getEntityId());

		final AccountConfiguration configuration;

		try {
			final Cipherer<AccountConfiguration, AccountConfiguration> cipherer = account.getRealmDef().getCipherer();
			if (cipherer != null && secret != null) {
				configuration = cipherer.encrypt(secret, account.getConfiguration());
			} else {
				configuration = account.getConfiguration();
			}
			values.put("configuration", new Gson().toJson(configuration));
		} catch (CiphererException e) {
			throw new RealmRuntimeException(account.getId(), e);
		}

		values.put("state", account.getState().name());

		return values;
	}

	private class LoadRealm extends AbstractDbQuery<Collection<Account>> {

		@Nullable
		private final AccountState state;

		protected LoadRealm(@Nonnull Context context, @Nullable AccountState state, @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.state = state;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			if (state == null) {
				return db.query("realms", null, null, null, null, null, null);
			} else {
				return db.query("realms", null, "state = ?", new String[]{state.name()}, null, null, null);
			}
		}

		@Nonnull
		@Override
		public Collection<Account> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<Account>(new AccountMapper(secret)).convert(cursor);
		}
	}

	private static class DeleteRealm extends AbstractObjectDbExec<String> {

		public DeleteRealm(@Nonnull String realmId) {
			super(realmId);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final String realmId = getNotNullObject();

			return db.delete("realms", "id = ?", new String[]{realmId});
		}
	}

}