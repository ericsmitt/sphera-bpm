package sphera.bpm.storage.definition

import sphera.bpm.Implicits._
import sphera.bpm.json._

trait NewDefStorage {
  val contactDetailsDef = ObjectDef(
    name = "contactDetails",
    description = None,
    mutable = false,
    value = Map(
      "address" -> "1 Fake Street, London, England",
      "phone" -> "0123-456-789"),
    path = "order.customer.contactDetails")

  val customerDef = ObjectDef(
    name = "customer",
    description = None,
    mutable = false,
    value = Map(
      "name" -> "Custy McCustomer",
      "contactDetails" -> contactDetailsDef),
    path = "order.customer")

  val item1Def = ObjectDef(
    name = "items",
    description = None,
    mutable = false,
    value = Map(
      "id" -> 2123,
      "description" -> "banana",
      "quantity" -> 1),
    path = "order.items[0]")

  val item2Def = ObjectDef(
    name = "items",
    description = None,
    mutable = false,
    value = Map(
      "id" -> 456,
      "description" -> "apple",
      "quantity" -> 2),
    path = "order.items[0]")

  val itemsDef = ArrayDef(
    name = "items",
    description = None,
    value = List(item1Def, item2Def),
    path = "order.items")

  val orderDef = ObjectDef(
    name = "order",
    description = None,
    mutable = false,
    value = Map(
      "customer" -> customerDef,
      "items" -> itemsDef,
      "total" -> 123.45),
    path = "order")

  val root = RootDef(
    mutable = false,
    value = Map(
      "order" -> orderDef))

  def newDefStorage = DefStorage.emptyRootDef().addJson(root.asJson)
}