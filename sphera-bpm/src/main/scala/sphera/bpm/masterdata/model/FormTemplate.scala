package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.bpm.runnable.RunnableTemplate
import sphera.core.domain.tenancy.model.User

case class FormTemplate(
  id: FormTemplate.Id,
  name: String,
  description: Option[String],
  dataStructureId: DataStructure.Id,
  formStructure: FormStructure,
  modifyAttr: ModifyAttr) extends RunnableTemplate

case class UpdateFormTemplate(
  id: FormTemplate.Id,
  name: String,
  description: Option[String],
  dataStructureId: DataStructure.Id,
  formStructure: FormStructure,
  userId: User.Id)

case class FormStructure(
  caption: MLString,
  description: Option[MLString],
  mainPanel: MainPanel,
  leftPanel: Option[Sidebar],
  rightPanel: Option[Sidebar])

case class MLString(strings: Map[String, String] = Map.empty) {
  def get(lang: String): Option[String] = strings.get(lang)
  def set(mlString: (String, String)): Map[String, String] = strings + mlString
}

case class MainPanel(
  caption: MLString,
  description: Option[MLString],
  sections: List[Section])

case class Sidebar(fixed: Boolean, items: List[FormItem])

case class Section(
  caption: MLString,
  description: Option[MLString],
  showCaption: Boolean,
  transitionAttr: Boolean,
  items: List[SectionItem])

sealed trait SectionItem

case class ColumnsLayout(columns: List[Column]) extends SectionItem

case class Column(percent: Double, items: List[FormItem])

sealed trait FormItem extends SectionItem {
  val caption: MLString
  val description: MLString
}

case class NextSectionButton(caption: MLString, description: MLString) extends FormItem

case class SaveButton(caption: MLString, description: MLString) extends FormItem

case class CompleteButton(caption: MLString, description: MLString) extends FormItem

case class Navigation(caption: MLString, description: MLString) extends FormItem

case class Input(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  readOnly: Boolean,
  validationRule: InputValidationRule) extends FormItem

sealed trait InputValidationRule

case object PhoneRule extends InputValidationRule

case object EmailRule extends InputValidationRule

case class NumberRule(mustInteger: Option[Boolean], min: Option[Double], max: Option[Double]) extends InputValidationRule

case class StringRule(minLength: Option[Int], maxLength: Option[Int]) extends InputValidationRule

case class MultiLineInput(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  readOnly: Boolean,
  lines: Int) extends FormItem

case class DateTimeInput(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  readOnly: Boolean,
  dateTimeType: DateTimeType) extends FormItem

sealed trait DateTimeType

object DateTime extends DateTimeType

object Date extends DateTimeType

object Time extends DateTimeType

case class SingleSelect(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  readOnly: Boolean,
  selectType: SelectType,
  variants: List[SingleSelectVariant]) extends FormItem

case class SingleSelectVariant(code: String, caption: MLString)

sealed trait SelectType

object Boxes extends SelectType

object Dropdown extends SelectType

object Buttons extends SelectType

case class MultiSelect(
  caption: MLString,
  description: MLString,
  variable: String,
  readOnly: Boolean,
  selectType: SelectType,
  variants: List[MultiSelectVariant]) extends FormItem

case class MultiSelectVariant(caption: MLString, variable: String)

case class FileItem(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  behaviour: FileItemBehaviour) extends FormItem

sealed trait FileItemBehaviour

object Upload extends FileItemBehaviour

object Download extends FileItemBehaviour

object UploadAndDownload extends FileItemBehaviour

case class RoleAssignment(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  readOnly: Boolean,
  projectRole: Boolean,
  roleName: String) extends FormItem

case class TwoFactorConfirmation(
  caption: MLString,
  description: MLString,
  variable: String,
  required: Boolean,
  variants: List[SingleSelectVariant]) extends FormItem

case class StaticText(caption: MLString, description: MLString) extends FormItem

case class PhoneCall(
  caption: MLString,
  description: MLString,
  phoneNumberVariable: String,
  showPhoneNumber: Boolean) extends FormItem

object FormTemplate {
  type Id = UUID
  type FormTemplates = Map[FormTemplate.Id, FormTemplate]
}