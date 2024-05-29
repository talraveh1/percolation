package percolation.util;

public interface Queue extends Cloneable {
    void enqueue(int x);
    void enqueue(int x, int y);
    int dequeue();
    int first();
    int last();
    int size();
    void clear();
    int[] arr();
    int start();
    int end();
}
