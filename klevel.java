import java.awt.*;
import java.applet.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.CropImageFilter;

public class klevel extends Canvas implements Runnable
{
    public String plot = "--Plot Line--";
    public String inst = "--Instruction Line--";
    public double done = 0.0d;

    public final static int TOP = -1, MIDDLE = 0, BOTTOM = 1;
    public final static int LEFT = -1, CENTER = 0, RIGHT = 1;

    public static Applet    master;
    public static boolean   disable_dim = false;

    public Image            oI, statI;
    public Graphics         oG, statG;

    public Thread           runner;
    public int              speed = 500; // half a second

    public boolean          doStatic = true, startLoad = false, loaded = false;
    public boolean          timer_on = false;


    // can't override
    public klevel ()
    {
        setBackground(Color.black);
    }

    // don't override
    public void paint (Graphics g)
    {
        ensureBuffers();
        update(g);
    }

    // call this
    public void start()
    {
        timer_on = true;
        runner = new Thread(this);
        runner.start();
    }

    // call this
    public void stop()
    {
        timer_on = false;
        runner = null;
    }

    // don't override
    public void run ()
    {
        long startTime = System.currentTimeMillis();

        //This is the animation loop.
        while (Thread.currentThread() == runner) {

            repaint();
            try {
                startTime += speed;
                Thread.sleep(Math.max(0,startTime-System.currentTimeMillis()));
//                Thread.sleep(speed);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    // call this
    public AudioClip waitAudioClip (String file)
    {
        return master.getAudioClip(master.getDocumentBase(),file);
    }

    // call this
    public Image waitImage (String file)
    {
        Image a = master.getImage(master.getDocumentBase(),file);
        MediaTracker track = new MediaTracker(this);
        track.addImage(a,0);
        try
        {
            track.waitForID(0);
        } catch (Exception e) { }
        return a;
    }

    // call this
    public Image cropImage (Image i, int x, int y, int w, int h)
    {
        MediaTracker st = new MediaTracker(this);
        Image cp = createImage(new FilteredImageSource(i.getSource(),
                               new CropImageFilter(x,y,w,h)));
        st.addImage(cp,0);
        try
        {
            st.waitForID(0);
        } catch (Exception e) { }

        return cp;
    }

    // don't override
    public void ensureBuffers ()
    {
        if (oI==null || statI==null)
        {
            oI      = createImage(400,300);
            statI   = createImage(400,300);
            oG      = oI.getGraphics();
            statG   = statI.getGraphics();
        }
    }

    // don't override
    public void update (Graphics g)
    {
        boolean skipthisround = false;
        if (loaded)
        {
            if (doStatic)
            {
                drawStatic(statG);
                doStatic=false;
            }
        } else {
            oG.setFont(new Font("TimesRoman",Font.BOLD,20));
            drawText(oG,"Loading Level, please wait.",new Point(200,100),CENTER,TOP,new Color(0,0,180),true);
            g.drawImage(oI,0,0,this);
            if (startLoad==false)
            {
                startLoad=true;
                loaded=true;
                loadData();
                skipthisround=true;
            }
        }

        if (timer_on == true && skipthisround == false)
        {
            if (loaded)
            {
                oG.drawImage(statI,0,0,this);
                drawNormal(oG);
                g.drawImage(oI,0,0,this);
            }
        } else {
            g.drawImage(oI,0,0,this);
        }

    }

    // call this, to call drawText simpler
    public void displayText (Graphics g, String s)
    {
        if (disable_dim == false)
        {
            Image dim = createImage(new FilteredImageSource(oI.getSource(), new dimFilter(100)));
            MediaTracker track = new MediaTracker(this);
            track.addImage(dim,0);
            try {
                track.waitForID(0);
            } catch (Exception e) { }

            g.drawImage(dim,0,0,this);
        } else {

            g.setColor(Color.black);
            for (int i=0;i<800;i+=2)
            {
                g.drawLine(i,0,0,i);
            }
        }

        for (int i=0;i<200;i++)
        {
            g.setColor(new Color(200-i,200-i,200-i));
            g.fillRect(i*2,50,2,50);
        }
        g.setColor(new Color(100,100,100));
        g.draw3DRect(0,50,400,50,true);
        g.setFont(new Font("TimesRoman",0,22));
        drawText(g, s, new Point(200,75),CENTER,MIDDLE,new Color(255,255,0),true);
    }


    // call this
    public void drawText (Graphics g, String s, Point p, int ha, int va, Color c, boolean three_d)
    {
        int tw = g.getFontMetrics().stringWidth(s);
        int tha = g.getFontMetrics().getAscent();
        int drx=0, dry=0;

        switch (ha)
        {
            case LEFT:     drx=p.x-tw; break;
            case CENTER:   drx=p.x-(tw/2); break;
            case RIGHT:    drx=p.x; break;
        }
        switch (va)
        {
            case TOP:      dry=p.y; break;
            case MIDDLE:   dry=p.y+(tha/4); break;  // one-forth is not exact
            case BOTTOM:   dry=p.y+tha; break;
        }
        if (three_d)  // if 3d
        {
            g.setColor(changeColor(c,1));
            g.drawString(s,drx-1,dry);
            g.setColor(changeColor(c,-1));
            g.drawString(s,drx+1,dry);
        }
        g.setColor(changeColor(c,0));
        g.drawString(s,drx,dry);

    }

    // call this
    public void drawAlignImage (Graphics g, Image i, int x, int y, int ha, int va)
    {
        drawAlignImage(g,i,x,y,ha,va,1);
    }

    // call this
    public void drawAlignImage (Graphics g, Image i, int x, int y, int ha, int va, double factor)
    {
        int w = (int) (i.getWidth(this) * factor);
        int h = (int) (i.getHeight(this) * factor);
        int drx=0, dry=0;

        switch (ha)
        {
            case LEFT:     drx=x; break;
            case CENTER:   drx=x-(w/2); break;
            case RIGHT:    drx=x-w; break;
        }
        switch (va)
        {
            case TOP:      dry=y-h; break;
            case MIDDLE:   dry=y-(h/2); break;
            case BOTTOM:   dry=y; break;
        }
        if (factor != 1) g.drawImage(i,drx,dry,w,h,this);
        else g.drawImage(i,drx,dry,this);

    }

    private Color changeColor(Color c, int change)
    {
        double m = (change==1) ? 23d/17d : ((change==-1) ? 9d/17d : 1);
        return new Color(multFix(c.getRed(),m),multFix(c.getGreen(),m),multFix(c.getBlue(),m));
    }
    private int multFix (int n, double m)
    {
        double i = (double) n;
        i *= m;
        if (i<0) i=0;
        if (i>255) i=255;
        return (int) i;
    }

    // override
    public void drawNormal (Graphics g)
    {
    }

    // override
    public void drawStatic (Graphics g)
    {
    }

    // override
    public void loadData ()
    {
    }

    // override
    public void cleanUp ()
    {
        stop();
    }

    // call this
    public void nextLevel ()
    {
        ((koffer)master).nextLevel();
    }

    // call this
    public void enableNextLevel ()
    {
        if (((koffer)master).ingame)
            ((koffer)master).nextbutton.enable(true);
    }

    // call this
    public void changeScore (int change)
    {
        ((koffer)master).changeScore(change);
    }

}
