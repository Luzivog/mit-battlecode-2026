"""Every test of the compute tools stays on this machine: a call that would reach DoC fails at once, and the shared
client state (session slots, the view, caches) is the test's own."""
from __future__ import annotations

import os
import subprocess
from pathlib import Path

import pytest

os.environ["BC26_WORKSPACE"] = "/vol/gpudata/test-bc26"  # before tools.remote reads it: the tests' own, never reached
from tools import remote  # noqa: E402


@pytest.fixture(autouse=True)
def no_doc(monkeypatch: pytest.MonkeyPatch, tmp_path: Path) -> None:
    monkeypatch.setattr(remote, "CLIENT", tmp_path / "client-state")
    monkeypatch.setattr(remote, "control_path", lambda host: f"/nonexistent/bc26-test-{host}")

    def no_master(host: str) -> None:
        raise remote.Unreachable(f"tests reach no DoC host ({host})")
    monkeypatch.setattr(remote, "start_master", no_master)
    monkeypatch.setattr(remote, "master_up", lambda host: False)
    monkeypatch.setattr(remote, "_submit_run", lambda argv, stdin=None: subprocess.CompletedProcess(
        argv, 255, b"", b"tests reach no DoC host"))
