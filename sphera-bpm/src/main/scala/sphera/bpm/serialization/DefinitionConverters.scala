//package sphera.bpm.serialization
//
//import sphera.bpm.definition.{ Definitions, _ }
//import sphera.bpm.serializer.proto.definition._
//
//trait DefinitionConverters {
//  import sphera.bpm.definition.Implicits._
//
//  val DefinitionsManifestV1 = "Definitions.v1"
//
//  def toDefinitionsBinary(obj: Definitions): Array[Byte] = {
//    val repr = obj.repr map { case (x, y) => x -> toAnySimpleDefinition(y) }
//    DefinitionsV1(repr).toByteArray
//  }
//
//  implicit def toDefinitions(obj: Definitions): DefinitionsV1 = {
//    val repr = obj.repr map { case (x, y) => x -> toAnySimpleDefinition(y) }
//    DefinitionsV1(repr)
//  }
//
//  implicit def fromDefinitions(x: DefinitionsV1): Definitions = {
//    val repr = x.repr map { case (a, b) => a -> fromAnySimpleDefinition(b) }
//    Definitions(repr)
//  }
//
//  def toAnySimpleDefinition(x: AnySimpleDefinition): AnySimpleDefinitionV1 = x match {
//    case y: StringDefinition =>
//      AnySimpleDefinitionV1.defaultInstance.withOpt1(
//        StringDefinitionV1(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path))
//    case y: IntDefinition =>
//      AnySimpleDefinitionV1.defaultInstance.withOpt2(
//        IntDefinitionV1(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path))
//    case y: BooleanDefinition =>
//      AnySimpleDefinitionV1.defaultInstance.withOpt3(
//        BooleanDefinitionV1(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path))
//    case y: DecimalDefinition =>
//      AnySimpleDefinitionV1.defaultInstance.withOpt4(
//        DecimalDefinitionV1(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path))
//  }
//
//  def fromDefinitionsBinary(bytes: Array[Byte]): Definitions = {
//    val x = DefinitionsV1.parseFrom(bytes).repr.map {
//      case (a, b) => (a, fromAnySimpleDefinition(b))
//    }
//    Definitions(x)
//  }
//
//  def fromAnySimpleDefinition(x: AnySimpleDefinitionV1): AnySimpleDefinition = {
//    val opt1 = x.anySimpleDefinitionOneof.opt1
//    val opt2 = x.anySimpleDefinitionOneof.opt2
//    val opt3 = x.anySimpleDefinitionOneof.opt3
//    val opt4 = x.anySimpleDefinitionOneof.opt4
//    Seq(opt1, opt2, opt3, opt4).flatten.head match {
//      case y: StringDefinitionV1 =>
//        StringDefinition(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path)
//      case y: IntDefinitionV1 =>
//        IntDefinition(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path)
//      case y: BooleanDefinitionV1 =>
//        BooleanDefinition(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path)
//      case y: DecimalDefinitionV1 =>
//        DecimalDefinition(
//          name = y.name,
//          description = y.description,
//          value = y.value,
//          path = y.path)
//      case _ => sys.error("Can'template deserialize AnySimpleDefinitionV1")
//    }
//  }
//}