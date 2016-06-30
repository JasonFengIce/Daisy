package tv.ismar.daisy.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by huibin on 6/30/16.
 */
public class CountdownManager {
    private static CountdownManager instance;

    private CountdownManager() {

    }

    public static CountdownManager getInstance() {
        if (instance == null) {
            instance = new CountdownManager();
        }
        return instance;
    }

    public Observable<Integer> countdown(int time) {
        if (time < 0) time = 0;
        final int countTime = time;
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime - increaseTime.intValue();
                    }
                })
                .take(countTime);
    }

}
