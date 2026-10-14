package app.lawnchair.allapps

import android.app.AlertDialog
import android.os.Process
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import app.lawnchair.data.folder.service.FolderService
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.firstCached
import app.lawnchair.preferences2.ReloadHelper
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.folder.Folder
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import kotlinx.coroutines.launch

/** Adds shortcuts to drawer folders without changing automatic classification. */
object FolderQuickAdd {
    private const val KEY_PREFIX = "core_category_additions:"

    fun withAdditions(
        categories: Map<String, List<AppInfo>>,
        apps: List<AppInfo>,
        context: android.content.Context,
    ): Map<String, List<AppInfo>> {
        val sp = PreferenceManager.getInstance(context).sp
        return categories.mapValues { (title, members) ->
            val saved = sp.getStringSet(KEY_PREFIX + title, emptySet()).orEmpty()
            (members + apps.filter { it.toComponentKey().toString() in saved })
                .distinctBy { it.toComponentKey().toString() }
        }
    }

    @JvmStatic
    fun show(folder: Folder) {
        val launcher = Launcher.getLauncher(folder.context)
        val context = folder.context
        val info = folder.info
        val title = info.title?.toString().orEmpty()
        val existing = info.getContents().mapNotNull { item ->
            item.targetComponent?.let { ComponentKey(it, item.user).toString() }
        }.toSet()
        val hidden = PreferenceManager2.getInstance(context).hiddenApps.firstCached()
        val apps = launcher.appsView.appsStore.apps.filter {
            it.user == Process.myUserHandle() &&
                it.toComponentKey().toString() !in existing &&
                it.toComponentKey().toString() !in hidden
        }.sortedBy { it.title.toString().lowercase() }
        if (apps.isEmpty()) {
            Toast.makeText(context, R.string.core_folder_no_apps, Toast.LENGTH_SHORT).show()
            return
        }
        val selected = linkedSetOf<String>()
        var visibleApps = apps
        val search = EditText(context).apply {
            setHint(R.string.core_folder_search_apps)
            isSingleLine = true
        }
        val list = ListView(context).apply { choiceMode = ListView.CHOICE_MODE_MULTIPLE }
        fun refresh(query: String) {
            visibleApps = apps.filter { it.title.toString().contains(query, ignoreCase = true) }
            list.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice,
                visibleApps.map { it.title.toString() })
            visibleApps.forEachIndexed { index, app ->
                list.setItemChecked(index, app.toComponentKey().toString() in selected)
            }
        }
        list.setOnItemClickListener { _, _, position, _ ->
            val key = visibleApps[position].toComponentKey().toString()
            if (list.isItemChecked(position)) selected.add(key) else selected.remove(key)
        }
        search.doAfterTextChanged { refresh(it?.toString().orEmpty()) }
        refresh("")
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, 0, padding, 0)
            addView(search)
            addView(list, LinearLayout.LayoutParams(-1,
                (320 * resources.displayMetrics.density).toInt()))
        }
        val dialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.core_folder_add_to, title))
            .setView(body)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.core_folder_add_apps, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (selected.isEmpty()) return@setOnClickListener
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                launcher.lifecycleScope.launch {
                    try {
                        if (info.id == com.android.launcher3.model.data.ItemInfo.NO_ID) {
                            val sp = PreferenceManager.getInstance(context).sp
                            val key = KEY_PREFIX + title
                            val additions = sp.getStringSet(key, emptySet()).orEmpty() + selected
                            sp.edit().putStringSet(key, additions).apply()
                        } else {
                            FolderService.INSTANCE.get(context).updateFolderWithItems(
                                info.id, title, (existing + selected).toList(),
                            )
                        }
                        dialog.dismiss()
                        folder.close(false)
                        ReloadHelper(context).reloadGrid()
                    } catch (e: Exception) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        Toast.makeText(context, R.string.core_folder_add_failed, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        dialog.show()
    }
}
