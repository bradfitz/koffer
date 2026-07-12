"use strict";

// Port of train.java.

class TrainLevel extends KLevel {
  constructor(game) {
    super(game);
    this.plot = "Larry fährt nach Mainz";
    this.inst = "Press right & left to drive the train.";

    this.sky_col = "rgb(50,70,200)";
    this.trainat = -350;
    this.mtat = 0;
    this.r = 0;
    this.mult = 0;
    this.multd = 0;
    this.checkat = 0;
    this.checktot = 6;
    this.horn = false;
  }

  async loadData() {
    this.mts = await loadImage("assets/train_mts.gif");
    this.train = await loadImage("assets/train.gif");
    this.cloud = await loadImage("assets/cloud.gif");
    this.choo = new Snd("assets/choo.wav");

    this.speed = 50;
    this.start();
  }

  drawNormal(g) {
    if (this.checkat++ > this.checktot) {
      this.checkat = 0;
      this.mult += this.multd;
      if (this.mult < -2) this.mult = -2;
      if (this.mult > 6) this.mult = 6;
    }

    this.mtat -= Math.trunc(0.5 * this.mult);
    if (this.mtat >= 0) this.mtat -= 400;
    g.drawImage(this.mts, this.mtat, 0);
    g.drawImage(this.mts, this.mtat + 400, 0);
    if (this.mtat < -400) this.mtat += 400;

    this.trainat += Math.trunc(1.5 * this.mult);
    g.drawImage(this.train, this.trainat, 300 - 78);

    // The connecting rod on the wheels: pivot at (259,61) radius 9,
    // reaching to x offset 333 within the train image.
    this.r += Math.PI / 180 * 5 * this.mult;
    g.fillStyle = "rgb(128,128,0)";
    const x1 = 259 + Math.floor(9 * Math.cos(this.r)) + this.trainat;
    const y1 = 61 + Math.floor(9 * Math.sin(this.r)) + (300 - 78);
    for (let i = -1; i <= 1; i++) {
      g.fillRect(x1, y1 + i, 333 - 259 + 1, 1);
    }

    if (this.trainat < -400) this.trainat = -400;
    if (this.trainat > 400) {
      this.stop();
      this.choo.play();
      this.displayText(g, "Willkommen in Mainz +100 pts");
      this.changeScore(100);
      this.enableNextLevel();
    }
  }

  drawStatic(g) {
    g.fillStyle = this.sky_col;
    g.fillRect(0, 0, 400, 300);
    g.drawImage(this.cloud, 25, 30);
    g.drawImage(this.cloud, 125, 55);
    g.drawImage(this.cloud, 225, 30);
    g.drawImage(this.cloud, 320, 55);
  }

  keyDown(key) {
    switch (key) {
      case "left":
        this.multd = -1;
        break;
      case "right":
        this.multd = 1;
        if (!this.horn) {
          this.choo.play();
          this.horn = true;
        }
        break;
    }
  }

  keyUp(key) {
    if (key === "left" || key === "right") this.multd = 0;
  }
}
