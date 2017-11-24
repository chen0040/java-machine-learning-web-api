package com.github.chen0040.ml.linearalg;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class Tuple2<T1, T2> {
    private T1 item1;
    private T2 item2;

    public Tuple2(T1 item1, T2 item2){
        this.item1 = item1;
        this.item2 = item2;
    }

    public T1 getItem1() {
        return item1;
    }

    public void setItem1(T1 item1) {
        this.item1 = item1;
    }

    public T2 getItem2() {
        return item2;
    }

    public void setItem2(T2 item2) {
        this.item2 = item2;
    }

    public static <U1, U2> Tuple2<U1, U2> create(U1 item1, U2 item2){
        return new Tuple2<U1, U2>(item1, item2);
    }
}
