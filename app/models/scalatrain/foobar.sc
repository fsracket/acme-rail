/**
 * Created by fabian on 2014-03-14.
 */
val grouped = bySegment.groupBy({case (cId,segment,amount,visits) => segment})
  .foreach({case (segment,gp) => {
  val (sId,_,desc,s,e) = segment
  val amountsVisits = gp.map({case (_,_,amount,visits) => (amount,visits)} )
  val amountStats = new DescriptiveStatistics(amountsVisits.map(_._1).toArray)
  val visitsStats = new DescriptiveStatistics(amountsVisits.map(_._2).toArray)
  logger.info(f"Segment ${segment._1} - avg spend/w: ${amountStats.getMean}%2.2f (dev ${amountStats.getStandardDeviation}%2.2f) - avg visits/w: ${visitsStats.getMean}%2.2f (dev ${visitsStats.getStandardDeviation}%2.2f)")
  val avgAmountDesc = f"$$${amountStats.getMean}%2.2f/week"
  val avgVisitsDesc = f"${visitsStats.getMean}%2.2f visits/week"
  val d1 = DateHelper.ResolveDateId(minTS.toLocalDate)(session)
  val d2 = DateHelper.ResolveDateId(maxTS.toLocalDate)(session)
  //println(s"insert into bi_customer_segment (customer_segment_id , name , description , spend_description , visit_description , valid_from_date_id , valid_to_date_id , generation_date)  values ($sId, '$desc', 'Segment ${sId} $$${s}-$$${e}/week', '$avgAmountDesc', '$avgVisitsDesc', $d1, $d2, DATE('now'))")
  CustomerSegment.create(sId, desc, s"Segment ${sId} $$${s}-$$${e}/week", avgAmountDesc,avgVisitsDesc,minTS.toLocalDate, maxTS.toLocalDate)(session)
  gp.map({case (cId,_,_,_) => cId}).foreach(cId =>
  {
    //println(s"insert into bi_customer_segment_rel (customer_segment_id , customer_id) values ($sId , $cId)")
    CustomerToSegment.create(sId, cId)(session)
  })
}})


val maxWeekly = allTransactions.map(_._2).max mean