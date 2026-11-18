#!/usr/bin/env python3
"""Read and compare .bc26 replays (gzipped flatbuffers, schema/battlecode.fbs of the engine repo), stdlib only.

    engine/test/replay.py A.bc26 [B.bc26]

One file: a summary (events, rounds, turns). Two files: where they first differ, as bytes and as game content.
"""

import gzip
import struct
import sys
from pathlib import Path

EVENTS = {1: "GameHeader", 2: "MatchHeader", 3: "Round", 4: "MatchFooter", 5: "GameFooter"}
ACTIONS = ["NONE", "CatFeed", "RatAttack", "RatNap", "RatCollision", "PlaceDirt", "BreakDirt", "CheesePickup",
           "CheeseSpawn", "CheeseTransfer", "CatScratch", "CatPounce", "PlaceTrap", "RemoveTrap", "TriggerTrap",
           "ThrowRat", "UpgradeToRatKing", "RatSqueak", "DamageAction", "StunAction", "SpawnAction", "DieAction",
           "IndicatorStringAction", "IndicatorDotAction", "IndicatorLineAction"]
# struct sizes of the struct actions (IndicatorStringAction, 22, is a table)
ACTION_SIZE = {1: 2, 2: 2, 3: 2, 4: 2, 5: 2, 6: 2, 7: 2, 8: 4, 9: 4, 10: 2, 11: 4, 12: 4, 13: 4, 14: 4, 15: 4,
               16: 2, 17: 2, 18: 4, 19: 4, 20: 8, 21: 4, 23: 8, 24: 8}
TURN_FIELDS = [(0, "robotId", "<i"), (1, "health", "<i"), (2, "cheese", "<i"), (3, "moveCooldown", "<i"),
               (4, "turningCooldown", "<i"), (5, "actionCooldown", "<i"), (6, "bytecodesUsed", "<i"),
               (7, "x", "<B"), (8, "y", "<B"), (9, "dir", "<B"), (12, "isCooperation", "<B")]
ROUND_VECTORS = [(0, "teamIds"), (1, "teamCheeseTransferred"), (2, "teamCatDamage"), (3, "teamAliveRatKings"),
                 (4, "teamAliveBabyRats"), (5, "teamRatTrapCount"), (6, "teamCatTrapCount"), (7, "teamDirtAmounts"),
                 (9, "diedIds")]


def load(path) -> bytes:
    data = Path(path).read_bytes()
    try:
        return gzip.decompress(data)
    except OSError:
        return data


class Table:
    def __init__(self, buf: bytes, pos: int):
        self.b = buf
        self.pos = pos
        self.vt = pos - struct.unpack_from("<i", buf, pos)[0]
        self.vtlen = struct.unpack_from("<H", buf, self.vt)[0]

    def off(self, slot):
        o = 4 + 2 * slot
        return struct.unpack_from("<H", self.b, self.vt + o)[0] if o < self.vtlen else 0

    def scalar(self, slot, fmt, default=0):
        o = self.off(slot)
        return struct.unpack_from(fmt, self.b, self.pos + o)[0] if o else default

    def ref(self, slot):
        o = self.off(slot)
        if not o:
            return None
        p = self.pos + o
        return p + struct.unpack_from("<I", self.b, p)[0]

    def table(self, slot):
        p = self.ref(slot)
        return Table(self.b, p) if p is not None else None

    def vector(self, slot):
        """(start, length) of a vector field, or (None, 0)."""
        p = self.ref(slot)
        if p is None:
            return None, 0
        return p + 4, struct.unpack_from("<I", self.b, p)[0]

    def ints(self, slot):
        s, n = self.vector(slot)
        return list(struct.unpack_from(f"<{n}i", self.b, s)) if n else []

    def bytes_(self, slot):
        s, n = self.vector(slot)
        return self.b[s:s + n] if n else b""

    def tables(self, slot):
        s, n = self.vector(slot)
        out = []
        for i in range(n):
            p = s + 4 * i
            out.append(p + struct.unpack_from("<I", self.b, p)[0])
        return out

    def string(self, slot):
        p = self.ref(slot)
        if p is None:
            return None
        n = struct.unpack_from("<I", self.b, p)[0]
        return self.b[p + 4:p + 4 + n].decode("utf-8", "replace")


class Replay:
    def __init__(self, buf: bytes):
        self.b = buf
        root = Table(buf, struct.unpack_from("<I", buf, 0)[0])
        self.events = []  # (type, wrapper position, event table position)
        for p in root.tables(0):
            w = Table(buf, p)
            self.events.append((w.scalar(0, "<B"), p, w.ref(1)))

    def event_name(self, k):
        t = self.events[k][0]
        name = EVENTS.get(t, f"event{t}")
        if t == 3:
            name += f" {Table(self.b, self.events[k][2]).scalar(10, '<i')}"
        return name

    def turns(self, k):
        return Table(self.b, self.events[k][2]).tables(8)

    def turn(self, pos):
        t = Table(self.b, pos)
        d = {name: t.scalar(slot, fmt) for slot, name, fmt in TURN_FIELDS}
        types = t.bytes_(10)
        acts = []
        for typ, p in zip(types, t.tables(11)):
            if typ == 22:
                acts.append((ACTIONS[typ], Table(self.b, p).string(0)))
            else:
                acts.append((ACTIONS[typ] if typ < len(ACTIONS) else str(typ), self.b[p:p + ACTION_SIZE.get(typ, 0)].hex()))
        d["actions"] = acts
        return d

    def round_info(self, k):
        t = Table(self.b, self.events[k][2])
        d = {name: t.ints(slot) for slot, name in ROUND_VECTORS}
        d["roundId"] = t.scalar(10, "<i")
        return d

    def locate(self, offset):
        """The event (and turn) whose bytes hold this offset: the builder writes back to front, so event k lies in
        [its wrapper, the previous event's wrapper)."""
        bounds = [p for _, p, _ in self.events]
        for k in range(len(bounds)):
            hi = bounds[k - 1] if k else len(self.b)
            if bounds[k] <= offset < hi:
                where = self.event_name(k)
                if self.events[k][0] == 3:
                    turns = self.turns(k)
                    for j, tp in enumerate(turns):
                        thi = turns[j - 1] if j else hi
                        if tp <= offset < thi:
                            rid = Table(self.b, tp).scalar(0, "<i")
                            return f"{where}, turn {j} (robot {rid})"
                    return f"{where}, round table"
                return where
        return "GameWrapper"


def without_winner(buf: bytes) -> bytes:
    """The replay with the MatchFooter and GameFooter winners zeroed (for coin-flip games)."""
    r = Replay(buf)
    out = bytearray(buf)
    for typ, _, pos in r.events:
        if typ in (4, 5):
            t = Table(buf, pos)
            o = t.off(0)
            if o:
                out[pos + o] = 0
    return bytes(out)


def compare(xa: bytes, xb: bytes) -> str:
    """None if equal, else a one-line description of the first difference."""
    if xa == xb:
        return None
    n = min(len(xa), len(xb))
    head = next((i for i in range(n) if xa[i] != xb[i]), n)
    tail = next((i for i in range(n) if xa[-1 - i] != xb[-1 - i]), n)
    msg = f"sizes {len(xa)}/{len(xb)}, first differing offset {head}"
    try:
        ra, rb = Replay(xa), Replay(xb)
        off = len(xa) - tail - 1
        msg += f"; first difference in write order at offset {off} of the first ({ra.locate(off)})"
        sem = semantic(ra, rb)
        if sem:
            msg += f"; first content difference: {sem}"
    except Exception as e:  # a broken buffer is itself the answer
        msg += f"; cannot decode: {e!r}"
    return msg


def semantic(ra: Replay, rb: Replay):
    ka, kb = len(ra.events), len(rb.events)
    for k in range(min(ka, kb)):
        ta, tb = ra.events[k][0], rb.events[k][0]
        if ta != tb:
            return f"event {k}: {ra.event_name(k)} vs {rb.event_name(k)}"
        if ta == 3:
            ia, ib = ra.round_info(k), rb.round_info(k)
            name = ra.event_name(k)
            tsa, tsb = ra.turns(k), rb.turns(k)
            for j in range(min(len(tsa), len(tsb))):
                da, db = ra.turn(tsa[j]), rb.turn(tsb[j])
                for f in da:
                    if da[f] != db[f]:
                        return f"{name}, turn {j} (robot {da['robotId']}): {f} {da[f]} vs {db[f]}"
            if len(tsa) != len(tsb):
                return f"{name}: {len(tsa)} vs {len(tsb)} turns"
            for f in ia:
                if ia[f] != ib[f]:
                    return f"{name}: {f} {ia[f]} vs {ib[f]}"
        else:
            sa, sb = event_bytes(ra, k), event_bytes(rb, k)
            if sa != sb:
                return f"event {k} ({ra.event_name(k)}) bytes differ"
    if ka != kb:
        return f"{ka} vs {kb} events"
    return None


def event_bytes(r: Replay, k):
    hi = r.events[k - 1][1] if k else len(r.b)
    return r.b[r.events[k][1]:hi]


def summary(r: Replay) -> str:
    rounds = [k for k in range(len(r.events)) if r.events[k][0] == 3]
    turns = sum(len(r.turns(k)) for k in rounds)
    bc = sum(r.turn(p)["bytecodesUsed"] for k in rounds for p in r.turns(k))
    return f"{len(r.b)} bytes, {len(r.events)} events, {len(rounds)} rounds, {turns} turns, {bc} bytecodes"


def main():
    if len(sys.argv) == 2:
        print(summary(Replay(load(sys.argv[1]))))
    elif len(sys.argv) == 3:
        d = compare(load(sys.argv[1]), load(sys.argv[2]))
        print("identical" if d is None else d)
        sys.exit(0 if d is None else 1)
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main()
