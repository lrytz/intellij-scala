package org.jetbrains.plugins.scala.dfa
package cfg
package impl

import org.jetbrains.plugins.scala.dfa.cfg.Builder.{Property, Variable}
import org.jetbrains.plugins.scala.dfa.utils.BuilderWithSize

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

private[cfg] class BuilderImpl[Info] extends Builder[Info] {
  override type Value = cfg.Value
  override type UnlinkedJump = UnlinkedJumpImpl
  override type LoopLabel = LoopLabelImpl

  private type NodeImpl = impl.NodeImpl[Info] with Node
  private type JumpingImpl = impl.JumpingImpl[Info] with Jumping
  private type Block = impl.BlockImpl[Info]

  private var nextValueId = 0
  private var curSourceInfo = Option.empty[Info]
  private val nodesBuilder = BuilderWithSize.newBuilder[NodeImpl](ArraySeq)
  private val blocksBuilder = BuilderWithSize.newBuilder[Block](ArraySeq)

  private def newValueId(): Int = {
    val next = nextValueId
    nextValueId += 1
    next
  }

  private def addNode(node: NodeImpl): node.type = {
    node._index = nodesBuilder.elementsAdded
    node._sourceInfo = curSourceInfo
    node._block = currentBlock

    node match {
      case value: ValueImpl[Info] =>
        value._valueId = newValueId()
      case _ =>
    }

    nodesBuilder += node
    node
  }

  private def startBlock(): Block = {
    assert(_currentBlock.isEmpty)
    val block = new Block(blockIndex = blocksBuilder.elementsAdded, nodeBegin = nodesBuilder.elementsAdded)
    blocksBuilder += block
    _currentBlock = Some(block)
    block
  }

  private def endBlock(): Unit = {
    val Some(cur) = _currentBlock.ensuring(_.nonEmpty)
    cur._endIndex = nodesBuilder.elementsAdded
    _currentBlock = None
  }

  private var _currentBlock = Option.empty[Block]
  private def currentBlock: Block = _currentBlock match {
    case Some(block) => block
    case None => startBlock()
  }

  override def constant(const: DfAny): Value =
    addNode(new ConstantImpl(const))

  override def readVariable(variable: Variable): Unit = ???
  override def writeVariable(variable: Variable, value: Value): Unit = ???

  override def readProperty(base: Value, property: Property): Value = ???
  override def writeProperty(base: Value, property: Property, value: Value): Unit = ???

  /***** Forward jumps ****/
  private val unlinkedJumps = mutable.Set.empty[UnlinkedJump]
  private def addForwardJump(jump: JumpingImpl): UnlinkedJump = {
    val unlinkedJump = new UnlinkedJumpImpl(addNode(jump))
    endBlock()
    unlinkedJumps += unlinkedJump
    unlinkedJump
  }

  override def jumpToFuture(): UnlinkedJump = addForwardJump(new JumpImpl)
  override def jumpToFutureIfNot(cond: Value): UnlinkedJump = addForwardJump(new JumpIfNotImpl(cond))
  override def jumpHere(labels: Seq[UnlinkedJump]): Unit = {
    endBlock()
    val targetIndex = nodesBuilder.elementsAdded
    labels.foreach(_.finish(targetIndex))
  }

  class UnlinkedJumpImpl(private[BuilderImpl] val jumping: JumpingImpl) {
    def finish(targetIndex: Int): Unit = {
      assert(unlinkedJumps contains this)
      unlinkedJumps -= this
      jumping._targetIndex = targetIndex
    }
  }

  /***** Backward jumps *****/
  override def loopJumpHere(): LoopLabelImpl = ???
  override def jumpBack(loop: LoopLabelImpl): Unit = ???

  class LoopLabelImpl {

  }

  /***** Create Graph *****/
  override def finish(): Graph[Info] = {
    addNode(new EndImpl)

    val graph = new Graph[Info](nodesBuilder.result(), blocksBuilder.result())

    assert(unlinkedJumps.isEmpty, "Unlinked labels: " + unlinkedJumps.iterator.map(_.jumping.index).mkString(", "))

    graph
  }
}