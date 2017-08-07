import math,string,itertools,fractions,heapq,collections,re,array,bisect,sys,random,time,copy,functools,json,pickle

sys.setrecursionlimit(10**7)
inf = 10**20
gosa = 1.0 / 10**9
mod = 10**9 + 7

def LI(): return [int(x) for x in sys.stdin.readline().split()]
def LI_(): return [int(x)-1 for x in sys.stdin.readline().split()]
def LF(): return [float(x) for x in sys.stdin.readline().split()]
def LS(): return sys.stdin.readline().split()
def I(): return int(sys.stdin.readline())
def F(): return float(sys.stdin.readline())
def S(): return input()

DEBUG = True

def pe(*a, **b):
    if not DEBUG:
        return
    with open('/tmp/t.log', 'a') as f:
        if a:
            f.write(str(a) + '\n')
        if b:
            s = ''
            for k, v in sorted(b.items()):
                if s:
                    s += ', '
                s += '{}={}'.format(k,v)
            f.write(s + '\n')
    return

def pf(s):
    print(s, flush=True)

class UnionFind:
    def __init__(self, size, table=None):
        if table:
            self.table = table
        else:
            self.table = [-1 for _ in range(size)]

    def find(self, x):
        if self.table[x] < 0:
            return x
        else:
            self.table[x] = self.find(self.table[x])
            return self.table[x]

    def union(self, x, y):
        s1 = self.find(x)
        s2 = self.find(y)
        if s1 != s2:
            if self.table[s1] <= self.table[s2]:
                self.table[s1] += self.table[s2]
                self.table[s2] = s1
            else:
                self.table[s2] += self.table[s1]
                self.table[s1] = s2
            return True
        return False

    def subsetall(self):
        a = []
        for i in range(len(self.table)):
            if self.table[i] < 0:
                a.append((i, -self.table[i]))
        return a

def setup():
    s_time = time.time()
    C,P,F,S,O = LI()
    pe(C=C,P=P,F=F,S=S,O=O)
    N, M, K = LI()
    e = set([tuple(LI()) for _ in range(M)])
    ee = collections.defaultdict(set)
    for a,b in e:
        ee[a].add(b)
        ee[b].add(a)

    MI = max([max(_) for _ in e])

    def search(s):
        d = {}
        d[s] = 0
        q = []
        heapq.heappush(q, (0, s))
        v = collections.defaultdict(bool)
        while len(q):
            k, u = heapq.heappop(q)
            if v[u]:
                continue
            v[u] = True

            for uv in ee[u]:
                if v[uv]:
                    continue
                vd = k + 1
                if uv not in d:
                    d[uv] = inf
                if d[uv] > vd:
                    d[uv] = vd
                    heapq.heappush(q, (vd, uv))

        for k in d.keys():
            d[k] **= 2
        return d

    ek = {}
    esa = LI()
    uf = UnionFind(MI + 1)
    ufs = [UnionFind(MI+1, uf.table[:]) for _ in range(C)]
    for m in esa:
        ek[m] = search(m)

    te = [set() for _ in range(C)]

    O = int(O) == 1
    OS = [K if O else 0 for _ in range(C)]

    state = {
        'e': e,
        'oe': set(list(e)),
        'ek': ek,
        'esa': esa,
        'P': P,
        'N': N,
        'M': M,
        'K': K,
        'C': C,
        'O': O,
        'OS': OS,
        'MI': MI,
        'te': te,
        'ufs': ufs,
        'fs': [],
    }
    #pe(state)

    pf(str(pickle.dumps(state)))
    pe(S)
    if S == 0:
        pf('0')
    else:
        pf('1')
        s = random.choice(esa)
        t = random.choice(list(ee[s]))
        if O:
            k = math.ceil(2.0 * K / C) ** 2
            tk = 1
            for ts in esa:
                for tt in range(N):
                    if tk < ek[ts][tt] <= k:
                        tk = ek[ts][tt]
                        s = ts
                        t = tt
        pf('{} {}'.format(s, t))
        state['fs'] = [[s,t,int(tk ** 1.5)]]
    pe(fs=state['fs'])

def play():
    s_time = time.time()
    state = input()
    state = pickle.loads(eval(state))
    e = state['e']
    oe = state['oe']
    ek = state['ek']
    esa = state['esa']
    P = state['P']
    N = state['N']
    M = state['M']
    K = state['K']
    C = state['C']
    O = state['O']
    OS = state['OS']
    MI = state['MI']
    te = state['te']
    ufs = state['ufs']
    fs = state['fs']
    pe(P=P,N=N,C=C)

    for i in range(C):
        na = LI()
        if na[0] < 1:
            continue
        for ni in range(1, na[0]):
            s,t = na[ni],na[ni+1]
            if s == -1:
                # passの場合
                continue
            te[i].add((s,t))
            if (s,t) in e:
                e.remove((s,t))
            elif (t,s) in e:
                e.remove((t,s))
            else:
                if (s,t) in oe:
                    oe.remove((s,t))
                elif (t,s) in oe:
                    oe.remove((t,s))
                OS[i] -= 1
            ufs[i].union(s,t)

    def res(te):
        scores = [0] * C
        for i in range(C):
            uf = UnionFind(MI+1, ufs[i].table[:])
            for t in te[i]:
                uf.union(t[0], t[1])
            tm = {}
            st = 0
            for m in esa:
                mf = uf.find(m)
                if mf not in tm:
                    tm[mf] = [m]
                else:
                    tm[mf].append(m)
            for iii in range(MI+1):
                ii = uf.find(iii)
                if ii not in tm:
                    continue
                for m in tm[ii]:
                    st += ek[m][iii]
            scores[i] = st
            if i == P:
               for ts,tf,tk in fs:
                   if uf.find(ts) == uf.find(tt):
                       scores[i] += tk
                   else:
                       scores[i] -= tk

        rs = scores[P]
        r = 0
        for s in scores:
            if rs == s:
                r += 1
            elif rs < s:
                r += 2
        return (r,rs * 2 - sum(scores))

    mel = list(e)
    el = list(e)
    ell = len(el)
    eil = {}
    for i in range(ell):
        eil[mel[i]] = i
    mol = list(oe)
    ol = list(oe)
    oll = len(ol)
    oil = {}
    for i in range(oll):
        oil[mol[i]] = i

    ss = [0 for _ in range(ell)]
    sc = [0 for _ in range(ell)]
    sr = [0 for _ in range(ell)]
    oss = [0 for _ in range(oll)]
    osc = [0 for _ in range(oll)]
    osr = [0 for _ in range(oll)]

    def playout():
        random.shuffle(el)
        nte = [set() for _ in range(C)]
        for i in range(ell):
            nte[(P+i) % C].add(el[i])
        ote = [set() for _ in range(C)]
        if O:
            random.shuffle(ol)
            ocs = [0 for _ in range(C)]
            for j in range(oll):
                i = (P+i) % C
                if OS[i] > ocs[i] and nte[i] and ol[j] not in te[i] and ol[j] not in nte[i]:
                    ocs[i] += 1
                    ote[i].add(ol[j])
                    nte[i].pop()
        r,s = res([nte[i] | ote[i] for i in range(C)])
        for nt in nte[P]:
            ei = eil[nt]
            ss[ei] += s
            sr[ei] += r
            sc[ei] += 1
        for ot in ote[P]:
            oi = oil[ot]
            oss[oi] += s
            osr[oi] += r
            osc[oi] += 1


    cnt = 0
    while time.time() - s_time < 0.8:
        cnt += 1
        playout()

    pe(cnt=cnt, time=time.time() - s_time)

    mr = inf
    ms = -1
    mri = -1
    of = False
    for i in range(oll):
        if osc[i] < 1:
            continue
        er = 1.0 * osr[i] / osc[i]
        es = 1.0 * oss[i] / osc[i]
        if mr > er or (mr == er and es > ms):
            mr = er
            ms = es
            mri = i
            of = True
    for i in range(ell):
        if sc[i] < 1:
            continue
        er = 1.0 * sr[i] / sc[i]
        es = 1.0 * ss[i] / sc[i]
        if mr > er or (mr == er and es > ms):
            mr = er
            ms = es
            mri = i
            of = False

    pf(str(pickle.dumps(state)))

    if mri == -1:
        pf('{}'.format(P))
    else:
        if of:
            pe(of=of,mr=mr,ms=ms,ol=mol[mri],len=ell,ein=(mol[mri][0], mol[mri][1]) in e)
            if (mol[mri][0], mol[mri][1]) in e:
                pf('{} {} {}'.format(P, mol[mri][0], mol[mri][1]))
            else:
                pf('{} -{} -{}'.format(P, mol[mri][0]+1, mol[mri][1]+1))
        else:
            pe(of=of,mr=mr,ms=ms,el=mel[mri],len=ell)
            pf('{} {} {}'.format(P, mel[mri][0], mel[mri][1]))

def init():
    pf('jUdon3.8')

def main():
    t = input()
    if t == '?':
        init()
    elif t == 'I':
        setup()
    elif 'G':
        play()

main()
