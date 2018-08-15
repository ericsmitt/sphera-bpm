package sphera.bpm.masterdata.actor

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.Bpm
import sphera.bpm.Implicits._
import sphera.bpm.masterdata.model._
import sphera.bpm.process.{ EndOperationTemplateData, OperationTemplateData, StartOperationTemplateData }
import sphera.core.akkaext.actor.ActorId
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future
import scala.io.Source
import scala.util.Random

trait NewProcessTemplatesRepository { _: PersistenceSpec with TestKit =>
  private def generateBpmId = ActorId(s"bpm$generateInt")

  private def newBpm(x: Bpm.Id = generateBpmId): Future[Bpm] = for {
    bpm <- Bpm(x)
    // data <- fillMasterData(bpm.masterDataManager)
  } yield bpm

  def newProcessTemplatesRepositoryActor(state: ProcessTemplatesRepositoryState = ProcessTemplatesRepositoryState()): Future[ActorRef] = {
    newBpm() map { bpm =>
      val id = generateActorId
      system.actorOf(ProcessTemplatesRepositoryActor.props(id, bpm, state), id.name)
    }
  }

  //  def generateRoleDefinitions(min: Int = 1, max: Int = 10): Map[String, RoleDef] = {
  //    val random = new Random()
  //    val n: Int = random.nextInt(max + 1 - min) + min
  //    (for (i <- 1 to n) yield {
  //      val id = generateUUID
  //      val i = generateId
  //      i -> RoleDef(
  //        s"role-$i",
  //        Option(s"descr-$i"),
  //        id,
  //        Option(id))
  //    }).toMap
  //  }
  def generateClassification(min: Int = 1, max: Int = 10): Map[Characteristic.Id, Characteristic.ValueId] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield generateUUID -> generateUUID).toMap
  }

  def generateUpdateProcessTemplate(
    id: ProcessTemplate.Id = generateUUID,
    opData: Set[OperationTemplateData] = Set(
      StartOperationTemplateData(
        "Start",
        List.empty),
      EndOperationTemplateData(
        "End",
        List.empty)),
    dataStructureId: DataStructure.Id = generateUUID,
    sourceCode: String = Source.fromURL(getClass.getResource("/bpmn/StartEnd.bpmn")).mkString): Future[UpdateProcessTemplate] = Future {
    val i = generateId
    UpdateProcessTemplate(
      id,
      generateString(),
      s"template$i",
      Option(s"desc$i"),
      sourceCode,
      Set.empty,
      opData,
      dataStructureId,
      generateClassification(),
      generateUUID)
  }
}