import java.awt.*;
import java.applet.AudioClip;

public class whack extends klevel
{
    public final static int DOWN_SPEED = 12, UP_SPEED = -8, TIME_UP = 12;

    Image[] board = new Image[3];
    Image[] stick = new Image[2];
    Image[] fraupic  = new Image[2];
    AudioClip punch;

    frauTarget[] frau = new frauTarget[6];

    int cx = 0, cy = 0, cp = -1;
    int riseat = 0, riseto = 40;

    int lnMiss, lnHit, lnTotal;

    int lastCursor;

    public whack ()
    {
        plot = "Frau Johnson ist nicht Frau Johnson!!";
        inst = "It's Whack-Attack, and the smugglers are in disguise...";
        setFont(new Font("Times",Font.BOLD,18));
    }

    public void loadData ()
    {
        frauTarget.reset();
        frauTarget.notify = this;
        frau[0] = new frauTarget(43,59);
        frau[1] = new frauTarget(154,59);
        frau[2] = new frauTarget(265,59);
        frau[3] = new frauTarget(32,126);
        frau[4] = new frauTarget(153,126);
        frau[5] = new frauTarget(276,126);

        changeCursor(1);
        Image bothfrau = waitImage("whack_frau.gif");
        for (int x=0;x<2;x++)
        {
            fraupic[x] = cropImage(bothfrau, x*90, 0, 90, 114);
            frauTarget.pic[x] = fraupic[x];
        }

        Image bothstick = waitImage("whack_stick.gif");
        for (int x=0;x<2;x++)
            stick[x] = cropImage(bothstick, x*40, 0, 40, 195);

        Image allboard = waitImage("whack_holes.gif");
        board[0] = cropImage(allboard, 0, 0, 400, 200);
        board[1] = cropImage(allboard, 0, 200, 400, 150);
        board[2] = cropImage(allboard, 0, 350, 400, 92);

        punch = waitAudioClip("punch.au");

        speed = 100;
        start();
    }

    // normally this is scary to override
    public void run ()
    {
        //This is the animation loop.
        while (Thread.currentThread() == runner) {

            repaint();
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    public void updateScore(int hit, int miss, int total)
    {
        lnHit = hit;
        lnMiss = miss;
        lnTotal = total;
    }

    public void changeCursor (int CURS)
    {
        if (lastCursor != CURS) {
            Object frame = getParent ();
            while (! (frame instanceof Frame))
            frame = ((Component) frame).getParent ();
            ((Frame) frame).setCursor (CURS);
            lastCursor=CURS;
        }
    }

    public boolean mouseDrag (Event e, int x, int y)
    {
        cp = 1;
        cx = x;
        cy = y;
        return true;
    }

    public boolean mouseMove (Event e, int x, int y)
    {
        cp = 0;
        cx = x;
        cy = y;
        return true;
    }

    public boolean mouseExit (Event e, int x, int y)
    {
        cp = -1;
        repaint();
        return true;
    }

    public boolean mouseDown (Event e, int x, int y)
    {
        int ww = -1;
        for (int i=5;i>=0;i--)
        {
            if (frau[i].area().inside(x,y) && ww==-1 && frau[i].isFine()) { ww = i; }
        }
        if (ww != -1)
        {
            punch.play();
            frau[ww].whack();
        }

        cp = 1;
        repaint();
        return true;
    }

    public boolean mouseUp (Event e, int x, int y)
    {
        cp = 0;
        repaint();
        return true;
    }

    public void drawStatic (Graphics g)
    {
        g.setColor(Color.white);
        g.fillRect(0,0,400,300);
        g.drawImage(board[0], 0, 100, this);

    }

    public void drawNormal (Graphics g)
    {
        if (++riseat>riseto)
        {
            int tr = 0;
            while ((! frau[(int)(Math.random()*6)].rise(UP_SPEED, TIME_UP)) && tr < 10) tr++;
            if ((riseto-=2)<2) { riseto = 5; }
            riseat=0;
        }

        for (int i=0;i<3;i++) frau[i].go(g, this);
        g.drawImage (board[1], 0, 150, this);

        for (int i=3;i<6;i++) frau[i].go(g, this);
        g.drawImage (board[2], 0, 208, this);

        twoColor(g,5,20,"Hits: ",String.valueOf(lnHit));
        twoColor(g,200,20,"Misses: ",String.valueOf(lnMiss));

        if (cp != -1) g.drawImage (stick[cp], cx-20, cy-30, this);

        if ((lnHit+lnMiss) >= 50)
        {
            stop();
            displayText(g, "Sehr gut!");
            changeCursor(0);
        }
    }

    public int twoColor (Graphics g, int x, int y, String a, String b)
    {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.black);
        g.drawString(a,x,y);
        g.setColor(Color.red);
        x += fm.stringWidth(a)+2;
        g.drawString(b,x,y);

        return fm.getHeight();

    }

}

class frauTarget
{
    static whack notify;
    public static int hits = 0, misses = 0, total = 0;

    public static Image pic[] = new Image[2];
    public final static int DEEP = 114, WIDE = 90;

    int bx, by, depth, speed;
    boolean whacked;
    boolean showing;

    int ct, ct_to;

    public frauTarget (int x, int y)
    {
        showing = false;
        whacked = false;
        bx = x;
        by = y;
        depth = DEEP;
        speed = 0;
    }

    public boolean rise (int speed, int ct_to)
    {
        if ((! showing) && total < 50)
        {
            showing = true;
            this.speed = speed;
            this.ct_to = ct_to;
            whacked = false;
            ct = 0;
            total++;
            return true;
        } else
        {
            return false;
        }

    }

    public void whack ()
    {
        whacked = true;
        speed = 0;
        depth += 4;
        hits++;
        notify.updateScore(hits,misses,total);
        notify.changeScore(5);
    }

    public void go (Graphics g, Component watcher)
    {
        if (showing)
        {
            depth += speed;
            if (depth > DEEP)
            {
                depth=DEEP; speed = 0; ct=0;
                if (! whacked)
                {
                    misses++;
                    notify.changeScore(-5);
                }
                notify.updateScore(hits,misses,total);
                whacked = false;
                showing = false;
            }
            if (depth < 0) depth=0;
            if (depth==0 || (showing && whacked)) if (ct++>ct_to) speed = whack.DOWN_SPEED;

            g.drawImage(pic[whacked?1:0], bx, by+depth, watcher);
        }
    }

    public Rectangle area ()
    {
        return new Rectangle(bx, by+depth, WIDE, DEEP-depth);
    }

    public static void reset ()
    {
        hits=0;
        misses=0;
        total=0;
    }

    public boolean isFine ()    { return ! whacked; }
    public int getHits ()       { return hits;      }
    public int getMisses ()     { return misses;    }

}