import java.awt.*;

public class plane extends klevel
{
    Image scene, cloud;

    final static int NUMCLOUDS = 7;
    Point[] cd = new Point[NUMCLOUDS];
    int[] sp = new int[NUMCLOUDS];

    Color sky_col = new Color(50,70,200);

    boolean moving = false;
    int planeat = -280, plane_y=10, plane_sy=0;
    final static int SPEED_CHANGE=1;

    long time;

    public plane ()
    {
        plot = "Larry fliegt nach Deutschland";
        inst = "Land the plane.  Press & release up or down to change the speed of descent.";

        speed=50;
        start();
    }

    public void loadData ()
    {
        scene = waitImage("fly_plane.gif");
        cloud = waitImage("cloud.gif");
        for (int i=0;i<NUMCLOUDS;i++)
        {
            cd[i] = new Point((int)(Math.random()*400),i*38+3);
            sp[i] = (int)(Math.random()*4)+3;
        }
        loaded=true;
    }

    public void drawNormal (Graphics g)
    {
        for (int i=0;i<5;i++)
        {
            g.drawImage(cloud,cd[i].x,cd[i].y,this);
        }

        if (moving) planeat+=4;
        g.drawImage(scene,planeat,plane_y,this);
        plane_y += plane_sy;

        for (int i=5;i<NUMCLOUDS;i++)
        {
            g.drawImage(cloud,cd[i].x,cd[i].y,this);
        }

        for (int i=0;i<NUMCLOUDS;i++)
        {
            cd[i].x -= sp[i];
            if (cd[i].x<-75) cd[i].x+=500;
        }

        if (planeat>400 || plane_y > 300)
        {
            String mes = "";
            stop();
            if (plane_y > 250)
            {
                mes = "Gut gemacht!  +100 points";
                changeScore(100);
            } else
            {
                mes = "Das Flugzeug ist zu hoch! -50 points";
                changeScore(-50);
            }
            displayText(g,mes);
            enableNextLevel();
        }

    }

    public void drawStatic (Graphics g)
    {
        g.setColor(sky_col);
        g.fillRect(0,0,400,300);
    }

    public boolean keyDown(Event evt, int key)
    {
        moving = true;

        switch (key)
        {
            case Event.UP:
                plane_sy -= SPEED_CHANGE;
                break;

            case Event.DOWN:
                plane_sy += SPEED_CHANGE;
                if (plane_sy > SPEED_CHANGE * 4) plane_sy = SPEED_CHANGE * 4;
                break;
        }
        return true;
    }

}