package org.virtuslab.config

import java.util.concurrent.TimeUnit
import org.joda.time.{ DateTime, Duration }
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class ConfigurationServiceTest extends AppTest {
  import testPlaySlickDbDriver._
  behavior of "Configuration API"

  it should "manage configuration correctly via repository" in rollback {
    //given
    val repository = new ConfigurationRepository
    val k1 = "k1"
    val k2 = "k2"
    val v1 = "v1"
    val v2 = "v2"
    val v3 = "v3"
    val conf1 = ConfigurationEntry(k1, v1)
    val conf2 = ConfigurationEntry(k2, v2)

    //when
    for {
      _ <- repository.createOrUpdate(conf1)
      _ <- repository.createOrUpdate(conf2)
      Some(testConf1) <- repository.findByKey(k1)
      Some(testConf2) <- repository.findByKey(k2)
      //then valid conf is returned
      _ = testConf1 shouldEqual v1
      _ = testConf2 shouldEqual v2
      //when update conf
      _ <- repository.createOrUpdate(conf1.copy(value = v3))
      //then conf should be updated
      _ <- repository.findByKey(k1).map(_ shouldEqual Some(v3))
    } yield ()
  }

  it should "work for Int keys" in rollback {
    // given define int key
    val intParam = ConfigurationParam[Int]("intKey")

    for {
      // when save it
      _ <- intParam.saveValue(1)
      // then it should be set
      _ <- intParam.value().map(_ shouldEqual 1)
    } yield ()
  }

  it should "work for String keys" in rollback {
    // given define String key
    val stringParam = ConfigurationParam[String]("stringKey")

    for {
      // when save it
      _ <- stringParam.saveValue("ala")
      // then it should be set
      _ <- stringParam.value().map(_ shouldEqual "ala")
    } yield ()
  }

  it should "work for DateTime keys" in rollback {
    // given define DateTime key
    val dataTimeParam = ConfigurationParam[DateTime]("dateTimeKey")
    val dateTime = new DateTime(2005, 3, 26, 12, 0, 0, 0)

    for {
      // when save it
      _ <- dataTimeParam.saveValue(dateTime)
      // then it should be set
      _ <- dataTimeParam.value().map(_ shouldEqual dateTime)
    } yield ()
  }

  it should "work for FiniteDuration keys" in rollback {
    // given define FiniteDuration key
    val finiteDurationParam = ConfigurationParam[FiniteDuration]("finiteDurationKey")
    val duration = FiniteDuration(12, TimeUnit.DAYS)

    for {
      // when save it
      _ <- finiteDurationParam.saveValue(duration)
      // then it should be set
      _ <- finiteDurationParam.value().map(_ shouldEqual duration)
    } yield ()
  }

  it should "work for Duration keys" in rollback {
    // given define FiniteDuration key
    val durationParam = ConfigurationParam[Duration]("finiteDurationKey")
    val duration = Duration.standardDays(12)

    for {
      // when save it
      _ <- durationParam.saveValue(duration)
      // then it should be set
      _ <- durationParam.value().map(_ shouldEqual duration)
    } yield ()
  }

  it should "work for Boolean keys" in rollback {
    // given define FiniteDuration key
    val booleanParam = ConfigurationParam[Boolean]("booleanKey")
    val boolean = true

    for {
      // when save it
      _ <- booleanParam.saveValue(boolean)
      // then it should be set
      _ <- booleanParam.value().map(_ shouldEqual boolean)
    } yield ()
  }

  it should "update value by key" in rollback {
    // given define key
    val intParam = ConfigurationParam[Int]("intKey")

    //when
    for {
      // when save it
      _ <- intParam.saveValue(1)
      // then it should be set
      _ <- intParam.value().map(_ shouldEqual 1)
      // when override itREADME.md
      _ <- intParam.saveValue(2)
      // then value changes
      _ <- intParam.value().map(_ shouldEqual 2)
    } yield ()
  }

  it should "be empty when param is not set" in rollback {
    // given
    val intParam = ConfigurationParam[Int]("intKey")

    for {
      //when & then
      _ <- intParam.valueOpt().map(_ shouldEqual None)
      _ = intercept[NoSuchElementException] {
        Await.result(db.run(intParam.value()), awaitTime)
      }.getMessage shouldEqual "Configuration value not found for key: intKey"
    } yield ()
  }

  it should "delete configuration key" in rollback {
    // given
    val intParam = ConfigurationParam[Int]("intKey")

    for {
      //when & then
      _ <- intParam.valueOpt().map(_ shouldEqual None)
      _ <- intParam.saveValue(1)
      _ <- intParam.value().map(_ shouldEqual 1)
      _ <- intParam.delete()
      _ <- intParam.valueOpt().map(_ shouldEqual None)
    } yield ()
  }
}
