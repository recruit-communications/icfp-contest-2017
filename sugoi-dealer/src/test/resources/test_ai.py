import sys
from logging import DEBUG, getLogger, StreamHandler, Formatter, FileHandler, WARN

logger = getLogger("logger")
logger.setLevel(DEBUG)

handler1 = StreamHandler()
handler1.setFormatter(Formatter("%(asctime)s %(levelname)8s %(message)s"))

handler2 = FileHandler(filename="test.log")
handler2.setLevel(WARN)
handler2.setFormatter(Formatter("%(asctime)s %(levelname)8s %(message)s"))

logger.addHandler(handler1)
logger.addHandler(handler2)


def test():
    print("19:{\"me\": \"kenkoooo\"}")
    input()
    input()
    print("10:123456789")


if __name__ == '__main__':
    test()
