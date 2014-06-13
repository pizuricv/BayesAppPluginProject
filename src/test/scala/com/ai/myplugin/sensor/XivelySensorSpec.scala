package com.ai.myplugin.sensor

import com.ai.api.SessionContext
import com.google.gson.{JsonObject, Gson}
import org.specs2.mutable.Specification

class XivelySensorSpec extends Specification{

  "executing the sensor" should {
    "return raw data in json format with a current_value > 0" in {
      skipped("skipped as integration test")
      val sensor = new XivelySensor()
      val context = new SessionContext(1)

      sensor.setup(context)

      val result = sensor.execute(context)

      val rawData = new Gson().fromJson(result.getRawData, classOf[JsonObject])

      rawData.get("current_value").getAsLong must be greaterThan 0
    }
  }
}
