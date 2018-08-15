//package sphera.bpm.serialization
//
//import sphera.bpm.definition._
//import sphera.bpm.serializer.proto.storage._
//import sphera.bpm.storage.`def`.DefStorage._
//import sphera.bpm.storage.ExceptionStorage._
//
//trait StorageConverters extends ExceptionConverters {
//  import Implicits._
//
//  val UpdatedDataElementEvtManifestV1 = "UpdatedDataElementEvt.v1"
//  val UpdatedDataEvtManifestV1 = "UpdatedDataEvt.v1"
//  val ExceptionEvtManifestV1 = "ExceptionEvt.v1"
//
//  def toUpdatedDataElementEvtBinary(x: UpdatedDataElementEvt): Array[Byte] = {
//    toUpdatedDataElementEvt(x).toByteArray
//  }
//
//  def fromUpdatedDataElementEvtBinary(x: Array[Byte]): UpdatedDataElementEvt = {
//    fromUpdatedDataElementEvt(UpdatedDataElementEvtV1.parseFrom(x))
//  }
//
//  def toUpdatedDataEvtBinary(x: UpdatedDataEvt): Array[Byte] = {
//    toUpdatedDataEvt(x).toByteArray
//  }
//
//  def fromUpdatedDataEvtBinary(x: Array[Byte]): UpdatedDataEvt = {
//    fromUpdatedDataEvt(UpdatedDataEvtV1.parseFrom(x))
//  }
//
//  def toExceptionEvtBinary(x: ExceptionEvt): Array[Byte] = {
//    toExceptionEvt(x).toByteArray
//  }
//
//  def fromExceptionEvtBinary(x: Array[Byte]): ExceptionEvt = {
//    fromExceptionEvt(ExceptionEvtV1.parseFrom(x))
//  }
//
//  def toUpdatedDataElementEvt(x: UpdatedDataElementEvt): UpdatedDataElementEvtV1 = {
//    UpdatedDataElementEvtV1(x.dataElement)
//  }
//
//  def fromUpdatedDataElementEvt(x: UpdatedDataElementEvtV1): UpdatedDataElementEvt = {
//    UpdatedDataElementEvt(x.dataElement)
//  }
//
//  def toUpdatedDataEvt(x: UpdatedDataEvt): UpdatedDataEvtV1 = {
//    UpdatedDataEvtV1(x.data)
//  }
//
//  def fromUpdatedDataEvt(x: UpdatedDataEvtV1): UpdatedDataEvt = {
//    UpdatedDataEvt(x.data)
//  }
//
//  implicit def toData(x: Data): DataV1 = {
//    DataV1(elements = x.elements.mapValues(toDataElementValue))
//  }
//
//  implicit def fromData(x: DataV1): Data = {
//    Data(elements = x.elements.mapValues(fromDataElementValue))
//  }
//
//  implicit def toDataElement(x: DataElement): DataElementV1 = {
//    DataElementV1(
//      path = x.path,
//      value = x.value)
//  }
//
//  implicit def fromDataElement(x: DataElementV1): DataElement = {
//    DataElement(
//      path = x.path,
//      value = x.value)
//  }
//
//  def toExceptionEvt(x: ExceptionEvt): ExceptionEvtV1 = {
//    ExceptionEvtV1(
//      x = x.x)
//  }
//
//  def fromExceptionEvt(x: ExceptionEvtV1): ExceptionEvt = {
//    ExceptionEvt(
//      x = x.x)
//  }
//
//  implicit def toDataElementValue(x: Value): DataElementValueV1 = {
//    x match {
//      case y: String => DataElementValueV1.defaultInstance.withOpt1(y)
//      case y: Int => DataElementValueV1.defaultInstance.withOpt2(y)
//      case y: Boolean => DataElementValueV1.defaultInstance.withOpt3(y)
//      case y: BigDecimal => DataElementValueV1.defaultInstance.withOpt4(y)
//    }
//  }
//
//  implicit def fromDataElementValue(x: DataElementValueV1): Value = {
//    val opt1 = x.dataElementValueOneof.opt1
//    val opt2 = x.dataElementValueOneof.opt2
//    val opt3 = x.dataElementValueOneof.opt3
//    val opt4 = x.dataElementValueOneof.opt4
//    Seq(opt1, opt2, opt3, opt4).flatten.head match {
//      case y: String => y
//      case y: Int => y
//      case y: Boolean => y
//      case y: DecimalV1 => BigDecimal(y.x)
//    }
//  }
//
//  implicit def toDecimal(x: BigDecimal): DecimalV1 = {
//    DecimalV1(x.toString)
//  }
//
//  implicit def fromDecimal(x: DecimalV1): BigDecimal = {
//    x.x
//  }
//}