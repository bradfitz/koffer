"use strict";

// Port of socks.java (memory-match game). The original logic is quirky
// (including the diamond placement: item 0 marks its cell as 0, i.e. still
// "free", so the diamond ends up in whichever cell is left unfilled) and is
// preserved as-is.

class SocksLevel extends KLevel {
  constructor(game) {
    super(game);
    this.plot = "Was ist im Koffer?";
    this.inst = "Click and Match the Items.";

    this.names = ["ein Diamant", "eine Socke", "eine Jacke", "eine Hose",
                  "ein Schuh", "ein Säckchen", "ein Sporthemd", "ein Hut"];
    this.numMatched = 0;
    this.casePic = [];
    this.itemPic = [];
    this.boxID = Array.from({ length: 5 }, () => [0, 0, 0]);
    this.boxST = Array.from({ length: 5 }, () => [0, 0, 0]);
    this.content = Array.from({ length: 5 }, () => [null, null, null]);
    this.timOn = false;
    this.curOpen = [null, null];
    this.numOpen = 0;
    this.numPRINT = 0;
    this.PRINTed = Array.from({ length: 5 }, () => [0, 0, 0]);
  }

  async loadData() {
    this.Ja = new Snd("assets/yes.wav");
    this.Nein = new Snd("assets/no.wav");
    this.DiamantSound = new Snd("assets/diamant.wav");
    const cases = await loadImage("assets/cases.gif");
    const items = await loadImage("assets/items.gif");
    this.sockback = await loadImage("assets/sockback.jpg");

    for (let buf = 0; buf < 2; buf++) {
      this.casePic[buf] = cropImage(cases, buf * 40, 0, 40, 40);
    }
    for (let buf = 0; buf < 8; buf++) {
      this.itemPic[buf] = cropImage(items, buf * 20, 0, 20, 20);
    }

    for (let item = 0; item < 8; item++) {
      for (let howmany = 0; howmany < ((item === 0) ? 1 : 2); howmany++) {
        let rx, ry;
        do {
          rx = Math.floor(Math.random() * 5);
          ry = Math.floor(Math.random() * 3);
        } while (this.boxID[rx][ry] !== 0);
        this.boxID[rx][ry] = item;
      }
    }
    for (let i = 0; i < 5; i++) {
      for (let j = 0; j < 3; j++) {
        this.content[i][j] = { x: i * 60 + 60, y: j * 60 + 100, w: 40, h: 40 };
      }
    }

    this.speed = 1000;
    this.start();
  }

  drawStatic(g) {
    g.drawImage(this.sockback, 0, 0);
  }

  drawNormal(g) {
    g.font = "12px sans-serif";
    this.numMatched = 0;
    for (let i = 0; i < 5; i++) {
      for (let j = 0; j < 3; j++) {
        const c = this.content[i][j];
        switch (this.boxST[i][j]) {
          case 0:
            g.drawImage(this.casePic[0], c.x, c.y);
            break;
          case 1:
            g.drawImage(this.casePic[1], c.x, c.y);
            g.drawImage(this.itemPic[this.boxID[i][j]], c.x + 10, c.y);
            if (this.numPRINT === 0 && this.PRINTed[i][j] === 0) {
              this.PRINTed[i][j] = 1;
            } else if (this.numPRINT === 1 && this.PRINTed[i][j] === 0) {
              this.PRINTed[i][j] = 2;
            }

            if (this.PRINTed[i][j] === 1) {
              this.drawText(g, this.names[this.boxID[i][j]], { x: 25, y: 25 },
                            KLevel.RIGHT, KLevel.TOP, [255, 255, 255], false);
              this.numPRINT = 1;
            } else if (this.PRINTed[i][j] === 2) {
              this.drawText(g, "und", { x: 200, y: 25 },
                            KLevel.CENTER, KLevel.TOP, [255, 255, 255], false);
              this.drawText(g, this.names[this.boxID[i][j]], { x: 375, y: 25 },
                            KLevel.LEFT, KLevel.TOP, [255, 255, 255], false);
              this.numPRINT = 0;
            }
            break;
          case 2:
            this.numMatched++;
            break;
          case 3:
            this.drawText(g, "ein Diamant", { x: 200, y: 75 },
                          KLevel.CENTER, KLevel.TOP, [0, 255, 255], false);
            g.drawImage(this.casePic[1], c.x, c.y);
            g.drawImage(this.itemPic[0], c.x + 10, c.y);
            this.boxST[i][j] = 2;
            this.DiamantSound.play();
            this.enableNextLevel();
            this.changeScore(100);
            this.timOn = false;
            break;
        }
      }
    }

    if (this.numOpen >= 2) {
      this.numOpen = 0;
      this.timOn = false;
      this.checkMatch(g, this.curOpen[0].x, this.curOpen[0].y,
                      this.curOpen[1].x, this.curOpen[1].y);
    }

    if (this.timOn && this.numMatched < 15) {
      this.timOn = false;
      this.stop();
    } else if (this.numMatched >= 15) {
      this.displayText(g, "Next Level: +100pts");
      this.changeScore(100);
      this.stop();
      this.numOpen = 0;
    } else {
      this.timOn = true;
    }
  }

  mouseDown(x, y) {
    for (let i = 0; i < 5; i++) {
      for (let j = 0; j < 3; j++) {
        const c = this.content[i][j];
        if (x >= c.x && x < c.x + c.w && y >= c.y && y < c.y + c.h &&
            this.boxST[i][j] === 0) {
          this.boxST[i][j] = 1;
          if (this.boxID[i][j] === 0) {
            this.boxST[i][j] = 3;
          } else {
            this.numOpen++;
            this.curOpen[this.numOpen - 1] = { x: i, y: j };
          }
          this.timOn = false;
          this.start();
        }
      }
    }
  }

  checkMatch(g, x1, y1, x2, y2) {
    if (this.boxID[x1][y1] === this.boxID[x2][y2]) {
      this.drawText(g, "Ja", { x: 200, y: 75 },
                    KLevel.CENTER, KLevel.TOP, [255, 255, 0], false);
      this.Ja.play();
      this.boxST[x1][y1] = 2;
      this.boxST[x2][y2] = 2;
      this.timOn = false;
      this.changeScore(50);
    } else {
      this.boxST[x1][y1] = 0;
      this.boxST[x2][y2] = 0;
      this.drawText(g, "Nein", { x: 200, y: 75 },
                    KLevel.CENTER, KLevel.TOP, [255, 255, 0], false);
      this.Nein.play();
      this.timOn = false;
      this.changeScore(-10);
    }

    this.PRINTed[x1][y1] = 0;
    this.PRINTed[x2][y2] = 0;
    this.numPRINT = 0;
  }
}
