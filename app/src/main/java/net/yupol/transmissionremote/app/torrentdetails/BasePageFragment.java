package net.yupol.transmissionremote.app.torrentdetails;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.yupol.transmissionremote.app.BaseFragment;
import net.yupol.transmissionremote.model.json.Torrent;
import net.yupol.transmissionremote.model.json.TorrentInfo;

public abstract class BasePageFragment extends BaseFragment implements OnDataAvailableListener<TorrentInfo> {

    private static final String KEY_TORRENT = "key_torrent";
    private static final String KEY_TORRENT_INFO = "key_torrent_info";

    private Torrent torrent;
    private TorrentInfo torrentInfo;

    @CallSuper
    public void setTorrent(Torrent torrent) {
        this.torrent = torrent;
    }

    protected Torrent getTorrent() {
        return torrent;
    }

    @CallSuper
    public void setTorrentInfo(TorrentInfo torrentInfo) {
        this.torrentInfo = torrentInfo;
    }

    protected TorrentInfo getTorrentInfo() {
        return torrentInfo;
    }

    @Override
    public void onDataAvailable(TorrentInfo data) {
        setTorrentInfo(data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TORRENT, torrent);
        outState.putParcelable(KEY_TORRENT_INFO, torrentInfo);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() instanceof TorrentDetailsActivity) {
            TorrentDetailsActivity activity = (TorrentDetailsActivity) getActivity();
            torrentInfo = activity.getTorrentInfo();
            activity.addTorrentInfoListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getActivity() instanceof TorrentDetailsActivity) {
            TorrentDetailsActivity activity = (TorrentDetailsActivity) getActivity();
            activity.removeTorrentInfoListener(this);
        }
    }

    @Nullable
    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (torrent == null) torrent = savedInstanceState.getParcelable(KEY_TORRENT);
            if (torrentInfo == null) torrentInfo = savedInstanceState.getParcelable(KEY_TORRENT_INFO);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
