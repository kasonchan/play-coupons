package controllers

import models.{Coupon, User}
import play.api.Logger
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by kasonchan on 5/2/15.
 */
object Coupons extends Controller {

  /**
   * List
   * Performs get request
   * If the request is successful and valid, a list of coupons is returned and
   * be shown
   * Otherwise, none will be returned and error message will be shown
   * @return Action[AnyContent]
   */
  def list: Action[AnyContent] = Action.async { request =>
    val coupons: Future[Option[Seq[Coupon]]] =
      Coupon.findAll("http://api.bluepromocode.com/v2/promotions")

    request.session.get("username").map { username =>
      Logger.info("Coupons.list - " + username)

      coupons.map {
        cs => cs match {
          case Some(s) => Ok(views.html.coupons.list(Some(username))(s))
          case None => Ok(views.html.coupons.list(Some(username))(Seq()))
        }
      }
    }.getOrElse {
      Logger.info("Coupons.list - new session")

      coupons.map {
        cs => cs match {
          case Some(s) => Ok(views.html.coupons.list(None)(s))
          case None => Ok(views.html.coupons.list(None)(Seq()))
        }
      }
    }
  }

}
