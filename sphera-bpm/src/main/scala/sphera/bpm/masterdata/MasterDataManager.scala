package sphera.bpm.masterdata

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.core.akkaext.actor.ActorId

import scala.concurrent.ExecutionContext

class MasterDataManager(val masterDataManagerActor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout)
  extends MasterDataManagerLike

object MasterDataManager {
  type Id = ActorId

  def apply(masterDataManagerActor: ActorRef)(implicit c: ExecutionContext, t: Timeout): MasterDataManager = {
    new MasterDataManager(masterDataManagerActor)
  }
}
