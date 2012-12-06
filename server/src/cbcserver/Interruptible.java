/**
 * Interruptible.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


import java.util.concurrent.ExecutorService;


/**
 * <p>
 * A with additional {@link #start()} and {@link #stop()} methods. The {@link #start()} method executes it using a
 * provided {@link ExecutorService}, {@link #stop()} uses {@link Thread#interrupt() interrupt()} to interrupt the
 * thread immediately; in addition, {@link #isRunning()} will subsequently return null, so that client code can
 * respect the requested status.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public abstract class Interruptible implements Runnable {
    protected final ExecutorService pool;
    
    protected Thread                t;
    
    public Interruptible(ExecutorService pool) {
        this.pool = pool;
    }
    
    @Override
    public void run() {
        t = Thread.currentThread();
        execute();
    }
    
    protected abstract void execute();
    
    public synchronized boolean isRunning() {
        return t != null;
    }
    
    public synchronized void start() {
        if(t != null) return;
        pool.execute(this);
    }
    
    public synchronized void stop() {
        if(t == null) return;
        t.interrupt();
        t = null;
    }
}
