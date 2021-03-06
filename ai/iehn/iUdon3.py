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

DEBUG = False

def pe(*a, **b):
    if not DEBUG:
        return
    with open('/tmp/t.log', 'a') as f:
        if a:
            f.write(str(a) + '\n')
        if b:
            s = ''
            for k, v in b.items():
                if s:
                    s += ', '
                s += '{}={}'.format(k,v)
            f.write(s + '\n')
    return

def pf(s):
    print(s, flush=True)

class UnionFind:
    def __init__(self, size):
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
    C,P,F = LI()
    N, M, K = LI()
    pe(K=K)
    e = {}
    for _ in range(M):
        a,b = LI()
        if a not in e:
            e[a] = set()
        if b not in e:
            e[b] = set()
        e[a].add(b)
        e[b].add(a)

    max_i = max(e.keys())
    uf = UnionFind(max_i + 1)

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

            for uv in e[u]:
                if v[uv]:
                    continue
                vd = k + 1
                if uv not in d:
                    d[uv] = inf
                if d[uv] > vd:
                    d[uv] = vd
                    heapq.heappush(q, (vd, uv))

        return d

    ek = {}
    es = {}
    esa = LI()
    for m in esa:
        ek[m] = search(m)
        es[m] = set([m])

    state = {
        'e': e,
        'ek': ek,
        'es': es,
        'esa': esa,
        'P': P,
        'N': N,
        'M': M,
        'K': K,
        'C': C,
        'uf': uf,
    }
    #pe(state)

    pf(str(pickle.dumps(state)))
    # futureは未対応
    pf('0')

def play():
    state = input()
    state = pickle.loads(eval(state))
    e = state['e']
    ek = state['ek']
    es = state['es']
    esa = state['esa']
    P = state['P']
    N = state['N']
    M = state['M']
    K = state['K']
    C = state['C']
    uf = state['uf']
    pe(P=P,N=N,C=C)

    def fr(s,v):
        if v in e and s in e[v]:
            e[v].remove(s)
        if s in e and v in e[s]:
            e[s].remove(v)
        esa.append(v)
        uf.union(s,v)
        return '{} {}'.format(s,v)

    for i in range(C):
        s,t = LI()
        if s == -1:
            # passの場合
            continue
        if s in e and t in e[s]:
            e[s].remove(t)
        if t in e and s in e[t]:
            e[t].remove(s)

    r = None
    esam = esa[:K]
    ess = set(esa)
    et = []
    for s in esam:
        if r:
            break
        for v in list(e[s]):
            if uf.find(s) == uf.find(v):
                continue
            if uf.table[s] != -1 and uf.table[v] != -1:
                r = fr(s,v)
                break
            et.append([len(e[v] - ess) + 2, -len(e[s]), (s,v)])

    esas = esa[K:]
    for s in esas:
        if r:
            break
        for v in list(e[s]):
            if uf.find(s) == uf.find(v):
                continue
            if uf.table[s] != -1 and uf.table[v] != -1:
                r = fr(s,v)
                break
            et.append([len(e[v] - ess), -len(e[s]), (s,v)])

    if not r and et:
        et.sort()
        s,v = et[-1][2]
        r = fr(s,v)

    for k,v in e.items():
        if r:
            break
        if not v:
            continue
        el = list(v)
        random.shuffle(el)
        r = fr(k,el[0])

    pe(uf=uf.table)

    pf(str(pickle.dumps(state)))

    if r:
        pe(r=r)
        pf('{} '.format(P) + r)
    else:
        pf('{} '.format(P) + '-1 -1')

def init():
    pf('iUdon2')

def main():
    t = input()
    if t == '?':
        init()
    elif t == 'I':
        setup()
    elif 'G':
        play()

main()
