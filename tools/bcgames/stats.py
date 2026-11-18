"""Statistics on paired cells: GSPRT (fishtest style), Elo, LOS, bootstrap over maps, SPRT simulation."""
from __future__ import annotations

import math
import random

# A cell is one (map, seed) played in both seats. Its score for a bot: 1 (won both), 0.5 (split), 0 (lost both).
TRINOMIAL = (0.0, 0.5, 1.0)
# Gauntlet: the paired difference of two bots' cell scores, d in {-1,-.5,0,.5,1}, mapped to (d+1)/2.
PENTANOMIAL = (0.0, 0.25, 0.5, 0.75, 1.0)


def _mle(probs: list[float], scores: tuple[float, ...], s: float) -> list[float]:
    """The distribution closest (max likelihood) to `probs` with mean s: p_i = probs_i / (1 + t (score_i - s))."""
    lo_t = max((-1 / (a - s) for a, p in zip(scores, probs) if p > 0 and a > s), default=-1e9)
    hi_t = min((1 / (s - a) for a, p in zip(scores, probs) if p > 0 and a < s), default=1e9)

    def f(t: float) -> float:
        return sum(p * (a - s) / (1 + t * (a - s)) for a, p in zip(scores, probs))

    lo, hi = lo_t + 1e-12 * (abs(lo_t) + 1), hi_t - 1e-12 * (abs(hi_t) + 1)
    for _ in range(64):  # f is decreasing in t; 64 halvings of a width-O(1) interval reach float precision
        mid = (lo + hi) / 2
        if f(mid) > 0:
            lo = mid
        else:
            hi = mid
    t = (lo + hi) / 2
    return [p / (1 + t * (a - s)) for a, p in zip(scores, probs)]


def llr(counts: list[int], scores: tuple[float, ...], s0: float, s1: float) -> float:
    """Generalized SPRT log-likelihood ratio of H1: mean = s1 vs H0: mean = s0 (Van den Bergh; fishtest).

    Zero counts are regularised to 1e-3, as fishtest does.
    """
    n = sum(counts)
    if n == 0:
        return 0.0
    reg = [c if c > 0 else 1e-3 for c in counts]
    tot = sum(reg)
    probs = [c / tot for c in reg]
    p0, p1 = _mle(probs, scores, s0), _mle(probs, scores, s1)
    return n * sum(p * math.log(b / a) for p, a, b in zip(probs, p0, p1))


def bounds(alpha: float, beta: float) -> tuple[float, float]:
    return math.log(beta / (1 - alpha)), math.log((1 - beta) / alpha)


def elo(score: float) -> float:
    score = min(max(score, 1e-6), 1 - 1e-6)
    return -400 * math.log10(1 / score - 1)


def los(wins: int, losses: int) -> float:
    """Likelihood of superiority from decisive cells (won both vs lost both); splits carry no information."""
    if wins + losses == 0:
        return 0.5
    return 0.5 * (1 + math.erf((wins - losses) / math.sqrt(2 * (wins + losses))))


def bootstrap_by_map(cells: list[tuple[str, float]], reps: int = 2000, seed: int = 1) -> tuple[float, float]:
    """95% CI of the mean cell score, resampling whole maps (cells of one map are correlated)."""
    by: dict[str, list[float]] = {}
    for m, s in cells:
        by.setdefault(m, []).append(s)
    groups = list(by.values())
    if len(groups) < 2:
        return (float("nan"), float("nan"))
    rng = random.Random(seed)
    means = []
    for _ in range(reps):
        tot = n = 0.0
        for _ in groups:
            g = groups[rng.randrange(len(groups))]
            tot += sum(g)
            n += len(g)
        means.append(tot / n)
    means.sort()
    return means[int(0.025 * reps)], means[int(0.975 * reps) - 1]


def simulate(probs: tuple[float, ...], scores: tuple[float, ...], s0: float, s1: float, alpha: float, beta: float,
             min_n: int, max_n: int, runs: int = 2000, seed: int = 1) -> dict:
    """Runs the sequential test on synthetic cells drawn from `probs`. Returns verdict rates and cells used."""
    lo, hi = bounds(alpha, beta)
    rng = random.Random(seed)
    verdicts = {"H1": 0, "H0": 0, "inconclusive": 0}
    used = []
    cum = [sum(probs[: i + 1]) for i in range(len(probs))]
    for _ in range(runs):
        counts = [0] * len(probs)
        verdict = "inconclusive"
        for n in range(1, max_n + 1):
            u = rng.random()
            counts[next(i for i, c in enumerate(cum) if u <= c + 1e-12)] += 1
            if n >= min_n:
                x = llr(counts, scores, s0, s1)
                if x >= hi:
                    verdict = "H1"
                elif x <= lo:
                    verdict = "H0"
                if verdict != "inconclusive":
                    break
        verdicts[verdict] += 1
        used.append(n)
    used.sort()
    return {"rates": {k: v / runs for k, v in verdicts.items()}, "mean": sum(used) / runs,
            "median": used[runs // 2], "p90": used[int(runs * 0.9)]}


def trinomial(score: float, split: float) -> tuple[float, float, float]:
    """Cell outcome probabilities (lost both, split, won both) with the given mean score and split rate."""
    w = score - split / 2
    l = 1 - split - w
    if w < -1e-12 or l < -1e-12:
        raise ValueError(f"score {score} impossible with split rate {split}")
    return (max(l, 0.0), split, max(w, 0.0))
