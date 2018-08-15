//package sphera.bpm.serialization
//
//import java.util.UUID
//
//import sphera.bpm.notification.actor._
//import sphera.bpm.notification.model.{ SmsNotification, SmsVerification }
//import sphera.bpm.serializer.proto.notification._
//
//trait NotificationConverters {
//  import Implicits._
//
//  val SmsVerificationStateManifestV1 = "SmsVerification.UserTaskOperationState.v1"
//  val SmsVerificationCreatedVerificationEvtManifestV1 = "SmsVerification.CreatedVerificationEvt.v1"
//  val SmsVerificationDeletedVerificationEvtManifestV1 = "SmsVerification.DeletedVerificationEvt.v1"
//
//  def toSmsVerificationCreatedVerificationEvtBinary(obj: SmsVerificationActor.CreatedVerificationEvt): Array[Byte] =
//    CreatedVerificationEvtV1(toSmsVerification(obj.x))
//      .toByteArray
//
//  def toSmsVerificationDeletedVerificationEvtBinary(obj: SmsVerificationActor.DeletedVerificationEvt): Array[Byte] =
//    DeletedVerificationEvtV1(obj.verificationId)
//      .toByteArray
//
//  def toSmsVerificationStateBinary(obj: SmsVerificationState): Array[Byte] = {
//    val n = obj.v.map { case (x, y) => x.toString -> toSmsVerification(y) }
//    SmsVerificationStateV1(n).toByteArray
//  }
//
//  def fromSmsVerificationCreatedVerificationEvt(bytes: Array[Byte]): SmsVerificationActor.CreatedVerificationEvt = {
//    val x = CreatedVerificationEvtV1.parseFrom(bytes).x
//    SmsVerificationActor.CreatedVerificationEvt(fromSmsVerification(x))
//  }
//
//  def fromSmsVerificationDeletedVerificationEvt(bytes: Array[Byte]): SmsVerificationActor.DeletedVerificationEvt = {
//    val x = UUID.fromString(DeletedVerificationEvtV1.parseFrom(bytes).verificationId)
//    SmsVerificationActor.DeletedVerificationEvt(x)
//  }
//
//  def fromSmsVerificationState(bytes: Array[Byte]): SmsVerificationState = {
//    val x = SmsVerificationStateV1.parseFrom(bytes).v.map {
//      case (a, b) => (UUID.fromString(a), fromSmsVerification(b))
//    }
//    SmsVerificationState(x)
//  }
//
//  implicit def toSmsNotificationVerification(x: SmsNotification.Verification): SmsNotificationVerificationV1 = {
//    SmsNotificationVerificationV1(
//      id = x.id,
//      phone = x.phone,
//      code = x.code,
//      subject = x.subject,
//      message = x.message,
//      retry = x.retry)
//  }
//
//  implicit def fromSmsNotificationVerification(x: SmsNotificationVerificationV1): SmsNotification.Verification = {
//    SmsNotification.Verification(
//      id = x.id,
//      phone = x.phone,
//      code = x.code,
//      subject = x.subject,
//      message = x.message,
//      retry = x.retry)
//  }
//
//  implicit def toSmsVerification(x: SmsVerification): SmsVerificationV1 = {
//    SmsVerificationV1(
//      id = x.id,
//      notification = x.notification,
//      duration = x.duration)
//  }
//
//  implicit def fromSmsVerification(x: SmsVerificationV1): SmsVerification = {
//    SmsVerification(
//      id = x.id,
//      notification = x.notification,
//      duration = x.duration)
//  }
//}