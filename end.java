import java.awt.*;

public class end extends klevel
{
    String[] text =  {
        "Ende",
        "",
        "Ulla gibt ein Bild",
        "von den Schmugglern",
        "dem Kommisar",
        "",
        "Die Schmuggler sind",
        "im Gefängnis und Larry",
        "bekommt die Belohnung"
    };

    double ty=300, ts=1, fontmax=40, fontmin=10;
    Point star[] = new Point[100];

    int stage = 1;  // 1 - text, 2  - move down, 9 - stop
    int ct = 0, ct_to = 8;
    boolean flash = false;

    public end ()
    {
        plot = "Ende";
        inst = "If you have 1000 pts, you can go to a bonus level.";
    }

    public void loadData ()
    {
        for (int i=0;i<100;i++) star[i] = new Point((int)(Math.random()*400),(int)(Math.random()*300));

        if (((koffer)master).score >= 1000) enableNextLevel();

        speed = 100;
        start();
    }

    public void drawStatic (Graphics g)
    {
        g.setColor(Color.black);
        g.fillRect(0,0,400,300);
    }

//  public void drawText (Graphics g, String s, Point p, int ha, int va, Color c, boolean three_d)

    public void drawNormal (Graphics g)
    {
        for (int i=0;i<100;i++)
        {
            g.setColor((Math.random()<.5)?Color.gray:Color.white);
            g.drawLine(star[i].x,star[i].y,star[i].x,star[i].y);
        }

        if (stage==1)
        {
            ty-=ts;
            double cy=ty;
            for (int i=0;i<text.length;i++)
            {
                if (cy<=300)
                {
                    g.setFont(new Font("Arial",Font.BOLD,(int)(fontmin+(fontmax-fontmin)*(cy/300))));
                    drawText(g,text[i],new Point(200,(int)cy),CENTER,BOTTOM, Color.yellow, true);
                    cy += g.getFontMetrics().getHeight();
                }
            }
            if (cy<10)
            {
                stage++;
                g.setFont(new Font("Arial",Font.BOLD,27));
            }
        }

        if (stage==2)
        {
            for (int i=0;i<100;i++)
            {
                int x = star[i].x, y = star[i].y;
                if ((y-=3)<0) y+=300;
                star[i] = new Point(x, y);
            }

            if (++ct>ct_to) { ct=0; flash=!flash; }
            if (flash)
            {
                String mes = (((koffer)master).score<1000) ? "Press RESTART" : "Go to Bonus Level!";
                drawText(g,mes,new Point(200,100),CENTER,TOP, Color.red, true);
            }
        }

    }



}