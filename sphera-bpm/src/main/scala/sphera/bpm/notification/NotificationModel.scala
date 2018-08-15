package sphera.bpm.notification

import java.util.UUID

import sphera.core.domain.tenancy.model.User

sealed trait Notification {
  def id: Notification.Id
  def message: String
  def retry: Int
}

object Notification {
  type Id = UUID
}

sealed trait MailNotification extends Notification {
  def email: String
  def subject: String
  def message: String
}

object MailNotification {
  /**
   * Уведомление с паролем
   * Обрабатывается особым образом
   */
  case class Password(
    id: Notification.Id,
    email: String,
    subject: String,
    message: String,
    password: String,
    retry: Int = 10) extends MailNotification

  /**
   * Обычное текстовое уведомление
   */
  case class TextMessage(
    id: Notification.Id,
    email: String,
    subject: String,
    message: String,
    retry: Int = 10) extends MailNotification
}

sealed trait SmsNotification extends Notification {
  def phone: String
  def subject: String
  def message: String
  def retry: Int
}

object SmsNotification {
  /**
   * Уведомление с паролем
   * Обрабатывается особым образом
   */
  case class Password(
    id: Notification.Id,
    phone: String,
    subject: String,
    message: String,
    password: String,
    retry: Int = 10) extends SmsNotification

  case class Verification(
    id: Notification.Id,
    phone: String,
    subject: String,
    message: String,
    code: String,
    retry: Int = 10) extends SmsNotification

  /**
   * Обычное текстовое уведомление
   */
  case class TextMessage(
    id: Notification.Id,
    phone: String,
    subject: String,
    message: String,
    retry: Int = 10) extends SmsNotification
}

sealed trait WebSocketNotification extends Notification {
  def userIds: Set[User.Id]
}

object WebSocketNotification {
  /**
   * Обычное текстовое уведомление
   */
  case class Message(
    id: Notification.Id,
    userIds: Set[User.Id],
    message: String,
    retry: Int = 10) extends WebSocketNotification
}
