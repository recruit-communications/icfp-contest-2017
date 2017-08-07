package icfpc2017.ex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ConnectionGame {
	public static void main(String[] args) {
		Random gen = new Random();
		while(true){
			boolean[][] g = makeGraph(gen);
			
			int n = g.length;
			for(int i = 0;i < n;i++){
				for(int j = i+1;j < n;j++){
					if(g[i][j]){
						tr(i, j);
					}
				}
			}
			boolean res = dfs(1, g, new int[n][n]);
			tr(res, getF(g));
//			if(res){
				
//				break;
//			}
		}
	}
	
	static long getF(boolean[][] g)
	{
		int n = g.length;
		List<Edge> es = new ArrayList<>();
		for(int i = 0;i < n;i++){
			for(int j = i+1;j < n;j++){
				if(g[i][j]){
					es.add(new Edge(i, j, 1));
				}
			}
		}
		return maximumFlowDinic(compileWU(n, es), 0, 1);
	}
	
	
	public static void tr(Object... o) { System.out.println(Arrays.deepToString(o)); }
	
	static boolean dfs(int turn, boolean[][] g, int[][] color)
	{
		int n = g.length;
		if(turn == -1){
			DJSet ds = new DJSet(n);
			for(int i = 0;i < n;i++){
				for(int j = i+1;j < n;j++){
					if(color[i][j] == 1){
						ds.union(i, j);
					}
				}
			}
			if(ds.equiv(0, 1))return false;
		}else{
			DJSet ds = new DJSet(n);
			for(int i = 0;i < n;i++){
				for(int j = i+1;j < n;j++){
					if(g[i][j] && color[i][j] != -1){
						ds.union(i, j);
					}
				}
			}
			if(!ds.equiv(0, 1))return false;
		}
		
		for(int i = 0;i < n;i++){
			for(int j = i+1;j < n;j++){
				if(g[i][j] && color[i][j] == 0){
					color[i][j] = turn;
					boolean res = !dfs(-turn, g, color);
					color[i][j] = 0;
					if(res)return true;
				}
			}
		}
		return false;
	}
	
	static boolean[][] makeGraph(Random gen)
	{
		while(true){
			int n = gen.nextInt(6)+3;
			boolean[][] g = new boolean[n][n];
			double p = gen.nextDouble();
			DJSet ds = new DJSet(n);
			for(int i = 0;i < n;i++){
				for(int j = Math.max(2, i+1);j < n;j++){
					g[i][j] = g[j][i] = gen.nextDouble() < p;
					if(g[i][j])ds.union(i, j);
				}
			}
			if(ds.equiv(0, 1)){
				return g;
			}
		}
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
	
	public static class Edge
	{
		public int from, to;
		public int capacity;
		public int flow;
		public Edge complement;
		public boolean cloned;
		
		public Edge(int from, int to, int capacity) {
			this.from = from;
			this.to = to;
			this.capacity = capacity;
		}
	}
	
	public static void reset(Edge[][] g)
	{
		for(Edge[] row : g){
			for(Edge e : row){
				e.flow = e.cloned ? e.capacity : 0;
			}
		}
	}
	
	public static Edge[][] compileWD(int n, List<Edge> edges)
	{
		Edge[][] g = new Edge[n][];
		// cloning
		for(int i = edges.size()-1;i >= 0;i--){
			Edge origin = edges.get(i);
			Edge clone = new Edge(origin.to, origin.from, origin.capacity);
			clone.flow = origin.capacity;
			clone.complement = origin;
			clone.cloned = true;
			origin.complement = clone;
			edges.add(clone);
		}
		
		int[] p = new int[n];
		for(Edge e : edges)p[e.from]++;
		for(int i = 0;i < n;i++)g[i] = new Edge[p[i]];
		for(Edge e : edges)g[e.from][--p[e.from]] = e;
		return g;
	}
	
	public static Edge[][] compileWU(int n, List<Edge> edges)
	{
		Edge[][] g = new Edge[n][];
		// cloning
		for(int i = edges.size()-1;i >= 0;i--){
			Edge origin = edges.get(i);
			Edge clone = new Edge(origin.to, origin.from, origin.capacity*2);
			origin.flow = origin.capacity;
			clone.flow = origin.capacity;
			clone.complement = origin;
			clone.cloned = true;
			origin.complement = clone;
			origin.capacity *= 2;
			edges.add(clone);
		}
		
		int[] p = new int[n];
		for(Edge e : edges)p[e.from]++;
		for(int i = 0;i < n;i++)g[i] = new Edge[p[i]];
		for(Edge e : edges)g[e.from][--p[e.from]] = e;
		return g;
	}	
	
	public static long maximumFlowDinic(Edge[][] g, int source, int sink)
	{
		int n = g.length;
		int[] d = new int[n];
		int[] q = new int[n];
		long ret = 0;
		while(true){
			Arrays.fill(d, -1);
			q[0] = source;
			int r = 1;
			d[source] = 0;
			
			for(int v = 0;v < r;v++){
				int cur = q[v];
				for(Edge ne : g[cur]){
					if(d[ne.to] == -1 && ne.capacity - ne.flow > 0) {
						q[r++] = ne.to;
						d[ne.to] = d[cur] + 1;
					}
				}
			}
			if(d[sink] == -1)break;
			int[] sp = new int[n];
			for(int i = 0;i < n;i++)sp[i] = g[i].length - 1;
			ret += dfsDinic(d, g, sp, source, sink, Long.MAX_VALUE);
		}
		
		return ret;
	}
	
	private static long dfsDinic(int[] d, Edge[][] g, int[] sp, int cur, int sink, long min)
	{
		if(cur == sink)return min;
		long left = min;
		for(int i = sp[cur];i >= 0;i--){
			Edge ne = g[cur][i];
			if(d[ne.to] == d[cur]+1 && ne.capacity - ne.flow > 0){
				long fl = dfsDinic(d, g, sp, ne.to, sink, Math.min(left, ne.capacity - ne.flow));
				if(fl > 0){
					left -= fl;
					ne.flow += fl;
					ne.complement.flow -= fl;
					if(left == 0){
						sp[cur] = i;
						return min;
					}
				}
			}
		}
		sp[cur] = -1;
		return min-left;
	}
	
	public static long maximumFlowDinicNoRec(Edge[][] g, int source, int sink)
	{
		int n = g.length;
		int[] d = new int[n]; // distance
		int[] q = new int[n]; // queue for BFS
		long ret = 0;
		int[] stack = new int[n];
		long[] lefts = new long[n]; // left to flow
		long[] parflow = new long[n];
		int[] probe = new int[n]; // search pointer
		while(true){
			// BFS
			Arrays.fill(d, -1);
			q[0] = source;
			int r = 1;
			d[source] = 0;
			for(int v = 0;v < r;v++){
				int cur = q[v];
				for(Edge ne : g[cur]){
					if(d[ne.to] == -1 && ne.capacity - ne.flow > 0) {
						q[r++] = ne.to;
						d[ne.to] = d[cur] + 1;
					}
				}
			}
			if(d[sink] == -1)break;
			
			// DFS
			for(int i = 0;i < n;i++)probe[i] = g[i].length-1;
			int sp = 0;
			stack[sp] = source;
			lefts[sp] = Long.MAX_VALUE / 10;
			parflow[sp] = 0;
			sp++;
			long delta = 0;
			boolean down = true;
			while(sp > 0){
				int cur = stack[sp-1];
				long left = lefts[sp-1];
				if(cur == sink){
					delta += left;
					parflow[sp-1] = left;
					sp--;
					down = false;
					continue;
				}
				if(!down){
					if(parflow[sp] > 0){
						lefts[sp-1] -= parflow[sp];
						left = lefts[sp-1];
						Edge ne = g[cur][probe[cur]];
						ne.flow += parflow[sp];
						ne.complement.flow -= parflow[sp];
						parflow[sp-1] += parflow[sp];
					}
					if(left > 0)probe[cur]--;
				}
				if(left > 0 && probe[cur] >= 0){
					down = true;
					Edge ne = g[cur][probe[cur]];
					if(d[ne.to] == d[cur]+1 && ne.capacity - ne.flow > 0){
						lefts[sp] = Math.min(left, ne.capacity - ne.flow);
						stack[sp] = ne.to;
						parflow[sp] = 0;
						sp++;
					}else{
						probe[cur]--;
					}
				}else{
					down = false;
					sp--;
				}
			}
			ret += delta;
		}
		return ret;
	}
	
	/**
	 * sourceに一番近い最小カットを返す。
	 * source側がtrue, sink側がfalse
	 * 
	 * @param g
	 * @param source
	 * @param sink
	 * @return
	 */
	public static boolean[] restoreCutPessimistically(Edge[][] g, int src)
	{
		int n = g.length;
		boolean[] ved = new boolean[n];
		ved[src] = true;
		int[] q = new int[n];
		int p = 0;
		q[p++] = src;
		for(int r = 0;r < p;r++){
			int cur = q[r];
			for(Edge e : g[cur]){
				assert e.from == cur;
				if(e.flow < e.capacity && !ved[e.to]){
					ved[e.to] = true;
					q[p++] = e.to;
				}
			}
		}
		return ved;
	}
	
	/**
	 * sinkに一番近い最小カットを返す。
	 * source側がtrue, sink側がfalse
	 * @param g
	 * @param sink
	 * @return
	 */
	public static boolean[] restoreCutOptimisitically(Edge[][] g, int sink)
	{
		int n = g.length;
		boolean[] ved = new boolean[n];
		Arrays.fill(ved, true);
		ved[sink] = false;
		int[] q = new int[n];
		int p = 0;
		q[p++] = sink;
		for(int r = 0;r < p;r++){
			int cur = q[r];
			for(Edge e : g[cur]){
				assert e.from == cur;
				if(e.flow > 0 && ved[e.to]){
					ved[e.to] = false;
					q[p++] = e.to;
				}
			}
		}
		return ved;
	}

}
