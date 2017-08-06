import sys
import telnetlib
import json


class OnlineBridge:
    def __init__(self, host, port):
        self.telnet = telnetlib.Telnet(host, port)
        self.buffer = b''

    def send_json(self, obj):
        d = json.dumps(obj)
        print('SEND', d)
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

        json_bytes = self.buffer[:n].decode()
        self.buffer = self.buffer[n:]
        obj = json.loads(json_bytes)
        print('RECV', obj)
        return obj

    def handshake(self, name):
        self.send_json({'me': name})
        reply = self.read_json()

    def setup(self):
        sup = self.read_json()
        self.punter_id = sup['punter']
        self.punters = sup['punters']
        self.map = sup['map']

    def ready(self):
        self.send_json({'ready': self.punter_id})

    def passmove(self):
        while 1:
            reply = self.read_json()
            if 'stop' in reply:
                break
            self.send_json({'pass': {'punter': self.punter_id}})


def main():
    host = 'punter.inf.ed.ac.uk'
    port = int(sys.argv[1])

    bridge = OnlineBridge(host, port)
    bridge.handshake('kenkoooo')
    bridge.setup()
    bridge.ready()

    bridge.passmove()


if __name__ == '__main__':
    main()
