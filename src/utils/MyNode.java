package utils;

public class MyNode<N,L>{
    // N - MyNode
    // L - MyList
    private MyNode<N, L> prev = null;
    private MyNode<N, L> next = null;

    private N value;
    private MyList<N,L> parent = null;

    public MyNode(N value){
        this.value = value;
    }

    public MyNode(N value, MyList<N,L> parent){
        this.value = value;
        this.parent = parent;
    }

    public MyNode<N, L> getPrev() {
        return prev;
    }

    public void setPrev(MyNode<N, L> prev) {
        this.prev = prev;
    }

    public MyNode<N, L> getNext() {
        return next;
    }

    public void setNext(MyNode<N, L> next) {
        this.next = next;
    }

    public N getValue() {
        return value;
    }

    public void setValue(N value) {
        this.value = value;
    }

    public MyList<N, L> getParent() {
        return parent;
    }

    public void setParent(MyList<N, L> parent) {
        this.parent = parent;
    }

    public void insertBefore(MyNode<N, L> node) {
        this.next = node;
        this.prev = node.prev;
        node.prev = this;
        if (this.prev != null) {
            this.prev.next = this;
        }
        this.parent = node.parent;
        this.parent.addNode();
        if (this.parent.getBegin() == node) {
            this.parent.setBegin(this);
        }
    }

    public void insertAfter(MyNode<N, L> node) {
        this.prev = node;
        this.next = node.next;
        node.next = this;
        if (this.next != null) {
            this.next.setPrev(this);
        }
        this.parent = node.getParent();
        this.parent.addNode();
        if (this.parent.getEnd() == node) {
            this.parent.setEnd(this);
        }
    }

    public void insertAtBegin(MyList<N, L> parent) {
        this.parent = parent;
        if (parent.isEmpty()) {
            parent.setBegin(this);
            parent.setEnd(this);
            parent.addNode();
        }
        else {
            insertBefore(parent.getBegin());
        }
    }

    public void insertAtEnd(MyList<N, L> parent) {
        this.parent = parent;
        if (parent.isEmpty()) {
            parent.setBegin(this);
            parent.setEnd(this);
            parent.addNode();
        }
        else {
            insertAfter(parent.getEnd());
        }
    }

    public MyNode<N, L> removeFromList() {
        parent.removeNode();
        if (parent.getBegin() == this) {
            this.parent.setBegin(this.next);
        }
        if (parent.getEnd() == this) {
            this.parent.setEnd(this.prev);
        }
        if (this.prev != null) {
            this.prev.setNext(this.next);
        }
        if (this.next != null) {
            this.next.setPrev(this.prev);
        }
        clear();
        return this;
    }

    public void clear() {
        this.prev = null;
        this.next = null;
        this.parent = null;
    }
}
