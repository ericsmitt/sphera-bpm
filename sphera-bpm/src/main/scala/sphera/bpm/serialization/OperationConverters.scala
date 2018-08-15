//package sphera.bpm.serialization
//
//import sphera.bpm.process.actor.operation._
//import sphera.bpm.serializer.proto.operation._
//
//trait OperationConverters {
//  import Implicits._
//
//  val ParallelGatewayOperationStateManifestV1 = "ParallelGatewayOperationState.v1"
//  val UserTaskOperationStateManifestV1 = "UserTaskOperationState.v1"
//  val OperationActivatedEvtManifestV1 = "OperationActivatedEvt.v1"
//
//  def toParallelGatewayOperationStateBinary(obj: ParallelGatewayOperationActor.State): Array[Byte] = {
//    ParallelGatewayOperationStateV1(
//      x = obj.x,
//      converged = obj.converged).toByteArray
//  }
//
//  def fromParallelGatewayOperationStateBinary(bytes: Array[Byte]): ParallelGatewayOperationActor.State = {
//    val x = ParallelGatewayOperationStateV1.parseFrom(bytes).get
//    ParallelGatewayOperationActor.State(
//      x = x.x,
//      converged = x.converged)
//  }
//
//  def toUserTaskOperationStateBinary(obj: UserTaskOperationActor.UserTaskOperationState): Array[Byte] = {
//    UserTaskOperationStateV1(activated = obj.activated).toByteArray
//  }
//
//  def fromUserTaskOperationStateBinary(bytes: Array[Byte]): UserTaskOperationActor.UserTaskOperationState = {
//    val x = UserTaskOperationStateV1.parseFrom(bytes).get
//    UserTaskOperationActor.State(activated = x.activated)
//  }
//
//  def toOperationActivatedEvtBinary(obj: OperationActor.ActivatedEvt): Array[Byte] = {
//    OperationActivatedEvtV1(bpmId = obj.bpmnId).toByteArray
//  }
//
//  def fromOperationActivatedEvtBinary(bytes: Array[Byte]): OperationActor.ActivatedEvt = {
//    val x = OperationActivatedEvtV1.parseFrom(bytes)
//    OperationActor.ActivatedEvt(
//      bpmnId = x.bpmId)
//  }
//}