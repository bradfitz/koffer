import java.awt.*;
import java.applet.*;

public class intro extends klevel
{
    static final Font f=new Font("TimesRoman",Font.ITALIC|Font.BOLD,22);
    int drx, dry; // base text cords

    int[] yshift = {-6,-6,-5,-4,-3,-2,-1,0,0,-1,-2,-3,-4,-5,-6};
    int[] cols = {130,140,150,160,170,180,190,200,210,220,230,240,250,255,250,240,230,220,210,200,190,180,170,160,150,140,130};
    int atColor=0, moveColor=1;
    int lastCursor=-1;

    FontMetrics fm;

    String mess = "Brad & Dan's \"Der verloren Koffer\"";
    char lets[];

    AudioClip anthem;

    Image town = null;

    public intro ()
    {
        plot = "Willkommen!";
        inst = "Click the NEXT button to go to the next level.";
    }

    public void loadData ()
    {
        town = waitImage("intro.gif");
        anthem = waitAudioClip("intro.au");

        setFont(f);
        lets=mess.toCharArray();

        anthem.loop();
        enableNextLevel();

        loaded=true;
        speed=150;
        start();
    }

    public void cleanUp ()
    {
        anthem.stop();
        anthem = null;
    }

    public void drawNormal (Graphics g)
    {
        fm=g.getFontMetrics();
        int tw  = fm.stringWidth(mess);
        int tha = fm.getAscent();
        drx=(koffer.gw-tw)/2;
        dry=(koffer.gh+tha/2)/2-50;

        int ax=drx;
        int tc=atColor;
        int cv, i;

        for (i=0;i<lets.length;i++) {

            cv=cols[tc % cols.length];
            g.setColor(Color.black);
            g.drawString(String.valueOf(lets[i]),ax+2,dry+yshift[tc % yshift.length]+2);
            g.setColor(new Color(234,(int)(1.7d*((double)cols[(tc+lets.length/2) % cols.length]-130d)),0));
            g.drawString(String.valueOf(lets[i]),ax,dry+yshift[tc % yshift.length]);

            tc++;
            ax+=fm.charWidth(lets[i]);
        }

        atColor+=moveColor;
    }

    public void drawStatic (Graphics g)
    {
        g.drawImage(town,0,0,this);
    }

}