package utils;

import java.util.Iterator;
import java.util.ListIterator;

public class MyList<N, L> implements Iterable<MyNode<N,L>> {
    private MyNode<N,L> begin;
    private MyNode<N,L> end;

    private final L value;
    private int size;

    public MyList(L value){
        this.begin = null;
        this.value = value;
        this.end = null;
        this.size = 0;
    }

    public MyNode<N, L> getBegin() {
        return begin;
    }

    public void setBegin(MyNode<N, L> begin) {
        this.begin = begin;
    }

    public MyNode<N, L> getEnd() {
        return end;
    }

    public void setEnd(MyNode<N, L> end) {
        this.end = end;
    }

    public L getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isEmpty(){
        return (this.getBegin() == null) && (this.getEnd() == null) && (getSize() == 0);
    }

    public void addNode(){
        // 只改变size
        this.size++;
    }

    public void removeNode(){
        // 只改变size
        this.size--;
    }

    public Iterator<MyNode<N,L>> iterator(){
        return new ListIterator(this.getBegin());
    }

    class ListIterator implements Iterator<MyNode<N,L>>{
        MyNode<N,L> now = new MyNode<>(null);
        MyNode<N,L> next = null;

        public ListIterator(MyNode<N,L> head){
            now.setNext(head);
        }

        public boolean hasNext() {
            return next != null || now.getNext() != null;
        }

        public MyNode<N, L> next() {
            if (next == null) {
                now = now.getNext();
            }
            else {
                now = next;
            }
            next = null;
            return now;
        }

        public void remove() {
            MyNode<N, L> prev = now.getPrev();
            MyNode<N, L> next = now.getNext();
            MyList<N, L> parent = now.getParent();
            if (prev != null) {
                prev.setNext(next);
            }
            else {
                parent.setBegin(next);
            }
            if (next != null) {
                next.setPrev(prev);
            }
            else {
                parent.setEnd(prev);
            }
            parent.removeNode();
            this.next = next;
            now.clear();
        }
    }
}
