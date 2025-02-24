package pres

import ox.*
import ox.channels.{selectOrClosed, Channel, ChannelClosed}
import ox.flow.Flow

import scala.concurrent.duration.DurationInt

@main def demo1(): Unit =
  Flow
    .fromValues(11, 24, 51, 76, 78, 9, 1, 44)
    .map(_ + 3)
    .filter(_ % 2 == 0)
    .intersperse(5)
    .mapStateful(0) { (state, value) =>
      val newState = state + value
      (newState, newState)
    }
    .runToList()
    .pipe(println)

@main def demo2(): Unit =
  val f1 = Flow.tick(1.second, "tick1")
  val f2 = Flow.tick(2.seconds, "tick2")

  f1.merge(f2).runForeach(println)

@main def demo3(): Unit =
  val loopingLetters = Flow.repeat('a' to 'z').mapConcat(identity)
  val numbers = Flow.iterate(0)(_ + 1)
  loopingLetters.zip(numbers).take(100).runForeach(println)

@main def demo4(): Unit =
  def sendHttpRequest(data: String) = ???
  Flow
    .fromInputStream(this.getClass().getResourceAsStream("/data.txt"))
    .linesUtf8
    .mapPar(4)(name => sendHttpRequest(name))
    .runDrain()

@main def demo5(): Unit =
  val f1 = Flow
    .iterate(0)(_ + 1)
    .tap(_ => sleep(1.second))
    .map { i =>
      if i > 3 then throw new RuntimeException("Boom!")
      else i
    }

  val f2 = Flow.tick(500.millis, "tick")

  f1.merge(f2).runForeach(println)
end demo5

@main def demo6(): Unit =
  val ch = Channel.bufferedDefault[Int]
  ch.send(10)
  ch.send(20)
  ch.send(50)
  ch.done()

  supervised:
    val result = Flow
      .fromSource(ch)
      .map(_ + 1)
      .map(_ * 2)
      .filter(_ % 3 == 0)
      .runToChannel()

    println(result.receiveOrClosed())
    println(result.receiveOrClosed())
    println(result.receiveOrClosed())
end demo6

@main def demo7(): Unit =
  val data = Channel.bufferedDefault[Int]
  val errors = Channel.unlimited[Exception]

  Flow
    .usingEmit: emit =>
      // connect to Kafka using data & errors
      forever:
        selectOrClosed(errors.receiveClause, data.receiveClause) match
          case data.Received(i) =>
            if i % 2 == 0 then emit(i)
            else
              emit(i)
              emit(i + 1)
          case errors.Received(e)     => throw e
          case ChannelClosed.Done     => // end
          case ChannelClosed.Error(e) => throw e
    .discard
end demo7
