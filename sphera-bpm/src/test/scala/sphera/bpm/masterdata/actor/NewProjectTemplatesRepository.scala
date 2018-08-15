package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.model._
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future

trait NewProjectTemplatesRepository { _: PersistenceSpec with TestKit =>
  def newProjectTemplateRepositoryActor(state: ProjectTemplatesState = ProjectTemplatesState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(ProjectTemplatesRepositoryActor.props(id, state), id.name)
  }

  def generateUpdateProjectTemplate(x: ProjectTemplate.Id = generateUUID): Future[UpdateProjectTemplate] = Future {
    val i = generateId
    UpdateProjectTemplate(
      x,
      s"p2-$i",
      Option(s"desc-$i"),
      UUID.randomUUID(),
      generateUUID)
  }
}