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


def setup():
    C,P,F = LI()
    N, M, K = LI()
    e = {}
    ee = {}
    for _ in range(M):
        a,b = LI()
        if a not in e:
            e[a] = set()
        if b not in e:
            e[b] = set()
        if a not in ee:
            ee[a] = set()
        e[a].add(b)
        e[b].add(a)
        ee[a].add(b)

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
        'ee': ee,
        'ek': ek,
        'es': es,
        'esa': esa,
        'P': P,
        'N': N,
        'M': M,
        'C': C,
    }
    #pe(state)

    pf(str(pickle.dumps(state)))
    # futureは未対応
    pf('0 ')

def play():
    state = input()
    state = pickle.loads(eval(state))
    e = state['e']
    ee = state['ee']
    ek = state['ek']
    es = state['es']
    esa = state['esa']
    P = state['P']
    N = state['N']
    M = state['M']
    C = state['C']
    #pe(ee=ee)
    pe(le=sum([len(_) for _ in ee.values()]))
    pe(P=P,N=N,C=C)
    for i in range(C):
        s,t = LI()
        if s == -1:
            # passの場合
            continue
        if t in ee[s]:
            ee[s].remove(t)
    r = None
    for s in esa[:M] + esa[::-1]:
        if r:
            break
        if s not in ee:
            continue
        el = list(ee[s])
        random.shuffle(el)
        for v in el:
            if r:
                break
            if v not in esa or True:
                r = '{} {}'.format(s,v)
                esa.append(v)
                break

    for k,v in ee.items():
        if r:
            break
        if v:
            v1 = list(v)[0]
            r = '{} {}'.format(k,v1)
            ee[k].remove(v1)
    pe(le2=sum([len(_) for _ in ee.values()]))

    pf(str(pickle.dumps(state)))

    if r:
        pe(r=r)
        pf(r)
    else:
        pf('-1 -1')

def init():
    pf('iUdon1')

def main():
    t = input()
    if t == '?':
        init()
    elif t == 'I':
        setup()
    elif 'G':
        play()

main()
