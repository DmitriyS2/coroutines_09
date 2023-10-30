import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

// не напечатает. на момент старта дочерней корутины, родительский job уже будет cancelling,
// а значит дочерняя не запустится, т.к. проверяет состояние родителя на своем старте
//fun main() = runBlocking {
//    val job = CoroutineScope(EmptyCoroutineContext).launch {
//        launch {
//            delay(500)
//            println("ok") // <--
//        }
//        launch {
//            delay(500)
//            println("ok")
//        }
//    }
//    delay(100)
//    job.cancelAndJoin()
//}

// не напечатает. Из-за child.cancel, который случается раньше(delay100), чем println(delay500)
//fun main() = runBlocking {
//    val job = CoroutineScope(EmptyCoroutineContext).launch {
//        val child = launch {
//            delay(500)
//            println("ok1") // <--
//        }
//        launch {
//            delay(500)
//            println("ok")
//        }
//        delay(100)
//        child.cancel()
//    }
//    delay(100)
//    job.join()
//}

// нет. т.к. блок try/catch нужно поместить внутрь корутины,
// а в данном случае искл в дочерней корутине не перехватывается в родителе
//fun main() {
//    with(CoroutineScope(EmptyCoroutineContext)) {
//        try {
//            launch {
//                throw Exception("something bad happened")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}

//да. т.к. coroutineScope перехватывает все исключения в дочерних корутинах и предоставляет их в виде Exception
//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            coroutineScope {
//                throw Exception("something bad happened")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}

//да. Хотя supervisorScope не должен пробрасывать исключения из дочерних - наверх
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
           supervisorScope {
                throw Exception("something bad happened")
            }
        } catch (e: Exception) {

            e.printStackTrace() // <--
        }
    }
    Thread.sleep(1000)
}


//нет. т.к. во второй корутине происходит исключение и первая тоже отменяется,
// а из-за delay - throw первой не отработало
//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            coroutineScope {
//                launch {
//                    delay(500)
//                    throw Exception("something bad happened") // <--
//                }
//                launch {
//                    throw Exception("something bad happened")
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//   Thread.sleep(1000)
//}

// throw первой корутины - отработает, т.к. исключение в другой дочерней корутине не останавливает соседнюю
// Но исключение не пробрасывается наверх, поэтому второй случай - не отработает
//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            supervisorScope {
//                launch {
//                    delay(500)
//                    throw Exception("something bad happened2") // <--
//                }
//                launch {
//                    throw Exception("something bad happened1")
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}

// нет. исключение в родительской запустит cancelling, а значит дочерние не запустятся из-за этого статуса
//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        CoroutineScope(EmptyCoroutineContext).launch {
//            launch {
//                delay(1000)
//                println("ok") // <--
//            }
//            launch {
//                delay(500)
//                println("ok")
//            }
//            throw Exception("something bad happened")
//        }
//    }
//    Thread.sleep(1000)
//}

// нет. т.к. throw действует на родительскую, где supervision и отменяет дочерние
//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        CoroutineScope(EmptyCoroutineContext + SupervisorJob()).launch {
//            launch {
//                delay(1000)
//                println("ok") // <--
//            }
//            launch {
//                delay(500)
//                println("ok")
//
//            }
//                 throw Exception("something bad happened")
//        }
//    }
//    Thread.sleep(1000)
//}