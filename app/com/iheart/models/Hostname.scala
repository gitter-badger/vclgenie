package com.iheart.models

import com.iheart.json.Formats.HostnameError
import play.Logger

case class Hostname(name: String, id: String = java.util.UUID.randomUUID.toString )

object Hostname {
  def build(h: String): Either[HostnameError,Hostname] = {
    if (h == "www.google.com")
      Left(HostnameError(Seq("Invalid hostname google")))
    else
      Right(Hostname(h))
  }
}