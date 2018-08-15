package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.masterdata.model.{ Characteristic, ModifyAttr, UpdateCharacteristic }
import sphera.core.akkaext.actor.CqrsState

case class CharacteristicState(storage: Map[UUID, Characteristic] = Map.empty) extends CqrsState {

  /**
   * Вставка новой записи [[Characteristic]] в репозиторий
   *
   * @return Репозиторий процессов [[CharacteristicState]] с добавленой записью
   */
  def create(c: UpdateCharacteristic) = {
    if (storage.get(c.id).isDefined) throw new IllegalArgumentException
    copy(
      storage + (c.id -> Characteristic(
        id = c.id,
        name = c.name,
        description = c.description,
        values = c.values,
        modifyAttr = ModifyAttr(c.userId, ZonedDateTime.now()))))
  }

  /**
   * Модификация записи  [[Characteristic]] в репозитории
   *
   * @return Репозиторий  [[CharacteristicState]] с изменённой записью
   */
  def update1(c: UpdateCharacteristic) = {
    if (storage.get(c.id).isEmpty) throw new IllegalArgumentException
    val s = storage(c.id)
    copy(
      storage + (c.id -> Characteristic(
        id = c.id,
        name = c.name,
        description = c.description,
        values = c.values,
        modifyAttr = s.modifyAttr.copy(updatedBy = Option(c.userId), updatedOn = Option(ZonedDateTime.now())))))
  }

  /**
   * Удаление записи  [[Characteristic]] из репозитория
   *
   * @param id идентификатор
   * @return Репозиторий  [[CharacteristicState]] с удалёной записью
   */
  def delete(id: Characteristic.Id) = {
    CharacteristicState(storage - id)
  }

  def resetUpdateCounter = {
    CharacteristicState(storage)
  }

  def exists(id: Characteristic.Id) = storage.get(id).isDefined
  def getById(id: Characteristic.Id) = storage.get(id)
  def getAll = storage
  //  def clear(): CharacteristicState = withDefStorage(storage = Map.empty)

  def update = {
    case CharacteristicsRepositoryActor.CreatedEvt(x) => create(x)
    case CharacteristicsRepositoryActor.UpdatedEvt(x) => update1(x)
    case CharacteristicsRepositoryActor.DeletedEvt(x) => delete(x)
    //    case CharacteristicRepositoryActor.ClearEvt() => clear()
  }

}

