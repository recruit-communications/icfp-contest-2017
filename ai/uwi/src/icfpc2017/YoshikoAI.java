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
import java.util.InputMismatchException;
import java.util.List;
import java.util.Queue;

class YoshikoAI {
	public InputStream is;
	public PrintWriter out;
	String INPUT = "";
	
	String guess(State s)
	{
		int n = s.N;
		DJSet ds = new DJSet(n); // TODO これもStateに入れてもいいかも
		for(Edge e : s.es){
			if(e.owner == s.P){
				ds.union(e.x, e.y);
			}
		}
		int[] mcount = new int[n];
		for(int i = 0;i < n;i++){
			if(s.mines.get(i)){
				mcount[ds.root(i)]++;
			}
		}
		long max = -1;
		Edge best = null;
		for(Edge e : s.es){
			if(e.owner == -1 && !ds.equiv(e.x, e.y)){
				long score = (long)(mcount[ds.root(e.x)]+1) * (mcount[ds.root(e.y)]+1);
				if(score > max){
					max = score;
					best = e;
				}
			}
		}
		if(best == null){
			for(Edge e : s.es){
				if(e.owner == -1){
					long score = mcount[ds.root(e.x)];
					if(score > max){
						max = score;
						best = e;
					}
				}
			}
		}
		assert best != null;
		return best.x + " " + best.y;
	}
	
	public static class DJSet {
		public int[] upper;

		public DJSet(int n) {
			upper = new int[n];
			Arrays.fill(upper, -1);
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


	public void solve() {
		char phase = ns().charAt(0);
		if(phase == '?'){
			// 初回入力
			out.println("僕らは目指した〜");
			out.flush();
		}else if(phase == 'I'){
			// 初回入力2
			int C = ni(), P = ni(), F = ni(), S = ni();
			int N = ni(), M = ni(), K = ni();
			List<Edge> es = new ArrayList<>();
			for(int i = 0;i < M;i++){
				int s = ni(), t = ni();
				Edge e = new Edge(Math.min(s, t), Math.max(s, t));
				es.add(e);
			}
			es.sort((a, b) -> {
				if(a.x != b.x)return a.x - b.x;
				return a.y - b.y;
			});
			BitSet mines = new BitSet();
			for(int i = 0;i < K;i++){
				mines.set(ni());
			}
			
			State state = new State();
			state.es = es;
			state.mines = mines;
			state.C = C;
			state.P = P;
			state.N = N;
			state.F = F;
			state.S = S;
			state.phase = 0;
			if(F == 1){
				state.futures = new ArrayList<>();
				for(int i = 0;i < N;i++)state.futures.add(null);
			}
			out.println(toBase64(state));
			out.println(0);
		}else if(phase == 'G'){
			// ゲーム中入力
			State state = (State)fromBase64(ns());
			int C = state.C;
			List<Edge> added = new ArrayList<>();
			for(int i = 0;i < C;i++){
				int L = ni();
				int[] a = new int[L];
				for(int j = 0;j < L;j++){
					a[j] = ni();
				}
				for(int j = 0;j < L-1;j++){
					int s = a[j], t = a[j+1];
					if(s > t){
						int d = s; s = t; t = d;
					}
					Edge e = new Edge(s, t);
					e.owner = i;
					added.add(e);
				}
			}
			added.sort((a, b) -> {
				if(a.x != b.x)return a.x - b.x;
				return a.y - b.y;
			});
			int q = 0;
			for(int p = 0;p < added.size();p++){
				Edge e = added.get(p);
				while(q < state.es.size() && (state.es.get(q).owner != -1 || state.es.get(q).x < e.x || state.es.get(q).x == e.x && state.es.get(q).y < e.y)){
					q++;
				}
				if(q < state.es.size() && state.es.get(q).x == e.x && state.es.get(q).y == e.y){
					state.es.get(q).owner = e.owner;
				}
			}
			
			state.phase++;
			out.println(toBase64(state));
			out.print(state.P + " ");
			out.println(guess(state));
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
		int N; // 頂点数
		int F, S;
		int phase; // 何回目か
		List<Edge> es; // 辺集合
		BitSet mines; // mineかどうか
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
		
		public Edge(int x, int y) {
			this.x = x;
			this.y = y;
			this.owner = -1;
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
		new YoshikoAI().run();
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
