package sphera.bpm.bpmn

import sphera.core.akkaext.actor.{ DataPath, Path }

case class BpmnId(raw: String) {
  require(!raw.contains(Path.FragmentSeparatorA), s"BpmnId must not contain ${Path.FragmentSeparatorA}")
  require(!raw.contains(Path.FragmentSeparatorB), s"BpmnId must not contain ${Path.FragmentSeparatorB}")

  override def toString = raw
}
