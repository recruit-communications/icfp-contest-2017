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
import java.util.Collections;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import icfpc2017.PassOnlyAI2.Edge;

class GrowAI {
	public InputStream is;
	public PrintWriter out;
	String INPUT = "";
	
	int level;
	
	public GrowAI(int level)
	{
		this.level = level;
	}
	
	final int PASS_WHEN_CHECKMATE = 0;
	
	public String guess(State s)
	{
		int n = s.g.size();
//		DJSetList ds = new DJSetList(n);
		RestorableDisjointSet2 rds = new RestorableDisjointSet2(n, n+5);
		for(List<Edge> row : s.g){
			for(Edge e : row){
				if(e.owner == s.P)rds.union(e.x, e.y);
			}
		}
		
		long ec = go(level, level, s, rds);
		if(ec == -1)return "";
		return (ec>>>32) + " " + ((int)ec);
	}
	
	Random gen = new Random(114514);
	
	long go(int rem, int dep, State s, RestorableDisjointSet2 rds)
	{
		int n = s.g.size();
		List<Edge> cans = new ArrayList<>();
		Set<Long> set = new HashSet<>();
		for(int i = 0;i < n;i++){
			List<Edge> row = s.g.get(i);
			for(Edge e : row){
				// 未所有で自分のクラスタ同士をくっつける辺をcansに追加
				if(e.x == i && e.owner == -1 && !rds.equiv(e.x, e.y) && set.add((long)Math.min(e.x, e.y)<<32|Math.max(e.x, e.y))){
					cans.add(e);
				}
			}
		}
		
		// TODO 候補辺がない場合は妨害に回る
		
		// 候補辺がない場合適当に返す
		if(cans.size() == 0){
			if(rem == dep){
				if(PASS_WHEN_CHECKMATE == 1){
					return -1;
				}else{
					for(int i = 0;i < n;i++){
						List<Edge> row = s.g.get(i);
						for(Edge e : row){
							if(e.x == i && e.owner == -1){
								return (long)e.x<<32|e.y;
							}
						}
					}
					throw new RuntimeException();
				}
			}else{
				return 0;
			}
		}
		
		// 候補辺を選んだときの追加スコア
//		tr("START");
		List<Datum> data = new ArrayList<>();
		for(Edge e : cans){
			long plus = 0;
			{
				int rx = rds.root(e.x);
				for(int cur = rx;cur != -1;cur = rds.next[cur]){
					if(s.mines.get(cur)){
//						long cha = plus;
						int ry = rds.root(e.y);
						for(int tar = ry;tar != -1; tar = rds.next[tar]){
							long d = s.mindistss.get(cur).get(tar);
							plus += d*d;
						}
//						tr(plus-cha, cur);
					}
				}
			}
			{
				int rx = rds.root(e.y);
				for(int cur = rx;cur != -1;cur = rds.next[cur]){
					if(s.mines.get(cur)){
//						long cha = plus;
						int ry = rds.root(e.x);
						for(int tar = ry;tar != -1; tar = rds.next[tar]){
							long d = s.mindistss.get(cur).get(tar);
							plus += d*d;
						}
//						tr(plus-cha, cur);
					}
				}
			}
//			tr(e, plus);
			data.add(new Datum(e, plus));
		}
		
		data.sort((x, y) -> -Long.compare(x.score, y.score)); // スコア降順にソート
		if(rem < dep){
			int ohp = rds.hp;
			List<Long> list = new ArrayList<>();
			for(int i = 0;i < data.size() && i <= 20;i++){
				Edge e = data.get(i).e;
				rds.union(e.x, e.y);
				long val = data.get(i).score + (rem > 0 ? go(rem-1, dep, s, rds) : 0);
				rds.revert(ohp);
				list.add(val);
			}
			Collections.sort(list);
			long ret = 0;
			for(int i = list.size()-1;i >= 0;i--){
				ret = ret * 2 / 3 + list.get(i);
			};
			return ret;
		}else{
			int ohp = rds.hp;
			long ret = 0;
			long arg = -1;
			for(int i = Math.min(cans.size()-1, 20);i >= 0;i--){
				Datum d = data.get(i);
				rds.union(d.e.x, d.e.y);
				long val = d.score + (rem > 0 ? go(rem-1, dep, s, rds) : 0);
				if(val > ret){
					ret = val;
					arg = (long)d.e.x<<32|d.e.y;
				}
				rds.revert(ohp);
			}
//			tr("ret", ret);
			return arg;
		}
	}
	
	public static class DJSetList {
		public int[] upper;
		public int[] next;
		public int[] tail;

		public DJSetList(int n) {
			upper = new int[n];
			Arrays.fill(upper, -1);
			next = new int[n];
			tail = new int[n];
			Arrays.fill(next, -1);
			for(int i = 0;i < n;i++)tail[i] = i;
		}

		public int root(int x) {
			return upper[x] < 0 ? x : (upper[x] = root(upper[x]));
		}

		public boolean equiv(int x, int y) {
			return root(x) == root(y);
		}

		public boolean union(int x, int y) {
			x = root(x);
			y = root(y);
			if (x != y) {
				if (upper[y] < upper[x]) {
					int d = x;
					x = y;
					y = d;
				}
				next[tail[x]] = y;
				tail[x] = tail[y];
				
				upper[x] += upper[y];
				upper[y] = x;
			}
			return x == y;
		}

		public int count() {
			int ct = 0;
			for (int u : upper)
				if (u < 0)
					ct++;
			return ct;
		}
	}
	
	public static class RestorableDisjointSet2 {
		public int[] upper; // minus:num_element(root) plus:root(normal)
		private int[] targets;
		private int[] histupper;
		public int[] next;
		public int[] tail;
		public int[] histtail;
		
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
			// 
			hp++;
		}
		
		public void revert(int to)
		{
			while(hp > to){
				upper[targets[hp-1]] = histupper[hp-1];
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
			state.mindistss = mindistss;
			if(F == 1){
				state.futures = new ArrayList<>();
				for(int i = 0;i < N;i++)state.futures.add(null);
			}
			if(S == 1){
				state.charges = new ArrayList<>();
				for(int i = 0;i < C;i++)state.charges.add(0);
			}
			out.println(toBase64(state));
			out.println(0);
		}else if(phase == 'G'){
			// ゲーム中入力
			State state = (State)fromBase64(ns());
			int C = state.C;
			outer:
			for(int i = 0;i < C;i++){
				int L = ni();
				int[] a = new int[L];
				for(int j = 0;j < L;j++){
					a[j] = ni();
				}
				for(int j = 0;j < L-1;j++){
					int s = a[j], t = a[j+1];
					for(Edge e : state.g.get(s)){
						if((e.x^e.y^s) == t && e.owner == -1){
							e.owner = i;
							continue outer;
						}
					}
				}
			}
			
			String output = guess(state);
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
	
	static class Datum
	{
		Edge e;
		long score;
		public Datum(Edge e, long score) {
			this.e = e;
			this.score = score;
		}
	}
	
	static class Edge implements Serializable
	{
		private static final long serialVersionUID = 5180071263476967427L;
		int x, y;
		int owner; // 辺の所有者。いないときは-1
		
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
		new GrowAI(3).run();
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
		System.out.println(Arrays.deepToString(o));
	}
}
