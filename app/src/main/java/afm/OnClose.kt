package afm

import afm.database.Database
import afm.user.Settings

internal object OnClose : Thread("On close thread") {

    init {
        isDaemon = true
    }

    // Save Settings preferences & Save MyList & ToWatch into database
    override fun run() {
        Settings.save()
        Database.saveAll()
    }

}
