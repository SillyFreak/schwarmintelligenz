/**
 * Interruptible.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


import java.util.concurrent.ExecutorService;


/**
 * <p>
 * The class Interruptible.
 * </p>
 * 
 * @version V0.0 06.12.2012
 * @author Clemens Koza
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
    
    public abstract void execute();
    
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
