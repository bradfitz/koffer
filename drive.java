import java.awt.*;
import java.util.Vector;
import java.applet.AudioClip;

public class drive extends klevel
{
    Image scene, cloud, dom;
    Image[] car = new Image[3];
    Image[] roadsign = new Image[6];
    Image[][] person = new Image[5][3];

    public static final int AMOUNTBLOOD = 20;

    BloodSpot[] blood = new BloodSpot[AMOUNTBLOOD];

    final static int NUMCLOUDS = 3;
    Point[] cd = new Point[NUMCLOUDS];
    int[] sp = new int[NUMCLOUDS];

    trapzoid road = new trapzoid(new Point(169,141),
                                 new Point(200,141),
                                 new Point(336,299));



    Vector obs = new Vector();
    AudioClip splat, hitpost, domharp;

    int comp = 0, comp_max = 50, comp_tick = 0, comp_ticktime = 10;
    boolean showsplash = false;

    int last = -1;      // last id of target
    int lastkey = 0;    // so you can switch right/left real quick
    int lastmousex = 0;


    int dot=0;
    static final int DPS = 3, DC = 3, REP = 3;

    int sign_side = -1, sign_step=0, sign_w=0, targetshow = 42, targetat = 0;
    static final int SIGN_STEPS = 20, SIGN_TOTAL_STEPS = 35, SIGN_HEIGHT=40;

    int car_x = 0, car_sx=0, car_y=95, car_wid;
    final static int CAR_MIN = -50, CAR_MAX = 50, BUMP_MIN = -33, BUMP_MAX = 33, SPEED_CHANGE=3;

    public drive ()
    {
        plot = "Larry fährt zum Dom.";
        inst = "Drive the car and try not to hit people.";
    }

    public void loadData ()
    {
        scene = waitImage("drive_scene.gif");
        cloud = waitImage("cloud.gif");
        dom = waitImage("dom.gif");
        Image allcar = waitImage("car.gif");
        for (int x=0;x<3;x++)
            car[x] = cropImage(allcar,97*x,0,97,71);
        car_wid = 97;
        splat = waitAudioClip("splat.au");
        hitpost = waitAudioClip("hitpost.au");
        domharp = waitAudioClip("domharp.au");

        Image signs, people;
        signs = waitImage("drive_signs.gif");
        people = waitImage("drive_people.gif");

        int[] taken = new int[3];
        int s;
        for (int i=0;i<NUMCLOUDS;i++)
        {
            cd[i] = new Point((int)(Math.random()*400),i*38+3);
            do
            {
                s = (int)(Math.random()*3);
            } while (taken[s] != 0);
            taken[s] = 1;
            sp[i] = s+1;
        }

        int ct=0;
        for (int y=0;y<2;y++)
            for (int x=0;x<3;x++)
                roadsign[ct++] = cropImage(signs, x*40, y*40, 40, 40);

        for (int y=0;y<3;y++)
            for (int x=0;x<5;x++)
                person[x][y] = cropImage(people, x*50, y*85, 50, 85);

//        loaded=true;
        speed=50;
        start();
    }

    public void drawNormal (Graphics g)
    {
        // lines on the road
        dot++;
        if (dot>DPS*DC) dot-=DPS*DC;
        int xt = 142, dl = (300-xt)/(DC*REP), shift = (dot/DPS) * dl;
        g.setColor(Color.yellow);
        for (int j=0;j<REP;j++) for (int i=-1;i<=2;i++)
            g.drawLine(200+i,xt+shift+j*dl*DC,200+i,xt+shift+j*dl*DC+dl);

        // clouds
        for (int i=0;i<NUMCLOUDS;i++)
        {
            g.drawImage(cloud,cd[i].x,cd[i].y,this);
        }

        for (int i=0;i<NUMCLOUDS;i++)
        {
            cd[i].x -= sp[i];
            if (cd[i].x<-75) cd[i].x+=500;
        }

        // the road sign
        if (sign_side != 0)
        {
            int six = 140 - (150-32)/SIGN_STEPS*sign_step;
            int siy = 141 + (299-141)/SIGN_STEPS*sign_step;
            if (sign_side==1) six=400-six;

            g.setColor(new Color(128,128,0));
            for (int i=0;i<2;i++) g.drawLine(six+i,siy,six+i,siy+1-SIGN_HEIGHT);

            siy -= (SIGN_HEIGHT + 40);
            six -= 20;

            g.drawImage(roadsign[sign_w],six,siy,this);

            sign_step++;
            boolean hit = false;
            if ((sign_step==SIGN_STEPS) &&
                ((sign_side == -1 && car_x < BUMP_MIN) ||
                (sign_side ==  1 && car_x > BUMP_MAX)))
                {
                    explode (1, 5, six, siy+60, 0);
                    changeScore(-10);
                    hitpost.play();
                    hit = true;
                }

            if (sign_step>=SIGN_TOTAL_STEPS || hit)
            {
                sign_step=0;
                sign_side *= -1;  // flip side
                sign_w = (int)(Math.random()*6);
            }
        }
        // end road sign

        Point cl = road.findPoint(car_x, car_y);
        int car_left = cl.x - (car_wid / 2);
        int car_right = cl.x + (car_wid / 2);
        int temp_car_y = cl.y;

        int bumpfactor = (car_x < BUMP_MIN || car_x > BUMP_MAX) ? (int)(0-Math.random()*5) : 0;
        if (bumpfactor != 0)
        {
            g.setColor(new Color(70,70,0));
            for (int i=0;i<15;i++)
            {
                g.drawLine(car_left+(int)(Math.random()*car_wid),cl.y-5+(int)(Math.random()*10),
                           car_left+(int)(Math.random()*car_wid),cl.y-5+(int)(Math.random()*10));
            }
            temp_car_y -= bumpfactor;
        }

        // decide when it's time for more targets (people)
        targetat++;
        if (targetat>=targetshow)
        {
            int idn = -1;
            while (idn == -1 || idn == last)
            {
                idn  = (int)(Math.random()*5);
            }
            last = idn;
            obs.addElement(new target(idn));
            targetat=0;
        }

        // move the targets
        for (int i=0;i<obs.size();i++)
        {
            target cur = (target) obs.elementAt(i);
            if (cur.id != -1)
            {
                Point p = road.findPoint(cur.xp, cur.yp);
                int frame = 0;
                if (cur.yp < 80) frame++;
                if (cur.yp < 10) frame++;
                drawAlignImage(g,person[cur.id][frame],p.x,p.y,CENTER,TOP,1);

                if (cur.go()) changeScore(20);

                if (cur.yp > 100 && cur.yp < 135 && p.x > car_left && p.x < car_right)
                {
                    cur.id = -1;
                    splat.play();
                    changeScore(-75);

                    explode (0, 5, p.x, p.y, car_sx);
                }
            }
        }


        for (int i=0;i<AMOUNTBLOOD;i++)
        {
            if (blood[i]!=null && blood[i].used)
            {
                blood[i].draw(g);
            }
        }

        // the car
        drawAlignImage(g,car[(car_sx==0)?0:(car_sx<0?1:2)],cl.x,temp_car_y,CENTER,TOP);


        car_x+=car_sx;
        if (car_x < CAR_MIN) car_x=CAR_MIN;
        if (car_x > CAR_MAX) car_x=CAR_MAX;

        boolean skip = false;
        // completion meter
        if (comp_tick++>comp_ticktime)
        {
            comp_tick=0;
            if (comp++>comp_max)
            {
                speed=3000;
//                stop();
                g.drawImage(dom,133,-4,this);
                domharp.play();
                showsplash = true;
                skip = true;
//                start();
            } else
            {
                if (targetshow--<5) targetshow=50; // basically turn off all people
            }

        }

        g.setColor(Color.red);
        g.fillRect(5,5,comp_max,10);
        g.setColor(Color.green);
        g.fillRect(5,5,comp,10);

        if (showsplash && ! skip)
        {
            enableNextLevel();
            stop();
            g.drawImage(dom,133,-4,this);
            displayText(g,"Hier ist der Dom +100 pts");
            changeScore(100);
        }

    }

    public void explode (int id, int amount, int x, int y, int xs)
    {
        int ctb = 0;
        for (int bi=0;bi<AMOUNTBLOOD;bi++)
        {
            if (ctb < amount)
            {
                if (blood[bi]==null || ! blood[bi].used)
                {
                    blood[bi] = new BloodSpot(id,x,y,xs);
                    ctb++;
                }
            }
        }
    }

    public boolean keyDown(Event evt, int key)
    {
        lastkey = key;
        switch (key)
        {
            case Event.LEFT:
                car_sx = -SPEED_CHANGE;
                break;

            case Event.RIGHT:
                car_sx = SPEED_CHANGE;
                break;
        }
        return true;
    }

    public boolean keyUp(Event evt, int key)
    {
        if (key == lastkey)
        {
            switch (key)
            {
                case Event.LEFT:
                    car_sx = 0;
                    break;

                case Event.RIGHT:
                    car_sx = 0;
                    break;
            }
        }
        return true;
    }

    public boolean mouseDown (Event e, int x, int y)
    {
        lastmousex=x;
        car_sx=0;
        return true;
    }

    public boolean mouseDrag (Event e, int x, int y)
    {
        if (x>lastmousex) car_sx=SPEED_CHANGE;
        if (x<lastmousex) car_sx=-SPEED_CHANGE;
        lastmousex=x;
        return true;
    }

    public boolean mouseUp (Event e, int x, int y)
    {
        lastkey=0;
        car_sx=0;
        return true;
    }

    public void drawStatic (Graphics g)
    {
        g.drawImage(scene,0,0,this);
    }


}

class trapzoid
{
    int center_x;
    int dent1, dent2;
    int top_y, bot_y;

    public trapzoid (Point tl, Point center, Point lr)
    {
        /*
              tl *__*___
                /  cent \
               /         \
              /___________*  lr

        */

        center_x = center.x;
        top_y = center.y;
        dent1 = center_x - tl.x;
        dent2 = lr.x - center_x;
        bot_y = lr.y;
    }

    public Point findPoint (int xp, int yp)  // -50 >= xp >= 50, 0 >= yp >= 100
    {
        int y = top_y + (bot_y - top_y) * yp / 100;
        int x = center_x + 2 * xp * (dent1 + (dent2 - dent1) * yp / 100) / 100;
        return new Point(x, y);
    }

    public void drawSelf (Graphics g)
    {
        int[] xa = { center_x - dent1, center_x + dent1,
                     center_x + dent2, center_x - dent2 };
        int[] ya = { top_y, top_y, bot_y, bot_y };
        g.setColor(Color.white);
        g.drawPolygon(xa,ya,4);

    }
}

class BloodSpot
{
    public final static Color[] red =
        { Color.red, new Color(190,0,0), new Color(140,0,0) };
    int xp, yp, sx, sy;
    int wx, wy;
    boolean used;

    int id = 0;  // 0 - blood, 1 - sign pieces
    int rd = 0;

    public BloodSpot (int id, int x, int y, int dr)
    {
        this.id = id;
        xp=x;
        yp=(int)(Math.random()*30)-100+y;
        sx=((int)(Math.random()*7))-3+2*dr;
        sy=(int)(Math.random()*-10)-3;
        wx=(int)(Math.random()*6)+2;
        wy=(int)(Math.random()*6)+2;
        rd=(int)(Math.random()*3);
        used=true;
    }

    public void draw (Graphics g)
    {
        yp += sy;
        xp += sx;

        sy ++;
        if (yp>300) used=false;
        else
        {
            switch (id)
            {
                case 0:
                    g.setColor(red[rd]);
                    g.fillOval(xp-wx,yp-wy,wx*2,wy*2);
                    g.setColor(red[2]);
                    g.drawOval(xp-wx,yp-wy,wx*2,wy*2);
                    break;

                case 1:
                    g.setColor(Color.white);
                    g.drawLine(xp-wx,yp-wy,xp+wx,yp+wy);
                    g.setColor(Color.gray);
                    int r = (int)(Math.random()*5);
                    g.drawLine(xp-wx+r,yp-wy,xp+wx-r,yp+wy);
                    break;
            }
        }
    }
}

class target
{
    int yp, xp, id;
    int ct = 0, state = 0;

    public target (int id)
    {
        yp = 0;
        xp = (int)(Math.random()*86-47);
        this.id = id;
        if (id==3) xp = (xp<0) ? -55 : 55;
    }

    public boolean go ()  // returns true if thing safe
    {
        ct++;
        if (ct>3) { ct=0; state = 1 - state; }

        yp+=5;
        if (yp>150) id = -1;
        return (id==-1);
    }
}
