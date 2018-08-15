package sphera.bpm.json

import de.heikoseeberger.akkahttpcirce._
import io.circe._

trait JsonSupport extends BaseCirceSupport with FailFastUnmarshaller {
  implicit val printer: Printer = Printer.spaces4.copy(dropNullKeys = true)
}