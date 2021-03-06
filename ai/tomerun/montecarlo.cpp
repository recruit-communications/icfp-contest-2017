#include <algorithm>
#include <utility>
#include <vector>
#include <bitset>
#include <string>
#include <iostream>
#include <sstream>
#include <array>
#include <unordered_set>
#include <unordered_map>
#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <cmath>
#include <sys/time.h>
// #define MEASURE_TIME

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

const double CLOCK_PER_SEC = 2.2e9;

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

inline ull get_tsc() {
#ifdef __i386
  ull ret;
  __asm__ volatile ("rdtsc" : "=A" (ret));
  return ret;
#else
  ull high,low;
  __asm__ volatile ("rdtsc" : "=a" (low), "=d" (high));
  return (high << 32) | low;
#endif
}

struct Timer {
	vector<ull> at;
	vector<ull> sum;

	void start(int i) {
		if (i >= at.size()) {
			at.resize(i+1);
			sum.resize(i+1);
		}
		at[i] = get_tsc();
	}

	void stop(int i) {
		sum[i] += (get_tsc() - at[i]);
	}

	void print() {
		cerr << "timer:[";
		for (int i = 0; i < at.size(); ++i) {
			cerr << (int)(sum[i] / CLOCK_PER_SEC * 1000) << ", ";
			if (i % 10 == 9) cerr << endl;
		}
		cerr << "]" << endl;
	}
};
Timer timer;

#ifdef MEASURE_TIME
#define START_TIMER(i) (timer.start(i))
#define STOP_TIMER(i) (timer.stop(i))
#define PRINT_TIMER() (timer.print())
#else
#define START_TIMER(i)
#define STOP_TIMER(i)
#define PRINT_TIMER()
#endif

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

struct XorShift {
	uint32_t x,y,z,w;
	static const double TO_DOUBLE;

	XorShift() {
		x = 123456789;
		y = 362436069;
		z = 521288629;
		w = 88675123;
	}

	uint32_t nextUInt(uint32_t n) {
		uint32_t t = x ^ (x << 11);
		x = y;
		y = z;
		z = w;
		w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));
		return w % n;
	}

	uint32_t nextUInt() {
		uint32_t t = x ^ (x << 11);
		x = y;
		y = z;
		z = w;
		return w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));
	}

	double nextDouble() {
		return nextUInt() * TO_DOUBLE;
	}
};
const double XorShift::TO_DOUBLE = 1.0 / (1LL << 32);

struct Edge {
	int from, to, owner, option_owner;
};

struct Game {

	int turn;
	int C, I, F, S, O, N, M, K;
	vector<Edge> all_edges;
	vector<vector<Edge*>> edges;
	vi mines;
	vi option_counts;
	vvi dists; // dists[i][j] := distance from mine i to vertex j

	Game(bool original) {
		scanf("%d %d %d %d %d %d %d %d", &C, &I, &F, &S, &O, &N, &M, &K);
		edges.resize(N);
		all_edges.resize(M);
		mines.resize(K);
		option_counts.resize(C);
		if (original) {
			turn = 0;
			for (int i = 0; i < M; ++i) {
				scanf("%d %d", &all_edges[i].from, &all_edges[i].to);
				all_edges[i].owner = NOT_OWNED;
				all_edges[i].option_owner = NOT_OWNED;
				edges[all_edges[i].from].push_back(&all_edges[i]);
				if (all_edges[i].from != all_edges[i].to) edges[all_edges[i].to].push_back(&all_edges[i]);
			}
			for (int i = 0; i < K; ++i) {
				scanf("%d", &mines[i]);
			}
			option_counts.assign(C, O ? K : 0);
		} else {
			scanf("%d", &turn);
			for (int i = 0; i < M; ++i) {
				scanf("%d %d %d %d", &all_edges[i].from, &all_edges[i].to, &all_edges[i].owner, &all_edges[i].option_owner);
				edges[all_edges[i].from].push_back(&all_edges[i]);
				if (all_edges[i].from != all_edges[i].to) edges[all_edges[i].to].push_back(&all_edges[i]);
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
		for (const Edge& e : all_edges) {
			ss << " " << e.from << " " << e.to << " " << e.owner << " " << e.option_owner;
		}
		for (int i = 0; i < K; ++i) {
			ss << " " << mines[i];
		}
		for (int i = 0; i < K; ++i) {
			ss << " " << option_counts[i];
		}
		return ss.str();
	}

	void use(int player_id, int s, int t, int owner) {
		if (s == -1) return;
		vector<Edge*>& es = edges[s];
		for (int i = 0; i < es.size(); ++i) {
			if (es[i]->from + es[i]->to - s == t) {
				if (es[i]->owner == NOT_OWNED) {
					es[i]->owner = owner;
					break;
				} else if (es[i]->option_owner == NOT_OWNED) {
					es[i]->option_owner = owner;
					option_counts[player_id]--;
					break;
				}
			}
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
				for (const Edge* e : edges[s]) {
					int t = e->from + e->to - s;
					if (visited[t]) continue;
					visited[t] = true;
					dists[i][t] = dist;
					q.push_back(t);
				}
			}
		}
	}
};

struct MCTS {
	struct PlayerState {
		vector<i64> reachable;
		vector<Edge*> candidate_edge;
		vector<Edge*> random_edge;
		vector<Edge*> selected_edge;
		int prev_from, prev_to;
		int random_edge_idx;
		int option_count;
	};

	XorShift rnd;
	int C, I, F, N, M, K;
	vector<Edge> all_edges;
	vector<vector<Edge*>> edges;
	vector<int> mines;
	vector<PlayerState> states_orig, states;
	const vvi& dists;
	int turn;
	vi buf;
	unordered_map<Edge*, pair<double, double>> values;

	MCTS(const Game& game) : C(game.C), I(game.I), F(game.F), N(game.N), M(game.M), K(game.K),
		                       all_edges(game.all_edges), edges(game.edges), mines(game.mines), dists(game.dists) {
		turn = M - ((game.turn - 1) * C + I + 1);
		states_orig.resize(C);
		states.resize(C);
		for (int player = 0; player < C; ++player) {
			states_orig[player].reachable.resize(N);
			states_orig[player].option_count = game.option_counts[player];
			vi q;
			for (int mine = 0; mine < K; ++mine) {
				q.clear();
				q.push_back(mines[mine]);
				states_orig[player].reachable[mines[mine]] |= (1LL << mine);
				for (int i = 0; i < q.size(); ++i) {
					for (Edge* e : edges[q[i]]) {
						if (e->owner == NOT_OWNED) states_orig[player].candidate_edge.push_back(e);
						if (e->owner != player) continue;
						int t = e->from + e->to - q[i];
						if (states_orig[player].reachable[t] & (1LL << mine)) continue;
						states_orig[player].reachable[t] |= (1LL << mine);
						q.push_back(t);
					}
				}
			}
		}
		for (Edge& e : all_edges) {
			if (e.owner == NOT_OWNED) {
				for (int j = 0; j < C; ++j) {
					states_orig[j].random_edge.push_back(&e);
				}
			}
		}
	}

	Edge* select_move(PlayerState& st) {
		const int EXPAND_PREV = 0, RANDOM_NODE = 1, RANDOM_EDGE = 2, EXPAND_PREV_PREV = 3;
		int strategy = RANDOM_EDGE;
		if (st.prev_to == -1 || st.reachable[st.prev_to] == 0) {
			strategy = (rnd.nextUInt() & 0x330) ? RANDOM_NODE : RANDOM_EDGE;
		} else {
			int rv = rnd.nextUInt(32);
			if (rv < 16) {
				strategy = EXPAND_PREV;
			} else if (rv < 28) {
				strategy = RANDOM_NODE;
			} else {
				strategy = RANDOM_EDGE;
			}
		}
		if (strategy == EXPAND_PREV) {
			const int size = edges[st.prev_to].size();
			int ei = rnd.nextUInt(size);
			for (int i = 0; i < size; ++i) {
				Edge* e = edges[st.prev_to][(i + ei) % size];
				if (e->owner == NOT_OWNED && st.reachable[st.prev_to] != st.reachable[e->to]) return e;
			}
			strategy = (rnd.nextUInt() & 0x800) ? EXPAND_PREV_PREV : RANDOM_NODE;
		}
		if (strategy == EXPAND_PREV_PREV) {
			const int size = edges[st.prev_from].size();
			int ei = rnd.nextUInt(size);
			for (int i = 0; i < size; ++i) {
				Edge* e = edges[st.prev_from][(i + ei) % size];
				if (e->owner == NOT_OWNED && st.reachable[st.prev_from] != st.reachable[e->to]) return e;
			}
			strategy = RANDOM_NODE;
		}
		if (strategy == RANDOM_NODE) {
			for (int i = 0; i < st.candidate_edge.size(); ++i) {
				int pos = rnd.nextUInt(st.candidate_edge.size() - i);
				swap(st.candidate_edge[i], st.candidate_edge[i + pos]);
				Edge* edge = st.candidate_edge[i];
				if (edge->owner != NOT_OWNED) {
					swap(st.candidate_edge[i], st.candidate_edge.back());
					st.candidate_edge.pop_back();
					--i;
					continue;
				}
				const int s = edge->from;
				const int t = edge->to;
				if (st.reachable[s] != st.reachable[t]) {
					return edge;
				}
			}
			strategy = RANDOM_EDGE;
		}
		if (strategy == RANDOM_EDGE) {
			while (st.random_edge[st.random_edge_idx]->owner != NOT_OWNED) {
				st.random_edge_idx++;
			}
			return st.random_edge[st.random_edge_idx];
		}
		return nullptr;
	}

	void diffuse(int player_id, int from, int to) {
		PlayerState& st = states[player_id];
		if ((st.reachable[from] & ~st.reachable[to]) == 0) return;
		buf.clear();
		buf.push_back(to);
		if (st.reachable[to] == 0) {
			for (Edge* e : edges[to]) {
				if (e->owner == NOT_OWNED) st.candidate_edge.push_back(e);
			}
		}
		st.reachable[to] |= st.reachable[from];
		for (int i = 0; i < buf.size(); ++i) {
			int s = buf[i];
			for (Edge* e : edges[s]) {
				if (e->owner != player_id) continue;
				int t = e->from + e->to - s;
				if ((st.reachable[from] & ~st.reachable[t]) == 0) continue;
				buf.push_back(t);
				if (st.reachable[t] == 0) {
					for (Edge* ne : edges[t]) {
						if (ne->owner == NOT_OWNED) st.candidate_edge.push_back(ne);
					}
				}
				st.reachable[t] |= st.reachable[from];
			}
		}
	}

	void playout() {
		for (int i = 0; i < C; ++i) {
			states[i] = states_orig[i];
			states[i].prev_from = states[i].prev_to = -1;
			states[i].random_edge_idx = 0;
		}
		for (int i = 0; i < C; ++i) {
			int size = states[i].random_edge.size();
			for (int j = 0; j < size; ++j) {
				int pos = rnd.nextUInt(size - j);
				swap(states[i].random_edge[j], states[i].random_edge[j + pos]);
			}
		}
		int player_id = I;
		for (int i = 0; i < turn; ++i, ++player_id) {
			if (player_id == C) player_id = 0;
			PlayerState& st = states[player_id];
			Edge* e = select_move(st);
			st.selected_edge.push_back(e);
			if (st.prev_to == e->from) {
				st.prev_from = st.prev_to;
				st.prev_to = e->to;
			} else if (st.prev_to == e->to) {
				st.prev_from = st.prev_to;
				st.prev_to = e->from;
			} else {
				st.prev_from = e->from;
				st.prev_to = e->to;
			}
			diffuse(player_id, e->from, e->to);
			diffuse(player_id, e->to, e->from);
		}
		vector<i64> score(C);
		for (int player = 0; player < C; ++player) {
			for (int i = 0; i < N; ++i) {
				i64 r = states[player].reachable[i];
				while (r != 0) {
					int mine_id = __builtin_ctzll(r);
					score[player] += (i64)dists[mine_id][i] * dists[mine_id][i];
					r &= r -1;
				}
			}
		}
		for (Edge* e : states_orig[0].random_edge) {
			e->owner = NOT_OWNED;
		}
		i64 my_score = score[I];
		i64 value = my_score - *max_element(score.begin(), score.end());
		if (value == 0) {
			score[I] = 0;
			value += (my_score - *max_element(score.begin(), score.end())) / 10;
		}
		double weight = 1.0;
		for (Edge* e : states[I].selected_edge) {
			if (values.find(e) == values.end()) {
				values[e] = make_pair(weight, value * weight);
			} else {
				auto& v = values[e];
				v.first += weight;
				v.second += value * weight;
			}
			weight *= 0.9;
		}
	}

	pair<int, int> best_move() {
		double best_value = -99999999;
		Edge* ret = nullptr;
		for (auto& vp : values) {
			// cerr << vp.first->from << " " << vp.first->to << " " << vp.second.first << " " << vp.second.second << endl;
			if (vp.second.first < 0.001) continue;
			double v = 1.0 * vp.second.second / vp.second.first;
			if (v > best_value) {
				best_value = v;
				ret = vp.first;
			}
		}
		debug("value:%.4f\n", best_value);
		if (!ret) {
			for (Edge& e : all_edges) {
				if (e.owner == NOT_OWNED) {
					ret = &e;
				}
			}
		}
		return ret ? make_pair(ret->from, ret->to) : make_pair(-1, -1);
	}
};

pair<int, int> create_move(const Game& game) {
	MCTS mcts(game);
	for (int i = 0; ; ++i) {
		if (get_elapsed_msec() > time_limit) {
			debug("monte carlo trial:%d\n", i);
			break;
		}
		mcts.playout();
	}
	return mcts.best_move();
}

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
			if (j > 0) game.use(i, path[j - 1], path[j], i);
		}
	}
	cout << game.serialize() << "\n";
	pair<int, int> res = create_move(game);
	cout << game.I << " ";
	cout << res.first << " " << res.second << endl;
	debug("move:%d\n", get_elapsed_msec());
	PRINT_TIMER();
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

