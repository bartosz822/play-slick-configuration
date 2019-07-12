package org.virtuslab.config.util

import slick.jdbc.JdbcProfile

trait HasJdbcDriver {
  val profile: JdbcProfile
}