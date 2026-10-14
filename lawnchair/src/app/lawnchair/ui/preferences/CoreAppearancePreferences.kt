package app.lawnchair.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.observeAsState
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.SliderPreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.ExpandAndShrink
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceTemplate
import com.android.launcher3.R

/** TokenX-inspired appearance editor; all changes are scoped to CORE settings. */
@Composable
fun CoreAppearancePreferences() {
    val prefs = preferenceManager()
    val miuix by prefs.coreSettingsMiuix.observeAsState()
    val glass by prefs.coreSettingsGlass.observeAsState()
    PreferenceGroup(heading = stringResource(R.string.core_settings_appearance)) {
        SwitchPreference(
            adapter = prefs.coreSettingsMiuix.getAdapter(),
            label = stringResource(R.string.core_settings_miuix),
            description = stringResource(R.string.core_settings_miuix_help),
        )
        ListPreference(
            value = "",
            onValueChange = { applyCoreAppearancePreset(prefs, it) },
            label = stringResource(R.string.core_settings_presets),
            description = stringResource(R.string.core_settings_presets_help),
            entries = listOf(
                ListPreferenceEntry("miuix") { stringResource(R.string.core_preset_miuix) },
                ListPreferenceEntry("crystal") { stringResource(R.string.core_preset_crystal) },
                ListPreferenceEntry("smoke") { stringResource(R.string.core_preset_smoke) },
                ListPreferenceEntry("amoled") { stringResource(R.string.core_preset_amoled) },
                ListPreferenceEntry("material") { stringResource(R.string.core_preset_material) },
            ),
        )
        SwitchPreference(
            adapter = prefs.coreSettingsGlass.getAdapter(),
            label = stringResource(R.string.core_settings_glass),
            description = stringResource(R.string.core_settings_glass_help),
        )
        ExpandAndShrink(visible = glass) {
            SliderPreference(
                adapter = prefs.coreSettingsOpacity.getAdapter(),
                label = stringResource(R.string.core_settings_opacity),
                valueRange = 0.4f..1f, step = 0.05f, showAsPercentage = true,
            )
        }
    }
    ExpandAndShrink(visible = miuix) {
        Column {
            PreferenceGroup(heading = stringResource(R.string.core_settings_personalize)) {
                ListPreference(
                    adapter = prefs.coreSettingsAccent.getAdapter(),
                    label = stringResource(R.string.core_settings_accent),
                    entries = listOf(
                        ListPreferenceEntry("system") { stringResource(R.string.core_accent_system) },
                        ListPreferenceEntry("blue") { stringResource(R.string.core_accent_blue) },
                        ListPreferenceEntry("purple") { stringResource(R.string.core_accent_purple) },
                        ListPreferenceEntry("cyan") { stringResource(R.string.core_accent_cyan) },
                        ListPreferenceEntry("emerald") { stringResource(R.string.core_accent_emerald) },
                        ListPreferenceEntry("wine") { stringResource(R.string.core_accent_wine) },
                        ListPreferenceEntry("orange") { stringResource(R.string.core_accent_orange) },
                        ListPreferenceEntry("graphite") { stringResource(R.string.core_accent_graphite) },
                    ),
                )
                ListPreference(
                    adapter = prefs.coreSettingsBackground.getAdapter(),
                    label = stringResource(R.string.core_settings_background),
                    entries = listOf(
                        ListPreferenceEntry("neutral") { stringResource(R.string.core_background_neutral) },
                        ListPreferenceEntry("gradient") { stringResource(R.string.core_background_gradient) },
                        ListPreferenceEntry("amoled") { stringResource(R.string.core_background_amoled) },
                    ),
                )
                SliderPreference(
                    adapter = prefs.coreSettingsRadius.getAdapter(),
                    label = stringResource(R.string.core_settings_radius),
                    valueRange = 12f..36f, step = 2f, showUnit = "dp",
                )
                SliderPreference(
                    adapter = prefs.coreSettingsBorder.getAdapter(),
                    label = stringResource(R.string.core_settings_border),
                    valueRange = 0f..0.3f, step = 0.02f, showAsPercentage = true,
                )
                ExpandAndShrink(visible = glass) {
                    SliderPreference(
                        adapter = prefs.coreSettingsTint.getAdapter(),
                        label = stringResource(R.string.core_settings_tint),
                        valueRange = 0f..0.3f, step = 0.02f, showAsPercentage = true,
                    )
                }
                SwitchPreference(
                    adapter = prefs.coreSettingsCompactHeader.getAdapter(),
                    label = stringResource(R.string.core_settings_compact_header),
                    description = stringResource(R.string.core_settings_compact_header_help),
                )
            }
            PreferenceGroup(
                heading = stringResource(R.string.core_settings_preview),
                description = stringResource(R.string.core_settings_preview_help),
            ) {
                PreferenceTemplate(
                    title = { Text(stringResource(R.string.core_settings_preview_title)) },
                    description = { Text(stringResource(R.string.core_settings_preview_description)) },
                    endWidget = { Switch(checked = true, onCheckedChange = null) },
                )
                PreferenceTemplate(
                    title = { Text(stringResource(R.string.core_settings_preview_accent), color = MaterialTheme.colorScheme.primary) },
                )
            }
        }
    }
}

private fun applyCoreAppearancePreset(prefs: PreferenceManager, preset: String) {
    // Presets use the same observable preferences as the individual controls.
    prefs.batchEdit {
        prefs.coreSettingsMiuix.set(preset != "material")
        prefs.coreSettingsGlass.set(preset == "crystal" || preset == "smoke")
        prefs.coreSettingsOpacity.set(when (preset) { "crystal" -> 0.45f; "smoke" -> 0.65f; else -> 0.75f })
        prefs.coreSettingsAccent.set(when (preset) { "smoke" -> "graphite"; "material" -> "system"; else -> "blue" })
        prefs.coreSettingsBackground.set(when (preset) { "amoled" -> "amoled"; "crystal", "smoke" -> "gradient"; else -> "neutral" })
        prefs.coreSettingsRadius.set(if (preset == "crystal") 32f else 28f)
        prefs.coreSettingsBorder.set(when (preset) { "crystal" -> 0.22f; "smoke" -> 0.18f; "amoled" -> 0.08f; else -> 0.12f })
        prefs.coreSettingsTint.set(if (preset == "smoke") 0.12f else 0.08f)
        prefs.coreSettingsCompactHeader.set(false)
    }
}
