package icfpc2017;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

// alpha-beta
// 連結成分をメモ化
class MeijinIDAI {
	public InputStream is;
	public PrintWriter out;
	String INPUT = "";
	
	long TL;
	
	public MeijinIDAI(long tl)
	{
		this.TL = tl;
	}
	
	final int PASS_WHEN_CHECKMATE = 1;
	long START;
	
	public String guess(State s)
	{
		if(s.C != 2)return ""; // 2人対戦じゃないと拗ねる。
		
		int n = s.g.size();
		RestorableDisjointSet2[] rdss = new RestorableDisjointSet2[2];
		for(int i = 0;i < 2;i++){
			rdss[i] = new RestorableDisjointSet2(n, n+5);
			for(List<Edge> row : s.g){
				for(Edge e : row){
					if(e.owner == i)rdss[i].union(e.x, e.y);
				}
			}
		}
		
		Map<Long, Edge> map = new HashMap<>();
		for(int i = 0;i < n;i++){
			List<Edge> row = s.g.get(i);
			for(Edge e : row){
				// 未所有の辺をcansに追加
				if(e.x == i && e.owner == -1){
					long code = (long)Math.min(e.x, e.y)<<32|Math.max(e.x, e.y);
					if(!map.containsKey(code)){
						map.put(code, e);
						e.dup = 1;
					}else{
						map.get(code).dup++;
					}
				}
			}
		}
		List<Edge> cans = new ArrayList<>(map.values());
		cache.add(new HashMap<>());
		cache.add(new HashMap<>());
		
		long ec = -1;
		for(int level = 1;level <= s.remturn && System.currentTimeMillis() - START <= TL;level++){
//			int maxdep = Math.min(s.remturn, level);
			try{
				long lstart = System.currentTimeMillis();
				ec = go(level, level, s.P, s, cans, rdss, Long.MIN_VALUE/2, Long.MAX_VALUE/2);
				tr("LEVEL: " + level +" " + (System.currentTimeMillis() - lstart) + "ms");
			}catch(TimeoutException tle){
			}
		}
		s.remturn--;
		if(ec == -1)return "";
		return (ec>>>32) + " " + ((int)ec);
	}
	
	static class Datum
	{
		Edge e;
		long score;
		public Datum(Edge e, long score) {
			this.e = e;
			this.score = score;
		}
	}
	
	List<Map<Long, Long>> cache = new ArrayList<>();
	
	long go(int rem, int dep, int turn, State s, List<Edge> cans, RestorableDisjointSet2[] rdss, long alpha, long beta) throws TimeoutException
	{
		if(rem == 0)return 0;
		List<Datum> data = new ArrayList<>();
		
		// 候補辺を選んだときの追加スコア
		for(Edge e : cans){
			if(e.dup == 0)continue;
			long plus = 0;
			if(!rdss[turn].equiv(e.x, e.y)){
				{
					int rx = rdss[turn].root(e.x);
					for(int cur = rx;cur != -1;cur = rdss[turn].next[cur]){
						if(s.mines.get(cur)){
							int ry = rdss[turn].root(e.y);
							long code = cur * 1000000009L + (rdss[turn].hash0[ry]<<32|rdss[turn].hash1[ry]);
							if(!cache.get(turn).containsKey(code)){
								long lplus = 0;
								for(int tar = ry;tar != -1; tar = rdss[turn].next[tar]){
									long d = s.mindistss.get(cur).get(tar);
									lplus += d*d;
								}
								cache.get(turn).put(code, lplus);
							}
							plus += cache.get(turn).get(code);
						}
					}
				}
				{
					int rx = rdss[turn].root(e.y);
					for(int cur = rx;cur != -1;cur = rdss[turn].next[cur]){
						if(s.mines.get(cur)){
							int ry = rdss[turn].root(e.x);
							long code = cur * 1000000009L + (rdss[turn].hash0[ry]<<32|rdss[turn].hash1[ry]);
							if(!cache.get(turn).containsKey(code)){
								long lplus = 0;
								for(int tar = ry;tar != -1; tar = rdss[turn].next[tar]){
									long d = s.mindistss.get(cur).get(tar);
									lplus += d*d;
								}
								cache.get(turn).put(code, lplus);
							}
							plus += cache.get(turn).get(code);
						}
					}
				}
				if(System.currentTimeMillis() - START > TL)throw new TimeoutException();
			}
			data.add(new Datum(e, plus));
		}
		if(data.isEmpty())return 0;
		
		data.sort((x, y) -> -Long.compare(x.score, y.score)); // スコア降順にソート
		if(rem < dep){
			int ohp = rdss[turn].hp;
//			long[] old = Arrays.copyOf(rdss[turn].hash0, rdss[turn].hash0.length);
			for(Datum d : data){
				rdss[turn].union(d.e.x, d.e.y);
				d.e.dup--;
				long val = d.score - (rem > 0 ? go(rem-1, dep, turn^1, s, cans, rdss, -beta, -alpha) : 0);
				alpha = Math.max(alpha, val);
				d.e.dup++;
				rdss[turn].revert(ohp);
				if(alpha >= beta)return alpha; // alpha-beta cut
			}
//			long[] anew = Arrays.copyOf(rdss[turn].hash0, rdss[turn].hash0.length);
//			assert Arrays.equals(old, anew);
			return alpha;
		}else{
			// 手をかえす
			int ohp = rdss[turn].hp;
			long ret = Long.MIN_VALUE;
			long arg = -1;
			for(Datum d : data){
				rdss[turn].union(d.e.x, d.e.y);
				d.e.dup--;
				long val = d.score - (rem > 0 ? go(rem-1, dep, turn^1, s, cans, rdss, -beta, -alpha) : 0);
				if(val > ret){
					ret = val;
					arg = (long)d.e.x<<32|d.e.y;
				}
				d.e.dup++;
				rdss[turn].revert(ohp);
			}
			return arg;
		}
	}
	
	public static class RestorableDisjointSet2 {
		public int[] upper; // minus:num_element(root) plus:root(normal)
		private int[] targets;
		private int[] histupper;
		public int[] next;
		public int[] tail;
		public int[] histtail;
		
		public long[] histhash0;
		public long[] histhash1;
		public long[] hash0;
		public long[] hash1;
		public int mod0 = 1000000009;
		public int mod1 = 1000000007;
		public int offset0 = 114514;
		public int offset1 = 893810;
		
		public int hp = 0;
		
		public RestorableDisjointSet2(int n, int m)
		{
			upper = new int[n];
			Arrays.fill(upper, -1);
			
			targets = new int[2*m];
			histupper = new int[2*m];
			// 
			next = new int[n];
			tail = new int[n];
			histtail = new int[m];
			Arrays.fill(next, -1);
			for(int i = 0;i < n;i++)tail[i] = i;
			
			histhash0 = new long[2*m];
			histhash1 = new long[2*m];
			hash0 = new long[n];
			hash1 = new long[n];
			for(int i = 0;i < n;i++){
				hash0[i] = offset0 + i;
				hash1[i] = offset1 + i;
			}
		}
		
		public RestorableDisjointSet2(RestorableDisjointSet2 ds)
		{
			this.upper = Arrays.copyOf(ds.upper, ds.upper.length);
			this.histupper = Arrays.copyOf(ds.histupper, ds.histupper.length);
			// 
			this.hp = ds.hp;
		}
		
		public int root(int x)
		{
			return upper[x] < 0 ? x : root(upper[x]);
		}
		
		public boolean equiv(int x, int y)
		{
			return root(x) == root(y);
		}
		
		public boolean union(int x, int y)
		{
			x = root(x);
			y = root(y);
			if(x != y) {
				if(upper[y] < upper[x]) {
					int d = x; x = y; y = d;
				}
//					w[x] += w[y];
				histtail[hp/2] = tail[x];
				record(x); record(y);
				next[tail[x]] = y;
				tail[x] = tail[y];
				
				hash0[x] = hash0[x] * hash0[y] % mod0;
				hash1[x] = hash1[x] * hash1[y] % mod1;
				
				upper[x] += upper[y];
				upper[y] = x;
			}
			return x == y;
		}
		
		public int time() { return hp; }
		
		private void record(int x)
		{
			targets[hp] = x;
			histupper[hp] = upper[x];
			histhash0[hp] = hash0[x];
			histhash1[hp] = hash1[x];
			// 
			hp++;
		}
		
		public void revert(int to)
		{
			while(hp > to){
				upper[targets[hp-1]] = histupper[hp-1];
				hash0[targets[hp-1]] = histhash0[hp-1];
				hash1[targets[hp-1]] = histhash1[hp-1];
				if((hp&1) == 1){
					tail[targets[hp-1]] = histtail[hp/2];
					next[tail[targets[hp-1]]] = -1;
				}
				// 
				hp--;
			}
		}
		
		public int count()
		{
			int ct = 0;
			for(int u : upper){
				if(u < 0)ct++;
			}
			return ct;
		}
		
		public int[][] makeUp()
		{
			int n = upper.length;
			int[][] ret = new int[n][];
			int[] rp = new int[n];
			for(int i = 0;i < n;i++){
				if(upper[i] < 0)ret[i] = new int[-upper[i]];
			}
			for(int i = 0;i < n;i++){
				int r = root(i);
				ret[r][rp[r]++] = i;
			}
			return ret;
		}

	}

	public void solve() {
		char phase = ns().charAt(0);
		if(phase == '?'){
			// 初回入力
			out.println(this.getClass().getSimpleName());
			out.flush();
		}else if(phase == 'I'){
			// 初回入力2
			int C = ni(), P = ni(), F = ni(), S = ni();
			int N = ni(), M = ni(), K = ni();
			List<List<Edge>> g = new ArrayList<>();
			for(int i = 0;i < N;i++)g.add(new ArrayList<>());
			for(int i = 0;i < M;i++){
				int s = ni(), t = ni();
				Edge e = new Edge(s, t);
				g.get(s).add(e);
				g.get(t).add(e);
			}
			BitSet mines = new BitSet();
			for(int i = 0;i < K;i++){
				mines.set(ni());
			}
			List<List<Integer>> mindistss = new ArrayList<>();
			for(int i = 0;i < N;i++){
				if(mines.get(i)){
					mindistss.add(mindists(g, i));
				}else{
					mindistss.add(null);
				}
			}
			
			State state = new State();
			state.g = g;
			state.mines = mines;
			state.C = C;
			state.P = P;
			state.F = F;
			state.S = S;
			state.phase = 0;
			state.mindistss = mindistss;
			state.remturn = (M-P+C-1)/C;
			state.futures = new ArrayList<>();
			for(int i = 0;i < N;i++)state.futures.add(null);
			if(F == 1){
				if(N <= 100 && check4EC(g)){ // 調子乗りすぎ
					for(int i = 0;i < N;i++){
						if(mines.get(i)){
							int maxd = 0;
							int arg = -1;
							for(int j = 0;j < N;j++){
								if(!mines.get(j)){
									if(mindistss.get(i).get(j) > maxd){
										maxd = mindistss.get(i).get(j);
										arg = j;
									}
								}
							}
							if(arg != -1){
								state.futures.set(i, arg);
							}
						}
					}
				}
			}
			if(S == 1){
				state.charges = new ArrayList<>();
				for(int i = 0;i < C;i++)state.charges.add(0);
			}
			out.println(toBase64(state));
			if(F == 0){
				out.println(0);
			}else{
				int ct = 0;
				for(Integer x : state.futures){
					if(x != null)ct++;
				}
				out.println(ct);
				for(int i = 0;i < N;i++){
					if(state.futures.get(i) != null){
						out.println(i + " " + state.futures.get(i));
					}
				}
			}
		}else if(phase == 'G'){
			// ゲーム中入力
			START = System.currentTimeMillis();
			State state = (State)fromBase64(ns());
			int C = state.C;
			for(int i = 0;i < C;i++){
				int L = ni();
				if(state.S == 1 && L == 0 && (state.phase > 0 || state.phase == 0 && i < state.P)){
					state.charges.set(i, state.charges.get(i)+1);
				}
				int[] a = new int[L];
				for(int j = 0;j < L;j++){
					a[j] = ni();
				}
				inner:
				for(int j = 0;j < L-1;j++){
					int s = a[j], t = a[j+1];
					for(Edge e : state.g.get(s)){
						if((e.x^e.y^s) == t && e.owner == -1){
							e.owner = i;
							continue inner;
						}
					}
					throw new RuntimeException("invalid input");
				}
			}
			
			String output = guess(state);
			state.phase++;
			out.println(toBase64(state));
			out.println(state.P + " " + output);
		}else{
			throw new RuntimeException();
		}
	}
	
	public static List<Integer> mindists(List<List<Edge>> g, int start)
	{
		int n = g.size();
		int I = 99999999;
		List<Integer> ds = new ArrayList<>();
		for(int i = 0;i < n;i++)ds.add(I);
		ds.set(start, 0);
		
		Queue<Integer> q = new ArrayDeque<>();
		q.add(start);
		while(!q.isEmpty()){
			int cur = q.poll();
			for(Edge e : g.get(cur)){
				int to = e.x^e.y^cur;
				if(ds.get(to) == I){
					ds.set(to, ds.get(cur) + 1);
					q.add(to);
				}
			}
		}
		return ds;
	}
	
	// 4重辺連結か調べる
	public static boolean check4EC(List<List<Edge>> g)
	{
		for(List<Edge> row : g){
			if(row.size() < 4)return false; // すべての点が次数4以上
		}
		long mincut = minCut(toDense(g));
		return mincut >= 4;
	}
	
	public static long minCut(long[][] g)
	{
		int n = g.length;
		long mincut = Long.MAX_VALUE;
		for(int t = 0;t < n-1;t++){
			long[] ws = new long[n-t];
			// local merge
			int u = 0, v = 0;
			for(int i = 0;i < n-t;i++){
				u = v;
				v = -1;
				long max = -1;
				for(int j = 0;j < n-t;j++){
					if(ws[j] > max){
						max = ws[j];
						v = j;
					}
				}
				if(i == n-t-1)mincut = Math.min(mincut, ws[v]);
				ws[v] = -1;
				for(int j = 0;j < n-t;j++){
					if(ws[j] >= 0)ws[j] += g[v][j];
				}
			}
			// merge u-v
			for(int i = 0;i < n-t;i++){
				g[i][u] += g[i][v];
				g[u][i] += g[v][i];
			}
			// swap(n-1-t,v)
			for(int i = 0;i < n-t;i++){
				g[i][v] = g[i][n-1-t];
				g[v][i] = g[n-1-t][i];
			}
		}
		return mincut;
	}
	
	static long[][] toDense(List<List<Edge>> g)
	{
		int n = g.size();
		long[][] h = new long[n][n];
		for(int i = 0;i < n;i++){
			for(Edge e : g.get(i)){
				h[e.x][e.y] = h[e.y][e.x] = 1;
			}
		}
		return h;
	}
	
	public static String toBase64(Object obj)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(ObjectOutputStream oos = new ObjectOutputStream( baos )){
			oos.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
	
	public static Object fromBase64(String code)
	{
		byte[] data = Base64.getDecoder().decode( code );
		try(ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream(  data ) )){
			return ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static class State implements Serializable
	{
		private static final long serialVersionUID = -4623606164150300132L;
		int C; // プレー人数
		int P; // お前のID(0~N-1)
		int F, S; // future splurge対応フラグ
		int remturn; // 残りターン
		int phase; // 何回目か
		List<List<Edge>> g; // グラフ
		List<Integer> charges; // splurgeチャージ量
		BitSet mines; // mineかどうか
		List<List<Integer>> mindistss; // 最短経路長
		List<Integer> futures;
	}
	
	static class Future implements Serializable
	{
		private static final long serialVersionUID = 8685203019168931849L;
		int mine, site;
		public Future(int mine, int site) {
			this.mine = mine;
			this.site = site;
		}
	}
	
	static class Edge implements Serializable
	{
		private static final long serialVersionUID = 5180071263476967427L;
		int x, y;
		int owner; // 辺の所有者。いないときは-1
		transient int dup;
		
		public Edge(int x, int y) {
			this.x = x;
			this.y = y;
			this.owner = -1;
		}

		@Override
		public String toString() {
			return "Edge [x=" + x + ", y=" + y + ", owner=" + owner + "]";
		}
	}

	void run() throws Exception {
		is = INPUT.isEmpty() ? System.in : new ByteArrayInputStream(INPUT.getBytes());
		out = new PrintWriter(System.out);

		long s = System.currentTimeMillis();
		solve();
		out.flush();
		if (!INPUT.isEmpty())
			tr(System.currentTimeMillis() - s + "ms");
	}

	public static void main(String[] args) throws Exception {
		new MeijinIDAI(800L).run();
	}

	private byte[] inbuf = new byte[1024];
	public int lenbuf = 0, ptrbuf = 0;

	private int readByte() {
		if (lenbuf == -1)
			throw new InputMismatchException();
		if (ptrbuf >= lenbuf) {
			ptrbuf = 0;
			try {
				lenbuf = is.read(inbuf);
			} catch (IOException e) {
				throw new InputMismatchException();
			}
			if (lenbuf <= 0)
				return -1;
		}
		return inbuf[ptrbuf++];
	}

	private boolean isSpaceChar(int c) {
		return !(c >= 33 && c <= 126);
	}

	private int skip() {
		int b;
		while ((b = readByte()) != -1 && isSpaceChar(b))
			;
		return b;
	}

	private double nd() {
		return Double.parseDouble(ns());
	}

	private char nc() {
		return (char) skip();
	}

	private String ns() {
		int b = skip();
		StringBuilder sb = new StringBuilder();
		while (!(isSpaceChar(b))) { // when nextLine, (isSpaceChar(b) && b != '
									// ')
			sb.appendCodePoint(b);
			b = readByte();
		}
		return sb.toString();
	}

	private char[] ns(int n) {
		char[] buf = new char[n];
		int b = skip(), p = 0;
		while (p < n && !(isSpaceChar(b))) {
			buf[p++] = (char) b;
			b = readByte();
		}
		return n == p ? buf : Arrays.copyOf(buf, p);
	}

	private char[][] nm(int n, int m) {
		char[][] map = new char[n][];
		for (int i = 0; i < n; i++)
			map[i] = ns(m);
		return map;
	}

	private int[] na(int n) {
		int[] a = new int[n];
		for (int i = 0; i < n; i++)
			a[i] = ni();
		return a;
	}

	private int ni() {
		int num = 0, b;
		boolean minus = false;
		while ((b = readByte()) != -1 && !((b >= '0' && b <= '9') || b == '-'))
			;
		if (b == '-') {
			minus = true;
			b = readByte();
		}

		while (true) {
			if (b >= '0' && b <= '9') {
				num = num * 10 + (b - '0');
			} else {
				return minus ? -num : num;
			}
			b = readByte();
		}
	}

	private long nl() {
		long num = 0;
		int b;
		boolean minus = false;
		while ((b = readByte()) != -1 && !((b >= '0' && b <= '9') || b == '-'))
			;
		if (b == '-') {
			minus = true;
			b = readByte();
		}

		while (true) {
			if (b >= '0' && b <= '9') {
				num = num * 10 + (b - '0');
			} else {
				return minus ? -num : num;
			}
			b = readByte();
		}
	}

	private static void tr(Object... o) {
		System.err.println(Arrays.deepToString(o));
	}
}
