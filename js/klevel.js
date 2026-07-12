"use strict";

// Port of klevel.java plus small asset helpers.

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error("failed to load " + src));
    img.src = src;
  });
}

// Equivalent of klevel.cropImage: returns a canvas holding the sub-image.
function cropImage(img, x, y, w, h) {
  const c = document.createElement("canvas");
  c.width = w;
  c.height = h;
  c.getContext("2d").drawImage(img, x, y, w, h, 0, 0, w, h);
  return c;
}

// Wrapper matching java.applet.AudioClip (play / loop / stop).
// Browsers block audio before the first user gesture, so a failed
// play is queued and retried on the next gesture.
class Snd {
  static pending = [];
  constructor(url) {
    this.a = new Audio(url);
  }
  play() {
    this.a.loop = false;
    this._go();
  }
  loop() {
    this.a.loop = true;
    this._go();
  }
  stop() {
    this.a.pause();
    this.a.currentTime = 0;
    Snd.pending = Snd.pending.filter(s => s !== this);
  }
  _go() {
    this.a.currentTime = 0;
    const p = this.a.play();
    if (p) p.catch(() => {
      if (!Snd.pending.includes(this)) Snd.pending.push(this);
    });
  }
  static unlock() {
    Snd.pending.splice(0).forEach(s => s._go());
  }
}
for (const ev of ["pointerdown", "keydown"]) {
  window.addEventListener(ev, () => Snd.unlock(), true);
}

function multFix(n, m) {
  let i = n * m;
  if (i < 0) i = 0;
  if (i > 255) i = 255;
  return Math.floor(i);
}

// Colors are [r,g,b] arrays so drawText can compute 3-D shades.
function changeColor(c, change) {
  const m = (change === 1) ? 23 / 17 : ((change === -1) ? 9 / 17 : 1);
  return `rgb(${multFix(c[0], m)},${multFix(c[1], m)},${multFix(c[2], m)})`;
}

class KLevel {
  static TOP = -1;
  static MIDDLE = 0;
  static BOTTOM = 1;
  static LEFT = -1;
  static CENTER = 0;
  static RIGHT = 1;

  constructor(game) {
    this.game = game;
    this.plot = "--Plot Line--";
    this.inst = "--Instruction Line--";
    this.speed = 500;
    this.timerOn = false;
    this.doStatic = true;
    this._timeout = null;
    this._nextAt = 0;
  }

  // Overridden by levels.
  async loadData() {}
  drawStatic(g) {}
  drawNormal(g) {}
  cleanUp() { this.stop(); }
  keyDown(key) {}
  keyUp(key) {}
  mouseDown(x, y) {}
  mouseUp(x, y) {}
  mouseDrag(x, y) {}
  mouseMove(x, y) {}
  mouseExit() {}

  start() {
    this._clearTimer();
    this.timerOn = true;
    this._nextAt = performance.now();
    this._tick();
  }

  stop() {
    this.timerOn = false;
    this._clearTimer();
  }

  _clearTimer() {
    if (this._timeout !== null) {
      clearTimeout(this._timeout);
      this._timeout = null;
    }
  }

  _tick() {
    if (!this.timerOn || this.game.curLevel !== this) return;
    const g = this.game.g;
    if (this.doStatic) {
      this.drawStatic(this.game.statG);
      this.doStatic = false;
    }
    g.drawImage(this.game.statCanvas, 0, 0);
    this.drawNormal(g);

    this._nextAt += this.speed;
    const now = performance.now();
    if (this._nextAt < now - 2 * this.speed) this._nextAt = now;  // don't spiral after tab throttling
    this._timeout = setTimeout(() => this._tick(), Math.max(0, this._nextAt - now));
  }

  // Port of klevel.drawText. c is [r,g,b].
  drawText(g, s, p, ha, va, c, three_d) {
    const m = g.measureText(s);
    const tw = m.width;
    const tha = m.fontBoundingBoxAscent || m.actualBoundingBoxAscent || 10;
    let drx = 0, dry = 0;

    switch (ha) {
      case KLevel.LEFT:   drx = p.x - tw; break;
      case KLevel.CENTER: drx = p.x - tw / 2; break;
      case KLevel.RIGHT:  drx = p.x; break;
    }
    switch (va) {
      case KLevel.TOP:    dry = p.y; break;
      case KLevel.MIDDLE: dry = p.y + tha / 4; break;
      case KLevel.BOTTOM: dry = p.y + tha; break;
    }
    drx = Math.floor(drx);
    dry = Math.floor(dry);
    if (three_d) {
      g.fillStyle = changeColor(c, 1);
      g.fillText(s, drx - 1, dry);
      g.fillStyle = changeColor(c, -1);
      g.fillText(s, drx + 1, dry);
    }
    g.fillStyle = changeColor(c, 0);
    g.fillText(s, drx, dry);
  }

  drawAlignImage(g, i, x, y, ha, va, factor = 1) {
    const w = Math.floor(i.width * factor);
    const h = Math.floor(i.height * factor);
    let drx = 0, dry = 0;

    switch (ha) {
      case KLevel.LEFT:   drx = x; break;
      case KLevel.CENTER: drx = x - Math.floor(w / 2); break;
      case KLevel.RIGHT:  drx = x - w; break;
    }
    switch (va) {
      case KLevel.TOP:    dry = y - h; break;
      case KLevel.MIDDLE: dry = y - Math.floor(h / 2); break;
      case KLevel.BOTTOM: dry = y; break;
    }
    if (factor !== 1) g.drawImage(i, drx, dry, w, h);
    else g.drawImage(i, drx, dry);
  }

  // Port of klevel.displayText with disable_dim=1 (the mode the original
  // HTML used): diagonal scanlines, gray gradient banner, yellow text.
  displayText(g, s) {
    g.strokeStyle = "#000";
    g.lineWidth = 1;
    g.beginPath();
    for (let i = 0; i < 800; i += 2) {
      g.moveTo(i, 0);
      g.lineTo(0, i);
    }
    g.stroke();

    for (let i = 0; i < 200; i++) {
      const v = 200 - i;
      g.fillStyle = `rgb(${v},${v},${v})`;
      g.fillRect(i * 2, 50, 2, 50);
    }
    // draw3DRect with current color (100,100,100): brighter top/left, darker bottom/right.
    g.fillStyle = "rgb(142,142,142)";
    g.fillRect(0, 50, 401, 1);
    g.fillRect(0, 50, 1, 51);
    g.fillStyle = "rgb(70,70,70)";
    g.fillRect(0, 100, 401, 1);
    g.fillRect(400, 50, 1, 51);

    g.font = "22px 'Times New Roman', serif";
    this.drawText(g, s, { x: 200, y: 75 }, KLevel.CENTER, KLevel.MIDDLE, [255, 255, 0], true);
  }

  nextLevel() { this.game.nextLevel(); }
  enableNextLevel() { this.game.enableNextLevel(); }
  changeScore(n) { this.game.changeScore(n); }
}
