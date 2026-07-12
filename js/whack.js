"use strict";

// Port of whack.java, including the frauTarget helper class.

class FrauTarget {
  static notify = null;
  static hits = 0;
  static misses = 0;
  static total = 0;
  static pic = [null, null];
  static DEEP = 114;
  static WIDE = 90;

  constructor(x, y) {
    this.showing = false;
    this.whacked = false;
    this.bx = x;
    this.by = y;
    this.depth = FrauTarget.DEEP;
    this.speed = 0;
    this.ct = 0;
    this.ct_to = 0;
  }

  rise(speed, ct_to) {
    if (!this.showing && FrauTarget.total < 50) {
      this.showing = true;
      this.speed = speed;
      this.ct_to = ct_to;
      this.whacked = false;
      this.ct = 0;
      FrauTarget.total++;
      return true;
    }
    return false;
  }

  whack() {
    this.whacked = true;
    this.speed = 0;
    this.depth += 4;
    FrauTarget.hits++;
    FrauTarget.notify.updateScore(FrauTarget.hits, FrauTarget.misses, FrauTarget.total);
    FrauTarget.notify.changeScore(5);
  }

  go(g) {
    if (!this.showing) return;
    this.depth += this.speed;
    if (this.depth > FrauTarget.DEEP) {
      this.depth = FrauTarget.DEEP;
      this.speed = 0;
      this.ct = 0;
      if (!this.whacked) {
        FrauTarget.misses++;
        FrauTarget.notify.changeScore(-5);
      }
      FrauTarget.notify.updateScore(FrauTarget.hits, FrauTarget.misses, FrauTarget.total);
      this.whacked = false;
      this.showing = false;
    }
    if (this.depth < 0) this.depth = 0;
    if (this.depth === 0 || (this.showing && this.whacked)) {
      if (this.ct++ > this.ct_to) this.speed = WhackLevel.DOWN_SPEED;
    }

    g.drawImage(FrauTarget.pic[this.whacked ? 1 : 0], this.bx, this.by + this.depth);
  }

  area() {
    return {
      x: this.bx, y: this.by + this.depth,
      w: FrauTarget.WIDE, h: FrauTarget.DEEP - this.depth,
    };
  }

  isFine() { return !this.whacked; }

  static reset() {
    FrauTarget.hits = 0;
    FrauTarget.misses = 0;
    FrauTarget.total = 0;
  }
}

class WhackLevel extends KLevel {
  static DOWN_SPEED = 12;
  static UP_SPEED = -8;
  static TIME_UP = 12;

  constructor(game) {
    super(game);
    this.plot = "Frau Johnson ist nicht Frau Johnson!!";
    this.inst = "It's Whack-Attack, and the smugglers are in disguise...";

    this.board = [];
    this.stick = [];
    this.fraupic = [];
    this.frau = [];
    this.cx = 0; this.cy = 0; this.cp = -1;
    this.riseat = 0; this.riseto = 40;
    this.lnMiss = 0; this.lnHit = 0; this.lnTotal = 0;
  }

  async loadData() {
    FrauTarget.reset();
    FrauTarget.notify = this;
    this.frau[0] = new FrauTarget(43, 59);
    this.frau[1] = new FrauTarget(154, 59);
    this.frau[2] = new FrauTarget(265, 59);
    this.frau[3] = new FrauTarget(32, 126);
    this.frau[4] = new FrauTarget(153, 126);
    this.frau[5] = new FrauTarget(276, 126);

    this.changeCursor(1);
    const bothfrau = await loadImage("assets/whack_frau.gif");
    for (let x = 0; x < 2; x++) {
      this.fraupic[x] = cropImage(bothfrau, x * 90, 0, 90, 114);
      FrauTarget.pic[x] = this.fraupic[x];
    }

    const bothstick = await loadImage("assets/whack_stick.gif");
    for (let x = 0; x < 2; x++) {
      this.stick[x] = cropImage(bothstick, x * 40, 0, 40, 195);
    }

    const allboard = await loadImage("assets/whack_holes.gif");
    this.board[0] = cropImage(allboard, 0, 0, 400, 200);
    this.board[1] = cropImage(allboard, 0, 200, 400, 150);
    this.board[2] = cropImage(allboard, 0, 350, 400, 92);

    this.punch = new Snd("assets/punch.wav");

    this.speed = 100;
    this.start();
  }

  cleanUp() {
    super.cleanUp();
    this.changeCursor(0);
  }

  updateScore(hit, miss, total) {
    this.lnHit = hit;
    this.lnMiss = miss;
    this.lnTotal = total;
  }

  // Java cursor ids: 0 = default, 1 = crosshair.
  changeCursor(curs) {
    this.game.canvas.style.cursor = (curs === 1) ? "crosshair" : "default";
  }

  mouseDrag(x, y) {
    this.cp = 1;
    this.cx = x;
    this.cy = y;
  }

  mouseMove(x, y) {
    this.cp = 0;
    this.cx = x;
    this.cy = y;
  }

  mouseExit() {
    this.cp = -1;
  }

  mouseDown(x, y) {
    let ww = -1;
    for (let i = 5; i >= 0; i--) {
      const a = this.frau[i].area();
      if (x >= a.x && x < a.x + a.w && y >= a.y && y < a.y + a.h &&
          ww === -1 && this.frau[i].isFine()) {
        ww = i;
      }
    }
    if (ww !== -1) {
      this.punch.play();
      this.frau[ww].whack();
    }

    this.cp = 1;
    this.cx = x;
    this.cy = y;
  }

  mouseUp(x, y) {
    this.cp = 0;
  }

  drawStatic(g) {
    g.fillStyle = "#fff";
    g.fillRect(0, 0, 400, 300);
    g.drawImage(this.board[0], 0, 100);
  }

  drawNormal(g) {
    g.font = "bold 18px 'Times New Roman', serif";

    if (++this.riseat > this.riseto) {
      let tr = 0;
      while (!this.frau[Math.floor(Math.random() * 6)]
               .rise(WhackLevel.UP_SPEED, WhackLevel.TIME_UP) && tr < 10) {
        tr++;
      }
      if ((this.riseto -= 2) < 2) this.riseto = 5;
      this.riseat = 0;
    }

    for (let i = 0; i < 3; i++) this.frau[i].go(g);
    g.drawImage(this.board[1], 0, 150);

    for (let i = 3; i < 6; i++) this.frau[i].go(g);
    g.drawImage(this.board[2], 0, 208);

    this.twoColor(g, 5, 20, "Hits: ", String(this.lnHit));
    this.twoColor(g, 200, 20, "Misses: ", String(this.lnMiss));

    if (this.cp !== -1) {
      g.drawImage(this.stick[this.cp], this.cx - 20, this.cy - 30);
    }

    if (this.lnHit + this.lnMiss >= 50) {
      this.stop();
      this.displayText(g, "Sehr gut!");
      this.changeCursor(0);
    }
  }

  twoColor(g, x, y, a, b) {
    g.fillStyle = "#000";
    g.fillText(a, x, y);
    g.fillStyle = "red";
    x += g.measureText(a).width + 2;
    g.fillText(b, x, y);
  }
}
