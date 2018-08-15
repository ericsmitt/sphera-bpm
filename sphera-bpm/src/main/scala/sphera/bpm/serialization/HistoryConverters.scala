//package sphera.bpm.serialization
//
//import sphera.bpm.history.actor.HistoryManagerActor.CreatedHistoryDataEvt
//import sphera.bpm.history.actor.HistoryManagerState
//import sphera.bpm.history.model.{ HistoryData, ProcessHistoryData, ProjectHistoryData, TaskHistoryData }
//import sphera.bpm.serializer.proto.history._
//
//trait HistoryConverters extends DefinitionConverters
//  with ModelConverters
//  with LangConverters
//  with ProjectConverters
//  with ProcessConverters
//  with TaskConverters {
//  import Implicits._
//
//  val HistoryStateManifestV1 = "HistoryState.v1"
//  val CreatedHistoryDataEvtManifestV1 = "CreatedHistoryDataEvt.v1"
//
//  def toHistoryStateBinary(obj: HistoryManagerState): Array[Byte] = {
//    HistoryStateV1(history = obj.v.mapValues(toHistoryData)).toByteArray
//  }
//
//  def fromHistoryStateBinary(obj: Array[Byte]): HistoryManagerState = {
//    val x = HistoryStateV1.parseFrom(obj).history.mapValues(fromHistoryData)
//    HistoryManagerState(x)
//  }
//
//  def toHCreatedHistoryDataEvtBinary(obj: CreatedHistoryDataEvt): Array[Byte] = {
//    toCreatedHistoryDataEvt(obj).toByteArray
//  }
//
//  def fromCreatedHistoryDataEvtBinary(obj: Array[Byte]): CreatedHistoryDataEvt = {
//    val x = CreatedHistoryDataEvtV1.parseFrom(obj).x
//    CreatedHistoryDataEvt(x)
//  }
//
//  implicit def toHistoryData(x: HistoryData): HistoryDataV1 = {
//    x match {
//      case y: ProjectHistoryData =>
//        HistoryDataV1.defaultInstance.withOpt1(
//          ProjectHistoryDataV1(
//            id = y.id,
//            extendedInfo = y.extendedInfo,
//            completedOn = y.completedOn))
//      case y: ProcessHistoryData =>
//        HistoryDataV1.defaultInstance.withOpt2(
//          ProcessHistoryDataV1(
//            id = y.id,
//            extendedInfo = y.extendedInfo,
//            completedOn = y.completedOn))
//      case y: TaskHistoryData =>
//        HistoryDataV1.defaultInstance.withOpt3(
//          TaskHistoryDataV1(
//            id = y.id,
//            extendedInfo = y.extendedInfo,
//            completedOn = y.completedOn))
//    }
//  }
//
//  implicit def fromHistoryData(x: HistoryDataV1): HistoryData = {
//    val opt1 = x.historyDataOneof.opt1
//    val opt2 = x.historyDataOneof.opt2
//    val opt3 = x.historyDataOneof.opt3
//    Seq(opt1, opt2, opt3).flatten.head match {
//      case y: ProjectHistoryDataV1 =>
//        ProjectHistoryData(
//          id = y.id,
//          extendedInfo = y.extendedInfo,
//          completedOn = y.completedOn)
//      case y: ProcessHistoryDataV1 =>
//        ProcessHistoryData(
//          id = y.id,
//          extendedInfo = y.extendedInfo,
//          completedOn = y.completedOn)
//      case y: TaskHistoryDataV1 =>
//        TaskHistoryData(
//          id = y.id,
//          extendedInfo = y.extendedInfo,
//          completedOn = y.completedOn)
//    }
//  }
//
//  def toCreatedHistoryDataEvt(x: CreatedHistoryDataEvt): CreatedHistoryDataEvtV1 = {
//    CreatedHistoryDataEvtV1(toHistoryData(x.x))
//  }
//
//  def fromCreatedHistoryDataEvt(x: CreatedHistoryDataEvtV1): CreatedHistoryDataEvt = {
//    CreatedHistoryDataEvt(fromHistoryData(x.x))
//  }
//}