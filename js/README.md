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
  diagonal-scanline effect behind message banners, the diamond's
  accidental-but-correct placement logic in the matching game, and the 1997
  German grammar (including the title).

Keyboard: arrow keys (plane, train, drive, darts). Mouse/touch also work where
the original supported them.
