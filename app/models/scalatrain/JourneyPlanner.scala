/*
 * Copyright  2012 Typesafe, Inc. All rights reserved.
 */

package models.scalatrain

//import scala.collection.immutable.Seq

class JourneyPlanner(trains: Set[Train]) {

  val stations: Set[Station] =
    // Could also be expressed in short notation: trains flatMap (_.stations)
    trains flatMap (train => train.stations)

  val hops: Map[Station, Set[Hop]] = {
    val hops = for {
      train <- trains
      (from, to) <- train.backToBackStations
    } yield Hop(from, to, train)
    hops groupBy (_.from)
  }
  
  
  def trainsAt(station: Station): Set[Train] =
    // Could also be expressed in short notation: trains filter (_.stations contains station)
    trains filter (train => train.stations contains station)

  def stopsAt(station: Station): Set[(Time, Train)] =
    for {
      train <- trains
      (time, `station`) <- train.schedule
    } yield (time, train)

  def isShortTrip(from: Station, to: Station): Boolean =
    trains exists (train =>
      train.stations dropWhile (station => station != from) match {
        case `from` +: `to` +: _      => true
        case `from` +: _ +: `to` +: _ => true
        case _                        => false
      }
    )

  def connections(from: Station, to: Station, departureTime: Time): Set[Trip] = {
    require(from != to, "from and to must not be equal!")
    
    def connections(soFar: Vector[Hop]): Set[Trip] = {
      if (soFar.last.to == to)
        Set(Trip(soFar))
      else {
        val soFarStations = soFar.head.from +: (soFar map (_.to))
        val nextHops = hops.getOrElse(soFar.last.to, Set()) filter (hop =>
          (hop.departureTime >= soFar.last.arrivalTime) && !(soFarStations contains hop.to)
        )
        nextHops flatMap (hop => connections(soFar :+ hop))
      }
    }
    
    val nextHops = hops.getOrElse(from, Set()) filter (_.departureTime >= departureTime)
    nextHops flatMap (hop => connections(Vector(hop)))
  }
}

case class Trip(hops: Seq[Hop]) {
  
  val Time = hops.last.arrivalTime - hops.head.departureTime
  val Price = hops.map(h => h.train.info.price).sum 
  
}

case class Hop(from: Station, to: Station, train: Train) {
  require(from != to, "from must not be equal to to")
  require(train.backToBackStations contains from -> to, "from and to must be back-to-back stations of train")

  val departureTime: Time =
    train departureTimes from

  val arrivalTime: Time =
    train departureTimes to
}
