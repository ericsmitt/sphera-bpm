package sphera.bpm.notification.client

import java.net.URLEncoder

import sphera.bpm.notification.{ Notification, SmsSettings }
import sphera.core.akkaext.actor.CqrsResponse
import com.roundeights.hasher.Implicits._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import scalaj.http._

import scala.util.{ Failure, Success, Try }

class SmsClient(s: SmsSettings) {
  def settings: SmsSettings = s
  def endpoint: String = settings.apiUrl + settings.route
  def sign(apiKey: String, json: String, apiSalt: String): String = (apiKey + json + apiSalt).sha256.hex
  def toUrlEncoded(params: Map[String, String]): String =
    params
      .map({ case (a, b) => a + "=" + URLEncoder.encode(b, "utf-8") })
      .mkString("&")
  def requestBody(apiKey: String, sign: String, json: String): String = {
    toUrlEncoded(
      Map(
        "vpbx_api_key" -> apiKey,
        "sign" -> sign,
        "JsonSupport" -> json))
  }
  def prepare(id: Notification.Id, to: String, message: String, from: String): String = {
    val json = RequestJson(
      command_id = id,
      text = message,
      from_extension = settings.fromExtension,
      to_number = to,
      sms_sender = from).asJson.noSpaces
    requestBody(settings.apiKey, sign(settings.apiKey, json, settings.apiSalt), json)
  }
  def send(id: Notification.Id, to: String, message: String, from: String): Try[SmsClient.Response] = {
    if (!settings.debug) {
      val msg = prepare(id, to, message, from)

      def request = Http(endpoint)
        .timeout(connTimeoutMs = 2000, readTimeoutMs = 3000)
        .postData(msg)
        .header("Content-Type", "application/p3-www-form-urlencoded")
        .asString

      Try(request).flatMap { x =>
        if (x.isError) {
          x.code match {
            case 401 => Failure(SmsClient.HttpUnauthorizedException(s"Http response {{code: ${x.code}, body: ${x.body}}}"))
            case _ => Failure(SmsClient.HttpException(s"Http response {{code: ${x.code}, body: ${x.body}}}"))
          }
        } else {
          decode[ResponseJson](x.body)
            .fold(_ => Failure(SmsClient.JsonProtocolException(s"Invalid JsonSupport response {{${x.body}}")), y => {
              y.result match {
                case 1000 => Success(SmsClient.Success)
                case 4001 => Failure(SmsClient.ServiceException("Команда не поддерживается"))
                case 4100 => Failure(SmsClient.ServiceException("Вызов не может быть завершен по логике работы ВАТС"))
                case 4101 => Failure(SmsClient.ServiceException("На момент поступления команды в ВАТС, вызов, к которому относится\nкоманда завершения, уже завершился либо указанный идентификатор\nвызова не найден"))
                case code: Int if 5000 to 5999 contains code => Failure(SmsClient.ServiceException("Ошибка сервера"))
                case _ => Failure(SmsClient.ServiceException("Неизвестная ошибка сервера"))
              }
            })
        }
      }
    } else Success(SmsClient.Success)
  }
}

object SmsClient {
  trait Response extends CqrsResponse

  case object Success extends Response

  abstract class Exception(message: String) extends RuntimeException(message)
  case class HttpUnauthorizedException(message: String) extends Exception(message)
  case class HttpException(message: String) extends Exception(message)
  case class JsonProtocolException(message: String) extends Exception(message)
  case class ServiceException(message: String) extends Exception(message)
}

/**
 * @param command_id идентификатор команды (строка не более 128 байт)
 * @param text текст сообщения. Ограничение поля — 1024 символа
 * @param from_extension идентификатор сотрудника
 * @param to_number номер вызываемого абонента (строка не более 128 байт)
 * @param sms_sender Имя отправителя SMS-сообщения
 */
sealed case class RequestJson(
  command_id: Notification.Id,
  text: String,
  from_extension: String,
  to_number: String,
  sms_sender: String)

sealed case class ResponseJson(
  command_id: Notification.Id,
  result: Int)