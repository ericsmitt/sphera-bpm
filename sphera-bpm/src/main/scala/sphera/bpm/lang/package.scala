package sphera.bpm

import sphera.bpm.process.{ Assignment, Branch }

package object lang {
  type Assignments = List[Assignment]
  type Expressions = List[Expression]
  type Branches = List[Branch]
}
