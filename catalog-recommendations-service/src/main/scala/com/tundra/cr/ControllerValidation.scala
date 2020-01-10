package com.tundra.cr

import argonaut.Argonaut._
import argonaut.DecodeJson
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import scalaz.\/
import scalaz.syntax.Ops
import scalaz.syntax.std.option._

import scala.util.Try

final case class ErrorResponse(message: String)

final case class ErrorException(error: ErrorResponse) extends Exception(error.message)

trait ControllerValidation {
  // TODO[Mikolaj]: Theses are copied from the buy project, should we move to common?
  protected type EValidation[+A] = \/[ErrorResponse, A]

  protected def requestBody[T: DecodeJson](implicit request: Request): EValidation[T] =
    \/.fromEither(request.getContentString().decodeEither[T]).leftMap(e => ErrorResponse(e))

  implicit class EValidationWrapper[T](val self: EValidation[T]) extends Ops[EValidation[T]] {
    def valueOrError: T = self.valueOr(e => throw ErrorException(e))

    def toFuture: Future[T] = self.map(Future.value).valueOr(e => Future.exception(ErrorException(e)))
  }
  protected def requiredParam(name: String)(implicit r: Request): EValidation[String] =
    r.params.get(name).toRightDisjunction(ErrorResponse(s"$name is required"))

  protected def validLong(value: String): EValidation[Long] =
    Try(value.toLong).toOption.toRightDisjunction(ErrorResponse(s"Number not found: $value"))
  // TODO[Mikolaj]: End copy
}
