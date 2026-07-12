"use strict";

// Port of end.java.

class EndLevel extends KLevel {
  constructor(game) {
    super(game);
    this.plot = "Ende";
    this.inst = "If you have 1000 pts, you can go to a bonus level.";

    this.text = [
      "Ende",
      "",
      "Ulla gibt ein Bild",
      "von den Schmugglern",
      "dem Kommisar",
      "",
      "Die Schmuggler sind",
      "im Gefängnis und Larry",
      "bekommt die Belohnung",
    ];

    this.ty = 300;
    this.ts = 1;
    this.fontmax = 40;
    this.fontmin = 10;
    this.star = [];
    this.stage = 1;  // 1 - text, 2 - move down, 9 - stop
    this.ct = 0;
    this.ct_to = 8;
    this.flash = false;
  }

  async loadData() {
    for (let i = 0; i < 100; i++) {
      this.star[i] = {
        x: Math.floor(Math.random() * 400),
        y: Math.floor(Math.random() * 300),
      };
    }

    if (this.game.score >= 1000) this.enableNextLevel();

    this.speed = 100;
    this.start();
  }

  drawStatic(g) {
    g.fillStyle = "#000";
    g.fillRect(0, 0, 400, 300);
  }

  drawNormal(g) {
    for (let i = 0; i < 100; i++) {
      g.fillStyle = (Math.random() < 0.5) ? "gray" : "white";
      g.fillRect(this.star[i].x, this.star[i].y, 1, 1);
    }

    if (this.stage === 1) {
      this.ty -= this.ts;
      let cy = this.ty;
      for (const line of this.text) {
        if (cy <= 300) {
          const size = Math.floor(this.fontmin + (this.fontmax - this.fontmin) * (cy / 300));
          g.font = `bold ${Math.max(size, 1)}px Arial, sans-serif`;
          this.drawText(g, line, { x: 200, y: Math.trunc(cy) },
                        KLevel.CENTER, KLevel.BOTTOM, [255, 255, 0], true);
          const m = g.measureText("M");
          cy += (m.fontBoundingBoxAscent + m.fontBoundingBoxDescent) || size * 1.2;
        }
      }
      if (cy < 10) {
        this.stage++;
      }
    }

    if (this.stage === 2) {
      for (let i = 0; i < 100; i++) {
        let y = this.star[i].y - 3;
        if (y < 0) y += 300;
        this.star[i].y = y;
      }

      if (++this.ct > this.ct_to) {
        this.ct = 0;
        this.flash = !this.flash;
      }
      if (this.flash) {
        const mes = (this.game.score < 1000) ? "Press RESTART" : "Go to Bonus Level!";
        g.font = "bold 27px Arial, sans-serif";
        this.drawText(g, mes, { x: 200, y: 100 },
                      KLevel.CENTER, KLevel.TOP, [255, 0, 0], true);
      }
    }
  }
}
