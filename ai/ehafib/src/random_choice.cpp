#include <bits/stdc++.h>
#include "json11/json11.hpp"
using namespace std;
using namespace json11;

class JsonUtil {
public:
    static vector<int> vector_ints(const Json &json) {
        vector<int> res;
        for(const auto &x: json.array_items()) {
            res.push_back(x.int_value());
        }
        return res;
    }
};

// ?: 初回入力
void put_my_name() {
    cout << "random" << endl;
}

class State {
public:
    int number_of_players, punter_id, future_enabled;
    int V, E, M;
    vector<int> sources, targets;
    vector<int> mines;
    vector<int> used;

    // I: 初回入力2時に最初に作るState
    State() {
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

        used = vector<int>(E, 0);
    }

    // I: state復元
    State(const string &json_str) {
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
    }

    // 自身をstateとして出力する
    string to_json() const {
        Json json = Json::object {
            {"players", number_of_players}, {"pid", punter_id}, {"future_enabled", future_enabled},
            {"v", V}, {"e", E}, {"m", M},
            {"sources", Json(sources)},     {"targets", Json(targets)},
            {"mines", Json(mines)},         {"used", Json(used)},
        };
        return json.dump();
    }

    vector<int> available_edge_indices() {
        vector<int> res;
        for (int i = 0; i < used.size(); i++) {
            if (!used[i]) res.push_back(i);
        }
        return res;
    }

    pair<int, int> choose_randomly() {
        vector<int> edge_idx = available_edge_indices();
        int idx = edge_idx[rand() % edge_idx.size()];
        return {sources[idx], targets[idx]};
    }

    void set_used_edge(int u, int v) {
        // TODO あとで高速化？
        for(int i = 0; i < used.size(); i++) {
            if(sources[i] == u && targets[i] == v && used[i] == 0) {
                used[i] = 1;
                break;
            }
        }
    }
};

void doit_first(State &s) {
    cout << s.to_json() << endl; // print new state
    cout << 0 << endl; // futureなんてなかった
}

void doit(State &s) {
    cout << s.to_json() << endl; // print new state

    auto e = s.choose_randomly();
    cout << e.first << " "  << e.second << endl; // print edge
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

            //cout << "json_str: " << json_str << endl;
            State s = State(json_str);

            for (int i = 0; i < s.number_of_players; i++) {
                int a, b;
                cin >> a >> b;
                s.set_used_edge(a, b);
            }

            doit(s);

        } else {
            cerr << "phase error. received: " << phase << endl;
            exit(1);
        }
    }
}

// g++ -std=c++11 json11.cpp random_choice.cpp -o random_choice && ./random_choice
