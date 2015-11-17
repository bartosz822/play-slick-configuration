package org.virtuslab.config.util

import org.virtuslab.config.{ConfigurationSerializersCake, ConfigurationEntriesCake, ConfigurationParamCake, ConfigurationRepositoryCake}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcDriver


object PlaySlickConfigurationDbDriver extends PlaySlickConfigurationDbDriver {
  override lazy val driver = DatabaseConfigProvider.get(play.api.Play.current).driver
}

trait PlaySlickConfigurationDbDriver extends HasJdbcDriver
  with ConfigurationRepositoryCake
  with ConfigurationSerializersCake
  with ConfigurationParamCake
  with ConfigurationEntriesCake
{
  val driver: JdbcDriver
}
