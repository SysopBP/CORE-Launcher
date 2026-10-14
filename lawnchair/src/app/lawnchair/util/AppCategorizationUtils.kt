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
    val googleApps = apps.filter { it.targetPackage?.startsWith("com.google.") == true }
    val otherApps = apps.filterNot { it in googleApps }
    val pots = Flowerpot.Manager.getInstance(context)
    val financeLabel = pots.getPot("FINANCE")?.displayName
    val gameLabel = pots.getPot("GAME")?.displayName
    val categories = linkedMapOf<String, List<AppInfo>>()
    if (googleApps.isNotEmpty()) categories[context.getString(R.string.core_category_google)] = googleApps
    pots.categorizeApps(otherApps).forEach { (category, items) ->
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
    return categories
}
