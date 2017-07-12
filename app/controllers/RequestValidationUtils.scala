package controllers

import com.markland.service.models.Problem
import models.Models.Problems

import scalaz.Scalaz._
import scalaz.{Scalaz, \/}

object RequestValidationUtils {

  def validateLimit(limit: Int): Problem \/ Unit = {
    if (limit < 1) Problems.invalidInputProblem("Limit must be greater than zero").left[Unit]
    else if (limit > 1000) Problems.invalidInputProblem("Limit cannot exceed 1000").left[Unit]
    else ().right[Problem]
  }

}
