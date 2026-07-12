"use strict";

// Port of drive.java, including its helper classes trapzoid, BloodSpot,
// and target.

class Trapzoid {
  constructor(tl, center, lr) {
    this.center_x = center.x;
    this.top_y = center.y;
    this.dent1 = this.center_x - tl.x;
    this.dent2 = lr.x - this.center_x;
    this.bot_y = lr.y;
  }

  // -50 <= xp <= 50, 0 <= yp <= 100 (integer math like the Java original)
  findPoint(xp, yp) {
    const y = this.top_y + Math.trunc((this.bot_y - this.top_y) * yp / 100);
    const x = this.center_x +
      Math.trunc(2 * xp * (this.dent1 + Math.trunc((this.dent2 - this.dent1) * yp / 100)) / 100);
    return { x, y };
  }
}

class BloodSpot {
  static red = ["rgb(255,0,0)", "rgb(190,0,0)", "rgb(140,0,0)"];

  // id 0 - blood, 1 - sign pieces
  constructor(id, x, y, dr) {
    this.id = id;
    this.xp = x;
    this.yp = Math.floor(Math.random() * 30) - 100 + y;
    this.sx = Math.floor(Math.random() * 7) - 3 + 2 * dr;
    this.sy = Math.floor(Math.random() * -10) - 3;
    this.wx = Math.floor(Math.random() * 6) + 2;
    this.wy = Math.floor(Math.random() * 6) + 2;
    this.rd = Math.floor(Math.random() * 3);
    this.used = true;
  }

  draw(g) {
    this.yp += this.sy;
    this.xp += this.sx;
    this.sy++;
    if (this.yp > 300) {
      this.used = false;
      return;
    }
    switch (this.id) {
      case 0:
        g.fillStyle = BloodSpot.red[this.rd];
        g.beginPath();
        g.ellipse(this.xp, this.yp, this.wx, this.wy, 0, 0, 2 * Math.PI);
        g.fill();
        g.strokeStyle = BloodSpot.red[2];
        g.stroke();
        break;
      case 1: {
        g.strokeStyle = "#fff";
        g.beginPath();
        g.moveTo(this.xp - this.wx, this.yp - this.wy);
        g.lineTo(this.xp + this.wx, this.yp + this.wy);
        g.stroke();
        g.strokeStyle = "gray";
        const r = Math.floor(Math.random() * 5);
        g.beginPath();
        g.moveTo(this.xp - this.wx + r, this.yp - this.wy);
        g.lineTo(this.xp + this.wx - r, this.yp + this.wy);
        g.stroke();
        break;
      }
    }
  }
}

class DriveTarget {
  constructor(id) {
    this.yp = 0;
    this.xp = Math.floor(Math.random() * 86 - 47);
    this.id = id;
    this.ct = 0;
    this.state = 0;
    if (id === 3) this.xp = (this.xp < 0) ? -55 : 55;
  }

  go() {  // returns true if the person got away safely
    this.ct++;
    if (this.ct > 3) {
      this.ct = 0;
      this.state = 1 - this.state;
    }
    this.yp += 5;
    if (this.yp > 150) this.id = -1;
    return this.id === -1;
  }
}

class DriveLevel extends KLevel {
  static AMOUNTBLOOD = 20;
  static NUMCLOUDS = 3;
  static DPS = 3; static DC = 3; static REP = 3;
  static SIGN_STEPS = 20; static SIGN_TOTAL_STEPS = 35; static SIGN_HEIGHT = 40;
  static CAR_MIN = -50; static CAR_MAX = 50;
  static BUMP_MIN = -33; static BUMP_MAX = 33;
  static SPEED_CHANGE = 3;

  constructor(game) {
    super(game);
    this.plot = "Larry fährt zum Dom.";
    this.inst = "Drive the car and try not to hit people.";

    this.car = [];
    this.roadsign = [];
    this.person = Array.from({ length: 5 }, () => []);
    this.blood = new Array(DriveLevel.AMOUNTBLOOD).fill(null);
    this.cd = [];
    this.sp = [];
    this.road = new Trapzoid({ x: 169, y: 141 }, { x: 200, y: 141 },
                             { x: 336, y: 299 });
    this.obs = [];

    this.comp = 0; this.comp_max = 50;
    this.comp_tick = 0; this.comp_ticktime = 10;
    this.showsplash = false;

    this.last = -1;
    this.lastkey = null;
    this.lastmousex = 0;

    this.dot = 0;
    this.sign_side = -1; this.sign_step = 0; this.sign_w = 0;
    this.targetshow = 42; this.targetat = 0;

    this.car_x = 0; this.car_sx = 0; this.car_y = 95; this.car_wid = 0;
  }

  async loadData() {
    this.scene = await loadImage("assets/drive_scene.gif");
    this.cloud = await loadImage("assets/cloud.gif");
    this.dom = await loadImage("assets/dom.gif");
    const allcar = await loadImage("assets/car.gif");
    for (let x = 0; x < 3; x++) {
      this.car[x] = cropImage(allcar, 97 * x, 0, 97, 71);
    }
    this.car_wid = 97;
    this.splat = new Snd("assets/splat.wav");
    this.hitpost = new Snd("assets/hitpost.wav");
    this.domharp = new Snd("assets/domharp.wav");

    const signs = await loadImage("assets/drive_signs.gif");
    const people = await loadImage("assets/drive_people.gif");

    const taken = [0, 0, 0];
    for (let i = 0; i < DriveLevel.NUMCLOUDS; i++) {
      this.cd[i] = { x: Math.floor(Math.random() * 400), y: i * 38 + 3 };
      let s;
      do {
        s = Math.floor(Math.random() * 3);
      } while (taken[s] !== 0);
      taken[s] = 1;
      this.sp[i] = s + 1;
    }

    let ct = 0;
    for (let y = 0; y < 2; y++) {
      for (let x = 0; x < 3; x++) {
        this.roadsign[ct++] = cropImage(signs, x * 40, y * 40, 40, 40);
      }
    }
    for (let y = 0; y < 3; y++) {
      for (let x = 0; x < 5; x++) {
        this.person[x][y] = cropImage(people, x * 50, y * 85, 50, 85);
      }
    }

    this.speed = 50;
    this.start();
  }

  drawStatic(g) {
    g.drawImage(this.scene, 0, 0);
  }

  drawNormal(g) {
    const D = DriveLevel;

    // lines on the road
    this.dot++;
    if (this.dot > D.DPS * D.DC) this.dot -= D.DPS * D.DC;
    const xt = 142;
    const dl = Math.trunc((300 - xt) / (D.DC * D.REP));
    const shift = Math.trunc(this.dot / D.DPS) * dl;
    g.fillStyle = "#ff0";
    for (let j = 0; j < D.REP; j++) {
      g.fillRect(199, xt + shift + j * dl * D.DC, 4, dl + 1);
    }

    // clouds
    for (let i = 0; i < D.NUMCLOUDS; i++) {
      g.drawImage(this.cloud, this.cd[i].x, this.cd[i].y);
    }
    for (let i = 0; i < D.NUMCLOUDS; i++) {
      this.cd[i].x -= this.sp[i];
      if (this.cd[i].x < -75) this.cd[i].x += 500;
    }

    // the road sign
    if (this.sign_side !== 0) {
      let six = 140 - Math.trunc((150 - 32) / D.SIGN_STEPS) * this.sign_step;
      let siy = 141 + Math.trunc((299 - 141) / D.SIGN_STEPS) * this.sign_step;
      if (this.sign_side === 1) six = 400 - six;

      g.fillStyle = "rgb(128,128,0)";
      g.fillRect(six, siy + 1 - D.SIGN_HEIGHT, 2, D.SIGN_HEIGHT);

      siy -= (D.SIGN_HEIGHT + 40);
      six -= 20;

      g.drawImage(this.roadsign[this.sign_w], six, siy);

      this.sign_step++;
      let hit = false;
      if (this.sign_step === D.SIGN_STEPS &&
          ((this.sign_side === -1 && this.car_x < D.BUMP_MIN) ||
           (this.sign_side === 1 && this.car_x > D.BUMP_MAX))) {
        this.explode(1, 5, six, siy + 60, 0);
        this.changeScore(-10);
        this.hitpost.play();
        hit = true;
      }

      if (this.sign_step >= D.SIGN_TOTAL_STEPS || hit) {
        this.sign_step = 0;
        this.sign_side *= -1;
        this.sign_w = Math.floor(Math.random() * 6);
      }
    }

    const cl = this.road.findPoint(this.car_x, this.car_y);
    const car_left = cl.x - Math.trunc(this.car_wid / 2);
    const car_right = cl.x + Math.trunc(this.car_wid / 2);
    let temp_car_y = cl.y;

    const bumpfactor = (this.car_x < D.BUMP_MIN || this.car_x > D.BUMP_MAX)
      ? Math.trunc(0 - Math.random() * 5) : 0;
    if (bumpfactor !== 0) {
      g.strokeStyle = "rgb(70,70,0)";
      g.beginPath();
      for (let i = 0; i < 15; i++) {
        g.moveTo(car_left + Math.random() * this.car_wid,
                 cl.y - 5 + Math.random() * 10);
        g.lineTo(car_left + Math.random() * this.car_wid,
                 cl.y - 5 + Math.random() * 10);
      }
      g.stroke();
      temp_car_y -= bumpfactor;
    }

    // decide when it's time for more targets (people)
    this.targetat++;
    if (this.targetat >= this.targetshow) {
      let idn = -1;
      while (idn === -1 || idn === this.last) {
        idn = Math.floor(Math.random() * 5);
      }
      this.last = idn;
      this.obs.push(new DriveTarget(idn));
      this.targetat = 0;
    }

    // move the targets
    for (const cur of this.obs) {
      if (cur.id !== -1) {
        const p = this.road.findPoint(cur.xp, cur.yp);
        let frame = 0;
        if (cur.yp < 80) frame++;
        if (cur.yp < 10) frame++;
        this.drawAlignImage(g, this.person[cur.id][frame], p.x, p.y,
                            KLevel.CENTER, KLevel.TOP, 1);

        if (cur.go()) this.changeScore(20);

        if (cur.yp > 100 && cur.yp < 135 && p.x > car_left && p.x < car_right) {
          cur.id = -1;
          this.splat.play();
          this.changeScore(-75);
          this.explode(0, 5, p.x, p.y, this.car_sx);
        }
      }
    }

    for (let i = 0; i < D.AMOUNTBLOOD; i++) {
      if (this.blood[i] !== null && this.blood[i].used) {
        this.blood[i].draw(g);
      }
    }

    // the car
    this.drawAlignImage(g, this.car[(this.car_sx === 0) ? 0 : (this.car_sx < 0 ? 1 : 2)],
                        cl.x, temp_car_y, KLevel.CENTER, KLevel.TOP);

    this.car_x += this.car_sx;
    if (this.car_x < D.CAR_MIN) this.car_x = D.CAR_MIN;
    if (this.car_x > D.CAR_MAX) this.car_x = D.CAR_MAX;

    let skip = false;
    // completion meter
    if (this.comp_tick++ > this.comp_ticktime) {
      this.comp_tick = 0;
      if (this.comp++ > this.comp_max) {
        this.speed = 3000;
        g.drawImage(this.dom, 133, -4);
        this.domharp.play();
        this.showsplash = true;
        skip = true;
      } else {
        if (this.targetshow-- < 5) this.targetshow = 50;
      }
    }

    g.fillStyle = "red";
    g.fillRect(5, 5, this.comp_max, 10);
    g.fillStyle = "rgb(0,255,0)";
    g.fillRect(5, 5, this.comp, 10);

    if (this.showsplash && !skip) {
      this.enableNextLevel();
      this.stop();
      g.drawImage(this.dom, 133, -4);
      this.displayText(g, "Hier ist der Dom +100 pts");
      this.changeScore(100);
    }
  }

  explode(id, amount, x, y, xs) {
    let ctb = 0;
    for (let bi = 0; bi < DriveLevel.AMOUNTBLOOD; bi++) {
      if (ctb < amount) {
        if (this.blood[bi] === null || !this.blood[bi].used) {
          this.blood[bi] = new BloodSpot(id, x, y, xs);
          ctb++;
        }
      }
    }
  }

  keyDown(key) {
    this.lastkey = key;
    switch (key) {
      case "left":
        this.car_sx = -DriveLevel.SPEED_CHANGE;
        break;
      case "right":
        this.car_sx = DriveLevel.SPEED_CHANGE;
        break;
    }
  }

  keyUp(key) {
    if (key === this.lastkey && (key === "left" || key === "right")) {
      this.car_sx = 0;
    }
  }

  mouseDown(x, y) {
    this.lastmousex = x;
    this.car_sx = 0;
  }

  mouseDrag(x, y) {
    if (x > this.lastmousex) this.car_sx = DriveLevel.SPEED_CHANGE;
    if (x < this.lastmousex) this.car_sx = -DriveLevel.SPEED_CHANGE;
    this.lastmousex = x;
  }

  mouseUp(x, y) {
    this.lastkey = null;
    this.car_sx = 0;
  }
}
