/*
 * Copyright 2022, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.ui.preferences

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.lawnchair.icons.CustomAdaptiveIconDrawable
import app.lawnchair.ui.preferences.destinations.IconPackInfo
import app.lawnchair.util.Constants.LAWNICONS_PACKAGE_NAME
import app.lawnchair.util.getPackageVersionCode
import com.android.launcher3.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal val iconPackIntents = listOf(
    Intent("com.novalauncher.THEME"),
    Intent("org.adw.launcher.icons.ACTION_PICK_ICON"),
    Intent("com.dlto.atom.launcher.THEME"),
    Intent("android.intent.action.MAIN").addCategory("org.adw.launcher.THEMES"),
    Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"),
)

/** Discover both advertised themes and exported APKs containing an appfilter. */
internal fun getInstalledIconPackApplications(pm: PackageManager) = buildMap<String, android.content.pm.ApplicationInfo> {
    iconPackIntents.flatMap { pm.queryIntentActivities(it, 0) }.forEach {
        val info = it.activityInfo.applicationInfo
        if (info.enabled) put(info.packageName, info)
    }
    pm.getInstalledApplications(0).forEach { info ->
        if (!info.enabled || containsKey(info.packageName)) return@forEach
        try {
            val resources = pm.getResourcesForApplication(info)
            @Suppress("DiscouragedApi")
            val appfilterId = resources.getIdentifier("appfilter", "xml", info.packageName)
            val hasAppfilter = appfilterId != 0 || runCatching {
                resources.assets.open("appfilter.xml").use { }
            }.isSuccess
            if (hasAppfilter) put(info.packageName, info)
        } catch (_: PackageManager.NameNotFoundException) {
            // The package may have been removed while scanning.
        }
    }
}.values.toList()

class PreferenceViewModel(private val app: Application) :
    AndroidViewModel(app),
    PreferenceInteractor {

    private val themedIconPackIntents = listOf(Intent(app.getString(R.string.icon_packs_intent_name)))
    private val packageChanges = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(app, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        trySend(Unit)
        awaitClose { app.unregisterReceiver(receiver) }
    }

    override val iconPacks = packageChanges.map {
        val pm = app.packageManager
        val iconPacks = getInstalledIconPackApplications(pm).map { info ->
            IconPackInfo(
                pm.getApplicationLabel(info).toString(),
                info.packageName,
                CustomAdaptiveIconDrawable.wrapNonNull(pm.getApplicationIcon(info)),
            )
        }
        val lawnchairIcon = CustomAdaptiveIconDrawable.wrapNonNull(
            ContextCompat.getDrawable(app, R.drawable.ic_launcher_home)!!,
        )
        val defaultIconPack = IconPackInfo(
            name = app.getString(R.string.system_icons),
            packageName = "",
            icon = lawnchairIcon,
        )
        val withSystemIcons = listOf(defaultIconPack) + iconPacks.sortedBy { it.name }
        withSystemIcons
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    override val themedIconPacks = packageChanges.map {
        val pm = app.packageManager
        val themedIconPacks = themedIconPackIntents
            .flatMap { pm.queryIntentActivities(it, 0) }
            .associateBy { it.activityInfo.packageName }
            .mapTo(mutableSetOf()) { (_, info) ->
                IconPackInfo(
                    info.loadLabel(pm).toString(),
                    info.activityInfo.packageName,
                    CustomAdaptiveIconDrawable.wrapNonNull(info.loadIcon(pm)),
                )
            }
        val lawnchairIcon = CustomAdaptiveIconDrawable.wrapNonNull(
            ContextCompat.getDrawable(app, R.drawable.ic_launcher_home)!!,
        )
        var defaultIconPack = listOf(
            IconPackInfo(
                name = app.getString(R.string.system_icons),
                packageName = "",
                icon = lawnchairIcon,
            ),
        )
        if (app.packageManager.getPackageVersionCode(LAWNICONS_PACKAGE_NAME) in 1..3) {
            val info = app.packageManager.getApplicationInfo(LAWNICONS_PACKAGE_NAME, 0)
            defaultIconPack = defaultIconPack + listOf(
                IconPackInfo(
                    name = pm.getApplicationLabel(info).toString(),
                    packageName = info.packageName,
                    icon = CustomAdaptiveIconDrawable.wrapNonNull(pm.getApplicationIcon(info)),
                ),
            )
        }
        val withSystemIcons = defaultIconPack + themedIconPacks.sortedBy { it.name }
        withSystemIcons
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())
}
