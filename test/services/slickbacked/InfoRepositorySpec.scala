package services.slickbacked

class InfoRepositorySpec extends DBSpec {
//
//  it should "save an info record into the database" in {
//
//    val expectedRuleId = UUID.randomUUID()
//
//    val result = info.insert(salesChannel, expectedRuleId, ruleWithSideEffect1.copy(id = None))
//    result.futureValue should === (expectedRuleId)
//
//    val allRules = rulesRepository.list(salesChannel).futureValue
//
//    allRules.size should === (1)
//    allRules.head should === (ruleWithSideEffect1.copy(id = Some(expectedRuleId.toString.id[RuleWithSideEffects])))
//  }
//
//
//  it should "update a rule in the database" in {
//
//    insertSalesChannelRecord
//
//    val insert1 = rulesRepository.insert(salesChannel, UUID.fromString(ruleId1.value), ruleWithSideEffect1)
//    Await.ready(insert1, waitDuration)
//
//    val insert2 = rulesRepository.insert(salesChannel, UUID.fromString(ruleId2.value), ruleWithSideEffect2)
//    Await.ready(insert2, waitDuration)
//
//    val update = rulesRepository.update(salesChannel, ruleId2, rule2Updated)
//    Await.ready(update, waitDuration)
//
//    val rules = rulesRepository.list(salesChannel).futureValue
//    rules.size should ===(2)
//
//    rules.contains(ruleWithSideEffect1) should ===(true)
//    rules.contains(ruleWithSideEffect2Updated) should ===(true)
//  }
//
//  it should "return last modified date" in {
//
//    val date1 = new DateTime(2017, 5, 1, 12, 20, DateTimeZone.UTC)
//    val date2 = new DateTime(2016, 5, 1, 12, 20, DateTimeZone.UTC)
//    val date3 = new DateTime(2015, 5, 1, 12, 20, DateTimeZone.UTC)
//
//    val rule1 = dataModel.Rule(id = UUID.randomUUID,
//                                   name = "rule1",
//                                   definition =Json.toJson(exp1),
//                                   linked = List.empty,
//                                   lastModified = new Timestamp(date1.getMillis),
//                                   salesChannelId = salesChannel.value,
//                                   rulesSourceId = sourceId.value)
//
//    val rule2 = rule1.copy(id = UUID.randomUUID, lastModified = new Timestamp(date2.getMillis))
//    val rule3 = rule1.copy(id = UUID.randomUUID, lastModified = new Timestamp(date3.getMillis))
//
//    val rules = Seq(rule1, rule2, rule3)
//
//    insertSalesChannelRecord
//    val insertResult = db.run(DBIO.sequence(rules.map(r => dataModel.rules += r))).futureValue
//
//    insertResult.sum should === (3)
//
//    val lastModifiedDate = rulesRepository.getLastModifiedDate(salesChannel).futureValue
//    lastModifiedDate should === (Some(date1))
//
//  }

}
