package net.yupol.transmissionremote.app.opentorrent.presenter

import android.util.Log
import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers.io
import net.yupol.transmissionremote.app.model.PriorityViewModel.HIGH
import net.yupol.transmissionremote.app.model.PriorityViewModel.LOW
import net.yupol.transmissionremote.app.opentorrent.model.TorrentFile
import net.yupol.transmissionremote.app.opentorrent.view.OpenTorrentFileView
import net.yupol.transmissionremote.app.server.ServerManager
import net.yupol.transmissionremote.model.Dir
import java.io.File
import java.util.*

class OpenTorrentFilePresenter(
        private val torrentFilePath: String,
        private val serverManager: ServerManager): MvpNullObjectBasePresenter<OpenTorrentFileView>()
{

    val torrentFile = TorrentFile(torrentFilePath)

    private var currentDir: Dir
    private val breadcrumbs: Stack<Dir> = Stack()

    private var addTorrentRequest: Disposable? = null

    init {
        currentDir = torrentFile.rootDir
        breadcrumbs.push(currentDir)

        if (currentDir.dirs.size == 1 && currentDir.fileIndices.isEmpty()) {
            currentDir = currentDir.dirs.first()
            breadcrumbs.push(currentDir)
        }
    }

    fun viewCreated() {
        view.showDir(currentDir)
        view.showBreadcrumbs(breadcrumbs)
    }

    fun onDirectorySelected(dir: Dir) {
        currentDir = dir
        breadcrumbs.push(dir)
        view.showDir(currentDir)
        view.showBreadcrumbs(breadcrumbs)
    }

    fun onBreadcrumbClicked(position: Int) {
        if (position < 0 || position >= breadcrumbs.size) {
            throw IndexOutOfBoundsException("There is no breadcrumb at position '$position'. " +
                    "# of breadcrumbs: ${breadcrumbs.size}")
        }

        for (i in breadcrumbs.size - 1 downTo position + 1) {
            breadcrumbs.removeAt(i)
        }

        currentDir = breadcrumbs.peek()
        view.showDir(currentDir)
        view.showBreadcrumbs(breadcrumbs)

    }

    fun onSelectAllFilesClicked() {
        torrentFile.selectAllFilesIn(currentDir)
        view.updateFileList()
    }

    fun onSelectNoneFilesClicked() {
        torrentFile.selectNoneFilesIn(currentDir)
        view.updateFileList()
    }

    fun onAddButtonClicked() {
        val addTorrentFile = serverManager.serverComponent?.addTorrentFileUseCase()
                ?: throw IllegalStateException("No server found while adding torrent file")

        val paused = !view.isStartWhenAddedChecked()
        val filesUnwanted = mutableListOf<Int>()
        val priorityHigh = mutableListOf<Int>()
        val priorityLow = mutableListOf<Int>()
        torrentFile.files.forEachIndexed { index, file ->
            if (!file.wanted) filesUnwanted.add(index)
            when (file.priority) {
                HIGH -> priorityHigh.add(index)
                LOW -> priorityLow.add(index)
                else -> {}
            }
        }

        addTorrentRequest = addTorrentFile.execute(
                file = File(torrentFilePath),
                destinationDir = view.getDownloadDirectory(),
                paused = paused,
                filesUnwanted = filesUnwanted,
                priorityHigh = priorityHigh,
                priorityLow = priorityLow)
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribeBy(
                        onSuccess = { result ->
                            Log.d("Add torrent", "result: $result")
                        },
                        onError = { error ->
                            Log.e("Add torrent", "Error: ${error.message}", error)
                        }
                )
    }
}
