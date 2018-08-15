package sphera.bpm.storage.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.actor.BpmActor
import sphera.bpm.storage.StorageManager._
import sphera.bpm.storage._
import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.storage.definition.actor.{ DefStorageNodeActor, DefStorageNodeState }
import sphera.bpm.storage.document.actor.{ DocumentStorageNodeActor, DocumentStorageNodeState }
import sphera.bpm.storage.document.{ Document, DocumentStorage }
import sphera.bpm.storage.json.JsonStorage
import sphera.bpm.utils.Printable
import sphera.core.akkaext.actor.ActorId
import sphera.core.akkaext.persistence._

import scala.concurrent.ExecutionContext

class StorageManagerActor(
  val id: StorageManager.Id,
  val bpm: Bpm,
  val initState: StorageManagerState)(implicit val c: ExecutionContext, val t: Timeout)
  extends CqrsPersistentActor[StorageManagerState] with Printable {

  def logNodeStatus(info: NodeInfo, status: NodeStatus): Unit = {
    log.info(s"node ${status.name.blue} [${info.id.name}]")
  }

  def createNode(state: State, create: CreateNodeInfo): Unit = {
    val info = NodeInfo(
      id = generateChildId,
      name = create.name,
      description = create.description,
      status = NodeStatus.Created)

    persist(state, CreatedNodeInfoEvt(info)) { (state, event) =>
      create.storage match {
        case x: DefStorage =>
          context.actorOf(DefStorageNodeActor.props(
            id = info.id,
            bpm = bpm,
            state = DefStorageNodeState(x)), info.id.name)
        case x: DocumentStorage =>
          context.actorOf(DocumentStorageNodeActor.props(
            id = info.id,
            bpm = bpm,
            state = DocumentStorageNodeState(x)), info.id.name)
      }

      sender() ! NodeInfoOpt(Some(info))
      logNodeStatus(info, NodeStatus.Created)
    }
  }

  def getNode(state: State, nodeId: ActorId): Unit = {
    getChildOpt(nodeId) match {
      case x @ Some(_) => sender() ! NodeOpt(x)
      case None =>
        val getNodeOptResult = {
          if (nodeId.contains(BpmActor.ProcessManagerActorName))
            bpm.processManager.concreteProcessManager.getNodeOpt(nodeId)
          else if (nodeId.contains(BpmActor.TaskManagerActorName))
            bpm.taskManager.concreteTaskManager.getNodeOpt(nodeId)
          else bpm.projectManager.concreteProjectManager.getNodeOpt(nodeId)
        }
        getNodeOptResult pipeTo sender()
    }
  }

  def behavior(state: State) = {
    case GetNode(x) => getNode(state, x)
    case GetNodeInfoById(x) => sender() ! NodeInfoOpt(state.getNodeInfoOpt(x))
    case GetNodeInfoAll => sender() ! NodeInfoMap(state.getNodeInfoMap)
    case GetNodeStatus(x) => sender() ! NodeStatusOpt(state.getNodeInfoOpt(x).map(_.status))
    case CreateNodeCmd(x) => createNode(state, x)
  }

  override def afterRecover(state: State): Unit = {
    state.getNodeInfoMap.foreach {
      case (_, info) =>
        context.actorOf(DefStorageNodeActor.props(
          id = info.id,
          bpm = bpm), info.id.name)

        logNodeStatus(info, info.status)
    }
  }
}

object StorageManagerActor {
  def props(id: Id, bpm: Bpm, state: StorageManagerState = StorageManagerState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new StorageManagerActor(id, bpm, state))
  }
}