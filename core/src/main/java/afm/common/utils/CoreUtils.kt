@file:JvmName("CoreUtils")
@file:JvmMultifileClass

package afm.common.utils

import java.net.URL
import java.util.Calendar
import java.util.prefs.Preferences

val UtilsJavaClass: Class<*> = object {}.javaClass.enclosingClass
val classLoader: ClassLoader = object {}.javaClass.classLoader

val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

@get:JvmName("inJar")
val inJar: Boolean = run {
    val url: URL? = classLoader.getResource("")
    // for some reason now url is null when in jar
    url == null || url.protocol == "jar"
}

@get:JvmName("isFirstRun")
val isFirstRun: Boolean = run {
    val prefs = Preferences.userRoot().node(UtilsJavaClass.canonicalName)
    val firstRun = prefs.getBoolean("FIRST_RUN", true)
    prefs.putBoolean("FIRST_RUN", false)
    firstRun
}
