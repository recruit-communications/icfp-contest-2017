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

class PassOnlyAI2 {
	public InputStream is;
	public PrintWriter out;
	String INPUT = "";

	public void solve() {
		char phase = ns().charAt(0);
		if(phase == '?'){
			// 初回入力
			out.println("PASSONLY");
			out.flush();
		}else if(phase == 'I'){
			// 初回入力2
			int C = ni(), P = ni(), F = ni(), S = ni(), O = ni();
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
			state.O = O;
			state.phase = 0;
			state.mindistss = mindistss;
			if(F == 1){
				state.futures = new ArrayList<>();
				for(int i = 0;i < N;i++)state.futures.add(null);
			}
			if(S == 1){
				state.charges = new ArrayList<>();
				for(int i = 0;i < C;i++)state.charges.add(0);
			}
			if(O == 1){
				state.options = new ArrayList<>();
				int z = mines.cardinality();
				for(int i = 0;i < C;i++)state.options.add(z);
			}
			out.println(toBase64(state));
			out.println(0);
		}else if(phase == 'G'){
			// ゲーム中入力
			State state = (State)fromBase64(ns());
			int C = state.C;
			int[][] moves = new int[C][];
			for(int i = 0;i < C;i++){
				int L = ni();
				int[] a = new int[L];
				for(int j = 0;j < L;j++){
					a[j] = ni();
				}
				moves[i] = a;
			}
			
			for(int z = 0, i = state.P;z < C;z++,i++){
				if(i == C)i = 0;
				int[] a = moves[i];
				int L = a.length;
				if(state.S == 1 && L == 0 && (state.phase > 0 || state.phase == 0 && i < state.P)){
					// 初回のダミーpassに注意してチャージ
					inc(state.charges, i, 1);
				}
				inner:
				for(int j = 0;j < L-1;j++){
					int s = a[j], t = a[j+1];
					for(Edge e : state.g.get(s)){
						if((e.x^e.y^s) == t){
							if(e.owner == -1){
								e.owner = i;
							}else if(state.O == 1 && state.options.get(i) >= 1){
								inc(state.options, i, -1);
								e.owner2 = i;
							}
							continue inner;
						}
					}
					throw new RuntimeException("invalid input");
				}
			}
			
			state.phase++;
			out.println(toBase64(state));
			out.println(state.P);
		}else{
			throw new RuntimeException();
		}
	}
	
	public static void inc(List<Integer> list, int id, int x)
	{
		list.set(id, list.get(id) + x);
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
		int F, S, O; // future splurge option対応フラグ
		int phase; // 何回目か
		List<List<Edge>> g; // グラフ
		List<Integer> charges; // splurgeチャージ量
		List<Integer> options; // option残り回数
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
		int owner2;
		
		public Edge(int x, int y) {
			this.x = x;
			this.y = y;
			this.owner = -1;
			this.owner2 = -1;
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
		new PassOnlyAI2().run();
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
