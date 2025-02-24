package pres

import org.apache.kafka.clients.producer.ProducerRecord
import ox.flow.Flow
import ox.kafka.KafkaStage.*
import ox.kafka.ProducerSettings

@main def s090_Kafka_publish(): Unit =
  val settings = ProducerSettings.default.bootstrapServers(bootstrapServer)
  Flow
    .repeatEval(randomString())
    .take(1000)
    .map(msg => ProducerRecord[String, String]("t1", msg))
    .mapPublish(settings)
    .tap(metadata => println(s"Published offset: ${metadata.offset()}"))
    .runDrain()
