#include <bits/stdc++.h>
using namespace std;

// {{{
// clang-format off
#define forr(x,arr) for(auto&& x:arr)
#define _overload3(_1,_2,_3,name,...) name
#define _rep2(i,n) _rep3(i,0,n)
#define _rep3(i,a,b) for(int i=int(a);i<int(b);++i)
#define rep(...) _overload3(__VA_ARGS__,_rep3,_rep2,)(__VA_ARGS__)
#define _rrep2(i,n) _rrep3(i,0,n)
#define _rrep3(i,a,b) for(int i=int(b)-1;i>=int(a);i--)
#define rrep(...) _overload3(__VA_ARGS__,_rrep3,_rrep2,)(__VA_ARGS__)
#define all(x) (x).begin(),(x).end()
#define bit(n) (1LL<<(n))
#define sz(x) ((int)(x).size())
#define TEN(n) ((ll)(1e##n))
#define fst first
#define snd second

string DBG_DLM(int &i){return(i++==0?"":", ");}
#define DBG_B(exp){int i=0;os<<"{";{exp;}os<<"}";return os;}
template<class T>ostream&operator<<(ostream&os,vector<T>v);
template<class T>ostream&operator<<(ostream&os,set<T>v);
template<class T>ostream&operator<<(ostream&os,queue<T>q);
template<class T>ostream&operator<<(ostream&os,priority_queue<T>q);
template<class T,class K>ostream&operator<<(ostream&os,pair<T,K>p);
template<class T,class K>ostream&operator<<(ostream&os,map<T,K>mp);
template<class T,class K>ostream&operator<<(ostream&os,unordered_map<T,K>mp);
template<int I,class TPL>void DBG(ostream&os,TPL t){}
template<int I,class TPL,class H,class...Ts>void DBG(ostream&os,TPL t){os<<(I==0?"":", ")<<get<I>(t);DBG<I+1,TPL,Ts...>(os,t);}
template<class T,class K>void DBG(ostream&os,pair<T,K>p,string delim){os<<"("<<p.first<<delim<<p.second<<")";}
template<class...Ts>ostream&operator<<(ostream&os,tuple<Ts...>t){os<<"(";DBG<0,tuple<Ts...>,Ts...>(os,t);os<<")";return os;}
template<class T,class K>ostream&operator<<(ostream&os,pair<T,K>p){DBG(os,p,", ");return os;}
template<class T>ostream&operator<<(ostream&os,vector<T>v){DBG_B(forr(t,v){os<<DBG_DLM(i)<<t;});}
template<class T>ostream&operator<<(ostream&os,set<T>s){DBG_B(forr(t,s){os<<DBG_DLM(i)<<t;});}
template<class T>ostream&operator<<(ostream&os,queue<T>q){DBG_B(for(;q.size();q.pop()){os<<DBG_DLM(i)<<q.front();});}
template<class T>ostream&operator<<(ostream&os,priority_queue<T>q){DBG_B(for(;q.size();q.pop()){os<<DBG_DLM(i)<<q.top();});}
template<class T,class K>ostream&operator<<(ostream&os,map<T,K>m){DBG_B(forr(p,m){os<<DBG_DLM(i);DBG(os,p,"->");});}
template<class T,class K>ostream&operator<<(ostream&os,unordered_map<T,K>m){DBG_B(forr(p,m){os<<DBG_DLM(i);DBG(os,p,"->");});}
#define DBG_OVERLOAD(_1,_2,_3,_4,_5,_6,macro_name,...)macro_name
#define DBG_LINE(){char s[99];sprintf(s,"line:%3d | ",__LINE__);cerr<<s;}
#define DBG_OUTPUT(v){cerr<<(#v)<<"="<<(v)<<endl;}
#define DBG1(v,...){DBG_OUTPUT(v);}
#define DBG2(v,...){DBG_OUTPUT(v);cerr<<", ";DBG1(__VA_ARGS__);}
#define DBG3(v,...){DBG_OUTPUT(v);cerr<<", ";DBG2(__VA_ARGS__);}
#define DBG4(v,...){DBG_OUTPUT(v);cerr<<", ";DBG3(__VA_ARGS__);}
#define DBG5(v,...){DBG_OUTPUT(v);cerr<<", ";DBG4(__VA_ARGS__);}
#define DBG6(v,...){DBG_OUTPUT(v);cerr<<", ";DBG5(__VA_ARGS__);}
#define DEBUG0(){DBG_LINE();cerr<<endl;}
#ifdef LOCAL
#define out(...){DBG_LINE();DBG_OVERLOAD(__VA_ARGS__,DBG6,DBG5,DBG4,DBG3,DBG2,DBG1)(__VA_ARGS__);cerr<<endl;}
#else
#define out(...)
#endif

using ll=long long;
using pii=pair<int,int>;using pll=pair<ll,ll>;using pil=pair<int,ll>;using pli=pair<ll,int>;
using vs=vector<string>;using vvs=vector<vs>;using vvvs=vector<vvs>;
using vb=vector<bool>;using vvb=vector<vb>;using vvvb=vector<vvb>;
using vi=vector<int>;using vvi=vector<vi>;using vvvi=vector<vvi>;
using vl=vector<ll>;using vvl=vector<vl>;using vvvl=vector<vvl>;
using vd=vector<double>;using vvd=vector<vd>;using vvvd=vector<vvd>;
using vpii=vector<pii>;using vvpii=vector<vpii>;using vvvpii=vector<vvpii>;
template<class A,class B>bool amax(A&a,const B&b){return b>a?a=b,1:0;}
template<class A,class B>bool amin(A&a,const B&b){return b<a?a=b,1:0;}
ll ri(){ll l;cin>>l;return l;} string rs(){string s;cin>>s;return s;}
// clang-format on
// }}}


#include <sys/time.h>
double getTime() {
  timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec + tv.tv_usec * 1e-6;
}

using Weight = int;
struct Edge {
  int dst;
  Weight weight;
  Edge() : dst(0), weight(0) {}
  Edge(int d, Weight w = 1) : dst(d), weight(w) {}
};
using Edges = vector<Edge>;
using Graph = vector<Edges>;

//Graph G(n);
//G[u].emplace_back(v); // kyori 1



struct UnionFind {
  const int V; /// 頂点数
  vector<int> par, rank; /// 親の番号, 木の大きさ, 同じ親の頂点数
  UnionFind(int V) : V(V), par(V), rank(V) { init(); }

  UnionFind(vi par, vi rank) : V(par.size()), par(par), rank(rank) {
    // 何もすることはない
  }

  /// 初期化する
  void init() {
    for (int i = 0; i < V; i++) par[i] = i;
    fill(rank.begin(), rank.end(), 0);
  }

  /// 木の根を求める
  int find(int x) {
    return par[x] == x ? x : par[x] = find(par[x]);
  }

  /// x と y の属する集合を併合
  /// 併合前は違う集合だったら true を返す
  bool unite(int x, int y) {
    if ((x = find(x)) == (y = find(y))) return false;

    if (rank[x] < rank[y]) par[x] = y;
    else {
      par[y] = x;
      if (rank[x] == rank[y]) rank[x]++;
    }
    return true;
  }

  /// x と y が同じ集合に属するか否か
  bool same(int x, int y) {
    return find(x) == find(y);
  }
};


// xor128
// http://tubo28.me/algorithm/random/
#ifdef _MSC_VER
#include <process.h>
#else
#include <sys/types.h>
#include <unistd.h>
#endif
struct xor128_random {
    uint32_t seed;
    xor128_random(uint32_t s = 0) {
        if (s)
            seed = s;
        else {
#ifdef _MSC_VER
            seed = (uint32_t)time(0) + _getpid() + 1145141919;
#else
            seed = (uint32_t)time(0) + getpid() + 1145141919;
#endif
        }
        for (int i = 0; i < 10; ++i) next();
#ifdef DEBUG
        cerr << "rand seed : " << seed << endl;
#endif
    }

    uint32_t next() {
        static uint32_t x = 123456789, y = 362436069, z = 521288629, w = seed;
        uint32_t t = x ^ (x << 11);
        x = y;
        y = z;
        z = w;
        w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));
        return w;
    }

    int32_t operator()() { return next(); }

    int32_t operator()(int32_t r) { return next_int(r); }

    int32_t operator()(int32_t l, int32_t r) { return next_int(l, r); }

    int32_t next_int(int32_t l, int32_t r) { return l + next() % (r - l + 1); }

    int32_t next_int(int r) { return next() % r; }

    template <int MOD>
    int32_t next_int() {
        return next() % MOD;
    }

    double next_double() { return next() * 0.00000000023283064365386962890625; }

    double next_double(double r) { return next() * 0.00000000023283064365386962890625 * r; }
};

xor128_random rng;

void handshake() {
  cout << "jUdon2" << endl;
}

void init() {
  //time_limit = 8000;
  int C, I, F, N, M, K;
  scanf("%d %d %d %d %d %d", &C, &I, &F, &N, &M, &K);
  Graph G(N);
  vi E(M*2);
  for(int i=0;i<M;i++){
    int a,b;
    scanf("%d %d", &a, &b);
    E[i*2] = a;
    E[i*2+1] = b;
    G[a].emplace_back(b);
    G[b].emplace_back(a);
  }

  vi KS(K);
  vvl DS(K, vl(N, 1e18));

  // ダイクストラ(BFSに書き換える?)
  for(int di=0;di<K;di++){
    int start;
    scanf("%d", &start);
    KS[di] = start;
    {
      priority_queue<pli, vector<pli>, greater<pli>> q;
      DS[di][start] = 0;
      q.emplace(0, start);

      while (!q.empty()) {
        ll c; int v;
        tie(c, v) = q.top(); q.pop();

        ///if (v == goal) {}
        if (DS[di][v] < c) continue;

        forr(edg, G[v]) {
          int nv = edg.dst;
          ll nc = c + edg.weight;
          if (DS[di][nv] > nc) {
            DS[di][nv] = nc;
            q.emplace(nc, nv);
          }
        }
      }
    }

    for(int i=0;i<N;i++){
      DS[di][i] = DS[di][i] * DS[di][i];
    }
  }

  UnionFind UF(N);
  vvi PARS(C, vi(N));
  vvi RANKS(C, vi(N));

  for(int i=0;i<N;i++){
    for(int j=0;j<C;j++){
      PARS[j][i] = UF.par[i];
      RANKS[j][i] = UF.rank[i];
    }
  }

  // state出力
  stringstream ser;
  ser << C << " " << I << " " << F << " " << N << " " << M << " " << K << " ";
  for(int i=0;i<M*2;i++){
    ser << E[i] << " ";
  }
  for(int i=0;i<K;i++){
    ser << KS[i] << " ";
  }
  forr(D, DS){
    forr(d, D){
      ser << d << " ";
    }
  }
  forr(P, PARS){
    forr(p, P){
      ser << p << " ";
    }
  }
  forr(R, RANKS){
    forr(r, R){
      ser << r << " ";
    }
  }

  cout << ser.str() << "\n";
  cout << 0 << endl;
}

void move() {
  double time_limit = 0.9;
  double start_time = getTime();

  // state読み込み
  int C, I, F, N, M, K;
  scanf("%d %d %d %d %d %d", &C, &I, &F, &N, &M, &K);
  vvi E(M, vi(2));
  for(int i=0;i<M;i++){
    for(int j=0;j<2;j++){
      scanf("%d", &E[i][j]);
    }
  }
  vi KS(K);
  for(int i=0;i<K;i++){
    scanf("%d", &KS[i]);
  }
  vvl DS(K, vl(N));
  forr(D, DS){
    for(int i=0;i<N;i++){
      scanf("%lld", &D[i]);
    }
  }
  vvi PARS(C, vi(N));
  vvi RANKS(C, vi(N));
  forr(P, PARS){
    for(int i=0;i<N;i++){
      scanf("%d", &P[i]);
    }
  }
  forr(R, RANKS){
    for(int i=0;i<N;i++){
      scanf("%d", &R[i]);
    }
  }

  // 選ばれた辺
  vi EF(M);
  for(int i=0;i<C;i++){
    int a,b;
    scanf("%d %d", &a, &b);
    if(a == -1){
      continue;
    }
    for(int j=0;j<M;j++){
      if(((a == E[j][0] && b == E[j][1]) || (a == E[j][1] && b == E[j][0])) && EF[j] == 0){
        EF[j] = 1;
        M -= 1;
        for(int k=0;k<2;k++){
          int tmp;
          tmp = E[j][k];
          E[j][k] = E[M][k];
          E[M][k] = tmp;
        }
        break;
      }
    }
    UnionFind uf(PARS[i], RANKS[i]);
    uf.unite(a, b);
    PARS[i] = uf.par;
    RANKS[i] = uf.rank;
  }

  // 処理!
  vl SR(M);
  vl SC(M);
  vl SS(M);
  vi EI(M);
  for(int i=0;i<M;i++){
    EI[i] = i;
  }

  int cnt = 0;
  while(getTime() - start_time < time_limit){
    cnt++;
    // シャッフル
    for(int i=M-1;i;i--){
      int ind = rng.next_int(i);
      int tmp = EI[ind];
      EI[ind] = EI[i];
      EI[i] = tmp;
    }
    vl scores(C);
    for(int i=0;i<C;i++){
      UnionFind uf(PARS[i], RANKS[i]);
      for(int j=i-I;j<M;j+=C){
        if(j < 0){
          continue;
        }
        uf.unite(E[EI[j]][0], E[EI[j]][1]);
      }
      for(int k=0;k<K;k++){
        int mi = uf.find(KS[k]);
        for(int j=0;j<N;j++){
          if(uf.find(j) == mi){
            scores[i] += DS[i][j];
          }
        }
      }
    }
    ll is = scores[I];
    int r = 0;
    for(int i=0;i<C;i++){
      if(is == scores[i]){
        r += 1;
      }else if(is < scores[i]){
        r += 2;
      }
    }
    for(int i=0;i<M;i+=C){
      SR[EI[i]] += r;
      SC[EI[i]] += 1;
      SS[EI[i]] += is * 2;
      for(int j=0;j<C;j++){
        SS[EI[i]] -= scores[j];
      }
    }
  }
  DBG_OUTPUT(cnt);

  double mr = -1;
  double ms = -1;
  int mi = 0;
  //DBG_OUTPUT(E)
  for(int i=0;i<M;i++){
    double ir = (double)SR[i] / (double)SC[i];
    double is = (double)SS[i] / (double)SC[i];
    if(mr < 0 || ir < mr || (ir == mr && is > ms)){
      mr = ir;
      ms = is;
      mi = i;
    }
  }
  cerr << "C=" << C << " I=" << I << " F=" << F << " N=" << N << " M=" << M << " K=" << K << " " << endl;
  cerr << "mr,ms = " << mr << ", " << ms << endl;

  // state出力
  stringstream ser;
  ser << C << " " << I << " " << F << " " << N << " " << M << " " << K << " ";
  for(int i=0;i<M;i++){
    ser << E[i][0] << " ";
    ser << E[i][1] << " ";
  }
  for(int i=0;i<K;i++){
    ser << KS[i] << " ";
  }
  forr(D, DS){
    forr(d, D){
      ser << d << " ";
    }
  }
  forr(P, PARS){
    //DBG_OUTPUT(P);
    forr(p, P){
      ser << p << " ";
    }
  }
  forr(R, RANKS){
    //DBG_OUTPUT(R);
    forr(r, R){
      ser << r << " ";
    }
  }
  cout << ser.str() << "\n";

  if(mi >= 0){
    cout << I << ' ' << E[mi][0] << ' ' << E[mi][1] << endl;
  }else{
    // とりあえずパス
    cout << I << ' ' << -1 << ' ' << -1 << endl;
  }
}

void Main() {
  //start_time = get_time();
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

signed main() {
  cin.tie(nullptr);
  ios::sync_with_stdio(false);
  Main();
  return 0;
}
