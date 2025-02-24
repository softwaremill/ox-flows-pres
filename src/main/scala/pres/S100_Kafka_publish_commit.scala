package pres

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import ox.kafka.ConsumerSettings
import ox.kafka.ConsumerSettings.AutoOffsetReset
import ox.kafka.KafkaFlow
import ox.kafka.KafkaStage.*
import ox.kafka.ProducerSettings
import ox.kafka.SendPacket
import ox.par
import sttp.client4.*
import sttp.client4.httpclient.HttpClientSyncBackend

@main def s100_Kafka_publish_commit(): Unit =
  val sourceTopic = "t1"
  val destTopic = "t2"
  val group = "g1"

  val logger = LoggerFactory.getLogger("pekko-streams-example")

  val consumerSettings = ConsumerSettings
    .default(group)
    .bootstrapServers(bootstrapServer)
    .autoOffsetReset(AutoOffsetReset.Earliest)
  val producerSettings =
    ProducerSettings.default.bootstrapServers(bootstrapServer)

  val backend = HttpClientSyncBackend()
  KafkaFlow
    .subscribe(consumerSettings, sourceTopic)
    .mapPar(5) { receivedMsg =>
      val msg = receivedMsg.value

      val (response1: Response[String], response2: Response[String]) = par(
        basicRequest
          .response(asStringOrFail)
          .post(uri"http://localhost:8080/work1")
          .body(msg)
          .send(backend),
        basicRequest
          .response(asStringOrFail)
          .post(uri"http://localhost:8080/work2")
          .body(msg)
          .send(backend)
      )

      val result = response1.body + ": " + response2.body
      logger.info(s"Result for $msg: $result")

      (result, receivedMsg)
    }
    .map((result, received) => SendPacket(ProducerRecord[String, String](destTopic, result), received))
    .mapPublishAndCommit(producerSettings)
    .runDrain()
