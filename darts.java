import java.awt.*;
import java.applet.AudioClip;

public class darts extends klevel
{
    public final static int SPEED_CHANGE = 7, TOP_LIMIT = 173;

    Image[][] man = new Image[3][2];
    Image scene;
    int lasth, lastv;  // last horiz and vert key directions
    int dx=200, dy=200, df, dsx, dsy;
    int ld = 0; // 0-face right, 1-faceleft

    Point walkto;

    public final static int NUMDARTS = 50;
    Dart[] dart = new Dart[NUMDARTS];
    int dart_at = 0, dart_to = 30;

    public darts ()
    {
        plot = "Larry ist am Ostportal";
        inst = "Dodge the darts and last as long as you can!!";
    }

    public void loadData ()
    {
        scene = waitImage("dart_scene.gif");

        Image allman = waitImage("dart_man.gif");
        for (int y=0;y<2;y++)
            for (int x=0;x<3;x++)
                man[x][y] = cropImage(allman, x*40, y*83, 40, 83);

        speed=50;
        start();
    }

    public void drawNormal (Graphics g)
    {
        // mouse movements, if applicable
        if (walkto != null)
        {
            dsx=0; dsy=0;
            if (walkto.x<dx) { dsx=-SPEED_CHANGE; ld=1; }
            if (walkto.x>dx) { dsx=SPEED_CHANGE; ld=0;  }
            if (walkto.y<dy) dsy=-SPEED_CHANGE;
            if (walkto.y>dy) dsy=SPEED_CHANGE;

            if ((int)(Math.abs((double)(walkto.x-dx))) < SPEED_CHANGE) walkto.x = dx;
            if ((int)(Math.abs((double)(walkto.y-dy))) < SPEED_CHANGE) walkto.y = dy;
            if (walkto.x==dx && walkto.y==dy)
            {
                walkto=null;
                dsx = 0;
                dsy = 0;
            }
        }

        // move the man
        dx += dsx;
        dy += dsy;
        if (dx<0) dx=0;
        if (dx>400) dx=400;
        if (dy<TOP_LIMIT) dy=TOP_LIMIT;
        if (dy>341) dy=341;

        // if applicable, add more darts
        if (++dart_at>dart_to)
        {
            dart_at=0;
            int fd = -1;
            for (int i=0;i<NUMDARTS;i++)
            {
                if (fd==-1 && (dart[i]==null || dart[i].used==false)) fd = i;
            }
            if (fd != -1)
            {
                dart[fd] = new Dart(dx, dy-40, 5);
            }
            if ((dart_to-=1)<2) dart_to=2;

            changeScore(5);
        }

        if (++df>2) df=0;
        g.drawImage(man[(dsx!=0||dsy!=0)?df:1][ld],dx-20,dy-83,this);

        Rectangle r = new Rectangle(dx-15,dy-80,30,60);

        boolean dead = false;
        for (int i=0;i<NUMDARTS;i++)
            if (dart[i]!=null && dart[i].used)
                if (dart[i].go(g, r)) dead = true;

        if (dead)
        {
            stop();
            enableNextLevel();
            displayText(g,"Du bist betäubt...");
        }
    }

    public void drawStatic (Graphics g)
    {
        g.drawImage(scene,0,0,this);
    }

    public boolean keyDown(Event evt, int key)
    {
        switch (key)
        {
            case Event.LEFT:
                lasth = key;
                dsx = -SPEED_CHANGE;
                ld=1;
                break;

            case Event.RIGHT:
                lasth = key;
                dsx = SPEED_CHANGE;
                ld=0;
                break;

            case Event.UP:
                lastv = key;
                dsy = -SPEED_CHANGE;
                break;

            case Event.DOWN:
                lastv = key;
                dsy = SPEED_CHANGE;
                break;
        }
        return true;
    }

    public boolean keyUp(Event evt, int key)
    {
        switch (key)
        {
            case Event.LEFT:
                if (key == lasth) dsx = 0;
                break;

            case Event.RIGHT:
                if (key == lasth) dsx = 0;
                break;

            case Event.UP:
                if (key == lastv) dsy = 0;
                break;

            case Event.DOWN:
                if (key == lastv) dsy = 0;
                break;
        }
        return true;
    }

    public boolean mouseDown (Event e, int x, int y)
    {
        lasth = 0;
        lastv = 0;
        return mouseDrag (e, x, y);
    }

    public boolean mouseDrag (Event e, int x, int y)
    {
        if (y<TOP_LIMIT) y = TOP_LIMIT;
        walkto = new Point(x, y);
        return true;
    }

    public boolean mouseUp (Event e, int x, int y)
    {
//      walkto = null;
        dsx = 0;
        dsy = 0;
        return true;
    }

}

class Dart
{
    boolean used = false;
    double x, y, dir, speed;

    public Dart (int tx, int ty, int sp)
    {
        int side = (int)(Math.random()*3);
        switch (side)
        {
            case 0:
                x = Math.random()*400;
                y = 300;
                break;
            case 1:
                x = 0;
                y = 160d+Math.random()*(400-160);
                break;
            case 2:
                x = 400;
                y = 160d+Math.random()*(400-160);
                break;
        }
        dir = dir_to(x,y,(double)tx,(double)ty);
        speed = sp;
        used = true;
    }

    public boolean go (Graphics g, Rectangle r)
    {
        x += speed * Math.cos(dir);
        y -= speed * Math.sin(dir);
        Point p = new Point((int)x, (int)y);

        /*

            |        picture is to scale, | is white, X is red
            |
            |
           XXX
           XXX
           XXX
           XXX
           XXX
           XXX
           XXX
            |
            |
          --+--

        */

        double opdir = Math.PI-dir, halfpi = Math.PI/2;
        Point back = movePoint(p,opdir,12);
        Point bl = movePoint(back,opdir-halfpi,2);
        Point br = movePoint(back,opdir+halfpi,2);
        Point taa = movePoint(p,opdir,3,opdir-halfpi,2);
        Point tab = movePoint(p,opdir,3,opdir+halfpi,2);
        Point tba = movePoint(p,opdir,9,opdir-halfpi,2);
        Point tbb = movePoint(p,opdir,9,opdir+halfpi,2);
        int[] px = { taa.x, tab.x, tbb.x, tba.x, taa.x };
        int[] py = { taa.y, tab.y, tbb.y, tba.y, taa.y };

        g.setColor(Color.white);
        g.drawLine(p.x,p.y,back.x,back.y);
        g.drawLine(bl.x,bl.y,br.x,br.y);

        g.setColor(Color.red);
        g.fillPolygon(px, py, 5);

        if (x<0 || x>400 || y<120 || y>300) used=false;

        return r.inside((int)x, (int)y);
    }

    public double dir_to (double x1, double y1, double x2, double y2)
    {
        double ret=-1;

        if (x1==x2)
        {
            if (y1>y2)
                ret=Math.PI/2;
            else
                ret=Math.PI*3/2;
        } else
        if (y1==y2)
        {
            if (x1>x2)
                ret=Math.PI;
            else
                ret=0;
        } else
        {
            double yleg= y2 - y1;
            double xleg= x2 - x1;
            double hyp=Math.sqrt((yleg*yleg)+(xleg*xleg));

            double ref=Math.asin(yleg/hyp);

            if (x2<x1) ref=Math.PI + ref; else ref=-ref;
            ret = ref;
        }

        return ret;
    }

    public Point movePoint (Point p, double d1, double s1, double d2, double s2)
    {
        double x=(double)p.x, y=(double)p.y;

        x+=Math.cos(d1) * s1;
        y+=Math.sin(d1) * s1;
        x+=Math.cos(d2) * s2;
        y+=Math.sin(d2) * s2;
        return new Point((int)x,(int)y);
    }

    public Point movePoint (Point p, double d1, double s1)
    {
        double x=(double)p.x, y=(double)p.y;

        x+=Math.cos(d1) * s1;
        y+=Math.sin(d1) * s1;
        return new Point((int)x,(int)y);
    }

}