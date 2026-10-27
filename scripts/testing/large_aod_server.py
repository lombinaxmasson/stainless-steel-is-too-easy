"""Exercise the actual GT6CE runtime in the isolated neoforge/build/large-aod-smoke world.

Requires that local test server with RCON enabled; never connects to a user-selected server.
Use --command only to inspect/debug this disposable world, or no arguments for the suite.
"""
from pathlib import Path
import argparse
import socket
import struct
import time

ROOT = Path(__file__).resolve().parents[2]
GAME = ROOT / 'neoforge/build/large-aod-smoke'

class Rcon:
    def __init__(self):
        props = dict(line.split('=',1) for line in (GAME/'server.properties').read_text().splitlines()
                     if '=' in line and not line.startswith('#'))
        assert props['server-ip'] == '127.0.0.1' and props['rcon.port'] == '25588'
        self.socket = socket.create_connection(('127.0.0.1',25588), timeout=20)
        self.send(3,props['rcon.password'])
        while True:
            request, kind, _ = self.receive()
            if kind == 2:
                assert request != -1, 'RCON authentication failed'
                break

    def read(self, count):
        data = b''
        while len(data)<count:
            packet = self.socket.recv(count-len(data))
            if not packet: raise EOFError('RCON closed')
            data += packet
        return data

    def send(self,kind,message):
        packet = struct.pack('<ii',42,kind)+message.encode()+b'\0\0'
        self.socket.sendall(struct.pack('<i',len(packet))+packet)

    def receive(self):
        count, = struct.unpack('<i',self.read(4))
        packet = self.read(count)
        request,kind = struct.unpack('<ii',packet[:8])
        return request,kind,packet[8:-2].decode()

    def command(self,command):
        self.send(2,command)
        return self.receive()[2].strip()

    def step(self,count=10):
        self.command(f'tick step {count}')
        time.sleep(count/20+.2)

def textpos(pos): return ' '.join(map(str,pos))

def suite(r):
    r.command('forceload add -16 -16 64 16')
    r.command('gamerule doMobSpawning false')
    r.command('tick freeze')
    r.step(5)
    for index, facing in enumerate(('north','east','south','west')):
        ox,oy,oz = index*16,100,0
        right,back = (((1,0),(0,1)),((0,1),(-1,0)),((-1,0),(0,-1)),((0,-1),(1,0)))[index]
        def pos(x,y,z): return ox+right[0]*x+back[0]*z,oy+y,oz+right[1]*x+back[1]*z
        def block(x,y,z,name,properties=''):
            result=r.command(f'setblock {textpos(pos(x,y,z))} {name}{properties}')
            assert 'Changed' in result or 'Could not set' in result, result
        r.command(f'fill {ox-5} 97 -7 {ox+5} 110 7 air')
        tier='ta4hfc5'
        block(0,0,0,'ssite:large_aod_'+tier,f'[facing={facing}]')
        for x in (-1,0,1):
            for z in (1,2,3): block(x,-1,z,'ssite:large_aod_heat')
        for y in (0,1,2):
            for x,z in ((-1,2),(1,2),(0,1),(0,3)): block(x,y,z,'ssite:large_aod_casing_'+tier)
        block(2,0,1,'ssite:large_aod_oxygen')
        block(2,0,3,'ssite:large_aod_argon')
        block(0,3,2,'ssite:large_aod_lid')
        r.step(25)
        value=r.command(f'data get block {ox} {oy} {oz} "gt.behavior".b."structure.formed"')
        assert value.endswith('1b'), (facing,value)
        print(f'PASS: {facing} structure forms with world-aligned port cells',flush=True)
    # Every sole cell must relay real HU from the original GT6CE burning box.
    r.command('fill -1 98 1 1 98 3 air')
    for x in (-1,0,1):
        for z in (1,2,3):
            r.command('data merge block 0 100 0 {"gt.behavior":{l:{"temp.k":293L,"heat.carry":0L}}}')
            r.command(f'setblock {x} 98 {z} gt6ce:burning_box_solid_steel{{"gt.energy":10000L,"gt.active":1b}}')
            r.step(5)
            value=r.command('data get block 0 100 0 "gt.behavior".l."heat.carry"')
            assert int(value.rsplit(': ',1)[1].rstrip('L')) > 0, value
            r.command(f'setblock {x} 98 {z} air')
    print('PASS: all nine sole cells receive HU from real GT6CE burning boxes',flush=True)

    def fields(mode,open=False):
        r.command('data merge block 0 103 2 {"gt.part_fields":{i:{"lid.mode":'+str(mode)+'},b:{open:'+('1b' if open else '0b')+'}}}')
    def opened():
        return r.command('data get block 0 103 2 "gt.part_fields".b.open').endswith('1b')
    fields(0)
    r.command('setblock 1 103 2 redstone_block'); r.step(3)
    assert not opened(), 'manual mode must ignore signal'
    fields(1); r.step(3); assert opened(), 'high signal must open lid'
    r.command('setblock 1 103 2 air'); r.step(3); assert not opened(), 'low signal must close lid'
    fields(2); r.step(3); assert opened(), 'inverted mode must open without signal'
    print('PASS: real redstone opens/closes lid in both modes; manual mode is unchanged',flush=True)

    fields(0,False)
    r.command('data merge block 0 100 0 {"gt.behavior":{l:{"temp.k":3000L}}}')
    r.command('setblock 0 104 2 hopper{Items:[{Slot:0b,id:"ssite:dust_carburizedstainlesssteel",count:9}]}')
    r.step(10)
    value=r.command('data get block 0 104 2 Items[0].count')
    assert value.endswith('9'), 'closed lid accepted hopper feed: '+value
    fields(0,True); r.step(80)
    value=r.command('data get block 0 104 2 Items')
    assert value.endswith('[]'), 'open lid refused hopper feed: '+value
    value=r.command('data get block 0 100 0 "gt.behavior".l."pool.carburized"')
    assert int(value.rsplit(': ',1)[1].rstrip('L')) > 0, 'charged material did not melt: '+value
    r.command('setblock 0 104 2 air')
    print('PASS: hopper blocked by closed lid; open lid accepts charge and residual heat melts it',flush=True)

    # Supply exact recipe gas and molten charges; assert actual port buffers, refining and casting.
    fields(0,False)
    unit=648648000  # gregtech.api.material.GTValues.U
    r.command('data merge block 0 100 0 {"gt.behavior":{l:{"temp.k":3000L,"pool.carburized":'+str(9*unit)+'L,"pool.ferrosilicon":'+str(unit)+'L,"pool.quicklime":'+str(unit)+'L,"pool.fluorite":'+str(unit)+'L}}}')
    for z,fluid in ((1,'oxygen'),(3,'argon')):
        value=r.command(f'data merge block 2 100 {z} {{"gt.part_tanks":[{{Capacity:32000L,Amount:10L,Fluid:{{id:"gt6ce:{fluid}",amount:10}}}}]}}')
    r.step(3)
    for z in (1,3):
        value=r.command(f'data get block 2 100 {z} "gt.part_tanks"[0].Amount')
        assert value.endswith('0L'), 'gas port was not drained: '+value
    value=r.command('data get block 0 100 0 "gt.behavior".l."pool.stainless"')
    assert value.endswith(str(9*unit)+'L'), 'closed mixture did not refine: '+value
    print('PASS: O2 and Ar ports each pay 10 mB for the 9 U stainless recipe',flush=True)

    r.command('setblock 0 100 -1 gt6ce:crucible_faucet_ta4hfc5[facing=south]{"gt.faucet.facing":3b,"gt.faucet.auto_pull":1b}')
    r.command('setblock 0 99 -1 gt6ce:mold_basin_ta4hfc5')
    r.command('setblock 0 101 -1 redstone_block')
    r.step(3)
    value=r.command('data get block 0 99 -1 "gt.content.amount"')
    assert value.endswith(str(9*unit)+'L'), 'faucet did not cast into basin: '+value
    value=r.command('data get block 0 100 0 "gt.behavior".l."pool.stainless"')
    assert value.endswith('0L'), 'cast metal was duplicated: '+value
    print('PASS: GT6CE faucet casts the exact 9 U into a real basin',flush=True)

    value=r.command('data get block 0 103 2 "gt.part_slots"[1].id')
    assert 'ssite:dust_aodslag' in value, 'slag did not reach the dedicated lid output: '+value
    print('PASS: slag is buffered in the charging lid output slot',flush=True)

    # A cold charge can be inserted, then sealed and heated; closing must not disable melting.
    r.command('data merge block 0 100 0 {"gt.behavior":{l:{"temp.k":293L,"pool.carburized":0L}}}')
    fields(0,True)
    r.command('setblock 0 104 2 hopper{Items:[{Slot:0b,id:"ssite:dust_carburizedstainlesssteel",count:1}]}')
    r.step(10)
    value=r.command('data get block 0 103 2 "gt.part_slots"[0].count')
    assert value.endswith('1'), 'cold charge missing: '+value
    fields(0,False)
    r.command('data merge block 0 100 0 {"gt.behavior":{l:{"temp.k":3000L}}}')
    r.step(3)
    value=r.command('data get block 0 100 0 "gt.behavior".l."pool.carburized"')
    assert value.endswith(str(unit)+'L'), 'closing the lid stopped already charged material melting: '+value
    r.command('setblock 0 104 2 air')
    print('PASS: already charged material melts after closing the lid with no processing switch',flush=True)

    # Explosion and meltdown affect only this disposable world, well above the ground.
    for x,open,lid,marker in ((16,False,(14,103,0),(17,100,0)),(32,True,(32,103,-2),(32,100,1))):
        r.command(f'setblock {textpos(marker)} glass')
        r.command('data merge block '+textpos(lid)+' {"gt.part_fields":{i:{"lid.mode":0},b:{open:'+('1b' if open else '0b')+'}}}')
        r.command(f'data merge block {x} 100 0 {{"gt.behavior":{{l:{{"temp.k":10000L}}}}}}')
        r.step(2)
        value=r.command(f'execute if block {x} 100 0 air')
        assert 'Test passed' in value, 'overheated large controller survived'
        value=r.command(f'execute if block {textpos(marker)} '+('glass' if open else 'air')+'')
        assert 'Test passed' in value, ('open' if open else 'closed')+' large failure mismatch: '+value
    print('PASS: closed large AOD explodes; open large AOD melts without a blast',flush=True)

    for i,(type,open) in enumerate((('aod_converter_steel',False),('aod_converter_steel',True),('var_furnace',False),('var_furnace',True))):
        x=i*12
        r.command(f'setblock {x} 125 0 ssite:{type}')
        r.command(f'setblock {x} 126 0 ssite:aod_lid')
        r.command(f'data merge block {x} 126 0'+ ' {"gt.part_fields":{b:{open:'+('1b' if open else '0b')+'}}}')
        r.command(f'setblock {x+1} 125 0 glass')
        r.step(15)
        r.command(f'data merge block {x} 125 0 {{"gt.behavior":{{l:{{"temp.k":10000L}}}}}}')
        r.step(2)
        value=r.command(f'execute if block {x} 125 0 air')
        assert 'Test passed' in value, type+' survived overtemperature'
        value=r.command(f'execute if block {x+1} 125 0 '+('glass' if open else 'air')+'')
        assert 'Test passed' in value, type+' failure mismatch: '+value
    print('PASS: small AOD and VAR both explode sealed and melt open',flush=True)
    print('PASS: runtime suite',flush=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--command')
    args=parser.parse_args()
    remote=Rcon()
    if args.command: print(remote.command(args.command))
    else: suite(remote)
