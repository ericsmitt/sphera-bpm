package sphera.bpm.storage.schema

import akka.event.LoggingAdapter
import sphera.bpm.Implicits._
import sphera.bpm.SchemaValidationException
import io.circe.{ Json, Printer }
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

object SchemaValidator {
  private val printer = Printer.noSpaces.copy(dropNullKeys = true)

  def validateTry(json: Json, schema: Json): Try[Unit] = {
    val x1 = new JSONObject(json.pretty(printer))
    val x2 = new JSONObject(schema.pretty(printer))
    Try(SchemaLoader.load(x2).validate(x1)) match {
      case Failure(e: org.everit.json.schema.ValidationException) =>
        Failure(SchemaValidationException(
          messages = e.getAllMessages.asScala.toList,
          cause = e))
      case Failure(e) =>
        Failure(SchemaValidationException(
          messages = List(e.message),
          cause = e))
      case Success(x) => Success(x)
    }
  }
  def validate(json: Json, schema: Json): Unit = {
    SchemaValidator.validateTry(json, schema) match {
      case Failure(e: SchemaValidationException) => throw e
      case _ =>
    }
  }
}

trait SchemaValidatorSupport {
  implicit def log: LoggingAdapter

  def validate(json: Json, schema: Json): Unit = {
    SchemaValidator.validateTry(json, schema) match {
      case Failure(e) =>
        e.printStackTrace()

        log.info("==========================================")
        log.info(json)
        log.info("==========================================")
        log.info(schema)

        throw e
      case Success(x) => Success(x)
    }
  }
}
