package sphera.bpm

import akka.testkit.TestKit
import sphera.bpm.masterdata.actor._
import sphera.bpm.model.ImplicitModifier
import sphera.bpm.process._
import sphera.core.test.PersistenceSpec

trait BpmBaseSpec extends NewMasterDataManager
  with ImplicitModifier
  with NewDataStructuresRepository
  with NewCharacteristicRepository
  with NewFileTypesRepository
  with NewFormTemplatesRepository
  with NewRoleTypesRepository
  with NewProjectTemplatesRepository
  with NewProcessTemplatesRepository
  with NewCalendarTypesRepository
  with NewProcessManager
  with ProcessExpectations
  with RunnableExpectations
  with TaskExpectations
  with HistoryExpectations
  //with ParallelTestExecution
  with PersistenceSpec { _: TestKit =>
}
