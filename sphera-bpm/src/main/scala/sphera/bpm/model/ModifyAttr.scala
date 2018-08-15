package sphera.bpm.model

import java.time.ZonedDateTime

case class ModifyAttr(
  createdBy: Modifier,
  createdOn: ZonedDateTime = ZonedDateTime.now(),
  updatedBy: Option[Modifier] = None,
  updatedOn: Option[ZonedDateTime] = None)
