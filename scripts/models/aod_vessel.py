"""Geometry for the furnace-mounted AOD. Dimensions are in world-block units."""
import math


def build(m):
    center=(.5,.5)
    # The furnace bottom itself meets the burner. No foundation, skid or pedestal.
    # Nine adjacent quads exactly cover [-1,2] x [-1,2] on Y=0.
    for x in range(-1,2):
        for z in range(-1,2):
            m.face([(x,0,z),(x+1,0,z),(x+1,0,z+1),(x,0,z+1)],"heat","heat_contact")
    # Twelve perimeter points retain the four exact square corners (45,135,...).
    rings=[]
    for y,r,square in [(0,1.5,True),(.28,1.5,True),(.85,1.73,False)]:
        ring=[]
        for i in range(12):
            a=(i+.5)*math.pi/6
            rr=r/max(abs(math.cos(a)),abs(math.sin(a))) if square else r
            ring.append((.5+rr*math.cos(a),y,.5+rr*math.sin(a)))
        rings.append(ring)
    for row,(a,b) in enumerate(zip(rings,rings[1:])):
        for i in range(12):
            j=(i+1)%12
            m.face([a[i],b[i],b[j],a[j]],"dark" if row==0 else "shell","lower_shell")
    # Y=1..2 and Y=2..3 are complete, identical column layers. The shoulder starts at 3.2.
    m.profile([(.85,1.73),(1,1.73),(2,1.73),(3,1.73),(3.2,1.73),(3.65,1.55),(4.2,1.1),(4.85,1.1)],"shell","vessel",center=center)
    m.profile([(5,.91),(4.22,.91),(3.5,1.34),(1.2,1.49),(.52,1.25)],"refractory","lining",center=center)
    m.tube((.5,.5,.5),(.5,.52,.5),1.25,"refractory","lining",12)
    m.tube((.5,1.84,.5),(.5,1.87,.5),1.47,"molten","bath",12)
    for y,r,h,mat in [(.85,1.8,.15,"dark"),(3.05,1.84,.15,"dark"),(3.5,1.72,.14,"edge")]:
        m.ring(y,r-.12,r,h,mat,"bands",center=center)
    m.ring(4.68,1.05,1.19,.15,"edge","neck",center=center)
    m.ring(4.83,.91,1.2,.17,"refractory","neck",center=center)
    # Exposed bolts, segmented wall ribs and lifting lugs preserve the heavy silhouette.
    for i in range(12):
        a=(i+1)*math.pi/6
        for y in (.94,3.12):
            m.tube((.5+1.70*math.cos(a),y,.5+1.70*math.sin(a)),(.5+1.86*math.cos(a),y,.5+1.86*math.sin(a)),.065,"edge","bands",6)
        m.tube((.5+1.76*math.cos(a),1.0,.5+1.76*math.sin(a)),(.5+1.76*math.cos(a),3.05,.5+1.76*math.sin(a)),.045,"edge","bands",4)
    for x in (-1.23,2.23):
        direction=-1 if x<0 else 1
        m.tube((x,3.05,.5),(x+direction*.3,3.05,.5),.3,"dark","lifting_lugs",8)
        m.tube((x+direction*.28,3.05,.5),(x+direction*.38,3.05,.5),.38,"edge","lifting_lugs",8)
    # Direct sidewall tuyere fittings: round neck, octagonal collar, thin square flange.
    # All end-face pixels and geometry finish on X=3; bolts stay behind that plane.
    for port,z,mat in [("oxygen",-.5,"pipe"),("argon",1.5,"copper")]:
        group="port_"+port
        m.tube((1.76,1.5,z),(2.83,1.5,z),.29,mat,group,8)
        m.tube((2.25,1.5,z),(2.42,1.5,z),.39,"dark",group,8)
        m.tube((2.65,1.5,z),(2.86,1.5,z),.42,"edge",group,8)
        m.box((2.88,1,z-.5),(3,2,z+.5),"edge",group,{"+x":group})
        for y in (1.12,1.88):
            for zz in (z-.38,z+.38):m.tube((2.78,y,zz),(2.879,y,zz),.055,"dark",group,6)
        m.box((2.2,1.86,z-.23),(2.53,1.98,z+.23),"dark",group)
        m.tube((2.37,1.98,z),(2.37,2.16,z),.055,"edge",group,6)
        m.tube((2.22,2.16,z),(2.52,2.16,z),.035,"red",group,6)
    # Refractory tapping boss. Its flat Z=-2 face accepts a separate world faucet.
    m.tube((.5,1.5,-1.05),(.5,1.5,-1.86),.34,"refractory","port_casting",8)
    m.tube((.5,1.5,-1.49),(.5,1.5,-1.66),.43,"dark","port_casting",8)
    m.box((0,1,-2),(1,2,-1.88),"edge","port_casting",{"-z":"port_casting"})
    # Keep the casting connection and the mold position directly below it unobstructed.
    m.box((.03,3.73,-.81),(.97,4.15,-.78),"dark","nameplate",{"-z":"nameplate"})
    # A narrow rear service step and a short body-mounted ladder; nothing below Y=0.
    m.box((-1.72,3.42,-.16),(-1.02,3.55,1.16),"grate","service_step")
    for z in (-.12,1.12):
        m.tube((-1.68,3.55,z),(-1.68,4.42,z),.035,"yellow","service_step",6)
        m.tube((-1.35,2.9,z),(-1.7,3.42,z),.045,"dark","service_step",4)
    m.tube((-1.68,4.42,-.12),(-1.68,4.42,1.12),.035,"yellow","service_step",6)
    for z in (.18,.82):m.tube((-1.78,.45,z),(-1.78,3.5,z),.04,"yellow","ladder",6)
    for i in range(10):m.tube((-1.78,.57+i*.3,.18),(-1.78,.57+i*.3,.82),.03,"edge","ladder",6)
    # The cover is one named moving object. Static hinge stays on the rear shoulder.
    for x in (-.18,1.18):
        m.box((x-.1,3.75,1.73),(x+.1,5.12,1.95),"dark","hinge_mount")
        m.tube((x-.2,5.1,1.84),(x+.2,5.1,1.84),.16,"edge","hinge_mount",8)
        m.box((x-.18,3.7,1.53),(x+.18,3.95,1.87),"plate","hinge_mount")
    m.ring(5.01,.9,1.23,.12,"edge","lid",center=center)
    m.profile([(5.13,1.23),(5.28,1.23),(5.57,.73)],"plate","lid",center=center)
    m.tube((.5,5.55,.5),(.5,5.63,.5),.74,"dark","lid",12)
    m.tube((.5,5.04,.5),(.5,5.12,.5),.91,"refractory","lid",12)
    m.ring(5.24,1.13,1.25,.07,"dark","lid",center=center)
    for x in (.12,.88):
        m.tube((x,5.59,.5),(x,5.85,.5),.045,"yellow","lid",6)
    m.tube((.12,5.85,.5),(.88,5.85,.5),.055,"yellow","lid",6)
    for x in (-.18,1.18):
        m.box((x-.06,5.14,1.27),(x+.06,5.26,1.89),"edge","lid")
    return m
