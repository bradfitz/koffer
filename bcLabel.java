import java.awt.*;
import java.util.StringTokenizer;

public class bcLabel extends Canvas //implements Runnable
{
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

    private String deftext;
    private String text;
    private bcManager bcm;
    private int align;

    // off-screen graphics, reduce mild flicker ;)
    private Dimension oD;
    private Image oI;
    private Graphics oG;
    private int yl;

    Color emp = new Color(0,0,0);
    boolean emp_on;

    public bcLabel (bcManager bcm, String text, int align, String deftext)
    {
        this.text=text;
        this.bcm=bcm;
        this.align=align;
        this.deftext=deftext;
        setBackground(bcm.flat);
    }

    public bcLabel (bcManager bcm, String text, int align)
    {
        this(bcm, text, align, "");
    }

    public bcLabel (bcManager bcm, String text)
    {
        this(bcm, text, CENTER);
    }

    public bcLabel (bcManager bcm)
    {
        this(bcm, "",CENTER);
    }

    // make it Label-compliant
    public String getText ()             { return text;                 }
    public void setText (String text)    { this.text=text;   repaint(); }
    public int getAlignment ()           { return align;                }
    public void setAlignment (int align) { this.align=align; repaint(); }

    public void setEmphasis(boolean on, Color c)  { emp_on = on; emp = c; repaint(); }

    public void cancelText (String old)
    {
        if (text.equals(old))
        {
            this.text=deftext;
            repaint();
        }
    }

    public void paint(Graphics g) {
        update(g);
    }

    public void update(Graphics g)
    {
        Dimension d = size();

        //Create the offscreen graphics context, if no good one exists.
        if ((oG == null)||(d.width != oD.width)||(d.height != oD.height))
        {
            oD = d;
            oI = createImage(d.width, d.height);
            oG = oI.getGraphics();
        }
        Color bk = oG.getColor(); // push

        oG.setColor(bcm.flat);
        oG.fillRect(0,0,d.width,d.height);
        oG.setColor(emp_on ? emp : bcm.fore);

        StringTokenizer st = new StringTokenizer(text, " \t\r\n", true);

        yl = 0-oG.getFontMetrics().getDescent();
        StringBuffer sb = new StringBuffer();
        String lastgood = "";
        String curtok;

        while (st.hasMoreTokens()) {
            curtok = st.nextToken();
            if (oG.getFontMetrics().stringWidth(curtok)>d.width)
            {
                dumpLine(lastgood);
                dumpLine(curtok);
                lastgood = "";
                sb.setLength(0);
            }
            else
            {
                sb.append(curtok);
                if (oG.getFontMetrics().stringWidth(sb.toString())>d.width
                    || sb.toString().indexOf("\n")!=-1)
                {
                    dumpLine(lastgood);
                    lastgood = "";
                }
                else
                {
                    lastgood = sb.toString();
                }
            }
        }
        dumpLine(lastgood);

        oG.setColor(bk);  // pop

        g.drawImage(oI,0,0,this);
    }

    private void dumpLine (String line)
    {
        int w=oG.getFontMetrics().stringWidth(line);
        int dx=0;

        if (align==LEFT) dx=0;
        if (align==CENTER) dx=(size().width-1-w)/2;
        if (align==RIGHT) dx=size().width-1-w;

        if (line.length()!=0)
        {
            yl+=oG.getFontMetrics().getHeight();
            oG.drawString(line,dx,yl);
        }

    }

    public void addNotify ()
    {
        repaint();
        super.addNotify();
    }

    public Dimension preferredSize ()
    {
        if (oG != null)
        {
           return new Dimension(oG.getFontMetrics().stringWidth(text)+bcm.border*2,oG.getFontMetrics().getHeight()+bcm.border*2);
        }
        else
            return new Dimension(60,20);
//        return super.preferredSize ();
    }

    public Dimension minimumSize ()
    {
        if (oG != null)
        {
            return new Dimension(oG.getFontMetrics().stringWidth(text)+bcm.border,oG.getFontMetrics().getHeight()+bcm.border);
        }
        else
        return super.minimumSize ();
    }
}