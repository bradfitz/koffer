import java.awt.*;
import java.applet.AudioClip;

public class train extends klevel
{

    Image train, mts, cloud;
    AudioClip choo;

    Color sky_col = new Color(50,70,200);

    int trainat = -350, mtat=0;
    double r=0;

    int mult = 0, multd = 0, checkat = 0, checktot = 6;
    boolean horn = false;

    long time;

    public train ()
    {
        plot = "Larry fährt nach Mainz";
        inst = "Press right & left to drive the train.";
    }

    public void loadData ()
    {
        mts = waitImage("train_mts.gif");
        train = waitImage("train.gif");
        cloud = waitImage("cloud.gif");
        choo = waitAudioClip("choo.au");

        speed=50;
        start();
    }

    public void drawNormal (Graphics g)
    {
        if (checkat++>checktot)
        {
            checkat = 0;
            mult += multd;
            if (mult < -2) mult = -2;
            if (mult > 6) mult = 6;
        }

        mtat-=(int)(.5d*(double)mult);
        if (mtat>=0) mtat -= 400;
        g.drawImage(mts,mtat,0,this);
        g.drawImage(mts,mtat+400,0,this);
        if (mtat<-400) mtat+=400;

        g.drawImage(train,trainat+=(int)(1.5d*(double)mult),300-78,this);

        r += Math.PI / 180 * 5 * mult;
        g.setColor(new Color(128,128,0));
        // 259,61   333   , rad 9
        int x1 = 259+(int)(9*Math.cos(r)) + trainat;
        int x2 = x1 + (333-259);
        int y1 = 61+(int)(9*Math.sin(r)) + (300-78);
        for (int i=-1;i<=1;i++)
        {
            g.drawLine(x1,y1+i,x2,y1+i);
        }

        if (trainat<-400) trainat=-400;
        if (trainat>400)
        {
                stop();
                choo.play();
                displayText(g, "Willkommen in Mainz +100 pts");
                changeScore(100);
                enableNextLevel();
        }
    }

    public void drawStatic (Graphics g)
    {
        g.setColor(sky_col);
        g.fillRect(0,0,400,300);
        g.drawImage(cloud,25,30,this);
        g.drawImage(cloud,125,55,this);
        g.drawImage(cloud,225,30,this);
        g.drawImage(cloud,320,55,this);
    }

    public boolean keyDown(Event evt, int key)
    {
        switch (key)
        {
            case Event.LEFT:
                multd = -1;
                break;

            case Event.RIGHT:
                multd = 1;
                if (! horn) { choo.play(); horn=true; }
                break;
        }
        return true;
    }

    public boolean keyUp (Event evt, int key)
    {
        switch (key)
        {
            case Event.LEFT:
                multd = 0;
                break;

            case Event.RIGHT:
                multd = 0;
                break;
        }
        return true;

    }

}