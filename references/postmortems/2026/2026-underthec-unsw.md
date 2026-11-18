# Archived postmortem: UnderTheC (2026)

| Field | Value |
|---|---|
| Title | UnderTheC: UNSW Battlecode 2026 postmortem series |
| Team | UnderTheC |
| Year | 2026 |
| Game | UNSW Battlecode 2026 'dragons' (NOT the MIT game) |
| Placement | UNSW Battlecode 2026 (a different contest and game: 'dragons'); best ladder rank #16 |
| Authors | syeyoung, bu-gye-jeong, equinox, chansolpark7 |
| Source | https://battlecode-underthec.github.io/ |
| Retrieved | 2026-11-18 |
| Format | Several pages/files, concatenated |
| Original file | none (web source) |
| Listed on battlecode.org/past.html | no (found by search) |
| Note | Eight posts concatenated in order. Included for its LLM-agent iteration loop, league/arena and win-probability metrics, not for game strategy. |

> **Archived copy of the authors' work.** Reproduced here unchanged in substance so this repo's agents can grep it; all rights remain with the authors (see Source). The text was machine-converted and lightly cleaned: page headers/footers, page numbers, broken hyphenation and tables of contents removed; figures replaced by `[figure omitted: <caption>]`. It is not a summary. Index and takeaways: `references/postmortems/INDEX.md`; cross-year lessons: `docs/lessons.md`.

---

2026-10-11

# 0\. Overview

A warning, the dragons game, why it reminded us of NYPC, and a map of this postmortem series.

**Warning.** The entire project behind these posts was vibe coded. LLM agents wrote almost all of the code: the bots, the simulators, the league, the arena, the scalers and the scripts. The posts are vibe worded too. An LLM drafted every one of them from our repositories and notes, and a human read and corrected each one before it went up. Where a number appears, it came from a commit, a log or a database; where we didn’t record something, we say so. Dates and times are UTC+9 unless marked Z (UTC).

## The game

We are UnderTheC (syeyoung, bu-gye-jeong, equinox and chansolpark7), and we entered UNSW Battlecode 2026. The game ran on the competition server at game.battlecode.au, with the official toolkit shipped as the `unswbc` pip package.

Each team controls dragons, snake-like chains of segments, on a torus: walk off the right edge and you come back on the left. Boards on the contest maps were 11 to 64 tiles a side. Between tiles there can be kelp, a wall that kills any dragon that steps into it, or a portal, an edge paired with another edge somewhere else on the board that you step through and come out of still heading the same way.

[figure omitted: Off the east edge and back on the west, then through a portal and straight into kelp. Animations from the unswbc docs .]

Some tiles are pearl beds. A bed grows a pearl after a random countdown; a dragon that eats one grows by a segment. A dragon that dies drops a pearl on every second segment of its body, so fights feed whoever cleans up. A dragon dies by stepping into kelp, into its own body, or into another dragon’s body. Two heads meeting kill both.

[figure omitted: A pearl adds a segment, and a dead dragon leaves a pearl on every second segment. Animations from the unswbc docs .]

A turn is one command. `MOVE` takes one or more steps. Since rules 1.2.3, a dragon of length L gets its first ceil(L/4) steps free and pays a segment for each step after that, so a sprint burns length. `SPLIT n` cuts the last n segments off as a new dragon, reversed, so the old tail becomes the new head. A team may have up to 64 dragons alive. Dragons act one at a time in id order within a round, and later movers see what earlier ones did.

[figure omitted: A sprint (north, north and east in one turn), and a length-6 dragon splitting: the child leaves head first from the old tail. Animations from the unswbc docs .]

Every dragon is its own process. The judge forks one per dragon, memory is private, and a child created by a split starts as a brand-new process with no memory of its parent. Each dragon sees only a 7x7 window around its head (it can see through kelp). The only way to talk to teammates is sonar: a 64-bit word sent in a straight line, which is delivered to the first living dragon it reaches. That can be an enemy, or the sender itself. A dragon can ping once in each direction per turn. In the judge’s sandbox each turn gets a budget of 100M wasm CPU points.

[figure omitted: A dragon’s 7x7 vision as it moves across a portal and the border, and sonar stopping at the first body it meets: an enemy in the way hears everything after. Animations from the unswbc docs .]

A match lasts at most 500 rounds. A team with no dragons left loses. The rules changed on 2026-10-01 (unswbc 1.2.3, “Slay Queen”): dragons 0 and 1 became their teams’ queens, and a queen stays the queen through splits because the parent keeps its id. At round 500 the longer queen wins, and a dead queen counts as length 0. Ties fall back to the longest living dragon, then total length. Before that change the longest dragon decided round 500, and every bot we had was built for that.

The full rules, as the official engine implements them, are in [docs/game.md](<https://github.com/Battlecode-UnderTheC/syeyoung-tokens/blob/main/docs/game.md>).

## It looked like NYPC

Our first impression was that we had seen this kind of contest before. [NYPC](<https://new.nypc.co.kr/en/>) is Nexon’s programming competition, and its Master Track gives “AI battle-type problems”: each team writes code that plays a given game, and the bots play other teams’ bots. Battlecode is the same shape. So we started from what we knew about NYPC.

Two things stood out. The NYPC winner had used reinforcement learning, so RL became [our first plan](</2026/10/11/1-rl/>). And NYPC’s rules, which otherwise let contestants use LLMs freely, prohibit “calling LLMs en masse in an automated manner or using multiple LLMs to screen code”. If a competition bothers to ban that, it probably works. We also didn’t want to spend much of our own time on the contest, so after RL we wrote a loop that generated bots with LLM agents and let it run: [mass code generation](</2026/10/11/2-mass-codegen/>).

## The series

  1. This post.
  2. [RL](</2026/10/11/1-rl/>): the first plan, reinforcement learning, and why a scripted bot ended it.
  3. [Mass code generation](</2026/10/11/2-mass-codegen/>): generating many bot variants with LLM agents and selecting, instead of training one bot.
  4. [The arena and vast.ai workers](</2026/10/11/3-arena/>): tokenz-arena, our team arena, and the machines that ran it.
  5. [Metrics engine and probability analysis](</2026/10/11/4-metrics/>): reading a game from its replay before it ends.
  6. [The LLM loop up close](</2026/10/11/5-llm-loop/>): orchestrator, strategists, analysts and workers, and the rules we added as they went wrong.
  7. [What the factory made](</2026/10/11/6-bots/>): the bot lines and which ideas moved the server rating.
  8. [Postmortem and results](</2026/10/11/7-results/>): the ladder, the sprint tournament, and what we’d do differently.

---

2026-10-11

# 1\. RL

Our first plan was reinforcement learning: the model, how we trained it, and why a scripted bot ended it.

Our first plan for the dragons game was reinforcement learning, because the NYPC winner had used it. It lived in [syeyoung-rl](<https://github.com/Battlecode-UnderTheC/syeyoung-rl>) and its game submodule [dragons-env](<https://github.com/Battlecode-UnderTheC/dragons-env>), and it ran from the first commit on 2026-09-22 at 20:09 to the last training commit on 09-27. On 09-28 a scripted bot beat the policy 79-1-0, and that was the end of RL for this project.

The design documents state what the policy was supposed to be. The implementation spec says: “Strategies must emerge from team-outcome RL; no tactical teaching curriculum.” `COMPETITIVE_RL_DESIGN.md` lists the behaviours we hoped would emerge (harvesting circuits, body blocking, selective splitting, cheap head trades, recovering corpse pearls), and calls them “hypotheses to test, not rules to hard-code into the actor”.

## The simulator

The rules lived only in the official wasm engine. Driving it in-process gave about 20k dragon-turns per second on one thread, which is slow for self-play. On 09-24 dragons-env was rewritten as a vectorised C++ port of the engine, checked byte for byte against the unswbc 1.0.0 wasm build. On one 14-core machine, with every thread busy, it stepped 7.7M dragon-turns per second. It could also hand a critic a summary of the true board, which no bot ever sees.

## The model

The policy runs once per dragon per turn. Our C++ layer turned the protocol text into what that dragon remembered (memory is private, and a child created by a split starts with none), then packed it into fixed-size inputs:

  * a 17x17 window of the remembered map around the head: for each tile, when it was last seen, pearls, bed countdowns, who occupies it, the kind of each edge and where its portal leads;
  * an 8x8 coarse grid summarising the whole remembered board: per cell, how much was seen, pearls, beds, allies, enemies, kelp and portals;
  * up to 49 visible dragons, the last 64 sonar words received, a few status values and the previous command.

[figure omitted: One decision. Each input has its own encoder; the outputs are joined, fused and fed to a GRU that carries the dragon from turn to turn.]

Each input has its own small encoder: convolutions and per-tile MLPs over the window, MLPs with mean and max pooling over sets such as the visible dragons and the inbox. The outputs are joined into 512 numbers and fused to 192. A GRU with a 192-wide state is the dragon’s memory between turns. It lives as long as the dragon does, and a split child starts from zero. The final actor had 675,798 parameters.

The decoder builds a command in steps, each conditioned on the last:

[figure omitted: The decoder. A command is a kind, then a split size or a path of up to 8 steps, then one sonar choice per direction.]

First it picks MOVE or SPLIT. A split scores every legal size. A move is a sequence of up to 8 direction tokens: a small GRU proposes one step at a time, scoring north, east, south, west and stop from the encoded tile in each direction, and walks its own remembered map, portals included, to know where the next step starts. Several tokens make a sprint. Last, each of the four sonar directions picks one of 257 options, silence or one of 256 words. The vocabulary was fixed, and what the words meant was left for training to work out.

The critic was used only in training. It saw the true board as an 8x8 grid, every living dragon with a summary of its actor state, and both teams’ lengths, and predicted the result for one team. It had 325,217 parameters.

This was the third design in a week. The first (09-22) gave each dragon a 42-channel memory map of the whole board, read by a multiscale ConvGRU that passed messages between the two ends of each portal. The second (09-25) used graph message passing. Both were replaced because each decision cost too much, not because they had been shown to play worse.

## Training

Training was PPO with self-play on the four RTX 2080 Ti GPUs of a lab server, poscatgpu2.

[figure omitted: The trainer. JAX only defined the model; one C++ process ran everything else.]

JAX was used only to define the model and export it as compiled programs, one to act and others for gradients. A single C++ program, `dragons_train`, ran them through PJRT. Two collection workers per GPU each stepped hundreds of matches in our engine, batching every waiting decision into one GPU call per step. Finished 500-round matches went to a queue, were replayed to rebuild their observations, and fed the learner, which computed advantages, ran the gradient programs on all four GPUs and stepped Adam on the host. New weights went back to the workers. Half the matches were self-play, with both sides learning; the other half were against a snapshot from a league saved every 20 updates. The maps were freshly generated, 7 to 64 tiles a side, with the 11 contest maps making up a tenth of the pool.

Each update used at least 65,536 decisions: one epoch in four minibatches, a clip of 0.1, a stop when the policy moved more than 0.02 in KL, and Adam at 3e-4. There was no GAE. Every decision of every dragon on a team got the same final return G, minus the critic’s value at that point, plus that dragon’s own shaping rewards discounted by 0.95 per decision. In the last version, G was the result plus half the longest-dragon margin plus a quarter of the total-length margin. The shaping paid +0.05 for a pearl and +0.1 for an enemy that died ramming our body, and a death cost 0.1 plus 0.05 per segment. An earlier version paid 0.3 per ram, and the policy spent its dragons ramming.

## Deploying it

A submitted bot gets 100M wasm CPU points and 48 MB of memory a turn, in a zip of at most 4 MB. The policy was rewritten as plain C++ with only the standard library, doing its arithmetic in half precision where the trained model did; it matched the JAX model’s commands on 1,199 of 1,200 test decisions. The 2.7 MB of weights first shipped as text, but decoding them blew the CPU budget, so they were compiled into the binary. A decision then cost about 23M points at the median. That policy, at update 28193, was our second server submission, v2.

## What it cost

syeyoung-rl has 150 commits across its branches. By day: 10 on 09-22, 18, 12, 43, 47, 15, and 5 on 09-28. Most of it went into making training fit on the GPUs: the 105 commits from 09-25 to 09-27 are mostly throughput and memory fixes. We never recorded how long the deployed run trained.

The README’s “Not done” section lists where this left us: “Any training run long enough to evaluate, ablations or promotion gates.” No RL run was ever evaluated against outside bots.

## The scripted bot

At 00:31 on 09-28, under five hours after the last reward change, a commit added a scripted C++ bot in its own `strategy/` folder. By then RL wasn’t looking promising, and we had decided on a loop of LLM agents instead; this bot was something for the loop to start from. The commit describes it as: “Explore, gather with many small dragons, feed them into leaders late, trade head-to-head when it pays.” It had a board memory (tiles, beds, kelp, portal pairs), breadth-first pathing with dead-end checks, value-per-round targeting and splitting out of traps, and it shared an agent interface with the RL policy so the two could play each other in the simulator.

`strategy/EXPERIMENTS.md` records every run, all against the RL policy. “Pool 40” is the first 40 maps of the RL training pool, played from both sides (80 games); “official” is the 11 contest maps (22 games).

Change | Maps | W-D-L
---|---|---
first version | official | 10-0-12
when trapped, split and walk the child out | official | 18-0-4
sweep baseline (split from length 8) | pool 40 | 69-0-11
split from length 6 | pool 40 | 74-0-6
survivor fleeing turned off | pool 40 | 78-0-2
split from length 4 | official | 22-0-0
split from length 4 | pool 40 | 79-1-0

The first version lost on small maps. The file’s own finding on the remaining losses: they “were small crowded maps (arena, Colosseum) where the RL swarm wins the one-for-one head-to-head attrition. Splitting at 4 wins the count race there; fleeing hurts.” The 79-1-0 configuration was committed at 01:31 on 09-28, an hour after the bot’s first commit. It became strategy-v1 and was submitted as server v3.

By the afternoon of 09-28 the last commit in the repository was a rule that strategy configurations are committed code, “results are recorded by the league runner”. strategy-v1 became the first variant in the league described in the [next stage](</2026/10/11/2-mass-codegen/>), and the league’s design document says in its opening section: “Nothing here depends on RL (abandoned 2026-09-28).”

---

2026-10-11

# 2\. Mass code generation

How we replaced one trained bot with hundreds of LLM-written variants, a local league to rank them, and what that league got wrong.

The idea came from NYPC, whose rules prohibit calling LLMs en masse in an automated way ([post 0](</2026/10/11/0-vibe-coded/>)). Our reading of the ban was short: if a competition bothers to ban it, it probably works, and there is published work behind the approach.

On 09-28 the [reinforcement learning plan](</2026/10/11/1-rl/>) had just lost 79-1-0 to a scripted C++ bot written the same night. That bot became strategy-v1, our third server submission, and the starting point for everything below. We didn’t improve it by hand because we didn’t want to spend much of our own time: a loop could run without us.

## The bet

Instead of training one bot, we would have LLM agents write many bot variants, measure every one of them the same way, and breed from the winners. A human would write the shared layer (engine port, protocol parser, the agent interface) and the measuring tools. Agents would write strategies.

The plan is in [DESIGN.md](<https://github.com/Battlecode-UnderTheC/syeyoung-tokens/blob/main/DESIGN.md>) in syeyoung-tokens. Its first principle was “the league is the only judge”: an agent never reports its own score. Its second was that nothing lives only in a terminal. Every match is a database row, every generation gets a committed report, and there are no command-line tuning knobs.

## The variant contract and the league

A variant was a directory, `variants/<name>/`, holding:

  * the strategy as C++, written against a shared agent interface;
  * `variant.toml` with its name, 0-2 parents, an operator (fresh, mutate, crossover or tweak), an archetype and the author;
  * `NOTES.md`: the idea, what changed from the parents, and what the author expected, written before evaluation.

Its identity was an annotated git tag, `variant/<name>`, which never moved. `scripts/submit.sh` refused an existing name and refused any change outside the variant’s own directory. The same source built two ways: a `.so` plugin loaded by our native simulator and the league, and a flat unswbc bot for the real judge.

The league service (`league/league.py`, 09-28) was standard-library Python with a SQLite database. Each pass it:

  1. ingested new `variant/*` tags;
  2. ran the gates: `check` on the public maps (no crash, no avoidable wall or self death), then at least 75% wins against a random anchor, both seats;
  3. played matches six at a time, every pair on every public and rotating map from both seats;
  4. refit Bradley-Terry ratings (draws as half a win, a weak prior), on an Elo scale with anchor-random fixed at 1000.

The maps came in suites. Public were the official maps. Rotating was 20 generated maps, replaced every 7 days. The holdout was 60 maps from the RL project’s map pool plus 40 generated from a secret seed range. Agents never saw the holdout or any per-map result on it, and it never fed the rating. It was there to pick champions and to catch overfitting.

The whole thing ran on one M1 Pro Mac with 10 cores. The native engine played about a match per second per thread.

## Generations, then a continuous loop

Generation 1 started on 09-28 with strategy-v1 (Elo 1743), the two anchors, and six tasks. The orchestrator, a Claude Code session following [ORCHESTRATOR.md](<https://github.com/Battlecode-UnderTheC/syeyoung-tokens/blob/main/ORCHESTRATOR.md>), wrote one brief per task and launched one worker per brief in its own git worktree. Workers were Opus 5.5, plus Sonnet 5 for a pure parameter tweak. Three started together. The other three joined an hour later, when we lifted the cap of three, since workers mostly wrote code and needed little CPU.

The results came back that evening. counter-swarm-1 rated 1907, gather-split-trade-1 1892 and swarm-hunt-1 1873, all against strategy-v1’s 1740. Each beat strategy-v1 45-0-17 or 44-0-18.

Midway through generation 1, we asked for a much shorter loop, because a generation took too long. From the evening of 09-28 tasks were capped at 20 minutes, six slots ran at once, and a new task started whenever a slot freed up, with no barrier between generations. The orchestrator still numbered tasks by generation, so “g14-9” is task 9 of generation 14. By g25 on 09-29 the gst line alone had reached gather-split-trade-131. The repository took 434 commits on 09-28 and 509 on 09-29, counting the workers’ task branches (378 and 393 reached main).

How the agent loop itself worked, and how it changed, is [post 5](</2026/10/11/5-llm-loop/>).

## The gst line

gather-split-trade-1 (“gst1”) was strategy-v1 plus a look-ahead that simulated bodies and pearl-target sharing between allies. In generation 2, gst4 added a champion that all dragons agreed on over sonar and fed in the endgame. That went 29-11 against its parent and rated about 70 Elo higher. Most of what came later in the line was small additions on that base: cap-aware trap safety (gst27), a portal fix, partial sightings and obituaries for dead champions, a centre pull on small maps, and pricing dead ends by expected loss so short dragons could farm corridors and split out (gst96).

Other lines ran beside it: counter-swarm, swarm-hunt, body-wall, late-surge, giant, territory, portal-farmer. They traded ideas freely. A gst fix was often ported into three other lines within the hour. gst stayed on top. What the lines actually did is in [post 6](</2026/10/11/6-bots/>).

We submitted to the competition server from the league’s top. We approved gst27 as v4 on 09-28 around 20:40. From 09-29 we had standing authority to submit the best bot every four hours:

server version | variant | submitted | evidence at the time
---|---|---|---
v4 | gather-split-trade-27 | 09-28 ~20:40 | judge sandbox 2-1 vs strategy-v1 (v3)
v5 | gather-split-trade-51 | 09-29 02:54 | league #1, judge sandbox 10-1 vs v4
v6 | gather-split-trade-90 | 09-29 06:54 | league #1, sandbox 12-10 vs v5
v7 | gather-split-trade-96 | 09-29 17:57 | league #1, sandbox 5-3 vs v6
v8 | gather-split-trade-131 | 09-30 01:01 | sim 287-233 vs v7, judge sandbox 24-20

The sandbox evidence behind each was a handful of games. The league rating carried the decision, and the last section of this post is about why that was a problem.

## The first server battles

The first server battle went 5-5 against a trading swarm: no sonar, always `SPLIT 2`, 3-5-long dragons lunging 2-4 steps at our long ones. Our dragons spent 8.7% of their moves in idle loops, 75% of those orbiting portals. The second we lost 2-8 to a stronger version of the same shape that crossed portals 1,098 times and farmed portal-only food (709 pearls to our 8 on Trauma).

[figure omitted: The first server battle, on Autarky, rounds 390 to 395 (ours in cyan). Our 21-long champion meets a 5-long enemy head-on, both die, and its length is left on the board as pearls.]

That evening produced a portal fix (gst33 and its ports into the other lines) and a new kind of variant: clones of real opponents, starting with trading-swarm-1. The clones mattered because A/B tests against our own parents couldn’t see these weaknesses at all.

## What the league got wrong

**Whose side we were on.** For about a day we read server replays with the sides swapped. The replay’s `botA`/`botB` fields hold _our_ submission id, and the opponent’s is blank. The “opponent” diving 1-wide columns on Devil was our own bot, in a win. On 09-29 we corrected the record: the real series against the current opponents was 7-18, not 13-7, and trading-swarm-6 was a clone of our own behaviour. g19’s premise was wrong and three of its tasks were dropped. The fix in KNOWLEDGE.md was a rule: identify our team by our own sonar words, never by the name.

**Sim versus judge.** By g25 (09-29 evening) the numbers stopped agreeing. Every variant since v7 that beat it 55-60% in our simulator or the league scored 19-24 out of 44 against it in the judge’s sandbox: gst100, 101, 115, 127, 128 and 131. Even at the same seed, single games differed. Until it was fixed, verdicts moved to multi-seed sandbox duels (`scripts/sbx-duel.sh`), and a tooling task was set to make the simulator reproduce the judge.

The cause was found on 10-01. Apple clang on arm64 compiles with `-ffp-contract=on` by default, so our native plugins fused `a*b+c` into one FMA instruction. The judge runs bots as WebAssembly, which never fuses. Floating-point scores differed in the last bit, ties broke the other way, and games diverged. Before the fix, gst-queen-1 against gst131 matched the judge in 18 of 30 games in one set and 13 of 20 in another. After compiling everything with

    -ffp-contract=off

they matched 30 of 30 and 20 of 20. An earlier theory, shared mutable state in the bots, had been wrong. Every sim number for the score-based lines (gst, search, rollouts) from before the fix carried this noise. That includes the evidence behind v5 to v8, although each of those also had sandbox games.

The league was the only judge by design, and for those days it had been judging a slightly different game. On 10-02 we stopped running it and moved every verdict to [the team arena](</2026/10/11/3-arena/>), which plays only the official judge. By then the local league database held 170,039 matches between 206 variants.

---

2026-10-11

# 3\. The arena and vast.ai workers

tokenz-arena, the team's shared league on the official judge: how its pieces fit together, the life of a submission, the workers it rented on vast.ai, and the C++ judge.

On 10-02 we told the orchestrator to stop running our own league and use the team arena instead: tokenz-arena, built the evening before (first commit 10-01 20:35, 129 commits in all, [on GitHub](<https://github.com/Battlecode-UnderTheC/tokenz-arena>)). From then on, every verdict on every bot came from it. An import agent loaded 209 of our old local variants as drafts on the first day.

We built it because our teammates wanted to work on the bots too, and the local league from [the previous post](</2026/10/11/2-mass-codegen/>) lived on one laptop. That league’s simulator had also disagreed with the official judge, so the arena was built not to have one. Its design doc keeps two lessons from the league: a child against its parent is the cleanest signal, and local wins aren’t server wins.

## The design

The first principle in tokenz-arena’s [DESIGN.md](<https://github.com/Battlecode-UnderTheC/tokenz-arena/blob/main/DESIGN.md>) is “the official engine is the only judge”. Every game is `unswbc run <map> A B --sandbox --seed S` at the toolkit version the competition server runs. There are no fast simulators. The rest follows from wanting agents to use it without help:

  * **One HTTP API.** Upload a bot as a zip, wait for its verdict, read its report, download replays and sources. An agent needs only `curl`. The same endpoints are exposed as MCP tools at `/mcp`, and `/llms.txt` explains the whole API on one page with live numbers. `docs/AGENTS.md` was the brief we handed to worker agents.
  * **A submission is an experiment.** It names its parent and carries a note: the hypothesis and what changed. `/ideas` searches the notes paired with their verdicts, so an agent can check whether its idea already lost before writing it.
  * **No auth.** The arena is only reachable on the team’s Tailscale network, and it asks Tailscale who is calling. Whatever runs on a teammate’s laptop submits as that teammate.

[figure omitted: The arena leaderboard, as the snapshot keeps it. Every submission carries a note with its hypothesis.]

## How it fits together

[figure omitted: The pieces of tokenz-arena. Everything ran as Docker services on one lab server, except the rented workers.]

The arena is a Python (FastAPI) service split into two processes since 10-02. The **engine** is the only writer: it schedules games, runs 20 local judge slots on the lab server, hands games to remote workers, checks new submissions and refits the ratings. The **web** process serves the pages, the API and MCP from a read-only copy of the state that is at most a second old, and forwards anything that writes to the engine.

The scheduler keeps a queue in priority order: draft games first, then placement for newly promoted bots, then duels, then holdout games among the top 4, then continuous games that fill idle slots with the least-played, closest-rated pairs. Within a priority, the owner with the fewest running games goes first, so one teammate’s burst of drafts couldn’t starve the others.

A remote worker asks for games and gets a lease of 120 seconds, renewed by a heartbeat every 30. It sends back the result, the replay and the game’s metrics. A lease that runs out puts the game back in the queue, and a result for an expired lease, or for a game already recorded, is dropped, so no game counts twice.

Ratings are Bradley-Terry over ranked games among current submissions, with a draw counted as half a win, on an Elo-like scale with a reference bot fixed at 1000. At first they were refitted after every game, which made the API take 10 to 20 seconds to answer; from 10-04 they were refitted at most every 15 seconds.

State was plain JSON, one file per submission and per game, written with an atomic rename. That held until 10-09, when the web process was reloading every game file every two minutes and holding two copies of all of them in memory, about 7.6 GB. Games moved into SQLite that day.

## The life of a submission

[figure omitted: From upload to the leaderboard.]

`tokenz submit` zips a folder and uploads it with its parent and note. The engine checks it: a safe zip under 4 MB, no reads of environment variables, a compile with the judge’s own toolchain into the metered wasm the server uses, and one sandboxed game against a reference bot. A failure rejects it with the log.

A draft then plays its parent and the current top 3 on the official, tournament, server and custom suites, from both seats, unranked. The design target was a verdict in about 5 minutes. `beats_parent` is simply more wins than losses against the parent, reported with a 95% interval and the record on server maps alone. Promoting is a deliberate step by the owner or agent: it retires the bot’s previous version and starts placement, first against the parent and top 5, then against every current bot. The top 4 also play the holdout.

The map suites copied the league’s scheme: official maps, tournament maps rebuilt from replays, a server suite with the first two seeds the server used on each map, a weekly rotating set of generated maps, and the hidden holdout. From 10-05 the generated suites also drew on our own map generator (`tools/mapgen`), including “trick” maps built to punish one habit, such as rushing the centre.

The server bridge was the arena’s window on the competition server. Once a minute it imported our new server games as arena games, with the opponent as an external bot `server:<team>`. It also held an auto-submit gate. A candidate had to be #1 on the arena with its rating clearly above the live bot’s, win at least 55% of at least 60 games against it, be no worse on the holdout and server suites, have no errors, and stay under 70M CPU points at the 99th percentile; the gate uploaded at most once every 4 hours. On 10-02 a force-upload button was added that skips it. Both paths were used. The gate itself uploaded queen-guard-7, ring-plus-4, ring-plus-9, ring-sched-8, ring-sealed-2 and ring-shield-3; the rest went up through the button.

## Remote workers

From 10-02 any machine with Docker could join (`docs/WORKERS.md`). The worker image, about 420 MB, held the competition’s toolkit, the C++ judge, the native metrics and Tailscale. On start it joined the team network with a key that could reach only the arena, and every call carried its own worker token. A worker on a different toolkit version was refused, so its results never mixed with the ratings.

A worker ran many games at once, kept a few leased games ready so no slot sat idle, downloaded each bot’s prebuilt wasm instead of compiling it (that took a game from 60 seconds to 10), and cached bots and maps by hash. An error caused by the machine, not the bot, didn’t count as a result: the game went back in the queue until it had failed on three different machines.

## The vast.ai scaler

[figure omitted: The scaler. It rented at most one machine a minute and returned machines it no longer needed.]

The same evening the scaler (`arena/scaler.py`, `docs/SCALER.md`) started renting workers on vast.ai. Our notes record why vast.ai: about $2.5-4.5 per 1000 thread-hours, against $15-20 on AWS Spot. Every minute it read the queue, worked out how many judge slots it would take to clear the backlog in about 5 minutes, subtracted the slots we already had, and, if over 200 games were waiting (500 before 10-05), rented one machine. It searched for verified hosts with at least 64 threads, picked the cheapest per slot, and bid 15% over the asking price for an interruptible instance. The container joined Tailscale by itself and started leasing. A machine idle for 10 minutes, outbid, or no longer needed was returned, and a worker that lost contact with the arena for 20 minutes destroyed its own instance.

The caps we approved were $1 an hour, $10 a day, 6 machines and 4 rentals an hour, with a prepaid balance as the last guard. We didn’t record what it spent in total. What the snapshot does record: 264 rented machines played games between 10-02 and 10-09, and they played 182,013 of the arena’s 291,668 games, 62%. The busiest played 9,292 games in about an hour.

Its early bugs, mostly on the morning of 10-05, were the kind an interruptible market invites: two of our instances on one host outbidding each other, the same flaky cheapest host rented and lost four times in 8 minutes, and wasmtime starting a thread pool as large as the host for every game until containers hit their task limit. Each got a rule: one instance per host, a 2-hour skip list, two threads per judge. One fix was deployed to the live arena without approval, and from then arena changes needed explicit permission.

## The C++ judge

`unswbc run --sandbox` spends most of a game in Python glue around the wasm: turn framing, the bots’ system calls, the engine’s host calls. On 10-02 and 10-03 we ported that glue to C++ (`judge/`, `docs/CPPJUDGE.md`), about 2,200 lines, keeping everything that decides a game, the metered bot builds and the engine wasm, exactly as the official judge has them. It produces the same replay byte for byte and used 1.6x to 7x less CPU. Whenever it couldn’t be exact (a Python bot, a missing binary, a toolkit release whose ported files changed) it handed the game to the official judge, and one game in 50 was played by both as a spot check, with the official result counting. It played 225,633 of the arena’s games. All 4,535 spot checks matched.

## Duels and ephemeral bots

From 10-04, workers could try a change before submitting it (`docs/DUELS.md`). An ephemeral bot is a zip checked like a submission but kept off the ladder, deleted after 24 hours. A duel plays any two bots on fresh random seeds (`random:N`), both seats, on any suite except the holdout. The fresh seeds keep a bot from being tuned to the ladder’s fixed ones. The result reports W-D-L with a 95% interval and whether it excludes 50%. Duel games are stored apart, so nothing official can see them.

## The end

The last arena game was played on 10-09 at 05:57Z, when we stopped all workers. 10-09 was the submission day. By then it had played 291,668 games between 680 submissions, plus 629 duels with 30,656 games. Its replays came to about 196 GB zipped.

On 10-11 it became a static site, the [arena snapshot](<https://battlecode-underthec.github.io/arena/>) (repo [Battlecode-UnderTheC/arena](<https://github.com/Battlecode-UnderTheC/arena>)). We published it so that this series could point at the games it describes. The pages are the arena’s own website, with the backend ported into a service worker that reads one SQLite file through HTTP range requests. The replays aren’t stored. A game is deterministic given its map, seed and both bots, so the game page runs the official engine wasm (unswbc 1.2.9) and both bots’ metered builds in a port of the judge’s sandbox, one Web Worker per dragon, refilling 100M CPU points per turn. The toolkit’s replay viewer shows the game as it runs and says whether the result matches the arena’s record.

---

2026-10-11

# 4\. Metrics engine and probability analysis

How the arena turned every replay into per-round metrics and a P(A wins) curve, how well that curve predicted real games, and what it taught us about the top teams.

A dragons game lasts 500 rounds, and a win-loss line throws almost all of it away. By early October our verdicts were 78-game draft runs in [the arena](</2026/10/11/3-arena/>), and 78 games is about ±9 wins: a 41-37 result told us nothing. On 10-02 we added a metrics engine to tokenz-arena (`arena/metrics/`) and an eval bar on top of it. We wanted agents to see how a bot did in the early, mid and end game, not only whether it won: we think the game splits into those phases and that each of them matters. Our own notes later recommended the eval as “a lower-variance signal” than W-L, and that is how it ended up being used: to read a game before it ended, and to find the round where it turned.

## From replay to numbers

Every game the arena played left a `.replay` file. The metrics code decodes it and replays the game round by round in `state.py`. Every 10 rounds, and at the last round, it calls every registered metric once per team with the board state and cached helpers (BFS distances from each team’s heads, running counters for pearls, deaths, kills and splits).

A metric is a decorated Python function in `arena/metrics/plugins/`; dropping a new file in that folder registers it. The one that mattered most was `board_control`: the share of all tiles that this team’s heads reach before the enemy’s (BFS, bodies ignored). The rest were counts (`dragons`, `log_dragons` = log(1 + dragons alive), `total_length`, `longest`, `pearls_eaten`, `deaths`, `kills`, `splits`), the queen (`queen_alive`, `queen_length`, plus `queen_room`, `queen_danger` and `queen_share` for how a bot handles her) and exploration (`tiles_visited`, `tiles_seen`).

The output is one small JSON per game: each metric’s series for both teams, plus how and when each queen died. Changing a metric means bumping its version; a background task then recomputes older games.

Python stayed the reference. On 10-03 we added `native/metrics`, a C++ twin of every plugin, about 50x faster (20-30 ms a game on one core). The arena uses the binary only when it has every registered metric at the same version, and a parity script must report every replay identical; a new metric runs in Python until someone ports it.

## The eval: P(A wins)

The eval is a derived metric: it takes the per-team values at a round and returns the chance that team A wins. It is a logistic model on five differences:

    z(r) = w1(r)·Δlog(1+dragons) + w2(r)·Δboard_control + w3(r)·Δlongest
         + w4(r)·Δqueen_alive   + w5(r)·leader

    P(A wins at round r) = 1 / (1 + e^(-z(r)))

Each Δ is A minus B. `leader` is +1 or -1 for whoever would win if the game ended now under the round-500 rule (queen length, then longest dragon, then total length), 0 if tied. The weights are fitted separately at 12 knots (rounds 10, 20, 50, 100, then every 50 to 450, and 490) and interpolated linearly in between. If a team has no dragons left the answer is 1 or 0, and at round 500 it is the result, not a prediction.

The fitted weights tell the story of a game:

[figure omitted: The fitted weights of eval v2, by round.]

Early on, numbers and space are what count. The queen’s weight grows from almost nothing to the largest term by the end, and the count of dragons stops mattering at all.

The inputs came from an analysis of about 8,500 arena games and 258 real competition games (`docs/METRICS.md`): board control and log dragons predicted best from round 50 to 350 (AUC 0.80-0.84 on real games), the longest dragon decided round 500 in about 95% of games that got there, and kills predicted nothing on their own. Deaths even went slightly _with_ winning: bots that split and expand lose more dragons. The finer queen metrics added nothing on top of whether she was alive.

Version 1 was fitted on arena games only. Version 2 (fitted 2026-10-02 04:40Z) used 1,141 games between the competition’s top teams, 435 of our server games and 3,980 arena games, each set weighted equally. Our bots didn’t play as well as the top players, so a model fitted on our own games had to be refitted against real ones. The top-team games played differently: round 500 was decided by the queen in 25% of them, against about 4% in our arena and server games. With them in the fit, v2 gives the queen about twice v1’s weight from round 200 on.

## How well it predicted

Out of fold, on top-team games, v2 called the winner 70% of the time at round 100, 75% at r300, 81% at r400 and 96% at r490. Version 1 had managed 70, 68, 74 and 89%. On our own server games v2 got 72, 76, 77 and 95%.

Workers reported the mean eval against their parent at r100, r200 and r300 next to their W-D-L, and analysts used the curves to find where losses turned: a loss whose eval stays below 0.5 from round 60 is a different problem from one that sits at 0.8 until a queen dies at round 410.

## The evals service

`arena evals`, a separate process, kept checking it against games it hadn’t seen: finished battles between _other_ teams on the competition server. Every 5 minutes it read the leaderboard, took the last six battles of each of the top 40 teams, downloaded any it hadn’t seen and computed their metrics. A fitted metric carries the timestamp of the newest game its fit saw, and the `/evals` page judges it only on later games.

The page showed accuracy, AUC, log loss and Brier per round against a baseline (whoever leads under the round-500 rule right now), calibration plots, and accuracy over 5-hour to all-time windows, so a change in how the top teams played would show in the short windows first. By the time the arena stopped, the snapshot held 64,090 eval games from 10,947 server battles.

## What it taught us

The biggest lesson came from evals-r13 on 10-05: an analyst took 2,382 eval games from 342 battles involving a top-10 team, plus 214 decoded replays, and compared them with 211 server games of our then-head ring-plus-9.

The top teams did not guard their queen better. Theirs died in 66% of games, ours in 36%. But they won 53% of the games in which their queen died, and we won 23%. What they had was army: at round 300 their total length was 120 to our 81, their board control 0.60 to our 0.51, and their longest dragon 12.7 to our 5.1. We lost 171 dragons to self-inflicted deaths before r300 against their 113, mostly 2-long split children boxed in by our own bodies.

[figure omitted: A top team (cyan) on Trophy, rounds 2 to 98. Its queen travels inside the swarm, and by round 98 it has 27 dragons.]

A server analysis the same day (server-r13, 156 losses) agreed from the other side. In 69 of 75 losses where our queen died head-on, P(win) was already below 0.35 before she died. More queen safety was worth at most about 5 games: the queen deaths were a symptom of a game already lost.

The deep analysis of 10-07 (`docs/analysis-2026-10-07`) used the eval to assign each of 287 server losses one primary cause. 150 of them (52%) were lost in the opening: the eval stayed below 0.5 from round 60 or earlier. Board control at round 60 between 0.2 and 0.3 won 32% of the time; between 0.7 and 0.8, 90%. The same report found our queen died in 207 of 288 losses, 64 of them thrown wins where the enemy queen had died first. Both readings are true; the eval let us say which came first.

[figure omitted: One of those opening losses, on Devil (ours in cyan). The enemy sent every starter to the central beds by round 20; by round 80 it had 33 dragons to our 5.]

## Metrics are not goals

On 10-05 we reminded the orchestrator of Goodhart’s law: a metric must not become the goal. A change can move the number named in its brief without fixing the behaviour.

We had examples on file. evals-r13 said we lost far more split children to our own bodies, so on 10-05 two workers cut splits: ring-churn-1 refused growth splits in crowds and went 38-41; ring-longmid-1 split only at longer lengths and went 25-42. Both shrank the army. The short-lived children were paying for themselves by taking pearls and space first. The churn number was real; treating it as the target was the mistake. ring-sprint-1 (10-06) closed a gap the analyses had measured, raising free multi-step moves from 2.5% to 33% of eligible moves, and its measured server performance didn’t move: its long dragons were killed more often, and the longest at r400 was unchanged. And on 10-06 the orchestrator held back ring-qforage-1 because its early queen death rate had doubled, while it was winning. We corrected that too: the queen’s survival is evidence to weigh, never a cap by itself.

What changed in practice:

  * Briefs named the target behaviour seen in replays; metrics were evidence for it.
  * Workers checked decoded replays of their own draft in the targeted situation, before and after, and named the game ids. “A metric moved but the replays don’t show the intended behaviour” counted as a failure.
  * Before a promotion, an analyst rendered the draft’s games as filmstrips and looked at them. Images earned their place: one Colosseum filmstrip showed our lone queen at length 7 among pearls, contradicting a worker’s numbers-based story that she sat at length 2 with no pearls.

The eval stayed on every game page until the end. It just stopped being the thing we tried to raise.

---

2026-10-11

# 5\. The LLM loop up close

Who did what in our agent loop (orchestrator, analysts, strategists, workers), what a brief looked like, how verdicts were judged, and the rules we added each time it went wrong.

From 09-28 to 10-09 almost all of our bot code was written by Claude Code subagents, coordinated by one long-running Claude Code session that we called the orchestrator. The paper trail is in syeyoung-tokens: 50 generation reports (`league/generations/g*.md`), 545 filled-in briefs, 78 strategist proposal files, and `ORCHESTRATOR.md`, the instruction file a fresh orchestrator session reads before it does anything. This post is about how that loop worked and how it broke.

## The roles

There were four kinds of agent by the end.

  * **The orchestrator** launched the other agents, verified every number they claimed, promoted drafts, kept the generation reports and `league/KNOWLEDGE.md` (735 lines by the end) up to date, and handled submissions. It was not allowed to design strategy.
  * **Analysts** (Opus, read-only) diagnosed server and arena losses from replays and wrote a ranked `REPORT.md` per round. Only they looked at images (rendered filmstrips).
  * **Strategists** (2-3 per round, Opus) read the analysis and `KNOWLEDGE.md` and each proposed ranked single changes, each with its evidence, its base bot and how to tell whether it worked. They wrote no code.
  * **Workers** (Opus 5.5) each made one change to one bot, tested it, submitted it to the arena and reported back.

[figure omitted: One round, from 10-04 on.]

None of this was the original design. On 09-29 the orchestrator was writing every task focus itself, and we pointed out that all the strategy was coming from it. Strategists followed, and the orchestrator was limited to coordinating. On 09-30, when it started digging into a new server battle itself, it was told that analysis wasn’t its job either. The orchestrator’s notes record why: a single view narrows the search and carries its mistakes through whole generations. The swapped-sides misread in [post 2](</2026/10/11/2-mass-codegen/>) was one such mistake: it drove three tasks.

Workers started on a mix of models. On 09-29 we moved them all to Opus 5.5 because the Sonnet and GPT workers often shipped only part of the brief, misreported results against a parent, or shipped a no-op as an improvement. Strategists ran on a mix of models until our routes to the GPT models stopped working on 09-30; from strategist round 11 we made them Opus-only too.

## A brief, a worktree, one change

Every worker started with no context. Its prompt was a template with the blanks filled in, written to a file so it survived restarts. The late template, `league/BRIEF-ARENA.md`, opens like this:

    You make **one** change to one of our bots for UNSW Battlecode "dragons" and get it judged
    by our team arena. You start with no context, so this brief is everything. Read it in full,
    then follow it.
    ...
    - **Base:** {base} (arena id `{base_id}`, line `{line}`)
    - **Change:** {change}
    - **Evidence it answers:** {evidence}
    - **How to tell if it worked:** {success}
    ...
    2. Make the ONE change. Don't sweep parameters or bundle a second idea.

The rest covered light arena duels, submitting with `--parent` set to the bot the code was built on, checking the draft’s own arena games for the targeted weak spot, and a 12-line report. Nearly every rule in it came from a correction.

`scripts/new-task.sh` gave each task its own git worktree and branch, with the base bot copied into `variants/<name>/`. Workers could write only that directory. The orchestrator checked that each diff touched nothing else before merging. Six workers ran at once on one laptop.

One change per task was the point. The cleanest signal we ever had was a child against its parent, and a verdict on two changes at once tells you nothing about either.

## Verdicts

How a change was judged moved through five stages: league Elo with a hidden holdout, then judge-sandbox duels, then round-robins on held-out seeds, then the arena’s `beats_parent` plus the eval P(win) from [the metrics engine](</2026/10/11/4-metrics/>), and finally server per-map rating-adjusted Elo.

In the arena era, a draft played its parent and the top 3 on several map suites, and `beats_parent` was simply true when it won more games against its parent than it lost. Promotion required it. That rule alone was too loose: 78 games is about ±9 wins, so the orchestrator was told to treat 38-40 or 41-37 as a tie and ask for more games. Round 9 showed what happens otherwise: a line can climb by three promotions that are all inside noise.

## How the method changed

  * **g1-g25 (09-28 to 09-29): local league, generations.** Six tasks per generation, each tagged as a mutate, crossover, fresh or tweak operator, rated by a local Bradley-Terry league. It soon became continuous at our request: a very short loop, tasks of about 20 minutes, a free slot refilled the moment a result came in. The gather-split-trade line climbed this way to gst131.
  * **Rounds 1-8 (g26-g33, 09-29 to 10-02): long-running teams.** Six Opus teams (search, swarm, giant, hunter, territory, free), mostly from scratch. We suspected that losing context between tasks limited how far an agent could improve a bot, and long-running teams kept theirs. arena-search-7 topped our round-robin at 62.2% and became v9, then went 0-5 against the server’s #1, having lost the corridor farming gst had built up. The rule that came out of it: build on gst131 and add one radical behaviour; no from-scratch bots.
  * **Round 9 (g34, 10-02 to 10-04): three long-running teams** on the new arena, each owning a line. Their contexts grew long, they drifted, and about 15 verdicts landed inside noise.
  * **Round 10 onward (g35, from 10-04): short fresh workers.** Server and arena losses go to an analyst, the analysis goes to 2-3 strategists, and about six fresh one-change workers build on the line head. ring-plus went 1 → 4 → 7 → 9 → 16 → 19 → 21 this way, and the ladder went from #35 at 1832 on 10-04 to #16 at 2139 on 10-06.
  * **Per-round merges, 10-05 to 10-06.** For a day each round’s winning changes were bundled into one submission, so that each round’s improvements would stack. On 10-06 we reversed it: one change at a time on the head, with ports of other winners as ordinary single-change tasks.
  * **After 10-06: stall.** A deep analysis, 3-4-agent rounds, planner stages and numeric bars; the ladder sat around 2100.
  * **The reset (10-08):** we told the orchestrator to drop every instruction given since 10-06 and work the way it had up to then. `ORCHESTRATOR.md` and the brief template went back to their end-of-Oct-6 text. On 10-09 we stopped all workers, mid-task, with four drafts unfinished.

In parallel, from 10-02 to 10-04, we ran Codex on separate branches, to get more tokens working on the problem. It made disciplined single-change queen tweaks and rejected its own failures honestly; codex-queen-value-1 reached #3 in the arena at 1543.

## Rules from corrections

Each of these is in the memory notes with the date it started. The ones that changed the most:

  * **No map hardcoding** (09-30). A worker’s bot picked its play mode from a table of known map sizes, and the orchestrator had accepted it, asking only for a fallback. We called it what it was, a hardcoded map. From then every brief banned map names, exact sizes and lookup tables, and every new threshold had to name the observed cause it responds to.
  * **Full brief at launch, no steering** (10-05). The orchestrator had been messaging running analysts with its own conclusions (“our queen never splits on Colosseum”), which they then dutifully confirmed. Mid-run messages became limited to new evidence or a rule change from the user, and strategists started only after the analyses finished.
  * **No unrequested builds** (10-05). Asked to watch how games played out, the orchestrator built a text replay watcher, extended it, and rewrote the briefs to require it, none of which had been asked for.
  * **Orchestrate without asking** (10-04). The orchestrator kept asking permission for routine steps such as starting an analysis; managing agents was its job, so it stopped asking.
  * **Line names describe the mechanism** (10-05): ring-contest, not ring-next, and not a name borrowed from the user’s example when the code did something else.
  * **Give an idea 2-3 tries if its target moves** (10-05), because single noisy pass/fail verdicts were throwing away partial successes.

## Failures and fixes

What went wrong | What fixed it
---|---
From-scratch v9 lost behaviours the gst line had | Build on gst131, add one behaviour
Round-9 teams drifted inside noise | Short fresh workers, one change each
The arena overrated ring-plus-4, mostly on its 2-seed server suite | Read verdicts split by suite
A centre rush won on known maps and lost on generated anti-centre maps | Test openings on `tools/mapgen` families before trusting arena gains
The cloud session running 10-06/07 stopped at the weekly usage limit | A “State at stop” section in `HANDOFF.md` for the next session

---

2026-10-11

# 6\. What the factory made

The bot lines we bred between 09-28 and 10-09, what each one did, and which of them moved the server rating.

Over twelve days the agents produced 322 variant directories locally and 680 arena submissions under 258 bot names. This post follows the lines that mattered, in rough order, and keeps two scores that often disagreed: the arena (our judge, our bots) and the competition server (everyone else).

## strategy-v1 and the gst line

**strategy-v1** (09-28) was a hand-written C++ bot from the RL repo. Its ideas: explore, gather pearls with many small dragons, split from length 4, trade head-to-head only with an enemy at least as long, and late in the game feed workers into the leaders. It beat the RL policy 79-1-0 on 40 pool maps and 22-0 on the official maps, and became server v3.

**gather-split-trade (gst)** was the first line the league bred from it, and it ran for about 130 generations. Its early gains were about consolidation: the team agreed on one champion over sonar and fed it late in the game.

Corridor farming was syeyoung’s idea (g20). Short “farmer” dragons claimed corridor bed farms over sonar and stayed small, cycling the beds. gst109 did it heavily (600-1050 farmer-eaten pearls a game) and did not grow the team at all. The real opponent out-ate us 1.22x in corridors and 1.30x in open ground: corridors were no special edge.

What did survive was dead-end pricing. Once the sim used the judge’s pearl seeding, most old “wins” re-tested as coin flips. Only gst131’s change held up (57%): charge every dead end a flat cost of 3.0, dropping the expected-loss model, which halved dead-end entries. gst131 went up as server v8 on 09-30.

## arena-search-7 (v9) and its 0-5

In the local arena rounds (09-29 to 10-02) six long-running Opus teams wrote bots, mostly from scratch. The search team’s **arena-search-7** used a per-dragon lookahead (depth 8) and finished the round-robin #1 at 62.2%, beating v8 184-115 on seeds it had never seen. It became v9 on 09-30.

Its first server battle was 0-5. We had picked that match against the leaderboard #1, so it said more about the #1 than about v9. But the replays showed something we had thrown away. The #1 farmed dead-end hot beds (beds that refill within about 3 rounds) with a tip split: eat up a dead end, split at length 4-5 so the rear half walks out. Hot-bed pearls went 150 to 6 on Autarky. arena-search-7’s room check kept it out of dead ends entirely, so the from-scratch bot had lost the corridor economy gst already had. Overall v9 went 112-0-86 and sat at #72 of 947, about 1767.

The rule that came out of it: build on gst131 and add one radical behaviour, no from-scratch bots.

## gst-giant, gst-search and the 1.2.3 queens

That rule produced two quick server versions on 10-01. **gst-giant-2** was gst131 plus a rollout judge (with a portal fix) from the giant team; it went 40-0-35 as v10 and the rating rose 1767 to 1797. **gst-search-4** (v11) put arena-search-7’s lookahead on gst’s plain late moves. That lookahead gained on all three gst bases it was added to.

Then the game changed. unswbc 1.2.3 (“Slay Queen”, about 18:15 on 10-01) made dragon id 0/1 a queen: at r500 the side with a living queen wins before longest dragon is even checked. Sprints got cheaper (the first ceil(L/4) steps free) and every rating reset to 1500. In the first 20 new-rules server games almost every queen died, median around r50.

**gst-queen-1** (v12) adapted gst-search-8 by making the queen the only champion. When she died, nobody fed anyone. The fix was an “heir champion”, which became gst-queen-3 (v13).

The **queen-guard** line came from the giant team’s survival core. The first version kept the queen alive in 21-30% of games instead of 8%, yet lost overall (66-74) because the guard cost early economy. After several rounds of fixes, queen-guard-7 was #1 on the arena and the arena’s gate auto-submitted it on 10-02. On the server it went 60-0-73. The arena and the ladder disagreed loudly for the first time.

**codex-queen-value-1** came from Codex runs on separate branches (10-02 to 10-04). It was queen-guard-2 with queen length weighted 3x from r300, and reached arena #3 at 1543. Its lesson was how it beat us: it fed its queen and grew her long, while our heir feeding of a non-queen dragon halved our material after r320. When both queens survive, the longer queen wins.

## queen-ring17 and the ring line

The bot that reset our direction was a teammate’s. bu-gye-jeong’s **queen-ring17-first-then-grow-1** kept gst’s economy, parked the queen in a 6-cell ring, and grew her late (she leaves at r430, nearby donors feed her from r450). It was 54-19 on queen-decided server games and was live at 77-0-75 when round 10 started on 10-04, with the team at #35, 1832. We imported it unchanged; each line below is one defining change on the previous head.

[figure omitted: The ring lines in the arena’s lineage view. Each line branched off the one above it; a version’s colour shows how it did against its parent (green beat, red lost, tan tied, faded within noise).]

  * **ring-plus** (10-04): first a forage opening (a short queen with 2 or fewer starters forages and splits before parking; ring-plus-1), then hunger-scaled exploration for workers before r150 (ring-plus-4). ring-plus-4 beat its parent 52-26 in the arena and was auto-deployed, then went 45-41 on the server, exactly at Elo expectation. On the arena’s server suite (2 fixed seeds per map) it had beaten ring17 50-18; elsewhere 26-24. Forage was real on 2-starter maps; hunger exploration hurt on 3+-starter maps. Measured on real opponents’ ratings, the line was still worth +80-90 Elo over ring17.
  * **ring-sched** (10-04 to 10-05): team task scheduling, our direction. ring-sched-1 beat its parent 50-28. ring-sched-8 added a race to the geometric centre’s bed cluster and performed at 2091 on the server, about +70-100 over ring-plus-9; the team peaked at #18, 2054 during its run. On 40 freshly generated anti-centre maps, though, the rush lost 64-96 to the same code without it: it was fitted to the maps we knew, and from then on every opening idea had to pass mapgen families. The rush-free merge ring-sched-17 beat ring-sched-8 67-45 and went live; on the server it went 29-43.
  * **ring-contest** (10-05): in the opening, contest food we reach first and split on dense food. 63-49 over ring-sched-17; live from 10-05.
  * **ring-sprint** (10-06): free sprints for non-queen dragons of length 5+. 61-51 over ring-contest-1, live from 10-06. The ladder went #31 2032 → #22 2099 → **#16 2139** , our best recorded rank. The server analysis then measured ring-sprint-1 at 2046 against ring-contest-1’s 2035: the sprints netted zero, because more of our 5+ dragons got killed.
  * **ring-field** (10-07): a committed opening that sends starters to the bed fields. ring-field-3 won 74.5% against the 10 non-ring arena bots and went live; on the server it performed the same as ring-sprint-21 (2071 v 2082).
  * **ring-sealed** (10-08): if the queen’s pocket is too small to win the queen race, play for longest dragon instead, plus a fix for a share discount that let any ally beside a kelp wall “claim” the whole field. ring-sealed-2 was auto-deployed on 10-09 and the team climbed from #35 to #29, but the server analysis put the gain down to opponent ratings drifting up.
  * **ring-shield** (10-09): a lunge guard for late big dragons, then a walled-bastion fix, then, after we allowed a soft centre preference, a port of the old centre rush with exits. ring-shield-10 won Trophy 16-0 in its arena games but lost Default 3-13. We uploaded it; the latest recorded ladder position was #24, 2194.

Two side bots belong here. **spar-field** was a sparring partner: every arena opponent was a home-bound relative of ours, so spar-field played the top teams’ opening. spar-field-2 beat its parent 67-45 but was worse than ring-sprint-21 against non-ring bots (61.7% v 71.8%): its edge was only over our own family. **syeyoung-bot** was syeyoung’s own from-scratch bot, 36 commits on 10-08 and 10-09, written to try syeyoung’s own strategy in the hope that it would work well. Its plan: spread fast, harvest beds into children, children spread to new beds, the rest fight. Its notes say v1 explored and controlled more of the board than ring-field-3 but lost duels (no combat, the queen dies). We have no recorded arena verdict for the final version; syeyoung’s summary is that it didn’t work.

## What the lines taught us

These held up across lines:

  * **Economy before r300 decides games; consolidation never paid.** Every delivery mechanism we tried was neutral or negative on held-out seeds. Champions grew by eating, not by being fed.
  * **Many short dragons early.** On the solo benchmark, splitting at 4 reached 90% coverage at r124 against r177 for splitting at 12. Cutting splits to reduce churn lost (ring-longmid-1 25-42): short-lived children still take pearls and space first.
  * **Top teams win on mid-game army, not queen safety.** Their queens died more often than ours and they still won; see [the metrics post](</2026/10/11/4-metrics/>).
  * **The arena overrated what it was fitted to.** Two fixed seeds per map and a pool of only our own bots rewarded beating our own habits: ring-plus-4 and queen-guard-7 looked better at home than on the ladder, and spar-field-2’s edge was only over our family.

What moved the server rating, by our own rating-adjusted measurements: switching to the teammate’s ring bot, the ring-plus line (+80-90 over ring17), and ring-sched-8 (+70-100 over ring-plus-9). The sprints, the sealed queen and most work from 10-07 on measured as flat, whatever the ladder did.

---

2026-10-11

# 7\. Postmortem and results

Where UnderTheC ended up on the ladder and in the sprint tournament, the numbers behind it, and what we would change.

We set a goal on 10-06: reach #10 on the competition server. We didn’t. Our best recorded rank was #16, the last one we recorded was #24, and we have no record of a final end-of-competition rank. This post is the scoreboard and what we make of it.

The main lesson is the plainest one. Throwing more tokens at the problem does win: our rating rose about linearly with the tokens we put in. The stall after 10-06 fits that, since rounds shrank from six workers to three or four agents and one session stopped at its weekly usage limit.

## The ladder

The server rating is Elo-like and reset to 1500 when the 1.2.3 queen rules shipped on 10-01. These are the points we wrote down, with the bot that was live (bots are described in [what the factory made](</2026/10/11/6-bots/>)).

[figure omitted: Server rating after the 10-01 reset, from the points we recorded.]

Date | Rank | Rating | Live bot
---|---|---|---
10-01 | #72 of 947 | ~1767 | arena-search-7 (v9)
10-01 |  | 1797 | gst-giant-2 (v10), then reset to 1500
10-04 | #35 | 1832 | the teammate’s queen-ring17
10-04 |  | 1977 | ring-plus-4’s window (from 1831)
10-05 | #22 → #26 | 2035 → 2012 | ring-sched-8, then ring-sched-17
10-06 | #31 → #28 → #22 → **#16** | 2032 → 2038 → 2099 → **2139** | ring-contest-1, then ring-sprint-1
10-07 | #19 → #23 | 2146 → 2131 | ring-sprint-1 (#10 was 2270)
10-07/08 | #28 → #29 → #27 | 2112 → 2104 → 2120 | ring-sprint-1, then ring-field-3
10-08 | #35 | 2095 | ring-field-3
10-09 | #29 → **#24** | 2150 → **2194** | ring-sealed-2, ring-shield-3, ring-shield-10

Two caveats sit under that table. First, the rating ran about 90 above our measured, rating-adjusted performance: at #16, ring-sprint-1 performed at 2046. Second, several jumps were not the bot. ring-sealed-2’s +78 came from opponents’ ratings drifting up; on 17 common opponents it won exactly as many games as the previous bot’s rates predicted. By our own measurements the real gains all came between 10-04 and 10-06 (see [what the factory made](</2026/10/11/6-bots/>)). After that the ladder mostly wandered between 2100 and 2200. #1 (aAah) was at 2526 on 10-07.

## The sprint tournament

On 10-01 the server ran a sprint tournament: 591 teams, best of 7, on 7 new maps (Australia, Islands, Maze, Stripes, Tower Defense, Around UNSW, weakhold). It ran under the old 1.2.2 rules, and our live bot was v11 (gst-search-4). We were seeded #67, won 5 matches, and lost the round of 16 3-4. Over the tournament we went 31-11 in games, worst on Tower Defense (1-4). Cutlery beat PPP 4-3 in the final.

A strategist went through 140 tournament replays afterwards. Winners covered the map faster: the winner had visited more of the board in 94 of 112 games, and our heads had covered 23% by r100, the lowest of the 17 teams measured. On Tower Defense we never left the home box. Winners also started consolidating 50-100 rounds before us.

## The numbers

The arena played 291,668 games between 680 submissions, 441 of them from the agents. Before it, the local league had played 170,039 matches between 206 variants. The agents left 545 worker briefs and 78 strategist files behind.

Commit volume traced the method. It was heaviest in the first two days, with six workers on 20-minute tasks in the local league; it fell from 10-01 to 10-03 while long-running teams worked and the arena was built; it rose again from 10-04 with loss analysis, strategists and short fresh workers.

## What worked

  * **Real games as the signal.** Every durable gain was found by analysing server losses, then making one change. The ring line’s climb (#35 to #16 in two days) came out of that loop.
  * **Short workers with one change each, judged externally.** Lines climbed by stacking single changes that beat their parent: gst went 15 → 23 → 27 → … → 131, ring-plus 1 → 4 → 7 → 9 → 16 → 19 → 21.
  * **Taking a teammate’s bot seriously.** queen-ring17 was better on the server than anything the agents had made, and building on it was the biggest single step.
  * **Fixing the measurement before trusting it.** Two bugs made early results worthless: the sim seeded pearls differently from the judge, and a compiler flag made native builds diverge from the wasm judge. Finding them changed which bots we believed in.
  * **Fresh maps as a check.** Without generated maps we would have shipped the centre rush as a general gain.

## What didn’t

  * **The arena drifted from the server.** Its server suite had 2 fixed seeds per map and its pool was our own bots, so queen-guard-7 and ring-plus-4 looked better at home than on the ladder.
  * **Consolidation.** Every attempt to feed one champion was neutral or worse in every line. The gap was always economy before r300.
  * **Long-running teams.** In round 9 their contexts grew and they drifted; about 15 verdicts landed inside noise.
  * **Per-round merges.** Bundling a round’s changes into one draft mixed effects; we reversed it on 10-06.
  * **The work after 10-06.** From 10-07 to 10-08 the ladder stalled around 2100 and we reset the orchestrator to its 10-06 instructions. Most of the ring-field, ring-sealed and ring-shield changes measured as flat on the server.

## What we would do differently

  * Judge on the server, per map and rating-adjusted, from the first day, and treat our own arena as a filter only. That became the written rule only on 10-04, after queen-guard-7.
  * Put non-family bots in the arena pool early. spar-field-2 beat its parent 67-45 but did worse against non-ring bots; a pool of our own relatives rewards beating our own habits.
  * Spend more of the effort on the mid-game army, which the eval crawl pointed at on 10-05.
  * Test every opening on generated map families before reading an arena gain.

## What’s left

The arena is kept as a [static snapshot](<https://battlecode-underthec.github.io/arena/>) where any game re-runs in the browser, and the code is public under [Battlecode-UnderTheC](<https://github.com/Battlecode-UnderTheC>): [syeyoung-tokens](<https://github.com/Battlecode-UnderTheC/syeyoung-tokens>) for the bots, the orchestrator instructions and the generation reports, [tokenz-arena](<https://github.com/Battlecode-UnderTheC/tokenz-arena>) for the arena and the eval model, and [syeyoung-rl](<https://github.com/Battlecode-UnderTheC/syeyoung-rl>) for the RL attempt.
