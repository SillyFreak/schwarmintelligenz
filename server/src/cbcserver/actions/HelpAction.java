/**
 * HelpAction.java
 * 
 * Created on 10.01.2013
 */

package cbcserver.actions;


import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;

import cbcserver.CBCGUI;


/**
 * <p>
 * The class HelpAction.
 * </p>
 * 
 * @version V0.0 10.01.2013
 * @author SillyFreak
 */
public class HelpAction extends CBCGUIAction {
    private static final long serialVersionUID = -5226127523632832790L;
    
    private static final ImageIcon icon1, icon2, icon3;
    
    static {
        String url = "javax/swing/plaf/metal/icons/ocean/info.png";
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource(url));
        int w = icon.getIconWidth() + 3, h = icon.getIconHeight() + 3;
        
        BufferedImage image;
        Graphics g;
        {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g = image.createGraphics();
            g.drawImage(icon.getImage(), 0, 0, null);
            g.dispose();
            icon1 = new ImageIcon(image);
        }
        
        {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g = image.createGraphics();
            g.drawImage(icon.getImage(), 3, 3, null);
            g.dispose();
            icon2 = new ImageIcon(image);
        }
        
        {
            ImageProducer p = new FilteredImageSource(icon.getImage().getSource(), new DarkerImageFilter());
            Image darker = Toolkit.getDefaultToolkit().createImage(p);
            
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g = image.createGraphics();
            while(!g.drawImage(darker, 2, 2, null))
                try {
                    //sleep a micro to be sure the image was loaded. dirty, but works...
                    Thread.sleep(0, 1000);
                } catch(InterruptedException ex) {}
            g.dispose();
            icon3 = new ImageIcon(image);
        }
    }
    
    public HelpAction(CBCGUI gui) {
        super(gui, "", icon1);
    }
    
    public void installIcons(AbstractButton b) {
        b.setPressedIcon(icon2);
        b.setSelectedIcon(icon3);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean b = ((AbstractButton) e.getSource()).isSelected();
        gui.setHelpVisible(b);
    }
    
    private static class DarkerImageFilter extends RGBImageFilter {
        @Override
        public int filterRGB(int x, int y, int rgb) {
            int a = rgb & 0xff000000;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;
            
            return a | convert(r) << 16 | convert(g) << 8 | convert(b);
        }
        
        private int convert(int color) {
            return (int) (color * .80);
        }
    }
}
