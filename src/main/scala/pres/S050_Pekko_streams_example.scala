package pres

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.kafka.CommitterSettings
import org.apache.pekko.kafka.ConsumerSettings
import org.apache.pekko.kafka.ProducerMessage
import org.apache.pekko.kafka.ProducerSettings
import org.apache.pekko.kafka.Subscriptions
import org.apache.pekko.kafka.scaladsl.Committer
import org.apache.pekko.kafka.scaladsl.Consumer
import org.apache.pekko.kafka.scaladsl.Consumer.DrainingControl
import org.apache.pekko.kafka.scaladsl.Producer
import org.slf4j.LoggerFactory
import ox.discard
import ox.get
import sttp.client4.*
import sttp.client4.httpclient.HttpClientFutureBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@main def s050_Pekko_streams_example(): Unit =
  val sourceTopic = "t1"
  val destTopic = "t2"
  val group = "g1"

  val logger = LoggerFactory.getLogger("pekko-streams-example")

  given system: ActorSystem = ActorSystem("transfer")
  try
    import system.dispatcher

    val producerSettings =
      ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServer)
    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(bootstrapServer)
        .withGroupId(group)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val backend = HttpClientFutureBackend()

    val stream = Consumer
      .committableSource(consumerSettings, Subscriptions.topics(sourceTopic))
      .mapAsync(5) { commitableMsg =>
        val msg = commitableMsg.record.value()
        val work1 = basicRequest
          .response(asStringOrFail)
          .post(uri"http://localhost:8080/work1")
          .body(msg)
          .send(backend)
        val work2 = basicRequest
          .response(asStringOrFail)
          .post(uri"http://localhost:8080/work2")
          .body(msg)
          .send(backend)

        for {
          response1 <- work1
          response2 <- work2
          result = response1.body + ": " + response2.body
          _ = logger.info(s"Result for $msg: $result")
        } yield ProducerMessage.single(
          new ProducerRecord[String, String](destTopic, null, result),
          commitableMsg.committableOffset
        )
      }
      .via(Producer.flexiFlow(producerSettings))
      .map(_.passThrough)
      .toMat(Committer.sink(CommitterSettings(system)))(DrainingControl.apply)
      .run()
      .streamCompletion

    stream.get().discard
  finally system.terminate().get().discard
end s050_Pekko_streams_example
