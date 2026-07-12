"use strict";

// Port of plane.java.

class PlaneLevel extends KLevel {
  static NUMCLOUDS = 7;
  static SPEED_CHANGE = 1;

  constructor(game) {
    super(game);
    this.plot = "Larry fliegt nach Deutschland";
    this.inst = "Land the plane.  Press & release up or down to change the speed of descent.";

    this.cd = [];
    this.sp = [];
    this.sky_col = "rgb(50,70,200)";
    this.moving = false;
    this.planeat = -280;
    this.plane_y = 10;
    this.plane_sy = 0;
    this.speed = 50;
  }

  async loadData() {
    this.scene = await loadImage("assets/fly_plane.gif");
    this.cloud = await loadImage("assets/cloud.gif");
    for (let i = 0; i < PlaneLevel.NUMCLOUDS; i++) {
      this.cd[i] = { x: Math.floor(Math.random() * 400), y: i * 38 + 3 };
      this.sp[i] = Math.floor(Math.random() * 4) + 3;
    }
    this.start();
  }

  drawStatic(g) {
    g.fillStyle = this.sky_col;
    g.fillRect(0, 0, 400, 300);
  }

  drawNormal(g) {
    for (let i = 0; i < 5; i++) {
      g.drawImage(this.cloud, this.cd[i].x, this.cd[i].y);
    }

    if (this.moving) this.planeat += 4;
    g.drawImage(this.scene, this.planeat, this.plane_y);
    this.plane_y += this.plane_sy;

    for (let i = 5; i < PlaneLevel.NUMCLOUDS; i++) {
      g.drawImage(this.cloud, this.cd[i].x, this.cd[i].y);
    }

    for (let i = 0; i < PlaneLevel.NUMCLOUDS; i++) {
      this.cd[i].x -= this.sp[i];
      if (this.cd[i].x < -75) this.cd[i].x += 500;
    }

    if (this.planeat > 400 || this.plane_y > 300) {
      let mes;
      this.stop();
      if (this.plane_y > 250) {
        mes = "Gut gemacht!  +100 points";
        this.changeScore(100);
      } else {
        mes = "Das Flugzeug ist zu hoch! -50 points";
        this.changeScore(-50);
      }
      this.displayText(g, mes);
      this.enableNextLevel();
    }
  }

  keyDown(key) {
    this.moving = true;

    switch (key) {
      case "up":
        this.plane_sy -= PlaneLevel.SPEED_CHANGE;
        break;
      case "down":
        this.plane_sy += PlaneLevel.SPEED_CHANGE;
        if (this.plane_sy > PlaneLevel.SPEED_CHANGE * 4) {
          this.plane_sy = PlaneLevel.SPEED_CHANGE * 4;
        }
        break;
    }
  }
}
