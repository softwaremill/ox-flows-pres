package pres

object S075_Channels_performance {
  /*

    Parallel benchmark: 10 000 thread/coroutine/goroutine pairs connected by channels,
    and send 100 000 values from the first thread/coroutine/goroutine in the pair,
    to the second:

                  ╔══════════════════════════════════════════════════╗
    Ox            ╢████████████████████████████████████████████░░░░░░╟ 14.155
    Kotlin        ╢███████████████████████████████░░░░░░░░░░░░░░░░░░░╟ 9.847
    Java built-in ╢██████████████████████████████████████████████████╟ 16.053
    Go            ╢████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░╟ 6.38
                  ╠══════════════════════════════════════════════════╣
                  0                                                  16.053

   */
}
