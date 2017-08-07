import sys
import telnetlib
import json
import subprocess
import shlex

JUNCTION = False
LAST_INPUT = ''


def compress(sorted_site_ids):
    """
    >>> conv = compress([1, 3, 6, 10])
    >>> conv
    {1: 0, 3: 1, 6: 2, 10: 3}
    """
    return {sid: i for i, sid in enumerate(sorted_site_ids)}


class OfflineBridge:
    def __init__(self):
        self.buffer = b''
        self.future = 0
        self.splurge = 0
        self.option = 0

    def send_json(self, obj):
        d = json.dumps(obj) + '\n'
        qqq = d.encode()
        n = len(qqq)
        sys.stdout.buffer.write(str(n).encode())
        sys.stdout.buffer.write(':'.encode())
        sys.stdout.buffer.write(qqq)
        sys.stdout.flush()

    def read_json(self):
        while 1:
            idx = self.buffer.find(b':')
            if idx >= 0:
                break
            # まだ : がない
            self.buffer += sys.stdin.buffer.read1(100)
        n = int(self.buffer[:idx].decode())
        self.buffer = self.buffer[idx + 1:]

        while len(self.buffer) < n:
            # n 以上になるまで追加で読む
            self.buffer += sys.stdin.buffer.read1(100)

        json_bytes = self.buffer[:n].decode()
        self.buffer = self.buffer[n:]
        obj = json.loads(json_bytes)
        if 'timeout' in obj:
            return self.read_json()
        return obj

    def handshake(self, name):
        self.send_json({'me': name})
        self.read_json()

    def setup(self, obj):
        self.punter_id = obj['punter']
        self.punters = obj['punters']
        self.map = obj['map']
        if 'settings' in obj and 'futures' in obj['settings']:
            self.future = 1 if obj['settings']['futures'] else 0
        if 'settings' in obj and 'splurges' in obj['settings']:
            self.splurge = 1 if obj['settings']['splurges'] else 0
        if 'settings' in obj and 'options' in obj['settings']:
            self.option = 1 if obj['settings']['options'] else 0

    def ready(self, F, state, sorted_site_ids, proc_idx=-1):
        obj = {'ready': self.punter_id, 'state': [state, sorted_site_ids, proc_idx]}
        if F:
            futures = []
            for m, s in F:
                futures.append({'source': m, 'target': s})
            obj['futures'] = futures
        self.send_json(obj)

    def recmove(self, move):
        moves = move['move']['moves']
        state, sorted_site_ids, _ = move['state']

        G = []
        for move in moves:
            if 'claim' in move:
                claim = move['claim']
                pid = claim['punter']
                s = claim['source']
                t = claim['target']
                G.append((pid, [s, t]))
            elif 'pass' in move:
                pid = move['pass']['punter']
                G.append((pid, []))
            elif 'option' in move:
                option = move['option']
                pid = option['punter']
                s = option['source']
                t = option['target']
                G.append((pid, [s, t]))
            elif 'splurge' in move:
                splurge = move['splurge']
                pid = splurge['punter']
                route = splurge['route']
                G.append((pid, route))
        G.sort()
        return G, state, sorted_site_ids

    def sendmove(self, pid, vs, state, sorted_site_ids, proc_idx=-1):
        if len(vs) == 2:
            if vs[0] >= 0:
                obj = {'claim': {'punter': pid, 'source': vs[0], 'target': vs[1]}}
            else:
                obj = {'option': {'punter': pid, 'source': -(vs[0] + 1), 'target': -(vs[1] + 1)}}
        elif len(vs) == 0:
            obj = {'pass': {'punter': pid}}
        else:
            # splurge
            obj = {'splurge': {'punter': pid, 'route': vs}}
        obj['state'] = [state, sorted_site_ids, proc_idx]
        self.send_json(obj)


class OnlineBridge:
    def __init__(self, host, port):
        self.telnet = telnetlib.Telnet(host, port)
        self.buffer = b''
        self.future = 0
        self.splurge = 0
        self.option = 0

    def close(self):
        self.telnet.close()

    def send_json(self, obj):
        d = json.dumps(obj)
        print('SEND', d.rstrip())
        bytes = d.encode()
        n = len(bytes)
        self.telnet.write(str(n).encode())
        self.telnet.write(':'.encode())
        self.telnet.write(bytes)

    def read_json(self):
        while 1:
            idx = self.buffer.find(b':')
            if idx >= 0:
                break
            # まだ : がない
            self.buffer += self.telnet.read_some()
        n = int(self.buffer[:idx].decode())
        self.buffer = self.buffer[idx + 1:]

        while len(self.buffer) < n:
            # n 以上になるまで追加で読む
            self.buffer += self.telnet.read_some()

        json_string = self.buffer[:n].decode().rstrip()
        print('RECV', json_string.rstrip())
        self.buffer = self.buffer[n:]
        obj = json.loads(json_string)
        return obj

    def handshake(self, name):
        self.send_json({'me': name})
        self.read_json()

    def setup(self):
        sup = self.read_json()
        self.punter_id = sup['punter']
        self.punters = sup['punters']
        self.map = sup['map']
        if 'settings' in sup and 'futures' in sup['settings']:
            self.future = 1 if sup['settings']['futures'] else 0
        if 'settings' in sup and 'splurge' in sup['settings']:
            self.splurge = 1 if sup['settings']['splurge'] else 0
        if 'settings' in sup and 'options' in sup['settings']:
            self.option = 1 if sup['settings']['options'] else 0

    def ready(self, F):
        obj = {'ready': self.punter_id}
        if F:
            futures = []
            for m, s in F:
                futures.append({'source': m, 'target': s})
            obj['futures'] = futures
        self.send_json(obj)

    def recmove(self):
        move = self.read_json()
        if 'stop' in move:
            return None
        moves = move['move']['moves']
        G = []
        for move in moves:
            if 'claim' in move:
                claim = move['claim']
                pid = claim['punter']
                s = claim['source']
                t = claim['target']
                G.append((pid, [s, t]))
            elif 'pass' in move:
                pid = move['pass']['punter']
                G.append((pid, []))
            elif 'option' in move:
                option = move['option']
                pid = option['punter']
                s = option['source']
                t = option['target']
                G.append((pid, [s, t]))
            elif 'splurge' in move:
                splurge = move['splurge']
                pid = splurge['punter']
                route = splurge['route']
                G.append((pid, route))
        G.sort()
        return G

    def sendmove(self, vs, state, sorted_site_ids):
        if len(vs) == 2:
            if vs[0] >= 0:
                obj = {'claim': {'punter': self.punter_id, 'source': vs[0], 'target': vs[1]}}
            else:
                obj = {'option': {'punter': self.punter_id, 'source': -(vs[0] + 1), 'target': -(vs[1] + 1)}}
        elif len(vs) == 0:
            obj = {'pass': {'punter': self.punter_id}}
        else:
            # splurge
            obj = {'splurge': {'punter': self.punter_id, 'route': vs}}
        self.send_json(obj)


class Process:
    def __init__(self, cmd):
        self.cmdline = shlex.split(cmd)

    def exec(self, input):
        proc = subprocess.Popen(self.cmdline, stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.PIPE)
        global LAST_INPUT
        LAST_INPUT = input
        stdout, stderr = proc.communicate(input=input.encode())
        print(stderr.decode().rstrip(), file=sys.stderr)
        return stdout.decode().rstrip()

    def handshake(self):
        return self.exec('?\n')

    def I(self, num_players, punter_id, future, splurge, option, ma):
        n = len(ma['sites'])
        m = len(ma['rivers'])
        k = len(ma['mines'])

        site_ids = []
        for site in ma['sites']:
            site_ids.append(site['id'])
        site_ids.sort()
        conv = compress(site_ids)

        S = []
        T = []
        for r in ma['rivers']:
            s = conv[r['source']]
            t = conv[r['target']]
            S.append(s)
            T.append(t)
        M = [conv[m] for m in ma['mines']]

        lines = ['I', ' '.join(map(str, [num_players, punter_id, future, splurge, option])),
                 ' '.join(map(str, [n, m, k]))]
        for i in range(m):
            lines.append(' '.join(map(str, [S[i], T[i]])))
        lines.append(' '.join(map(str, M)))
        txt = '\n'.join(lines)
        sorted_site_ids = site_ids
        ret = self.exec(txt)
        splited = ret.split('\n')
        state = splited[0]
        num_f = int(splited[1])
        F = []
        for i in range(2, len(splited)):
            m, s = map(int, splited[i].split())
            m = sorted_site_ids[m]
            s = sorted_site_ids[s]
            F.append((m, s))
        return state, sorted_site_ids, F

    def G(self, G, state, sorted_site_ids):
        conv = compress(sorted_site_ids)
        lines = ['G', state]
        for pid, vs in G:
            lis = [len(vs)]
            for v in vs:
                lis.append(conv[v])
            lines.append(' '.join(map(str, lis)))
        txt = '\n'.join(lines)
        recv = self.exec(txt)
        new_state, vline = recv.split('\n')
        pid_vs = list(map(int, vline.split(' ')))
        pid = pid_vs[0]

        vs = []
        for v in pid_vs[1:]:
            if v >= 0:
                vs.append(sorted_site_ids[v])
            else:
                vs.append(-(sorted_site_ids[-(v + 1)] + 1))
        return pid, vs, new_state


def offline():
    cmd = sys.argv[1]
    name = sys.argv[2]
    proc = Process(cmd)

    bridge = OfflineBridge()
    bridge.handshake(name)

    obj = bridge.read_json()

    if 'punter' in obj:
        # Setup
        bridge.setup(obj)
        state, sorted_site_ids, futures = proc.I(bridge.punters, bridge.punter_id, bridge.future, bridge.splurge,
                                                 bridge.option, bridge.map)
        bridge.ready(futures, state, sorted_site_ids)
    elif 'move' in obj:
        # Gameplay
        G, state, sorted_site_ids = bridge.recmove(obj)
        pid, vs, state = proc.G(G, state, sorted_site_ids)
        bridge.sendmove(pid, vs, state, sorted_site_ids)
    elif 'stop' in obj:
        # Scoreing
        pass


def online():
    host = 'punter.inf.ed.ac.uk'
    cmd = sys.argv[1]
    name = sys.argv[2]
    port = int(sys.argv[3])

    proc = Process(cmd)

    bridge = OnlineBridge(host, port)
    bridge.handshake(name)
    bridge.setup()

    state, sorted_site_ids, futures = proc.I(bridge.punters, bridge.punter_id, bridge.future, bridge.splurge,
                                             bridge.option, bridge.map)
    bridge.ready(futures)

    while 1:
        G = bridge.recmove()
        if G is None:
            break
        pid, vs, state = proc.G(G, state, sorted_site_ids)
        bridge.sendmove(vs, state, sorted_site_ids)
    bridge.close()


def decision(bridge):
    """
    bridge を受けて、何番のAIが処理を担当するかを決定する。
    ここの決定にかかる時間は、初期化の10秒に含まれる
    """

    future = bridge.future
    punter_id = bridge.punter_id
    punter_count = bridge.punters
    ma = bridge.map
    n = len(ma['sites'])
    m = len(ma['rivers'])
    k = len(ma['mines'])

    return 0


def junction():
    name = sys.argv[1]
    cmds = sys.argv[2:]

    procs = [Process(cmd) for cmd in cmds]
    # warmup?

    bridge = OfflineBridge()

    bridge.handshake(name)
    obj = bridge.read_json()

    proc = None

    if 'punter' in obj:
        # Setup
        # まだ実行プログラム未定
        bridge.setup(obj)
        proc_idx = decision(bridge)
        proc = procs[proc_idx]
        state, sorted_site_ids, futures = proc.I(bridge.punters, bridge.punter_id, bridge.future, bridge.splurge,
                                                 bridge.option, bridge.map)
        bridge.ready(futures, state, sorted_site_ids, proc_idx)
    elif 'move' in obj:
        # Gameplay
        proc_idx = obj['state'][2]
        proc = procs[proc_idx]
        G, state, sorted_site_ids = bridge.recmove(obj)
        pid, vs, state = proc.G(G, state, sorted_site_ids)
        bridge.sendmove(pid, vs, state, sorted_site_ids, proc_idx)
    elif 'stop' in obj:
        # Scoreing
        pass

    for p in procs:
        if p != proc:
            p.handshake()


if __name__ == '__main__':
    try:
        if JUNCTION:
            junction()
        elif len(sys.argv) == 3:
            offline()
        elif len(sys.argv) == 4:
            online()
    except:
        print(LAST_INPUT, file=sys.stderr)
        raise
