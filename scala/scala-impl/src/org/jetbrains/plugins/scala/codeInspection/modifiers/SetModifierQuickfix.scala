package org.jetbrains.plugins.scala
package codeInspection
package modifiers

import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.annotations.Nls
import org.jetbrains.plugins.scala.codeInspection.modifiers.SetModifierQuickfix.makeName
import org.jetbrains.plugins.scala.lang.lexer.ScalaModifier
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScModifierListOwner

class SetModifierQuickfix(_modifierListOwner: ScModifierListOwner, modifierText: ScalaModifier, set: Boolean)
  extends AbstractFixOnPsiElement[ScModifierListOwner](makeName(modifierText, set), _modifierListOwner)
{
  override protected def doApplyFix(modifierListOwner: ScModifierListOwner)(implicit project: Project): Unit = {
    val ml = modifierListOwner.getModifierList
    ml.setModifierProperty(modifierText.text(), set)

    if (!set) {
      val textRange = ml.getTextRange
      CodeStyleManager.getInstance(project).reformatText(
        modifierListOwner.getContainingFile,
        textRange.getStartOffset,
        textRange.getEndOffset + 1
      )
    }
  }
}

object SetModifierQuickfix {
  @Nls
  private def makeName(modifier: ScalaModifier, set: Boolean): String = {
    val modifierText = modifier.text()
    if (set) ScalaInspectionBundle.message("add.modifier", modifierText)
    else ScalaInspectionBundle.message("remove.modifier", modifierText)
  }
}
