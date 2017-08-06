import json
import random
import sys


def solve(fn):
    with open(fn) as in_f:
        sys.stdin = in_f

        n, m = map(int, input().split())

        sites = []

        for i in range(n):
            x = random.random() * 1000
            y = random.random() * 1000
            sites.append({'id': i, 'x': x, 'y': y})

        rivers = []
        for i in range(m):
            u, v = map(int, input().split())
            rivers.append({'source': u, 'target': v})

        obj = {'sites': sites, 'rivers': rivers}

        for d in [50, 500]:
            k = (n + d - 1) // d

            if k > 1:
                mines = random.sample(range(n), k)
                obj['mines'] = mines
                ofn = 'json/%d_%d_%d.json' % (n, m, k)
                with open(ofn, 'w') as out_f:
                    json.dump(obj, out_f)


def main():
    for fn in sys.argv[1:]:
        solve(fn)


if __name__ == '__main__':
    main()
