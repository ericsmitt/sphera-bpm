package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.model._
import sphera.core.domain.tenancy.model.User
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future
import scala.util.Random

trait NewRoleTypesRepository { _: PersistenceSpec with TestKit =>
  def newRoleTypesRepositoryActor(state: RoleTypesState = RoleTypesState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(RoleTypesRepositoryActor.props(id, state), id.name)
  }

  def generateRoleTypeUser(min: Int = 1, max: Int = 10): Set[User.Id] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      UUID.randomUUID()
    }).toSet
  }

  def generateRoleTypeClassification(min: Int = 1, max: Int = 10): Map[Characteristic.Id, Characteristic.ValueId] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      (UUID.randomUUID(), UUID.randomUUID())
    }).toMap
  }

  def generateUpdateRoleType(id: Characteristic.Id = generateUUID): Future[UpdateRoleType] = Future {
    val i = generateId
    UpdateRoleType(
      id,
      s"p2-$i",
      Option(s"descr-$i"),
      generateRoleTypeUser(2, 5),
      generateRoleTypeClassification(2, 5),
      generateUUID)
  }
}