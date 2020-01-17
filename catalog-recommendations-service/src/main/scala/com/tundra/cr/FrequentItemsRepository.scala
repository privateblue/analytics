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
  def getFrequentItemsUnion(productId: Id, storeId: Id): Future[Option[FrequentItems]]
}

object FrequentItemsTable {

  object Attributes {
    val hashKey          = "id"
    val productIdStoreId = "productIdStoreId"
    val active           = "active"
  }

  object Indexes {
    val prodactive = "prodactive"
  }

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
          .withIndexName(Indexes.prodactive)
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

}

class FrequentItemsRepositoryDynamo @Inject()(config: DynamoDBConfig)
    extends RepositoryAWSWrapper[Dmlfrequentitems, ItemId, Unit](
      FrequentItemsTable.tableRequest,
      config
    )
    with FrequentItemsRepository {

  import FrequentItemsTable._

  implicit val serializer: EntitySerializer =
    new EntitySerializer(Attributes.hashKey)

  implicit val dynamoExecutionContext = executors.dynamoDBExecutionContext

  def queryFrequentItems(productId: Id, storeId: Id): Future[Seq[FrequentItems]] =
    for {
      records <- mapper
        .query[Dmlfrequentitems](
          mkHashAndRangeKeyQuery[Id](
            Attributes.productIdStoreId -> s"$productId$psSeparator$storeId",
            Attributes.active -> new Condition()
              .withComparisonOperator(ComparisonOperator.EQ)
              .withAttributeValueList(new AttributeValue().withN("1"))
          ).withIndexName(Indexes.prodactive)
        )
      items = records.map(recordToModel)
    } yield items

  def getFrequentItems(productId: Id, storeId: Id, size: Long) =
    for {
      items <- queryFrequentItems(productId, storeId)
    } yield {
      items
        .filter(_.products.size == size)
        .sortBy(-_.frequency.get) // we can use Option.get becvause we know it's a Some
        .headOption
    }

  def getFrequentItemsUnion(productId: Id, storeId: Id) =
    for {
      items <- queryFrequentItems(productId, storeId)
      union = items.foldLeft(Set.empty[Id])(_ ++ _.products.toSet).toList
    } yield items.headOption.map { i =>
      FrequentItems(
        id = i.id,
        productIdStoreId = i.productIdStoreId,
        products = union,
        frequency = None
      )
    }

  val psSeparator = '/'

  def recordToModel(r: Dmlfrequentitems): FrequentItems =
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
