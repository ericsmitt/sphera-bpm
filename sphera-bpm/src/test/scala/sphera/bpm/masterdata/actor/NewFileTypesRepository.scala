package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.model._
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future
import scala.util.Random

trait NewFileTypesRepository { _: PersistenceSpec with TestKit =>
  def newFileTypesRepositoryActor(state: FileTypesState = FileTypesState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(FileTypesRepositoryActor.props(id, state), id.name)
  }

  def generateExtensions(min: Int = 1, max: Int = 10): Set[String] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      val id = generateId
      s"str-$id"
    }).toSet
  }

  def generateFileClassification(min: Int = 1, max: Int = 10): Map[Characteristic.Id, Characteristic.ValueId] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      (UUID.randomUUID(), UUID.randomUUID())
    }).toMap
  }

  def generateUpdateFileType(id: Characteristic.Id = generateUUID): Future[UpdateFileType] = Future {
    val i = generateId
    val random = new Random()
    UpdateFileType(
      id,
      s"p2-$i",
      Option(s"descr-$i"),
      generateExtensions(2, 5),
      random.nextBoolean(),
      generateFileClassification(2, 5),
      generateUUID)
  }
}

