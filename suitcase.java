import java.awt.*;

public class suitcase extends klevel
{
    int bagClicked = -1;
    Image scene, man, acase;

    final static int NUMCASES = 10;
    double r[] = new double[NUMCASES];
    double r_base = 0;

    long time;

    public suitcase ()
    {
        plot = "Larry ist am Flughafen";
        inst = "Click on the bag you think is Larry's";
    }

    public void loadData ()
    {
        time = System.currentTimeMillis();

        scene = waitImage("case_scene.gif");
        man = waitImage("case_man.gif");
        acase = waitImage("case.gif");
        for (int i=0;i<NUMCASES;i++)
        {
            r[i] = 2d*Math.PI / NUMCASES * i;
        }

        loaded=true;
        speed=100;
        start();
    }

    public void drawNormal (Graphics g)
    {
        r_base += (2d*Math.PI) / 360d;
        if (r_base > 2d*Math.PI) r_base-=2d*Math.PI;

        for (int i=0;i<NUMCASES;i++)
        {
            if (i != bagClicked) g.drawImage(acase,199-25+(int)(Math.cos(r[i]+r_base)*154d),160-18+(int)(Math.sin(r[i]+r_base)*49d),this);
        }
        g.drawImage(man,200,150,this);

        if (bagClicked != -1)
        {
            stop();
            displayText(g,"Hmmm... es ist schwerer zu ihn...");
            enableNextLevel();
        }
    }

    public void drawStatic (Graphics g)
    {
        g.drawImage(scene,0,0,this);
    }

    public boolean mouseDown (Event e, int x, int y)
    {
        long now = System.currentTimeMillis();
        boolean good = false;
        for (int i=0;i<NUMCASES;i++)
        {
            Rectangle sc = new Rectangle(199-25+(int)(Math.cos(r[i]+r_base)*154d),160-18+(int)(Math.sin(r[i]+r_base)*49d),50,36);
            if (sc.inside(x,y) && now>(time+500))
            {
                bagClicked = i;
            }
        }
        return true;

    }

}