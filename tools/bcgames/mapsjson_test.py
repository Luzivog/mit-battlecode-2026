"""python3 -m unittest discover -s tools/bcgames -t tools -p 'mapsjson_test.py'

~/battlecode26-data/theory/t4/maps.json, the map table theory scripts read, against the map files as the engine loads
them: every body's team, place and chirality. It once kept 12 maps' teams reversed after maps/ changed; regenerate it
with ~/battlecode26-data/theory/t4/src/MapDump.java (java -cp ENGINE.jar:. MapDump maps OUT.json)."""
import json
import unittest

from bcgames import mapclass, store

MAPS_JSON = store.DATA.parent / "theory" / "t4" / "maps.json"


@unittest.skipUnless(MAPS_JSON.exists(), f"no {MAPS_JSON}")
class MapsJsonTest(unittest.TestCase):
    def test_bodies_match_the_map_files(self):
        table = {m["name"]: m for m in json.loads(MAPS_JSON.read_text())}
        names = sorted(p.stem for p in (store.ROOT / "maps").glob("*.map26"))
        self.assertEqual(sorted(table), names)
        engine = mapclass.dump(names)
        for name in names:
            want = sorted((b["id"], b["team"], b["type"], b["x"], b["y"], b["chir"]) for b in engine[name]["body_list"])
            have = sorted((b["id"], b["team"], b["type"], b["x"], b["y"], b["chir"]) for b in table[name]["bodies"])
            self.assertEqual(have, want, name)
            self.assertEqual(table[name]["sym"], engine[name]["symmetry"], name)


if __name__ == "__main__":
    unittest.main()
