#include "json11/json11.hpp"
#include <cmath>
#include <iostream>
#include <numeric>
#include <queue>
#include <vector>

using namespace std;
using namespace json11;

// ?: 初回入力
void put_my_name() {
    cout << "greedy" << endl;
}

class JsonUtil {
public:
    static vector<int> vector_ints(const Json &json) {
        vector<int> res;
        for (const auto &x : json.array_items()) {
            res.push_back(x.int_value());
        }
        return res;
    }
};

class UnionFind {
    vector<int> par;

    int root(int a) {
        if (a == par[a])
            return a;
        return par[a] = root(par[a]);
    }

public:
    UnionFind(int size) : par(size) { iota(begin(par), end(par), 0); }
    void unite(int A, int B) { par[root(B)] = root(A); }
    bool eq(int a, int b) { return root(a) == root(b); }
};

struct Edge {
    int to, cost, idx;
    bool rev;
    double weight;
};

class Dijkstra { // {{{
public:
    static vector<int> solve(const vector<vector<Edge>> &G, int s) {
        int n = (int)G.size();
        vector<int> dist(n, -1);

        dist[s] = 0;
        queue<int> q;
        q.push(s);

        while (!q.empty()) {
            int v = q.front();
            q.pop();
            for (const Edge &e : G[v]) {
                int w = e.to;
                if (dist[w] != -1)
                    continue;
                dist[w] = dist[v] + 1;
                q.push(w);
            }
        }

        return dist;
        /*
           頭が死んでいるのでダイクストラした
        typedef pair<int, int> pii;
        priority_queue<pii, vector<pii>, greater<pii>> pq;
        pq.push({dist[s] = 0, s});

        while(!pq.empty()) {
            auto p = pq.top(); pq.pop();
            int d = p.first;
            int v = p.second;
            if(dist[v] > d) continue;
            for(const int &w: G[v]) {
                int dd = d + 1;
            }
        }
        */
    }
}; //}}}

class State {
public:
    int number_of_players, punter_id, future_enabled;
    int V, E, M;
    vector<int> sources, targets;
    vector<int> mines;
    vector<int> used;
    vector<vector<Edge>> G;
    vector<vector<int>> score;
    vector<vector<int>> claim;
    vector<int> my_mines;
    vector<UnionFind> uf;

    // I: 初回入力2時に最初に作るState
    State() { // {{{
        cin >> number_of_players >> punter_id >> future_enabled;
        cin >> V >> E >> M;
        for (int i = 0; i < E; i++) {
            int a, b;
            cin >> a >> b;
            sources.push_back(a);
            targets.push_back(b);
        }

        for (int i = 0; i < M; i++) {
            int a;
            cin >> a;
            mines.push_back(a);
        }

        used = vector<int>(E, -1);
        claim = vector<vector<int>>(number_of_players, vector<int>());
        my_mines = vector<int>();
        // 最初の下準備はここから

        if (M * 1.0 * V > 1e7)
            return;

        G = vector<vector<Edge>>(V, vector<Edge>());
        for (int i = 0; i < E; i++) {
            G[sources[i]].push_back({targets[i], 1, i, false, 0.0});
            G[targets[i]].push_back({sources[i], 1, i, true, 0.0});
        }

        for (const int &mine : mines) {
            auto v = Dijkstra::solve(G, mine);
            score.push_back(v);
        }
    } // }}}

    // I: state復元
    State(const string &json_str) {
        // {{{
        string err;
        auto json = Json::parse(json_str, err);

        number_of_players = json["players"].int_value();
        punter_id = json["pid"].int_value();
        V = json["v"].int_value();
        E = json["e"].int_value();
        M = json["m"].int_value();

        sources = JsonUtil::vector_ints(json["sources"]);
        targets = JsonUtil::vector_ints(json["targets"]);
        mines = JsonUtil::vector_ints(json["mines"]);
        used = JsonUtil::vector_ints(json["used"]);
        my_mines = JsonUtil::vector_ints(json["my_mines"]);

        claim = vector<vector<int>>();
        for (const auto &x : json["claim"].array_items()) {
            claim.push_back(JsonUtil::vector_ints(x));
        }

        score = vector<vector<int>>();
        for (const auto &x : json["score"].array_items()) {
            score.push_back(JsonUtil::vector_ints(x));
        }

        G = vector<vector<Edge>>(V, vector<Edge>());
        for (int i = 0; i < E; i++) {
            G[sources[i]].push_back({targets[i], 1, i, false, 0.0});
            G[targets[i]].push_back({sources[i], 1, i, true, 0.0});
        }
        // }}}
    }

    // 自身をstateとして出力する
    string to_json() const { // {{{
        Json json = Json::object{
            {"players", number_of_players},
            {"pid", punter_id},
            {"future_enabled", future_enabled},
            {"v", V},
            {"e", E},
            {"m", M},
            {"sources", Json(sources)},
            {"targets", Json(targets)},
            {"mines", Json(mines)},
            {"used", Json(used)},
            {"claim", Json(claim)},
            {"score", Json(score)},
            {"my_mines", Json(my_mines)},
        };
        return json.dump();
    } // }}}

    // {{{
    vector<int> available_edge_indices() {
        vector<int> res;
        for (int i = 0; i < used.size(); i++) {
            if (!used[i])
                res.push_back(i);
        }
        return res;
    }

    pair<int, int> choose_randomly() {
        vector<int> edge_idx = available_edge_indices();
        int idx = edge_idx[rand() % edge_idx.size()];
        return {sources[idx], targets[idx]};
    }
    // }}}

    // O(EMV)
    void add_next_score_weight() {
        for (int mi = 0; mi < M; mi++) {
            int m = mines[mi];
            vector<Edge> elist;

            for (int v = 0; v < V; v++) {
                if (uf[punter_id].eq(m, v)) {
                    for (Edge &e : G[v]) {
                        if (used[e.idx] != -1)
                            continue;
                        if (uf[punter_id].eq(v, e.to))
                            continue;

                        e.weight += score[mi][e.to] * 1.0 * score[mi][e.to];
                        // 本当は他のマインとの兼ね合いも含めて出すべき
                        // elist.push_back(e); //
                        // 次数や周囲も評価に入れるために格納
                    }
                }
            }

            // for (Edge &e : elist) {
            //     int v = e.to;
            //     queue<int> q;
            //     vector<int> dist(V, -1);
            //     dist[v] = 0;
            //     q.push(v);

            //     while (!q.empty()) {
            //         int w = q.front();
            //         q.pop();

            //         if (dist[w] < 5) {
            //             for (Edge &f : G[w]) {
            //                 if (used[f.idx] != -1)
            //                     continue; // 自分のでなければ。

            //                 int u = f.to;
            //                 if (dist[u] != -1)
            //                     continue;
            //                 dist[u] = dist[w] + 1;
            //                 if (dist[u] < 5)
            //                     q.push(u);

            //                 e.weight += score[mi][u] / 1000000.0 *
            //                 score[mi][u];
            //             }
            //         }
            //     }
            // }
        }
    }

    // O(MV)
    long long calc_score(int uid, UnionFind &u) {
        long long res = 0;
        for(int mi = 0; mi < M; mi++) {
            int m = mines[mi];
            for(int v = 0; v < V; v++) {
                if(u.eq(m, v)) {
                    res += score[mi][v] * 1LL * score[mi][v];
                }
            }
        }
        return res;
    }

    // O(EMV)
    void add_next_score_weight_kai() {
        for (int i = 0; i < number_of_players; i++) {
            for (int v = 0; v < V; v++) {
                for (Edge &e : G[v]) {

                    if (used[e.idx] != -1)
                        continue;
                    if (v >= e.to)
                        continue;

                    // O(V)
                    UnionFind u = uf[i];

                    long long lprev = calc_score(i, u);
                    u.unite(v, e.to);
                    long long lnow = calc_score(i, u);
                    lnow -= lprev;

                    e.weight += (lnow * 1.0 / number_of_players);
                    if(i == punter_id) {
                        e.weight += lnow;
                    }
                }
            }
        }
    }

    void add_near_lambda() {
        // m(lambda)に近い頂点に高い得点を与える

        for (int mi = 0; mi < M; mi++) {

            int m = mines[mi];
            bool mine = false;

            for (const int &eidx : claim[punter_id]) {
                mine |= (m == sources[eidx]);
                mine |= (m == targets[eidx]);
            }

            // 既につながっているならむし
            if (mine)
                return;

            // BFSしてADD
            queue<int> q;
            vector<int> dist(V, -1);

            q.push(m);
            dist[m] = 0;

            while (!q.empty()) {
                int v = q.front();
                q.pop();

                for (Edge &e : G[v]) {
                    if (used[e.idx] != -1 && used[e.idx] != punter_id)
                        continue;
                    if (dist[e.to] != -1)
                        continue;

                    dist[e.to] = dist[v] + 1;
                    if (dist[e.to] <= 1) {
                        e.weight += 1145141919.0;
                        q.push(e.to);
                    }
                }
            }
        }
    }


    void add_enemy_gain() {}

    pair<int, int> choose_greedily() {
        // O(ME)
        if (M * 1.0 * E > 1e8) {
            return choose_randomly();
        }

        for (int i = 0; i < M; i++) {
            uf.push_back(UnionFind(V));
            for (const int &eidx : claim[i]) {
                uf[i].unite(sources[eidx], targets[eidx]);
            }
        }

        // add_next_score_weight();
        add_next_score_weight_kai();
        add_near_lambda();

        // cerr << "idx = " << idx << endl;
        // cerr << "score = " << max_score << endl;

        double max_score = 0;
        int idx = -1;

        for (int v = 0; v < V; v++) {
            for (const Edge &e : G[v]) {
                if (max_score < e.weight) {
                    max_score = e.weight;
                    idx = e.idx;
                }
            }
        }

        if (idx == -1) {
            return make_pair(-1, -1);
            // return choose_randomly();
        } else {
            return make_pair(sources[idx], targets[idx]);
        }
    }

    void set_used_edge(int u, int v, int uid) {
        // TODO あとで高速化？
        for (int i = 0; i < used.size(); i++) {
            if (used[i] != -1)
                continue;
            if ((sources[i] == u && targets[i] == v) ||
                (sources[i] == v && targets[i] == u)) {
                used[i] = uid;
                claim[uid].push_back(i);
                break;
            }
        }
    }
};

void doit_first(State &s) {
    cout << s.to_json() << endl; // print new state
    cout << 0 << endl;           // futureなんてなかった
}

void doit(State &s) {
    cout << s.to_json() << endl; // print new state

    auto e = s.choose_greedily();
    cout << s.punter_id << " " << e.second << " " << e.first
         << endl; // print edge
}

int main() {
    string phase;
    while (cin >> phase) {
        char c = phase[0];
        cin.get();

        if (c == '?') {
            put_my_name();
        } else if (c == 'I') {
            State s = State();
            doit_first(s);
        } else if (c == 'G') {
            string json_str;
            getline(cin, json_str);

            State s = State(json_str);

            for (int i = 0; i < s.number_of_players; i++) {
                int a, b;
                cin >> a >> b;
                s.set_used_edge(a, b, i);
            }

            doit(s);

        } else {
            cerr << "phase error. received: " << phase << endl;
            exit(1);
        }
    }
}

// g++ -std=c++11 json11.cpp random_choice.cpp -o random_choice &&
