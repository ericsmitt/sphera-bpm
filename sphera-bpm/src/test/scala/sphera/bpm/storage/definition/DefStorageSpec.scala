package sphera.bpm.storage.definition

import org.scalatest.{ Matchers, WordSpec }

class DefStorageSpec extends WordSpec with Matchers with NewDefStorage {
  "A DefStorage" must {
    "getDef" in {
      val defStorage = newDefStorage.getDef("order")
      defStorage shouldBe a[ObjectDef]
    }
    "updateDefValues" in {
      val path = "order.customer.contactDetails.phone"
      val phoneDef = StringDef(
        name = "phone",
        description = None,
        index = None,
        value = "updated",
        path = "order.customer.contactDetails.phone")
      val defStorage = newDefStorage
        .updateDefValueOnly(path, phoneDef)
        .getDef(path)
    }
    "createDef" in {
      val path = "order.customer.contactDetails"
      val phoneDef = StringDef(
        name = "phone",
        description = Some("updated"),
        index = None,
        value = "updated",
        path = "order.customer.contactDetails.phone")
      val contactDetails = CreateObjectDef(
        name = "contactDetails",
        description = Some("updated"),
        mutable = false,
        value = Map(
          "phone" -> phoneDef,
          "cellphone" -> phoneDef))
      val defStorage = newDefStorage.createDef(path, contactDetails)
      val d1 = defStorage.getDef("order.customer.contactDetails.phone")
      val d2 = defStorage.getDef("order.customer.contactDetails.cellphone")
      d1.value shouldBe "updated"
      d2.value shouldBe "updated"
      d2.description should contain("updated")
    }
    "patchDef" in {
      val path = "order.customer.contactDetails"
      val phoneDef = StringDef(
        name = "phone",
        description = Some("updated"),
        index = None,
        value = "updated",
        path = "order.customer.contactDetails.phone")
      val contactDetails = CreateObjectDef(
        name = "contactDetails",
        description = Some("updated"),
        mutable = false,
        value = Map(
          "phone" -> phoneDef,
          "cellphone" -> phoneDef))
      val defStorage = newDefStorage.patchDef(path, contactDetails)
      val d1 = defStorage.getDef("order.customer.contactDetails.phone")
      val d2 = defStorage.getDef("order.customer.contactDetails.cellphone")
      d1.value shouldBe "updated"
      d2.value shouldBe "updated"
      d2.description should contain("updated")
    }
  }
}