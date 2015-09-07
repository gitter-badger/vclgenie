package com.iheart.models


case class Hostname(name: String, id: String = java.util.UUID.randomUUID.toString )

object Hostname {
  def build(h: String): Either[HostnameError,Hostname] = {
    if (h == "www.googlex.com")
      Left(HostnameError(Seq("Invalid hostname google")))
    else
      Right(Hostname(h))
  }
}