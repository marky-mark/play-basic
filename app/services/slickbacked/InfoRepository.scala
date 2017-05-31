package services.slickbacked

import nl.grons.metrics.scala.DefaultInstrumented

trait InfoRepository {
//  def list(salesChannelId: String)(implicit ec: ExecutionContext): Future[Seq[DataModel.Info]]
//  def insert(salesChannelId: SalesChannelId, id: UUID, rule: RuleWithSideEffects)(implicit ec: ExecutionContext): Future[UUID]
//  def update(salesChannelId: SalesChannelId, ruleId: RuleWithSideEffectsId, rule: Rule)(implicit ec: ExecutionContext): Future[Option[RuleWithSideEffectsId]]
//  def getNewRuleId(salesChannelId: SalesChannelId, enrichment: JsObject)(implicit ec: ExecutionContext): Future[Option[UUID]]
//  def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]]
}

class InfoRepositoryImpl(dbProvider: DatabaseProvider) extends InfoRepository with DefaultInstrumented {

  private val db = dbProvider.database
  private val dm = dbProvider.dataModel


//  override def list(salesChannelId: String)(implicit ec: ExecutionContext) : Future[Seq[RuleWithSideEffects]] =  {
//    timeAndIncrementFut(GetRulesTimer, GetRulesCounter) {
//
//      val rules = for {
//        r <- dm.rules if r.salesChannelId === salesChannelId.value
//        e <- dm.sideEffects if e.ruleId === r.id
//      } yield (r, e)
//
//      db.run(rules.result)
//        .map(t => t.groupBy(_._1).mapValues(_.map(_._2)))
//        .map(m => m.map(t => (t._1, t._2.head)))
//        .map(toRuleWithSideEffects)
//        .map(_.toSeq)
//
//    }
//  }

//  override def insert(salesChannelId: SalesChannelId, id: UUID, ruleWithEnrichment: RuleWithSideEffects)(implicit ec: ExecutionContext): Future[UUID] = {
//
//    val dmRule =  dm.Rule(id = id,
//      name = ruleWithEnrichment.rule.name,
//      definition = Json.toJson(ruleWithEnrichment.rule.definition),
//      linked = ruleWithEnrichment.rule.linkedRules.toList,
//      lastModified = new Timestamp((new DateTime(DateTimeZone.UTC)).getMillis),
//      salesChannelId = salesChannelId.value,
//      rulesSourceId = ruleWithEnrichment.sourceId.value)
//
//    val dmSideEffect = dm.SideEffect(ruleId = id, data = ruleWithEnrichment.enrichment)
//
//    timeAndIncrementFut(InsertRuleTimer, InsertRuleCounter) {
//      val insertRule = dm.rules += dmRule
//      val insertSideEffect = dm.sideEffects += dmSideEffect
//      db.run {
//        for {
//          _  <- insertRule andThen insertSideEffect
//        } yield id
//      }
//    }
//  }
//
//  override def getNewRuleId(salesChannelId: SalesChannelId, enrichment: JsObject)(implicit ec: ExecutionContext): Future[Option[UUID]] ={
//    val ruleId = generateId(salesChannelId, enrichment)
//
//    db.run(dm.rules.filter(_.id === ruleId).exists.result).map {
//      case true => None
//      case false => Some(ruleId)
//    }
//  }
//
//
//  override def update(salesChannelId: SalesChannelId, ruleId: RuleWithSideEffectsId, rule: Rule)(implicit ec: ExecutionContext): Future[Option[RuleWithSideEffectsId]] = {
//    val id = UUID.fromString(ruleId.value)
//
//    db.run(dm.rules.filter(_.salesChannelId === salesChannelId.value)
//      .filter(_.id === id)
//      .map(r => (r.name, r.definition, r.linked, r.lastModified))
//      .update((rule.name, Json.toJson(rule.definition), rule.linkedRules.toList, new Timestamp((new DateTime(DateTimeZone.UTC)).getMillis)))).map{
//      case 0 => None
//      case _ => Some(ruleId)
//    }
//  }
//
//  override def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]] = {
//    db.run(dm.rules.filter(_.salesChannelId === salesChannelId.value).map(_.lastModified).max.result)
//      .map(o => o.map(t => new DateTime(t.getTime).toDateTime(DateTimeZone.UTC)))
//  }
//
//  private def toRuleWithSideEffects(rules: Map[dm.Rule, dm.SideEffect]): Iterable[RuleWithSideEffects] = {
//    rules.map { case (rule, enrichment) =>
//      val expression = rule.definition.validate[Expression].get
//      val ruleDef = Rule(name = rule.name, definition = expression, linkedRules = rule.linked)
//      RuleWithSideEffects(id = Some(rule.id.toString.id[RuleWithSideEffects]),
//                          rule = ruleDef,
//                          enrichment = enrichment.data.as[JsObject],
//                          sourceId = rule.rulesSourceId.id[SourceRef])}
//  }
}
