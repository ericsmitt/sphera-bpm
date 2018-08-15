package sphera.bpm.storage.definition

import java.util.UUID

import sphera.bpm.Implicits._
import sphera.bpm.json._
import sphera.bpm.masterdata.model._
import sphera.bpm.storage.schema.JsonSchema._
import sphera.bpm.storage.schema.{ JsonSchema, SchemaValidatorSupport }
import sphera.bpm.storage.{ CreateNodeInfo, Node, NodeInfo }
import sphera.bpm.utils.Printable
import sphera.bpm.{ Bpm, DefStorageBuilderException, UnknownDataStructureElementException }
import sphera.core.akkaext.actor._
import io.circe.Json

import scala.concurrent.Future
import scala.reflect.runtime.universe._

/**
 * Рекурсивно генерериет структуру и схему структуры.
 * Заполняет структуру данными и проверяет по схеме.
 * В результате получае хранилище [[DefStorage]] c заполненными данными.
 */
trait DefStorageBuilder extends SchemaValidatorSupport with FutureSupport with Printable {
  val id: Node.Id

  def bpm: Bpm

  def mapToRoleGroup(element: RoleGroupDataStructureElement, path: DataPath): RoleGroupDef = {
    RoleGroupDef(
      name = element.name,
      description = element.description,
      value = {
        element
          .defaultValue
          .map { roles =>
            roles.mapValues { role =>
              RoleDef(
                name = role.name,
                description = role.description,
                value = role.defaultValue.getOrElse(UUID.randomUUID()),
                path = path / element.name / role.name)
            }
          }
          .getOrElse(Map[String, RoleDef]())

      },
      path = path / element.name)
  }

  def mapToRoleGroups(elements: Map[String, RoleGroupDataStructureElement], path: DataPath): Map[String, RoleGroupDef] = {
    elements.mapValues(mapToRoleGroup(_, path))
  }

  def combine[T](
    acc: Future[DefStorage],
    name: String,
    defOrJson: Either[Def, Json],
    schemaOrJson: Either[JsonSchema[T], Json],
    indexes: DefIndexStorage)(implicit tag: WeakTypeTag[T]): Future[DefStorage] = {

    (defOrJson, schemaOrJson) match {
      case (Left(x), Left(y)) => acc.map {
        _.addDef(name, x)
          .mapIndexes(_.addIndexes(indexes))
          .mapSchema(_.addDefSchema(name, y)(tag))
      }
      case (Right(x), Right(y)) => acc.map {
        _.addDef(name, x)
          .mapIndexes(_.addIndexes(indexes))
          .mapSchema(_.addDefSchema(name, y))
      }
      case _ => throw DefStorageBuilderException(s"Combine exception ($defOrJson, $schemaOrJson)")
    }
  }

  def mapToObject(
    dataStructureElement: ObjectDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath) = {

    val dataStructure = dataStructures(dataStructureElement.dataStructureId)

    val mutable = dataStructure.mutable

    val trAcc = DefStorage(
      repr = Json.obj(),
      schema = DefSchemaStorage.emptyObjectDef(mutable))

    traverse(
      dataStructure = dataStructure,
      dataStructures = dataStructures,
      data = data,
      acc = trAcc,
      accIndexPath = indexPath,
      accPath = path).flatMap {
      case DefStorage(repr, indexes, schema) =>
        val objectDef = ObjectDef(
          name = dataStructureElement.name,
          description = dataStructureElement.description,
          mutable = mutable,
          value = Map.empty,
          path = path)

        val x1 = objectDef
          .addJson(repr)
          .asJson

        val x2 = schema.get.repr

        combine(
          acc = acc,
          name = dataStructureElement.name,
          defOrJson = Right(x1),
          schemaOrJson = Right(x2),
          indexes = indexes)
    }
  }

  def mapInt(
    dataStructureElement: IntDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath) = {

    val value = dataStructureElement.defaultValue.getOrElse(0)

    val indexOpt = if (dataStructureElement.indexed) {
      Some(IntDefIndexElement(
        value = value,
        path = indexPath))
    } else None

    val indexes = indexOpt
      .map(DefIndexStorage.apply)
      .getOrElse(DefIndexStorage.empty)

    val x1 = IntDef(
      name = dataStructureElement.name,
      description = dataStructureElement.description,
      index = indexOpt.map(_.indexId),
      value = value,
      path = path)

    val x2 = intDefSchema

    combine(
      acc = acc,
      name = dataStructureElement.name,
      defOrJson = Left(x1),
      schemaOrJson = Left(x2),
      indexes = indexes)
  }

  def mapString(
    dataStructureElement: StringDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath): Future[DefStorage] = {

    val value = dataStructureElement.defaultValue.getOrElse("")

    val indexOpt = if (dataStructureElement.indexed) {
      Some(StringDefIndexElement(
        value = value,
        path = indexPath))
    } else None

    val indexes = indexOpt
      .map(DefIndexStorage.apply)
      .getOrElse(DefIndexStorage.empty)

    val x1 = StringDef(
      name = dataStructureElement.name,
      description = dataStructureElement.description,
      index = indexOpt.map(_.indexId),
      value = value,
      path = path)

    val x2 = stringDefSchema

    combine(
      acc = acc,
      name = dataStructureElement.name,
      defOrJson = Left(x1),
      schemaOrJson = Left(x2),
      indexes = indexes)
  }

  def mapDecimal(
    dataStructureElement: DecimalDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath) = {

    val value = dataStructureElement.defaultValue.getOrElse(BigDecimal(0))

    val indexOpt = if (dataStructureElement.indexed) {
      Some(DecimalDefIndexElement(
        value = value,
        path = indexPath))
    } else None

    val indexes = indexOpt
      .map(DefIndexStorage.apply)
      .getOrElse(DefIndexStorage.empty)

    val x1 = DecimalDef(
      name = dataStructureElement.name,
      description = dataStructureElement.description,
      index = indexOpt.map(_.indexId),
      value = value,
      path = path)

    val x2 = decimalDefSchema

    combine(
      acc = acc,
      name = dataStructureElement.name,
      defOrJson = Left(x1),
      schemaOrJson = Left(x2),
      indexes = indexes)
  }

  def mapBoolean(
    dataStructureElement: BooleanDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath) = {

    val value = dataStructureElement.defaultValue.getOrElse(false)

    val indexOpt = if (dataStructureElement.indexed) {
      Some(BooleanDefIndexElement(
        value = value,
        path = indexPath))
    } else None

    val indexes = indexOpt
      .map(DefIndexStorage.apply)
      .getOrElse(DefIndexStorage.empty)

    val x1 = BooleanDef(
      name = dataStructureElement.name,
      description = dataStructureElement.description,
      index = indexOpt.map(_.indexId),
      value = value,
      path = path)

    val x2 = booleanDefSchema

    combine(
      acc = acc,
      name = dataStructureElement.name,
      defOrJson = Left(x1),
      schemaOrJson = Left(x2),
      indexes = indexes)
  }

  def mapToMLStringElement(element: StringDataStructureElement, path: DataPath): StringDef = {
    StringDef(
      name = element.name,
      description = element.description,
      index = None,
      value = element.defaultValue.getOrElse(""),
      path = path / element.name)
  }

  def mapToMLStringElements(elements: Map[String, StringDataStructureElement], path: DataPath): Map[String, StringDef] = {
    elements.mapValues(mapToMLStringElement(_, path))
  }

  def mapMLString(
    dataStructureElement: MLStringDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath) = {

    val x1 = MLStringDef(
      name = dataStructureElement.name,
      description = dataStructureElement.description,
      value = dataStructureElement.defaultValue
        .map(mapToMLStringElements(_, path))
        .getOrElse(Map.empty),
      path = path)

    val x2 = mlStringDefSchema

    combine(
      acc = acc,
      name = dataStructureElement.name,
      defOrJson = Left(x1),
      schemaOrJson = Left(x2),
      indexes = DefIndexStorage.empty)
  }

  def mapRoles(
    dataStructureElement: RolesDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    path: DataPath) = {

    val x1 = RolesDef(
      name = dataStructureElement.name,
      description = dataStructureElement.description,
      value = dataStructureElement.defaultValue
        .map(mapToRoleGroups(_, path))
        .getOrElse(Map.empty),
      path = path)

    val x2 = rolesDefSchema

    combine(
      acc = acc,
      name = dataStructureElement.name,
      defOrJson = Left(x1),
      schemaOrJson = Left(x2),
      indexes = DefIndexStorage.empty)
  }

  def mapRef(
    dataStructureElement: RefDataStructureElement,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: Future[DefStorage],
    indexPath: Path,
    path: DataPath) = {

    /**
     * Проверяем существование узла, если нет создаем его и заполняем данными
     */
    def buildRef: Future[(Path, DefIndexStorage)] = dataStructureElement.defaultValue match {
      case Some(p) => Future.successful((p, DefIndexStorage.empty))
      case None =>
        // Build referenced structure
        def buildRefDefStorage: Future[DefStorage] = {
          val dataStructure = dataStructures(dataStructureElement.dataStructureId)
          val mutable = dataStructure.mutable
          val rootDef = RootDef(
            name = dataStructureElement.name,
            description = dataStructureElement.description,
            mutable = mutable)

          build(
            dataStructureId = dataStructureElement.dataStructureId,
            data = data(path).getOrElse(Json.obj()),
            accIndexPath = indexPath,
            accPath = path,
            rootDef = Some(rootDef))
        }

        // Create storage node
        def createRefNode(defStorage: DefStorage): Future[NodeInfo] = {
          bpm.createNode(
            CreateNodeInfo(
              name = dataStructureElement.name,
              description = dataStructureElement.description,
              storage = defStorage))
        }

        for {
          r1 <- buildRefDefStorage
          r2 <- createRefNode(r1)
        } yield {
          val indexes = r1.indexes
          (Path(r2.id), r1.indexes)
        }
    }

    buildRef.flatMap {
      case (refPath, indexes) =>
        val x1 = RefDef(
          name = dataStructureElement.name,
          description = dataStructureElement.description,
          value = refPath,
          path = path)

        val x2 = refDefSchema

        combine(
          acc = acc,
          name = dataStructureElement.name,
          defOrJson = Left(x1),
          schemaOrJson = Left(x2),
          indexes = indexes)
    }
  }

  /**
   * Recursive traverse [[DataStructure]].
   */
  def traverse(
    dataStructure: DataStructure,
    dataStructures: Map[DataStructure.Id, DataStructure],
    data: Json,
    acc: DefStorage,
    accIndexPath: Path,
    accPath: DataPath): Future[DefStorage] = {

    def op(acc: Future[DefStorage], element: DataStructureElement): Future[DefStorage] = {
      val path = accPath / element.name

      log.info(s"build [$path]")

      element match {
        case x: IntDataStructureElement => mapInt(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath / path,
          path = path)
        case x: StringDataStructureElement => mapString(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath / path,
          path = path)
        case x: DecimalDataStructureElement => mapDecimal(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath / path,
          path = path)
        case x: BooleanDataStructureElement => mapBoolean(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath / path,
          path = path)
        case x: MLStringDataStructureElement => mapMLString(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath / path,
          path = path)
        case x: ObjectDataStructureElement => mapToObject(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath,
          path = path)
        case x: RolesDataStructureElement => mapRoles(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          path = path)
        case x: RefDataStructureElement => mapRef(
          dataStructureElement = x,
          dataStructures = dataStructures,
          data = data,
          acc = acc,
          indexPath = accIndexPath,
          path = path)
        case x =>
          throw UnknownDataStructureElementException(x)
      }
    }

    dataStructure.elements.values.foldLeft(Future.successful(acc))(op)
  }

  def build(
    dataStructureId: DataStructure.Id,
    data: Json,
    accIndexPath: Path = id,
    accPath: DataPath = RootDataPath,
    rootDef: Option[RootDef] = None): Future[DefStorage] = {

    log.info(s"build [$dataStructureId]")

    bpm.masterDataManager.getDataStructureMap.flatMap { dataStructures =>
      // root structure mutable or not
      val mutable = dataStructures(dataStructureId).mutable

      val defSchemaStorage = DefSchemaStorage.emptyRootDef(mutable)

      val defStorage = rootDef match {
        case Some(x) => DefStorage(x.asJson, defSchemaStorage)
        case None => DefStorage.emptyRootDef(mutable, Some(defSchemaStorage))
      }

      val dataStructure = dataStructures(dataStructureId)

      val tr = traverse(
        dataStructure = dataStructure,
        dataStructures = dataStructures,
        data = data,
        acc = defStorage,
        accIndexPath = accIndexPath,
        accPath = accPath)

      tr.map {
        case tr @ DefStorage(repr, indexes, Some(schema)) =>
          // println(repr)
          // print(schema)
          validate(repr, schema)
          val updated = tr.updateJson(data)
          validate(updated, schema)

          log.info(s"build ${"success".blue} [$dataStructureId], indexes: ${updated.indexes}")

          updated
      }
    }
  }

  def build(dataStructureId: DataStructure.Id): Future[DefStorage] = {
    build(dataStructureId, Json.obj())
  }

  def build(dataStructureId: Option[DataStructure.Id]): Future[DefStorage] = {
    bpm.masterDataManager.getDataStructureMap.flatMap { dataStructures =>
      dataStructureId.fold(Future.successful(DefStorage.emptyRootDef()))(build)
    }
  }

  def build(dataStructureId: Option[DataStructure.Id], data: Json): Future[DefStorage] = {
    bpm.masterDataManager.getDataStructureMap.flatMap { dataStructures =>
      dataStructureId.fold(Future.successful(DefStorage.emptyRootDef().addJson(data)))(build(_, data))
    }
  }
}