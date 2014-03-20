package controllers

import play.api._
import play.api.mvc._
import models.scalatrain.{ Time, Station, TestData, Trip, Hop, Train, TrainInfo }
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json
import play.api.cache.Cache
import play.api.Play.current
import scala.collection.mutable.HashMap
import scala.concurrent.Future
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.duration.SECONDS

//
//class AuthenticatedRequest[A](val username: String, request: Request[A]) extends WrappedRequest[A](request)
//
//object Authenticated extends ActionBuilder[AuthenticatedRequest] {
//  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]) = {
//    request.session.get("username").map { username =>
//      block(new AuthenticatedRequest(username, request))
//    } getOrElse {
//      Future.successful(Unau)
//    }
//  }
//}

object Application extends Controller {

  object SecuredAction extends ActionBuilder[Request]
    with play.api.mvc.Results {
    def invokeBlock[A](req: Request[A], block: (Request[A]) => Future[SimpleResult]) = {

      println(req.cookies)
      val cookies = req.cookies

      if (!(isAdminUser(cookies))) {
        Future.successful(Unauthorized("you are not allowed to do this. go away"))
      } else
        block(req)
    }
  }

  def isAdminUser(cookies: Cookies) = (cookies.get("user").map(c => c.value).getOrElse("")) == "admin"

  def loginForm = Form(
    mapping(
      "userName" -> text,
      "password" -> text)(LoginInfo.apply)(LoginInfo.unapply))

  case class LoginInfo(userName: String, password: String) {}

  def logout = SecuredAction {
    Redirect(routes.Application.index()).discardingCookies(DiscardingCookie("user"))
  }

  def hops = SecuredAction {
    val allHops = TestData.planner.hops.flatMap(x => x._2).toSeq.sortBy(h => h.train.info.number)

    Ok(views.html.hops(allHops.toSeq))

  }

  def login = Action {
    Ok(views.html.login(loginForm, ""))
  }

  def doLogin = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithError =>
          {
            BadRequest(views.html.login(formWithError, "Bad username or password."))
          },
        loginInfo =>
          {
            if (loginInfo.userName.trim == "admin" && loginInfo.password.trim == "password") {
              Ok(views.html.index("Welcome to your new home", true)).withCookies(Cookie("user", "admin"))
            } else {

              Ok(views.html.login(loginForm, "Bad username or password."))
            }
          })
  }

  def index = Action {
    implicit request =>
      {
        val isAdmin = isAdminUser(request.cookies)
        Ok(views.html.index("Your new application is ready.", isAdmin))

      }

    //routes.Application.
  }

  def stations = Action {
    val p = TestData.planner
    Ok(views.html.stations(p.stations))

  }

  def trips(name: String) = Action {
    val s = Station(name)
    val hops = TestData.planner.hops(s)
    println(hops)
    Ok(views.html.trips(s, hops))

  }
  //
  //  Cache.set("user.5", user)
  //val u = Cache.getAs[User]("user.5")
  //Cache.remove("user.5")

  def historyFromCache = {
    val maybeHistory = Cache.getAs[HashMap[SearchTripInfo, Long]]("history")
    maybeHistory.getOrElse(new HashMap[SearchTripInfo, Long]())
  }

  case class WeatherAt(name: String, temp: Double, desc: String)

  def tempsAt(city: String) = {

    val (lat, long) = GeoInfo.llLookup(city.toLowerCase())

    val url = "https://api.forecast.io/forecast/" + GeoInfo.API_KEY + "/" + lat + "," + long
    println(url)
    val holder: WSRequestHolder = WS.url(url)

    val fResult =
      holder.get().map {
        response =>
          {
            val json = response.json
            val currentlyVal = json \ "currently"
            val desc = (currentlyVal \ "summary").as[String]
            val temp = (currentlyVal \ "temperature").as[Double]
            WeatherAt(city, temp, desc)
          }
      }
    Await.result(fResult, Duration(30, SECONDS))
  }

  def tempsAt(cities: Set[String]): Map[String, WeatherAt] =
    {
      cities.map(city => (city, tempsAt(city))).toMap
    }

  def searchResult(searchInfo: SearchTripInfo): (Seq[Trip], WeatherAt, WeatherAt) =
    {

      val currTimestamp: Long = System.currentTimeMillis / 1000

      val history = historyFromCache
      history.put(searchInfo, currTimestamp)
      //println("history before filter: " + history)

      //val recentHistory = history.filter { case (k, v) => currTimestamp - v < 300 } //remove old history entries
      //println("history after filter: " + recentHistory)
      Cache.set("history", history)

      val trips = TestData.planner.connections(searchInfo.from, searchInfo.to, Time(searchInfo.hour, searchInfo.minute)).toSeq
      val sortedTrips =
        searchInfo.sortOption match {
          case 0 => trips
          case 1 => trips.sortBy(t => t.Time)
          case 2 => trips.sortBy(t => t.Price)
        }

      //get the weather for the from and to stations
      val fromCity = trips.head.hops.head.from.name
      val toCity = trips.head.hops.last.to.name

      (sortedTrips, tempsAt(fromCity), tempsAt(toCity))

    }

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  case class TripInfo(trainNumber: Int, price: Double, tripDuration: Int, from: String, to: String)
  implicit val tripInfoWrites = Json.writes[TripInfo]

  

  def apiSearch(from: String, to: String, tNum: Option[Int]) = Action {
    implicit request =>
      {
        val time = tNum.map(v => {
          val hour = v / 100
          val minute = v % 100
          Time(hour, minute)
        }).getOrElse(Time(0, 1))

        val (trips, _, _) = searchResult(SearchTripInfo(Station(from), Station(to), time.hours, time.minutes, 0))
        val tripInfos = trips.map(t =>

          {
            val firstHop = t.hops.head
            val lastHop = t.hops.last
            TripInfo(firstHop.train.info.number, t.Price, t.Time, firstHop.from.name, lastHop.to.name)
          })
          
           Ok(Json.toJson(tripInfos))
      }
  }
     

  def getSearchTrip(from: String, to: String, hour: Int, minute: Int, sortBy: Int) = Action {

    val tripsData = SearchTripInfo(Station(from), Station(to), hour, minute, sortBy)
    val (trips, weatherFrom, weatherTo) = searchResult(tripsData)

    Ok(views.html.trips2(tripsData, trips, weatherFrom, weatherTo))
  }

  def doSearchTrip = Action {
    implicit request =>
      searchTripsForm.bindFromRequest.fold(
        formWithError =>
          {
            BadRequest(views.html.searchtrip(TestData.planner.stations.map(s => (s.name, s.name)).toList,
              formWithError))
          },
        tripsData =>
          {
            val (trips, weatherFrom, weatherTo) = searchResult(tripsData)
            Ok(views.html.trips2(tripsData, trips, weatherFrom, weatherTo))

          })

  }

  def searchHistory = Action {
    val history = historyFromCache
    val searchTripInfos = history.map(x => x._1)
    //println("searchTripInfos: " + searchTripInfos.toString)
    Ok(views.html.searchHistory(searchTripInfos.toSeq))
  }

  def searchTrip = Action {
    Ok(views.html.searchtrip(TestData.planner.stations.map(s => (s.name, s.name)).toList,
      searchTripsForm))

  }

  //def create = Action {
  //implicit request =>
  //userForm.bindFromRequest.fold(
  //formWithErrors =>
  //BadRequest(views.html.user(formWithErrors)),
  //userData =>
  //Redirect(routes.Application.home())
  //)
  //}

  def rangeCheck(from: Int, to: Int) = (x: Int) => x >= from && x <= to

  val searchTripsForm = Form(
    mapping("fromStation" -> text.transform[Station](str => Station(str), st => st.name),
      "toStation" -> text.transform[Station](str => Station(str), st => st.name),

      "hour" -> default(number(min = 0, max = 23), 0),
      "minute" -> default(number(min = 1, max = 60), 1), "sort_options" -> default(number, 0))(SearchTripInfo.apply)(SearchTripInfo.unapply)
      verifying ("The departure station must be different from the destination station",
        fields => fields match {
          case SearchTripInfo(from, to, hour, minute, sortOption) => from.name != to.name
        }))

  case class SearchTripInfo(from: Station, to: Station, hour: Int, minute: Int,
    sortOption: Int) {
    def readableSortBy: String =
      sortOption match {
        case 0 => "None"
        case 1 => "Time"
        case 2 => "Price"
      }
  }


  object GeoInfo {

    def API_KEY = "7f8233e55259bf6f6a9f424a2c7a2ecf"

    def mapsInfo = List(("munich", (48.135125, 11.581981)),
      ("cologne", (50.937531, 6.960279)),
      ("frankfurt", (50.110922, 8.682127)),
      ("nuremberg", (49.452030, 11.076750)),
      ("essen", (51.455643, 7.011555))).toMap

    def llLookup(name: String) = mapsInfo.get(name.toLowerCase).get

  }
}
//
//def admin = SecuredAction.async {
//Ok("Hello administrator.")
//}