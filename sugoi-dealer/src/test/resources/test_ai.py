import json
import sys
from logging import DEBUG, getLogger, StreamHandler, Formatter, FileHandler

logger = getLogger("logger")
logger.setLevel(DEBUG)

handler1 = StreamHandler()
handler1.setFormatter(Formatter("%(asctime)s %(levelname)8s %(message)s"))

handler2 = FileHandler(filename="test.log")
handler2.setLevel(DEBUG)
handler2.setFormatter(Formatter("%(asctime)s %(levelname)8s %(message)s"))

logger.addHandler(handler1)
logger.addHandler(handler2)


def write_obj(obj):
    d = json.dumps(obj) + "\n"
    b = d.encode()
    n = len(b)
    sys.stdout.buffer.write(str(n).encode())
    sys.stdout.buffer.write(":".encode())
    sys.stdout.buffer.write(b)
    sys.stdout.flush()


def read_json():
    buffer = b''
    while 1:
        idx = buffer.find(b':')
        if idx >= 0:
            break
        # まだ : がない
        buffer += sys.stdin.buffer.read1(1)
    n = int(buffer[:idx].decode())
    buffer = buffer[idx + 1:]

    while len(buffer) < n:
        # n 以上になるまで追加で読む
        buffer += sys.stdin.buffer.read1(1)

    print(buffer)


def test():
    write_obj({"me": "kenkoooo"})
    read_json()
    # input()
    read_json()
    write_obj(["aaa"])


if __name__ == '__main__':
    test()
