package sphera.bpm.process

import akka.testkit.TestKit
import sphera.bpm.BpmBaseSpec
import sphera.bpm.json._
import sphera.bpm.masterdata.actor.NewMasterDataManager
import sphera.core.test.PersistenceSpec

trait NewSubProcess extends NewResidentRegistration { _: PersistenceSpec with TestKit =>
  val subProcessA = "SubProcessA"
  val subProcessB = "SubProcessB"
}