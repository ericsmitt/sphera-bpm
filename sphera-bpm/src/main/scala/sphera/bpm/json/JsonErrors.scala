package sphera.bpm.json

import sphera.core.exception.SpheraException

case class JsonErrors(
  httpErrorStatusCode: Int,
  httpErrorMessage: String,
  httpReason: String,
  errors: Seq[SpheraException])