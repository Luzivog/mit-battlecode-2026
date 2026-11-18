"""python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'"""
import unittest

from bcgames import stats


class GsprtTest(unittest.TestCase):
    def test_error_rates_match_alpha_beta(self):
        # At the H0 boundary the test may accept H1 at most ~alpha; at H1 it must accept H1 ~1-beta of the time.
        probs0 = stats.trinomial(0.5, 0.7)
        probs1 = stats.trinomial(0.6, 0.7)
        r0 = stats.simulate(probs0, stats.TRINOMIAL, 0.5, 0.6, 0.05, 0.05, 16, 2000, runs=400, seed=3)
        r1 = stats.simulate(probs1, stats.TRINOMIAL, 0.5, 0.6, 0.05, 0.05, 16, 2000, runs=400, seed=4)
        self.assertLess(r0["rates"]["H1"], 0.09)
        self.assertGreater(r1["rates"]["H1"], 0.91)

    def test_llr_sign_follows_the_data(self):
        self.assertGreater(stats.llr([0, 5, 10], stats.TRINOMIAL, 0.5, 0.6), 2.0)
        self.assertLess(stats.llr([10, 5, 0], stats.TRINOMIAL, 0.5, 0.6), -2.0)

    def test_all_splits_is_evidence_against_h1(self):
        # Identical bots split every cell: ~13 such cells reject a 10% gain (as likely as 13 splits at score 0.6).
        self.assertLess(stats.llr([0, 13, 0], stats.TRINOMIAL, 0.5, 0.6), -2.8)
        self.assertGreater(stats.llr([0, 12, 0], stats.TRINOMIAL, 0.5, 0.6), -2.94)

    def test_mle_has_the_requested_mean(self):
        p = stats._mle([0.2, 0.6, 0.2], stats.TRINOMIAL, 0.6)
        self.assertAlmostEqual(sum(p), 1.0, places=9)
        self.assertAlmostEqual(sum(a * b for a, b in zip(p, stats.TRINOMIAL)), 0.6, places=9)

    def test_pentanomial_non_inferiority(self):
        # Paired difference always 0 (same behaviour as the reference): "not worse" is accepted.
        self.assertGreater(stats.llr([0, 0, 30, 0, 0], stats.PENTANOMIAL, 0.45, 0.5), 2.95)
        # Consistently worse by a cell in four: "worse" is accepted.
        self.assertLess(stats.llr([0, 10, 30, 0, 0], stats.PENTANOMIAL, 0.45, 0.5), -2.95)


class SummaryStatsTest(unittest.TestCase):
    def test_elo_and_los(self):
        self.assertAlmostEqual(stats.elo(0.5), 0.0)
        self.assertAlmostEqual(stats.elo(0.6), 70.4, places=1)
        self.assertAlmostEqual(stats.los(5, 5), 0.5)
        self.assertGreater(stats.los(10, 2), 0.98)

    def test_bootstrap_by_map_is_cluster_robust(self):
        # Two maps, each with many identical cells: the CI spans the map means, not the cell count's tiny SE.
        cells = [("m1", 1.0)] * 50 + [("m2", 0.0)] * 50
        lo, hi = stats.bootstrap_by_map(cells, reps=500)
        self.assertLessEqual(lo, 0.0 + 1e-9)
        self.assertGreaterEqual(hi, 1.0 - 1e-9)

    def test_trinomial(self):
        l, d, w = stats.trinomial(0.6, 0.7)
        self.assertAlmostEqual(w + d / 2, 0.6)
        self.assertAlmostEqual(l + d + w, 1.0)
        with self.assertRaises(ValueError):
            stats.trinomial(0.9, 0.7)


if __name__ == "__main__":
    unittest.main()
