/*
 * Copyright  2012 Typesafe, Inc. All rights reserved.
 */

package models.scalatrain

object TestData {

  val munich = new Station("Munich")

  val nuremberg = new Station("Nuremberg")

  val frankfurt = new Station("Frankfurt")

  val cologne = new Station("Cologne")

  val essen = new Station("Essen")

  val ice724MunichTime = Time(8, 50)

  val ice724NurembergTime = Time(10)

  val ice724FrankfurtTime = Time(12, 10)

  val ice724CologneTime = Time(13, 39)

  val ice726MunichTime = Time(7, 50)

  val ice726NurembergTime = Time(9)

  val ice726FrankfurtTime = Time(11, 10)

  val ice726CologneTime = Time(13, 2)

  val ice724 = Train(
    TrainInfo.InterCityExpress(724, 55.13),
    Vector(
      ice724MunichTime -> munich,
      ice724NurembergTime -> nuremberg,
      ice724FrankfurtTime -> frankfurt,
      ice724CologneTime -> cologne
    )
  )

  val ice726 = Train(
    TrainInfo.InterCityExpress(726, 67.28),
    Vector(
      ice726MunichTime -> munich,
      ice726NurembergTime -> nuremberg,
      ice726FrankfurtTime -> frankfurt,
      ice726CologneTime -> essen
    )
  )

  val planner = new JourneyPlanner(Set(ice724, ice726))
}
