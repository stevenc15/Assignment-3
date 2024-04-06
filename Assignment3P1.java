import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

//Node class
class Node {
    int key;
    Node next;

    boolean locked;

    public Node(int guest){
        this.key=guest;
    }

    //Lock method 
    public synchronized void lock() throws InterruptedException {
        while (locked) {
            wait(); 
        }
        locked = true; 
    }

    //Unlock method
    public synchronized void unlock() {
        locked = false; 
        notify(); 
    }
}

//List implementation
class CoarseList {

    //list variables
    private Node head;
    private Lock lock = new ReentrantLock();
    private int npresents;
    private int nletters;
    
    //constructor
    public CoarseList(int npresents) {
        this.npresents=npresents;
        this.nletters=0;
        head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

//1. Take a present from the unordered bag and 
//add it to the chain in the correct location by hooking it to the
//predecessor’s link.
    public boolean action1(int item) throws InterruptedException {
        int key = item;
        head.lock();
        Node pred = head;
        try {
            Node curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    return false;
                }
                Node newNode = new Node(item);
                newNode.next = curr;
                pred.next = newNode;
                npresents-=1;
                return true;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

//2. Write a “Thank you” card to a guest and remove the present from the chain.
    public boolean action2(int item)throws InterruptedException {
        Node pred = null, curr = null;
        int key = item;
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while (curr.key < key) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.key == key) {
                    pred.next = curr.next;
                    nletters+=1;
                    return true;
                }
                return false;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

//Per the Minotaur’s request, check whether a gift with a particular tag was
//present in the chain or not
public boolean action3 (int item)throws InterruptedException{
    Node pred, curr;
    int key = item;
    head.lock();
    try {
    while (true) {
    pred = this.head;
    curr = pred.next;
    curr.lock();
    try {
        while (curr.key < key) {
            pred.unlock();   
            pred = curr; 
            curr = curr.next;
    }
        if (validate(pred, curr)) {
            return (curr.key == key);
        }
    
}finally {
    curr.unlock();
}
    }
    } finally {
        head.unlock();
    }  
}

//helper methods
public boolean validate(Node pred, Node curr){
    Node node = head;
while (node.key <= pred.key) {
    if (node == pred)
        return pred.next == curr;
    node = node.next;
}
return false;
}

public int presentsLeft() {
    return npresents;
}

public int numLetters() {
    return nletters;
}

}

//servant thread
class Servant extends Thread {

    //private variables
    private CoarseList lList;

    //constructor 
    public Servant(CoarseList lList){
       this.lList=lList;
    }

    //Run for each thread
    public void run() {
        while (lList.numLetters()<50000 || lList.presentsLeft()>0){
        
            int item = ThreadLocalRandom.current().nextInt(0, 50000);
            int actionR = ThreadLocalRandom.current().nextInt(1, 3);
        
        if (actionR==1){
            if (lList.presentsLeft()>0)
            try {
                lList.action1(item); 
            } catch (InterruptedException e) {
                e.printStackTrace();      
        }}
        else if (actionR==2){
            try {
                lList.action2(item); 
            } catch (InterruptedException e) {
                e.printStackTrace();      
        }
        }
        else{
            try {
                lList.action3(item); 
            } catch (InterruptedException e) {
                e.printStackTrace();      
        }
        }
    }
    }

}

//main class
public class Assignment3p1 {
	public static void main(String[] args) 
	{

        //number of servants
		int N = 4;

        //num of presents
        int npresents = 50000;

        //concurrent linked list
        CoarseList Presents = new CoarseList(npresents);

        Servant[] threads = new Servant[N];

        System.out.println("Servants have not started working!");
        System.out.println("Number of presents left: " + Presents.presentsLeft());
        System.out.println("Number of cards made: " + Presents.numLetters());

        for (int i=0;i<(N);i++){
            threads[i] = new Servant(Presents);
            threads[i].start();
        }
        
        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n");

        System.out.println("Servants finished working!");
        System.out.println("Number of presents left: " + Presents.presentsLeft());
        System.out.println("Number of cards made: " + Presents.numLetters());

        //check if correct number of letters were written and no presents are left
        if (Presents.numLetters()==50000 && Presents.presentsLeft()==0){
            System.out.println("Success!");
        }
    }    

}
