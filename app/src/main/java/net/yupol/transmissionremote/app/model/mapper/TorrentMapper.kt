package net.yupol.transmissionremote.app.model.mapper

import net.yupol.transmissionremote.app.model.TorrentViewModel
import net.yupol.transmissionremote.data.model.ErrorType
import net.yupol.transmissionremote.domain.model.Torrent
import net.yupol.transmissionremote.domain.model.Torrent.Status.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class TorrentMapper @Inject constructor() {

    fun toViewModel(torrent: Torrent, selected: Boolean): TorrentViewModel {
        val rechecking = torrent.status == CHECK || torrent.status == CHECK_WAIT
        return TorrentViewModel(
                id = torrent.id,
                name = torrent.name,
                downloadedSize = torrent.sizeWhenDone - torrent.leftUntilDone,
                totalSize = torrent.totalSize,
                sizeWhenDone = torrent.sizeWhenDone,
                completed = torrent.isCompleted,
                uploadedSize = torrent.uploadedSize,
                uploadRatio = max(torrent.uploadRatio, 0.0),
                downloadRate = torrent.downloadRate,
                uploadRate = torrent.uploadRate,
                paused = torrent.status == STOPPED,
                rechecking = rechecking,
                progressPercent = torrent.percentDone,
                recheckProgressPercent = torrent.recheckProgress,
                eta = torrent.eta,
                errorMessage = torrent.errorString,
                errorType = ErrorType.fromCode(torrent.errorId),
                selected = selected
        )
    }

    fun toViewModel(torrents: Iterable<Torrent>, selectedTorrents: Set<Int>): List<TorrentViewModel> {
        return torrents.map { torrent ->
            toViewModel(torrent, selectedTorrents.contains(torrent.id))
        }
    }
}
