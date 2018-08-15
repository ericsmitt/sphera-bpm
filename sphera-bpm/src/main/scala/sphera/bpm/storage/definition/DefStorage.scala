package sphera.bpm.storage.definition

import sphera.bpm.json._
import sphera.bpm.lang.Evaluator
import sphera.bpm.model.Modifier
import sphera.bpm.storage.definition.DefStorage.Command
import sphera.bpm.storage.definition.Patch.{Compound, UpdateMetadata, UpdateValue}
import sphera.bpm.storage.json._
import sphera.bpm.{DefDecoderException, DefNotFoundException}
import sphera.core.akkaext.actor._
import sphera.core.domain.tenancy.model.User
import io.circe.{Decoder, Encoder, Json, JsonObject}

import scala.concurrent.Future

trait DefStorageLike[A1 <: DefStorageLike[A1]] extends JsonStorageLike { self: A1 =>
  type A = A1
  type B = DefSchemaStorage

  def indexes: DefIndexStorage
  def schema: Option[B]

  def withIndexes(indexes: DefIndexStorage): A
  def withSchema(schema: B): A

  def getDef(path: DataPath): Def = getDefOpt(path) match {
    case Some(x) => x
    case None => throw DefNotFoundException(path)
  }

  def getDefOpt(path: DataPath): Option[Def] = getDefOptTyped[Def](path)

  def getDefOptTyped[T <: Def](path: DataPath)(implicit d: Decoder[T]): Option[T] = {
    apply(resolveDefPath(path))
      .map(_.as[T])
      .map {
        case Right(x) => x
        case Left(e) => throw DefDecoderException(path, e.message)
      }
  }

  def getIntDefOpt(path: DataPath): Option[IntDef] = getDefOptTyped[IntDef](path)
  def getIntDef(path: DataPath): IntDef = getIntDefOpt(path).get
  def getStringDefOpt(path: DataPath): Option[StringDef] = getDefOptTyped[StringDef](path)
  def getStringDef(path: DataPath): StringDef = getStringDefOpt(path).get
  def getBooleanDefOpt(path: DataPath): Option[BooleanDef] = getDefOptTyped[BooleanDef](path)
  def getBooleanDef(path: DataPath): BooleanDef = getBooleanDefOpt(path).get
  def getDecimalDefOpt(path: DataPath): Option[DecimalDef] = getDefOptTyped[DecimalDef](path)
  def getDecimalDef(path: DataPath): DecimalDef = getDecimalDefOpt(path).get
  def getRefDefOpt(path: DataPath): Option[RefDef] = getDefOptTyped[RefDef](path)
  def getRefDef(path: DataPath): RefDef = getRefDefOpt(path).get
  def getRoleDefOpt(path: DataPath): Option[RoleDef] = getDefOptTyped[RoleDef](path)
  def getRoleDef(path: DataPath): RoleDef = getRoleDefOpt(path).get
  def getRoleGroupDefOpt(path: DataPath): Option[RoleGroupDef] = getDefOptTyped[RoleGroupDef](path)
  def getRoleGroupDef(path: DataPath): RoleGroupDef = getRoleGroupDefOpt(path).get
  def getRolesDefOpt(path: DataPath): Option[RolesDef] = getDefOptTyped[RolesDef](path)
  def getRolesDef(path: DataPath): RolesDef = getRolesDefOpt(path).get
  def getMLStringDefOpt(path: DataPath): Option[MLStringDef] = getDefOptTyped[MLStringDef](path)
  def getMLStringDef(path: DataPath): MLStringDef = getMLStringDefOpt(path).get
  def getObjectDefOpt(path: DataPath): Option[ObjectDef] = getDefOptTyped[ObjectDef](path)
  def getObjectDef(path: DataPath): ObjectDef = getObjectDefOpt(path).get
  def getRootDefOpt(path: DataPath): Option[RootDef] = getDefOptTyped[RootDef](path)
  def getRootDef(path: DataPath): RootDef = getRootDefOpt(path).get
  def getDefIntValue(path: DataPath): Option[Int] = getIntOpt(resolveDefValuePath(path))
  def getDefStringValueOpt(path: DataPath): Option[String] = getStringOpt(resolveDefValuePath(path))
  def getDefStringValue(path: DataPath): String = getDefStringValueOpt(path).get
  def getDefBooleanValue(path: DataPath): Boolean = getBooleanOpt(resolveDefValuePath(path)).get
  def getDefBooleanValueOpt(path: DataPath): Option[Boolean] = getBooleanOpt(resolveDefValuePath(path))
  def getDefDecimalValue(path: DataPath): BigDecimal = getDecimalOpt(resolveDefValuePath(path)).get
  def getDefDecimalValueOpt(path: DataPath): Option[BigDecimal] = getDecimalOpt(resolveDefValuePath(path))
  def getDescription(path: DataPath): Description = getDef(path).description
  def getName(path: DataPath): Name = getDef(path).name

  def getDefStringListValueOpt(path: DataPath): Option[List[String]] = {
    def jsonToStringOpt(x: Json): Option[String] = {
      x.as[StringDef]
        .map(x => Some(x.value))
        .getOrElse(None)
    }
    getListOpt(resolveDefValuePath(path)).map(_.flatMap(jsonToStringOpt))
  }

  def getDefStringListValue(path: DataPath): List[String] = getDefStringListValueOpt(path).get

  def getArrayDefOpt(path: DataPath): Option[ArrayDef] = {
    if (path contains "*") getJsonObjectOpt(resolveDefPath(path)).flatMap(obj => ArrayDef.fromObject(path, obj))
    else getDefOptTyped[ArrayDef](path)
  }
  def getArrayDef(path: DataPath): ArrayDef = getArrayDefOpt(path).get

  //  def decodeArrayDef(path: DataPath): Option[ArrayDef] = {
  //
  //    apply(resolveDefPath(path)).flatMap { json =>
  //      json.arrayOrObject(
  //        throw DefDecoderException(path, s"Invalid def, found $json"),
  //        x => ArrayDef.fromObject(path, x),
  //        x => ArrayDef.fromObject(path, x.toVector.map(_.asJson)))
  //    }
  //  }

  def findAllByType(defType: String): List[FindAllByKeyEntry] = findAllByKey("type")

  def findAllRoleDef: List[RoleDef] = {
    findAllByType(Def.RoleTypeName)
      .filter(_.value == "role".asJson)
      .flatMap(_.parent.as[RoleDef].toOption)
  }

  def findRoleDef(role: String): Option[RoleDef] = findAllRoleDef.find(_.name == role)

  def findRoleUser(role: String): Option[User.Id] = findRoleDef(role).map(_.value)

  def mapValue[T](value: T)(implicit encoder: Encoder[T]): Json = {
    Json.obj("value" -> value.asJson)
  }

  def filterMetadata(elements: Map[String, Def]): Map[String, Json] = {
    elements.mapValues {
      case x: ObjectDef => filterMetadata(x.value).asJson
      case x: RootDef => filterMetadata(x.value).asJson
      case x: IntDef => mapValue(x.value)
      case x: StringDef => mapValue(x.value)
      case x: DecimalDef => mapValue(x.value)
      case x: BooleanDef => mapValue(x.value)
      case x: ArrayDef =>
        val indices = x.value.indices.map(_.toString)
        val elements = indices zip x.value
        val filtered = filterMetadata(elements.toMap)
        Json.fromValues(filtered.values)
      case x: MLStringDef => filterMetadata(x.value).asJson
      case x: RoleDef => mapValue(x.value)
      case x: RoleGroupDef => x.value.map(x => mapValue(x)).asJson
      case x: RolesDef => x.value.map(x => mapValue(x)).asJson
      case x: RefDef => mapValue(x.value)
      case x: Def => sys.error(s"Unknown def type $x")
    }
  }

  /**
   * = Update definition value =
   *
   * Рекурсивно мапит все элементы в { "value": element }.
   */
  def updateDefValue(path: DataPath, json: Json): A = {
    def mapJson(json: Json): Json = json.arrayOrObject(
      or = mapValue(json),
      jsonArray = mapValue(_),
      jsonObject = x => traverse(x.toMap))

    def traverse(elements: Map[String, Json]): Json = {
      (Map[String, Json]() /: elements) { case (acc, (x, y)) => acc + (x -> mapJson(y)) }.asJson
    }

    updateJson(resolveDefPath(path), mapJson(json))
  }

  /**
   * = Update definition value only =
   *
   * Updates only def value without metadata.
   * Be careful when you add new elements to object, use `createDef` or `patchDef` instead.
   *
   * Метод обновляет только поля `value`.
   * Данный метод удобен когда не меняешь структуру документа, а только обновляешь значения в ней.
   * Используется в [[Evaluator]].
   */
  def updateDefValueOnly(path: DataPath, definition: Def): A = {
    val resolvedPath = resolveDefValuePath(path)
    definition.fold(
      intDef => updateInt(resolvedPath, intDef.value),
      stringDef => updateString(resolvedPath, stringDef.value),
      booleanDef => updateBoolean(resolvedPath, booleanDef.value),
      decimalDef => updateDecimal(resolvedPath, decimalDef.value),
      refDef => updatePath(resolvedPath, refDef.value),
      roleDef => updateUUID(resolvedPath, roleDef.value),
      roleGroupDef => updateJson(resolvedPath, filterMetadata(roleGroupDef.value).asJson),
      rolesDef => updateJson(resolvedPath, filterMetadata(rolesDef.value).asJson),
      mlStringDef => updateJson(resolvedPath, filterMetadata(mlStringDef.value).asJson),
      arrayDef => ???,
      objectDef => updateJson(resolvedPath, filterMetadata(objectDef.value).asJson),
      rootDef => updateJson(resolvedPath, filterMetadata(rootDef.value).asJson))
  }

  /**
   * = Create definition =
   *
   * Создает определение по указанному пути.
   */
  def createDef(path: DataPath, create: CreateDef): A = create match {
    case x: CreateIntDef => createDefRepr(path, IntDef(
      name = x.name,
      description = x.description,
      index = x.index,
      value = x.value,
      path = path))
    case x: CreateStringDef => createDefRepr(path, StringDef(
      name = x.name,
      description = x.description,
      index = x.index,
      value = x.value,
      path = path))
    case x: CreateBooleanDef => createDefRepr(path, BooleanDef(
      name = x.name,
      description = x.description,
      index = x.index,
      value = x.value,
      path = path))
    case x: CreateDecimalDef => createDefRepr(path, DecimalDef(
      name = x.name,
      description = x.description,
      index = x.index,
      value = x.value,
      path = path))
    case x: CreateObjectDef => createDefRepr(path, ObjectDef(
      name = x.name,
      description = x.description,
      mutable = x.mutable,
      value = x.value,
      path = path))
    case x: CreateArrayDef => createDefRepr(path, ArrayDef(
      name = x.name,
      description = x.description,
      value = x.value,
      path = path))
    case x: CreateMLStringDef => createDefRepr(path, MLStringDef(
      name = x.name,
      description = x.description,
      value = x.value,
      path = path))
    case x: CreateRoleDef => createDefRepr(path, RoleDef(
      name = x.name,
      description = x.description,
      value = x.value,
      path = path))
    case x: CreateRefDef => createDefRepr(path, RefDef(
      name = x.name,
      description = x.description,
      value = x.value,
      path = path))
    case x: CreateRootDef => createDefRepr(path, RootDef(
      name = x.name,
      description = x.description,
      mutable = x.mutable,
      value = x.value,
      path = path))
  }

  /**
   * = Update definition =
   *
   * Обновляет определение по указанному пути.
   */
  def updateDef(path: DataPath, update: UpdateDef): A = {
    updateDefMetadata(path, update)

    update.fold(
      updateIntDef => {
        val x = getIntDef(path)
        updateDefRepr(path, x.copy(
          name = updateIntDef.name.getOrElse(x.name),
          description = updateIntDef.description.getOrElse(x.description),
          value = updateIntDef.value.getOrElse(x.value)))
      },
      updateStringDef => {
        val x = getStringDef(path)
        updateDefRepr(path, x.copy(
          name = updateStringDef.name.getOrElse(x.name),
          description = updateStringDef.description.getOrElse(x.description),
          value = updateStringDef.value.getOrElse(x.value)))
      },
      updateBooleanDef => {
        val x = getBooleanDef(path)
        updateDefRepr(path, x.copy(
          name = updateBooleanDef.name.getOrElse(x.name),
          description = updateBooleanDef.description.getOrElse(x.description),
          value = updateBooleanDef.value.getOrElse(x.value)))
      },
      updateDecimalDef => {
        val x = getDecimalDef(path)
        updateDefRepr(path, x.copy(
          name = updateDecimalDef.name.getOrElse(x.name),
          description = updateDecimalDef.description.getOrElse(x.description),
          value = updateDecimalDef.value.getOrElse(x.value)))
      },
      updateRefDef => {
        val x = getRefDef(path)
        updateDefRepr(path, x.copy(
          name = updateRefDef.name.getOrElse(x.name),
          description = updateRefDef.description.getOrElse(x.description),
          value = updateRefDef.value.getOrElse(x.value)))
      },
      updateRoleDef => {
        val x = getRoleDef(path)
        updateDefRepr(path, x.copy(
          name = updateRoleDef.name.getOrElse(x.name),
          description = updateRoleDef.description.getOrElse(x.description),
          value = updateRoleDef.value.getOrElse(x.value)))
      },
      updateRoleGroupDef =>
        updateDefMetadata(path, updateRoleGroupDef)
          .updateComplexDefValue(path, updateRoleGroupDef),
      updateRolesDef =>
        updateDefMetadata(path, updateRolesDef)
          .updateComplexDefValue(path, updateRolesDef),
      updateMLStringDef =>
        updateDefMetadata(path, updateMLStringDef)
          .updateComplexDefValue(path, updateMLStringDef),
      updateArrayDef => ???,
      updateObjectDef =>
        updateDefMetadata(path, updateObjectDef)
          .updateComplexDefValue(path, updateObjectDef),
      updateRootDef =>
        updateDefMetadata(path, updateRootDef)
          .updateComplexDefValue(path, updateRootDef))
  }

  def deleteDef(path: DataPath, delete: DeleteDef): A = ???

  private def createDefRepr(path: DataPath, definition: Def): A = createJson(resolveDefPath(path), definition.asJson)
  private def updateDefRepr(path: DataPath, definition: Def): A = updateJson(resolveDefPath(path), definition.asJson)
  private def deleteDefRepr(path: DataPath): A = deleteJson(resolveDefPath(path))

  def updateDefMetadata(path: DataPath, update: UpdateDef): A = {
    def mapStringDef(stringDef: StringDef): UpdateStringDef = {
      UpdateStringDef(
        name = Some(stringDef.name),
        description = Some(stringDef.description),
        index = None,
        value = None
      )
    }
    def mapIntDef(intDef: IntDef, updateMetadata: Boolean, updateValue: Boolean): UpdateIntDef = {
      UpdateIntDef(
        name = Some(intDef.name),
        description = Some(intDef.description),
        index = None,
        value = Some(intDef.value)
      )
    }
    def mapBooleanDef(booleanDef: BooleanDef, updateMetadata: Boolean, updateValue: Boolean): UpdateBooleanDef = {
      UpdateBooleanDef(
        name = Some(booleanDef.name),
        description = Some(booleanDef.description),
        index = None,
        value = Some(booleanDef.value)
      )
    }
    def mapDecimalDef(decimalDef: DecimalDef, updateMetadata: Boolean, updateValue: Boolean): UpdateDecimalDef = {
      UpdateDecimalDef(
        name = Some(decimalDef.name),
        description = Some(decimalDef.description),
        index = None,
        value = Some(decimalDef.value)
      )
    }
    def mapRefDef(refDef: RefDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRefDef = {
      UpdateRefDef(
        name = Some(refDef.name),
        description = Some(refDef.description),
        value = Some(refDef.value)
      )
    }
    def mapRoleDef(roleDef: RoleDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRoleDef = {
      UpdateRoleDef(
        name = Some(roleDef.name),
        description = Some(roleDef.description),
        value = Some(roleDef.value)
      )
    }
    def mapRoleGroupDef(roleGroupDef: RoleGroupDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRoleGroupDef = {
      UpdateRoleGroupDef(
        name = Some(roleGroupDef.name),
        description = Some(roleGroupDef.description),
        value = Some(roleGroupDef.value.mapValues(mapRoleDef)),
        patch = Some(Compound(updateMetadata = true, updateDefs = true))
      )
    }
    def mapRolesDef(rolesDef: RolesDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRolesDef = {
      UpdateRolesDef(
        name = Some(rolesDef.name),
        description = Some(rolesDef.description),
        value = Some(rolesDef.value.mapValues(mapRoleGroupDef)),
        patch = Some(Compound(updateMetadata = true, updateDefs = true))
      )
    }
    def mapMLStringDef(mlStringDef: MLStringDef, updateMetadata: Boolean, updateValue: Boolean): UpdateMLStringDef = {
      UpdateMLStringDef(
        name = Some(mlStringDef.name),
        description = Some(mlStringDef.description),
        value = Some(mlStringDef.value.mapValues(mapStringDef)),
        patch = Some(Compound(updateMetadata = true, updateDefs = true))
      )
    }
    def mapArrayDef(arrayDef: ArrayDef): UpdateArrayDef = UpdateArrayDef(
      name = Some(arrayDef.name),
      description = Some(arrayDef.description),
      value = Some(???),
      patch = Some(Compound(updateMetadata = true, updateDefs = true))
    )
    def mapObjectDef(objectDef: ObjectDef): UpdateObjectDef = UpdateObjectDef(
      name = Some(objectDef.name),
      description = Some(objectDef.description),
      value = Some(objectDef.value.mapValues(mapDef)),
      patch = Some(Compound(updateMetadata = true, updateDefs = true))
    )
    def mapRootDef(rootDef: RootDef): UpdateRootDef = UpdateRootDef(
      name = Some(rootDef.name),
      description = Some(rootDef.description),
      value = Some(rootDef.value.mapValues(mapDef)),
      patch = Some(Compound(updateMetadata = true, updateDefs = true))
    )
    def mapDef(definition: Def): UpdateDef = definition.fold(
      mapIntDef,
      mapStringDef,
      mapBooleanDef,
      mapDecimalDef,
      mapRefDef,
      mapRoleDef,
      mapRoleGroupDef,
      mapRolesDef,
      mapMLStringDef,
      mapArrayDef,
      mapObjectDef,
      mapRootDef
    )

    def traverse(elements: Map[String, Def], path: DataPath, acc: A): A = {
      (acc /: elements) { case (a, (name, element)) => updateDefMetadata(path / name, mapDef(element))
    }


    update.fold(
      updateIntDef => {
        val x = getIntDef(path)
        val y = x.copy(
          name = updateIntDef.name.getOrElse(x.name),
          description = updateIntDef.description.getOrElse(x.description),
          value = updateIntDef.value.getOrElse(x.value))
        updateDefRepr(path, y)
      },
      updateStringDef => {
        val x = getStringDef(path)
        val y = x.copy(
          name = updateStringDef.name.getOrElse(x.name),
          description = updateStringDef.description.getOrElse(x.description),
          value = updateStringDef.value.getOrElse(x.value))
        updateDefRepr(path, y)
      },
      updateBooleanDef => {
        val x = getBooleanDef(path)
        val y = x.copy(
          name = updateBooleanDef.name.getOrElse(x.name),
          description = updateBooleanDef.description.getOrElse(x.description),
          value = updateBooleanDef.value.getOrElse(x.value))
        updateDefRepr(path, y)
      },
      updateDecimalDef => {
        val x = getDecimalDef(path)
        val y = x.copy(
          name = updateDecimalDef.name.getOrElse(x.name),
          description = updateDecimalDef.description.getOrElse(x.description),
          value = updateDecimalDef.value.getOrElse(x.value))
        updateDefRepr(path, y)
      },
      updateRefDef => {
        val x = getRefDef(path)
        val y = x.copy(
          name = updateRefDef.name.getOrElse(x.name),
          description = updateRefDef.description.getOrElse(x.description),
          value = updateRefDef.value.getOrElse(x.value))
        updateDefRepr(path, y)
      },
      updateRoleDef => {
        val x = getRoleDef(path)
        val y = x.copy(
          name = updateRoleDef.name.getOrElse(x.name),
          description = updateRoleDef.description.getOrElse(x.description),
          value = updateRoleDef.value.getOrElse(x.value))
        updateDefRepr(path, y)
      },
      updateRoleGroupDef => {
        updateRoleGroupDef.patch match {
          case Some(Compound(updateMetadata, _)) => updateMetadata.fold(
            this,
            {
              val x = getRoleGroupDef(path)
              val y = x.copy(
                name = update.name.getOrElse(x.name),
                description = update.description.getOrElse(x.description))
              updateDefRepr(path, y)
            },
            {
              val x = getRoleGroupDef(path)
              val y = x.copy(
                name = update.name.getOrElse(x.name),
                description = update.description.getOrElse(x.description)
              )
              val acc = updateDefRepr(path, y)
              traverse(x.value, path, acc)
            }
          )
          case None => this
        }


      },
      ifUpdateRolesDef => {
        val x = getRolesDef(path)
        updateDefRepr(path, x.copy(
          name = update.name.getOrElse(x.name),
          description = update.description.getOrElse(x.description)))
      },
      ifUpdateMLStringDef => {
        val x = getMLStringDef(path)
        updateDefRepr(path, x.copy(
          name = update.name.getOrElse(x.name),
          description = update.description.getOrElse(x.description)))
      },
      ifUpdateArrayDef => ???,
      ifUpdateObjectDef => {
        val x = getObjectDef(path)
        updateDefRepr(path, x.copy(
          name = update.name.getOrElse(x.name),
          description = update.description.getOrElse(x.description)))
      },
      ifUpdateRootDef => {
        val x = getRootDef(path)
        updateDefRepr(path, x.copy(
          name = update.name.getOrElse(x.name),
          description = update.description.getOrElse(x.description)))
      })
  }

  def updateComplexDefValue(path: DataPath, update: UpdateComplexDef): A = {
    update.value match {
      case Some(_) => updateComplexDef(path, update)
      case None => this
    }
  }

  def updateComplexDef(path: DataPath, update: UpdateComplexDef, acc: A = this): A = {
    val patch = update.patch
    val updateMetadata = patch.map(_.updateMetadata)

    def mapStringDef(stringDef: StringDef, updateMetadata: Boolean, updateValue: Boolean): UpdateStringDef = {
      UpdateStringDef(
        name = if (updateMetadata) Some(stringDef.name) else None,
        description = if (updateMetadata) Some(stringDef.description) else None,
        index = None,
        value = if (updateValue) Some(stringDef.value) else None
      )
    }
    def mapIntDef(intDef: IntDef, updateMetadata: Boolean, updateValue: Boolean): UpdateIntDef = {
      UpdateIntDef(
        name = if (updateMetadata) Some(intDef.name) else None,
        description = if (updateMetadata) Some(intDef.description) else None,
        index = None,
        value = if (updateValue) Some(intDef.value) else None
      )
    }
    def mapBooleanDef(booleanDef: BooleanDef, updateMetadata: Boolean, updateValue: Boolean): UpdateBooleanDef = {
      UpdateBooleanDef(
        name = if (updateMetadata) Some(booleanDef.name),
        description = Some(booleanDef.description),
        index = None,
        value = Some(booleanDef.value)
      )
    }
    def mapDecimalDef(decimalDef: DecimalDef, updateMetadata: Boolean, updateValue: Boolean): UpdateDecimalDef = {
      UpdateDecimalDef(
        name = Some(decimalDef.name),
        description = Some(decimalDef.description),
        index = None,
        value = Some(decimalDef.value)
      )
    }
    def mapRefDef(refDef: RefDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRefDef = {
      UpdateRefDef(
        name = Some(refDef.name),
        description = Some(refDef.description),
        value = Some(refDef.value)
      )
    }
    def mapRoleDef(roleDef: RoleDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRoleDef = {
      UpdateRoleDef(
        name = Some(roleDef.name),
        description = Some(roleDef.description),
        value = Some(roleDef.value)
      )
    }
    def mapRoleGroupDef(roleGroupDef: RoleGroupDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRoleGroupDef = {
      UpdateRoleGroupDef(
        name = Some(roleGroupDef.name),
        description = Some(roleGroupDef.description),
        value = Some(roleGroupDef.value.mapValues(mapRoleDef)),
        patch = Some(Compound(updateMetadata = true, updateDefs = true))
      )
    }
    def mapRolesDef(rolesDef: RolesDef, updateMetadata: Boolean, updateValue: Boolean): UpdateRolesDef = {
      UpdateRolesDef(
        name = Some(rolesDef.name),
        description = Some(rolesDef.description),
        value = Some(rolesDef.value.mapValues(mapRoleGroupDef)),
        patch = Some(Compound(updateMetadata = true, updateDefs = true))
      )
    }
    def mapMLStringDef(mlStringDef: MLStringDef, updateMetadata: Boolean, updateValue: Boolean): UpdateMLStringDef = {
      UpdateMLStringDef(
        name = Some(mlStringDef.name),
        description = Some(mlStringDef.description),
        value = Some(mlStringDef.value.mapValues(mapStringDef)),
        patch = Some(Compound(updateMetadata = true, updateDefs = true))
      )
    }
    def mapArrayDef(arrayDef: ArrayDef): UpdateArrayDef = UpdateArrayDef(
      name = Some(arrayDef.name),
      description = Some(arrayDef.description),
      value = Some(???),
      patch = Some(Compound(updateMetadata = true, updateDefs = true))
    )
    def mapObjectDef(objectDef: ObjectDef): UpdateObjectDef = UpdateObjectDef(
      name = Some(objectDef.name),
      description = Some(objectDef.description),
      value = Some(objectDef.value.mapValues(mapDef)),
      patch = Some(Compound(updateMetadata = true, updateDefs = true))
    )
    def mapRootDef(rootDef: RootDef): UpdateRootDef = UpdateRootDef(
      name = Some(rootDef.name),
      description = Some(rootDef.description),
      value = Some(rootDef.value.mapValues(mapDef)),
      patch = Some(Compound(updateMetadata = true, updateDefs = true))
    )
    def mapDef(definition: Def): UpdateDef = definition.fold(
      mapIntDef,
      mapStringDef,
      mapBooleanDef,
      mapDecimalDef,
      mapRefDef,
      mapRoleDef,
      mapRoleGroupDef,
      mapRolesDef,
      mapMLStringDef,
      mapArrayDef,
      mapObjectDef,
      mapRootDef
    )

    (acc /: update.value.get) { case (acc, (name, patchDef)) =>
      val elementPath = path / name
      val useUpdateDef = update.patch match {
        case Some(Compound(UpdateMetadata.Read, UpdateValue.Read)) => false
        case Some(Compound(_, _)) => true
        case None => false
      }
      patchDef match {
        case x: ReadDef if useUpdateDef => acc.updateDef(elementPath, mapDef(x.value))
        // if read than leave it unchanged
        case x: ReadDef => acc
        case x: UpdateComplexDef => acc.updateDef(elementPath, mapDef(x.value))
          updateComplexDef(elementPath, x, acc)
        case x: CreateDef => acc.createDef(elementPath, x)
        case x: UpdateDef => acc.updateDef(elementPath, x)
        case x: DeleteDef => acc.deleteDefRepr(elementPath)
      }
    }
  }

  /**
   * = Patch definition =
   *
   * Рекурсивно обходит узлы и производит операцию (обновления, добавления, удаления),
   * если в документе есть метка patch.
   */
  def patchDef(path: DataPath, patch: PatchDef): A = patch match {
    case x: UpdateComplexDef => updateComplexDef(path, x)
    case x: CreateDef => createDef(path, x)
    case x: UpdateDef => updateDef(path, x)
    case x: DeleteDef => deleteDef(path, x)
    case x: ReadDef => this
  }

  def patchDef(patch: PatchDef): A = patchDef(RootDataPath, patch)

  /**
   * = Add definition =
   *
   * `addDef, addDefs` methods without schema validation.
   * Use them methods only for building structure.
   */
  def addDef(path: DataPath, definition: Def): A = addJson(resolveDefPath(path), definition.asJson)
  def addDef(path: DataPath, json: Json): A = addJson(resolveDefPath(path), json)
  def addDef(definition: Def): A = addDef(DataPath(definition.name), definition)
  def addDef(definitions: Map[DataPath, Def]): A = definitions.foldLeft(this)({ case (s, (p, d)) => s.addDef(p, d) })
  def addDef(definitions: Iterable[Def]): A = definitions.foldLeft(this)({ case (s, d) => s.addDef(d) })

  def fragments(path: DataPath): List[String] = {
    if (path.fragments.isEmpty) List.empty
    else path.fragments
      .zip(List.fill(path.fragments.size)("value"))
      .flatMap { case (x1, x2) => List(x2, x1) }
  }

  def resolveDefPath(path: DataPath): DataPath = DataPath(fragments(path))

  def resolveDefValuePath(path: DataPath): DataPath = DataPath(fragments(path) :+ "value")

  def mapSchema(f: DefSchemaStorage => DefSchemaStorage): A
}

case class DefStorage(
  repr: Json,
  indexes: DefIndexStorage = DefIndexStorage.empty,
  schema: Option[DefSchemaStorage] = None) extends DefStorageLike[DefStorage] {

  def withRepr(repr: Json) = copy(repr = repr)
  def withIndexes(indexes: DefIndexStorage) = copy(indexes = indexes)
  def withSchema(schema: DefSchemaStorage) = copy(schema = Some(schema))
  def mapIndexes(f: DefIndexStorage => DefIndexStorage) = copy(indexes = indexes.map(f))
  def mapSchema(f: DefSchemaStorage => DefSchemaStorage) = copy(schema = schema.map(f))
}

object DefStorage {
  trait Command extends JsonStorage.Command
  trait Request extends JsonStorage.Request
  trait Response extends JsonStorage.Response
  trait Event extends JsonStorage.Event

  case class GetDef(path: DataPath) extends Request
  case class GetDefIntValue(path: DataPath) extends Request
  case class GetDefStringValue(path: DataPath) extends Request
  case class GetDefBooleanValue(path: DataPath) extends Request
  case class GetDefDecimalValue(path: DataPath) extends Request
  case class FindRoleUser(role: String) extends Request

  case class UpdateDefValueCmd(path: DataPath, json: Json, modifier: Modifier) extends Command
  case class UpdateDefValueOnlyCmd(path: DataPath, definition: Def, modifier: Modifier) extends Command
  case class CreateDefCmd(path: DataPath, create: CreateDef, modifier: Modifier) extends Command
  case class UpdateDefCmd(path: DataPath, update: UpdateDef, modifier: Modifier) extends Command
  case class DeleteDefCmd(path: DataPath, delete: DeleteDef, modifier: Modifier) extends Command

  case class UpdatedDefValueEvt(path: DataPath, json: Json, modifier: Modifier) extends Event
  case class UpdatedDefValueOnlyEvt(path: DataPath, definition: Def, modifier: Modifier) extends Event
  case class CreatedDefEvt(path: DataPath, create: CreateDef, modifier: Modifier) extends Event
  case class UpdatedDefEvt(path: DataPath, update: UpdateDef, modifier: Modifier) extends Event
  case class DeletedDefEvt(path: DataPath, delete: DeleteDef, modifier: Modifier) extends Event

  case class DefOpt(x: Option[Def]) extends Response
  case class DefIntValueOpt(x: Option[Int]) extends Response
  case class DefStringValueOpt(x: Option[String]) extends Response
  case class DefBooleanValueOpt(x: Option[Boolean]) extends Response
  case class DefDecimalValueOpt(x: Option[BigDecimal]) extends Response
  case class RoleUserOpt(x: Option[User.Id]) extends Response
  case class DefValueOpt(x: Option[Json]) extends Response

  def apply(repr: Json, schema: Option[DefSchemaStorage]): DefStorage = {
    DefStorage(
      repr = repr,
      indexes = DefIndexStorage.empty,
      schema = schema)
  }

  def apply(repr: Json, schema: DefSchemaStorage): DefStorage = {
    DefStorage(
      repr = repr,
      indexes = DefIndexStorage.empty,
      schema = Some(schema))
  }

  def emptyRootDef(mutable: Boolean = false, schema: Option[DefSchemaStorage] = None): DefStorage = {
    DefStorage(
      repr = RootDef.empty(mutable).asJson,
      indexes = DefIndexStorage.empty,
      schema = schema)
  }
}