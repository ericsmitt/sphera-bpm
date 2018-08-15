package sphera.bpm.json

import sphera.core.exception.SpheraException

case class JsonError(
  httpErrorStatusCode: Int,
  httpErrorMessage: String,
  httpReason: String,
  error: SpheraException)