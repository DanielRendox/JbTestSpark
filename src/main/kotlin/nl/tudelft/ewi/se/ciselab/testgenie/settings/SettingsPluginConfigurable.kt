package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.openapi.options.Configurable
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService
import javax.swing.JComponent

/**
 * This class allows to configure some Plugin settings via the Plugin page in the Settings dialog, observes the changes and manages the UI and state
 * It interacts with the SettingsPluginComponent, TestGenieSettingsService and TestGenieSettingsState.
 * It provides controller functionality for the TestGenieSettingsState.
 */
class SettingsPluginConfigurable : Configurable {

    var settingsComponent: SettingsPluginComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsPluginComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        val settingsState: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        settingsComponent!!.javaPath = settingsState.javaPath
        settingsComponent!!.buildPath = settingsState.buildPath
        settingsComponent!!.colorRed = settingsState.colorRed
        settingsComponent!!.colorGreen = settingsState.colorGreen
        settingsComponent!!.colorBlue = settingsState.colorBlue
        settingsComponent!!.buildCommand = settingsState.buildCommand
        settingsComponent!!.telemetryEnabled = settingsState.telemetryEnabled
        settingsComponent!!.telemetryPath = settingsState.telemetryPath
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        val settingsState: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        var modified: Boolean = settingsComponent!!.javaPath != settingsState.javaPath
        modified = modified or (settingsComponent!!.buildPath != settingsState.buildPath)
        modified = modified or (settingsComponent!!.colorRed != settingsState.colorRed)
        modified = modified or (settingsComponent!!.colorGreen != settingsState.colorGreen)
        modified = modified or (settingsComponent!!.colorBlue != settingsState.colorBlue)
        modified = modified or (settingsComponent!!.buildCommand != settingsState.buildCommand)
        modified = modified or (settingsComponent!!.telemetryEnabled != settingsState.telemetryEnabled)
        modified = modified or (settingsComponent!!.telemetryPath != settingsState.telemetryPath)
        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        val settingsState: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        settingsState.javaPath = settingsComponent!!.javaPath
        settingsState.colorRed = settingsComponent!!.colorRed
        settingsState.colorGreen = settingsComponent!!.colorGreen
        settingsState.colorBlue = settingsComponent!!.colorBlue
        settingsState.buildPath = settingsComponent!!.buildPath
        settingsState.buildCommand = settingsComponent!!.buildCommand
        settingsState.telemetryEnabled = settingsComponent!!.telemetryEnabled
        if (checkEmptyTelemetryPath(settingsState)) return
        settingsState.telemetryPath = settingsComponent!!.telemetryPath
    }

    /**
     * Check if the telemetry path is empty when telemetry is enabled.
     * If empty, then sets to previous state. Else, keep the new one.
     */
    private fun checkEmptyTelemetryPath(settingsState: TestGenieSettingsState): Boolean {
        if (settingsComponent!!.telemetryEnabled && settingsComponent!!.telemetryPath.isEmpty()) {
            settingsState.telemetryPath = settingsState.telemetryPath
            settingsComponent!!.telemetryPath = settingsState.telemetryPath
            return true
        }
        return false
    }

    /**
     * Returns the displayed name of the Settings tab.
     *
     * @return the name displayed in the menu (settings)
     */
    override fun getDisplayName(): String {
        return "TestGenie"
    }

    /**
     * Returns the UI component that should be focused when the TestGenie Settings page is opened.
     *
     *  @return preferred UI component
     */
    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.getPreferredFocusedComponent()
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
