package sphera.bpm.masterdata.model

import java.time.ZonedDateTime

import sphera.core.domain.tenancy.model.User

case class ModifyAttr(
  createdBy: User.Id,
  createdOn: ZonedDateTime,
  updatedBy: Option[User.Id] = None,
  updatedOn: Option[ZonedDateTime] = None)