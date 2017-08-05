#include <algorithm>
#include <utility>
#include <vector>
#include <bitset>
#include <string>
#include <iostream>
#include <sstream>
#include <array>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <cmath>
#include <sys/time.h>

using namespace std;
using u8 = uint8_t;
using u16 = uint16_t;
using u32 = uint32_t;
using u64 = uint64_t;
using i64 = int64_t;
using ll = int64_t;
using ull = uint64_t;
using vi = vector<int>;
using vvi = vector<vi>;

ll start_time, time_limit; // in msec

inline ll get_time() {
	struct timeval tv;
	gettimeofday(&tv, NULL);
	ll result =  tv.tv_sec * 1000LL + tv.tv_usec / 1000LL;
	return result;
}

inline ll get_elapsed_msec() {
	return get_time() - start_time;
}

#ifdef DEBUG
#define debug(format, ...) fprintf(stderr, format, __VA_ARGS__)
#define debugStr(str) fprintf(stderr, str)
#define debugln() fprintf(stderr, "\n")
#else
#define debug(format, ...)
#define debugStr(str)
#define debugln()
#endif

#ifndef PLAYER_NAME
#define PLAYER_NAME "tomerun"
#endif

const int NOT_OWNED = -1;

struct Edge {
	int to, owner;
};

bool operator<(const Edge& e1, const Edge& e2) {
	return e1.to < e2.to;
}

struct Game {

	int C, I, F, N, M, K;
	vector<vector<Edge>> edges;
	vector<int> mines;
	vector<i64> values;

	Game(bool original) {
		scanf("%d %d %d %d %d %d", &C, &I, &F, &N, &M, &K);
		edges.resize(N);
		values.resize(N);
		mines.resize(K);
		if (original) {
			for (int i = 0; i < M; ++i) {
				int s, t;
				scanf("%d %d", &s, &t);
				edges[s].push_back({t, NOT_OWNED});
				edges[t].push_back({s, NOT_OWNED});
			}
			for (int i = 0; i < N; ++i) {
				sort(edges[i].begin(), edges[i].end());
			}
			for (int i = 0; i < K; ++i) {
				scanf("%d", &mines[i]);
			}
		} else {
			for (int i = 0; i < N; ++i) {
				int ec;
				scanf("%d %d", &values[i], &ec);
				edges[i].resize(ec);
				for (int j = 0; j < ec; ++j) {
					scanf("%d %d", &edges[i][j].to, &edges[i][j].owner);
				}
			}
			for (int i = 0; i < K; ++i) {
				scanf("%d", &mines[i]);
			}
		}
	}

	string serialize() const {
		stringstream ss;
		ss << C << " " << I << " " << F << " " << N << " " << M << " " << K;
		for (int i = 0; i < N; ++i) {
			ss << " " << values[i] << " " << edges[i].size();
			for (const Edge& e : edges[i]) {
				ss << " " << e.to << " " << e.owner;
			}
		}
		for (int i = 0; i < K; ++i) {
			ss << " " << mines[i];
		}
		return ss.str();
	}

	void init() {
		values.resize(N);
		vi visited(N, 0);
		vi q;
		q.reserve(N);
		for (int i = 0; i < K; ++i) {
			q.clear();
			q.push_back(mines[i]);
			visited[mines[i]] |= (1 << i);
			int dist = 1;
			int dist_end = q.size();
			for (int j = 0; j < q.size(); ++j) {
				if (j == dist_end) {
					++dist;
					dist_end = q.size();
				}
				int s = q[j];
				for (const Edge& e : edges[s]) {
					int t = e.to;
					if (visited[t] & (1 << i)) continue;
					visited[t] |= (1 << i);
					values[t] += dist * dist;
					q.push_back(t);
				}
			}
		}
	}

	void use(int s, int t, int owner) {
		if (s == -1) return;
		for (int flip = 0; flip < 2; ++flip) {
			// TODO: binary search?
			vector<Edge>& es = edges[s];
			for (int i = 0; i < es.size(); ++i) {
				if (es[i].to == t && es[i].owner == NOT_OWNED) {
					es[i].owner = owner;
					break;
				}
			}
			swap(s, t);
		}
	}
};

pair<int, int> create_move(const Game& game) {
	return make_pair(-1, -1);
}

void handshake() {
	cout << PLAYER_NAME << endl;
}

void init() {
	time_limit  = 8000;
	Game game(true);
	game.init();
	string ser = game.serialize();
	cout << ser << "\n";
	int num_future = 0;
	cout << num_future << endl;
}

void move() {
	time_limit = 500;
	Game game(false);
	for (int i = 0; i < game.C; ++i) {
		int s, t;
		scanf("%d %d", &s, &i);
		game.use(s, t, i);
	}
	cout << game.serialize() << "\n";
	pair<int, int> res = create_move(game);
	cout << res.first << " " << res.second << endl;
}

int main() {
	start_time = get_time();
	string phase;
	cin >> phase;
	if (phase == "?") {
		handshake();
	} else if (phase == "I") {
		init();
	} else {
		move();
	}
}

