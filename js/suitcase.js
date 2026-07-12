"use strict";

// Port of suitcase.java.

class SuitcaseLevel extends KLevel {
  static NUMCASES = 10;

  constructor(game) {
    super(game);
    this.plot = "Larry ist am Flughafen";
    this.inst = "Click on the bag you think is Larry's";

    this.bagClicked = -1;
    this.r = [];
    this.r_base = 0;
    this.time = 0;
  }

  async loadData() {
    this.time = performance.now();

    this.scene = await loadImage("assets/case_scene.gif");
    this.man = await loadImage("assets/case_man.gif");
    this.acase = await loadImage("assets/case.gif");
    for (let i = 0; i < SuitcaseLevel.NUMCASES; i++) {
      this.r[i] = 2 * Math.PI / SuitcaseLevel.NUMCASES * i;
    }

    this.speed = 100;
    this.start();
  }

  _bagXY(i) {
    return {
      x: 199 - 25 + Math.floor(Math.cos(this.r[i] + this.r_base) * 154),
      y: 160 - 18 + Math.floor(Math.sin(this.r[i] + this.r_base) * 49),
    };
  }

  drawNormal(g) {
    this.r_base += (2 * Math.PI) / 360;
    if (this.r_base > 2 * Math.PI) this.r_base -= 2 * Math.PI;

    for (let i = 0; i < SuitcaseLevel.NUMCASES; i++) {
      if (i !== this.bagClicked) {
        const p = this._bagXY(i);
        g.drawImage(this.acase, p.x, p.y);
      }
    }
    g.drawImage(this.man, 200, 150);

    if (this.bagClicked !== -1) {
      this.stop();
      this.displayText(g, "Hmmm... es ist schwerer zu ihn...");
      this.enableNextLevel();
    }
  }

  drawStatic(g) {
    g.drawImage(this.scene, 0, 0);
  }

  mouseDown(x, y) {
    const now = performance.now();
    for (let i = 0; i < SuitcaseLevel.NUMCASES; i++) {
      const p = this._bagXY(i);
      if (x >= p.x && x < p.x + 50 && y >= p.y && y < p.y + 36 &&
          now > this.time + 500) {
        this.bagClicked = i;
      }
    }
  }
}
