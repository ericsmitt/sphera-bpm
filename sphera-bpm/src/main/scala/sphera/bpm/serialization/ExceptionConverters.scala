//package sphera.bpm.serialization
//
//import sphera.bpm.Exception._
//import sphera.bpm.serializer.proto.exceptions._
//
//trait ExceptionConverters extends ModelConverters {
//  import Implicits._
//
//  implicit def toBpmException(x: BpmException): BpmExceptionV1 = {
//    x match {
//      case y: UnknownException =>
//        BpmExceptionV1.defaultInstance.withOpt1(
//          UnknownExceptionV1(
//            y.message))
//      case y: EntityNotFoundException =>
//        BpmExceptionV1.defaultInstance.withOpt2(
//          EntityNotFoundExceptionV1(
//            entityId = y.entityId,
//            entityType = y.entityType))
//      case y: EntityAlreadyExistsException =>
//        BpmExceptionV1.defaultInstance.withOpt3(
//          EntityAlreadyExistsExceptionV1(
//            entityId = y.entityId,
//            entityType = y.entityType))
//      case y: DefinitionsTreeException =>
//        BpmExceptionV1.defaultInstance.withOpt4(
//          DefinitionsTreeExceptionV1(
//            path = y.path))
//      case y: DataConsistencyException =>
//        BpmExceptionV1.defaultInstance.withOpt5(
//          DataConsistencyExceptionV1(
//            path = y.path))
//      case y: OperationException =>
//        BpmExceptionV1.defaultInstance.withOpt6(
//          OperationExceptionV1(
//            dateTime = y.dateTime,
//            operationId = y.operationId,
//            operationBpmId = y.operationBpmId,
//            operationName = y.operationName,
//            cause = y.cause.map(toBpmException)))
//      case y: UpdateValueException =>
//        BpmExceptionV1.defaultInstance.withOpt7(
//          SetVarExceptionV1(
//            dateTime = y.dateTime,
//            cause = y.cause.map(toBpmException)))
//    }
//  }
//
//  implicit def fromBpmException(x: BpmExceptionV1): BpmException = {
//    val opt1 = x.bpmExceptionOneof.opt1
//    val opt2 = x.bpmExceptionOneof.opt2
//    val opt3 = x.bpmExceptionOneof.opt3
//    val opt4 = x.bpmExceptionOneof.opt4
//    val opt5 = x.bpmExceptionOneof.opt5
//    val opt6 = x.bpmExceptionOneof.opt6
//    val opt7 = x.bpmExceptionOneof.opt7
//    Seq(opt1, opt2, opt3, opt4, opt5, opt6, opt7).flatten.head match {
//      case y: UnknownExceptionV1 =>
//        UnknownException(
//          message = y.message)
//      case y: EntityNotFoundExceptionV1 =>
//        EntityNotFoundException(
//          entityId = y.entityId,
//          entityType = fromEntityType(y.entityType))
//      case y: EntityAlreadyExistsExceptionV1 =>
//        EntityAlreadyExistsException(
//          entityId = y.entityId,
//          entityType = fromEntityType(y.entityType))
//      case y: DefinitionsTreeExceptionV1 =>
//        DefinitionsTreeException(
//          path = y.path)
//      case y: DataConsistencyExceptionV1 =>
//        DataConsistencyException(
//          path = y.path)
//      case y: OperationExceptionV1 =>
//        OperationException(
//          dateTime = y.dateTime,
//          operationId = y.operationId,
//          operationBpmId = y.operationBpmId,
//          operationName = y.operationName,
//          cause = y.cause.map(fromBpmException))
//      case y: SetVarExceptionV1 =>
//        UpdateValueException(
//          dateTime = y.dateTime,
//          cause = y.cause.map(fromBpmException))
//    }
//  }
//}