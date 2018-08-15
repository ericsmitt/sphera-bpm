//package sphera.bpm.task.actor
//
//import akka.actor.ActorRef
//import akka.testkit.{TestKit, TestProbe}
//import sphera.bpm.history.HistoryManager
//import sphera.bpm.masterdata.MasterDataManager
//import sphera.bpm.masterdata.actor.NewMasterDataManager
//import sphera.bpm.masterdata.model._
//import sphera.bpm.model.ModifyAttr
//import sphera.bpm.notification.{NewNotificationManager, NotificationManager}
//import sphera.bpm.process.{NewProcessManager, Process}
//import sphera.bpm.task
//import sphera.bpm.task._
//import sphera.bpm.task.actor.UserTaskActor._
//import sphera.core.test.PersistenceSpec
//import org.scalatest.Assertion
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//
//trait NewUserTask extends NewMasterDataManager
//  with NewNotificationManager
//  with NewProcessManager
//  with NewDefinitionStorage { _: PersistenceSpec with TestKit =>
//
//  val formInputFirstName = Input(
//    caption = MLString(Map(
//      "EN" -> "firstname")),
//    description = MLString(Map(
//      "EN" -> "input")),
//    variable = "firstname",
//    required = true,
//    readOnly = false,
//    validationRule = StringRule(None, None))
//
//  val formInputLastName = Input(
//    caption = MLString(Map(
//      "EN" -> "lastname")),
//    description = MLString(Map(
//      "EN" -> "input")),
//    variable = "lastname",
//    required = true,
//    readOnly = false,
//    validationRule = StringRule(None, None))
//
//  val formStructureA = FormStructure(
//    caption = MLString(Map(
//      "EN" -> "caption")),
//    description = None,
//    mainPanel = MainPanel(
//      caption = MLString(Map(
//        "EN" -> "panel")),
//      description = None,
//      sections = List(
//        Section(
//          caption = MLString(Map(
//            "EN" -> "section")),
//          description = None,
//          showCaption = true,
//          transitionAttr = true,
//          items = List(
//            formInputFirstName,
//            formInputLastName)))),
//    None,
//    None)
//
//  def updateData(a: ActorRef, x: Data): Future[Any] =
//    ask(a, DefStorage.UpdateDataCmd(x))
//      .mapTo[DefStorage.Response]
//
//  def getData(a: ActorRef): Future[Data] =
//    ask(a, DefStorage.GetDataAll)
//      .mapTo[DefStorage.StorageData]
//      .map(_.data)
//
//  def fishForUpdatedDataEvt(p: TestProbe): Assertion = {
//    p.fishForSpecificMessage[DefStorage.UpdatedDataEvt](1 second, s"UpdatedDataEvt") {
//      case event: UpdatedDataEvt => event
//    }
//    succeed
//  }
//
//  def expectUpdatedDataEvt(p: TestProbe): Assertion = {
//    p.expectMsgType[UpdatedDataEvt]
//    succeed
//  }
//
//  def completeTask(a: ActorRef, taskId: Task.Id): Future[Task.Response] =
//    ask(a, Task.Complete).mapTo[Task.Response]
//
//  implicit class SubscribedOnTaskEventsTestProbe(p: TestProbe) {
//    def withSubscriptionOnUserTaskEvents: TestProbe = {
//      system.eventStream.subscribe(p.ref, classOf[TaskManagerActor.Event])
//      system.eventStream.subscribe(p.ref, classOf[DefStorage.Event])
//      p
//    }
//  }
//
//  def generateTaskId = s"task-$generateInt"
//
//  def generateTaskInfo: TaskInfo = {
//    val x = generateCreateTaskInfo()
//    task.TaskInfo(
//      id = generateTaskId,
//      name = x.name,
//      description = x.description,
//      taskType = x.taskType,
//      status = TaskStatus.Created,
//      initiator = x.initiator,
//      ownerId = x.ownerId,
//      assigneeId = x.assigneeId,
//      reviewerId = x.reviewerId,
//      watcherIds = x.watcherIds,
//      processId = x.processId,
//      operationId = x.operationId,
//      operationBpmId = x.operationBpmnId,
//      formTemplateId = x.formTemplateId,
//      startedOn = None,
//      actualEnd = None,
//      plannedStart = None,
//      plannedDuration = None,
//      modifyAttr = ModifyAttr(createdBy = generateModifier))
//  }
//
//  def generateCreateTaskInfo(processId: Process.Id = generateProcessId): CreateTaskInfo = {
//    CreateTaskInfo(
//      name = s"name-$generateInt",
//      description = Option(s"desc-$generateInt"),
//      defStorage = definitions,
//      taskType = TaskType.UserTask,
//      initiator = Initiator.ProcessManager,
//      ownerId = userIdA,
//      assigneeId = None,
//      reviewerId = None,
//      watcherIds = Set.empty,
//      processId = Option(processId),
//      operationId = Option(generateOperationId),
//      operationBpmnId = Option(generateOperationId),
//      formTemplateId = Option(formTemplateIdA),
//      plannedStart = None,
//      plannedDuration = None)
//  }
//
//  def generateUpdateTaskInfo(data: Task.Id = generateTaskId): Future[UpdateTaskInfo] = Future {
//    task.UpdateTaskInfo(
//      id = data,
//      name = Option(s"name-$generateInt"),
//      description = Option(Option(s"desc-$generateInt")),
//      status = Option(TaskStatus.Created),
//      initiator = Option(Initiator.ProcessManager),
//      assigneeId = Option(Option(userIdB)),
//      reviewerId = Option(Option(userIdB)),
//      watcherIds = Option(Set(userIdA, userIdB)))
//  }
//
//  def newUserTask(
//    taskId: Task.Id,
//    masterDataManager: MasterDataManager,
//    notificationManager: NotificationManager): Future[ActorRef] = Future {
//    system.actorOf(
//      props = UserTaskActor.props(
//        taskId = taskId,
//        notificationManager = notificationManager,
//        processManager = ProcessManager(newTestProbeRef, newHistoryManager, masterDataManager),
//        state = UserTaskState(definitions)),
//      name = taskId)
//  }
//
//  def newUserTaskInTestProbeContext(
//    taskId: Task.Id,
//    masterDataManager: MasterDataManager,
//    notificationManager: NotificationManager): Future[(TestProbe, ActorRef)] = Future {
//    createInContext(
//      props = UserTaskActor.props(
//        taskId = taskId,
//        notificationManager = notificationManager,
//        processManager = ProcessManager(newTestProbeRef, newHistoryManager, masterDataManager)),
//      name = taskId)
//  }
//
//  def newUserTaskA(data: Task.Id = generateTaskId): Future[ActorRef] = {
//    for {
//      FilledMasterDataManager(m, _, _, t) <- newFilledMasterDataManagerA
//      n <- newNotificationManager()
//      data <- newUserTask(data, m, n)
//    } yield data
//  }
//
//  def newUserTaskB(data: Task.Id = generateTaskId): Future[(TestProbe, ActorRef)] = {
//    for {
//      FilledMasterDataManager(m, _, _, t) <- newFilledMasterDataManagerA
//      n <- newNotificationManager()
//      (p, a) <- newUserTaskInTestProbeContext(data, m, n)
//    } yield (p, a)
//  }
//
//  private def newHistoryManager = HistoryManager(newTestProbeRef)
//}