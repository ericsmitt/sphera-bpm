package sphera.bpm.storage.actor

import sphera.bpm.runnable.RunnableManagerBehavior
import sphera.bpm.storage.StorageManager.{ GetNode, NodeOpt }

trait NodeContainerBehavior { _: RunnableManagerBehavior =>
  override def preBehavior(state: State): Receive = {
    case GetNode(x) => getRunnableRefOpt(state, x).map(NodeOpt.apply) pipeTo sender()
  }
}
