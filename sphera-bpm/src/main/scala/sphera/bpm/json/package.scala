package sphera.bpm

import io.circe._

package object json extends JsonSupport with Codec with ExtendedJson {
  def parse(input: String): Json = parser.parse(input: String).right.get

  implicit final class EncoderOps[A](val wrappedEncodeable: A) extends AnyVal {
    def asJson(implicit encoder: Encoder[A]): Json = encoder(wrappedEncodeable)

    def asJsonObject(implicit encoder: ObjectEncoder[A]): JsonObject =
      encoder.encodeObject(wrappedEncodeable)

    def asJsonStr(implicit encoder: Encoder[A]): String = encoder(wrappedEncodeable).pretty(printer)
  }
}
