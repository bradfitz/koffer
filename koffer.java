import java.awt.*;
import java.util.Vector;
import java.util.StringTokenizer;

public class koffer extends java.applet.Applet
{
    Choice whichlev = new Choice();

    bcManager bcm = new bcManager(70,70,70,255,255,255,2);
    bcButton gobut = new bcButton(bcm,"Restart Game");
    bcButton nextbutton = new bcButton(bcm,"Next Level");
    bcLabel title = new bcLabel(bcm, "", bcLabel.LEFT);
    bcLabel inst = new bcLabel(bcm, "", bcLabel.LEFT);
    bcLabel scorelabel = new bcLabel(bcm, "Score: 0", bcLabel.LEFT);

    boolean ingame = true;

    int level=0;
    int score=0;

    klevel curLevel;
    final static int gx = 5, gy = 45, gw = 400, gh = 300;
    Vector levname = new Vector();

    public void init ()
    {
        curLevel = new klevel();
        klevel.master = this;
        Dimension d = size();
        setLayout(null);
        setBackground(Color.black);

        title.setFont(new Font("TimesRoman", Font.BOLD, 12));
        inst.setFont(new Font("TimesRoman", 0, 12));
        inst.setEmphasis(true, Color.yellow);

        Panel p = new Panel();
        p.setBackground(bcm.flat);
        p.setLayout(new GridLayout(2,1));
        p.add(title);
        p.add(inst);
        add(p);
        p.reshape(5,5,400,35);

        add(gobut);
        gobut.reshape(5,gy+gh+5,100,20);
        add(nextbutton);
        nextbutton.reshape(gx+gw-101,gy+gh+5,100,20);

        scorelabel.setFont(new Font("Helvetica", 0, 15));
        add(scorelabel);
        scorelabel.reshape(110,gy+gh+5,100,20);

        String dim = getParameter("disable_dim");
        if (dim != null && Integer.valueOf(dim).intValue()==1)
            klevel.disable_dim = true;

        String t = getParameter("sceneorder");
        StringTokenizer tok = new StringTokenizer(t,"|",false);
        while (tok.countTokens()!=0)
        {
            String next = tok.nextToken();
            levname.addElement(next);
            whichlev.addItem(next);
        }
        add(whichlev);
        whichlev.reshape(   nextbutton.location().x-80,
                            nextbutton.location().y,
                            75,
                            25
                        );
    }

    public void start()
    {
        setLevel(0);
    }

    public void stop ()
    {
        if (curLevel != null)
        {
            curLevel.cleanUp();
//          remove(curLevel);
        }
    }

    public void loadLevel (String levelName)
    {
        whichlev.select(levelName);
        curLevel.cleanUp();
        remove(curLevel);

        curLevel = new klevel();
        title.setText("");
        inst.setText("");
        nextbutton.enable(false);

        try
        {
            curLevel = (klevel)Class.forName(levelName).newInstance();
        } catch (Exception e)
        {
            System.out.println("Error loading level: "+levelName);
        }

        title.setText(curLevel.plot);
        inst.setText(curLevel.inst);

        add(curLevel);
        curLevel.reshape(gx,gy,gw,gh);
        curLevel.requestFocus();
    }

    public boolean action (Event e, Object blah)
    {
        if (e.target==gobut)
        {
            ingame = true;
            nextbutton.enable(true);
            score=0;
            updateScore();
            setLevel(0);
        }
        if (e.target==nextbutton)
        {
            nextLevel();
        }
        if (e.target==whichlev)
        {
            if (! whichlev.getSelectedItem().equals("whack"))
            {
                ingame = false;
                nextbutton.enable(false);
                score = 0;
                updateScore();
                loadLevel(whichlev.getSelectedItem());
            } else
            {

            }
        }
        return true;

    }

    public void showLevel (int i)
    {
        loadLevel((String) levname.elementAt(i));
    }

    public void setLevel (int lev)
    {
        level=lev;
        showLevel(level);
    }

    public void nextLevel ()
    {
        setLevel(++level);
    }

    public void updateScore ()
    {
        if (ingame)
            scorelabel.setText("Score: "+Integer.toString(score));
            else
            scorelabel.setText("Not in Game!");
    }

    public void changeScore (int change)
    {
        score+=change;
        updateScore();
    }

    public void paint (Graphics g)
    {
        Dimension d = size();
        g.setColor(bcm.flat);
        g.fillRect(0,0,d.width,d.height);
        bcm.drawBevel(g,d,2,true);
        bcm.drawBevel(g,gx-2,gy-2,new Dimension(400+4,300+4),2,false);
    }


}
