#include "json11/json11.hpp"
#include <cmath>
#include <iostream>
#include <numeric>
#include <queue>
#include <vector>

// M*E > 1e8 --> random
// M*N*V > 1e8 --> random
// M*E > 1e8 --> random
using namespace std;
using namespace json11;

const int USE_VERTEX_WEIGHT = 0;
const double VERTEX_COEFFICIENT = 1e-4;
const int USE_LAMBDA_WEIGHT = 1;
const int DEBUG = 1;

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

public:
    UnionFind(int size) : par(size) { iota(begin(par), end(par), 0); }
    int root(int a) {
        if (a == par[a])
            return a;
        return par[a] = root(par[a]);
    }
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

        for (int i = 0; i < n; i++) {
            if (dist[i] == -1)
                dist[i] = 0;
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
    int number_of_players, punter_id, future_enabled, splurge_enabled;
    int T;
    int V, E, M;
    vector<int> sources, targets;
    vector<int> mines;
    vector<int> used;
    vector<vector<Edge>> G;
    vector<vector<int>> score;
    vector<vector<int>> claim;
    vector<int> my_mines;
    vector<UnionFind> uf;
    vector<vector<double>> vertex_weight;
    vector<int> vertex_deg;
    vector<long long> current_score;

    // I: 初回入力2時に最初に作るState
    State() { // {{{
        cin >> number_of_players >> punter_id >> future_enabled >>
            splurge_enabled;
        cin >> V >> E >> M;
        T = 0;
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

        used = vector<int>(E, 0);
        claim = vector<vector<int>>(number_of_players, vector<int>());
        my_mines = vector<int>();

        // 最初の下準備はここから
        if (M * 1.0 * E > 1e7)
            return;

        // O(E)
        G = vector<vector<Edge>>(V, vector<Edge>());
        for (int i = 0; i < E; i++) {
            G[sources[i]].push_back({targets[i], 1, i, false, 0.0});
            G[targets[i]].push_back({sources[i], 1, i, true, 0.0});
        }

        // 各mineからはばゆうせんしてきょりをもとめる O(ME)
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
        future_enabled = json["future_enabled"].int_value();
        splurge_enabled = json["splurge_enabled"].int_value();
        T = json["t"].int_value();

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

        // if(DEBUG) cout << "INPUT PARSE DONE" << endl;
        // }}}
    }

    // 自身をstateとして出力する
    string to_json() const { // {{{
        Json json = Json::object{
            {"players", number_of_players},
            {"pid", punter_id},
            {"future_enabled", future_enabled},
            {"splurge_enabled", splurge_enabled},
            {"t", T + 1},
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
            if (used[i] == 0) res.push_back(i);
        }
        return res;
    }

    pair<int, int> choose_randomly() {
        vector<int> edge_idx = available_edge_indices();
        int idx = edge_idx[rand() % edge_idx.size()];
        return {sources[idx], targets[idx]};
    }
    // }}}

    // O(MV)
    // long long calc_score(int uid, UnionFind &u) {
    //    long long res = 0;
    //    for (int mi = 0; mi < M; mi++) {
    //        int m = mines[mi];
    //        for (int v = 0; v < V; v++) {
    //            if (u.eq(m, v)) {
    //                res += score[mi][v] * 1LL * score[mi][v];
    //            }
    //        }
    //    }
    //    return res;
    //}

    // O(MNV)
    // USE_VERTEX_WEIGHTが0でないときはO(M^2NV)
    void add_next_score_weight_kai() {
        for (int i = 0; i < number_of_players; i++) { // O(N)
            vector<vector<long long>> addm(M, vector<long long>(V, 0));

            // cout << "ADDM" << endl;
            // cout << "MV " << i << " " << M << " "  << V << endl;
            for (int mi = 0; mi < M; mi++) {
                for (int v = 0; v < V; v++) {
                    // cout << mi << " "  << v << endl;
                    addm[mi][uf[i].root(v)] += score[mi][v];
                    // cout << "root: " << uf[i].root(v) << endl;
                }
            }

            // long long lprev = calc_score(i, uf[i]); // O(V)

            // cout << "LPREV" << endl;
            long long lprev = 0;
            for (int mi = 0; mi < M; mi++) {
                int m = mines[mi];
                lprev += addm[mi][uf[i].root(m)];
            }

            for (int v = 0; v < V; v++) {
                for (Edge &e : G[v]) { // O(E)
                    if (used[e.idx] != 0)
                        continue;
                    if (v >= e.to)
                        continue;
                    if (uf[i].eq(v, e.to))
                        continue;

                    //// O(V)
                    // UnionFind u = uf[i];
                    // u.unite(v, e.to); // O(V)
                    // long long lnow = calc_score(i, u);

                    long long lnow = lprev;
                    for (int mi = 0; mi < M; mi++) {
                        int m = mines[mi];
                        if (uf[i].eq(m, v)) {
                            lnow += addm[mi][uf[i].root(e.to)];
                        }
                        if (uf[i].eq(m, e.to)) {
                            lnow += addm[mi][uf[i].root(v)];
                        }
                    }

                    lnow -= lprev;

                    if (i == punter_id) {
                        e.weight += lnow;
                    } else {
                        e.weight += (lnow * 0.5 / (number_of_players - 1));
                    }

                    // add edge weight
                    if (USE_VERTEX_WEIGHT) {
                        for (int mi = 0; mi < M; mi++) {
                            int m = mines[mi];
                            if (uf[punter_id].eq(m, v) &&
                                !uf[punter_id].eq(m, e.to)) {
                                e.weight += vertex_deg[e.to];
                                e.weight += VERTEX_COEFFICIENT *
                                            vertex_weight[mi][e.to];
                            }
                            if (!uf[punter_id].eq(m, v) &&
                                uf[punter_id].eq(m, e.to)) {
                                e.weight += vertex_deg[e.to];
                                e.weight +=
                                    VERTEX_COEFFICIENT * vertex_weight[mi][v];
                            }
                        }
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
                    if (used[e.idx] != 0 && used[e.idx] != punter_id + 1)
                        continue;
                    if (dist[e.to] != -1)
                        continue;

                    dist[e.to] = dist[v] + 1;
                    if (dist[e.to] <= 1) {
                        e.weight += 1145141919.0;
                        q.push(e.to);
                    }

                    if (dist[e.to] <= 2) {
                        e.weight += 364364.0;
                        q.push(e.to);
                    }
                }
            }
        }
    }

    void init_union_find() {
        for (int i = 0; i < number_of_players; i++) {
            uf.push_back(UnionFind(V));
            for (const int &eidx : claim[i]) {
                uf[i].unite(sources[eidx], targets[eidx]);
            }
        }
    }

    void init_vertex_deg() {
        // O(E)
        vertex_deg = vector<int>(V, 0);
        for (int e = 0; e < E; e++) {
            if (used[e])
                continue;
            vertex_deg[sources[e]]++;
            vertex_deg[targets[e]]++;
        }
    }

    // O(MV^2) TODO: 時間決めて打ち切る処理の追加
    void init_vertex_weight() {
        if (!USE_VERTEX_WEIGHT)
            return;

        vertex_weight = vector<vector<double>>(M, vector<double>(V, 0));
        if (M * 1.0 * V * V > 1e7)
            return;

        for (int v = 0; v < V; v++) {
            queue<int> q;
            vector<int> dist(V, -1);
            const int D = 4;
            dist[v] = 0;
            q.push(v);

            while (!q.empty()) {
                int w = q.front();
                q.pop();
                for (Edge &e : G[w]) {
                    if (used[e.idx] == 0)
                        continue;
                    if (dist[e.to] != -1)
                        continue;
                    dist[e.to] = dist[w] + 1;

                    // cnt[dist[e.to]]++;
                    for (int mi = 0; mi < M; mi++) {
                        vertex_weight[mi][dist[e.to]] +=
                            score[mi][e.to] * 1.0 * score[mi][e.to] / D / V;
                    }

                    if (dist[e.to] + 1 < D) {
                        q.push(e.to);
                    }
                }
            }
        }
    }

    // O(M^2 E)
    pair<int, int> choose_steiner() {
        if (M * 1.0 * M * E > 1e7) return choose_greedily();

        vector<double> edge_weight(E, 0);

        for (int mi = 0; mi < M; mi++) {
            deque<int> q;
            vector<int> dist(V, -1);
            int m = mines[mi];

            q.push_back(m);
            dist[m] = 0;

            while (!q.empty()) {
                int v = q.front();
                q.pop_front();

                for (const Edge &e : G[v]) {
                    if (dist[e.to] != -1)
                        continue;
                    if (used[e.idx] == punter_id + 1) {
                        dist[e.to] = dist[v];
                        q.push_front(e.to);
                    } else if (used[e.idx] == 0) {
                        dist[e.to] = dist[v] + 1;
                        q.push_back(e.to);
                    }
                }
            }

            for (int mj = 0; mj < M; mj++) {
                if (mi == mj)
                    continue;
                int cur = mines[mj];

                if (dist[cur] <= 0)
                    continue;

                double w = 1e5 * pow(0.95, T) / dist[cur];

                for (int v = 0; v < V; v++) {
                    for(const Edge &e: G[v]) {
                        if (used[e.idx] == 0 && dist[e.to] == dist[v] + 1) {
                            // w適用
                            edge_weight[e.idx] += w;
                        }
                    }
                }
            }

            // for(int mj = 0; mj < M; mj++) {
            //     if(mi == mj) continue;

            //     int cur = mines[mj];
            //     // いけない or すでにれんけつ
            //     if(dist[cur] <= 0) continue;

            //     // 逆数なのはとくべつないみはあに
            //     double w = 1e6 *pow(0.99, T) / dist[cur];

            //     while(cur != m) {
            //         int edge = from_e[cur];
            //         int vert = from_v[cur];

            //         edge_weight[edge] += w;
            //         cur = vert;
            //     }
            // }
        }

        init_union_find();
        add_next_score_weight_kai();

        for (int v = 0; v < V; v++) {
            for (Edge &e : G[v]) {
                edge_weight[e.idx] += e.weight;
            }
        }

        double max_weight = 0;
        int idx = -1;
        for (int v = 0; v < V; v++) {
            for (Edge &e : G[v]) {
                if (used[e.idx])
                    continue;
                if (uf[punter_id].eq(v, e.to)) //まじで意味ないのでやめよう
                    continue;
                if (max_weight < edge_weight[e.idx]) {
                    max_weight = edge_weight[e.idx];
                    idx = e.idx;
                }
            }
        }

        if (idx == -1) {
            return make_pair(-1, -1);
        } else {
            return make_pair(sources[idx], targets[idx]);
        }
    }

    // O(ME)
    pair<int, int> choose_greedily() {
        if (M * 1.0 * number_of_players * E > 1e7) {
            return choose_randomly();
        }

        // cout << "INIT_UF" << endl;
        init_union_find();
        // cout << "VD" << endl;
        init_vertex_deg();
        // cout << "VW" << endl;
        init_vertex_weight();
        // cout << "ANSW" << endl;
        add_next_score_weight_kai();
        // cout << "ANL" << endl;
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
        if (u == -1 && v == -1)
            return;
        // TODO あとで高速化？
        for (int i = 0; i < used.size(); i++) {
            if (used[i] != 0)
                continue;
            if ((sources[i] == u && targets[i] == v) ||
                (sources[i] == v && targets[i] == u)) {
                used[i] = uid + 1;
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

    // auto e = s.choose_greedily();
    auto e = s.choose_steiner();
    if(e.first == e.second) e = s.choose_randomly();
    cout << s.punter_id << " " << e.second << " " << e.first << endl;
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

            // cout << "State received." << endl;
            State s = State(json_str);

            for (int i = 0; i < s.number_of_players; i++) {
                int path_length;
                cin >> path_length;

                vector<int> path(path_length);
                for (int j = 0; j < path_length; j++) {
                    cin >> path[j];
                }

                for (int j = 0; j + 1 < path_length; j++) {
                    s.set_used_edge(path[j], path[j + 1], i);
                }
            }
            doit(s);

        } else {
            cerr << "phase error. received: " << phase << endl;
            exit(1);
        }
    }
}

// g++ -std=c++11 json11.cpp random_choice.cpp -o random_choice &&
