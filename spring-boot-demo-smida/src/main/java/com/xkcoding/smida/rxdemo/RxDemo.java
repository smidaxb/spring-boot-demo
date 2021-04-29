package com.xkcoding.smida.rxdemo;

import com.alibaba.fastjson.JSON;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Created by YangYifan on 2020/12/21.
 */
@Slf4j
public class RxDemo {

//    public void timeWindowTest() throws Exception{
//        Observable<Integer> source = Observable.interval(50, TimeUnit.MILLISECONDS).map(i -> RandomUtils.nextInt(2));
//        source.window(1, TimeUnit.SECONDS).subscribe(window -> {
//            int[] metrics = new int[2];
//            window.subscribe(i -> metrics[i]++,
//                ,
//                () -> System.out.println("窗口Metrics:" + JSON.toJSONString(metrics)));
//        });
//        TimeUnit.SECONDS.sleep(3);
//    }


    /**
     * base
     * startWith
     */
    public static void baseTest() {
        Observable.create(observableEmitter -> {
            observableEmitter.onNext("str1");
            observableEmitter.onNext("str2");
        }).startWith("str before")
            .subscribe(str -> {
                log.info("consumeStr {}", str);
            });
    }

    /**
     * from
     */
    public static void fromTest() {
        List<String> fruitList = Arrays.asList("apple", "orange");
        Observable.fromIterable(fruitList).subscribe(fruit -> log.info("fruit = {}", fruit));
    }

    /**
     * defer
     */
    public static void deferTest() {
        List<String> fruitList = Arrays.asList("apple", "orange");
        Observable
            .defer((Callable<ObservableSource<?>>) () -> Observable.fromIterable(fruitList))
            .subscribe(fruit -> log.info("fruit = {}", fruit));
    }

    /**
     * filter
     * map
     */
    public static void filterMapTest() {
        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1, 0);
        Observable.fromIterable(list)
            .map(integer -> integer < 2 ? 11 : integer)
            .filter(integer -> integer > 4)
            .subscribe(num -> log.info("比4大的num = {}", num));
    }

    /**
     * flatMap
     */
    public static void flatMapTest() {
        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1, 0);
        Observable.fromIterable(list)
            .flatMap(
                (Function<Integer, ObservableSource<String>>) integer -> {
                    List<String> strings = Arrays.asList(integer + "|", integer + "||");
                    return Observable.fromIterable(strings);
                }).subscribe(string -> {
            log.info("str:{}", string);
        });
    }

    /**
     * contactMap
     * 同步版本的flatMap
     */
    public static void contactMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.single())
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(@NonNull String s) throws Exception {
                    log.info("contactMap : accept : {}", s);
                }
            });
    }

    /**
     * reduce
     * scan
     * scan和reduce都是把上一次操作的结果做为第二次的参数传递给第二次Observable使用
     * 但是scan每次操作之后先把数据输出，然后在调用scan的回调函数进行第二次操作，看例子
     */
    public static void reduceScanTest() {
        log.info("========================= reduce ============================");
        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1);
        Observable.fromIterable(list)
            .reduce(
                (result, num) -> {
                    log.info("开始前： result {}, num = {}", result, num);
                    result += num;
                    return result;
                }).subscribe(integer -> {
            log.info("result:{}", integer);
        });

        log.info("========================= scan ============================");
        Observable.fromIterable(list)
            .scan(
                (result, num) -> {
                    log.info("开始前： result {}, num = {}", result, num);
                    result += num;
                    return result;
                }).skip(2).subscribe(integer -> {
            log.info("result:{}", integer);
        });
    }

    /**
     * window
     * 将Observable的数据分拆成一些Observable窗口，然后把Observable窗口推送给订阅者
     */
    public static void windowTest() throws InterruptedException {
        log.info("=========================count,skip=========================");
        List<Integer> list = Arrays.asList(10, 5, 3, 2, 1, 0);
        //count,skip 每count个为一组，每0..*skip个开始算
        Observable.fromIterable(list).window(2, 3).subscribe(
            windowObservable -> {
                log.info("new window");
                windowObservable
//                    .reduce((sum, num) -> sum += num)
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            log.info("2个打印一组 = {}", integer);
                        }
                    });
            });
        log.info("=========================timeSpan,timeUnit=========================");
        //timeSpan,timeUnit
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Observable.create(observableEmitter -> observableEmitter.onNext("我是生产者........."))
            .window(5000, TimeUnit.MILLISECONDS)
            .subscribe(
                o -> {
                    System.out.println(o);
                    Calendar calendar = Calendar.getInstance();
                    int i = calendar.get(Calendar.SECOND);
                    log.info("我会{}就被唤醒触发...", i);
                });
        countDownLatch.await();
    }

    /**
     * 滑动窗口demo
     */
    public static PublishSubject<Integer> behaviorSubject = PublishSubject.create();

    public static void testWindowSlideMy() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(1);
        }
        behaviorSubject.serialize()
//            .subscribeOn(Schedulers.single())
            // timeSpan 秒作为一个基本块,横向移动
            .window(1000, TimeUnit.MILLISECONDS)
            //将flatMap汇总平铺成一个事件,然后累加成一个Observable<Integer>对象，比如说1s内有10个对象，被累加起来
            .flatMap(
                (Function<Observable<Integer>, ObservableSource<Integer>>) integerObservable ->
                    integerObservable
                        .reduce(1,(sum, num) -> sum += num)
                        .toObservable())
//            .startWith(list)
//            .window(10, 1)
//            //对窗口里面的进行求和,用的scan, 每次累加都会打印出来
//            .flatMap(
//                (Function<Observable<Integer>, ObservableSource<Integer>>) integerObservable ->
//                    integerObservable
//                        .scan((sum, num) -> {
////                            log.info("开始前： result {}, num = {}", sum, num);
////                            log.info("ttttt:{}",Thread.currentThread().getName());
//                            Thread.sleep(100);
//                            return sum += num;
//                        })
//                        .skip(9)
//            )
            .observeOn(Schedulers.from(new ThreadPoolExecutor(1, 2, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10))))
            .subscribe((Integer integer) ->
                // 输出统计数据到日志
                log.info("[{}] call ...... {}", Thread.currentThread().getName(), integer));
//        for (int i = 0; i < 10; i++) {
//            Thread t = new Thread(new myR());
//            t.start();
//        }
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
//        Runnable special = new Runnable() {
//            @Override
//            public void run() {
//                behaviorSubject.onNext(0);
//                executor.schedule(this, 500, TimeUnit.MILLISECONDS);
//            }
//        };
//        executor.submit(special);
        countDownLatch.await(1000, TimeUnit.SECONDS);
    }

    private static class myR implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 60; i++) {
                //200ms生产一个数据，
                Random random = new Random();
//                behaviorSubject.onNext(random.nextInt(5));
                behaviorSubject.onNext(1);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void timeWindowTest() throws Exception {
        Observer<Observable<Integer>> o = new Observer<Observable<Integer>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(Observable<Integer> window) {
                int[] metrics = new int[2];
                window.subscribe(i -> metrics[i]++,
                    throwable -> {
                    },
                    () -> System.out.println("窗口Metrics:" + JSON.toJSONString(metrics)));
                log.info("next");
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        Observable<Integer> source = Observable
            .interval(50, TimeUnit.MILLISECONDS)
            .map(i -> new Random().nextInt(2));
        source
            .window(1, TimeUnit.SECONDS).
            observeOn(Schedulers.computation())
            .subscribe(o);
//            .subscribe(window -> {
//                int[] metrics = new int[2];
//                window.subscribe(i -> metrics[i]++,
//                    throwable -> {
//                    },
//                    () -> System.out.println("窗口Metrics:" + JSON.toJSONString(metrics)));
//            });

        TimeUnit.SECONDS.sleep(5);
    }

    /**
     * asyncSubject
     * AysncSubject只在原始的Observable完成后，发送最后一个数据给Observer，如果Observable发射过程中出现错误终止，AysncSubject将不发送任何数据。
     */
    public static void asyncSubjectTest() {
        Subject aSubject = AsyncSubject.create().toSerialized();

        aSubject.onNext("1");
        aSubject.onNext("2");

        aSubject.subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                System.out.println("1-AsyncSubject:" + s);
            }
        });
        aSubject.onNext("3");
        aSubject.onNext("4");
        aSubject.onNext("5");
        //必须调用，否则Subject不知道什么时候发射完，Observer接收不到数据
        aSubject.onComplete();

        aSubject.subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                System.out.println("2-AsyncSubject:" + s);
            }
        });
    }

    public static void main(String[] args) throws Exception {
//        asyncSubjectTest();
//        reduceScanTest();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
//        testWindowSlide();
        testWindowSlideMy();
//        timeWindowTest();
//        contactMap();
        Thread.sleep(10000);
    }
}
