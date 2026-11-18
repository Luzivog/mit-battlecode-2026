# Archived postmortem: llllllll's Disciple (2021)

| Field | Value |
|---|---|
| Title | Winning Elections Without Voting |
| Team | llllllll's Disciple |
| Year | 2021 |
| Game | Campaign (2021) |
| Placement | HS 3rd (65th-128th overall) |
| Authors | Ivy Zhang (Kaunta) |
| Source | https://ivyzhang.me/bc21-postmortem |
| Retrieved | 2026-11-18 |
| Format | Web page, converted to Markdown (html2text) |
| Original file | none (web source) |

> **Archived copy of the authors' work.** Reproduced here unchanged in substance so this repo's agents can grep it; all rights remain with the authors (see Source). The text was machine-converted and lightly cleaned: page headers/footers, page numbers, broken hyphenation and tables of contents removed; figures replaced by `[figure omitted: <caption>]`. It is not a summary. Index and takeaways: `references/postmortems/INDEX.md`; cross-year lessons: `docs/lessons.md`.

---

Published Feb 01, 2021 Updated Feb 01, 2021

# Winning Elections Without Voting

Hi, I’m Ivy (Kaunta) and this was my first year competing in Battlecode on **llllllll’s Disciple** (2021, 65th-128th overall, 3rd high school).

As someone both competing for both the first time and solo, I want to use this as a guide to help future newbies find their footing. Also before we get started, quick shout-out to **llllllll** who basically became my mentor throughout the competition.

## The Game

Unlike traditional RTSs, this year’s game included only passive income from enlightenment centers and from slanderers. Each team started with 1-3 enlightenment centers with the possibility of an additional 6 neutral enlightenment centers to convert scattered around the map, and the game was decided by killing all opponent units and enlightenment centers. If this condition wasn’t met, then the tiebreakers were in order: votes, number of enlightenment centers, total unit influence, and coin flip (everyone’s favorite).

### Resources

There were two resources this year: influence and conviction. Influence being the source of money which you used to spawn troops and conviction being the hit-points of your units. The two caveats were that enlightenment center influence was always equal to its conviction and muckrakers which had a max conviction of \lceil 0.7I \rceil.

### Voting

Each enlightenment center has the ability to bet some of it’s influence into a blind auction for a vote. The EC which bid highest would garner a vote for its team in return for the bid, the highest bid from the opposing team would lose 1/2 of its bid, and all other ECs would get their bid returned. If there was a tie, both teams would spend 1/2 of the highest bid and lose their vote to a third party.

### Units

Enlightenment centers had the unique ability to spawn units, passively generate influence, and bid for votes. Much like traditional RTSs, these proved to be the main points of contention which teams fought over.

Politicians had could empower: spread their conviction to surrounding units. The politician self destructs and each robot in a small radius would receive (C - 10) / N conviction. (C being politician conviction and N being number of bots within radius) Friendly units would gain conviction up to their initial conviction, enemy units would lose conviction. If a muckraker or slanderer’s conviction went below 0, they would be destroyed, and politicians and enlightenment centers would be converted to your team.

## The Run

### Pre-Sprint

Although I had read dozens of postmortems and knew that strategy wise I should be copying others, pre-sprint me completely ignored that advice. While most people were working towards a healthy balance of eco + expansion, I poured all my time and energy into creating a muckraker bot.

The most interesting thing about the micro was:

    for (RobotInfo ally : allyRobots) {
     if (ally.getType() != RobotType.MUCKRAKER) continue;
     ...
     density[directionToAlly] += (31 - distanceToAlly);
    }

This ensured that my muckrakers would spread out and cover the map evenly, but it had a very poor start where muckrakers would bounce around at spawn rather than exploring the map. This was solved in later versions where muckrakers would no longer move to the square they just came from _unless_ they hit a border.

Also whether it’s your first year competing or not, the first week should really be focused on implementing essentials like communication, path-finding, etc. The 2 limiting factors of Battlecode are time and sanity, so leave actual strategy for after you’ve established a solid foundation.

### Sprint 1

This was an absolute wash; my bot was eliminated before making stream to no one’s surprise. Muckraker rush bots and hard eco found some success, but the prevailing strategy seemed to be quick expansion and safer economy which **Super Cow Powers** pulled off wonderfully to take the first Sprint.

The one interesting concept I used here was stolen from **llllllll**. By compressing EC influences into buckets of 50, I was able to communicate the location and influence of enemy/neutral ECs within the 24 bits provided. The general idea of sacrificing accuracy for speed of communication seems like an important factor to consider for future competitions.

### Sprint 2

The week leading up to the tournament, we had a few interesting strategical developments. The first of which was **BUFF** rakers (big muckrakers). While the first week placed a large focus on 1 influence muckrakers because of their ability to be extremely cost efficient by removing slanderers of any influence, people started adapting. Politicians became over-fitted to trade efficiently against 1 conviction muckrakers, and this opened the door for larger muckrakers who could waltz right by and demolish the economy of the opposing team. These new “buffrakers” also enabled a new play-style of late-game turtling. The exponential buff you got from exposing slanderers combined with the ability to self empower your own enlightenment centers meant that teams could make “immortal” ECs then win the game off votes, as it was impossible to come back against an exponential muckraker buff without … you guessed it! An exponential muckraker buff of your own.

Unlike Sprint 1, I had decided to follow the general meta with a normal eco/fast expand bot and made it to Top 32. I was then promptly wiped out by **Chicken** who had much stronger eco and path-finding than I did which were key on Sprint 2 maps with very defined best paths. Once again, **Super Cow Powers’** solid path-finding, adaptable strategy, and superb defense of slanderers reigned king, and he got yet another Sprint win under his belt.

### Qualifying Tournament

After Sprint 2, Teh Devs wisely decided that bunkering up after exposing a few slanderers wasn’t great game design and nerfed the muckraker buff into the ground (made it linear rather than exponential) while removing the ability to self empower. With sustained pushes becoming the most popular viable strategy, teams continued pushing the bounds of how greedy they could be. There also became a new counter school of thought led by **wololo** who would exclusively build muckrakers and politicians. Although he’d fall greatly behind in eco, the quick takeover of neutral ECs allowed him to dominate tighter maps where raw unit count could overwhelm influence advantages.

Unfortunately, my nerves got to me before the contest, and I submitted the bot with a critical bug. While my politicians were supposed to clean up enemies past Turn 900 to avoid going to voting tiebreaks, which I never got around to implementing, they only ran the cleanup code **on** Turn 900. I got knocked out 0-2 and came in last place.

It also wasn’t until right before this tournament that I really understood how important the basics are. 30 minutes before, I decided to implement a simple greedy path-finding which was simply:

    Direction[] dirs; // Direction to target and two adjacent
    Direction bestDir; int maxPassability = -INF;
    for (Direction dir : dirs) {
        // If this have higher passability, this becomes the new best direction
    }
    rc.tryMove(bestDir);

Just this simple change in my bot made the matchup against the previous bot an **100%** win rate. Seriously, focus on doing the basics really well. It matters.

### High School Tournament

This was really supposed to be a fight for 2nd. In scrims, **Coast** had a clear first place lead with 2nd-6th all hovering around 100 ELO points lower than them. **Bytecode Mafia** had actually taken down **Coast** in the Qualifying tournament, but I don’t think anyone expected an upset like that to happen again with a new map pool.

Starting off with a narrow loss to **Bytecode Mafia** 2-3, I was instantly knocked down to the Loser’s Bracket and had to make a Cinderella run. I might be just a _smidge_ biased, but I think I had the best story line in the High School bracket.

After a few sweeps against lower seeded teams, I faced my first real match against the first seed, **Coast**. They were a greedier eco bot than I was, so this matchup really came down to whether the maps would favor heavier eco or quicker expansion. After trading back and forth in a slugfest of tight games, we reached Map 5 tied 2-2. There’s people calling upset, I’m shaking with excitement, and then the map loads. A small map with 6 neutral ECs scattered around the map. Their greed came back to bite them, as through sheer muckraker spam, I was able to destroy their eco, and get the first large upset 3-2.

Next up was **Fandead** , the 4th seed. I had never won a single ranked scrim against them, so this truly felt like the end of the line for me, but once again, their greedier bot was punished by smaller maps with an abundance of expansions. Another extremely close series which came down to a game 5 win for me. My crucial wins all came from being less greedy than my opponent, but this would finally come back to haunt me in the Loser’s Finals against **java :ghosthug:**. Although I had a very conservative economy, **java :ghosthug:** only made a few slanderers to start off with before producing only politicians and muckrakers. Their fast politician scouts and quick takeover of neutral ECs quickly wiped out my economy, and I ended up losing the series 0-5 and finishing 3rd.

### Final Tournament

Seeing as I didn’t qualify for this, you’ll want to find another post-mortem to find out how the event ends! Spoiler: it’s very hype. You should definitely read some more postmortems not just to find out the final results; but also, for the top secret strategies some top teams had.

## Final Thoughts

Although I placed last in the qualifying tournament because I submitted a bot with a critical bug and didn’t make it to HS Grand Finals, I’m still very proud of what I managed to accomplish and the great times this event brought. With such tough competition, this competition really pushed me to the bounds of my skills and helped me learn so much.

Thanks to Teh Devs (commentators included) and the great community for starting off 2021 with a bang, and I can’t wait to compete next year!
