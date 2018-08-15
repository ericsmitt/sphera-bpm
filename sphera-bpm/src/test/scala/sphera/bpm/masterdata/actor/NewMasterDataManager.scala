package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestKit
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.Implicits._
import sphera.bpm.lang.{ Assignments, Compiler }
import sphera.bpm.masterdata.MasterDataManager
import sphera.bpm.masterdata.model.{ DataStructure, FormTemplate, ProcessTemplate }
import sphera.bpm.process._
import sphera.core.akkaext.actor._
import sphera.core.test.PersistenceSpec
import io.circe.Json

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.FiniteDuration
import scala.io.Source
import sphera.bpm.json._
import sphera.bpm.masterdata.model.DataStructure.Id

trait NewMasterDataManager extends NewProcessTemplatesRepository
  with NewDataStructuresRepository
  with NewFormTemplatesRepository { _: PersistenceSpec with TestKit =>

  val userIdA: UUID = generateUUID
  val userIdB: UUID = generateUUID

  case class MasterData(
    masterDataManager: MasterDataManager,
    processTemplates: Seq[ProcessTemplate],
    dataStructures: Seq[DataStructure],
    formTemplates: Seq[FormTemplate])

  def generateBpmId: ActorId = ActorId(s"bpm$generateInt")

  def newBpm(x: Bpm.Id = generateBpmId): Future[Bpm] = for {
    bpm <- Bpm(x)
    data <- fillMasterData(bpm.masterDataManager)
  } yield bpm

  def newMasterDataManagerActor: Future[ActorRef] = {
    newBpm() map { bpm =>
      val id = generateActorId
      system.actorOf(MasterDataManagerActor.props(id, bpm), id.name)
    }
  }

  def newMasterDataManager: Future[MasterDataManager] = {
    newBpm() map { bpm =>
      val id = generateActorId
      MasterDataManager(system.actorOf(MasterDataManagerActor.props(id, bpm), id.name))
    }
  }

  /**
   * Process templates:
   */
  object ProcessTemplates {
    object StartEnd {
      val id = UUID.fromString("73d55eaa-cb49-4bc0-ae1c-beda477574c1")
      val opData: Set[OperationTemplateData] = Set(
        StartOperationTemplateData("Start"),
        EndOperationTemplateData("End"))

      def sourceCode = Future {
        Source.fromURL(getClass.getResource("/bpmn/StartEnd.bpmn")).mkString
      }
    }

    object ResidentRegistration {
      val id = UUID.fromString("ed7c46dc-9c81-46cd-aef5-ec21cc5f8450")

      val opData: Set[OperationTemplateData] = Set(
        StartOperationTemplateData("Start"),
        EndOperationTemplateData("End"),
        UserTaskOperationTemplateData(
          bpmnId = "EnterData",
          role = "candidate",
          assigneeId = None,
          reviewerId = None,
          watcherIds = Set.empty,
          formTemplateId = formTemplateIdA,
          dataStructureId = formDataStructureIdB,
          plannedStart = None,
          plannedDuration = None,
          preAssignments = Compiler.compileAssignments(
            """
                |$task.company = company,
                |$task.title = title,
                |$task.employees = employees,
                |$task.phone = phone,
                |$task.email = email"
              """.stripMargin).right.get,
          postAssignments = Compiler.compileAssignments(
            """
                |company = $task.company,
                |title = $task.title,
                |employees = $task.employees,
                |phone = $task.phone,
                |email = $task.email
              """.stripMargin).right.get),
        ExclusiveGatewayOperationTemplateData(
          bpmnId = "Switch",
          sequenceflowExpr = Map(
            "AcceptFlow" -> Compiler.compileExpression("approved == true").right.get)),
        ParallelGatewayOperationTemplateData("Fork"),
        SendTaskOperationTemplateData(
          bpmnId = "SendAck",
          recipients = Compiler.compileExpressions("email").right.get,
          subject = Compiler.compileExpression("subject1").right.get,
          message = Compiler.compileExpression("msg1").right.get,
          preAssignments = List.empty,
          postAssignments = List.empty),
        UserTaskOperationTemplateData(
          bpmnId = "Validation",
          role = "checkingUser",
          assigneeId = None,
          reviewerId = None,
          watcherIds = Set.empty,
          formTemplateId = formTemplateIdC,
          dataStructureId = formDataStructureIdC,
          plannedStart = None,
          plannedDuration = None,
          preAssignments = Compiler.compileAssignments(
            """
                |$task.approved = approved,
                |$task.rejectReason = rejectReason,
                |$task.filledForm.company = company,
                |$task.filledForm.title = title,
                |$task.filledForm.employees = employees,
                |$task.filledForm.phone = phone,
                |$task.filledForm.email = email
              """.stripMargin).right.get,
          postAssignments = Compiler.compileAssignments(
            """
                |approved = $task.approved,
                |rejectReason = $task.rejectReason
              """.stripMargin).right.get),
        SendTaskOperationTemplateData(
          bpmnId = "SendAccept",
          recipients = Compiler.compileExpressions("email").right.get,
          subject = Compiler.compileExpression("subject2").right.get,
          message = Compiler.compileExpression("msg2").right.get,
          preAssignments = List.empty,
          postAssignments = List.empty),
        SendTaskOperationTemplateData(
          bpmnId = "SendReject",
          recipients = Compiler.compileExpressions("email").right.get,
          subject = Compiler.compileExpression("subject2").right.get,
          message = Compiler.compileExpression("msg2").right.get,
          preAssignments = List.empty,
          postAssignments = List.empty),
        EndOperationTemplateData("End"),
        ParallelGatewayOperationTemplateData("WaitForOperation"),
        ScriptTaskOperationTemplateData(
          bpmnId = "AcceptPreparation",
          preAssignments = Compiler.compileAssignments(
            """msg2 = "Заявка на регистрацию организации" + title.ru + "принята."""".stripMargin).right.get,
          postAssignments = List.empty),
        ScriptTaskOperationTemplateData(
          bpmnId = "RejectPreparation",
          preAssignments = Compiler.compileAssignments(
            """msg2 = "Заявка на регистрацию организации" + title.ru + "отклонена. Причина отказа: " + rejectReason""".stripMargin).right.get,
          postAssignments = List.empty),
        ScriptTaskOperationTemplateData(
          bpmnId = "AckPreparation",
          preAssignments = Compiler.compileAssignments(
            """msg1 = "Заявка на регистрацию организации" + title.ru + "получена и будет рассмотрена в течении трёх дней."""".stripMargin).right.get,
          postAssignments = List.empty))

      def sourceCode = Future {
        Source.fromURL(getClass.getResource("/bpmn/ResidentRegistration.bpmn")).mkString
      }
    }

    object SubProcess {
      val id = UUID.fromString("ed7c46dc-9c81-46cd-aef5-ec21cc5f8451")

      val opData: Set[OperationTemplateData] = Set(
        StartOperationTemplateData("Start"),
        ParallelGatewayOperationTemplateData("Fork"),
        SubProcessOperationTemplateData(
          bpmnId = "SubProcessA",
          data = Json.obj(),
          templateId = StartEnd.id,
          taskDataStructureId = None,
          plannedStart = None,
          plannedDuration = None,
          preAssignments = List.empty,
          postAssignments = List.empty),
        SubProcessOperationTemplateData(
          bpmnId = "SubProcessB",
          data = Json.obj(),
          templateId = ResidentRegistration.id,
          taskDataStructureId = None,
          plannedStart = None,
          plannedDuration = None,
          preAssignments = List.empty,
          postAssignments = Compiler.compileAssignments(
            """
              |company = $sub.company + " (updated)"
            """.stripMargin).right.get),
        ParallelGatewayOperationTemplateData("Join"),
        EndOperationTemplateData("End"))

      def sourceCode = Future {
        Source.fromURL(getClass.getResource("/bpmn/SubProcess.bpmn")).mkString
      }
    }
  }

  val formTemplateIdA: FormTemplate.Id = UUID.fromString("532ed122-91e6-4dc5-bbcb-ccf0dba1c72a")
  val formTemplateIdC: FormTemplate.Id = UUID.fromString("6ba8aad7-9008-4662-aa58-a472a92d6184")

  def fillMasterData(masterDataManager: MasterDataManager): Future[MasterData] = {
    for {
      startEndSourceCode <- ProcessTemplates.StartEnd.sourceCode
      residentRegistrationSourceCode <- ProcessTemplates.ResidentRegistration.sourceCode
      subProcessSourceCode <- ProcessTemplates.SubProcess.sourceCode

      dataStructureA <- generateUpdateDataStructure()
      dataStructureB <- generateUpdateDataStructure()

      startEndProcessTemplate <- generateUpdateProcessTemplate(
        id = ProcessTemplates.StartEnd.id,
        opData = ProcessTemplates.StartEnd.opData,
        dataStructureId = processDataStructureId,
        sourceCode = startEndSourceCode)

      residentRegistrationTemplate <- generateUpdateProcessTemplate(
        id = ProcessTemplates.ResidentRegistration.id,
        opData = ProcessTemplates.ResidentRegistration.opData,
        dataStructureId = processDataStructureId,
        sourceCode = residentRegistrationSourceCode)

      subProcessTemplate <- generateUpdateProcessTemplate(
        id = ProcessTemplates.SubProcess.id,
        opData = ProcessTemplates.SubProcess.opData,
        dataStructureId = processDataStructureId,
        sourceCode = subProcessSourceCode)

      formTemplateA <- generateUpdateFormTemplate(
        id = formTemplateIdA,
        dataStructureId = formDataStructureIdA)

      formTemplateC <- generateUpdateFormTemplate(
        id = formTemplateIdC,
        dataStructureId = formDataStructureIdC)

      processTemplateA <- masterDataManager.createAndGetProcessTemplate(startEndProcessTemplate)
      processTemplateB <- masterDataManager.createAndGetProcessTemplate(residentRegistrationTemplate)
      processTemplateC <- masterDataManager.createAndGetProcessTemplate(subProcessTemplate)

      dataStructureA <- masterDataManager.createAndGetDataStructure(dataStructureA)
      dataStructureB <- masterDataManager.createAndGetDataStructure(dataStructureB)

      secretaryDataStructure <- masterDataManager.createAndGetDataStructure(secretaryDataStructure)
      processDataStructureA <- masterDataManager.createAndGetDataStructure(processDataStructure)

      formDataStructureA <- masterDataManager.createAndGetDataStructure(formDataStructureA)
      formDataStructureB <- masterDataManager.createAndGetDataStructure(formDataStructureB)
      formDataStructureC <- masterDataManager.createAndGetDataStructure(formDataStructureC)

      filesDataStructureA <- masterDataManager.createAndGetDataStructure(filesDataStructureA)
      filesDataStructureB <- masterDataManager.createAndGetDataStructure(filesDataStructureB)

      userDataStructure <- masterDataManager.createAndGetDataStructure(userDataStructure)
      usersDataStructure <- masterDataManager.createAndGetDataStructure(usersDataStructure)

      formTemplateA <- masterDataManager.createAndGetFormTemplate(formTemplateA)
      formTemplateC <- masterDataManager.createAndGetFormTemplate(formTemplateC)
    } yield {

      val masterData = MasterData(
        masterDataManager = masterDataManager,

        processTemplates = Seq(
          processTemplateA,
          processTemplateB,
          processTemplateC),

        dataStructures = Seq(
          dataStructureA,
          dataStructureB,
          secretaryDataStructure,
          processDataStructureA,
          formDataStructureA,
          formDataStructureB,
          formDataStructureC,
          filesDataStructureA,
          filesDataStructureB),

        formTemplates = Seq(
          formTemplateA,
          formTemplateC))

      val dataStructures: Seq[String] = masterData.dataStructures.map(x => s"${x.name} [${x.id}]")

      log.info(s"added all test entities, dataStructures: ${dataStructures.asJson}")

      masterData
    }
  }

  /**
   * Filled MasterDataManager with sample entities:
   *  - DataStructures
   *  - ProcessTemplates
   *  - FormTemplates
   */
  def newFilledMasterDataManager: Future[MasterData] =
    for {
      x <- newMasterDataManager
      y <- fillMasterData(x)
    } yield y
}
