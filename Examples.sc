import scala.reflect.ClassTag
import scala.reflect.classTag
class Blue
class Cyan extends Blue
class Azure extends Blue
class Navy extends Blue
class Sky extends Blue



  def returnGernicType[T<:Blue]: T = {
    t match {
      case _: Cyan => new Cyan().asInstanceOf[T]
      case _: Azure => new Azure().asInstanceOf[T]
      case _: Navy => new Navy().asInstanceOf[T]
      case _: Sky => new Sky().asInstanceOf[T]
      case _ => new Blue().asInstanceOf[T]
    }


val skyType = TypeErasureExample.returnGernicType[Sky]

object AvoidingTypeErasureExample {
  val BlueType = classTag[Blue]
  val CyanType = classTag[Cyan]
  val AzureType = classTag[Azure]
  val NavyType = classTag[Navy]
  val SkyType = classTag[Sky]

  def returnGernicType[T](implicit t: ClassTag[T]): T = {
    t match {
      case CyanType => new Cyan().asInstanceOf[T]
      case AzureType => new Azure().asInstanceOf[T]
      case NavyType => new Navy().asInstanceOf[T]
      case SkyType => new Sky().asInstanceOf[T]
      case _ => new Blue().asInstanceOf[T]
    }
  }
}
val skyType = TypeErasureExample.returnGernicType[Sky]