import java.awt.*;
import java.applet.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.CropImageFilter;

public class socks extends klevel {
    int numMatched;
    String[] names = {"ein Diamant", "eine Socke", "eine Jacke", "eine Hose", "ein Schuh", "ein Säckchen", "ein Sporthemd", "ein Hut"};
    Image[] casePic = new Image[2];
    Image[] itemPic = new Image[8];
    int[][] boxID = new int[5][3];
    int[][] boxST = new int[5][3];
    Image cases, items, sockback;
    Rectangle[][] content = new Rectangle[5][3];
    boolean timOn = false;
    Point[] curOpen = new Point[2];
    int numOpen = 0, numPRINT = 0;
    int[][] PRINTed = new int[5][3];
    AudioClip Ja, Nein, DiamantSound;

    public socks() {
        inst = "Click and Match the Items.";
        plot = "Was ist im Koffer?";
    }

    public void loadData() {
        Ja = waitAudioClip("yes.au");
        Nein = waitAudioClip("no.au");
        DiamantSound = waitAudioClip("diamant.au");
        cases = waitImage("cases.gif");
        items = waitImage("items.gif");
        sockback = waitImage("sockback.jpg");
        MediaTracker track = new MediaTracker(this);
        for ( int buf= 0; buf < 2; buf++ )
        {
            casePic[buf] = cropImage(cases, buf*40, 0, 40, 40);
            track.addImage(casePic[buf], 0);
        }
        for ( int buf = 0; buf < 8; buf++) {
            itemPic[buf] = cropImage(items, buf*20, 0, 20, 20);
            track.addImage(itemPic[buf], 0);
        }


        try
        {
            track.waitForID(0);
        } catch (Exception e) { }



        int rx=0, ry=0;
        int ct = 0;

        for (int item=0; item< 8; item++)
        {

        	for (int howmany=0; howmany<((item==0)?1:2); howmany++)
        	{
        		do
        		{

        			rx = (int)(Math.random() * 5d);
        			ry = (int)(Math.random() * 3d);
        		} while (boxID[rx][ry]!=0);

        		boxID[rx][ry] = item;
        		ct++;

        	}
        }
        for( int i = 0; i < 5; i++) for (int j = 0; j < 3; j++) {
            content[i][j] = new Rectangle(i*40+20*(i)+60, j*40+20*(j)+100, 40, 40);

        }


            speed = 1000;
            start();
   }

    public void drawStatic (Graphics g)
    {
        g.drawImage(sockback,0,0,this);
    }


    public void drawNormal(Graphics g) {
        numMatched = 0;
            for (int i = 0; i < 5; i++) for ( int j = 0; j < 3; j++) {
                switch (boxST[i][j]) {
                    case 0:
                        g.drawImage(casePic[0], content[i][j].x, content[i][j].y, this);
                        break;
                    case 1:
                        g.drawImage(casePic[1], content[i][j].x, content[i][j].y, this);
                        g.drawImage(itemPic[boxID[i][j]], content[i][j].x+10, content[i][j].y, this);
                        if ((numPRINT == 0) && (PRINTed[i][j] == 0)) {PRINTed[i][j] = 1;} else
                        if ((numPRINT == 1) && (PRINTed[i][j] == 0)) { PRINTed[i][j] = 2;}

                        if (PRINTed[i][j] == 1) {
                            drawText(g, names[boxID[i][j]], new Point(25,25), RIGHT, TOP, Color.white, false);
                            numPRINT = 1;

                        } else if (PRINTed[i][j] == 2) {
                            drawText(g, "und",  new Point(200,25), CENTER, TOP, Color.white, false);
                            drawText(g, names[boxID[i][j]], new Point(375,25), LEFT, TOP, Color.white, false);
                            numPRINT = 0;

                        }
                        break;

                    case 2:
                        numMatched++;
                        break;
                    case 3:

                        drawText(g, "ein Diamant", new Point(200,75), CENTER, TOP, Color.cyan, false);
                        g.drawImage(casePic[1], content[i][j].x, content[i][j].y, this);
                        g.drawImage(itemPic[0], content[i][j].x+10, content[i][j].y, this);
                        boxST[i][j] = 2;
                        DiamantSound.play();
                        enableNextLevel();
                        changeScore(100);
                        timOn = false;

                        break;
                }



            }


                if (numOpen >= 2) {
                    numOpen = 0;
                    timOn = false;

                checkMatch(g, curOpen[0].x, curOpen[0].y, curOpen[1].x, curOpen[1].y);
                }

                if (timOn && numMatched < 15) { timOn = false; stop(); } else if (numMatched >= 15) {
                    displayText(g, "Next Level: +100pts");
                    changeScore(100);
                    stop();
                    numOpen =0;
} else  {timOn = true; }


    }
    public boolean mouseDown(Event e, int x, int y) {
        for ( int i = 0; i < 5; i++) for ( int j = 0; j < 3; j++) {



            if ((content[i][j].inside(x, y) == true) && (boxST[i][j] == 0)) {
                boxST[i][j] = 1;
                if (boxID[i][j] == 0) {

                boxST[i][j] = 3;



                        } else {

                numOpen++;
                curOpen[numOpen-1] = new Point(i , j);
                        }
                timOn = false;
                start();
            }

        }
    return true;
    }


    public boolean checkMatch(Graphics g, int x1, int y1, int x2, int y2) {

        boolean retval = false;
        if (boxID[x1][y1] == boxID[x2][y2]) {
            drawText(g, "Ja", new Point(200,75), CENTER, TOP, Color.yellow, false);
            Ja.play();
            boxST[x1][y1] = 2;
            boxST[x2][y2] = 2;
            timOn = false;
            retval = true;
            changeScore(50);
        } else {
            boxST[x1][y1] = 0;
            boxST[x2][y2] = 0;
            drawText(g, "Nein", new Point(200,75), CENTER, TOP, Color.yellow, false);
            Nein.play();
            timOn = false;
            changeScore(-10);
        }

            PRINTed[x1][y1] = 0;
            PRINTed[x2][y2] = 0;
numPRINT = 0;
        return retval;
    }
    public boolean drawCrap(Graphics g, int i, int j, int numPRINT) {
        switch (numPRINT) {
            case 0:
             drawText(g, names[boxID[i][j]], new Point(25,25), RIGHT, TOP, Color.white, false);
             numPRINT++;
             break;
             case 1:
             drawText(g, "und" , new Point(200, 25), CENTER, TOP, Color.white, false);
             drawText(g, names[boxID[i][j]], new Point(375, 25), LEFT, TOP, Color.white, false);
             numPRINT = 0;
             break;
        }

    return true;
    }


}