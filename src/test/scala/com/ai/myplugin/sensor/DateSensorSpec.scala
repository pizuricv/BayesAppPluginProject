package com.ai.myplugin.sensor

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, LocalDateTime}

import scala.collection.JavaConverters._
import org.specs2.mutable.Specification
import TimeAbstractSensor._
import DateTimeFormatter._

class DateSensorSpec extends Specification {

  private val UTC = ZoneId.of("UTC")

  "the date sensor" should {

    "contain true and false as states" in {
      val sensor = new DateSensor
      sensor.getSupportedStates.asScala must contain("true")
      sensor.getSupportedStates.asScala must contain("false")
    }

    "return false for a past date" in {
      val sensor = new DateSensor
      sensor.setProperty(DATE_FORMAT, "2013-09-10T00:00:00.000Z")
      sensor.setProperty(TIME_ZONE, "UTC")

      val result = sensor.execute(null).getObserverState

      result must be equalTo "false"
    }

    "return true for today" in {
      val sensor = new DateSensor()
      sensor.setProperty(DATE_FORMAT, LocalDateTime.now().atZone(UTC).format(ISO_OFFSET_DATE_TIME))
      sensor.setProperty(TIME_ZONE, "UTC")
      val result = sensor.execute(null).getObserverState
      result must be equalTo "true"
    }

//    "work with iso zoned date time" in {
//      val sensor = new DateSensor()
//      sensor.setProperty(DATE_FORMAT, LocalDateTime.now().atZone(UTC).format(ISO_ZONED_DATE_TIME))
//      sensor.setProperty(TIME_ZONE, "UTC")
//      val result = sensor.execute(null).getObserverState
//      result must be equalTo "true"
//    }
  }
}
