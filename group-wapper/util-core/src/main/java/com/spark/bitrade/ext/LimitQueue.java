package com.spark.bitrade.ext;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * 线程安全的固定队列
 * @author yangch
 * @time 2018.08.20 9:27
 */
public class LimitQueue<E>{

    private int limit; // 队列长度
    AtomicInteger count = new AtomicInteger(0);

    //private LinkedList<E> queue = new LinkedList<E>();
    private Queue<E> queue = new ConcurrentLinkedQueue<E>();


    public LimitQueue(int limit){
        this.limit = limit;
    }

    /**
     * 入列：当队列大小已满时，把队头的元素poll掉
     */
//    public synchronized void offer(E e){
//        if(queue.size() >= limit){
//            queue.poll();
//        }
//        queue.offer(e);
//    }
    public synchronized void offer(E e){
        if(count.get() >= limit){
            if(queue.poll()!=null){
                count.decrementAndGet();
            }
        }
        if(queue.offer(e)){
            count.incrementAndGet();
        }
    }

    public E poll(){
        E e = queue.poll();
        if(e!=null){
            count.decrementAndGet();
        }
        return e;
    }

    public int getLimit() {
        return limit;
    }

    public int size() {
        return count.get();
        //return queue.size();
    }

    public synchronized List<E> pollAll(){
        if(queue.isEmpty()){
            return null;
        }

        List list = new ArrayList();
        while (queue.isEmpty()==false) {
            list.add(queue.poll());
            count.decrementAndGet();
        }

        return list;
    }

    public synchronized List<E> getAll(){
        if(queue.isEmpty()){
            return null;
        }

        List list = new ArrayList(count.get());
        queue.forEach(t->list.add(t));

        return list;
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}
