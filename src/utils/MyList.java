package utils;

import java.util.Iterator;
import java.util.LinkedList;
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

    public void removeNode(MyNode<N, L> targetNode) {
        if (targetNode == null) {
            return; // 空节点无需删除
        }

        MyNode<N, L> prev = targetNode.getPrev();
        MyNode<N, L> next = targetNode.getNext();

        // 调整前后节点的链接
        if (prev != null) {
            prev.setNext(next);
        } else {
            // 如果前节点为空，说明目标节点是头节点
            this.begin = next;
        }

        if (next != null) {
            next.setPrev(prev);
        } else {
            // 如果后节点为空，说明目标节点是尾节点
            this.end = prev;
        }

        // 更新链表大小
        this.size--;

        // 清理目标节点
        targetNode.clear();
    }

    public MyNode<N, L> getFirst() {
        // 获取链表头节点
        return this.begin;
    }

    public MyNode<N, L> getLast() {
        // 获取链表尾节点
        return this.end;
    }


    public Iterator<MyNode<N,L>> iterator(){
        return new ListIterator(this.getBegin());
    }

    public void clear() {
        // 遍历链表，逐个清理节点
        MyNode<N, L> current = this.begin;
        while (current != null) {
            MyNode<N, L> next = current.getNext(); // 保存下一个节点
            current.clear(); // 清理当前节点
            current = next;  // 移动到下一个节点
        }
        // 重置链表属性
        this.begin = null;
        this.end = null;
        this.size = 0;
    }

    public void addAll(MyList<N, L> otherList) {
        if (otherList == null || otherList.isEmpty()) {
            return; // 如果传入的链表为空或没有元素，则不做任何操作
        }

        // 如果当前链表为空
        if (this.isEmpty()) {
            this.begin = otherList.getBegin();
            this.end = otherList.getEnd();
        } else {
            // 如果当前链表不为空，连接两个链表
            this.end.setNext(otherList.getBegin());
            otherList.getBegin().setPrev(this.end);
            this.end = otherList.getEnd();
        }

        // 更新链表大小
        this.size += otherList.getSize();
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
