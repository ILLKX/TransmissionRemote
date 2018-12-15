package net.yupol.transmissionremote.app.preferences;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import com.google.common.collect.ImmutableMap;

import net.yupol.transmissionremote.app.BaseActivity;
import net.yupol.transmissionremote.app.ProgressbarFragment;
import net.yupol.transmissionremote.app.R;
import net.yupol.transmissionremote.app.TransmissionRemote;
import net.yupol.transmissionremote.app.torrentdetails.SaveChangesDialogFragment;
import net.yupol.transmissionremote.data.api.model.ServerSettingsEntity;
import net.yupol.transmissionremote.data.api.rpc.Parameter;
import net.yupol.transmissionremote.model.Server;
import net.yupol.transmissionremote.model.json.ServerSettings;
import net.yupol.transmissionremote.data.api.Transport;
import net.yupol.transmissionremote.data.api.rpc.RpcArgs;
import net.yupol.transmissionremote.model.mapper.ServerMapper;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServerPreferencesActivity extends BaseActivity implements SaveChangesDialogFragment.SaveDiscardListener {

    private static final String TAG = ServerPreferencesActivity.class.getSimpleName();
    private static final String TAG_SERVER_PREFERENCES_FRAGMENT = "tag_server_preferences_fragment";
    private static final String TAG_SAVE_CHANGES_DIALOG = "tag_save_changes_dialog";

    private CompositeDisposable requests = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_preferences_activity);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
        }

        if (savedInstanceState == null) {
            showProgressbarFragment();

            final TransmissionRemote app = (TransmissionRemote) getApplication();
            Server activeServer = app.getActiveServer();
            new Transport(ServerMapper.toDomain(activeServer)).api().serverSettings(ImmutableMap.<String, Object>of())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<ServerSettingsEntity>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            requests.add(d);
                        }

                        @Override
                        public void onSuccess(ServerSettingsEntity entity) {
                            ServerSettings settings = ServerMapper.toViewModel(entity);
                            app.setSpeedLimitEnabled(settings.isAltSpeedLimitEnabled());
                            showPreferencesFragment(settings);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Failed to obtain server settings", e);
                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        requests.clear();
        super.onStop();
    }

    private void showProgressbarFragment() {
        FragmentManager fm = getSupportFragmentManager();

        ServerPreferencesFragment preferencesFragment = (ServerPreferencesFragment)
                fm.findFragmentById(R.id.server_preferences_fragment_container);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        if (preferencesFragment != null) ft.remove(preferencesFragment);
        ft.add(R.id.progress_bar_fragment_container, new ProgressbarFragment());
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        ServerPreferencesFragment fragment = (ServerPreferencesFragment)
                getSupportFragmentManager().findFragmentByTag(TAG_SERVER_PREFERENCES_FRAGMENT);
        if (fragment == null) {
            super.onBackPressed();
            return;
        }

        List<Parameter<String, ?>> sessionParameters = fragment.getSessionParameters();
        if (!sessionParameters.isEmpty()) {
            new SaveChangesDialogFragment().show(getFragmentManager(), TAG_SAVE_CHANGES_DIALOG);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onSavePressed() {
        ServerPreferencesFragment fragment = (ServerPreferencesFragment)
                getSupportFragmentManager().findFragmentByTag(TAG_SERVER_PREFERENCES_FRAGMENT);
        if (fragment != null) {
            new Transport(ServerMapper.toDomain(TransmissionRemote.getInstance().getActiveServer())).api().setServerSettings(RpcArgs.parameters(fragment.getSessionParameters()))
                    .subscribeOn(Schedulers.io())
                    .onErrorComplete()
                    .subscribe();
        }
        super.onBackPressed();
    }

    @Override
    public void onDiscardPressed() {
        super.onBackPressed();
    }

    private void showPreferencesFragment(ServerSettings settings) {
        FragmentManager fm = getSupportFragmentManager();

        ProgressbarFragment progressbarFragment = (ProgressbarFragment)
                fm.findFragmentById(R.id.progress_bar_fragment_container);

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (progressbarFragment != null) ft.remove(progressbarFragment);
        ServerPreferencesFragment fragment = new ServerPreferencesFragment();
        Bundle args = new Bundle();
        args.putParcelable(ServerPreferencesFragment.KEY_SERVER_SETTINGS, settings);
        fragment.setArguments(args);
        ft.add(R.id.server_preferences_fragment_container, fragment, TAG_SERVER_PREFERENCES_FRAGMENT);
        ft.commit();
    }
}
