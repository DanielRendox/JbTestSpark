package org.jetbrains.research.testspark.helpers

import com.google.gson.JsonParser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.util.io.HttpRequests
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.data.LLMPlatform
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.tools.llm.generation.Info
import java.net.HttpURLConnection
import javax.swing.DefaultComboBoxModel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Checks if the Grazie class is loaded.
 * @return true if the Grazie class is loaded, false otherwise.
 */
private fun isGrazieClassLoaded(): Boolean {
    val className = "org.jetbrains.research.grazie.Request"
    return try {
        Class.forName(className)
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

private fun loadGrazieInfo(): Info? {
    val className = "org.jetbrains.research.grazie.Info"
    return try {
        Class.forName(className).getDeclaredConstructor().newInstance() as Info
    } catch (e: ClassNotFoundException) {
        null
    }
}

/**
 * Updates the model selector based on the selected platform in the platform selector.
 * If the selected platform is "Grazie", the model selector is disabled and set to display only "GPT-4".
 * If the selected platform is not "Grazie", the model selector is updated with the available modules fetched asynchronously using llmUserTokenField and enables the okLlmButton.
 * If the modules fetch fails, the model selector is set to display the default modules and is disabled.
 *
 * This method runs on a separate thread using ApplicationManager.getApplication().executeOnPooledThread{}.
 */
private fun updateModelSelector(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
) {
    val settingsState = SettingsApplicationService.getInstance().state!!

    // TODO create more common implementation
    if (platformSelector.selectedItem!!.toString() == TestSparkDefaultsBundle.defaultValue("grazie")) {
        val modules = loadGrazieInfo()?.availableProfiles()?.toTypedArray() ?: arrayOf("")
        if (modules != null) {
            modelSelector.model = DefaultComboBoxModel(modules)
            for (llmPlatform in settingsState.llmPlatforms) {
                if (modules.contains(llmPlatform.model) && platformSelector.selectedItem!!.toString() == llmPlatform.name) modelSelector.selectedItem = llmPlatform.model
            }
            modelSelector.isEnabled = true
        } else {
            modelSelector.model = DefaultComboBoxModel(arrayOf(""))
            modelSelector.isEnabled = false
        }
    }
    if (platformSelector.selectedItem!!.toString() == TestSparkDefaultsBundle.defaultValue("openAI")) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val modules = getOpenAIModules(llmUserTokenField.text)
            if (modules != null) {
                modelSelector.model = DefaultComboBoxModel(modules)
                for (llmPlatform in settingsState.llmPlatforms) {
                    if (modules.contains(llmPlatform.model) && platformSelector.selectedItem!!.toString() == llmPlatform.name) modelSelector.selectedItem = llmPlatform.model
                }
                modelSelector.isEnabled = true
            } else {
                modelSelector.model = DefaultComboBoxModel(arrayOf(""))
                modelSelector.isEnabled = false
            }
        }
    }
}

/**
 * Updates LlmUserTokenField based on the selected platform in the platformSelector ComboBox.
 *
 * @param platformSelector The ComboBox that allows the user to select a platform.
 * @param llmUserTokenField The JTextField that displays the user token for the selected platform.
 */
private fun updateLlmUserTokenField(platformSelector: ComboBox<String>, llmUserTokenField: JTextField) {
    val settingsState = SettingsApplicationService.getInstance().state!!
    for (llmPlatform in settingsState.llmPlatforms) {
        if (platformSelector.selectedItem!!.toString() == llmPlatform.name) {
            llmUserTokenField.text = llmPlatform.token
        }
    }
}

/**
 * Retrieves all available models from the OpenAI API using the provided token.
 *
 * @param token Authorization token for the OpenAI API.
 * @return An array of model names if request is successful, otherwise null.
 */
private fun getOpenAIModules(token: String): Array<String>? {
    val url = "https://api.openai.com/v1/models"

    val httpRequest = HttpRequests.request(url).tuner {
        it.setRequestProperty("Authorization", "Bearer $token")
    }

    val models = mutableListOf<String>()

    try {
        httpRequest.connect {
            if ((it.connection as HttpURLConnection).responseCode == HttpURLConnection.HTTP_OK) {
                val jsonObject = JsonParser.parseString(it.readString()).asJsonObject
                val dataArray = jsonObject.getAsJsonArray("data")
                for (dataObject in dataArray) {
                    val id = dataObject.asJsonObject.getAsJsonPrimitive("id").asString
                    models.add(id)
                }
            }
        }
    } catch (e: HttpRequests.HttpStatusException) {
        return null
    }

    val gptComparator = Comparator<String> { s1, s2 ->
        when {
            s1.contains("gpt") && s2.contains("gpt") -> s2.compareTo(s1)
            s1.contains("gpt") -> -1
            s2.contains("gpt") -> 1
            else -> s1.compareTo(s2)
        }
    }

    if (models.isNotEmpty()) {
        return models.sortedWith(gptComparator).toTypedArray().filter { !it.contains("vision") }
            .toTypedArray()
    }

    return null
}

/**
 * Adds listeners to the given components to handle events and perform necessary actions.
 *
 * @param platformSelector The combo box used for selecting platforms.
 * @param modelSelector The combo box used for selecting models.
 * @param llmUserTokenField The text field used for entering the user token.
 * @param defaultModulesArray An array of default module names.
 */
fun addLLMPanelListeners(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
    llmPlatforms: List<LLMPlatform>,
) {
    llmUserTokenField.document.addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            updateToken()
        }

        override fun removeUpdate(e: DocumentEvent?) {
            updateToken()
        }

        override fun changedUpdate(e: DocumentEvent?) {
            updateToken()
        }

        private fun updateToken() {
            for (llmPlatform in llmPlatforms) {
                if (platformSelector.selectedItem!!.toString() == llmPlatform.name) {
                    llmPlatform.token = llmUserTokenField.text
                }
            }

            updateModelSelector(
                platformSelector,
                modelSelector,
                llmUserTokenField,
            )
        }
    })

    platformSelector.addItemListener {
        updateLlmUserTokenField(platformSelector, llmUserTokenField)

        updateModelSelector(
            platformSelector,
            modelSelector,
            llmUserTokenField,
        )
    }

    modelSelector.addItemListener {
        for (llmPlatform in llmPlatforms) {
            if (platformSelector.selectedItem!!.toString() == llmPlatform.name) {
                llmPlatform.model = modelSelector.item
            }
        }
    }
}

/**
 * Stylizes the main components of the application.
 *
 * @param llmUserTokenField the text field for the LLM user token
 * @param modelSelector the combo box for selecting the model
 * @param platformSelector the combo box for selecting the platform
 */
fun stylizeMainComponents(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
) {
    val settingsState = SettingsApplicationService.getInstance().state!!

    // Check if the Grazie platform access is available in the current build
    if (isGrazieClassLoaded()) {
        platformSelector.model = DefaultComboBoxModel(
            arrayOf(
                TestSparkDefaultsBundle.defaultValue("grazie"),
                TestSparkDefaultsBundle.defaultValue("openAI"),
            ),
        )
        platformSelector.selectedItem = settingsState.currentLLMPlatformName
    } else {
        platformSelector.isEnabled = false
    }

    llmUserTokenField.toolTipText = TestSparkToolTipsBundle.defaultValue("llmToken")
    updateLlmUserTokenField(platformSelector, llmUserTokenField)

    modelSelector.toolTipText = TestSparkToolTipsBundle.defaultValue("model")
    modelSelector.isEnabled = false
    updateModelSelector(
        platformSelector,
        modelSelector,
        llmUserTokenField,
    )
}

/**
 * Retrieves the list of LLMPlatforms.
 *
 * @return The list of LLMPlatforms.
 */
fun getLLLMPlatforms(): List<LLMPlatform> {
    return listOf(
        LLMPlatform(
            TestSparkDefaultsBundle.defaultValue("openAI"),
            TestSparkDefaultsBundle.defaultValue("openAIToken"),
            TestSparkDefaultsBundle.defaultValue("openAIModel"),
        ),
        LLMPlatform(
            TestSparkDefaultsBundle.defaultValue("grazie"),
            TestSparkDefaultsBundle.defaultValue("grazieToken"),
            TestSparkDefaultsBundle.defaultValue("grazieModel"),
        ),
    )
}
