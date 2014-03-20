import models.scalatrain.TestData

object test {
 
 import scala.collection.BitSet
 import scala.collection.mutable.HashMap
 import scala.collection.Map
 
 import com.google.gson.Gson

	//BitSet(1,2,3)(1)
	
	val gson = new Gson()                     //> gson  : com.google.gson.Gson = {serializeNulls:falsefactories:[Factory[typeH
                                                  //| ierarchy=com.google.gson.JsonElement,adapter=com.google.gson.internal.bind.T
                                                  //| ypeAdapters$25@663e89c3], com.google.gson.internal.bind.ObjectTypeAdapter$1@
                                                  //| 13c695a6, com.google.gson.internal.Excluder@528acf6e, Factory[type=java.lang
                                                  //| .String,adapter=com.google.gson.internal.bind.TypeAdapters$13@17386918], Fac
                                                  //| tory[type=java.lang.Integer+int,adapter=com.google.gson.internal.bind.TypeAd
                                                  //| apters$7@787bb290], Factory[type=java.lang.Boolean+boolean,adapter=com.googl
                                                  //| e.gson.internal.bind.TypeAdapters$3@10849bc], Factory[type=java.lang.Byte+by
                                                  //| te,adapter=com.google.gson.internal.bind.TypeAdapters$5@4720d62b], Factory[t
                                                  //| ype=java.lang.Short+short,adapter=com.google.gson.internal.bind.TypeAdapters
                                                  //| $6@3cccc588], Factory[type=java.lang.Long+long,adapter=com.google.gson.inter
                                                  //| nal.bind.TypeAdapters$8@27bc82e7], Factory[type=java.lang.Double+double,adap
                                                  //| ter=com.google.gson.Gson
                                                  //| Output exceeds cutoff limit.
	TestData.planner.hops                     //> res0: Map[models.scalatrain.Station,Set[models.scalatrain.Hop]] = Map(Statio
                                                  //| n(Nuremberg) -> Set(Hop(Station(Nuremberg),Station(Frankfurt),Train(InterCit
                                                  //| yExpress(726,67.28,false),Vector((07:50,Station(Munich)), (09:00,Station(Nur
                                                  //| emberg)), (11:10,Station(Frankfurt)), (13:02,Station(Essen))))), Hop(Station
                                                  //| (Nuremberg),Station(Frankfurt),Train(InterCityExpress(724,55.13,false),Vecto
                                                  //| r((08:50,Station(Munich)), (10:00,Station(Nuremberg)), (12:10,Station(Frankf
                                                  //| urt)), (13:39,Station(Cologne)))))), Station(Frankfurt) -> Set(Hop(Station(F
                                                  //| rankfurt),Station(Essen),Train(InterCityExpress(726,67.28,false),Vector((07:
                                                  //| 50,Station(Munich)), (09:00,Station(Nuremberg)), (11:10,Station(Frankfurt)),
                                                  //|  (13:02,Station(Essen))))), Hop(Station(Frankfurt),Station(Cologne),Train(In
                                                  //| terCityExpress(724,55.13,false),Vector((08:50,Station(Munich)), (10:00,Stati
                                                  //| on(Nuremberg)), (12:10,Station(Frankfurt)), (13:39,Station(Cologne)))))), St
                                                  //| ation(Munich) -> Set(Hop
                                                  //| Output exceeds cutoff limit.
	gson.toJson(TestData.planner.hops)        //> res1: String = {"key1":{"name":"Nuremberg"},"value1":{"bitmap":33792,"elems"
                                                  //| :[{"key":{"from":"Nuremberg","to":"Frankfurt","train":{"info":{"number":726,
                                                  //| "price":67.28,"hasWifi":false},"schedule":{},"stations":{},"backToBackStatio
                                                  //| ns":{},"departureTimes":{}},"departureTime":{"hours":9,"minutes":0,"asMinute
                                                  //| s":540,"toString":"09:00","bitmap$0":true},"arrivalTime":{"hours":11,"minute
                                                  //| s":10,"asMinutes":670,"toString":"11:10","bitmap$0":true}},"hash":1760849098
                                                  //| },{"key":{"from":"Nuremberg","to":"Frankfurt","train":{"info":{"number":724,
                                                  //| "price":55.13,"hasWifi":false},"schedule":{},"stations":{},"backToBackStatio
                                                  //| ns":{},"departureTimes":{}},"departureTime":{"hours":10,"minutes":0,"asMinut
                                                  //| es":600,"toString":"10:00","bitmap$0":true},"arrivalTime":{"hours":12,"minut
                                                  //| es":10,"asMinutes":730,"toString":"12:10","bitmap$0":true}},"hash":-74377048
                                                  //| 1}],"size0":2},"key2":{"name":"Frankfurt"},"value2":{"bitmap":16777344,"elem
                                                  //| s":[{"key":{"from":"Fran
                                                  //| Output exceeds cutoff limit.
}