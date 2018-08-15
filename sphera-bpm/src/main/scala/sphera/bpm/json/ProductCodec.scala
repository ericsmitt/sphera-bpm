package sphera.bpm.json
import io.circe._
import io.circe.syntax._
import shapeless._
import shapeless.labelled._
import io.circe.generic._

import io.circe.syntax._

//trait ProductCodec {
//  implicit def encoderValueClass[T <: AnyVal, V](implicit
//    g: Lazy[Generic.Aux[T, V :: HNil]],
//    e: Encoder[V]): Encoder[T] = Encoder.instance { value ⇒
//    e(g.value.to(value).head)
//  }
//  implicit def decoderValueClass[T <: AnyVal, V](implicit
//    g: Lazy[Generic.Aux[T, V :: HNil]],
//    unchanged: Decoder[V]): Decoder[T] = Decoder.instance { cursor ⇒
//    unchanged(cursor).map { value ⇒
//      g.value.from(value :: HNil)
//    }
//  }
//
//  trait IsEnum[C <: Coproduct] {
//    def to(c: C): String
//    def from(s: String): Option[C]
//  }
//  object IsEnum {
//    implicit val cnilIsEnum: IsEnum[CNil] = new IsEnum[CNil] {
//      def to(c: CNil): String = sys.error("Impossible")
//      def from(s: String): Option[CNil] = None
//    }
//    implicit def cconsIsEnum[K <: Symbol, H <: Product, T <: Coproduct](implicit
//      witK: Witness.Aux[K],
//      witH: Witness.Aux[H],
//      gen: Generic.Aux[H, HNil],
//      tie: IsEnum[T]): IsEnum[FieldType[K, H] :+: T] = new IsEnum[FieldType[K, H] :+: T] {
//      def to(c: FieldType[K, H] :+: T): String = c match {
//        case Inl(h) => witK.value.name
//        case Inr(t) => tie.to(t)
//      }
//      def from(s: String): Option[FieldType[K, H] :+: T] =
//        if (s == witK.value.name) Some(Inl(field[K](witH.value)))
//        else tie.from(s).map(Inr(_))
//    }
//  }
//
//  implicit def encodeEnum[A, C <: Coproduct](implicit
//    gen: LabelledGeneric.Aux[A, C],
//    rie: IsEnum[C]): Encoder[A] = Encoder[String].contramap[A](processManagerActor => rie.to(gen.to(processManagerActor)))
//  implicit def decodeEnum[A, C <: Coproduct](implicit
//    gen: LabelledGeneric.Aux[A, C],
//    rie: IsEnum[C]): Decoder[A] = Decoder[String].emap { s =>
//    rie.from(s).map(gen.from).toRight("enum")
//  }
//}
