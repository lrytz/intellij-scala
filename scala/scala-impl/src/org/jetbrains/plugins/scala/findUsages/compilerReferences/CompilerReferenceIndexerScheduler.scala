package org.jetbrains.plugins.scala.findUsages.compilerReferences

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.findUsages.compilerReferences.IndexerJob.InvalidateIndex

private class CompilerReferenceIndexerScheduler(
  project:              Project,
  expectedIndexVersion: Int
) extends IndexerScheduler {
  import CompilerReferenceIndexerScheduler._

  private[this] val indexer  = new CompilerReferenceIndexer(project, expectedIndexVersion)
  private[this] val jobQueue = new BackgroundTaskQueue(project, "Indexing classfiles ...")

  override def schedule(job: IndexerJob): Unit = synchronized {
    job match {
      case _: InvalidateIndex => jobQueue.clear()
      case _                  => ()
    }

    val task = indexer.toTask(job)
    logger.debug(s"Scheduled indexer job $job.")
    jobQueue.run(task)
  }

  def schedule(runnable: () => Unit): Unit = synchronized {
    val t = task(project)(_ => runnable())
    jobQueue.run(t)
  }

  override def scheduleAll(jobs: Seq[IndexerJob]): Unit = synchronized(jobs.foreach(schedule))
}

object CompilerReferenceIndexerScheduler {
  private val logger = Logger.getInstance(classOf[CompilerReferenceIndexerScheduler])
}
