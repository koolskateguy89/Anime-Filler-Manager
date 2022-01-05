package afm.utils

import afm.database.Database
import afm.user.Settings

object OnClose : Thread() {

    init {
        name = "On close thread"
        isDaemon = true
    }

    // Save Settings preferences & Save MyList & ToWatch into database
    override fun run() {
        Settings.save()
        Database.saveAll()
    }

}
