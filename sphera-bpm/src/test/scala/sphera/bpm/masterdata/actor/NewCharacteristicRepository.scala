package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.model._
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future
import scala.util.Random

trait NewCharacteristicRepository extends { _: PersistenceSpec with TestKit =>
  def newCharacteristicRepositoryActor(state: CharacteristicState = CharacteristicState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(CharacteristicsRepositoryActor.props(id, state), id.name)
  }

  def generateCharacteristicElements(min: Int = 1, max: Int = 10): Map[Characteristic.Id, String] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      val id = generateId
      (UUID.randomUUID(), s"str-$id")
    }).toMap
  }

  def generateUpdateCharacteristic(id: Characteristic.Id = generateUUID): Future[UpdateCharacteristic] = Future {
    val i = generateId
    UpdateCharacteristic(
      id,
      s"p2-$i",
      Option(s"descr-$i"),
      generateCharacteristicElements(2, 5),
      generateUUID)
  }
}