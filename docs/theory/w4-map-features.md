# W4: map features of the `goal` and `teammade` maps, with the book's gains

Every visible map of the two pools (the sealed `holdout` is never read): its static features from the engine's own view
of the map file (`bcplay.MapInfo` via `tools/bcgames/mapclass.py`), joined per map-seat with clean main's win rate and
the book's gain. `D:` = `~/battlecode26-data/scratch/w4/`; machine-readable: `D:mapfeat.json` (features, per seat),
`D:mapjoin.json` (rates and gains), `D:bookfam.json` (book-final's entries by family per map-seat). Tools:
`D:mapfeat.py`, `D:mapjoin.py`, `D:famshare.py`, `D:featpred.py`, `D:featmd.py` (this table).

**Data.** Goal: run `1121-095049-ab-6e6d` (clean main 501bde1d against book-final 11a7f247, both against awubot, the
same 3,000 cells, seed base 26001): clean main **0.842** [0.816, 0.867], book-final 0.916, **clean − book −0.074
[−0.097, −0.053]** (bootstrap over maps); 32 games a map-seat a side. Teammade: `1121-034517-ab-d1fc` (clean main, seed
base 20030, 580 cells: 0.818 [0.765, 0.869], about 23 games a map-seat). "Seat" is our seat. A per-map-seat rate on
32 games has a standard error of about 0.06, a paired gain about 0.08: read single cells as noisy.

## 1. What the book carried, by family

A map-seat's gain (book-final − clean main, same cells) is split equally among the families of its entries
(`D:famshare.py`, `D:famshare.txt`).

| family (book switches) | map-seats | gain on its map-seats | share of the gap |
|---|---|---|---|
| second king (`CM_KING2_ROUND`, `KING2_SITE`, `MINES_KING2`) | 26 | +0.234 | 31% (+2.3 pp) |
| economy (`SPAWN_CAP`, `SPAWN_CAP_SMALL`, `SPAWN_RICH`, `PM_SIEGE`, `EXPLORE_P`, `DIRT_ROUND`, `IDLE_GOHOME`, `PM_COOP_HOLD`) | 24 | +0.219 | 22% (+1.6 pp) |
| first king's site (`KING1_SITE`, `CM_KING1_SITE`, `PM_SKIP_FAILED`) | 16 | +0.184 | 15% (+1.1 pp) |
| cats (`CAT_LANE`, `CM_LANE_MEMORY`, `CM_LANE_FLEE`, `CM_CAT_HUNT_START`, `CAT_DEF_DSQ`) | 17 | +0.125 | 12% (+0.9 pp) |
| routes (`KING_HAND`, `KING_PATH`, `SG_LOOK_RIGHT`) | 9 | +0.251 | 11% (+0.8 pp) |
| knowledge (`PM_SYM`, `CM_MINE_BOOK`, `AT_MAP`) | 10 | +0.245 | 10% (+0.7 pp) |
| map-seats without entries | 112 | −0.001 | 0 (the book's identity costs nothing measurable) |

## 2. Which observable features predict where the book helped

Per goal map-seat, the book's gain and clean main's rate by feature bin; each feature scored by a leave-one-map-out
prediction (bin means fitted on the other maps; `D:featpred.py`, every bin in `D:featpred.txt`).

- **No single feature predicts the gain well**: leave-one-map-out r ≤ +0.18 for every feature (cat clock +0.18, map
  classes +0.08, mine density +0.07, mines +0.06, symmetry +0.06, nearest mine +0.06; the rest ≤ +0.05). The book's
  gains sit on particular maps, as CM4 found for sites (docs/theory/cm4-king-site.md §3). A rule keyed on one feature
  class captures at most the class's mean gain, not the book's.
- **Bins where the book gained most** (each ≥ 5 goal maps; the means are carried by a few maps of each):
  - **a cat reaches a king's 3×3 by r200** (cat clock ≤ 200; 7 maps: directconfrontation, externalsustenance,
    groveisbeingdesecrated, RUN, sunandmoon, t_minus_five_minutes, tminusten): book **+0.212**, clean **0.707**;
    every other map +0.062, 0.853. The clearest observable class (cats seen near our start early).
  - **≥ 15 mines** (12 maps): book +0.153, clean 0.783; carried by TheHeist (+0.52 both seats), tminusten,
    groveisbeingdesecrated, externalsustenance, longwayaround; sieveofriches, safelycontained, superioristhmus,
    seepastyourgreed gain ≤ +0.03.
  - **≥ 5 cats** (7 maps): +0.145, clean 0.812.
  - **HORIZONTAL** symmetry (20 maps): +0.117, clean 0.827; VERTICAL (27) +0.086, 0.813; ROTATIONAL (48) +0.050, 0.864.
  - **seat B on VERTICAL maps**: clean **0.773** (the lowest seat-symmetry class: MS's king-hand finding), book +0.094;
    seat A there 0.853.
  - the **second-acting seat** (our king has the higher id): book +0.086 against +0.063, clean 0.831 against 0.853.
  - nearest mine 13–20 king steps (26 maps): +0.114, clean 0.795; mine density ≥ 8 per 1,000 tiles (18 maps): +0.109,
    0.821; walls 5–12% (33 maps): +0.099, 0.826; ≥ 3 mines tied on the symmetry line (21 maps): +0.099, 0.821.
  - least: mine density 3–5 (18 maps) +0.020; nearest mine > 20 steps (14 maps) +0.037; 1–2 tied mines +0.048.
- **Clean main's own rate** is lowest on early-cat maps (0.707; CHOKE+CATPATH 0.667), seat B VERTICAL (0.773), ≥ 15
  mines (0.783) and nearest mine 13–20 steps (0.795).

## 3. The table

Columns: `W×H`; `size` S ≤ 900 tiles (the bot's small-map line), M ≤ 1,900, L above; `sym` ROT, HOR (the engine's
HORIZONTAL, y flipped), VER (x flipped); `mines` (cheese mine tiles) and `/1k` per 1,000 tiles; `wall%`, `dirt%`;
`cats`; `cat clock`: earliest round a cat walking its waypoints straight touches a king's 3×3 (−1 never);
`king cheb`, `king walk`: Chebyshev and walking steps (walls block, dirt one step) king to king; `path`: a rat's
cheapest path between the spawn rings, dirt 3.5 (−1 none); `cut`: fewest tiles separating the kings' 5×5 boxes (capped
at 5); `near mine`: walking steps from our king to the nearest mine; `mines ≤15`: mines within 15 steps (both seats
alike: the maps are symmetric); `tied`: mines whose walking distances to the two kings differ by ≤ 2; `classes`: T4's
(FACEOFF, NOCONTACT, CHOKE, CATPATH, MINEPOOR, DIRT, OPEN). `clean A/B`: clean main's win rate from that seat; `book
A/B`: book-final − clean main on the same cells; `n A/B`: clean main's games; `book fams`: the families of the book's
entries there. In `D:mapfeat.json` per seat also `acts_first` (our king has the lower id: our rats act first each
round; team A on 81 of 95 goal maps), `king_x_frac`, `king_y_frac`.

| map | pool | W×H | size | sym | mines | /1k | wall% | dirt% | cats | cat clock | king cheb | king walk | path | cut | near mine | mines ≤15 | tied | classes | clean A | clean B | book A | book B | n A | n B | book fams |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 5t4rv4t10n_1337 | offi | 21×21 | S | HOR | 1 | 2.27 | 2.0 | 9.8 | 2 | -1 | 18 | 18 | 16.5 | 5 | 10 | 1 | 1 | MINEPOOR | 1.00 | 0.94 | +0.00 | +0.07 | 31 | 31 |  |
| AceOfSpades | tour | 45×60 | L | VER | 16 | 5.93 | 5.4 | 12.7 | 2 | -1 | 24 | 24 | 22 | 5 | 28 | 0 | 12 | OPEN | 0.81 | 0.84 | +0.16 | -0.09 | 32 | 32 | A:king1 |
| arrows | offi | 30×30 | S | ROT | 4 | 4.44 | 14.9 | 0.0 | 2 | -1 | 25 | 29 | 25 | 5 | 8 | 1 | 0 | MINEPOOR | 1.00 | 1.00 | +0.00 | +0.00 | 31 | 31 |  |
| asabovesobelow | tour | 59×40 | L | VER | 13 | 5.51 | 7.4 | 37.7 | 2 | -1 | 52 | 52 | 48 | 5 | 26 | 0 | 3 | DIRT | 0.91 | 0.56 | +0.09 | +0.31 | 32 | 32 | A:king1 B:king2+routes |
| attack_on_cat | tour | 50×50 | L | ROT | 12 | 4.8 | 14.3 | 11.8 | 2 | 306 | 43 | 44 | 91.0 | 5 | 10 | 2 | 0 | CATPATH | 0.97 | 0.94 | +0.00 | +0.00 | 31 | 31 |  |
| averyfineline | offi | 20×20 | S | ROT | 2 | 5.0 | 0.0 | 19.0 | 2 | -1 | 15 | 15 | 16.0 | 5 | 5 | 2 | 0 | MINEPOOR | 0.97 | 0.94 | +0.00 | -0.03 | 32 | 32 |  |
| averystrangespace | offi | 60×60 | L | ROT | 6 | 1.67 | 19.8 | 25.8 | 2 | -1 | 53 | 101 | 252.0 | 5 | 7 | 1 | 0 | DIRT | 0.97 | 0.97 | +0.00 | +0.00 | 32 | 32 |  |
| bed | tour | 20×20 | S | VER | 4 | 10.0 | 15.0 | 12.0 | 2 | -1 | 17 | 17 | 13 | 5 | 8 | 4 | 2 | MINEPOOR | 0.87 | 0.74 | +0.13 | +0.13 | 31 | 31 | A:cats B:cats |
| canyoudig | offi | 20×20 | S | ROT | 2 | 5.0 | 0.0 | 30.0 | 2 | -1 | 15 | 15 | 18.5 | 5 | 15 | 2 | 2 | MINEPOOR DIRT | 0.94 | 0.97 | -0.03 | -0.12 | 32 | 32 |  |
| cheddarchokehold | tour | 31×31 | M | HOR | 8 | 8.32 | 19.9 | 13.1 | 2 | -1 | 28 | 36 | 32 | 2 | 18 | 0 | 4 | CHOKE | 0.81 | 0.38 | +0.12 | +0.53 | 32 | 32 | A:king2 B:king2+routes |
| cheesebottles | offi | 59×20 | M | ROT | 6 | 5.08 | 18.1 | 0.0 | 2 | -1 | 54 | 61 | 57 | 2 | 23 | 0 | 0 | CHOKE | 0.88 | 0.97 | +0.00 | +0.00 | 32 | 32 | A:cats B:cats |
| cheesefarm | offi | 30×30 | S | HOR | 10 | 11.11 | 8.0 | 20.9 | 2 | -1 | 19 | 19 | 22.5 | 5 | 5 | 3 | 2 | DIRT | 0.94 | 0.94 | +0.00 | -0.07 | 31 | 31 |  |
| cheeseguardians | offi | 60×60 | L | HOR | 18 | 5.0 | 1.2 | 7.4 | 10 | -1 | 51 | 51 | 47 | 5 | 14 | 1 | 10 | OPEN | 0.91 | 0.94 | +0.09 | +0.06 | 32 | 32 | A:economy B:economy |
| cheeseysquare | tour | 40×40 | M | ROT | 8 | 5.0 | 12.8 | 8.4 | 2 | -1 | 35 | 59 | 62.5 | 5 | 18 | 0 | 6 | OPEN | 0.84 | 0.84 | +0.03 | +0.03 | 32 | 32 |  |
| closeup | offi | 30×30 | S | ROT | 8 | 8.89 | 13.8 | 17.3 | 2 | -1 | 27 | 31 | 40.5 | 5 | 9 | 2 | 4 | OPEN | 0.56 | 0.84 | +0.06 | +0.03 | 32 | 32 | B:cats |
| constrictinglattice | tour | 30×60 | M | HOR | 8 | 4.44 | 15.6 | 30.8 | 2 | -1 | 19 | 19 | 15 | 5 | 6 | 5 | 0 | DIRT | 0.88 | 0.78 | -0.06 | -0.03 | 32 | 32 |  |
| corridorofdoomanddespair | offi | 20×60 | M | HOR | 2 | 1.67 | 4.2 | 0.3 | 2 | -1 | 51 | 51 | 47 | 5 | 11 | 1 | 0 | MINEPOOR | 1.00 | 1.00 | +0.00 | +0.00 | 32 | 32 |  |
| dangerouscrossing | tour | 50×30 | M | HOR | 6 | 4.0 | 19.5 | 7.5 | 4 | -1 | 23 | 23 | 29.0 | 5 | 11 | 1 | 4 | OPEN | 1.00 | 0.97 | -0.03 | -0.07 | 31 | 31 |  |
| DefaultLarge | offi | 60×60 | L | ROT | 8 | 2.22 | 2.8 | 9.7 | 4 | -1 | 29 | 29 | 25 | 5 | 9 | 3 | 0 | OPEN | 0.94 | 0.97 | +0.00 | -0.03 | 32 | 32 |  |
| DefaultMedium | offi | 45×45 | L | ROT | 6 | 2.96 | 3.4 | 6.5 | 2 | -1 | 36 | 36 | 32 | 5 | 14 | 1 | 2 | OPEN | 0.75 | 0.78 | +0.00 | +0.03 | 32 | 32 |  |
| DefaultSmall | offi | 30×30 | S | ROT | 4 | 4.44 | 2.4 | 8.9 | 2 | -1 | 21 | 21 | 17 | 5 | 6 | 4 | 2 | MINEPOOR | 0.78 | 0.66 | -0.03 | +0.12 | 32 | 32 |  |
| diamond | tour | 40×30 | M | VER | 8 | 6.67 | 17.2 | 19.0 | 2 | -1 | 37 | 55 | 61.0 | 2 | 19 | 0 | 2 | CHOKE | 0.90 | 0.58 | +0.03 | +0.07 | 31 | 31 | B:king1 |
| directconfrontation | tour | 30×30 | S | VER | 8 | 8.89 | 5.8 | 2.9 | 4 | 80 | 13 | 13 | 14.0 | 5 | 6 | 4 | 0 | CATPATH | 0.84 | 0.84 | +0.00 | +0.03 | 32 | 32 |  |
| dirtfulcat | offi | 30×30 | S | VER | 6 | 6.67 | 0.9 | 7.3 | 2 | -1 | 15 | 15 | 11 | 5 | 5 | 4 | 0 | FACEOFF | 0.81 | 0.66 | +0.00 | +0.28 | 32 | 32 | A:cats B:cats |
| dirtpassageway | offi | 50×50 | L | VER | 6 | 2.4 | 1.9 | 18.0 | 4 | -1 | 43 | 43 | 39 | 5 | 14 | 2 | 0 | OPEN | 0.78 | 0.81 | +0.09 | -0.03 | 32 | 32 | A:economy B:economy |
| EscapeTheNight | offi | 45×50 | L | VER | 11 | 4.89 | 8.0 | 21.0 | 4 | -1 | 18 | 58 | 121.0 | 4 | 26 | 0 | 7 | CHOKE DIRT | 1.00 | 0.68 | +0.00 | +0.03 | 31 | 31 |  |
| evileye | offi | 20×20 | S | ROT | 4 | 10.0 | 9.0 | 11.5 | 2 | -1 | 15 | 18 | 20 | 5 | 7 | 4 | 2 | MINEPOOR | 0.97 | 1.00 | +0.03 | +0.00 | 31 | 31 |  |
| Excavation | tour | 39×39 | M | ROT | 3 | 1.97 | 1.1 | 34.6 | 2 | -1 | 30 | 30 | 49 | 5 | 15 | 2 | 1 | MINEPOOR DIRT | 0.87 | 0.97 | -0.07 | -0.03 | 31 | 31 |  |
| externalsustenance | tour | 30×40 | M | ROT | 24 | 20.0 | 9.2 | 2.0 | 2 | 36 | 19 | 19 | 15 | 5 | -1 | 0 | 0 | CATPATH | 0.66 | 0.78 | +0.28 | +0.19 | 32 | 32 | A:knowledge B:king1+knowledge |
| foolsgold | tour | 60×60 | L | HOR | 10 | 2.78 | 1.8 | 9.5 | 2 | -1 | 17 | 33 | 29 | 5 | 18 | 0 | 6 | OPEN | 1.00 | 0.97 | +0.00 | +0.00 | 31 | 31 |  |
| FreeTheCheese | tour | 60×60 | L | HOR | 10 | 2.78 | 7.3 | 13.6 | 8 | -1 | 53 | 53 | 70.0 | 2 | 6 | 1 | 4 | CHOKE | 1.00 | 0.97 | +0.00 | +0.00 | 32 | 32 |  |
| fullofsecrets | tour | 40×45 | M | ROT | 12 | 6.67 | 19.7 | 12.7 | 2 | -1 | 35 | 136 | 152.0 | 1 | 37 | 0 | 0 | CHOKE | 0.90 | 0.97 | -0.03 | -0.03 | 31 | 31 |  |
| gelcatinouscube | tour | 37×43 | M | ROT | 6 | 3.77 | 14.0 | 7.9 | 4 | -1 | 34 | 34 | 37 | 5 | 17 | 0 | 2 | OPEN | 0.77 | 0.87 | +0.19 | +0.07 | 31 | 31 | A:king1 B:king1+king2 |
| gridlock | tour | 20×20 | S | ROT | 4 | 10.0 | 4.0 | 31.5 | 2 | -1 | 15 | 15 | 11 | 5 | 10 | 4 | 4 | FACEOFF MINEPOOR DIRT | 0.88 | 0.94 | -0.06 | -0.06 | 32 | 32 |  |
| groveisbeingdesecrated | tour | 60×60 | L | ROT | 18 | 5.0 | 9.5 | 13.2 | 2 | 98 | 55 | 65 | 66 | 2 | 17 | 0 | 2 | CHOKE CATPATH | 0.72 | 0.56 | +0.28 | +0.25 | 32 | 32 | A:economy+king2 B:economy |
| hatefullattice | offi | 50×41 | L | HOR | 14 | 6.83 | 8.5 | 0.9 | 4 | -1 | 36 | 36 | 32 | 5 | 9 | 3 | 4 | OPEN | 0.77 | 0.87 | +0.16 | +0.10 | 31 | 31 | A:economy |
| hellofromtheotherside | tour | 20×20 | S | HOR | 8 | 20.0 | 6.5 | 16.5 | 2 | -1 | 15 | 17 | 23.0 | 5 | 6 | 6 | 0 | OPEN | 0.78 | 0.94 | +0.00 | +0.00 | 32 | 32 |  |
| Hike | tour | 20×60 | M | HOR | 6 | 5.0 | 12.3 | 4.3 | 2 | -1 | 53 | 85 | 86.0 | 1 | 35 | 0 | 0 | CHOKE | 1.00 | 1.00 | +0.00 | -0.03 | 32 | 32 |  |
| holeinthewall | tour | 50×50 | L | ROT | 6 | 2.4 | 11.8 | 8.2 | 2 | -1 | 43 | 47 | 68.0 | 5 | 24 | 0 | 2 | OPEN | 0.84 | 0.81 | +0.00 | +0.03 | 31 | 31 |  |
| houseofcards | tour | 30×30 | S | VER | 2 | 2.22 | 4.0 | 13.3 | 2 | -1 | 25 | 25 | 21 | 5 | 10 | 1 | 0 | MINEPOOR | 0.62 | 0.22 | +0.00 | +0.56 | 32 | 32 | B:economy+king2 |
| insertmeowname | tour | 60×60 | L | ROT | 10 | 2.78 | 8.1 | 4.7 | 4 | -1 | 55 | 87 | 85 | 3 | 34 | 0 | 0 | CHOKE | 0.81 | 0.88 | -0.09 | -0.03 | 32 | 32 |  |
| isnotgold | tour | 36×60 | L | ROT | 12 | 5.56 | 5.6 | 6.7 | 4 | -1 | 45 | 45 | 41 | 5 | 10 | 1 | 0 | OPEN | 0.97 | 0.84 | -0.03 | +0.13 | 31 | 31 | A:cats B:cats |
| jail | offi | 60×40 | L | HOR | 4 | 1.67 | 6.3 | 11.2 | 2 | -1 | 35 | 41 | 46.0 | 5 | 18 | 0 | 0 | MINEPOOR | 0.69 | 0.78 | +0.31 | +0.19 | 32 | 32 | A:economy+knowledge+routes B:economy+knowledge |
| keepout | offi | 50×50 | L | VER | 12 | 4.8 | 1.4 | 13.2 | 4 | -1 | 39 | 39 | 35 | 5 | 7 | 2 | 2 | OPEN | 1.00 | 0.90 | +0.00 | -0.03 | 31 | 31 |  |
| kingofthehill | tour | 60×60 | L | VER | 6 | 1.67 | 1.2 | 27.1 | 2 | -1 | 53 | 53 | 49 | 5 | 15 | 1 | 0 | DIRT | 0.72 | 0.53 | +0.16 | +0.44 | 32 | 32 | A:economy+king1 B:economy+king1 |
| knifefight | offi | 40×40 | M | VER | 6 | 3.75 | 3.8 | 5.8 | 2 | -1 | 5 | 5 | 1 | 5 | 10 | 4 | 6 | FACEOFF | 0.84 | 1.00 | -0.06 | +0.00 | 32 | 32 |  |
| learn_your_lesson | tour | 40×40 | M | ROT | 10 | 6.25 | 15.4 | 14.4 | 4 | -1 | 37 | 45 | 62.0 | 4 | 17 | 0 | 4 | CHOKE | 1.00 | 1.00 | +0.00 | +0.00 | 31 | 31 |  |
| longwayaround | tour | 60×60 | L | VER | 20 | 5.56 | 12.0 | 12.7 | 2 | -1 | 51 | 77 | 82.0 | 3 | 10 | 1 | 0 | CHOKE | 0.71 | 0.39 | +0.16 | +0.13 | 31 | 31 | A:economy+king2 B:economy |
| Meow | offi | 50×30 | M | HOR | 8 | 5.33 | 10.9 | 1.6 | 2 | -1 | 21 | 21 | 17 | 5 | 6 | 4 | 0 | OPEN | 0.94 | 0.68 | +0.07 | +0.03 | 31 | 31 |  |
| mercifullattice | offi | 41×35 | M | ROT | 10 | 6.97 | 17.1 | 5.4 | 2 | -1 | 30 | 30 | 26 | 5 | 12 | 3 | 0 | OPEN | 1.00 | 0.94 | +0.00 | +0.00 | 31 | 31 |  |
| mincemeat | tour | 31×31 | M | ROT | 3 | 3.12 | 19.4 | 41.0 | 4 | -1 | 28 | 30 | 51.5 | 5 | 16 | 0 | 3 | MINEPOOR DIRT | 0.90 | 0.90 | +0.00 | +0.03 | 31 | 31 |  |
| minimaze | offi | 34×46 | M | ROT | 12 | 7.67 | 19.9 | 12.7 | 2 | -1 | 43 | 69 | 90.0 | 2 | 6 | 1 | 0 | CHOKE | 0.91 | 0.75 | -0.03 | +0.12 | 32 | 32 |  |
| moonlitriverpark | tour | 60×39 | L | ROT | 4 | 1.71 | 6.9 | 19.5 | 2 | -1 | 47 | 47 | 45.5 | 2 | 10 | 1 | 0 | CHOKE MINEPOOR | 0.78 | 0.84 | +0.16 | +0.09 | 32 | 32 | A:knowledge B:knowledge |
| mushroom | tour | 60×40 | L | VER | 8 | 3.33 | 14.8 | 21.6 | 4 | -1 | 27 | 27 | 55.5 | 5 | 15 | 1 | 0 | DIRT | 0.65 | 0.87 | +0.29 | -0.10 | 31 | 31 | A:king2 |
| neitheroptionisgood | tour | 60×60 | L | HOR | 8 | 2.22 | 1.6 | 10.7 | 4 | -1 | 39 | 39 | 35 | 5 | 7 | 2 | 0 | OPEN | 1.00 | 0.97 | +0.00 | +0.03 | 32 | 32 |  |
| Nofreecheese | offi | 60×60 | L | ROT | 6 | 1.67 | 1.3 | 4.4 | 2 | -1 | 49 | 49 | 55.0 | 5 | 30 | 0 | 2 | OPEN | 0.84 | 0.81 | +0.03 | +0.03 | 32 | 32 | A:economy B:economy |
| peaceinourtime | offi | 35×50 | M | VER | 14 | 8.0 | 19.9 | 1.4 | 2 | 252 | 30 | -1 | -1 | 0 | 7 | 1 | 0 | NOCONTACT CATPATH | 0.62 | 0.91 | +0.31 | +0.06 | 32 | 32 | A:economy+king2 |
| pipes | offi | 50×34 | M | VER | 10 | 5.88 | 12.2 | 14.2 | 2 | -1 | 41 | 41 | 62.0 | 2 | 10 | 2 | 0 | CHOKE | 1.00 | 0.97 | +0.00 | +0.03 | 32 | 32 | A:king2 B:king2 |
| pocketsofcheese | tour | 40×45 | M | VER | 6 | 3.33 | 16.0 | 8.0 | 2 | -1 | 29 | 29 | 25 | 5 | 9 | 1 | 2 | OPEN | 0.94 | 0.94 | +0.00 | -0.07 | 31 | 31 |  |
| popthecork | offi | 30×40 | M | VER | 10 | 8.33 | 8.7 | 1.3 | 4 | -1 | 17 | 17 | 13 | 5 | 9 | 1 | 8 | OPEN | 1.00 | 0.31 | +0.00 | +0.59 | 32 | 32 | B:routes |
| QueenOfHearts | tour | 35×45 | M | ROT | 6 | 3.81 | 12.4 | 14.3 | 2 | -1 | 20 | 22 | 25.5 | 5 | 9 | 3 | 0 | OPEN | 1.00 | 0.69 | -0.06 | -0.03 | 32 | 32 | A:king1+routes |
| rift | offi | 60×30 | M | ROT | 12 | 6.67 | 12.4 | 7.9 | 6 | -1 | 51 | 77 | 88.0 | 2 | 8 | 3 | 0 | CHOKE | 0.75 | 0.75 | +0.22 | +0.25 | 32 | 32 | A:cats B:cats |
| rose | tour | 60×32 | L | VER | 8 | 4.17 | 19.9 | 5.0 | 4 | -1 | 27 | 113 | 136.0 | 1 | 16 | 0 | 2 | CHOKE | 1.00 | 1.00 | +0.00 | -0.06 | 32 | 32 |  |
| RUN | tour | 60×60 | L | ROT | 8 | 2.22 | 7.7 | 12.4 | 4 | 174 | 39 | 45 | 80.0 | 1 | 15 | 1 | 0 | CHOKE CATPATH | 0.69 | 0.91 | +0.25 | +0.03 | 32 | 32 | A:cats+routes B:routes |
| safelycontained | offi | 60×60 | L | VER | 16 | 4.44 | 9.1 | 11.7 | 2 | -1 | 37 | 49 | 55.0 | 5 | 7 | 2 | 2 | OPEN | 0.97 | 0.91 | +0.00 | +0.03 | 32 | 32 |  |
| seepastyourgreed | tour | 55×35 | L | ROT | 16 | 8.31 | 14.1 | 5.7 | 4 | -1 | 50 | 250 | 271.0 | 1 | 53 | 0 | 0 | CHOKE | 0.97 | 0.87 | +0.03 | +0.03 | 31 | 31 |  |
| shell | tour | 50×50 | L | ROT | 6 | 2.4 | 12.6 | 25.5 | 4 | -1 | 37 | 37 | 65.0 | 5 | 17 | 0 | 0 | DIRT | 0.81 | 0.72 | -0.06 | +0.28 | 32 | 32 | A:knowledge B:economy+king1+knowledge |
| sieveofriches | tour | 60×20 | M | VER | 30 | 25.0 | 8.7 | 7.0 | 2 | -1 | 55 | 55 | 51 | 5 | 10 | 7 | 0 | OPEN | 1.00 | 1.00 | +0.00 | +0.00 | 31 | 31 |  |
| sittingducks | offi | 50×25 | M | VER | 10 | 8.0 | 10.7 | 14.1 | 2 | -1 | 33 | 33 | 29 | 5 | 7 | 3 | 2 | OPEN | 0.97 | 1.00 | +0.03 | +0.00 | 31 | 31 |  |
| squaresaremodernart | tour | 30×30 | S | ROT | 6 | 6.67 | 4.7 | 14.7 | 4 | -1 | 21 | 21 | 22.0 | 5 | 9 | 2 | 2 | OPEN | 0.87 | 0.58 | +0.07 | +0.16 | 31 | 31 | B:routes |
| stareintotheabyss | tour | 60×60 | L | ROT | 8 | 2.22 | 1.0 | 22.6 | 2 | -1 | 15 | 18 | 20 | 5 | 7 | 4 | 4 | DIRT | 0.87 | 0.90 | +0.03 | -0.13 | 31 | 31 |  |
| starvation | offi | 35×25 | S | VER | 3 | 3.43 | 2.9 | 11.3 | 2 | -1 | 26 | 26 | 27.0 | 5 | 8 | 2 | 1 | MINEPOOR | 0.97 | 1.00 | +0.03 | +0.00 | 32 | 32 |  |
| streetsofnewyork | offi | 60×60 | L | VER | 6 | 1.67 | 0.0 | 27.8 | 4 | -1 | 47 | 47 | 68.0 | 5 | 16 | 0 | 0 | DIRT | 0.91 | 0.91 | -0.03 | -0.03 | 32 | 32 |  |
| stuckinthemiddle | tour | 23×40 | M | ROT | 2 | 2.17 | 14.8 | 6.7 | 2 | -1 | 37 | 37 | 38.0 | 5 | 15 | 1 | 0 | MINEPOOR | 0.94 | 0.87 | +0.00 | +0.03 | 31 | 31 |  |
| sunandmoon | tour | 60×40 | L | HOR | 8 | 3.33 | 9.2 | 7.2 | 20 | 18 | 17 | 17 | 13 | 5 | 5 | 1 | 6 | CATPATH | 0.68 | 0.55 | +0.23 | +0.26 | 31 | 31 | A:cats+king2 B:cats+king2 |
| superioristhmus | tour | 60×60 | L | ROT | 20 | 5.56 | 7.7 | 45.4 | 4 | -1 | 47 | 48 | 45 | 5 | 3 | 5 | 2 | DIRT | 0.97 | 0.97 | +0.03 | +0.03 | 31 | 31 |  |
| t_minus_five_minutes | tour | 20×20 | S | HOR | 4 | 10.0 | 10.0 | 27.0 | 4 | 78 | 17 | -1 | -1 | 0 | 5 | 2 | 0 | NOCONTACT CATPATH MINEPOOR DIRT | 0.81 | 0.74 | +0.10 | +0.19 | 31 | 31 | A:economy B:cats+economy |
| thehallway | tour | 40×40 | M | ROT | 4 | 2.5 | 5.2 | 17.0 | 2 | -1 | 29 | 35 | 34.5 | 5 | 15 | 1 | 2 | MINEPOOR | 0.75 | 0.72 | +0.06 | +0.12 | 32 | 32 | B:routes |
| TheHeist | offi | 60×40 | L | HOR | 18 | 7.5 | 11.3 | 11.0 | 2 | -1 | 31 | 193 | 199.0 | 4 | 18 | 0 | 0 | CHOKE | 0.48 | 0.45 | +0.52 | +0.52 | 31 | 31 | A:economy+knowledge B:economy+knowledge |
| therainbow | tour | 40×40 | M | VER | 4 | 2.5 | 1.5 | 14.6 | 2 | -1 | 31 | 31 | 27 | 5 | 13 | 1 | 0 | MINEPOOR | 0.52 | 0.71 | +0.42 | +0.03 | 31 | 31 | A:king1+king2 B:king2 |
| therearenowalls | tour | 55×55 | L | VER | 6 | 1.98 | 0.0 | 18.7 | 6 | -1 | 48 | 48 | 66.5 | 5 | 9 | 1 | 0 | OPEN | 0.74 | 0.71 | +0.26 | +0.19 | 31 | 31 | A:king1 B:king1 |
| thunderdome | offi | 45×35 | M | ROT | 10 | 6.35 | 3.0 | 11.0 | 2 | -1 | 8 | 8 | 4 | 5 | 12 | 5 | 0 | FACEOFF | 0.88 | 0.72 | +0.12 | +0.28 | 32 | 32 | A:king1 B:king1 |
| tiny | offi | 20×20 | S | VER | 2 | 5.0 | 6.5 | 14.5 | 2 | -1 | 5 | 5 | 3.5 | 5 | 7 | 2 | 2 | FACEOFF MINEPOOR | 0.94 | 0.90 | +0.03 | +0.00 | 31 | 31 |  |
| tminusten | tour | 60×50 | L | HOR | 30 | 10.0 | 4.4 | 14.0 | 4 | 12 | 47 | 47 | 43 | 3 | 7 | 4 | 10 | CHOKE CATPATH | 0.41 | 0.72 | +0.59 | +0.28 | 32 | 32 | A:king2 B:king2 |
| toomuchcheese | offi | 30×30 | S | ROT | 8 | 8.89 | 6.0 | 5.3 | 2 | -1 | 15 | 17 | 22.0 | 5 | 4 | 3 | 2 | OPEN | 0.75 | 0.81 | +0.16 | +0.16 | 32 | 32 | A:king2 B:king2 |
| trapped | offi | 50×50 | L | HOR | 4 | 1.6 | 2.9 | 22.4 | 2 | -1 | 43 | 43 | 69.0 | 5 | 16 | 0 | 2 | MINEPOOR DIRT | 0.77 | 0.65 | +0.23 | +0.29 | 31 | 31 | A:king2 B:king2 |
| UncomfortablyClose | tour | 20×20 | S | ROT | 2 | 5.0 | 9.5 | 2.5 | 2 | -1 | 13 | 13 | 14.0 | 5 | 7 | 1 | 0 | MINEPOOR | 0.94 | 0.88 | +0.06 | +0.00 | 32 | 32 |  |
| uneruesansfin | offi | 60×60 | L | ROT | 4 | 1.11 | 18.9 | 0.0 | 2 | -1 | 51 | 229 | 225 | 3 | 49 | 0 | 0 | CHOKE MINEPOOR | 0.97 | 0.87 | -0.07 | +0.13 | 31 | 31 | A:cats B:cats |
| wallsofparadis | offi | 60×60 | L | ROT | 8 | 2.22 | 5.5 | 4.6 | 2 | -1 | 55 | 79 | 90.0 | 5 | 27 | 0 | 0 | OPEN | 0.88 | 0.84 | +0.03 | +0.06 | 32 | 32 | A:king2 B:king2 |
| warhammer | tour | 30×30 | S | ROT | 6 | 6.67 | 7.6 | 7.3 | 2 | -1 | 27 | 37 | 55.5 | 2 | 19 | 0 | 4 | CHOKE | 0.69 | 0.88 | +0.31 | +0.12 | 32 | 32 | A:economy+king2 B:economy |
| whatsthecatdoin | offi | 40×40 | M | ROT | 4 | 2.5 | 13.9 | 37.1 | 2 | -1 | 33 | 53 | 49 | 4 | 21 | 0 | 0 | CHOKE MINEPOOR DIRT | 0.97 | 0.91 | +0.00 | +0.06 | 32 | 32 |  |
| where_are_you | tour | 60×60 | L | ROT | 16 | 4.44 | 19.8 | 14.7 | 6 | -1 | 55 | 84 | 136.0 | 5 | 5 | 1 | 2 | OPEN | 0.87 | 0.90 | +0.10 | +0.00 | 31 | 31 | A:king1 |
| whereisthecheese | offi | 20×20 | S | ROT | 2 | 5.0 | 11.5 | 33.0 | 2 | -1 | 15 | 19 | 45.0 | 3 | 9 | 2 | 0 | CHOKE MINEPOOR DIRT | 1.00 | 0.97 | +0.00 | +0.00 | 32 | 32 |  |
| worldshardestgame | tour | 40×50 | L | ROT | 12 | 6.0 | 15.4 | 11.5 | 8 | -1 | 31 | 71 | 92.0 | 3 | 38 | 0 | 6 | CHOKE | 0.78 | 0.81 | +0.22 | +0.16 | 32 | 32 | A:king2 B:king2 |
| ZeroDay | offi | 40×34 | M | ROT | 12 | 8.82 | 3.4 | 12.8 | 2 | -1 | 31 | 31 | 30 | 3 | 9 | 4 | 0 | CHOKE | 1.00 | 1.00 | +0.00 | +0.00 | 32 | 32 |  |
| arrowsCatless | team | 30×30 | S | ROT | 4 | 4.44 | 16.4 | 0.0 | 2 | -1 | 25 | 29 | 25 | 5 | 8 | 1 | 0 | MINEPOOR | 1.00 | 0.96 |  |  | 24 | 24 |  |
| bridges | team | 50×35 | M | HOR | 28 | 16.0 | 7.8 | 9.9 | 2 | -1 | 16 | 60 | 56 | 2 | 27 | 0 | 14 | CHOKE | 0.96 | 1.00 |  |  | 23 | 23 |  |
| bridgesCatless | team | 50×35 | M | HOR | 26 | 14.86 | 8.8 | 10.4 | 2 | -1 | 16 | 60 | 56 | 2 | 27 | 0 | 12 | CHOKE | 0.79 | 0.96 |  |  | 24 | 24 |  |
| checkerboard | team | 35×35 | M | ROT | 13 | 10.61 | 18.9 | 4.4 | 2 | -1 | 30 | 88 | 108.0 | 2 | 30 | 0 | 1 | CHOKE | 0.79 | 0.38 |  |  | 24 | 24 |  |
| cups | team | 35×35 | M | ROT | 9 | 7.35 | 14.7 | 8.3 | 2 | -1 | 26 | 38 | 49.0 | 4 | 17 | 0 | 5 | CHOKE | 0.96 | 0.87 |  |  | 23 | 23 |  |
| DefaultLargeCatless | team | 60×60 | L | ROT | 8 | 2.22 | 3.9 | 9.7 | 2 | -1 | 29 | 29 | 25 | 5 | 9 | 3 | 0 | OPEN | 0.96 | 0.91 |  |  | 23 | 23 |  |
| DefaultMediumCatless | team | 45×45 | L | ROT | 6 | 2.96 | 5.3 | 6.5 | 2 | -1 | 36 | 36 | 32 | 5 | 14 | 1 | 2 | OPEN | 0.65 | 0.78 |  |  | 23 | 23 |  |
| DefaultSmallCatless | team | 30×30 | S | ROT | 4 | 4.44 | 6.9 | 8.9 | 2 | -1 | 21 | 21 | 17 | 5 | 6 | 4 | 2 | MINEPOOR | 0.74 | 0.78 |  |  | 23 | 23 |  |
| far | team | 60×40 | L | HOR | 10 | 4.17 | 13.1 | 4.1 | 2 | -1 | 35 | 169 | 192.0 | 2 | 81 | 0 | 0 | CHOKE | 0.91 | 0.70 |  |  | 23 | 23 |  |
| hidden | team | 39×39 | M | VER | 7 | 4.6 | 13.9 | 4.9 | 2 | -1 | 34 | 48 | 44 | 3 | 24 | 0 | 5 | CHOKE | 0.96 | 0.96 |  |  | 23 | 23 |  |
| maze1 | team | 60×60 | L | ROT | 8 | 2.22 | 16.1 | 5.7 | 2 | -1 | 55 | 105 | 101 | 3 | 99 | 0 | 0 | CHOKE | 0.56 | 0.78 |  |  | 23 | 23 |  |
| maze2 | team | 60×60 | L | ROT | 6 | 1.67 | 15.0 | 0.0 | 2 | -1 | 55 | 163 | 159 | 4 | 82 | 0 | 0 | CHOKE | 0.83 | 0.70 |  |  | 23 | 23 |  |
| maze3 | team | 60×60 | L | ROT | 4 | 1.11 | 15.1 | 0.0 | 2 | -1 | 55 | 213 | 209 | 4 | 91 | 0 | 0 | CHOKE MINEPOOR | 0.83 | 0.91 |  |  | 23 | 23 |  |
| messy | team | 40×40 | M | ROT | 12 | 7.5 | 7.8 | 15.6 | 4 | -1 | 27 | 27 | 25 | 5 | 11 | 3 | 4 | OPEN | 0.87 | 0.91 |  |  | 23 | 23 |  |
| micro1 | team | 30×30 | S | ROT | 6 | 6.67 | 9.8 | 6.9 | 2 | -1 | 25 | 27 | 23 | 5 | 7 | 2 | 0 | OPEN | 0.52 | 0.56 |  |  | 23 | 23 |  |
| micro2 | team | 30×30 | S | ROT | 6 | 6.67 | 8.0 | 1.3 | 2 | -1 | 25 | 26 | 22 | 5 | 7 | 2 | 0 | OPEN | 0.58 | 0.71 |  |  | 24 | 24 |  |
| microtest | team | 45×45 | L | ROT | 0 | 0.0 | 11.8 | 1.1 | 2 | -1 | 26 | 26 | 22 | 3 | -1 | 0 | 0 | CHOKE MINEPOOR | 0.83 | 0.75 |  |  | 24 | 24 |  |
| microtest_wide | team | 45×45 | L | ROT | 0 | 0.0 | 11.4 | 1.1 | 2 | -1 | 26 | 26 | 22 | 5 | -1 | 0 | 0 | MINEPOOR | 0.91 | 0.87 |  |  | 23 | 23 |  |
| mining | team | 60×60 | L | ROT | 18 | 5.0 | 19.3 | 6.8 | 2 | -1 | 37 | -1 | -1 | 0 | 7 | 2 | 0 | NOCONTACT | 0.70 | 0.43 |  |  | 23 | 23 |  |
| pvparena | team | 30×30 | S | ROT | 0 | 0.0 | 15.1 | 3.1 | 2 | -1 | 13 | 13 | 11.5 | 5 | -1 | 0 | 0 | FACEOFF MINEPOOR | 0.96 | 1.00 |  |  | 23 | 23 |  |
| pvparena_mines | team | 30×30 | S | ROT | 6 | 6.67 | 18.4 | 3.1 | 2 | -1 | 13 | 13 | 11.5 | 5 | 5 | 3 | 0 | FACEOFF | 0.65 | 0.83 |  |  | 23 | 23 |  |
| pvparena_mines2 | team | 30×30 | S | ROT | 2 | 2.22 | 11.3 | 3.1 | 2 | -1 | 13 | 13 | 11.5 | 5 | 6 | 2 | 0 | FACEOFF MINEPOOR | 0.96 | 1.00 |  |  | 23 | 23 |  |
| quadrants | team | 60×60 | L | ROT | 38 | 10.56 | 5.6 | 14.3 | 2 | -1 | 51 | 51 | 65.5 | 5 | 16 | 0 | 6 | OPEN | 0.91 | 1.00 |  |  | 23 | 23 |  |
| testpathfinding | team | 40×40 | M | ROT | 4 | 2.5 | 19.6 | 22.9 | 2 | -1 | 33 | -1 | -1 | 0 | 21 | 0 | 0 | NOCONTACT MINEPOOR DIRT | 0.91 | 0.70 |  |  | 23 | 23 |  |
| testpathfinding2 | team | 40×40 | M | ROT | 4 | 2.5 | 19.2 | 22.4 | 2 | -1 | 33 | -1 | -1 | 0 | 21 | 0 | 0 | NOCONTACT MINEPOOR DIRT | 0.91 | 0.83 |  |  | 23 | 23 |  |
