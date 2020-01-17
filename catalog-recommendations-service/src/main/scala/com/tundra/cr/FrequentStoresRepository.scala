package com.tundra.cr

import com.tundra.cr.repo.Codecs._
import com.tundra.cr.repo._
import com.tundra.cr.model.definitions.types._
import com.tundra.cr.model.definitions._

import com.amazonaws.services.dynamodbv2.model._
import com.github.dwhjames.awswrap.dynamodb._

import com.twitter.util.Future

import vox.api.repo._
import vox.aws.dynamo.codecs._
import vox.aws.dynamo.ScanParameters

import javax.inject.Inject

trait FrequentStoresRepository extends vox.api.repo.Repository {
  def getFrequentStores(storeId: Id, size: Long): Future[Option[FrequentStores]]
  def getFrequentStoresUnion(storeId: Id): Future[Option[FrequentStores]]
}

object FrequentStoresTable {

  object Attributes {
    val hashKey = "id"
    val storeId = "storeId"
    val active  = "active"
  }

  object Indexes {
    val storeactive = "storeactive"
  }

  val tableRequest: CreateTableRequest =
    new CreateTableRequest()
      .withProvisionedThroughput(Schema.provisionedThroughput(2L, 2L))
      .withAttributeDefinitions(
        Schema.numberAttribute(Attributes.hashKey),
        Schema.stringAttribute(Attributes.storeId)
      )
      .withKeySchema(
        Schema.hashKey(Attributes.hashKey)
      )
      .withGlobalSecondaryIndexes(
        new GlobalSecondaryIndex()
          .withIndexName(Indexes.storeactive)
          .withProvisionedThroughput(Schema.provisionedThroughput(2L, 2L))
          .withProjection(
            new Projection()
              .withProjectionType(ProjectionType.ALL)
          )
          .withKeySchema(
            Schema.hashKey(Attributes.storeId),
            Schema.rangeKey(Attributes.active)
          )
      )

}

class FrequentStoresRepositoryDynamo @Inject()(config: DynamoDBConfig)
    extends RepositoryAWSWrapper[Dmlfrequentstores, ItemId, Unit](
      FrequentStoresTable.tableRequest,
      config
    )
    with FrequentStoresRepository {

  import FrequentStoresTable._

  implicit val serializer: EntitySerializer =
    new EntitySerializer(Attributes.hashKey)

  implicit val dynamoExecutionContext = executors.dynamoDBExecutionContext

  private def findFrequentStores(storeId: Id): Future[Seq[FrequentStores]] =
    for {
      records <- mapper
        .query[Dmlfrequentstores](
          mkHashAndRangeKeyQuery[Id](
            Attributes.storeId -> storeId,
            Attributes.active -> new Condition()
              .withComparisonOperator(ComparisonOperator.EQ)
              .withAttributeValueList(new AttributeValue().withN("1"))
          ).withIndexName(Indexes.storeactive)
        )
      items = records.map(fromRecord)
    } yield items

  def getFrequentStores(storeId: Id, size: Long) =
    for {
      items <- findFrequentStores(storeId)
    } yield {
      items
        .filter(_.stores.size == size)
        .sortBy(-_.frequency.get) // we can use Option.get becvause we know it's a Some
        .headOption
    }

  def getFrequentStoresUnion(storeId: Id) =
    for {
      items <- findFrequentStores(storeId)
      union = items.foldLeft(Set.empty[Id])(_ ++ _.stores.toSet).toList
    } yield items.headOption.map { i =>
      FrequentStores(
        id = i.id,
        storeId = i.storeId,
        stores = union,
        frequency = None
      )
    }

  def fromRecord(r: Dmlfrequentstores): FrequentStores =
    FrequentStores(
      id = r.id,
      storeId = r.storeId,
      stores = List(
        r.storeOne.flatMap(DbQuirks.fromPipelineString),
        r.storeTwo.flatMap(DbQuirks.fromPipelineString),
        r.storeThree.flatMap(DbQuirks.fromPipelineString),
        r.storeFour.flatMap(DbQuirks.fromPipelineString),
        r.storeFive.flatMap(DbQuirks.fromPipelineString)
      ).flatten,
      frequency = Some(r.frequency)
    )
}
