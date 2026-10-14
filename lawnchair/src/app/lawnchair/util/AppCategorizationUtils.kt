package app.lawnchair.util

import android.content.Context
import app.lawnchair.flowerpot.Flowerpot
import com.android.launcher3.R
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ApplicationInfoWrapper

/** Google first, then functional categories, with System Apps as a fallback only. */
fun categorizeAppsWithSystemAndGoogle(
    apps: List<AppInfo>,
    context: Context,
): Map<String, List<AppInfo>> {
    val sortedApps = apps.distinctBy { it.toComponentKey().toString() }
        .sortedBy { it.title.toString().lowercase(java.util.Locale.getDefault()) }
    val googleApps = sortedApps.filter { it.targetPackage?.startsWith("com.google.") == true }
    val otherApps = sortedApps.filterNot { it in googleApps }
    val pots = Flowerpot.Manager.getInstance(context)
    val financeLabel = pots.getPot("FINANCE")?.displayName
    val gameLabel = pots.getPot("GAME")?.displayName
    val categories = linkedMapOf<String, List<AppInfo>>()
    if (googleApps.isNotEmpty()) categories[context.getString(R.string.core_category_google)] = googleApps
    val overrides = mapOf(
        "com.sec.android.app.camera" to "PHOTOGRAPHY",
        "com.sec.android.gallery3d" to "PHOTOGRAPHY",
        "com.samsung.android.app.notes" to "PRODUCTIVITY",
        "com.openai.chatgpt" to "PRODUCTIVITY",
        "com.android.settings" to "TOOLS",
        "com.sec.android.app.myfiles" to "TOOLS",
        "com.samsung.android.bixby.agent" to "TOOLS",
        "com.samsung.android.dialer" to "COMMUNICATION",
        "com.samsung.android.app.contacts" to "COMMUNICATION",
        "com.samsung.android.messaging" to "COMMUNICATION",
        "app.d2lock" to "TOOLS",
    )
    val overridden = otherApps.filter { it.targetPackage in overrides }
    pots.categorizeApps(otherApps.filterNot { it in overridden }).forEach { (category, items) ->
        when (category) {
            financeLabel -> categories[context.getString(R.string.core_category_finance)] = items
            gameLabel -> categories[context.getString(R.string.core_category_games)] = items
            "Other" -> {
                val system = items.filter {
                    val intent = it.intent
                    intent != null && ApplicationInfoWrapper(context, intent).isSystem()
                }
                val remaining = items.filterNot { it in system }
                if (system.isNotEmpty()) categories[context.getString(R.string.core_category_system)] = system
                if (remaining.isNotEmpty()) categories[context.getString(R.string.core_category_other)] = remaining
            }
            else -> categories[category] = items
        }
    }
    overridden.forEach { app ->
        val title = pots.getPot(overrides.getValue(app.targetPackage!!))?.displayName
            ?: context.getString(R.string.core_category_other)
        categories[title] = categories[title].orEmpty() + app
    }
    val googleTitle = context.getString(R.string.core_category_google)
    val otherTitle = context.getString(R.string.core_category_other)
    return categories.entries.sortedWith(compareBy<Map.Entry<String, List<AppInfo>>> {
        when (it.key) { googleTitle -> 0; otherTitle -> 2; else -> 1 }
    }.thenBy { it.key.lowercase(java.util.Locale.getDefault()) }).associate { (title, members) ->
        title to members.sortedBy { it.title.toString().lowercase(java.util.Locale.getDefault()) }
    }
}
