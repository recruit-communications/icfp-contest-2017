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
    C,P,F,S = LI()
    N, M, K = LI()
    pe(K=K)
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

    state = {
        'e': e,
        'ek': ek,
        'esa': esa,
        'P': P,
        'N': N,
        'M': M,
        'K': K,
        'C': C,
        'MI': MI,
        'te': te,
        'ufs': ufs,
    }
    #pe(state)

    pf(str(pickle.dumps(state)))
    # futureは未対応
    pf('0')

def play():
    s_time = time.time()
    state = input()
    state = pickle.loads(eval(state))
    e = state['e']
    ek = state['ek']
    esa = state['esa']
    P = state['P']
    N = state['N']
    M = state['M']
    K = state['K']
    C = state['C']
    MI = state['MI']
    te = state['te']
    ufs = state['ufs']
    pe(P=P,N=N,C=C)

    for i in range(C):
        na = LI()
        if na[0] < 1:
            continue
        pe(na=na)
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

    ss = [0 for _ in range(ell)]
    sc = [0 for _ in range(ell)]
    sr = [0 for _ in range(ell)]
    def playout():
        random.shuffle(el)
        nte = [set() for _ in range(C)]
        for i in range(ell):
            nte[(P+i) % C].add(el[i])
        r,s = res(nte)
        for nt in nte[P]:
            ei = eil[nt]
            ss[ei] += s
            sr[ei] += r
            sc[ei] += 1

    cnt = 0
    while time.time() - s_time < 0.9:
        cnt += 1
        playout()

    pe(cnt=cnt, time=time.time() - s_time)

    mr = inf
    ms = -1
    mri = -1
    for i in range(ell):
        if sc[i] < 1:
            continue
        er = 1.0 * sr[i] / sc[i]
        es = 1.0 * ss[i] / sc[i]
        if mr > er or (mr == er and es > ms):
            mr = er
            ms = es
            mri = i

    pf(str(pickle.dumps(state)))

    pe(mr=mr,ms=ms,el=mel[mri],len=ell)
    if mri == -1:
        pf('{}'.format(P))
    else:
        pf('{} {} {}'.format(P, mel[mri][0], mel[mri][1]))

def init():
    pf('jUdon1')

def main():
    t = input()
    if t == '?':
        init()
    elif t == 'I':
        setup()
    elif 'G':
        play()

main()
