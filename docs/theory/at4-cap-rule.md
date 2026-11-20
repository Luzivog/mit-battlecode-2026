# AT4: can the bot read a map's best army cap?

Package AT4 (wave 6). The book's cap entries (`SPAWN_CAP` 60 on cheeseguardians, hatefullattice A, grove, longwayaround;
30 on dirtpassageway; `SPAWN_CAP_SMALL` on warhammer) play only on their maps. A rule would carry them to maps the
book cannot name if something the bot knows during a game predicts the map's best cap. It does not. Data and scripts:
`~/battlecode26-data/scratch/at/at4/` (`load.py`, `analyze.py`, `split.py`, `cv.py`, `ws3.py`, `k2.py`).

## 1. Data

- Effects (cap − main, paired on the same cells, per map-seat): SG's wide screen, `SPAWN_CAP` 30, 60, 80 on 50 maps
  × 40 cells, seed base 22017 (main `1120-045957-ab-b9a4`; caps `1120-055020-ab-b483`, `1120-054920-ab-1b76`,
  `1120-055006-ab-1298`); AT2's 30 and 60 on 17 maps × 80 cells, 26005 (`1120-024047-ab-4d38`, `1120-031046-ab-e9c7`,
  `1120-030709-ab-3e6b`); held out until the end, SG's WS3, 25, 30, 35, 50, 60 on 17 maps × 100 cells, 22019
  (`1120-070420-ab-3602` and its five variants).
- Features, from a different main run so they share no noise with the effects (the 3,000-cell `goal` run
  `1120-022339-ab-124d`): our cheese delivered, rats and kings at r100, r200, r300 (the result records' series), main's
  win rate; static: tiles, mines, mine density, wall and dirt shares, cats, mines within the king's reach
  (`ms/reach_all.txt`).

## 2. Results

- **The map-seat effects are real and repeat.** Split-half r over seeds within the wide screen: +0.72 for 60/80,
  +0.52 for 30; across screens (other seeds, other mains): wide screen against AT2 +0.78 (60), +0.81 (30); against
  WS3 +0.81 (60), +0.71 (30).
- **What predicts them predicts the harm, not the gain.** Leave one map out, ridge on standardised features, 200 nulls
  with whole maps' features permuted: our rats and kings at r300 predict how much 60/80 loses (out-of-sample r +0.40
  to +0.44; null 95% +0.16 to +0.26): a high cap hurts less where the rich-spawn lines already fire (the top fifth by
  rats at r300: −0.002 on average). Gating 60/80 where the prediction is above 0 gains −0.28 to +0.05 pp of `goal`.
  Where 30 helps is not predicted (r −0.09 to +0.17, inside the nulls).
- **Held out (WS3, new seeds, its 17 maps left out of the fit):** the model's gate gains −0.08 pp (50, 60) and −0.20 to
  −0.41 pp (25–35), against +0.29 and +0.75 pp for the per-map-seat best the book can take.

So the best cap is a per-map fact (repeats at r 0.7–0.8) that nothing the bot sees predicts: it stays in the book.

## 3. The early second king

`CM_KING2_ROUND` 30 without a site, wide screen (`1120-050157-ab-a4d0`): −0.085 a map-seat on average; no static
feature predicts it (mines, our own mines, contested mines within 3 of equidistant, the nearest mine's distance: all
|r| ≤ 0.17; the mean is the same with 0, 2 or 4 contested mines). Where it gains (therainbow, worldshardestgame,
gelcatinouscube B, warhammer A) it needs the map's site; no rule was tested.
