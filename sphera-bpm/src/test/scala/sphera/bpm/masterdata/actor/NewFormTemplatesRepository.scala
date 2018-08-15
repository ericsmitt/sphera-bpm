package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.Implicits._
import sphera.bpm.masterdata.model.{ ModifyAttr, _ }
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future

trait NewFormTemplatesRepository { _: PersistenceSpec with TestKit =>
  val formTemplateA = FormTemplate(
    id = generateUUID,
    name = "formTemplateA",
    description = None,
    dataStructureId = generateUUID,
    formStructure = generateFormStructure,
    modifyAttr = ModifyAttr(
      createdBy = generateUUID,
      createdOn = ZonedDateTime.now()))

  def newFormTemplatesRepositoryActor(state: FormTemplatesRepositoryState = FormTemplatesRepositoryState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(FormTemplatesRepositoryActor.props(id, state), id.name)
  }

  def generateTuple: (String, String) = "en" -> s"str$generateId"

  def generateFormStructure: FormStructure = {
    val leftPanel = Option(Sidebar(fixed = false, List.empty))
    val rightPanel = Option(Sidebar(fixed = false, List.empty))
    FormStructure(
      generateTuple,
      generateTuple,
      MainPanel(
        generateTuple,
        generateTuple,
        List(
          Section(
            generateTuple,
            generateTuple,
            showCaption = false,
            transitionAttr = false,
            List(ColumnsLayout(
              List(
                Column(
                  100.0,
                  List(
                    SaveButton(generateTuple, generateTuple))))))))),
      leftPanel,
      rightPanel)
  }

  def generateUpdateFormTemplate(id: FormTemplate.Id = generateUUID, dataStructureId: DataStructure.Id = generateUUID): Future[UpdateFormTemplate] = Future {
    val i = generateId
    UpdateFormTemplate(
      id,
      s"form-template-$i",
      Option(s"descr-$i"),
      dataStructureId,
      generateFormStructure,
      generateUUID)
  }
}