import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.PriorityQueue;

//queue that holds top 5 highest temps
class highQueue {

    //private variables
    private PriorityQueue<Integer> queue;
    private final Lock lock = new ReentrantLock();
    
    //constructor
    public highQueue(){
        this.queue = new PriorityQueue<>((a, b) -> a - b);
    }
    
    //check method to verify if recorded temp makes top 5
    public int checkHigh(int temp){
        int testedTemp = temp;
        lock.lock();
        try {
            if (queue.size() < 5){
                queue.offer(temp);
                return 82;
            } else {
                if (temp > queue.peek()) {
                    testedTemp = queue.poll();
                    queue.offer(temp);
                    return testedTemp;
                }
            }
            return temp;
        } finally {
            lock.unlock();
        }
    }
    
    //print method for queue
    public void printHQ(){
        lock.lock();
        try {
            for (Integer temp : queue) {
                System.out.print(temp + " ");
            }
        } finally {
            lock.unlock();
        }
    }

    //returns highest temp in top 5
    public int getHigh(){
        lock.lock();
        try {
        int lowest = 300;
        for (Integer temp : queue) {
            if (temp<lowest){
                lowest=temp;
            }
    }
    return lowest;
} finally {
    lock.unlock();
}
}

}

//queue for top 5 lowest temps
class lowQueue {
    
    //private variables
    private final Lock lock = new ReentrantLock();
    private PriorityQueue<Integer> queue;

    //constructor
    public lowQueue(){
        this.queue = new PriorityQueue<>();
    }

    //check method to verify if recorded temp makes top 5
    public void checkLow(int temp){
        lock.lock();
        try {
            if (queue.size() < 5){
                queue.offer(temp);
            } else {
                if (temp < queue.peek()) {
                    queue.poll();
                    queue.offer(temp);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    //print method for queue
    public void printLQ(){
        lock.lock();
        try {
            for (Integer temp : queue) {
                System.out.print(temp + " ");
            }
        } finally {
            lock.unlock();
        }
    }

    //returns lowest temp in top 5
    public int getLow(){
        lock.lock();
        try {
        int highest = 300;
        for (Integer temp : queue) {
            if (temp<highest){
                highest=temp;
            }
    }
    return highest;
} finally {
    lock.unlock();
}
}

}

//array class used to get 10-minute interval of time when the largest temperature
//difference was observed
class tops {

    //private variables
    private final Lock lock = new ReentrantLock();
    private int [][] times;

    //constructor
    public tops(){
        this.times = new int [60][2];
    }

    //adds highest value at recording time to check difference later
    public void addHigh (int time, int h){
        lock.lock();
        try {
            if ( h>times[time][0])
                times[time][0]=h;
        } finally {
            lock.unlock();
        }
    }

    //adds lowest value at recording time to check difference later
    public void addLow (int time, int l){
        lock.lock();
        try {
            if ( l<times[time][1])
                times[time][1]=l;
        } finally {
            lock.unlock();
        }
    }

    //returns temp recorded at selected minute
    public int getTemp (int time, int select){
        return times[time][select];
    }
    
}

//sensor threads
class Sensor extends Thread {

    //private variables
    private highQueue hq;
    private lowQueue lq;
    private Random random;
    private tops tenM;

    //cosntructor
    public Sensor(highQueue hq, lowQueue lq, tops tenM) {
       this.hq = hq;
       this.lq = lq;
       this.random = new Random();
       this.tenM = tenM;
    }

    //run method for threads
    public void run() {
        int testedTemp = 0, high=0, low=0;
        for (int i = 0; i < 59; i++) {
            int rn = random.nextInt(70 - (-100) + 1) + (-100);
            testedTemp = hq.checkHigh(rn);
            if (testedTemp < 82) {
                lq.checkLow(testedTemp);
            }
            high=hq.getHigh();
            low= lq.getLow();
            tenM.addHigh(i, high);
            tenM.addLow(i, low);
        }
    }
     
}

//main class
public class Assignment3P2 {
    public static void main(String[] args) {

        //variables
        int N = 8;
        highQueue hq = new highQueue();
        lowQueue lq = new lowQueue();
        tops times = new tops();
        Sensor[] threads = new Sensor[N];

        //thread use
        for (int i = 0; i < N; i++) {
            threads[i] = new Sensor(hq, lq, times);
            threads[i].start();
        }

        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //find 10-minute interval of time when the largest temperature
        //difference was observed
        int largest[]= new int [2];
        largest[0] = times.getTemp(0, 0)-times.getTemp(0, 1);
        largest[1] = 0;
        for (int j=0;j<59;j++){
            if ((times.getTemp(j, 0)-times.getTemp(j, 1))>largest[0]) {
                largest[0] = times.getTemp(j, 0)-times.getTemp(j, 1);
                largest[1] = j;
            }
        }

        //results
        if (largest[1]+10<=60){
        System.out.println("10-minute interval where largest temperature difference was recorded: minute " + (largest[1]) + " -> minute " + (largest[1]+10));
        }
        else{
            System.out.println("10-minute interval where largest temperature difference was recorded: minute " + (largest[1]-10) + " -> minute " + (largest[1]));
        }

        System.out.println("5 Highest temps:");
        hq.printHQ();
        System.out.println("\n5 Lowest temps:");
        lq.printLQ();
    }
}