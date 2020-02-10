package bogomolov.aa.fitrack.android

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object Rx {
    @JvmStatic
    fun worker(runnable: Runnable) {
        Completable.fromRunnable(runnable).subscribeOn(Schedulers.io()).subscribe()
    }

}

fun worker(runnable: ()-> Unit) {
    Completable.fromRunnable(runnable).subscribeOn(Schedulers.io()).subscribe()
}

fun ui(runnable: () -> Unit) {
    Completable.fromRunnable(runnable).subscribeOn(AndroidSchedulers.mainThread()).subscribe()
}