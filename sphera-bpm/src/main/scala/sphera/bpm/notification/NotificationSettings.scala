package sphera.bpm.notification

trait NotificationSettings

case class MailSettings(
  smtp: Map[String, AnyRef],
  from: String,
  username: String,
  password: String,
  debug: Boolean) extends NotificationSettings

case class SmsSettings(
  apiUrl: String,
  apiKey: String,
  apiSalt: String,
  route: String,
  fromExtension: String,
  debug: Boolean) extends NotificationSettings
