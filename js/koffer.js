"use strict";

// Port of koffer.java (the applet shell / level manager).

const SCENE_ORDER = ["intro", "plane", "suitcase", "train", "socks",
                     "drive", "darts", "end", "whack"];

const LEVEL_CLASSES = {
  intro:    IntroLevel,
  plane:    PlaneLevel,
  suitcase: SuitcaseLevel,
  train:    TrainLevel,
  socks:    SocksLevel,
  drive:    DriveLevel,
  darts:    DartsLevel,
  end:      EndLevel,
  whack:    WhackLevel,
};

class Game {
  constructor() {
    this.canvas = document.getElementById("game");
    this.g = this.canvas.getContext("2d");
    this.statCanvas = document.createElement("canvas");
    this.statCanvas.width = 400;
    this.statCanvas.height = 300;
    this.statG = this.statCanvas.getContext("2d");

    this.titleEl = document.getElementById("title");
    this.instEl = document.getElementById("inst");
    this.scoreEl = document.getElementById("score");
    this.restartBtn = document.getElementById("restart");
    this.nextBtn = document.getElementById("next");
    this.selectEl = document.getElementById("whichlev");

    this.score = 0;
    this.level = 0;
    this.ingame = true;
    this.curLevel = null;

    for (const name of SCENE_ORDER) {
      const opt = document.createElement("option");
      opt.value = name;
      opt.textContent = name;
      this.selectEl.appendChild(opt);
    }

    this.restartBtn.addEventListener("click", () => {
      this.ingame = true;
      this.nextBtn.disabled = false;
      this.score = 0;
      this.updateScore();
      this.setLevel(0);
    });

    this.nextBtn.addEventListener("click", () => this.nextLevel());

    this.selectEl.addEventListener("change", () => {
      const name = this.selectEl.value;
      if (name !== "whack") {
        this.ingame = false;
        this.nextBtn.disabled = true;
        this.score = 0;
        this.updateScore();
        this.level = SCENE_ORDER.indexOf(name);
        this.loadLevel(name);
      }
      this.canvas.focus();
    });

    this._wireInput();
    const want = new URLSearchParams(location.search).get("level");
    this.setLevel(Math.max(0, SCENE_ORDER.indexOf(want)));
  }

  _canvasPos(e) {
    const r = this.canvas.getBoundingClientRect();
    return {
      x: Math.floor((e.clientX - r.left) * 400 / r.width),
      y: Math.floor((e.clientY - r.top) * 300 / r.height),
    };
  }

  _wireInput() {
    const keyName = (e) => ({
      ArrowUp: "up", ArrowDown: "down", ArrowLeft: "left", ArrowRight: "right",
    }[e.key] || e.key);

    window.addEventListener("keydown", (e) => {
      if (e.key.startsWith("Arrow")) e.preventDefault();
      if (e.repeat || !this.curLevel) return;
      this.curLevel.keyDown(keyName(e));
    });
    window.addEventListener("keyup", (e) => {
      if (this.curLevel) this.curLevel.keyUp(keyName(e));
    });

    const c = this.canvas;
    c.addEventListener("pointerdown", (e) => {
      c.setPointerCapture(e.pointerId);
      c.focus();
      const p = this._canvasPos(e);
      if (this.curLevel) this.curLevel.mouseDown(p.x, p.y);
    });
    c.addEventListener("pointermove", (e) => {
      if (!this.curLevel) return;
      const p = this._canvasPos(e);
      if (e.buttons & 1) this.curLevel.mouseDrag(p.x, p.y);
      else this.curLevel.mouseMove(p.x, p.y);
    });
    c.addEventListener("pointerup", (e) => {
      const p = this._canvasPos(e);
      if (this.curLevel) this.curLevel.mouseUp(p.x, p.y);
    });
    c.addEventListener("pointerleave", () => {
      if (this.curLevel) this.curLevel.mouseExit();
    });
  }

  async loadLevel(name) {
    this.selectEl.value = name;
    if (this.curLevel) {
      this.curLevel.cleanUp();
      this.curLevel = null;
    }

    const lev = new LEVEL_CLASSES[name](this);
    this.titleEl.textContent = lev.plot;
    this.instEl.textContent = lev.inst;
    this.nextBtn.disabled = true;

    const g = this.g;
    g.fillStyle = "#000";
    g.fillRect(0, 0, 400, 300);
    g.font = "bold 20px 'Times New Roman', serif";
    lev.drawText(g, "Loading Level, please wait.", { x: 200, y: 100 },
                 KLevel.CENTER, KLevel.TOP, [0, 0, 180], true);

    this.curLevel = lev;
    try {
      await lev.loadData();
    } catch (err) {
      console.error("Error loading level: " + name, err);
    }
  }

  setLevel(lev) {
    this.level = lev;
    this.loadLevel(SCENE_ORDER[this.level]);
  }

  nextLevel() {
    if (this.level + 1 >= SCENE_ORDER.length) return;
    this.setLevel(this.level + 1);
    this.canvas.focus();
  }

  updateScore() {
    this.scoreEl.textContent = this.ingame ? "Score: " + this.score : "Not in Game!";
  }

  changeScore(change) {
    this.score += change;
    this.updateScore();
  }

  enableNextLevel() {
    if (this.ingame) this.nextBtn.disabled = false;
  }
}

function fitScale() {
  const s = Math.min(2, window.innerWidth / 410, window.innerHeight / 375);
  document.getElementById("scaler").style.transform = `scale(${s})`;
}
window.addEventListener("resize", fitScale);
fitScale();

window.game = new Game();
