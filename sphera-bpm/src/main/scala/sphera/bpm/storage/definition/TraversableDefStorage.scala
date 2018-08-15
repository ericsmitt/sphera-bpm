package sphera.bpm.storage.definition

import akka.actor.ActorRef
import sphera.bpm._
import sphera.bpm.model.Modifier
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.definition.DefStorage._
import sphera.core.akkaext.actor.{ DataPath, FutureSupport, Path, RootDataPath }
import io.circe.{ Json, JsonNumber, JsonObject }

import scala.concurrent.Future
import sphera.bpm.json._
import sphera.bpm.storage.NodeContainer
import sphera.bpm.storage.json.actor.JsonStorageNodeBehavior
import sphera.bpm.storage.Node
import sphera.core.domain.tenancy.model.User

import scala.collection.immutable
import scala.util.{ Failure, Success }

trait TraversableDefStorage extends NodeContainer {
  def getDef(path: Path): Future[Def] = getNode(path.nodeId).flatMap(getDef(_, path.dataPath))

  def getDef(node: ActorRef, path: DataPath): Future[Def] = getDefOpt(node, path).map {
    case Some(y) => y
    case None => throw DefNotFoundException(path)
  }

  def getDefOpt(path: Path): Future[Option[Def]] = getNode(path.nodeId).flatMap(getDefOpt(_, path.dataPath))

  def getDefOpt(node: ActorRef, path: DataPath): Future[Option[Def]] =
    ask(node, GetDef(path))
      .mapTo[DefOpt]
      .map(_.x)

  def getDefIntValue(path: Path): Future[Int] = getNode(path.nodeId).flatMap(getDefIntValue(_, path.dataPath))

  def getDefIntValue(node: ActorRef, path: DataPath): Future[Int] =
    ask(node, GetDefIntValue(path))
      .mapTo[DefIntValueOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw DefValueNotFoundException(path)
      }

  def getDefStringValue(path: Path): Future[String] = getNode(path.nodeId).flatMap(getDefStringValue(_, path.dataPath))

  def getDefStringValue(node: ActorRef, path: DataPath): Future[String] =
    ask(node, GetDefStringValue(path))
      .mapTo[DefStringValueOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw DefValueNotFoundException(path)
      }

  def getDefBooleanValue(path: Path): Future[Boolean] = getNode(path.nodeId).flatMap(getDefBooleanValue(_, path.dataPath))

  def getDefBooleanValue(node: ActorRef, path: DataPath): Future[Boolean] =
    ask(node, GetDefBooleanValue(path))
      .mapTo[DefBooleanValueOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw DefValueNotFoundException(path)
      }

  def getDefDecimalValue(path: Path): Future[BigDecimal] = getNode(path.nodeId).flatMap(getDefDecimalValue(_, path.dataPath))

  def getDefDecimalValue(node: ActorRef, path: DataPath): Future[BigDecimal] =
    ask(node, GetDefDecimalValue(path))
      .mapTo[DefDecimalValueOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw DefValueNotFoundException(path)
      }

  def findRoleUser(nodeId: Node.Id, role: String): Future[User.Id] = getNode(nodeId).flatMap(findRoleUser(nodeId, _, role))

  def findRoleUser(nodeId: Node.Id, node: ActorRef, role: String): Future[User.Id] =
    ask(node, FindRoleUser(role))
      .mapTo[RoleUserOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw RoleNotFoundException(nodeId, role)
      }

  def updateDefValue(path: Path, json: Json)(implicit modifier: Modifier): Future[Node.Id] =
    getNode(path.nodeId).flatMap(updateDefValue(_, path.dataPath, json))

  def updateDefValue(node: ActorRef, path: DataPath, json: Json)(implicit modifier: Modifier): Future[Node.Id] =
    updateDefValueTry(node, path, json)
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

  def updateDefValueTry(path: Path, json: Json)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    getNode(path.nodeId).flatMap(updateDefValueTry(_, path.dataPath, json))

  def updateDefValueTry(node: ActorRef, path: DataPath, json: Json)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    node.ask(UpdateDefValueCmd(path, json, modifier))
      .mapTo[NodeUpdatedTry]

  def updateDefValueTryList(path: Path, json: Json)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] =
    getNode(path.nodeId).flatMap(updateDefValueTry(_, path.dataPath, json))
      .map(List(_))

  def updateDefValueOnly(path: Path, definition: Def)(implicit modifier: Modifier): Future[Node.Id] =
    getNode(path.nodeId).flatMap(updateDefValueOnly(_, path.dataPath, definition))

  def updateDefValueOnly(node: ActorRef, path: DataPath, definition: Def)(implicit modifier: Modifier): Future[Node.Id] =
    updateDefValueOnlyTry(node, path, definition)
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

  def updateDefValueOnlyTry(path: Path, definition: Def)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    getNode(path.nodeId).flatMap(updateDefValueOnlyTry(_, path.dataPath, definition))

  def updateDefValueOnlyTryList(path: Path, definition: Def)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] =
    getNode(path.nodeId).flatMap(updateDefValueOnlyTry(_, path.dataPath, definition))
      .map(List(_))

  def updateDefValueOnlyTry(node: ActorRef, path: DataPath, definition: Def)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    node.ask(UpdateDefValueOnlyCmd(path, definition, modifier))
      .mapTo[NodeUpdatedTry]

  def createDef(path: Path, create: CreateDef)(implicit modifier: Modifier): Future[Node.Id] =
    getNode(path.nodeId).flatMap(createDef(_, path.dataPath, create))

  def createDef(node: ActorRef, path: DataPath, create: CreateDef)(implicit modifier: Modifier): Future[Node.Id] =
    createDefTry(node, path, create)
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

  def createDefTry(path: Path, create: CreateDef)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    getNode(path.nodeId).flatMap(createDefTry(_, path.dataPath, create))

  def createDefTryList(path: Path, create: CreateDef)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] =
    getNode(path.nodeId).flatMap(createDefTry(_, path.dataPath, create))
      .map(List(_))

  def createDefTry(node: ActorRef, path: DataPath, create: CreateDef)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    node.ask(CreateDefCmd(path, create, modifier))
      .mapTo[NodeUpdatedTry]

  def updateDef(path: Path, update: UpdateDef)(implicit modifier: Modifier): Future[Node.Id] =
    getNode(path.nodeId).flatMap(updateDef(_, path.dataPath, update))

  def updateDef(node: ActorRef, path: DataPath, update: UpdateDef)(implicit modifier: Modifier): Future[Node.Id] =
    updateDefTry(node, path, update)
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

  def updateDefTry(path: Path, update: UpdateDef)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    getNode(path.nodeId).flatMap(updateDefTry(_, path.dataPath, update))

  def updateDefTryList(path: Path, update: UpdateDef)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] =
    getNode(path.nodeId).flatMap(updateDefTry(_, path.dataPath, update))
      .map(List(_))

  def updateDefTry(node: ActorRef, path: DataPath, update: UpdateDef)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    node.ask(UpdateDefCmd(path, update, modifier))
      .mapTo[NodeUpdatedTry]

  def deleteDef(path: Path, delete: DeleteDef)(implicit modifier: Modifier): Future[Node.Id] =
    getNode(path.nodeId).flatMap(deleteDef(_, path.dataPath, delete))

  def deleteDef(node: ActorRef, path: DataPath, delete: DeleteDef)(implicit modifier: Modifier): Future[Node.Id] =
    deleteDefTry(node, path, delete)
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

  def deleteDefTry(path: Path, delete: DeleteDef)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    getNode(path.nodeId).flatMap(deleteDefTry(_, path.dataPath, delete))

  def deleteDefTryList(path: Path, delete: DeleteDef)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] =
    getNode(path.nodeId).flatMap(deleteDefTry(_, path.dataPath, delete))
      .map(List(_))

  def deleteDefTry(node: ActorRef, path: DataPath, delete: DeleteDef)(implicit modifier: Modifier): Future[NodeUpdatedTry] =
    node.ask(DeleteDefCmd(path, delete, modifier))
      .mapTo[NodeUpdatedTry]

  /**
   * = Traversable =
   */

  def retrieveDef(path: Path): Future[Def] = retrieveDefOpt(path).map {
    case Some(x) => x
    case None => throw DefNotFoundException(path)
  }

  /**
   * Рекурсивно извлекает ссылочные данные из хранилища.
   */
  def retrieveDefOpt(path: Path): Future[Option[Def]] = {
    def traverse(elements: Map[String, Def]): Future[Map[String, Def]] = Future.sequence {
      elements.map {
        case (name, element) => element match {
          case x: RefDef =>
            // if def is RootDef map it to ObjectDef
            getDef(x.value).map {
              case x: RootDef => name -> ObjectDef(
                name = x.name,
                description = x.description,
                mutable = x.mutable,
                value = x.value,
                path = x.path)
              case x: Def => name -> x
            }
          case x => Future.successful(name -> x)
        }
      }
    }.map(_.toMap)

    getDefOpt(path).flatMap {
      case Some(x: ObjectLikeDef) =>
        traverse(x.value)
          .map(x.withValue)
          .map(Some(_))
      case Some(x: Def) => Future.successful(Some(x))
      case None => Future.successful(None)
    }
  }

  /**
   * Рекурсивно извлекает ссылочные данные (без метаданных) из хранилища.
   */
  def retrieveDefValueOpt(path: Path): Future[Option[Json]] = {
    def mapDef(definition: Def): Future[Json] = definition.fold(
      intDef => Future.successful(intDef.value.asJson),
      stringDef => Future.successful(stringDef.value.asJson),
      booleanDef => Future.successful(booleanDef.value.asJson),
      decimalDef => Future.successful(decimalDef.value.asJson),
      refDef => Future.successful(refDef.value.asJson),
      roleDef => Future.successful(roleDef.value.asJson),
      roleGroup => traverse(roleGroup.value).map(_.asJson),
      rolesDef => traverse(rolesDef.value).map(_.asJson),
      mlStringDef => traverse(mlStringDef.value).map(_.asJson),
      arrayDef => Future.successful(arrayDef.value.asJson),
      objectDef => traverse(objectDef.value).map(_.asJson),
      rootDef => traverse(rootDef.value).map(_.asJson))

    def traverse(elements: Map[String, Def]): Future[Map[String, Json]] = {
      (Future.successful(Map[String, Json]()) /: elements) {
        case (acc, (name, element)) => for {
          r1 <- acc
          r2 <- mapDef(element)
        } yield r1 + (name -> r2)
      }
    }

    retrieveDefOpt(path).flatMap {
      case Some(x) => mapDef(x).map(Some.apply)
      case None => Future.successful(None)
    }
  }

  def writeDefValue(path: Path, json: Json)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] = {
    def mapJsonToCreateDef(name: String, element: Json): CreateDef = {
      def ifNull = CreateStringDef(
        name = name,
        description = None,
        index = None,
        value = "")

      def ifBoolean(value: Boolean) = CreateBooleanDef(
        name = name,
        description = None,
        index = None,
        value = value)

      def ifNumber(value: JsonNumber): CreateDef = {
        (value.toBigDecimal, value.toInt) match {
          case (Some(x), _) => CreateDecimalDef(
            name = name,
            description = None,
            index = None,
            value = x)
          case (_, Some(x)) => CreateIntDef(
            name = name,
            description = None,
            index = None,
            value = x)
        }
      }

      def ifString(value: String) = CreateStringDef(
        name = name,
        description = None,
        index = None,
        value = value)

      def ifArray(value: Vector[Json]) = CreateArrayDef(
        name = name,
        description = None,
        value = ???)

      def ifObject(value: JsonObject) = {
        //        parent.foldObjectLike(
        //          roleGroupDef => RoleDef(
        //            name = name,
        //            description = None,
        //            value = value,
        //            path = path / name)
        //          )
        //        )
        CreateObjectDef(
          name = name,
          description = None,
          mutable = false,
          value = Map.empty)
      }

      element.fold(
        ifNull,
        ifBoolean,
        ifNumber,
        ifString,
        ifArray,
        ifObject)
    }

    def traverse(json: Json, path: Path): Future[List[NodeUpdatedTry]] = {
      def updateElement(name: String, element: Json): Future[List[NodeUpdatedTry]] = {
        getDefOpt(path / name).flatMap {
          // if ref than write to ref node
          case Some(x: RefDef) => json.asObject match {
            case Some(_) => writeDefValue(x.value, element)
            case None => throw UpdateDefValueException(path, element)
          }
          // if def exists than write json as is
          case Some(x: Def) => writeDefValue(path / name, element)
          // if def doesn't exist than generate metadata
          case None => patchDef(path / name, mapJsonToCreateDef(name, element))
        }
      }

      (Future.successful(List[NodeUpdatedTry]()) /: json.toMap) {
        case (acc, (name, element)) => for {
          r1 <- acc
          r2 <- updateElement(name, element)
        } yield r1 ++ r2
      }
    }

    getDefOpt(path) flatMap {
      case Some(x) => x.fold(
        intDef => updateDefValueTryList(path, json),
        stringDef => updateDefValueTryList(path, json),
        booleanDef => updateDefValueTryList(path, json),
        decimalDef => updateDefValueTryList(path, json),
        refDef => updateDefValueTryList(path, json),
        roleDef => updateDefValueTryList(path, json),
        roleGroup => traverse(json, path),
        rolesDef => traverse(json, path),
        mlStringDef => traverse(json, path),
        arrayDef => updateDefValueTryList(path, json),
        objectDef => traverse(json, path),
        rootDef => traverse(json, path))
      case None => updateDefValueTryList(path, json)
    }
  }

  def traverseDef(elements: Map[String, Def], path: Path)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] = {
    def updateElement(name: String, element: Def): Future[List[NodeUpdatedTry]] = {
      getDefOpt(path / name).flatMap {
        case Some(x: RefDef) =>
          // write def if ObjectDef map it to RootDef
          element match {
            case t: ObjectDef => writeDefValueOnly(x.value, RootDef(t))
            case t: Def => throw UpdateDefValueOnlyException(path, t)
          }
        case x => writeDefValueOnly(path / name, element)
      }
    }

    (Future.successful(List[NodeUpdatedTry]()) /: elements) {
      case (acc, (name, element)) => for {
        r1 <- acc
        r2 <- updateElement(name, element)
      } yield r1 ++ r2
    }
  }

  def writeDefValueOnly(path: Path, definition: Def)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] = {
    getDefOpt(path) flatMap {
      case Some(x) => definition.fold(
        intDef => updateDefValueOnlyTryList(path, definition),
        stringDef => updateDefValueOnlyTryList(path, definition),
        booleanDef => updateDefValueOnlyTryList(path, definition),
        decimalDef => updateDefValueOnlyTryList(path, definition),
        refDef => updateDefValueOnlyTryList(path, definition),
        roleDef => updateDefValueOnlyTryList(path, definition),
        roleGroup => traverseDef(roleGroup.value, path),
        rolesDef => traverseDef(rolesDef.value, path),
        mlStringDef => traverseDef(mlStringDef.value, path),
        arrayDef => updateDefValueOnlyTryList(path, definition),
        objectDef => traverseDef(objectDef.value, path),
        rootDef => traverseDef(rootDef.value, path))
      case None => updateDefValueOnlyTryList(path, definition)
    }
  }

  def traversePatchDef(elements: Map[String, PatchDef], path: Path)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] = {
    def updateElement(name: String, element: PatchDef): Future[List[NodeUpdatedTry]] = {
      getDefOpt(path / name).flatMap {
        case Some(x: RefDef) =>
          element match {
            case read: ReadDef => Future.successful(List.empty)
            case create: CreateObjectDef => patchDef(x.value, CreateRootDef(
              name = create.name,
              description = create.description,
              mutable = create.mutable,
              value = create.value,
              path = create.path))
            case update: UpdateObjectDef => patchDef(x.value, UpdateRootDef(
              name = update.name,
              description = update.description,
              value = update.value,
              patch = update.patch,
              path = update.path))
            case delete: DeleteDef => patchDef(path, delete)
            case patch: PatchDef => throw PatchDefException(path, patch)
          }
        case x => patchDef(path / name, element)
      }
    }

    (Future.successful(List[NodeUpdatedTry]()) /: elements) {
      case (acc, (name, element)) => for {
        r1 <- acc
        r2 <- updateElement(name, element)
      } yield r1 ++ r2
    }
  }

  def patchDef(path: Path, patch: PatchDef)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] = {
    getDefOpt(path) flatMap {
      case Some(x) => x.fold(
        intDef => patch.fold(
          readDef => Future.successful(List.empty),
          createDef => sys.error("sssssssss"),
          updateDef => updateDefTryList(path, updateDef),
          deleteDef => deleteDefTryList(path, deleteDef)),
        stringDef => patch.fold(
          readDef => Future.successful(List.empty),
          createDef => sys.error("sssssssss"),
          updateDef => updateDefTryList(path, updateDef),
          deleteDef => deleteDefTryList(path, deleteDef)),
        booleanDef => patch.fold(
          readDef => Future.successful(List.empty),
          createDef => sys.error("sssssssss"),
          updateDef => updateDefTryList(path, updateDef),
          deleteDef => deleteDefTryList(path, deleteDef)),
        decimalDef => patch.fold(
          readDef => Future.successful(List.empty),
          createDef => sys.error("sssssssss"),
          updateDef => updateDefTryList(path, updateDef),
          deleteDef => deleteDefTryList(path, deleteDef)),
        refDef => patch.fold(
          readDef => Future.successful(List.empty),
          createDef => sys.error("sssssssss"),
          updateDef => updateDefTryList(path, updateDef),
          deleteDef => deleteDefTryList(path, deleteDef)),
        roleDef => patch.fold(
          readDef => Future.successful(List.empty),
          createDef => sys.error("sssssssss"),
          updateDef => updateDefTryList(path, updateDef),
          deleteDef => deleteDefTryList(path, deleteDef)),

        roleGroup => patch.foldRoleGroup(
          readRoleGroupDef => Future.successful(List.empty),
          createRoleGroupDef => sys.error("sssssssss"),
          updateRoleGroupDef => {
            updateRoleGroupDef.value match {
              case Some(elements) =>
                for {
                  r1 <- updateDefTryList(path, updateRoleGroupDef.copy(value = None))
                  r2 <- traversePatchDef(elements, path)
                } yield r1 ++ r2
              case None =>
                updateDefTryList(path, updateRoleGroupDef)
            }
          },
          deleteRoleGroupDef => deleteDefTryList(path, deleteRoleGroupDef)),

        rolesDef => patch.foldRoles(
          readRolesDef => Future.successful(List.empty),
          createRolesDef => sys.error("sssssssss"),
          updateRolesDef => {
            updateRolesDef.value match {
              case Some(elements) =>
                for {
                  r1 <- updateDefTryList(path, updateRolesDef.copy(value = None))
                  r2 <- traversePatchDef(elements, path)
                } yield r1 ++ r2
              case None =>
                updateDefTryList(path, updateRolesDef)
            }
          },
          deleteRolesDef => deleteDefTryList(path, deleteRolesDef)),

        mlStringDef => patch.foldMLString(
          readMLStrinDef => Future.successful(List.empty),
          createMLStringDef => sys.error("sssssssss"),
          updateMLStringDef => {
            updateMLStringDef.value match {
              case Some(elements) =>
                for {
                  r1 <- updateDefTryList(path, updateMLStringDef.copy(value = None))
                  r2 <- traversePatchDef(elements, path)
                } yield r1 ++ r2
              case None =>
                updateDefTryList(path, updateMLStringDef)
            }
          },
          deleteMLStringDef => deleteDefTryList(path, deleteMLStringDef)),

        arrayDef => patch.foldArray(
          readdArrayDef => Future.successful(List.empty),
          createArrayDef => sys.error("sssssssss"),
          updateArrayDef => {
            updateArrayDef.value match {
              case Some(elements) => ???
              case None =>
                updateDefTryList(path, updateArrayDef)
            }
          },
          deleteArrayDef => deleteDefTryList(path, deleteArrayDef)),

        objectDef => patch.foldObject(
          readObjectDef => Future.successful(List.empty),
          createObjectDef => sys.error("sssssssss"),
          updateObjectDef => {
            updateObjectDef.value match {
              case Some(elements) =>
                for {
                  r1 <- updateDefTryList(path, updateObjectDef.copy(value = None))
                  r2 <- traversePatchDef(elements, path)
                } yield r1 ++ r2
              case None =>
                updateDefTryList(path, updateObjectDef)
            }
          },
          deleteObjectDef => deleteDefTryList(path, deleteObjectDef)),

        rootDef => patch.foldRoot(
          readRootDef => Future.successful(List.empty),
          createRootDef => sys.error("sssssssss"),
          updateRootDef => {
            updateRootDef.value match {
              case Some(elements) =>
                for {
                  r1 <- updateDefTryList(path, updateRootDef.copy(value = None))
                  r2 <- traversePatchDef(elements, path)
                } yield r1 ++ r2
              case None =>
                updateDefTryList(path, updateRootDef)
            }
          },
          deleteRootDef => deleteDefTryList(path, deleteRootDef)))

      case None => patch.fold(
        readDef => Future.successful(List.empty),
        createDef => createDef.fold(
          createIntDef => createDefTryList(path, createIntDef),
          createStringDef => createDefTryList(path, createStringDef),
          createBooleanDef => createDefTryList(path, createBooleanDef),
          createDecimalDef => createDefTryList(path, createDecimalDef),
          createRefDef => createDefTryList(path, createRefDef),
          createRoleDef => createDefTryList(path, createRoleDef),
          createRoleGroupDef => {
            for {
              r1 <- createDefTryList(path, createRoleGroupDef.copy(value = Map.empty))
              r2 <- traverseDef(createRoleGroupDef.value, path)
            } yield r1 ++ r2
          },
          createRolesDef => {
            for {
              r1 <- createDefTryList(path, createRolesDef.copy(value = Map.empty))
              r2 <- traverseDef(createRolesDef.value, path)
            } yield r1 ++ r2
          },
          createMLStringDef => {
            for {
              r1 <- createDefTryList(path, createMLStringDef.copy(value = Map.empty))
              r2 <- traverseDef(createMLStringDef.value, path)
            } yield r1 ++ r2
          },
          createArrayDef => ???,
          createObjectDef => {
            for {
              r1 <- createDefTryList(path, createObjectDef.copy(value = Map.empty))
              r2 <- traverseDef(createObjectDef.value, path)
            } yield r1 ++ r2
          },
          createRootDef => {
            for {
              r1 <- createDefTryList(path, createRootDef.copy(value = Map.empty))
              r2 <- traverseDef(createRootDef.value, path)
            } yield r1 ++ r2
          }),
        updateDef => sys.error("sssssssss"),
        deleteDef => sys.error("sssssssss"))

    }
  }
}