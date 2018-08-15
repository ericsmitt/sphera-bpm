package sphera.bpm.storage.json

import io.circe.Json
import org.scalatest.{ Matchers, WordSpec }

class JsonStorageSpec extends WordSpec with Matchers with NewJsonStorage {
  "A JsonStorage" must {
    "update jsonstorage" in {
      val x = jsonStorage
        .updateJson(jsonA)
        .updateJson("order.items[1].id", Json.fromInt(1))

      x.getStringOpt("order.customer.name") should contain("updated")
      x.getIntOpt("order.total") should contain(150)
      x.getIntOpt("order.items[1].id") should contain(1)
    }
    "delete jsonstorage" in {
      val x = jsonStorage.deleteJson("order.customer.contactDetails")

      x.exists("order.customer.contactDetails") shouldBe false
    }
  }
}