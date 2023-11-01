package org.jetbrains.research.testspark.toolwindow

import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.settings.SettingsLLMConfigurable
import java.awt.Desktop
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.net.URI
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.event.HyperlinkEvent

/**
 * This class stores the main panel and the UI of the "Parameters" tool window tab.
 */
class QuickAccessParameters(private val project: Project) {

    private val panelTitle = JLabel(TestSparkLabelsBundle.defaultValue("quickAccess"))

    private val testSparkDescription = JTextPane().apply {
        isEditable = false
        contentType = "text/html"
        addHyperlinkListener { evt ->
            if (HyperlinkEvent.EventType.ACTIVATED == evt.eventType) {
                Desktop.getDesktop().browse(evt.url.toURI())
            }
        }
    }

    private val testSparkEvoSuiteDescription = JTextPane().apply {
        isEditable = false
        contentType = "text/html"
        addHyperlinkListener { evt ->
            if (HyperlinkEvent.EventType.ACTIVATED == evt.eventType) {
                Desktop.getDesktop().browse(evt.url.toURI())
            }
        }
    }

    // Link to documentation
    private val documentationLink = ActionLink(
        TestSparkLabelsBundle.defaultValue("documentationLink"),
        ActionListener { _: ActionEvent? ->
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI("https://github.com/JetBrains-Research/TestSpark"))
            }
        },
    )

    // Link to open settings
    private val settingsLink: ActionLink = ActionLink(TestSparkLabelsBundle.defaultValue("settingsLink")) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Plugin")
    }

    // Link to LLM settings

    private val LLMSettingsLink: ActionLink = ActionLink("LLM Settings"){
        ShowSettingsUtil.getInstance().showSettingsDialog(project,SettingsLLMConfigurable::class.java )
    }

    // Tool Window panel
    private var toolWindowPanel: JPanel = JPanel()

    init {
        panelTitle.font = Font("Monochrome", Font.BOLD, 20)

        // Create the main panel and set the font of the title
        toolWindowPanel = createToolWindowPanel()

        testSparkDescription.text = getDescriptionText(getContent().preferredSize.width)
        testSparkEvoSuiteDescription.text = getEvoSuiteDescriptionText(getContent().preferredSize.width)
    }



    /**
     * Creates the entire tool window panel.
     */
    private fun createToolWindowPanel() = FormBuilder.createFormBuilder()
        // Add indentations from the left border and between the lines, and add title
        .setFormLeftIndent(30)
        .addVerticalGap(25)
        .addComponent(panelTitle)
        .addComponent(testSparkDescription, 10)
        .addComponent(LLMSettingsLink, 20)
        .addComponent(testSparkEvoSuiteDescription, 10)
        .addComponent(documentationLink, 20)
        .addComponent(settingsLink, 20)
        // Add the main panel
        .addComponentFillVertically(JPanel(), 20)
        .panel

    /**
     * Gets the panel that is the main wrapper component of the tool window.
     * The panel is put into a scroll pane so that all the parameters can fit.
     *
     * @return the created tool window pane wrapped into a scroll pane
     */
    fun getContent(): JComponent {
        return JBScrollPane(toolWindowPanel)
    }


    private fun getEvoSuiteDescriptionText(width: Int): String? {
        return "<html><body style='width: ${(0.2 * width).toInt()} px;'><font face=\"Monochrome\">" +
                "<b>Search-based test generation</b>:<br>"+
                "Uses <a href=\\\"https://www.evosuite.org\\\">EvoSuite</a>. You can generate tests with this tool locally.<br>"+
                "However, it only supports projects implemented by Java versions 8 to 11.<br>"+
                "TestSpark is currently developed and maintained by <a href=\"https://lp.jetbrains.com/research/ictl/\">ICTL at JetBrains Research</a>.<br>" +
                "<br>" +
                "<strong>DISCLAIMER</strong><br><br>" +
                "TestSpark is currently designed to serve as an experimental tool.<br>" +
                "Please keep in mind that tests generated by TestSpark are meant to augment your existing test suites. " +
                "They are not meant to replace writing tests manually.</font></body></html>"
    }

    private fun getDescriptionText(width: Int): String {
        return "<html><body style='width: ${(1.2 * width).toInt()} px;'><font face=\"Monochrome\">" +
            "Welcome and thank you for using TestSpark!<br>" +
            "This plugin let you to generate tests for Java classes, method, and single lines.<br>"+
            "We are currently supporting to types of test generation:<br>"+
            "<b>LLM-based test generation</b>:<br>"+
            "Needs <a href=\"https://openai.com\">OpenAI</a> or JetBrains AI Assistant platform (currently, accessible to JetBrains employees) tokens. To use this test generation, you need to enter your token and select your model in the settings.</font></body></html>"



//            "<ul>"+
//            "<li><b>LLM-based test generation</b>: needs <a href=\"https://openai.com\">OpenAI</a> or JetBrains AI Assistant platform (currently, accessible to JetBrains employees) tokens. To use this test generation, you need to enter your token and select your model in the ${LLMSettingsLink.} <a href=>settings </a></li>"+
//            "<li>Uses <a href=\"https://www.evosuite.org\">EvoSuite</a> and <a href=\"https://openai.com\">OpenAI</a> models for unit tests generation.</li>" +
//            "<li>Generates tests for different test criteria: line coverage, branch coverage, I/O diversity, exception coverage, mutation score.</li>" +
//            "<li>Generates unit tests for capturing failures.</li>" +
//            "<li>Generate tests for Java classes, method, and single lines.</li>" +
//            "</ul><br>" +
//            "Initially implemented by <a href=\"https://www.ciselab.nl\">CISELab</a> at <a href=\"https://se.ewi.tudelft.nl\">SERG @ TU Delft</a>, " +
//            "TestSpark is currently developed and maintained by <a href=\"https://lp.jetbrains.com/research/ictl/\">ICTL at JetBrains Research</a>.<br>" +
//            "<br>" +
//            "<strong>DISCLAIMER</strong><br><br>" +
//            "TestSpark is currently designed to serve as an experimental tool.<br>" +
//            "Please keep in mind that tests generated by TestSpark are meant to augment your existing test suites. " +
//            "They are not meant to replace writing tests manually.</font></body></html>"
    }
}
