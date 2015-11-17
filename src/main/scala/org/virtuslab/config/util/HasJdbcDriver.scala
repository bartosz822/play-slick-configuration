package org.virtuslab.config.util

import slick.driver.JdbcDriver


trait HasJdbcDriver {
  val driver: JdbcDriver
}