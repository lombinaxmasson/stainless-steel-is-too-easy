"""Check real HU ducts/plates in the existing disposable NeoForge test server.

Requires :neoforge:runServer -PssiteSmokeTest
         -PssiteServerDir=build/large-aod-smoke
Never starts a server, creates a world, or accepts an EULA.
"""
import argparse

from large_aod_server import Rcon
from fluidized_bed_server import fixture_guard


STAGES = ('native', 'shared', 'topology', 'disconnect', 'unloaded')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--stage', choices=STAGES, help='Each stage creates its own independent fixtures.')
    args = parser.parse_args()
    remote = Rcon()
    remote.socket.settimeout(90)  # First creation of remote fixture chunks may exceed the shared 20 s default.
    try:
        fixture_guard(remote)
        remote.command('tick freeze')
        for stage in (args.stage,) if args.stage else STAGES:
            result = remote.command('ssite_heat_transport_smoke ' + stage)
            if 'PASS: heat transport ' + stage not in result:
                raise AssertionError(f'{stage}: {result}')
            print(result, flush=True)
        print('PASS: requested heat-transport runtime checks; server remains tick-frozen', flush=True)
    finally:
        remote.socket.close()


if __name__ == '__main__':
    main()
