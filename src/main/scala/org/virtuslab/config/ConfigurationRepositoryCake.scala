package org.virtuslab.config

import org.virtuslab.config.util.HasJdbcDriver
import scala.concurrent.ExecutionContext


trait ConfigurationRepositoryCake extends ConfigurationEntriesCake {
  self: HasJdbcDriver =>

  import profile.api._

  private[config] trait ConfigurationQueries {

    protected val configurationEntriesQueries = TableQuery[ConfigurationEntries]

    protected lazy val valueByKeyQuery = Compiled(
      (key: Rep[String]) =>
        for {
          conf <- configurationEntriesQueries
          if conf.key === key
        } yield conf.value
    )

  }

  private[config] class ConfigurationRepository extends ConfigurationQueries {

    def findByKey(key: String): DBIO[Option[String]] = {
      valueByKeyQuery(key).result.headOption
    }

    /**
      * Updates configuration entry if it exists or create new one otherwise.
      */
    def createOrUpdate(entry: ConfigurationEntry)(implicit executionContext: ExecutionContext): DBIO[Unit] = {
      valueByKeyQuery(entry.key).update(entry.value).flatMap { rowsUpdated =>
        if (rowsUpdated == 0) {
          configurationEntriesQueries.forceInsert(entry).map(_ => Unit)
        } else {
          DBIO.successful(Unit)
        }
      }
    }

    /**
      * Removes element from configuration.
      * @return number of deleted elements
      */
    def delete(key: String): DBIO[Int] = {
      valueByKeyQuery(key).delete
    }
  }

}