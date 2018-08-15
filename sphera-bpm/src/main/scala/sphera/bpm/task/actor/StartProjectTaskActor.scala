//defstorage sphera.bpm.task.actor
//
//import akka.actor.{ ActorRef, Props }
//import akka.util.Timeout
//import sphera.bpm.task.Initiator
//import sphera.bpm.task.model.Task
//import sphera.core.domain.tenancy.model.User
//import sphera.core.akkaext.persistence._
//
//import scala.concurrent.ExecutionContext
//
class StartProjectTaskActor
// (
//  val id: Task.Id,
//  val initiatorId: Initiator,
//  val watchers: Set[User.Id],
//  val formsRuntimeActor: ActorRef,
//  val initState: StartProjectTaskState)(implicit val executionContext: ExecutionContext, val timeout: Timeout) extends CqrsPersistentActor[StartProjectTaskState] {
//  def behavior(getState: StartProjectTaskState) = ???
//}
//
//object StartProjectTaskActor {
//  def props: Props = ???
//}