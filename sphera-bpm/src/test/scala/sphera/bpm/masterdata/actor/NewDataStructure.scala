package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.masterdata.model.{ModifyAttr, _}
import sphera.bpm.model.Language
import sphera.core.utils.Generator

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import sphera.bpm.Implicits._

trait NewDataStructure extends Generator {
  implicit def executionContext: ExecutionContext
  def generateDataStructureElements(min: Int = 1, max: Int = 10): Map[String, DataStructureElement] = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      val id = generateId
      (id, StringDataStructureElement(s"str-$id", Option(s"desc-$id"), Some(s"val-$id")))
    }).toMap
  }
  def generateDataStructure(id: DataStructure.Id = generateUUID): DataStructure = {
    DataStructure(
      id = id,
      name = s"p2-$id",
      description = Option(s"desc-$id"),
      elements = generateDataStructureElements(),
      mutable = false,
      modifyAttr = ModifyAttr(
        generateUUID,
        ZonedDateTime.now()
      )
    )
  }
  def generateDataStructures(min: Int = 1, max: Int = 10): Future[Map[DataStructure.Id, DataStructure]] = Future {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    (for (i <- 1 to n) yield {
      val id = generateUUID
      (id, generateDataStructure(id))
    }).toMap
  }

  val stringDataStructureElementIdA: String = generateId
  val stringDataStructureElementTupleA: (String, StringDataStructureElement) = {
    val id = stringDataStructureElementIdA
    id -> StringDataStructureElement(id, Option(s"desc-$id"), Option(s"val-$id"))
  }

  val stringDataStructureElementIdB: String = generateId
  val stringDataStructureElementTupleB: (String, StringDataStructureElement) = {
    val id = stringDataStructureElementIdB
    id -> StringDataStructureElement(id, Option(s"desc-$id"), Option(s"val-$id"))
  }

  val decimalDataStructureElementIdA: String = generateId
  val decimalDataStructureElementTupleA: (String, DecimalDataStructureElement) = {
    val id = decimalDataStructureElementIdA
    id -> DecimalDataStructureElement(id, Option(s"desc-$id"), Option(BigDecimal(generateInt)))
  }

  val decimalDataStructureElementIdB: String = generateId
  val decimalDataStructureElementTupleB: (String, DecimalDataStructureElement) = {
    val id = decimalDataStructureElementIdB
    id -> DecimalDataStructureElement(id, Option(s"desc-$id"), Option(BigDecimal(generateInt)))
  }

  val booleanDataStructureElementIdA: String = generateId
  val booleanDataStructureElementTupleA: (String, BooleanDataStructureElement) = {
    val id = booleanDataStructureElementIdA
    id -> BooleanDataStructureElement(id, Option(s"desc-$id"), Option(false))
  }

  val booleanDataStructureElementIdB: String = generateId
  val booleanDataStructureElementTupleB: (String, BooleanDataStructureElement) = {
    val id = booleanDataStructureElementIdB
    id -> BooleanDataStructureElement(id, Option(s"desc-$id"), Option(false))
  }

  val dataElementIdsA = Seq(
    stringDataStructureElementIdA,
    booleanDataStructureElementIdA,
    decimalDataStructureElementIdA
  )

  val dataElementIdsB = Seq(
    stringDataStructureElementIdB,
    booleanDataStructureElementIdB,
    decimalDataStructureElementIdB
  )

  val dataElementIds: Seq[String] = dataElementIdsA ++ dataElementIdsB

  val dataStructureIdA: UUID = generateUUID
  val dataStructureA: DataStructure = {
    val id = dataStructureIdA
    val elements = Map[String, DataStructureElement](
      stringDataStructureElementTupleA,
      decimalDataStructureElementTupleA,
      booleanDataStructureElementTupleA
    )
    DataStructure(
      id = id,
      name = s"p2-$id",
      description = Option(s"desc-$id"),
      elements = elements,
      mutable = false,
      modifyAttr = ModifyAttr(
        generateUUID,
        ZonedDateTime.now()
      )
    )
  }

  val dataStructureIdB: UUID = generateUUID
  val dataStructureB: DataStructure = {
    val id = dataStructureIdB
    val elements = Map[String, DataStructureElement](
      stringDataStructureElementTupleB,
      decimalDataStructureElementTupleB,
      booleanDataStructureElementTupleB
    )
    DataStructure(
      id = id,
      name = s"p2-$id",
      description = Option(s"desc-$id"),
      elements = elements,
      mutable = false,
      modifyAttr = ModifyAttr(
        generateUUID,
        ZonedDateTime.now()
      )
    )
  }

  val objectDataStructureElementIdA: String = generateId
  val objectDataStructureElementTupleA: (String, ObjectDataStructureElement) = {
    val id = objectDataStructureElementIdA
    id -> ObjectDataStructureElement(id, Option(s"desc-$id"), dataStructureIdA)
  }

  val objectDataStructureElementIdB: String = generateId
  val objectDataStructureElementTupleB: (String, ObjectDataStructureElement) = {
    val id = objectDataStructureElementIdB
    id -> ObjectDataStructureElement(id, Option(s"desc-$id"), dataStructureIdB)
  }

  val dataStructureIdC: UUID = generateUUID
  val dataStructureC: DataStructure = {
    val id = dataStructureIdC
    val elements = Map[String, DataStructureElement](
      objectDataStructureElementTupleA,
      objectDataStructureElementTupleB
    )
    DataStructure(
      id,
      s"p2-$id",
      Option(s"desc-$id"),
      elements,
      mutable = false,
      modifyAttr = ModifyAttr(
        generateUUID,
        ZonedDateTime.now()
      )
    )
  }

  val decimalDataStructureElementIdD: String = generateId
  val decimalDataStructureElementTupleD: (String, DecimalDataStructureElement) = {
    val id = decimalDataStructureElementIdD
    id -> DecimalDataStructureElement(id, Option(s"desc-$id"), None)
  }

  val dataStructureIdD: UUID = generateUUID
  val dataStructureD: DataStructure = {
    val id = dataStructureIdD
    val elements = Map[String, DataStructureElement](
      stringDataStructureElementTupleA,
      decimalDataStructureElementTupleD
    )
    DataStructure(
      id = id,
      name = s"p2-$id",
      description = Option(s"desc-$id"),
      elements = elements,
      mutable = false,
      modifyAttr = ModifyAttr(
        generateUUID,
        ZonedDateTime.now()
      )
    )
  }

  val objectDataStructureElementIdC: String = generateId
  val objectDataStructureElementTupleC: (String, ObjectDataStructureElement) = {
    val id = objectDataStructureElementIdC
    id -> ObjectDataStructureElement(id, Option(s"desc-$id"), dataStructureIdC)
  }

  val dataStructures = Map(
    dataStructureIdA -> dataStructureA,
    dataStructureIdB -> dataStructureB,
    dataStructureIdC -> dataStructureC,
  )

  val dataStructuresWithD = dataStructures + (dataStructureD.id -> dataStructureD)

  val data = Map(
    stringDataStructureElementIdA -> "strA",
    decimalDataStructureElementIdA -> BigDecimal(1000),
    booleanDataStructureElementIdA -> true
  )

  val dataD = Map(
    "a.b.c" -> true,
    "b.a.c" -> true,
    "a.c" -> true,
    "a.dateTime" -> true,
    "b.c.a" -> true
  )

  val stringDataStructureElementL1IdA = s"$objectDataStructureElementIdA.$stringDataStructureElementIdA"
  val decimalDataStructureElementL1IdA = s"$objectDataStructureElementIdA.$decimalDataStructureElementIdA"
  val booleanDataStructureElementL1IdA = s"$objectDataStructureElementIdA.$booleanDataStructureElementIdA"

  val dataL1 = Map(
    stringDataStructureElementL1IdA -> "strA",
    decimalDataStructureElementL1IdA -> BigDecimal(1000),
    booleanDataStructureElementL1IdA -> true
  )

  val secretaryDataStructureId: UUID = generateUUID
  val secretaryDataStructure = UpdateDataStructure(
    id = secretaryDataStructureId,
    name = "secretary",
    description = None,
    elements = Map(
      "firstname" -> StringDataStructureElement("firstname", None, Option("Mark"), indexed = true),
      "lastname" -> StringDataStructureElement("lastname", None, Option("Duberg"), indexed = true),
      "middlename" -> StringDataStructureElement("middlename", None, Option("A."), indexed = true),
      "fullname" -> StringDataStructureElement("fullname", None, None),
      "changeable" -> BooleanDataStructureElement("changeable", None, Option(true), indexed = true),
    ),
    mutable = true,
    userId = generateUUID
  )

  val filesDataStructureIdA: UUID = generateUUID
  val filesDataStructureA = UpdateDataStructure(
    id = filesDataStructureIdA,
    name = "files",
    description = None,
    elements = Map(
      "reportA" -> StringDataStructureElement("reportA", None, Option(generateFileName("docx"))),
      "reportB" -> StringDataStructureElement("reportB", None, Option(generateFileName("docx"))),
      "reportC" -> StringDataStructureElement("reportC", None, Option(generateFileName("docx"))),
      "reportD" -> StringDataStructureElement("reportD", None, Option(generateFileName("docx"))),
    ),
    mutable = true,
    userId = generateUUID
  )

  val filesDataStructureIdB: UUID = generateUUID
  val filesDataStructureB = UpdateDataStructure(
    id = filesDataStructureIdB,
    name = "files",
    description = None,
    elements = Map(
      "reportA" -> StringDataStructureElement("reportA", None, Option(generateFileName("pdf"))),
      "reportB" -> StringDataStructureElement("reportB", None, Option(generateFileName("pdf"))),
      "reportC" -> StringDataStructureElement("reportC", None, Option(generateFileName("pdf"))),
      "reportD" -> StringDataStructureElement("reportD", None, Option(generateFileName("pdf"))),
    ),
    mutable = true,
    userId = generateUUID
  )

  val userDataStructureIdStr: String = "6de4e1c7-0a28-4a98-ae75-db02ef76ea0b"
  val userDataStructureId: UUID = userDataStructureIdStr
  val userDataStructure = generateUserDataStructure(userDataStructureId)

  def generateUserDataStructure(dataStructureId: DataStructure.Id = generateUUID) = UpdateDataStructure(
    id = dataStructureId,
    name = "user",
    description = None,
    elements = Map(
      "firstname" -> StringDataStructureElement("firstname", None, Some(generateString(10)), indexed = true),
      "lastname" -> StringDataStructureElement("lastname", None, Some(generateString(10)), indexed = true),
      "middlename" -> StringDataStructureElement("middlename", Some(generateString(10)), None, indexed = true),
      "age" -> IntDataStructureElement("age", None, Some(generateInt(15, 45)), indexed = true),
      "email" -> StringDataStructureElement("email", Some(generateEmail), None, indexed = true),
      "phone" -> StringDataStructureElement("phone", None, Some(generatePhone))
    ),
    mutable = true,
    userId = generateUUID
  )

  val usersDataStructureId: UUID = generateUUID
  val usersDataStructure = UpdateDataStructure(
    id = usersDataStructureId,
    name = "users",
    description = None,
    elements = Map(
      userDataStructureIdStr -> ObjectDataStructureElement(
        name = userDataStructureIdStr,
        description = None,
        dataStructureId = userDataStructureId)
    ),
    mutable = true,
    userId = generateUUID
  )

  val checkingUser = generateUUID
  val checkingUserRole = RoleDataStructureElement(
    name = "checkingUser",
    description = Some("Resident checking user"),
    defaultValue = Option(checkingUser),
    typeId = generateUUID
  )

  val candidateUser = generateUUID
  val candidateRole = RoleDataStructureElement(
    name = "candidate",
    description = Some("Candidate"),
    defaultValue = Option(candidateUser),
    typeId = generateUUID
  )
  val residentGroupDataStructure = RoleGroupDataStructureElement(
    name = "resident",
    description = Some("Resident group"),
    defaultValue = Some(Map(
      "checkingUser" -> checkingUserRole,
      "candidate" -> candidateRole
    ))
  )

  val rolesDataStructureId: UUID = generateUUID
  val rolesDataStructure = RolesDataStructureElement(
    name = "roles",
    description = Some("Process roles"),
    defaultValue = Some(Map("resident" -> residentGroupDataStructure))
  )

  val processDataStructureId: UUID = generateUUID
  val processDataStructure = UpdateDataStructure(
    id = processDataStructureId,
    name = "process",
    description = None,
    elements = Map(
      "secretary" -> ObjectDataStructureElement(
        name = "secretary",
        description = None,
        dataStructureId = secretaryDataStructureId
      ),
      "company" -> StringDataStructureElement("company", None, None),
      "title" -> MLStringDataStructureElement(
        name = "title",
        description = None,
        defaultValue = Some(Map(
          Language.RU -> StringDataStructureElement(Language.RU, None, Option("Название")),
          Language.EN -> StringDataStructureElement(Language.EN, None, Option("Title"))
        ))),
      "employees" -> DecimalDataStructureElement("employees", None, Option(10)),
      "email" -> StringDataStructureElement("email", None, Some(generateEmail)),
      "phone" -> StringDataStructureElement("phone", None, Some(generatePhone)),
      "approved" -> BooleanDataStructureElement("approved", None, None),
      "rejectReason" -> StringDataStructureElement("rejectReason", None, None),
      "changeable" -> BooleanDataStructureElement("changeable", None, Option(true)),
      "files" -> ObjectDataStructureElement(
        name = "files",
        description = None,
        dataStructureId = filesDataStructureIdA
      ),
      "comment" -> StringDataStructureElement("comment", None, Option("comment")),

      // Users as reference
      "users" -> RefDataStructureElement(
        name = "users",
        description = None,
        dataStructureId = usersDataStructureId
      ),

      "subject1" -> StringDataStructureElement("subject1", None, Option("Регистрация")),
      "subject2" -> StringDataStructureElement("subject2", None, Option("Подтверждение регистрации")),
      "msg1" -> StringDataStructureElement("msg1", None, None),
      "msg2" -> StringDataStructureElement("msg2", None, None),
      "roles" -> rolesDataStructure,
    ),
    mutable = false,
    userId = generateUUID
  )

  val formDataStructureIdA: UUID = generateUUID
  val formDataStructureA = UpdateDataStructure(
    id = formDataStructureIdA,
    name = "Form",
    description = None,
    elements = Map(
      "firstname" -> StringDataStructureElement("firstname", None, None),
      "lastname" -> StringDataStructureElement("lastname", None, None),
      "middlename" -> StringDataStructureElement("middlename", None, None),
      "fullname" -> StringDataStructureElement("fullname", None, None),
      "changeable" -> BooleanDataStructureElement("changeable", None, Option(false)),
      "role" -> StringDataStructureElement("role", None, Option("user")),
      "reports" -> ObjectDataStructureElement(
        name = "reports",
        description = None,
        dataStructureId = filesDataStructureIdB
      ),
      "counter" -> DecimalDataStructureElement("counter", None, Option(0))
    ),
    mutable = false,
    userId = generateUUID
  )

  val formDataStructureIdB: UUID = generateUUID
  val formDataStructureB = UpdateDataStructure(
    id = formDataStructureIdB,
    name = "Form",
    description = None,
    elements = Map(
      "company" -> StringDataStructureElement("company", None, None),
      "title" -> MLStringDataStructureElement(
        name = "title",
        description = None,
        defaultValue = Some(Map(
          Language.RU -> StringDataStructureElement(Language.RU, None, Option("Название")),
          Language.EN -> StringDataStructureElement(Language.EN, None, Option("Title"))
        ))),
      "employees" -> DecimalDataStructureElement("employees", None, Option(10)),
      "email" -> StringDataStructureElement("email", None, None),
      "phone" -> StringDataStructureElement("phone", None, None),
    ),
    mutable = false,
    userId = generateUUID
  )

  val formDataStructureIdC: UUID = generateUUID
  val formDataStructureC = UpdateDataStructure(
    id = formDataStructureIdC,
    name = "Form approve",
    description = None,
    elements = Map(
      "approved" -> BooleanDataStructureElement("approved", None, Option(false)),
      "rejectReason" -> StringDataStructureElement("rejectReason", None, Option("reject reason")),
      "filledForm" -> ObjectDataStructureElement(
        name = "filledForm",
        description = None,
        dataStructureId = formDataStructureIdB
      )
    ),
    mutable = false,
    userId = generateUUID
  )
}