import java.awt.*;

public class bcManager
{
    public int border;

    public Color flat;
    public Color shadow;
    public Color highlight;
    public Color fore;

    public bcManager (int r, int g, int b, int fr, int fg, int fb, int border)
    {
        this.border=border;
        double m;
        m=1;
        flat=new Color(multFix(r,m),multFix(g,m),multFix(b,m));
        m=9d/17d;
        shadow=new Color(multFix(r,m),multFix(g,m),multFix(b,m));
        m=23d/17d;
        highlight=new Color(multFix(r,m),multFix(g,m),multFix(b,m));
        fore=new Color(fr,fg,fb);
    }

    public void drawBevel (Graphics g, int xo, int yo, Dimension d, int b, boolean r)
    {
        int x=d.width-1, y=d.height-1;
        Color bk = g.getColor(); // push

        if (r) g.setColor(highlight);
        else g.setColor(shadow);
        for (int i=0; i<b; i++)
        {
            g.drawLine(0+xo,i+yo,x-1-i+xo,i+yo);
            g.drawLine(i+xo,0+yo,i+xo,y-i+yo);
        }
        if (! r) g.setColor(highlight);
        else g.setColor(shadow);
        for (int i=0; i<b; i++)
        {
            g.drawLine(x-i+xo,i+yo,x-i+xo,y+yo);
            g.drawLine(1+i+xo,y-i+yo,x+xo,y-i+yo);
        }

        g.setColor(bk); // pop
    }

    public void drawBevel (Graphics g, Dimension d, int b, boolean r)
    {
        drawBevel(g,0,0,d,b,r);
    }

    public void drawBevel (Graphics g, Dimension d, boolean r)
    {
        drawBevel(g,0,0,d,2,r);
    }

    private int multFix (int n, double m)
    {
        double i = (double) n;
        i *= m;
        if (i<0) i=0;
        if (i>255) i=255;
        return (int) i;
    }
}