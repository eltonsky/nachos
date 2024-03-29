package nachos.threads;

import java.util.ArrayList;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	condLock  = new Lock();
    	speakCond = new Condition2(condLock);
    	listenCond = new Condition2(condLock);
    	msgList = new ArrayList<Integer>();
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	boolean intStatus = Machine.interrupt().disable();
    	
    	condLock.acquire();
    	
    	if(listenCount == 0){
    		speakCond.sleep();
    	}
    		
    	listenCount--;
    	
    	msgList.add(word);
    	
    	listenCond.wake();
    	
    	condLock.release();
    	
    	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	boolean intStatus = Machine.interrupt().disable();
    	
    	condLock.acquire();
    	
    	listenCount++;
    	
    	speakCond.wake();
    	
    	if(msgList.isEmpty())
    		listenCond.sleep();
    	
    	condLock.release();
    	
    	Machine.interrupt().restore(intStatus);
 
    	// Must have msg if reach here.
    	return msgList.remove(0);
    }
    
    /**
     */
    private static Communicator comm;
    
    private static class TestSpeaker implements Runnable {
    	TestSpeaker(int word) {
		    this.word = word;
		}
		
		public void run() {
			Lib.debug('t', "### Speak " + word + " from " + KThread.currentThread().toString());
			
			comm.speak(word);
		}
	
		private int word;
    }
    
    private static class TestListener implements Runnable {
    	TestListener() {
		}
		
		public void run() {
			Lib.debug('t', "### Try to listen from " + KThread.currentThread().toString());
			
		    int res = comm.listen();
		    
		    Lib.debug('t', "### listened " + res + " from " + KThread.currentThread().toString());
		}
    }
    
    public static void selfTest() {
    	comm = new Communicator();

    	KThread t6 = new KThread(new TestListener()).setName("Listener 1");
    	t6.fork();
    	
    	KThread t7 = new KThread(new TestListener()).setName("Listener 2");
    	t7.fork();
    	
    	KThread t8 = new KThread(new TestListener()).setName("Listener 3");
    	t8.fork();
    	
    	KThread t9 = new KThread(new TestListener()).setName("Listener 4");
    	t9.fork();
    	
    	KThread t10 = new KThread(new TestListener()).setName("Listener 5");
    	t10.fork();
    	
    	
    	KThread t1 = new KThread(new TestSpeaker(123)).setName("Speaker 1");
    	t1.fork();
    	
    	KThread t2 = new KThread(new TestSpeaker(456)).setName("Speaker 2");
    	t2.fork();
    	
    	KThread t3 = new KThread(new TestSpeaker(789)).setName("Speaker 3");
    	t3.fork();
    	
    	KThread t4 = new KThread(new TestSpeaker(101112)).setName("Speaker 4");
    	t4.fork();
    	
    	KThread t5 = new KThread(new TestSpeaker(131415)).setName("Speaker 5");
    	t5.fork();
    	

    	
    	
    	t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
        t7.join();
        t8.join();
        t9.join();
        t10.join();
    	
    	KThread.yield();
    }
    
    private ArrayList<Integer> msgList;
    private int listenCount = 0;
    private Condition2 speakCond;
    private Condition2 listenCond;
    private Lock condLock;
}
