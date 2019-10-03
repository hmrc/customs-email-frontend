package utils

object TestEnvironment extends Enumeration {

  type TestEnvironment = Value

  val DEV = Value("dev")
  val QA = Value("qa")
  val LOCAL = Value("local")

  def withNameEither(s: String): Either[String, Value] = values.find(_.toString.toLowerCase == s.toLowerCase) match {
    case None => Left(s"Environment name $s is incorrect")
    case Some(v) => Right(v)
  }

}
