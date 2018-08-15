package sphera.bpm.storage.definition

import sphera.core.akkaext.actor.{ DataPath, Path, RawPath }

case class DefIndex(id: DefIndex.Id, elements: Map[DefIndexElement.Id, DefIndexElement]) {
  def addElement(element: DefIndexElement): DefIndex = {
    val elementId = element.path.dataPath.raw
    copy(elements = elements + (elementId -> element))
  }

  def addElements(that: DefIndex): DefIndex = {
    (this /: that.elements)({ case (acc, (elementId, element)) => acc.addElement(element) })
  }
}

object DefIndex {
  type Id = RawPath

  def apply(id: DefIndex.Id, element: DefIndexElement): DefIndex = {
    DefIndex(
      id = id,
      elements = Map(element.path.raw -> element))
  }
}

trait DefIndexElement {
  def value: Value
  def path: Path
  def indexId: DataPath = path.dataPath >/ "../*"
  def indexIdStr: String = indexId.raw

  //  override def toString = this.asJson.spaces2
}

case class IntDefIndexElement(value: Int, path: Path) extends DefIndexElement
case class StringDefIndexElement(value: String, path: Path) extends DefIndexElement
case class BooleanDefIndexElement(value: Boolean, path: Path) extends DefIndexElement
case class DecimalDefIndexElement(value: BigDecimal, path: Path) extends DefIndexElement

object DefIndexElement {
  type Id = RawPath
}