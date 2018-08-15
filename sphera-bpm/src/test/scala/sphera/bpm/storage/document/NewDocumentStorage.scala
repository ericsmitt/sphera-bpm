package sphera.bpm.storage.document

import akka.testkit.TestKit
import sphera.bpm.json._
import sphera.bpm.process.NewDefStorageBuilder
import sphera.bpm.storage.CreateNodeInfo
import sphera.bpm.storage.definition.{ DefStorage, PatchDef, RootDef }
import sphera.bpm.storage.json.JsonStorageLike
import sphera.core.test.PersistenceSpec

import scala.io.Source

trait NewDocumentStorage extends NewDefStorageBuilder { _: PersistenceSpec with TestKit =>
  val documentResource = "/storage/document/Document.json"

  val documentStr = Source.fromURL(getClass.getResource(documentResource)).mkString
  val document = parse(documentStr)

  log.info(s"parse $documentResource")

  def generateCreateNodeInfo(storage: DefStorage): CreateNodeInfo = {
    val i = generateInt
    CreateNodeInfo(
      name = s"name-$i",
      description = Some(s"desc-$i"),
      storage = DocumentStorage(storage))
  }
}
