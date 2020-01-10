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

trait FrequentItemsRepository extends vox.api.repo.Repository {
  def getFrequentItems(productId: Id, storeId: Id, size: Long): Future[Option[FrequentItems]]
  def getFrequentItemsAll(productId: Id, storeId: Id): Future[Option[FrequentItems]]
}

class FrequentItemsRepositoryDynamo @Inject()(config: DynamoDBConfig)
    extends RepositoryAWSWrapper[Dmlfrequentitems, ItemId, Unit](
      FrequentItemsRepositoryDynamo.tableRequest,
      config
    )
    with FrequentItemsRepository {

  import FrequentItemsRepositoryDynamo._

  implicit val serializer: EntitySerializer =
    new EntitySerializer(Attributes.hashKey)

  implicit val dynamoExecutionContext = executors.dynamoDBExecutionContext

  private def findFrequentItems(productId: Id, storeId: Id): Future[Seq[FrequentItems]] =
    for {
      records <- mapper
        .query[Dmlfrequentitems](
          mkHashAndRangeKeyQuery[Id](
            Attributes.productIdStoreId -> s"$productId$psSeparator$storeId",
            Attributes.active -> new Condition()
              .withComparisonOperator(ComparisonOperator.EQ)
              .withAttributeValueList(new AttributeValue().withN("1"))
          ).withIndexName(prodactiveIndexName)
        )
      items = records.map(fromRecord)
    } yield items

  def getFrequentItems(productId: Id, storeId: Id, size: Long) =
    for {
      items <- findFrequentItems(productId, storeId)
    } yield {
      items
        .filter(_.products.size == size)
        .sortBy(-_.frequency.get) // we can use Option.get becvause we know it's a Some
        .headOption
    }

  def getFrequentItemsAll(productId: Id, storeId: Id) =
    for {
      items <- findFrequentItems(productId, storeId)
      union = items.foldLeft(Set.empty[Id])(_ ++ _.products.toSet).toList
    } yield items.headOption.map { i =>
      FrequentItems(
        id = i.id,
        productIdStoreId = i.productIdStoreId,
        products = union,
        frequency = None
      )
    }

}

object FrequentItemsRepositoryDynamo {

  object Attributes {
    val hashKey          = "id"
    val productIdStoreId = "productIdStoreId"
    val active           = "active"
  }

  val prodactiveIndexName = "prodactive"

  val tableRequest: CreateTableRequest =
    new CreateTableRequest()
      .withProvisionedThroughput(Schema.provisionedThroughput(2L, 2L))
      .withAttributeDefinitions(
        Schema.numberAttribute(Attributes.hashKey),
        Schema.stringAttribute(Attributes.productIdStoreId)
      )
      .withKeySchema(
        Schema.hashKey(Attributes.hashKey)
      )
      .withGlobalSecondaryIndexes(
        new GlobalSecondaryIndex()
          .withIndexName(prodactiveIndexName)
          .withProvisionedThroughput(Schema.provisionedThroughput(2L, 2L))
          .withProjection(
            new Projection()
              .withProjectionType(ProjectionType.ALL)
          )
          .withKeySchema(
            Schema.hashKey(Attributes.productIdStoreId),
            Schema.rangeKey(Attributes.active)
          )
      )

  val psSeparator = '/'

  def fromRecord(r: Dmlfrequentitems): FrequentItems =
    FrequentItems(
      id = r.id,
      productIdStoreId = r.productIdStoreId,
      products = List(
        r.itemOne.flatMap(DbQuirks.fromPipelineString),
        r.itemTwo.flatMap(DbQuirks.fromPipelineString),
        r.itemThree.flatMap(DbQuirks.fromPipelineString),
        r.itemFour.flatMap(DbQuirks.fromPipelineString),
        r.itemFive.flatMap(DbQuirks.fromPipelineString)
      ).flatten,
      frequency = Some(r.frequency)
    )
}
