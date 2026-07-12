# Der verloren Koffer — JavaScript port

This directory is an **AI-generated port** of the 1997 Java applet game in the
root of this repository. It was translated from the original Java sources by
Claude (Anthropic's Claude Code) in July 2026, so the game is playable again in
modern browsers — Java applets having been removed from browsers years ago.

## Playing

Open `index.html` via any static web server (or GitHub Pages). For example:

    python3 -m http.server

then visit http://localhost:8000/js/. Append `?level=name` (e.g. `?level=darts`)
to jump straight to a level.

## About the port

The port aims to be pixel- and behavior-faithful to the original applet:

- Each Java class was translated one-to-one into a JavaScript file:
  `klevel.java` → `klevel.js` (the level framework), `koffer.java` →
  `koffer.js` (the applet shell), and one file per level (`intro`, `plane`,
  `suitcase`, `train`, `socks`, `drive`, `darts`, `end`, `whack`), including
  the helper classes (`trapzoid`, `BloodSpot`, `target`, `Dart`, `frauTarget`).
- The AWT `Canvas`/offscreen-`Image` rendering maps to a 400×300 HTML canvas;
  the applet's beveled gray chrome (`bcManager`, `bcButton`, `bcLabel`) is
  recreated in HTML/CSS at the original 410×375 size, scaled 2×.
- The original `.au` (8 kHz µ-law) sounds were converted to `.wav` in
  `assets/`; the GIF artwork is used unmodified.
- Original quirks are preserved deliberately, e.g. the `disable_dim=1`
  diagonal-scanline effect behind message banners, the 1997 German grammar
  (including the title), and the quirks described below.

Keyboard: arrow keys (plane, train, drive, darts). Mouse/touch also work where
the original supported them.

## Quirks

### The diamond placement bug (that cancels itself out)

The matching game (`socks.java`) seeds its 5×3 board like this:

```java
for (int item=0; item<8; item++) {
    for (int howmany=0; howmany<((item==0)?1:2); howmany++) {
        do {
            rx = (int)(Math.random() * 5d);
            ry = (int)(Math.random() * 3d);
        } while (boxID[rx][ry]!=0);   // "!=0 means taken"
        boxID[rx][ry] = item;
    }
}
```

Item 0 is the diamond, and `boxID` starts as all zeros — where 0 *also* means
"empty." So the loop dutifully finds a free cell for the diamond and writes...
`0` into it. A no-op. The diamond's cell is still marked empty, and the later
items happily stomp on it: the diamond placement code is 100% dead code.

It works anyway, by arithmetic luck: the board has 15 cells and items 1–7 are
placed twice each, filling exactly 14. Precisely one cell is left untouched,
still holding 0 — and 0 is the diamond. So every game gets exactly one
uniformly-random diamond, placed not by the placement code but by elimination.
(Had the board been 4×4, there would have been two diamonds.)

The port reproduces this faithfully, dead placement loop and all.

### The Mainzer Dom is, art-historically speaking, Notre-Dame de Paris

The game is set in Mainz: the drive level ends at "der Dom" and the darts
level takes place "am Ostportal." But look closely at `dom.gif` and
`dart_scene.gif`: twin flat-topped Gothic towers, a Gallery of Kings, a
central rose window, three portals, a flèche behind — that is the west facade
of **Notre-Dame de Paris**, deployed about 550 km east of where it stands.
The real Mainz Cathedral is Romanesque red sandstone with an octagonal
crossing tower and looks nothing like it. (The *text* is Mainz-accurate,
though — the Mainzer Dom really is entered through side portals and has an
east choir. The vocabulary did its homework; the pixel artist grabbed the
most famous cathedral reference picture available in 1997.)
