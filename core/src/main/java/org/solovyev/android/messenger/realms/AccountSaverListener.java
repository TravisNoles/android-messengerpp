package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.widget.Toast;
import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.tasks.Tasks;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:47 PM
 */
final class AccountSaverListener extends MessengerContextCallback<Activity, Account> {

	private AccountSaverListener() {
	}

	@Nonnull
	static FutureCallback<Account> newInstance(@Nonnull Activity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new AccountSaverListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity context, Account account) {
		final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
		eventManager.fire(AccountGuiEventType.newAccountEditFinishedEvent(account, AccountGuiEventType.FinishedState.saved));
	}

	@Override
	public void onFailure(@Nonnull Activity context, Throwable e) {
		if (e instanceof InvalidCredentialsException) {
			Toast.makeText(context, "Invalid credentials!", Toast.LENGTH_SHORT).show();
		} else if (e instanceof AccountAlreadyExistsException) {
			Toast.makeText(context, "Same account already configured!", Toast.LENGTH_SHORT).show();
		} else {
			super.onFailure(context, e);
		}
	}
}