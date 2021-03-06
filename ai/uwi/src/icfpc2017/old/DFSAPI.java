package icfpc2017.old;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/**
 * 単純に3手先まで読むやつ
 *
 */
public class DFSAPI {
	static Scanner in;
	static PrintWriter out;
	static String INPUT = "";

	static void solve() {
		char phase = in.next().charAt(0);
		if(phase == '?'){
			// 初回入力
			out.println(DFSAPI.class.getSimpleName());
			out.flush();
		}else if(phase == 'I'){
			// 初回入力2
			int N = ni(), P = ni(), F = ni();
			int V = ni(), E = ni(), M = ni();
			List<List<Edge>> g = new ArrayList<>();
			for(int i = 0;i < V;i++)g.add(new ArrayList<>());
			for(int i = 0;i < E;i++){
				int s = ni(), t = ni();
				Edge e = new Edge(s, t);
				g.get(s).add(e);
				g.get(t).add(e);
			}
			BitSet mines = new BitSet();
			for(int i = 0;i < M;i++){
				mines.set(ni());
			}
			List<List<Integer>> mindistss = new ArrayList<>();
			for(int i = 0;i < V;i++){
				if(mines.get(i)){
					mindistss.add(mindists(g, i));
				}else{
					mindistss.add(null);
				}
			}
			
			State state = new State();
			state.g = g;
			state.mines = mines;
			state.N = N;
			state.P = P;
			state.mindistss = mindistss;
			if(F == 1){
				state.futures = new ArrayList<>();
				for(int i = 0;i < V;i++)state.futures.add(null);
			}
			out.println(toBase64(state));
		}else if(phase == 'G'){
			// ゲーム中入力
			State state = (State)fromBase64(in.next());
			int N = state.N;
			outer:
			for(int i = 0;i < N;i++){
				int s = ni(), t = ni();
				if(s != -1 && t != -1){
					for(Edge e : state.g.get(s)){
						if((e.x^e.y^s) == t && e.owner == -1){
							e.owner = i;
							continue outer;
						}
					}
					throw new RuntimeException(); // ここにはこない
				}
			}
			
			out.println(toBase64(state));
			
			int E = 0;
			for(List<Edge> row : state.g)E += row.size();
			E /= 2;
			
			RestorableDisjointSet2 rds = new RestorableDisjointSet2(state.g.size(), E+5);
			for(List<Edge> row : state.g){
				for(Edge e : row){
					if(e.owner == state.P){
						rds.union(e.x, e.y);
					}
				}
			}
			
			Hand h = dfs(3, rds, state);
			System.err.println("hand:" + h);
			out.println(h.s + " " + h.t);
		}else{
			throw new RuntimeException();
		}
	}
	
	static long calcScore(RestorableDisjointSet2 rds, State s)
	{
		long score = 0;
		for(int i = 0;i < s.g.size();i++){
			if(s.mines.get(i)){
				for(int j = 0;j < s.g.size();j++){
					if(rds.equiv(i, j)){
						long d = s.mindistss.get(i).get(j);
						score += d*d;
					}
				}
			}
		}
		return score;
	}
	
	static class Hand
	{
		long score;
		int s, t;
		public Hand(long score, int s, int t) {
			this.score = score;
			this.s = s;
			this.t = t;
		}
		@Override
		public String toString() {
			return "Hand [score=" + score + ", s=" + s + ", t=" + t + "]";
		}
	}
	
	static long dfsCore(int rem, RestorableDisjointSet2 rds, State s)
	{
		int ohp = rds.hp;
		List<Long> scores = new ArrayList<>();
		for(int i = 0;i < s.g.size();i++){
			for(Edge e : s.g.get(i)){
				if(e.owner == -1 && e.x == i){
					e.owner = s.P;
					rds.union(e.x, e.y);
					long score = rem == 0 ? calcScore(rds, s) : dfsCore(rem-1, rds, s);
					scores.add(score);
					rds.revert(ohp);
				}
			}
		}
		Collections.sort(scores);
		long ret = 0;
		for(long v : scores){
			ret = (ret+v)/2;
		}
		return ret;
	}
	
	static Hand dfs(int rem, RestorableDisjointSet2 rds, State s)
	{
		int ohp = rds.hp;
		int bx = -1, by = -1;
		long maxscore = -1;
		for(int i = 0;i < s.g.size();i++){
			for(Edge e : s.g.get(i)){
				if(e.owner == -1 && e.x == i){
					e.owner = s.P;
					rds.union(e.x, e.y);
					long score = dfsCore(rem-1, rds, s);
					if(score > maxscore){
						maxscore = score;
						bx = e.x; by = e.y;
					}
					rds.revert(ohp);
				}
			}
		}
		return new Hand(maxscore, bx, by);
	}
	
	public static class RestorableDisjointSet2 {
		public int[] upper; // minus:num_element(root) plus:root(normal)
		private int[] targets;
		private int[] histupper;
		public int hp = 0;
		
		public RestorableDisjointSet2(int n, int m)
		{
			upper = new int[n];
			Arrays.fill(upper, -1);
			
			targets = new int[2*m];
			histupper = new int[2*m];
			// 
//				w = new int[n];
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
				record(x); record(y);
				upper[x] += upper[y];
				// 
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
		int N; // プレー人数
		int P; // お前のID(0~N-1)
		List<List<Edge>> g; // グラフ
		BitSet mines; // mineかどうか
		List<List<Integer>> mindistss; // 最短経路長
		List<Future> futures;
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

	public static void main(String[] args) throws Exception {
		in = INPUT.isEmpty() ? new Scanner(System.in) : new Scanner(INPUT);
		out = new PrintWriter(System.out);

		solve();
		out.flush();
	}

	static int ni() {
		return Integer.parseInt(in.next());
	}

	static long nl() {
		return Long.parseLong(in.next());
	}

	static double nd() {
		return Double.parseDouble(in.next());
	}

	static void tr(Object... o) {
		if (INPUT.length() != 0)
			System.out.println(Arrays.deepToString(o));
	}
}
