package com.xkcoding.smida.rxdemo.statistics;

import io.reactivex.Observable;

/**
 * 事件流抽象接口，observer返回对应的可被观察者
 * @author Created by YangYifan on 2021/4/26.
 */
public interface HermesEventStream<E extends HermesEvent> {
    Observable<E> observe();
}
