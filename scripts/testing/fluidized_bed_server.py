"""Exercise real fluidized-bed machines in the disposable NeoForge test world.

Requires :neoforge:runServer -PssiteSmokeTest
         -PssiteServerDir=build/large-aod-smoke
The fixture is loaded only by the optional smoke source set. This script never
creates a world, changes EULA, starts a server, or accepts a remote address.
"""
from pathlib import Path
import argparse
import re

from large_aod_server import GAME, ROOT, Rcon


STAGES = (
    ('prepare', 30),
    ('break', 5),
    ('repair', 5),
    ('structure', 0),
    ('energy', 0),
    ('energy_gate', 5),
    ('fuels', 0),
    ('controls', 0),
    ('automation_start', 100),
    ('automation_check', 0),
    ('receivers_start', 100),
    ('receivers_check', 0),
    ('persistence', 0),
)


def fixture_guard(remote):
    expected = (ROOT / 'neoforge/build/large-aod-smoke').resolve()
    if GAME.resolve() != expected:
        raise RuntimeError('Refusing a non-fixture game directory')
    props = dict(line.split('=', 1) for line in (GAME / 'server.properties').read_text().splitlines()
                 if '=' in line and not line.startswith('#'))
    if (props.get('server-ip'), props.get('rcon.port'), props.get('level-name')) != (
            '127.0.0.1', '25588', 'world'):
        raise RuntimeError('Refusing unexpected fixture address or world name')
    players = remote.command('list')
    if not re.search(r'There are 0 of a max of \d+ players online', players):
        raise RuntimeError('Refusing to modify a world with online players: ' + players)


def run_stage(remote, stage, ticks):
    result = remote.command('ssite_fluidized_bed_smoke ' + stage)
    expected = 'PASS: fluidized bed ' + stage
    if expected not in result:
        raise AssertionError(f'{stage}: {result}')
    print(result, flush=True)
    if ticks:
        remote.step(ticks)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--stage', choices=[stage for stage, _ in STAGES],
                        help='Run one fixture stage for targeted diagnosis; prepare is required first.')
    args = parser.parse_args()
    remote = Rcon()
    try:
        fixture_guard(remote)
        remote.command('tick freeze')
        selected = STAGES if args.stage is None else [row for row in STAGES if row[0] == args.stage]
        for stage, ticks in selected:
            run_stage(remote, stage, ticks)
        if args.stage is None:
            print('PASS: fluidized-bed runtime suite; test server remains tick-frozen', flush=True)
    finally:
        remote.socket.close()


if __name__ == '__main__':
    main()
