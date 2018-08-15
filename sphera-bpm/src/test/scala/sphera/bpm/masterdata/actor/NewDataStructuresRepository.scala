package sphera.bpm.masterdata.actor

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.model.{ DataStructure, UpdateDataStructure }
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future

trait NewDataStructuresRepository extends NewDataStructure { _: PersistenceSpec with TestKit =>
  def newDataStructureRepositoryActor(state: DataStructuresRepositoryState = DataStructuresRepositoryState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(DataStructuresRepositoryActor.props(id, state), id.name)
  }
  def generateUpdateDataStructure(id: DataStructure.Id = generateUUID): Future[UpdateDataStructure] = Future {
    val i = generateId
    UpdateDataStructure(
      id = id,
      name = s"p2-$i",
      description = Option(s"descr-$i"),
      elements = generateDataStructureElements(2, 5),
      mutable = false,
      userId = generateUUID)
  }
}