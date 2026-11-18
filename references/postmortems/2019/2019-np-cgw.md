# Archived postmortem: NP-cgw (2019)

| Field | Value |
|---|---|
| Title | Battlecode '19 |
| Team | NP-cgw |
| Year | 2019 |
| Game | Crusade (2019) |
| Placement | Finalist |
| Authors | Jerry Mao (with Angus Ritossa, Albert Smith) |
| Source | https://jerrymao.net/projects/battlecode/ |
| Retrieved | 2026-11-18 |
| Format | Web page, converted to Markdown (html2text) |
| Original file | none (web source) |

> **Archived copy of the authors' work.** Reproduced here unchanged in substance so this repo's agents can grep it; all rights remain with the authors (see Source). The text was machine-converted and lightly cleaned: page headers/footers, page numbers, broken hyphenation and tables of contents removed; figures replaced by `[figure omitted: <caption>]`. It is not a summary. Index and takeaways: `references/postmortems/INDEX.md`; cross-year lessons: `docs/lessons.md`.

---

This was my fourth year competing in Battlecode, having previous competed in the teams **AustRAYlian Memes** (2016), **wcgw** (2017, 7th-8th) and **wcgw** (2018, 2nd).

This year, I competed in the team **NP-cgw** with [Angus Ritossa](<https://github.com/AngusRitossa>) and [Albert Smith](<https://github.com/ChiCubed>), fellow teammates together at IOI 2018. This team was in fact a merge of two teams from Battlecode 2018: **wcgw** and **NP-ez**.

We have open-sourced our code, so you can check it out [here](<https://github.com/j-mao/battlecode-2019>).

## The Game: Crusade

[figure omitted: crusade.png]

The plotline of this year’s game _Crusade_ was based on the aftermath of the 2018 game _Escape to Mars_ , with two opposing factions of robots engaging in battle on Mars. On the surface, it appeared that this year’s robot types were similar to last year’s, and to these ends we were expecting elements of gameplay and strategy to be similar too.

### The units

  * [figure omitted: Castle sprite]  **Castles:** Immobile archon-like Structures that built all other robots except for Churches. Players started with between 1 to 3 Castles on the map, and losing all of your Castles would spell defeat.

  * [figure omitted: Church sprite]  **Churches:** A “pop-up Castle”: immobile Structures that could also spawn other robots. Could not attack.

  * [figure omitted: Pilgrim sprite]  **Pilgrims:** A simple but fragile farming robot capable of mining and building Churches. Has a large vision range so can also be used for scouting. Could not attack.

  * [figure omitted: Crusader sprite]  **Crusaders:** A cheap fast-moving melee robot that could deal medium damage in a short range.

  * [figure omitted: Prophet sprite]  **Prophets:** A ranged but fragile robot that deals medium damage. Could not attack in close range.

  * [figure omitted: Preacher sprite]  **Preachers:** Strong, powerful but expensive robots capable of dealing “splash” area-of-effect damage in a short range.

### Winning the game

The primary goal of the game is to annihilate the enemy Castles, which grants instant victory.

If neither team accomplishes this by turn 1000, then the game is decided by the following tiebreaks:

  * Larger quantity of Castles remaining
  * Greater total unit health
  * Random coin-toss

### Other game specifics

  * Units were each controlled by their own process, rather than a single process that controlled all units like last year. Unit vision was not shared, but units had access to the entire map layout, including impassable squares, and the location of resource depots.
  * Units this year did not have action delays, and instead performed a single action per turn.
  * There were two resource types that had to be managed simultaneously. Only Pilgrims could mine resource depots, which had an infinite supply of resources. This could be done when standing atop a depot (and not adjacent). However, resources that they collect are sent to a _personal_ collection limited in size, which must then be delivered to a Castle or a Church before usage, either by direct delivery or via other (possibly non-Pilgrim) units.
    * _Karbonite_ is spent on building units, with different Karbonite costs for different unit types.
    * _Fuel_ is spent for _all_ game actions, including unit-building, moving, attacking and even resource mining.
  * The game map was procedurally generated, and the algorithm used guaranteed that:
    * resource depots would appear in high-density _clusters_ that contained at least one depot of each type.
    * clusters closer to the centre of the map would generally contain a larger number of depots.
    * every Castle would be part of a cluster, but would generally be closer to the side of the map and therefore have less depots.
  * _Resource reclaim_ enabled units to earn a proportion of the resource cost of units they destroy, which is also added to their personal collection.
  * There were two modes of communication this year. Units were allowed to create one message of each type per turn.
    * Units could send a _radio_ broadcast to a specified radius, containing a message of 16 bits. This costed Fuel proportional to the radius of the broadcast area.
    * Units could send a _castle talk_ message of 8 bits. These messages can only be read by friendly Castles, but are sent for free.
  * Units are only aware of their own “age” (in rounds), but do not know the global turn-count.

### A note on trading

There was in fact another action that could be performed this year, which was called _trading_. Castles would be allowed to propose resource trades to the opponent team, consisting of a Karbonite amount and a Fuel amount, up to 1024 each. In a zero-sum-game such as this, trading was almost certainly not going to be useful. However, we expected many teams to attempt to utilise trade as a means of communication, as it offered two extra 10-bit integers of shared memory between Castles, especially as it was valid to propose a “trade” of the form, “ _if you give me 17 Karbonite, I’ll give you negative 23 Fuel._ ”

Such a mode of communication deserves sabotage. As such, we ensured that our Castles continually checked for opponent trade offers, and accepted any trades that it could not pay in lieu of enacting a null-action. Trades that could not be paid were not only nullified, but would also empty the trade cache, effectively destroying the entire communication.

## Pre-Sprint: Turtles and rushes

In a recall to the 2016 Battlecode game _Zombie Invasion_ , our initial strategy was to _turtle_ our Castles. A purely defensive strategy, it involved encasing our Castles within a large mass of Prophets, to capitalise on their large range to create a fortress. We used _castle talk_ to enable Castles to coordinate turtle-building (to ensure that turtles were all approximately the same size), and new units would scan their visible radius to determine an acceptable “turtling” location to produce a lattice checkerboard structure.

[figure omitted: turtle.png]

Our initial turtle structure

This checkerboard structure had many advantages, in that it was dense and accumulated a high amount of attack damage, yet also maintained freedom of movement in and out of the lattice without incurring high movement Fuel costs. It also enabled Prophets to pass back to the Castle any _resource reclaim_ gained by destroying enemy units that wandered too near, even without moving as resources could be given via adjacent lattice Prophets.

Our analysis of the game encouraged us to believe in what we dubbed the “ _defender’s advantage_ ”. As units could only do one of move or attack per turn, this meant that any attacking unit would necessarily spend a turn within the enemy’s attack range before being able to itself attack. Essentially, it seemed that sitting still and defending was the best strategy, and this gave us further confidence in our turtle strategy.

However, a few scrimmages quickly showed us a major weakness. The low health of Castles meant that they were highly vulnerable in the opening, before the fortress is established. Furthermore, it soon became apparent that Preachers were a very powerful unit: while they were expensive, they were capable of destroying a Prophet in one shot, and of destroying a Castle in five. Yet on the other hand, it can withstand six shots from non-Preacher units before being destroyed itself.

##### Crusader

Melee attack
Health: 40
Damage per shot: 10

##### Prophet

Ranged attack
Health: 20
Damage per shot: 10

##### Preacher

Splash attack
Health: 60
Damage per shot: 20

A strong strategy in this opening week was a _Preacher rush_ , and this strategy quickly became the focus of all discussion within and between teams, either to craft their own rush or defend against those of others (or even both). The _Preacher rush_ creates 1 Pilgrim to mine Karbonite from the Castle’s cluster and spends all remaining resources on sending Preachers directly to the enemy Castles.

[figure omitted: rush.png]

A Castle being destroyed by a Preacher rush

With Crusaders and Prophets appearing very weak in such a defense (especially since enemy Preachers could ignore these units and directly target the Castle), the only viable option seemed to be to use Preachers in fight off a Preacher rush. This hypothesis was supported by our tests, in which we played our own _rush-bot_ against a _defense-bot_ and found that only a Preacher defense could be effective.

However, this too had its inherent issues. It seemed that this defensive strategy would restrict our bot to forever waiting for an imminent attack, and never expanding on its own. Any Karbonite expenditure would reduce the number of Preachers we could produce in response to an incoming rush, spelling potential defeat if a full-sized enemy rush arrived at that moment.

The alternative of building Preachers to perform a rush ourselves was not particularly desirable, as it would likely lead to the game being decided by luck as both teams leave their Castles entirely undefended. Experience (and possibly also common sense) dictates that relying on luck is not always the best way to go.

As such, we had arrived at the following situation:

\\[\text{Turtle} < \text{Rush} < \text{Defence} < \text{Turtle}\\]

In the end, we decided to submit a merge of our turtle and defense strategies. This strategy stayed in defensive mode for the first 30 turns while waiting for an enemy rush, after which we would assume that the scene is clear and commence building turtles. As Karbonite was a scarcity due to the urgent need to build more units, sufficient Pilgrims would gradually be built to farm Karbonite deposits in the friendly half of the map. Our strategy would also coordinate an attacking swarm of 10 robots if it detects that it has a large amount of Fuel (2500 Fuel) to support the attack.

## Sprint tournament

The Sprint tournament consisted mainly of rush-bots and turtle-bots, and the top 4 places were **Monad** , **MOVE BIT << GET OUT THE WAY**, **Emoticons** , and **HAHA NOT TEAMLEADER**.

We made it to the round of 32, where we were eliminated due to accidentally giving up our _defender’s advantage_. In defending against a Preacher rush, we sent our own Preachers towards the incoming enemy, but in doing so became the ones who first stepped into enemy range. As such, our defending Preachers were first to be destroyed, leaving the Castle open to attack.

There was a game in which our Pilgrims’ preference for Karbonite left us with plenty of Karbonite but almost no Fuel income, meaning that we were barely able to perform any actions; we rectified this issue in future submissions by enabling Pilgrims to explore Fuel depots.

## Pre-Seeding: Thirsty for resources

With **Teh Devs** nerfing the rush strategy by increasing Castle health and allowing them to attack, and later by also allowing Preachers to attack empty squares to maximise the potential of their splash damage, rushes were no longer as formidable a strategy. We found that we could defend against first-turn rushes with only two Preachers by making use of the _defender’s advantage_ , and in some cases even just one Preacher was enough if the splash damage was managed well.

This period saw a shift in the metastrategy as the turtle approach was extended by the concept of territorial expansion. Thus far, the limiting resource had been Karbonite as it was slow to mine and spent in large quantities when building units. But, with this resource no longer as restrained by the need to prepare for a rush, building masses of Pilgrims at the start of the game was suddenly a viable long-term investment, forming an _eco_ strategy.

We saw teams such as **Codelympians** quickly expand their Pilgrims to capture resource clusters across the board, claiming ownership of territory while their opponents only farmed local depots. With a much larger resource income, they were able to quickly overwhelm the map with “ _hundreds of units_ ” (quote from **Teh Devs** , taken both literally and figuratively), effectively smothering the opponent team.

[figure omitted: eco.png]

**Dogma** (red) vs **Codelympians** (blue)

It soon became clear to us that the win-condition was almost equivalent to establishing a stronghold in the centre of the map. With control of the centre comes greater territory and a greater resource income, leading to a larger army with which to erode enemy turtles. As such, we decided so send Pilgrims to resources near the centre of the board as soon as we could. Furthermore, in order to solidify our claim to the centre territory, we would send a Prophet _bodyguard_ before the Pilgrims, and thus deter any potential enemy competitors.

With more units on the map, our previous swarms of 10 robots were no longer true swarms. We needed to send larger swarms of robots, but also not drain our turtles so completely that our Castles would be left defenceless. As such, we initiated swarms when we had approximately 100 units (actual number varied depending on the map size). Trusting the game’s random number generator to be sufficiently uniform with robot IDs, our Castles would inspect the units in their turtle and broadcast a minimum robot ID, denoting a set of robots which would participate in the swarm. This worked rather well for us, as it meant that we could easily communicate without ambiguity a specific subset of our robots, ensuring that we had both a sufficiently large army and a reasonable defense body.

It was only then that we began to realise how important Fuel was. Until then, it seemed to be very plentiful, largely due to our relatively small swarms and the passive Fuel income. However, when executing large attacks consisting of 100 units, Fuel is quickly drained as units may spend over 100 Fuel each just travelling to their destination, and 10000 Fuel took a hundreds of rounds to accumulate. We were therefore compelled to include a Fuel threshold for initiating a swarm; this value was initially set at 150 Fuel per unit, but was continually revised (and increased) for the reaminder of the game.

## Seeding tournament

The seeding tournament was largely dominated by this eco metastrategy, and was won by **Standard Technology** , with **DOS** coming second.

Interestingly, **DOS** ’s bot was not purely based on eco, but also created continual bouts of Preacher attacks that gradually eroded the health of their opponents’ Castles.

**Team Barcode** also produced an interesting strategy in which they sent Prophets to occupy the map’s line of symmetry. This “wall” was almost impenetrable once formed, and acted as a powerful claim to territory.

We were eliminated in the round of eight, when a bug in our code caused all of our Castles to stop building their turtles entirely. allowing ourselves to eventually be consumed in a swarm of enemy units. As **Teh Devs** quite rightly noted, “ _if you’ve got over 10000 Karbonite, you really should spend it_ ”. However, for us, this bug enabled us to discover a much larger flaw in our work, as discussed below.

## Pre-Qualifiers: A fresh start

With a growing codebase consisting of 2000 lines of strategy, patches, and `if` statements that no longer seem to make sense, it seemed to us that our code was going nowhere. Our bug in the Seeding tournament was caused by the addition of later patches which broke assumptions and invariants asserted by earlier patches; in particular, we used _castle talk_ messages to coordinate turtles, but while Castles tried to read this data from all Structures, Churches did not actually broadcast any information at all. In a sense, our code was a sprawling collection of undocumented conditions, an accumulation of quick fixes here and there, which resulted in a need for a major overhaul.

Albeit with some hesitation, it was unanimously resolved that we would restart our entire bot. We would be guided by our analyses of the game strategy, along with the opportunity to implement a new form of metastrategy.

Thus far, we had taken the idea of robots being individually controlled literally. However, most of these robots had little information about the state of the game, while Castles could acquire almost complete knowledge via _castle talk_. Analysing many of our scrimmages showed that while our Pilgrims would bustle to and fro between resource depots, often to find that it was already occupied by another friendly Pilgrim, while enemy Pilgrims could produce substantially more resources by being more coordinated.

We came upon the solution while watching our scrimmages against **Oak’s Last Disciple** , who appeared to centralise all decision-making within their Castles. Whenever a new unit was built, the Castle would send a radio message in a small range, presumably to assign that unit a location in which to stay. This streamlined the resource gathering process, ensuring that their Pilgrims never wasted time searching for a depot, as the locations were all predetermined.

This formed the basis of our new strategy, by which we radio-messaged a location assignment for all robots. Castles became omniscient beings that were constantly aware of the welfare and location of all friendly units, and could easily manage new unit assignments.

However, while attacking via swarms was a good idea, its effect was still largely unsynchronised, as both during the Sprint tournament and during our tests it was evident that simple swarms were easily demolished by turtles. Reflecting back on the large-scale ranger wars of Battlecode 2018, it became apparent that we needed a means of creating a concave shape around the enemy defense. Such a concave shape would enable us to concentrate fire, and with a large enough concave negate the effect of their defender’s advantage.

This we implemented both on the attacking front and in our defensive turtles, which would form a concave facing the enemy, increasing the difficulty for the enemy to create a concave attack.

Our turtle concave worked by assigning each map location a real-number _turtle penalty_ , calculated as the weighted average of a number of factors, including:

  * distance from the friendly Castle;
  * distance from enemy Castles;
  * difference in distance from enemy Castles and the distance between the friendly Castle and those Castles;
  * difference in distance to the friendly Castle and to the enemy Castles. This factor was used as an exponential term to encourage units not to stray into enemy territory.

Our full formula and experimental graphs can be found below. See the Appendix for some amusing stepping-stones that we encountered along the way to this graph.

On the attacking front, initial ideas included calculating the convex hull of enemy locations and somehow broadcasting this data to construct the tightest possible concave. Of course, this idea was rather intractable due to the need for large quantities of Pilgrims to scout the enemy frontline, and to maintain this information without going bankrupt on Fuel. Our eventual solution was to create a constricting circle around the enemy Castles, starting with a circle that covered the whole board and gradually decreased in size until the Castle was within sight. This would create not only the desired concave shape, but also enable our units to engage in battle simultaneously, to overwhelm the defending enemy.

This solution discovered that _patience is key_. Instead of allowing units to blindly charge towards the enemy, receiving a command to attack would instead initiate a clock counter. The size of the circle would be a function of this clock, and so a unit would only advance if it was outside the circle. Furthermore, it made sense for the circle to constrict quickly in the initial phase of advancing, and more slowly in the later stages as units engage in combat. In order to ensure that our circle “accommodates” all of the enemy Castles, we used a smooth-min function to accumulate all of the distances, and then our “radius” function was constructed to satisfy the following:

\\[\begin{aligned} r(0) &= \sqrt{2}\cdot\text{mapSize} \\\ r'(t) &= -\frac{S_f^2-S_i^2}{2(r(0)-r_f)}\cdot t-S_i \end{aligned}\\]

where

  * \\(S_i=\frac{\text{mapSize}}{64}\\) is the initial squeeze rate, in squares per turn
  * \\(S_f=0.05\\) is the final squeeze rate, in squares per turn
  * \\(r_f=4\\) is the final circle radius

This, of course, was solved with a healthy dose of calculus, producing the following effect:

[figure omitted: circle.png]

Circle formation

## Qualifying tournament

Talk of “secret strategies” was rampant in the lead-up to the Qualifying tournament. The tournament revealed **Knights of Cowmelot** ’s _Church saber_ , which abused the turn queue to create a long line alternating between Pilgrims and Churches extending deep into the enemy turtle. This infiltration could then produce a wealth of Preachers to devour the turtle and Castle from within.

Overall, the Qualifying tournament contained a mix of eco strategies that handled expansion of territory, invasion into enemy farms, and bodyguards in different ways. We qualified in the top 16 without losing a match, although in the full tournament bracket lost to **Standard Technology** , who were able to secure the central resource clusters better than us, as our Pilgrims had a tendency to leave some resource clusters undefended.

## Pre-Finals: offense or defense?

To be frank, the home run felt like a bit of a scramble.

This period of time consisted largely of bugfixes and minor improvements, and a last-minute dash to implement our own rendition of a Church saber. In retrospect, what we were majorly lacking in was still a good earlygame resource standoff, and this may well become our Achilles’ heel during the finals.

As much as our Pilgrims were able to colonise faraway clusters with the aid of bodyguards, often our Karbonite reserve for protecting against rush-bots seemed too high, meaning that our opponent Pilgrims could arrive at central clusters before ours. While finetuning our earlygame was always on our todo-list, the quest to eradicate bugs and chase the rapidly evolving metagame caused this part of our bot to be underdeveloped. This was a major oversight in the aftermath of the Qualifying tournament, in which the Church saber strategy had left me rather concerned and disrupted our priorities.

In the meantime, the Church saber was incorporated by many more teams such as **Gisthekey** , who used it very effectively to dismantle enemy farms and strongholds. Early scrimmages showed that our Church saber was also effective, but considering that it was implemented in the final hour it was not perfected or thoroughly tested.

## Finals tournament

To be continued… maybe?

## Concluding remarks

Firstly, many thanks must go to **Teh Devs** for coordinating yet another spectacular Battlecode. It takes a significant amount of work to plan and create a balanced game so complex despite its simple mechanics, and also to provide such amazing support throughout the competition, especially with swiftly resolving technical issues even though the issues were outside their control.

This year’s competition saw a large shift towards the metagame compared to the details of micro strategies in previous years, which was definitely a worthwhile experience.

This year’s Battlecode was also a very educational experience in project management. For most of the competition we were very well-organised with a prioritised todo list, and for at least half the competition we had well-maintained code too. However, the urgent need to completely refactor our codebase was a sobering experience as we were bitten by the consequences of undocumented code. Most important, though, is the need to remain calm despite surprises, which definitely caught us off-guard when we encountered the Church saber.

Overall, this year’s Battlecode was a very enjoyable competition, and I look forward to competing again next year!

##  Appendix: Pretzels

Creating a graph that simulates a concave is not easy. Below are some failed attempts at procuring the desired equation, which I have included here for your general amusement.

[figure omitted: pretzel_graph.png]

[figure omitted: IMG_6465.jpg]

Take 1: A pretzel

[figure omitted: compoundeye_graph.png]

This "compound eye" image has been hidden for the sake of your sanity. If you really wish to see it, click [here](<https://cdn.iwastesomuchtime.com/f55fd726-52bc-4f88-949d-bdfb0a397b3f.jpeg>).

Take 2: A compound eye (?!)
