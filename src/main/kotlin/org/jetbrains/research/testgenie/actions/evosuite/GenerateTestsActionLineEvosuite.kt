package org.jetbrains.research.testgenie.actions.evosuite

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.updateForLine
import org.jetbrains.research.testgenie.tools.Manager

class GenerateTestsActionLineEvosuite : AnAction() {
    /**
     * Creates and calls (GPT) Pipeline to generate tests for a line when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) = Manager.generateTestsForLineByEvoSuite(e)

    override fun update(e: AnActionEvent) = updateForLine(e, "EvoSuite")
}
