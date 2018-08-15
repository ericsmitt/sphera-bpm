package sphera.bpm

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import sphera.bpm.process.ProcessInfo
import sphera.bpm.project.ProjectInfo
import sphera.bpm.task.TaskInfo
import sphera.core.CoreModule
import sphera.core.akkaext.actor.ActorId
import sphera.core.modularize.SpheraHttpModule

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

object BpmModule extends SpheraHttpModule with Directives { //with Routes {
  import CoreModule.system

  val routes = pathPrefix("asd") {
    ???
  }

  implicit val infoClass1 = reflect.classTag[ProcessInfo]
  implicit val infoClass2 = reflect.classTag[TaskInfo]
  implicit val infoClass3 = reflect.classTag[ProjectInfo]

  implicit val c: ExecutionContext = CoreModule.system.dispatcher
  implicit val t: Timeout = 10 seconds
  implicit val log: LoggingAdapter = CoreModule.system.log

  val bpmId = ActorId("bpm-v5.1")
  val bpm: Bpm = Await.result(Bpm(bpmId, CoreModule.config), 3 seconds)

  override def init(): Future[Unit] = {
    FastFuture.successful {

    }
  }

  override def name: String = "sphera-bpm"
  override def buildInfo = "BuildInfo.toString"
}