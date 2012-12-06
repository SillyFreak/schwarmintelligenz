/**
 * ChangedListener.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


import java.util.EventListener;


/**
 * <p>
 * The class ChangedListener.
 * </p>
 * 
 * @version V0.0 06.12.2012
 * @author SillyFreak
 */
public interface ChangedListener extends EventListener {
    public void change(Robot r);
}
