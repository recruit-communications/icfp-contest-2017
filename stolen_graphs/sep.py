import sys


def solve(fn):
    with open(fn) as in_f:
        sys.stdin = in_f
        t = int(input())

        for i in range(t):
            ofn = 'sep/' + fn + '_' + str(i)
            print(ofn)
            with open(ofn, 'w') as of:
                n, m = map(int, input().split())
                print(n, m, file=of)

                for j in range(m):
                    u, v, w = map(int, input().split())
                    print(u, v, file=of)
                s = int(input())


def main():
    solve(sys.argv[1])


if __name__ == '__main__':
    main()
