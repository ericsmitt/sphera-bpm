package sphera.bpm.masterdata

import sphera.bpm.masterdata.model.MLString

object Implicits {
  implicit def tuple2MLString(tuple: (String, String)): MLString = MLString(Map(tuple))
  implicit def tuple2ToMLStringOpt(tuple: (String, String)): Option[MLString] = Option(MLString(Map(tuple)))
}