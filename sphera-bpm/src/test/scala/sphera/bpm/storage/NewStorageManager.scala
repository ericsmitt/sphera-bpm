package sphera.bpm.storage

import akka.testkit.TestKit
import sphera.bpm.json._
import sphera.bpm.process.NewDefStorageBuilder
import sphera.bpm.storage.definition.{ DefStorage, PatchDef, RootDef }
import sphera.bpm.storage.json.JsonStorageLike
import sphera.core.test.PersistenceSpec

import scala.io.Source

trait NewStorageManager extends NewDefStorageBuilder { _: PersistenceSpec with TestKit =>
  val documentDefResource = "/storage/definition/DocumentDef.json"
  val documentDefPatchResource = "/storage/definition/DocumentDefPatch.json"

  val documentDefStr = Source.fromURL(getClass.getResource(documentDefResource)).mkString
  val documentDef = parse(documentDefStr).as[RootDef].right.get

  log.info(s"parse $documentDefResource")

  val documentDefPatchStr = Source.fromURL(getClass.getResource(documentDefPatchResource)).mkString
  val documentDefPatch = parse(documentDefPatchStr).as[PatchDef] match {
    case Right(x) => x
    case Left(e) => throw e
  }

  log.info(s"parse $documentDefPatchResource")

  def generateCreateNodeInfo(storage: JsonStorageLike): CreateNodeInfo = {
    val i = generateInt
    CreateNodeInfo(
      name = s"name-$i",
      description = Some(s"desc-$i"),
      storage = storage)
  }
}
