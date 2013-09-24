package org.solovyev.android.view;

import javax.annotation.Nullable;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 1:35 AM
 */
public interface PullToRefreshListViewProvider {

	@Nullable
	PullToRefreshListView getPullToRefreshListView();
}
