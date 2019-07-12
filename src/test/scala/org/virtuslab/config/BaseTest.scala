package org.virtuslab.config

import org.scalatest._
import org.virtuslab.config.util.PlaySlickConfigurationDbDriver
import play.api.{ Application, Play }
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

class TestPlaySlickDbDriver(app: Application) extends PlaySlickConfigurationDbDriver {
  final def databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfigProvider.get(app)
  override lazy val profile: JdbcProfile = databaseConfig.profile
}

class BaseTest extends FlatSpecLike with Matchers

trait AppTest extends BaseTest with BeforeAndAfterAll with BeforeAndAfterEach {
  private val testDb = Map(
    "slick.dbs.default.driver" -> "slick.driver.H2Driver$", //you must provide the required Slick driver! //https://www.playframework.com/documentation/2.4.x/PlaySlickMigrationGuide
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.db.url" -> "jdbc:h2:mem:config",
    "slick.dbs.default.db.user" -> "sa",
    "slick.dbs.default.db.password" -> "")

  val app: Application = {
    val fake = new GuiceApplicationBuilder().configure(testDb).build()
    Play.start(fake)
    fake
  }

  val testPlaySlickDbDriver = new TestPlaySlickDbDriver(app)
  import testPlaySlickDbDriver.profile.api._
  import testPlaySlickDbDriver._

  override protected def beforeEach(): Unit = {

    Await.result(db.run(sqlu"""DROP ALL OBJECTS"""), Duration.Inf)

    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    Await.result(db.shutdown, Duration.Inf) //test pass without this line (?we don't use pools in tests?) but see 'Closing Database' section: http://slick.typesafe.com/doc/3.0.3/upgrade.html?#closing-databases
    Play.stop(app)
  }

  private lazy val configurationEntriesQuery = TableQuery[ConfigurationEntries]

  protected def db = testPlaySlickDbDriver.databaseConfig.db

  case class IntentionalRollbackException() extends Exception("Transaction intentionally aborted")

  protected val awaitTime = 5.seconds

  /**
   * Runs function in rolled-back transaction.
   *
   * @param testCase function to run in rolled-back transaction
   */
  def rollback(testCase: => DBIO[_]): Unit = {
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
