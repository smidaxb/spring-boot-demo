package com.xkcoding.smida.rxdemo;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by YangYifan on 2021/4/29.
 */
@Slf4j
public class RxDemo2 {
    public static void baseCase() {
        Observable.create(
            new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> observableEmitter) throws Exception {
                    log.info("create observable,send data");
                    observableEmitter.onNext("str1");
                    observableEmitter.onNext("str2");
                    observableEmitter.onComplete();
                }
            })
//            .observeOn(Schedulers.computation())
//            .subscribeOn(Schedulers.io())
//            .map(new Function<String, String>() {
//                @Override
//                public String apply(String s) throws Exception {
//                    return s.toUpperCase();
//                }
//            })
            .subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable disposable) {
                    log.info("开始订阅");
                }

                @Override
                public void onNext(String s) {
                    log.info("接收到 {} ", s);
                }

                @Override
                public void onError(Throwable throwable) {
                    log.info("发生异常");
                }

                @Override
                public void onComplete() {
                    log.info("所有事件结束");
                }
            });
    }

    public static void main(String[] args) throws InterruptedException {
//        baseCase();
        testWindowSlideMy();
        Thread.sleep(500000);
//        publishSubject.onComplete();


    }


    /**
     * 滑动窗口Demo演示
     */
    public static PublishSubject<Integer> publishSubject = PublishSubject.create();

    public static void testWindowSlideMy() throws InterruptedException {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(1);
        }

        publishSubject
            // timeSpan 秒作为一个基本块,横向移动
            .window(1000, TimeUnit.MILLISECONDS)
            .flatMap(new Function<Observable<Integer>, ObservableSource<? extends Integer>>() {
                @Override
                public ObservableSource<? extends Integer> apply(Observable<Integer> integerObservable) throws Exception {
                    return integerObservable
                        .reduce(0, (sum, num) -> sum += num)
                        .toObservable();
                }
            })
            .startWith(list)
            .window(10, 1)
            //对窗口里面的进行求和,用的scan, 每次累加都会打印出来
            .flatMap(
                (Function<Observable<Integer>, ObservableSource<Integer>>) integerObservable ->
                    integerObservable
                        .scan(0, (sum, num) -> {
                            Thread.sleep(100);
                            return sum += num;
                        })
                        .skip(10)
            )
            .observeOn(Schedulers.from(new ThreadPoolExecutor(1, 2, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10))))
            .subscribe((Integer integer) ->
                // 输出统计数据到日志
                log.info("onNext ...... {}", integer));


        for (int i = 0; i < 1; i++) {
            Thread t = new Thread(new Runnable() {
                int i=0;
                @SneakyThrows
                @Override
                public void run() {
                    while (true) {
                        publishSubject.onNext(1);
                        Thread.sleep(200);
                        if (++i == 100) {
                            publishSubject.onComplete();
                            Thread.sleep(100000);
                        }
                    }
                }
            });
            t.start();
        }
    }

}
