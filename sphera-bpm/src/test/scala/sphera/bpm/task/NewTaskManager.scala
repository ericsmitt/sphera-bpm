//package sphera.bpm.task
//
//import akka.actor.ActorRef
//import akka.testkit.{TestKit, TestProbe}
//import sphera.bpm.history.HistoryManager
//import sphera.bpm.task.actor.TaskManagerActor
//import sphera.bpm.task.actor.TaskManagerActor._
//import sphera.core.test.PersistenceSpec
//import org.scalatest.Assertion
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//
//trait NewTaskManager extends NewHistoryManager with NewUserTask { _: PersistenceSpec with TestKit =>
//
//  def generateTaskManagerId = s"taskManager-$generateInt"
//
//  def fishForCreateTask(p: TestProbe): Task.Id = {
//    val event = p.fishForSpecificMessage[CreatedTaskInfoEvt](1 second, s"CreatedTaskInfoEvt") {
//      case event: CreatedTaskInfoEvt => event
//    }
//    event.x.id
//  }
//
//  def expectCreateTask(p: TestProbe): Task.Id = {
//    p.expectMsgType[CreatedTaskInfoEvt].x.id
//  }
//
//  def expectCompleteTask(p: TestProbe, x: Task.Id): Assertion =
//    p.expectMsgType[DeletedTaskInfoEvt].taskId shouldBe x
//
//  def fishForCompleteTask(p: TestProbe, x: Task.Id): Assertion = {
//    val event = p.fishForSpecificMessage[DeletedTaskInfoEvt](1 second, s"DeletedTaskInfoEvt") {
//      case event: DeletedTaskInfoEvt => event
//    }
//    succeed
//  }
//
//  def newTaskManagerActor(x: TaskManager.Id = generateTaskManagerId, y: HistoryManager): Future[ActorRef] =
//    for {
//      FilledMasterDataManager(m, _, _, t) <- newFilledMasterDataManagerA
//      n <- newNotificationManager()
//    } yield {
//      system.actorOf(
//        props = TaskManagerActor.props(
//          id = x,
//          masterDataManager = m,
//          notificationManager = n,
//          historyManager = y),
//        name = x)
//    }
//
//  def newTaskManager(x: TaskManager.Id = generateTaskManagerId): Future[TaskManager] =
//    for {
//      a <- newHistoryManager()
//      b <- newTaskManagerActor(x, a)
//    } yield TaskManager(b, a)
//
//  def newSubscribedOnTaskManagerEventsTestProbe: TestProbe = {
//    val p = newTestProbe
//    subscribeOnTaskManagerEvents(p)
//  }
//
//  def subscribeOnTaskManagerEvents(p: TestProbe): TestProbe = {
//    system.eventStream.subscribe(p.ref, classOf[Event])
//    p
//  }
//}
