package pres

import ox.*
import ox.channels.{Channel, selectOrClosed}

import scala.concurrent.duration.DurationInt

@main def s070_Channels(): Unit =
  val ch1 = Channel.bufferedDefault[Int]
  val ch2 = Channel.bufferedDefault[Int]

  supervised:
    def startFork(ch: Channel[Int], start: Int, end: Int): Unit =
      forkDiscard:
        for i <- start until end do
          ch.send(i)
          sleep(100.millis)
        ch.done()

    startFork(ch1, 0, 5)
    startFork(ch2, 40, 45)

    for i <- 0 to 11 do println(selectOrClosed(ch1, ch2))
