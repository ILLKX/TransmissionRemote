package net.yupol.transmissionremote.app.server

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import net.yupol.transmissionremote.app.R
import net.yupol.transmissionremote.app.TransmissionRemote
import net.yupol.transmissionremote.domain.repository.ServerListRepository
import javax.inject.Inject

class AddServerActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_TAG = "fragment_tag"

        fun intent(context: Context): Intent {
            return Intent(context, AddServerActivity::class.java)
        }
    }

    @Inject lateinit var repo: ServerListRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        TransmissionRemote.getInstance().appComponent().inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_server_activity)
        ButterKnife.bind(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ServerDetailsFragment.create()
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, FRAGMENT_TAG)
                    .commit()
        }
    }

    @OnClick(R.id.ok_button)
    fun onOkButtonClicked() {
        val server = findFragment().newServer
        if (server != null) {
            repo.addServer(server)
            repo.setActiveServer(server)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun findFragment(): ServerDetailsFragment {
        return supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as ServerDetailsFragment
    }
}
