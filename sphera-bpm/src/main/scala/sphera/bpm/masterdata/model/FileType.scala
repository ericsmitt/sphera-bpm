package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.bpm.masterdata.model.Characteristic.CharacteristicAssignments
import sphera.core.domain.tenancy.model.User

case class FileType(
  id: FileType.Id,
  name: String,
  description: Option[String],
  extensions: Set[String],
  isMultiple: Boolean,
  classification: CharacteristicAssignments,
  modifyAttr: ModifyAttr)

object FileType {
  type Id = UUID
  type FileTypes = Map[FileType.Id, FileType]
}

case class UpdateFileType(
  id: FileType.Id,
  name: String,
  description: Option[String],
  extensions: Set[String],
  isMultiple: Boolean,
  classification: CharacteristicAssignments,
  userId: User.Id)
