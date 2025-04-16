package io.datatroops.models

import spray.json._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Bid(bidId: Long, userId: Long, itemId: Long, bidAmount: BigDecimal, bidTime: Option[LocalDateTime])

object Bid extends DefaultJsonProtocol {

  implicit object LocalDateTimeFormat extends RootJsonFormat[LocalDateTime] {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    def write(dt: LocalDateTime): JsValue = JsString(dt.format(formatter))
    def read(value: JsValue): LocalDateTime = value match {
      case JsString(str) => LocalDateTime.parse(str, formatter)
      case _ => throw new DeserializationException("Invalid date format for LocalDateTime")
    }
  }

  implicit val bidFormat: RootJsonFormat[Bid] = jsonFormat5(Bid.apply)

  override implicit def immSeqFormat[T: JsonFormat]: RootJsonFormat[Seq[T]] = new RootJsonFormat[Seq[T]] {
    def write(seq: Seq[T]): JsValue = JsArray(seq.map(_.toJson).toVector)

    def read(json: JsValue): Seq[T] = json match {
      case JsArray(elements) => elements.map(_.convertTo[T])
      case _ => throw new DeserializationException("Expected an array for immutable.Seq")
    }
  }
}
