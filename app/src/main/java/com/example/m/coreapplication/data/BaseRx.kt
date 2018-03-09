package com.example.m.coreapplication.data

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseRx {

    protected fun <T> prepareRx(o: Observable<T>): Observable<T> {
        return o.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    protected fun <T> prepareRx(o: Flowable<T>): Flowable<T> {
        return o.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    protected fun <T> prepareRx(o: Single<T>): Single<T> {
        return o.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    protected fun <T> prepareRx(o: Maybe<T>): Maybe<T> {
        return o.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    protected fun prepareRx(o: Completable): Completable {
        return o.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }
}