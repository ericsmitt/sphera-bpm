package sphera.bpm.json

case class JsonNotification(
  email: Option[String],
  phone: Option[String],
  subject: String,
  message: String)

case class JsonVerification(
  phone: String,
  subject: String,
  message: String,
  code: String)