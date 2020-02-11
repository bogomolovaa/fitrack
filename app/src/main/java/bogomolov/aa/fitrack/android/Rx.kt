package bogomolov.aa.fitrack.android

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

fun worker(runnable: suspend CoroutineScope.() -> Unit) {
    //Completable.fromRunnable(runnable).subscribeOn(Schedulers.io()).subscribe()
    GlobalScope.launch(context = Dispatchers.IO, block =  runnable)
}

fun ui(runnable: () -> Unit) {
    Completable.fromRunnable(runnable).subscribeOn(AndroidSchedulers.mainThread()).subscribe()
}