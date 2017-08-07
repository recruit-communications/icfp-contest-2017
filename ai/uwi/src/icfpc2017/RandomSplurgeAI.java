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
import java.util.SplittableRandom;

class RandomSplurgeAI {
	public InputStream is;
	public PrintWriter out;
	String INPUT = "";

	static SplittableRandom gen = new SplittableRandom();
	
	public void solve() {
		char phase = ns().charAt(0);
		if(phase == '?'){
			// 初回入力
			out.println("Splatoon");
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
			state.N = N;
			state.M = M;
			state.F = F;
			state.S = S;
			state.O = O;
			state.phase = 0;
			state.mindistss = mindistss;
			if(F == 1){
				state.futures = new ArrayList<>();
				for(int i = 0;i < N;i++)state.futures.add(null);
				for(int i = 0;i < N;i++){
					if(mines.get(i)){
						for(int j = 0;j < 5;j++){ // futureを適当に選ぶ
							int id = gen.nextInt(N);
							if(!mines.get(id)){
								state.futures.set(i, id);
								break;
							}
						}
					}
				}
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
			
			StringBuilder line2 = new StringBuilder();
			line2.append(state.P);
			if(state.S == 0 || gen.nextInt(10) < 7){
				// pass
			}else{
//				int use = gen.nextInt(state.charges.get(state.P)+1);
				int len = state.charges.get(state.P) + 1;
//				tr("charge", state.charges.get(state.P));
				outer:
				for(int grep = 0;grep < 10;grep++){
					int id = gen.nextInt(state.M*2);
					for(int i = 0;i < state.N;i++){
						if(id < state.g.get(i).size()){
							Edge e = state.g.get(i).get(id);
							int vorem = state.O == 1 ? state.options.get(state.P) : 0;
							
							int ok = e.ok(state.P);
							if(ok == 1 && vorem <= 0)ok = -1;
							if(e.marked)ok = -1;
							if(ok == -1)continue;
//							tr(e, ok, state.options, state.P);

							e.marked = true;
							vorem -= ok;
							List<Edge> es = new ArrayList<>();
							es.add(e);
							int ap = e.y;
							inner:
							while(es.size() < len){
								for(int rep = 0;rep < 10;rep++){
									int nid = gen.nextInt(state.g.get(ap).size());
									
									Edge ne = state.g.get(ap).get(nid);
									int lok = ne.ok(state.P);
									if(lok == 1 && vorem <= 0)lok = -1;
									if(ne.marked)lok = -1;
									if(lok == -1)continue;
									
									ne.marked = true;
									vorem -= lok;
									es.add(ne);
									ap = ap ^ ne.x ^ ne.y;
									e = ne;
									continue inner;
								}
								break;
							}
							if(es.size() == 1 && es.get(0).owner != -1){
								line2.append(" " + (-es.get(0).x-1) + " " + (-es.get(0).y-1));
							}else{
								line2.append(" " + es.get(0).x);
								ap = es.get(0).x;
								for(Edge z : es){
									ap ^= z.x ^ z.y;
									line2.append(" " + ap);
								}
							}
							if(state.S == 1){
								state.charges.set(state.P, state.charges.get(state.P) - (es.size() - 1));
							}
							if(state.O == 1){
								state.options.set(state.P, vorem);
							}
							break outer;
						}else{
							id -= state.g.get(i).size();
						}
					}
				}
			}
			state.phase++;
			out.println(toBase64(state));
			out.println(line2);
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
		int N, M;
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
		transient boolean marked;
		
		public Edge(int x, int y) {
			this.x = x;
			this.y = y;
			this.owner = -1;
			this.owner2 = -1;
		}
		
		public int ok(int who)
		{
			if(this.owner == -1)return 0;
			if(this.owner != who && this.owner2 == -1)return 1;
			return -1;
		}

		@Override
		public String toString() {
			return "Edge [x=" + x + ", y=" + y + ", owner=" + owner + ", owner2=" + owner2 + "]";
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
		new RandomSplurgeAI().run();
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
