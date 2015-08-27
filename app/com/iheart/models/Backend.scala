package com.iheart.models


case class Backend(name: String, host: String, hostHeader: String, port: Int, probe: Option[String])


object Backend extends ModelValidations {

  def build(name: String,
            host: String,
            hostHeader: String,
            port: Option[Int]  ,
            probe: Option[String]): Either[BackendError,Backend] = {

    isValid(Seq(validateBackend(name,host))) match {
      case Right(x) => Right(Backend(name,host,hostHeader,port.getOrElse(80),probe))
      case Left(x) => Left(BackendError(x))
    }
  }
}