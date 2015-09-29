package com.iheart.models

import com.iheart.util.VclUtils._

case class Hostname(name: String, id: String = generateUUID )

object Hostname {
  def build(h: String): Either[HostnameError,Hostname] = {
    if (h == "www.googlex.com")
      Left(HostnameError(Seq("Invalid hostname google")))
    else
      Right(Hostname(h))
  }
}