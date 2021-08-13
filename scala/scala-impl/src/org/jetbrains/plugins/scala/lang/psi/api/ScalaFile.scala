package org.jetbrains.plugins.scala
package lang
package psi
package api

import com.intellij.openapi.util.TextRange
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScPackaging
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScMember, ScTypeDefinition}

/**
 * @author ilyas
 */
trait ScalaFile extends ScalaPsiElement
  with ScFile
  with ScImportsHolder
  with ScExportsHolder {

  def firstPackaging: Option[ScPackaging]

  def typeDefinitions: Seq[ScTypeDefinition]

  def members: Seq[ScMember]

  def packagingRanges: Seq[TextRange]

  def isScriptFile: Boolean

  def isMultipleDeclarationsAllowed: Boolean

  def isWorksheetFile: Boolean

  def allowsForwardReferences: Boolean

  /**
   * Context is any kind of information required for the file analyzing, different from the file contents itself.<br>
   * It allows us to force-invalidate psi elements caches without changing file contents<br>
   * @see [[org.jetbrains.plugins.scala.caches.BlockModificationTracker.apply]]<br>
   * @see [[org.jetbrains.plugins.scala.caches.CachesUtil.fileContextModTracker]]
   */
  def getContextModificationStamp: Long
  def incContextModificationStamp(): Unit
}