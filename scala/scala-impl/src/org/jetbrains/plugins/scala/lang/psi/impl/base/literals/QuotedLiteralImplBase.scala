package org.jetbrains.plugins.scala
package lang
package psi
package impl
package base
package literals

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util
import org.jetbrains.plugins.scala.macroAnnotations.CachedInUserData

abstract class QuotedLiteralImplBase(node: ASTNode,
                                     override val toString: String)
  extends ScLiteralImplBase(node, toString) {

  protected def startQuote: String

  protected def endQuote: String = startQuote

  protected def toValue(text: String): V

  @CachedInUserData(this, util.PsiModificationTracker.MODIFICATION_COUNT)
  override final def getValue: V = getText match {
    case text if text.startsWith(startQuote) =>
      val trimLeft = startQuote.length
      val beginIndex = trimLeft

      val trimRight = if (text.endsWith(endQuote)) endQuote.length else 0
      val endIndex = text.length - trimRight

      if (trimLeft == 0 && trimRight == 0 || endIndex < beginIndex) null
      else toValue(text.substring(beginIndex, endIndex))
    case _ => null
  }

  override final def contentRange: TextRange = {
    val range = super.contentRange
    new TextRange(
      range.getStartOffset + startQuote.length,
      range.getEndOffset - endQuote.length
    )
  }
}

object QuotedLiteralImplBase {

  private[psi] val CharQuote = "\'"
  private[psi] val SingleLineQuote = "\""
  private[psi] val MultiLineQuote = "\"\"\""
}