"use strict";

// Port of darts.java, including the Dart helper class.

class DartProjectile {
  constructor(tx, ty, sp) {
    const side = Math.floor(Math.random() * 3);
    switch (side) {
      case 0:
        this.x = Math.random() * 400;
        this.y = 300;
        break;
      case 1:
        this.x = 0;
        this.y = 160 + Math.random() * (400 - 160);
        break;
      case 2:
        this.x = 400;
        this.y = 160 + Math.random() * (400 - 160);
        break;
    }
    this.dir = this.dir_to(this.x, this.y, tx, ty);
    this.speed = sp;
    this.used = true;
  }

  // Returns true when the dart hits the rectangle r {x,y,w,h}.
  go(g, r) {
    this.x += this.speed * Math.cos(this.dir);
    this.y -= this.speed * Math.sin(this.dir);
    const p = { x: Math.trunc(this.x), y: Math.trunc(this.y) };

    const opdir = Math.PI - this.dir;
    const halfpi = Math.PI / 2;
    const back = this.movePoint(p, opdir, 12);
    const bl = this.movePoint(back, opdir - halfpi, 2);
    const br = this.movePoint(back, opdir + halfpi, 2);
    const taa = this.movePoint2(p, opdir, 3, opdir - halfpi, 2);
    const tab = this.movePoint2(p, opdir, 3, opdir + halfpi, 2);
    const tba = this.movePoint2(p, opdir, 9, opdir - halfpi, 2);
    const tbb = this.movePoint2(p, opdir, 9, opdir + halfpi, 2);

    g.strokeStyle = "#fff";
    g.beginPath();
    g.moveTo(p.x, p.y);
    g.lineTo(back.x, back.y);
    g.moveTo(bl.x, bl.y);
    g.lineTo(br.x, br.y);
    g.stroke();

    g.fillStyle = "red";
    g.beginPath();
    g.moveTo(taa.x, taa.y);
    g.lineTo(tab.x, tab.y);
    g.lineTo(tbb.x, tbb.y);
    g.lineTo(tba.x, tba.y);
    g.closePath();
    g.fill();

    if (this.x < 0 || this.x > 400 || this.y < 120 || this.y > 300) {
      this.used = false;
    }

    const ix = Math.trunc(this.x), iy = Math.trunc(this.y);
    return ix >= r.x && ix < r.x + r.w && iy >= r.y && iy < r.y + r.h;
  }

  dir_to(x1, y1, x2, y2) {
    if (x1 === x2) {
      return (y1 > y2) ? Math.PI / 2 : Math.PI * 3 / 2;
    }
    if (y1 === y2) {
      return (x1 > x2) ? Math.PI : 0;
    }
    const yleg = y2 - y1;
    const xleg = x2 - x1;
    const hyp = Math.sqrt(yleg * yleg + xleg * xleg);
    let ref = Math.asin(yleg / hyp);
    if (x2 < x1) ref = Math.PI + ref;
    else ref = -ref;
    return ref;
  }

  movePoint(p, d1, s1) {
    return {
      x: Math.trunc(p.x + Math.cos(d1) * s1),
      y: Math.trunc(p.y + Math.sin(d1) * s1),
    };
  }

  movePoint2(p, d1, s1, d2, s2) {
    return {
      x: Math.trunc(p.x + Math.cos(d1) * s1 + Math.cos(d2) * s2),
      y: Math.trunc(p.y + Math.sin(d1) * s1 + Math.sin(d2) * s2),
    };
  }
}

class DartsLevel extends KLevel {
  static SPEED_CHANGE = 7;
  static TOP_LIMIT = 173;
  static NUMDARTS = 50;

  constructor(game) {
    super(game);
    this.plot = "Larry ist am Ostportal";
    this.inst = "Dodge the darts and last as long as you can!!";

    this.man = Array.from({ length: 3 }, () => []);
    this.lasth = null;
    this.lastv = null;
    this.dx = 200; this.dy = 200;
    this.df = 0; this.dsx = 0; this.dsy = 0;
    this.ld = 0;  // 0 - face right, 1 - face left
    this.walkto = null;
    this.dart = new Array(DartsLevel.NUMDARTS).fill(null);
    this.dart_at = 0;
    this.dart_to = 30;
  }

  async loadData() {
    this.scene = await loadImage("assets/dart_scene.gif");
    const allman = await loadImage("assets/dart_man.gif");
    for (let y = 0; y < 2; y++) {
      for (let x = 0; x < 3; x++) {
        this.man[x][y] = cropImage(allman, x * 40, y * 83, 40, 83);
      }
    }
    this.speed = 50;
    this.start();
  }

  drawStatic(g) {
    g.drawImage(this.scene, 0, 0);
  }

  drawNormal(g) {
    const S = DartsLevel.SPEED_CHANGE;

    // mouse movements, if applicable
    if (this.walkto !== null) {
      this.dsx = 0;
      this.dsy = 0;
      if (this.walkto.x < this.dx) { this.dsx = -S; this.ld = 1; }
      if (this.walkto.x > this.dx) { this.dsx = S; this.ld = 0; }
      if (this.walkto.y < this.dy) this.dsy = -S;
      if (this.walkto.y > this.dy) this.dsy = S;

      if (Math.abs(this.walkto.x - this.dx) < S) this.walkto.x = this.dx;
      if (Math.abs(this.walkto.y - this.dy) < S) this.walkto.y = this.dy;
      if (this.walkto.x === this.dx && this.walkto.y === this.dy) {
        this.walkto = null;
        this.dsx = 0;
        this.dsy = 0;
      }
    }

    // move the man
    this.dx += this.dsx;
    this.dy += this.dsy;
    if (this.dx < 0) this.dx = 0;
    if (this.dx > 400) this.dx = 400;
    if (this.dy < DartsLevel.TOP_LIMIT) this.dy = DartsLevel.TOP_LIMIT;
    if (this.dy > 341) this.dy = 341;

    // if applicable, add more darts
    if (++this.dart_at > this.dart_to) {
      this.dart_at = 0;
      let fd = -1;
      for (let i = 0; i < DartsLevel.NUMDARTS; i++) {
        if (fd === -1 && (this.dart[i] === null || this.dart[i].used === false)) {
          fd = i;
        }
      }
      if (fd !== -1) {
        this.dart[fd] = new DartProjectile(this.dx, this.dy - 40, 5);
      }
      if ((this.dart_to -= 1) < 2) this.dart_to = 2;

      this.changeScore(5);
    }

    if (++this.df > 2) this.df = 0;
    const moving = (this.dsx !== 0 || this.dsy !== 0);
    g.drawImage(this.man[moving ? this.df : 1][this.ld], this.dx - 20, this.dy - 83);

    const r = { x: this.dx - 15, y: this.dy - 80, w: 30, h: 60 };

    let dead = false;
    for (let i = 0; i < DartsLevel.NUMDARTS; i++) {
      if (this.dart[i] !== null && this.dart[i].used) {
        if (this.dart[i].go(g, r)) dead = true;
      }
    }

    if (dead) {
      this.stop();
      this.enableNextLevel();
      this.displayText(g, "Du bist betäubt...");
    }
  }

  keyDown(key) {
    const S = DartsLevel.SPEED_CHANGE;
    switch (key) {
      case "left":
        this.lasth = key;
        this.dsx = -S;
        this.ld = 1;
        break;
      case "right":
        this.lasth = key;
        this.dsx = S;
        this.ld = 0;
        break;
      case "up":
        this.lastv = key;
        this.dsy = -S;
        break;
      case "down":
        this.lastv = key;
        this.dsy = S;
        break;
    }
  }

  keyUp(key) {
    switch (key) {
      case "left":
      case "right":
        if (key === this.lasth) this.dsx = 0;
        break;
      case "up":
      case "down":
        if (key === this.lastv) this.dsy = 0;
        break;
    }
  }

  mouseDown(x, y) {
    this.lasth = null;
    this.lastv = null;
    this.mouseDrag(x, y);
  }

  mouseDrag(x, y) {
    if (y < DartsLevel.TOP_LIMIT) y = DartsLevel.TOP_LIMIT;
    this.walkto = { x, y };
  }

  mouseUp(x, y) {
    this.dsx = 0;
    this.dsy = 0;
  }
}
