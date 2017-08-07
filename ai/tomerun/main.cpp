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

const int NOT_OWNED = -1;
const i64 SCORE_SCALE = 1000000;

struct Edge {
	int to, owner, option_owner;
};

struct Game {

	int turn;
	int C, I, F, S, O, N, M, K;
	vector<vector<Edge>> edges;
	vi mines;
	vi option_counts;
	vvi dists;

	Game(bool original) {
		scanf("%d %d %d %d %d %d %d %d", &C, &I, &F, &S, &O, &N, &M, &K);
		edges.resize(N);
		mines.resize(K);
		option_counts.resize(C);
		if (original) {
			turn = 0;
			for (int i = 0; i < M; ++i) {
				int s, t;
				scanf("%d %d", &s, &t);
				edges[s].push_back({t, NOT_OWNED, NOT_OWNED});
				if (s != t) edges[t].push_back({s, NOT_OWNED, NOT_OWNED});
			}
			for (int i = 0; i < K; ++i) {
				scanf("%d", &mines[i]);
			}
			option_counts.assign(C, O ? K : 0);
		} else {
			scanf("%d", &turn);
			for (int i = 0; i < N; ++i) {
				int ec;
				scanf("%d", &ec);
				edges[i].resize(ec);
				for (int j = 0; j < ec; ++j) {
					scanf("%d %d %d", &edges[i][j].to, &edges[i][j].owner, &edges[i][j].option_owner);
				}
			}
			for (int i = 0; i < K; ++i) {
				scanf("%d", &mines[i]);
			}
			for (int i = 0; i < C; ++i) {
				scanf("%d", &option_counts[i]);
			}
			calc_mine_dists();
		}
	}

	string serialize() const {
		stringstream ss;
		ss << C << " " << I << " " << F << " " << S << " " << O << " " << N << " " << M << " " << K << " " << (turn + 1);
		for (int i = 0; i < N; ++i) {
			ss << " " << edges[i].size();
			for (const Edge& e : edges[i]) {
				ss << " " << e.to << " " << e.owner << " " << e.option_owner;
			}
		}
		for (int i = 0; i < K; ++i) {
			ss << " " << mines[i];
		}
		for (int i = 0; i < K; ++i) {
			ss << " " << option_counts[i];
		}
		return ss.str();
	}

	void use(int s, int t, int owner) {
		if (s == -1) return;
		for (int flip = 0; flip < 2; ++flip) {
			vector<Edge>& es = edges[s];
			for (int i = 0; i < es.size(); ++i) {
				if (es[i].to == t) {
					if (es[i].owner == NOT_OWNED) {
						es[i].owner = owner;
						break;
					} else if (es[i].option_owner == NOT_OWNED) {
						es[i].option_owner = owner;
						break;
					}
				}
			}
			swap(s, t);
		}
	}

	void calc_mine_dists() {
		vi q;
		q.reserve(N);
		dists.assign(K, vector<int>(N));
		for (int i = 0; i < K; ++i) {
			vector<bool> visited(N);
			q.clear();
			q.push_back(mines[i]);
			visited[mines[i]] = true;
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
					if (visited[t]) continue;
					visited[t] = true;
					dists[i][t] = dist;
					q.push_back(t);
				}
			}
		}
	}

	pair<int, int> create_move() const {
		vector<int> orders(N);
		for (int i = 0; i < N; ++i) {
			for (const Edge& e : edges[i]) {
				if (e.owner == NOT_OWNED) orders[i]++;
			}
		}

		vi reachable(N); // i-th bit of reachable[j] := vertex j is reachable from mine i
		vector<bool> candidate_to(N);
		vi approach_node_score(N);
		for (int i = 0; i < K; ++i) {
			vector<int> q = {mines[i]};
			reachable[q[0]] |= (1 << i);
			for (int j = 0; j < q.size(); ++j) {
				for (const Edge& e : edges[q[j]]) {
					if (e.owner == NOT_OWNED && (reachable[e.to] & (1 << i)) == 0) {
						candidate_to[q[j]] = true;
					} else if (e.owner == I && (reachable[e.to] & (1 << i)) == 0) {
						q.push_back(e.to);
						reachable[e.to] |= (1 << i);
					}
				}
			}
			int dist = 1;
			int dist_end = q.size();
			vi visited(reachable);
			for (int j = 0; j < q.size(); ++j) {
				if (j == dist_end) {
					dist++;
					dist_end = q.size();
				}
				for (const Edge& e : edges[q[j]]) {
					if (e.owner == NOT_OWNED && (visited[e.to] & (1 << i)) == 0) {
						visited[e.to] |= (1 << i);
						approach_node_score[e.to] += SCORE_SCALE / (dist + 0.1);
						q.push_back(e.to);
					}
				}
			}
		}

		vector<pair<int, const Edge*>> cand_edges;
		vector<i64> connect_score;
		vector<i64> order_score;
		vector<i64> expand_score;
		vector<i64> approach_score;

		for (int i = 0; i < N; ++i) {
			if (!candidate_to[i]) continue;
			for (const Edge& e : edges[i]) {
				if (e.owner != NOT_OWNED) continue;
				if (reachable[i] == reachable[e.to]) continue;
				cand_edges.push_back(make_pair(i, &e));
				connect_score.push_back(__builtin_popcount(~reachable[i] & reachable[e.to]) * SCORE_SCALE);
				order_score.push_back(SCORE_SCALE * orders[e.to]);
				expand_score.push_back(0);
				approach_score.push_back(0);
				for (int j = 0; j < K; ++j) {
					if (reachable[i] & (1 << j)) {
						expand_score.back() += dists[j][e.to] * dists[j][e.to] * SCORE_SCALE;
					} else {
						approach_score.back() += approach_node_score[e.to];
					}
				}
			}
		}

		if (cand_edges.empty()) {
			for (int i = 0; i < N; ++i) {
				for (const Edge& e : edges[i]) {
					if (e.owner == NOT_OWNED) {
						return make_pair(i, e.to);
					}
				}
			}
			return make_pair(-1, -1);
		}

		vector<pair<i64, int>> scores(cand_edges.size());
		vi weights;
		if (turn < M / C / 3) {
			weights = {50, 1, 0, 5}; // in early phase, prior connecting mines
		} else {
			weights = {5, 2, 1, 3};
		}
		for (int i = 0; i < cand_edges.size(); ++i) {
			// cerr << "(" << cand_edges[i].first << " " << cand_edges[i].second->to << ") ";
			// cerr << connect_score[i] << " " << order_score[i] << " " << expand_score[i] << " " << approach_score[i] << endl;
			scores[i].first = connect_score[i] * weights[0] + order_score[i] * weights[1] + expand_score[i] * weights[2] + approach_score[i] * weights[3];
			scores[i].second = i;
		}
		int idx = max_element(scores.begin(), scores.end())->second;
		return make_pair(cand_edges[idx].first, cand_edges[idx].second->to);
	}
};

void handshake() {
	cout << "nihonbashi" << endl;
}

void init() {
	time_limit  = 8000;
	Game game(true);
	string ser = game.serialize();
	cout << ser << "\n";
	int num_future = 0;
	cout << num_future << endl;
	debug("init:%d\n", get_elapsed_msec());
}

void move() {
	time_limit = 500;
	Game game(false);
	for (int i = 0; i < game.C; ++i) {
		int c;
		scanf("%d", &c);
		if (c == 0) continue;
		vi path(c);
		for (int j = 0; j < c; ++j) {
			scanf("%d", &path[j]);
			if (j > 0) game.use(path[j - 1], path[j], i);
		}
	}
	cout << game.serialize() << "\n";
	pair<int, int> res = game.create_move();
	cout << game.I << " ";
	cout << res.first << " " << res.second << endl;
	debug("move:%d\n", get_elapsed_msec());
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

