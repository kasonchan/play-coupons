package models

import models.http.HTTP
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by kasonchan on 4/24/15.
 */
case class Savings(percentOff: Option[Double],
                   amountOff: Option[Double],
                   types: Seq[String])

case class Merchant(name: String)

case class Coupon(savings: Savings, merchant: Merchant)

object Coupon extends HTTP {

  /**
   * Savings
   * Retrieves savings amount off, percent off and types if any
   * @param c JsObject
   * @return Savings
   */
  private def savings(c: JsObject): Savings = {
    Savings(((c \ "savings").as[JsObject] \ "amountOff").asOpt[Double],
      ((c \ "savings").as[JsObject] \ "percentOff").asOpt[Double],
      ((c \ "savings").as[JsObject] \ "types").as[Seq[String]])
  }

  /**
   * Merchant
   * Retrieves merchant name and logo
   * @param c JsObject
   * @return Merchant
   */
  private def merchant(c: JsObject): Merchant = {
    Merchant(((c \ "merchant").as[JsObject] \ "name").as[String])
  }

  /**
   * Findall
   * Performs get request with the parameter url
   * Parses the response body
   * Extract the coupons information
   * If everything is valid, findall will return the sequence of coupon
   * Otherwise it will return None
   * @param url String
   * @return Future[Option[Seq[Coupon]]]
   */
  def findAll(url: String): Future[Option[Seq[Coupon]]] = {

    val apiFuture: Future[WSResponse] =
      getRequest(url)

    apiFuture.map {
      response => {
        val json: JsValue = Json.parse(response.body)

        val promotions: Option[JsArray] = (json \ "promotions").asOpt[JsArray]

        promotions match {
          case Some(ps: JsArray) =>
            val coupons: Option[Seq[JsObject]] =
              (json \ "promotions").asOpt[Seq[JsObject]]

            coupons match {
              case Some(coupon) =>
                val c = coupon.map { c =>
                  Coupon(
                    savings(c),
                    merchant(c))
                }

                Logger.info(c.toString)
                Some(c)
              case None =>
                Logger.info("No coupons.")
                None
            }
          case None =>
            Logger.info("No promotions.")
            None
        }
      }
    }
  }

}