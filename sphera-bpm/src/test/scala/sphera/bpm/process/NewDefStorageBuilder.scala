package sphera.bpm.process

import akka.testkit.TestKit
import sphera.bpm.storage.definition.DefStorageBuilder
import sphera.bpm.{ Bpm, BpmBaseSpec }
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future

trait NewDefStorageBuilder extends BpmBaseSpec { _: PersistenceSpec with TestKit =>
  def newDefStorageBuilder(): Future[DefStorageBuilder] = newBpm().map(newDefStorageBuilder)

  def newDefStorageBuilder(bpm: Bpm): DefStorageBuilder = {
    val x = bpm
    val timeout = t
    new DefStorageBuilder {
      val id = generateActorId
      def bpm = x
      implicit val c = executionContext
      implicit val t = timeout
      implicit def log = system.log
    }
  }
}
