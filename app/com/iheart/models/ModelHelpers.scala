package com.iheart.models

import com.iheart.models.VclConfigCondition._
import com.iheart.util.VclUtils.VclConditionType._
import play.Logger

import scala.util.matching.Regex


trait ModelHelpers {

  type ValidationError = String
  type Validation = Either[ValidationError,Boolean]

  def getErrors[S <: BaseError,T](coll: Seq[Either[S,T]]): Seq[String] =
    coll.filter(c => c.isLeft).flatMap(c => c.left.get.errors)

  def getErrors[S <: BaseError,T](i: Either[S,T]): Seq[String] = i.isLeft match {
    case true => i.left.get.errors
    case false => Seq()
  }

  def needsMatcher(key: String): Boolean = {
    conditionMap.get(key).isDefined &&
      conditionMap(key).conditionType != BoolCond
  }
  object RegexUtils {
    class RichRegex(underlying: Regex) {
      def matches(s: String) = underlying.pattern.matcher(s).matches
    }
    implicit def regexToRichRegex(r: Regex): RichRegex = new RichRegex(r)
  }

  implicit class validationToEither(b: Boolean) {
    def toValidate(s: String): Validation = b match {
      case true => Right(true)
      case false => Left(s)
    }
  }

  implicit class validationOfString(s: String) {
    def asInt: Option[Int] = try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => None }
  }
}
