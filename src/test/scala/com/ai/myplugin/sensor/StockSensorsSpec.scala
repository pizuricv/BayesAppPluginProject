package com.ai.myplugin.sensor

import java.{lang, util}

import play.api.libs.json.Json

import scala.collection.JavaConverters._
import com.ai.api.RawDataType
import org.specs2.mutable.Specification

class StockSensorsSpec extends Specification{

  private def dummyStockSensor() = new StockAbstractSensor {
    override protected def getSensorName: String = "Test"
    override def getRequiredRawData = Map.empty[String,RawDataType].asJava
    override protected def getObserverState(results: util.Map[String, lang.Double], threshold: lang.Double): String = "Dummy"
  }

  "The abstract stock sensor" should {

    "return the correct json raw data" in {
      val stockSensor = dummyStockSensor()
      stockSensor.setProperty(StockAbstractSensor.STOCK, "GOOG")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "100")
      val result = stockSensor.execute(null)
      result.isSuccess must beTrue
      val raw = Json.parse(result.getRawData)

      // validate that all said to be produced raw data values are there
      val expected = stockSensor.getProducedRawData.asScala.keys.toSeq
      raw.as[Map[String, Option[Double]]] must haveKeys(expected:_*)
    }

    "return an error result on stock property missing" in {
      val stockSensor = dummyStockSensor()
      val result = stockSensor.execute(null)
      result.isSuccess must beFalse
      result.errorMessage must be equalTo "stock property missing"
    }

    "return an error result on unparsable threshold" in {
      val stockSensor = dummyStockSensor()
      stockSensor.setProperty(StockAbstractSensor.STOCK, "DUMMY")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "i do not parse")
      val result = stockSensor.execute(null)
      result.isSuccess must beFalse
      result.errorMessage must be equalTo """Could not parse threshold as number: For input string: "i do not parse""""
    }

    "return an error if the stock symbol is not found" in {
      val stockSensor = dummyStockSensor()
      stockSensor.setProperty(StockAbstractSensor.STOCK, "IDONOTEXIST")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "100")
      val result = stockSensor.execute(null)
      result.isSuccess must beFalse
      result.errorMessage must be equalTo "No stock with symbol IDONOTEXIST found"
    }
  }

  "The stock price sensor" should {

    "return the state [Below] for MSFT with threshold 1000" in {
      val stockSensor = new StockPriceSensor()
      stockSensor.setProperty(StockAbstractSensor.STOCK, "MSFT")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "1000")
      val result = stockSensor.execute(null)
      result.isSuccess must beTrue
      result.getObserverState must be equalTo StockAbstractSensor.STATE_BELOW
    }

    "return the state [ABOVE] for GOOG with threshold 100" in {
      val stockSensor = new StockPriceSensor()
      stockSensor.setProperty(StockAbstractSensor.STOCK, "GOOG")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "100")
      val result = stockSensor.execute(null)
      result.isSuccess must beTrue
      result.getObserverState must be equalTo StockAbstractSensor.STATE_ABOVE
    }
  }

  "The stock formula sensor" should {

    "have one exta raw data property" in {
      val stockFormulaSensor = new StockFormulaSensor
      stockFormulaSensor.getProducedRawData.asScala must haveKey("formulaValue")
    }

    "do a substraction correctly" in {
      val stockFormulaSensor = new StockFormulaSensor
      stockFormulaSensor.setProperty("stock", "ALU")
      stockFormulaSensor.setProperty("threshold", 0)
      stockFormulaSensor.setProperty("formula", "<this.rawData.price> - <this.rawData.moving_average>")

      val result = stockFormulaSensor.execute(null)

//      println(result.getObserverState)
//      println(result.getRawData)

      Json.parse(result.getRawData).as[Map[String, Option[Double]]] must haveKey("formulaValue")
    }

    "execute an advanced formula correctly" in {
      val stockFormulaSensor = new StockFormulaSensor
      stockFormulaSensor.setProperty("stock", "GOOG")
      stockFormulaSensor.setProperty("threshold", .15)
      stockFormulaSensor.setProperty("formula", "(<this.rawData.price> - <this.rawData.moving_average>)/<this.rawData.moving_average>")

      val result = stockFormulaSensor.execute(null)

//      println(result.getObserverState)
//      println(result.getRawData)

      Json.parse(result.getRawData).as[Map[String, Option[Double]]] must haveKey("formulaValue")
    }
  }

  "The stock moving average sensor" should {
    "return a correct state" in {
      val stockSensor = new StockMovingAverageSensor

      stockSensor.setProperty(StockAbstractSensor.STOCK, "MSFT")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "36")

      val result = stockSensor.execute(null)
      result.getObserverState must be oneOf(StockAbstractSensor.STATE_ABOVE, StockAbstractSensor.STATE_BELOW)
    }
  }

  "The stock low sensor" should {

    "be able to handle null values" in {
      val stockSensor = new StockLowSensor

      stockSensor.setProperty(StockAbstractSensor.STOCK, "MSFT")
      stockSensor.setProperty(StockAbstractSensor.THRESHOLD, "36")

      val result = stockSensor.execute(null)
      result.isSuccess must beTrue

      // might be null when there is no trading
      // this case happens when the us stock exchange is closed and there is no low/high value
      Option(result.getObserverState) must be oneOf(None, Some(StockAbstractSensor.STATE_ABOVE), Some(StockAbstractSensor.STATE_BELOW))
    }
  }
}
