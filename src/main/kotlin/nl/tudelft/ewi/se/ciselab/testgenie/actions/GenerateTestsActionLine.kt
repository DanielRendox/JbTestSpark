package nl.tudelft.ewi.se.ciselab.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod

class GenerateTestsActionLine : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
    }

    /**
     * Makes the action visible only if a line has been selected.
     * It also updates the action name depending on which line has been selected.
     *
     * @param e an action event that contains useful information
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        e.project ?: return

        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return
        val doc: Document = PsiDocumentManager.getInstance(e.project!!).getDocument(psiFile) ?: return
        // val line: Int? = getSurroundingLine(doc, caret)

        val lineValid = validateLine(caret.caretModel.primaryCaret.offset, psiMethod, doc)
        e.presentation.isEnabledAndVisible = lineValid
        e.presentation.text = "Generate Tests For ${getMethodDisplayName(psiMethod)}"
    }
}
