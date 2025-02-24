package pres

import org.apache.kafka.clients.producer.ProducerRecord
import ox.*

import ox.kafka.*
import ox.flow.Flow
import java.util.Properties
import scala.jdk.CollectionConverters.*
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic

val bootstrapServer = "localhost:9092"

@main def createTopic(): Unit =
  val props = new Properties()
  props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)

  val adminClient = AdminClient.create(props)
  try
    val newTopic = new NewTopic("t2", 1, 1: Short)
    adminClient.createTopics(Seq(newTopic).asJava).all().get()
    println(s"Topic created successfully!")
  finally adminClient.close()
end createTopic

@main def publish(): Unit =
  val topic = "t1"

  timedWithLabel("publish"):
    import KafkaStage.*

    val settings = ProducerSettings.default.bootstrapServers(bootstrapServer)
    Flow
      .repeatEval(randomString())
      // 100 bytes * 10000000 = 1 GB
      .take(10_000_000)
      .map(msg => ProducerRecord[String, String](topic, msg))
      .mapPublish(settings)
      .runDrain()
end publish
