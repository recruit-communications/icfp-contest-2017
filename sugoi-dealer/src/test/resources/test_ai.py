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


def test():
    write_obj({"me": "kenkoooo"})
    input()
    input()
    write_obj("aaa")


if __name__ == '__main__':
    test()
