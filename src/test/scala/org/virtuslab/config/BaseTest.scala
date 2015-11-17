package org.virtuslab.config

import org.scalatest._
import org.scalatestplus.play.OneAppPerSuite
import org.virtuslab.config.util.PlaySlickConfigurationDbDriver
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.FakeApplication
import slick.backend.DatabaseConfig
import slick.driver.JdbcDriver
import scala.concurrent.Await
import scala.concurrent.duration._


object TestPlaySlickDbDriver extends PlaySlickConfigurationDbDriver {
  final def databaseConfig: DatabaseConfig[Nothing] = DatabaseConfigProvider.get(play.api.Play.current)
  override lazy val driver: JdbcDriver = databaseConfig.driver
}

class BaseTest extends FlatSpecLike with Matchers

trait AppTest extends BaseTest with OneAppPerSuite {

  import TestPlaySlickDbDriver._
  import TestPlaySlickDbDriver.driver.api._

  private val testDb = Map(
    "slick.dbs.default.driver" -> "slick.driver.H2Driver$", //you must provide the required Slick driver! //https://www.playframework.com/documentation/2.4.x/PlaySlickMigrationGuide
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.db.url" -> "jdbc:h2:mem:config",
    "slick.dbs.default.db.user" -> "sa",
    "slick.dbs.default.db.password" -> ""
  )


  private def withApp[A](func: => A): A = {
    val app = new FakeApplication(additionalConfiguration = testDb)
    Play.start(app)
    val ret = func
    Await.result(db.shutdown, Duration.Inf) //test pass without this line (?we don't use pools in tests?) but see 'Closing Database' section: http://slick.typesafe.com/doc/3.0.3/upgrade.html?#closing-databases
    Play.stop(app)
    ret
  }

  private lazy val configurationEntriesQuery = TableQuery[ConfigurationEntries]

  protected def db = TestPlaySlickDbDriver.databaseConfig.db

  case class IntentionalRollbackException() extends Exception("Transaction intentionally aborted")

  protected val awaitTime = 5.seconds

  /**
   * Runs function in rolled-back transaction.
   *
   * @param testCase function to run in rolled-back transaction
   */
  def rollback(testCase: => DBIO[_]): Unit = {
    withApp {
      try {
        val createSchemaAction = configurationEntriesQuery.schema.create
        val testCaseAction = testCase
        val action = createSchemaAction
          .andThen(testCaseAction)
          .andThen(DBIO.failed(new IntentionalRollbackException))
        val future = db.run(action.transactionally)
        Await.result(future, awaitTime)
      } catch {
        case e: IntentionalRollbackException => //Success
      }
    }
  }

}
