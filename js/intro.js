"use strict";

// Port of intro.java.

class IntroLevel extends KLevel {
  constructor(game) {
    super(game);
    this.plot = "Willkommen!";
    this.inst = "Click the NEXT button to go to the next level.";

    this.yshift = [-6, -6, -5, -4, -3, -2, -1, 0, 0, -1, -2, -3, -4, -5, -6];
    this.cols = [130, 140, 150, 160, 170, 180, 190, 200, 210, 220, 230, 240,
                 250, 255, 250, 240, 230, 220, 210, 200, 190, 180, 170, 160,
                 150, 140, 130];
    this.atColor = 0;
    this.moveColor = 1;
    this.mess = "Brad & Dan's \"Der verloren Koffer\"";
  }

  async loadData() {
    this.town = await loadImage("assets/intro.gif");
    this.anthem = new Snd("assets/intro.wav");
    this.anthem.loop();
    this.enableNextLevel();
    this.speed = 150;
    this.start();
  }

  cleanUp() {
    super.cleanUp();
    if (this.anthem) this.anthem.stop();
  }

  drawStatic(g) {
    g.drawImage(this.town, 0, 0);
  }

  drawNormal(g) {
    g.font = "italic bold 22px 'Times New Roman', serif";
    const m = g.measureText(this.mess);
    const tw = m.width;
    const tha = m.fontBoundingBoxAscent || 17;
    const drx = Math.floor((400 - tw) / 2);
    const dry = Math.floor((300 + tha / 2) / 2) - 50;

    let ax = drx;
    let tc = this.atColor;
    const half = Math.floor(this.mess.length / 2);

    for (const ch of this.mess) {
      const shift = this.yshift[tc % this.yshift.length];
      g.fillStyle = "#000";
      g.fillText(ch, ax + 2, dry + shift + 2);
      const green = Math.floor(1.7 * (this.cols[(tc + half) % this.cols.length] - 130));
      g.fillStyle = `rgb(234,${green},0)`;
      g.fillText(ch, ax, dry + shift);

      tc++;
      ax += g.measureText(ch).width;
    }

    this.atColor += this.moveColor;
  }
}
