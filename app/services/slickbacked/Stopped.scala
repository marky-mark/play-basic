package services.slickbacked

import com.markland.service.models.{BatchInfoUpdateStatusStatusEnum, Problem, BatchInfoUpdateStatusResultEnum}
import models.Models.Problems

import scalaz.syntax.std.option._

case class Stopped(result: BatchInfoUpdateStatusResultEnum.Result,
                   status: Option[BatchInfoUpdateStatusStatusEnum.Status],
                   problems: Seq[Problem])

object Stopped {
  def updated(): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.Updated, BatchInfoUpdateStatusStatusEnum.Successful.some, Nil)
  }

  def internalError(description: String): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.InternalError, BatchInfoUpdateStatusStatusEnum.Failed.some, Seq(Problems.internalErrorProblem(description)))
  }

  def untrackedInternalError(description: String): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.InternalError, None, Seq(Problems.internalErrorProblem(description)))
  }

  def validationError(description: String): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.ValidationError, BatchInfoUpdateStatusStatusEnum.Failed.some, Seq(Problems.invalidInputProblem(description)))
  }

  def validationError(problems: Seq[Problem]): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.ValidationError, BatchInfoUpdateStatusStatusEnum.Failed.some, problems)
  }

  def inputError(description: String): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.InputError, BatchInfoUpdateStatusStatusEnum.Failed.some, Seq(Problems.invalidInputProblem(description)))
  }

  def forbidden(description: String): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.ForbiddenError, BatchInfoUpdateStatusStatusEnum.Failed.some, Seq(Problems.forbiddenProblem(description)))
  }

  def missing(description: String): Stopped = {
    Stopped(BatchInfoUpdateStatusResultEnum.Missing, BatchInfoUpdateStatusStatusEnum.Failed.some, Seq(Problems.invalidInputProblem(description)))
  }
}