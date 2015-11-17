package org.virtuslab.config

import java.util.NoSuchElementException
import org.virtuslab.config.util.HasJdbcDriver
import scala.concurrent.ExecutionContext


trait ConfigurationParamCake extends ConfigurationSerializersCake with ConfigurationRepositoryCake{
  self: HasJdbcDriver =>

  import driver.api._

  /**
   * Class for easy configuration management.
   */
  case class ConfigurationParam[A: ConfigurationSerializer](paramName: String) {

    private val serializer = implicitly[ConfigurationSerializer[A]]

    private val repo = new ConfigurationRepository

    /**
     * @return option with value for this key
     */
    def valueOpt()(implicit executionContext: ExecutionContext): DBIO[Option[A]] = {
      repo.findByKey(paramName).map(_.map(serializer.read))
    }

    /**
     * @return value for this key
     * @throws NoSuchElementException if no configuration entry was found
     */
    def value()(implicit executionContext: ExecutionContext): DBIO[A] = {
      valueOpt().map {
        _.getOrElse(
          throw new NoSuchElementException(s"Configuration value not found for key: $paramName")
        )
      }
    }

    /**
     * Saves this key with given value
     *
     * @param value value to save for this key
     */
    def saveValue(value: A)(implicit executionContext: ExecutionContext): DBIO[Unit] = {
       repo.createOrUpdate(ConfigurationEntry(paramName, serializer.write(value)))
    }

    /** Removes element from configuration. */
    def delete(): DBIO[Unit] = {
      repo.delete(paramName).andThen(DBIO.successful(Unit))
    }

  }

}