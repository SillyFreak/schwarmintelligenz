/**
 * ChangedListener.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


import java.util.EventListener;

import cbcserver.actions.LeaderAction;


/**
 * <p>
 * A ChangedListener is invoked when a {@link Robot}'s {@linkplain LeaderAction#isCharging() charging} status
 * changes.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public interface ChangedListener extends EventListener {
    /**
     * <p>
     * Invoked when a {@link Robot}'s {@linkplain LeaderAction#isCharging() charging} status changes.
     * </p>
     */
    public void change(Robot r);
}
