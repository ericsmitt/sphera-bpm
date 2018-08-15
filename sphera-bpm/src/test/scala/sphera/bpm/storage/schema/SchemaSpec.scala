package sphera.bpm.storage.schema

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.storage.definition.{ DefSchemaStorage, DefStorage, RoleDef }
import org.scalatest.WordSpecLike
import sphera.bpm.storage.schema.JsonSchema._
import sphera.core.domain.authorization.model.Role

class SchemaSpec extends TestKit(ActorSystem("SchemaSpec")) with SchemaValidatorSupport with WordSpecLike {
  implicit def log = system.log
  "A Schema" when {
    //    "RolesDef" must {
    //      "derive schema" in {
    //        case class User(name: String)
    //        val x = deriveFor[User]
    //        println(x)
    //        succeed
    //      }
    //    }
    "EmptyRootDef" must {
      "validate" in {
        val defSchemaStorage = DefSchemaStorage.emptyRootDef(false)
        val defStorage = DefStorage.emptyRootDef(mutable = false, Some(defSchemaStorage))
        validate(defStorage.repr, defSchemaStorage.repr)
      }
    }
  }
}