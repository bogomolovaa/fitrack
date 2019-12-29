package bogomolov.aa.fitrack.core;

import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Rx {
    public static <T> void rx(Supplier<T> supplier, Consumer<T> consumer) {
        Single.create((SingleEmitter<Optional<T>> emitter) -> {
            emitter.onSuccess(Optional.of(supplier.get()));
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Optional<T>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Optional<T> t) {
                consumer.accept(t.get());
            }

            @Override
            public void onError(Throwable e) {
                Log.i("test","error "+e.getMessage());
            }
        });
    }

    public static void worker(Runnable runnable){
        Completable.fromRunnable(runnable).subscribeOn(Schedulers.io()).subscribe();
    }

    private static class Optional<T> {
        T t;

        private Optional(T t) {
            this.t = t;
        }

        public static <T> Optional<T> of(T t){
            return new Optional<>(t);
        }

        public T get() {
            return t;
        }
    }

    public interface Consumer<T> {
        void accept(T t);
    }

    public interface Supplier<T> {
        T get();
    }
}
