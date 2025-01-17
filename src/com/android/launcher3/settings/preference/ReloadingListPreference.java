package com.android.launcher3.settings.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

import java.util.function.Function;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.THREAD_POOL_EXECUTOR;

@SuppressWarnings("unused")
public class ReloadingListPreference extends ListPreference {
    public interface OnReloadListener {
        Runnable listUpdater(ReloadingListPreference pref);
    }

    private OnReloadListener mOnReloadListener = null;

    public ReloadingListPreference(Context context) {
        super(context);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                   int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        // Run the entries updater on the main thread immediately.
        // Should be fast as the data was cached from the async load before.
        // If it wasn't, we need to block to ensure the data has been loaded.
        loadEntries(false);
        super.onClick();
    }

    public void setOnReloadListener(Function<Context, OnReloadListener> supplier) {
        mOnReloadListener = supplier.apply(getContext());
        loadEntries(true);
    }

    public void onResume() {
        loadEntries(true);
    }

    private void loadEntries(boolean async) {
        async = false;
	if (mOnReloadListener != null) {
            if (async) {
                THREAD_POOL_EXECUTOR.execute(
                        () -> MAIN_EXECUTOR.execute(mOnReloadListener.listUpdater(this)));
            } else {
                mOnReloadListener.listUpdater(this).run();
            }
        } else {
	    throw new RuntimeException("mOnReladListener is not set!");
	}
    }

    void setEntriesWithValues(CharSequence[] entries, CharSequence[] entryValues) {
        setEntries(entries);
        setEntryValues(entryValues);
        setSummary("%s");
    }
}
