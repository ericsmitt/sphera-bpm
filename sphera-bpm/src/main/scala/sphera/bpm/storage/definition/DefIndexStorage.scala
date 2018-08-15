package sphera.bpm.storage.definition

import sphera.bpm.json._

case class DefIndexStorage(indexes: Map[DefIndex.Id, DefIndex]) {
  def addIndex(index: DefIndex): DefIndexStorage = {
    val indexOpt = indexes.get(index.id)
    indexOpt match {
      case Some(x) => copy(indexes = indexes + (x.id -> x.addElements(index)))
      case None => copy(indexes = indexes + (index.id -> index))
    }
  }

  def addIndexes(that: DefIndexStorage): DefIndexStorage = {
    (this /: that.indexes)({ case (acc, (indexId, index)) => acc.addIndex(index) })
  }

  def getIndex(indexId: DefIndex.Id): Option[DefIndex] = indexes.get(indexId)

  def map(f: DefIndexStorage => DefIndexStorage): DefIndexStorage = f(this)

  override def toString = indexes.keys.asJsonStr
}

object DefIndexStorage {
  def empty = DefIndexStorage(Map[DefIndex.Id, DefIndex]())

  def apply(element: DefIndexElement): DefIndexStorage = {
    val index = DefIndex(element.indexIdStr, element)
    val indexes = Map(element.indexIdStr -> index)
    new DefIndexStorage(indexes)
  }
}
