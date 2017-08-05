package icfpc2017;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.SplittableRandom;

public class LocalSimulator {
	
	static Logger logger = new Logger("/tmp/icfpclog", LocalDateTime.now());
	
	public static void main(String[] args) {
		int win = 0, lose = 0, draw = 0;
		for(int i = 0;i < 1000;i++){
//			int N = 10, M = 20, K = 5;
//			int N = 10, M = 15, K = 5;
			int N = 50, M = 80, K = 20;
//			int N = 5, M = 8, K = 2;
//			int N = 8, M = 12, K = 3;
			long[] scores = game(i, N, M, K, GrowAI.class, GrowAI.class);
			if(scores[0] > scores[1]){
				win++;
			}else if(scores[0] == scores[1]){
				draw++;
			}else{
				lose++;
			}
		}
		tr(win, draw, lose);
	}
	
	static long[] game(long seed, int N, int M, int K, Class<?>... players)
	{
		int C = players.length;
		
		// 環境作成
		SplittableRandom gen = new SplittableRandom(seed);
		int[][] es = genGraph(gen, N, M, false);
		int[] mines = distinct(N, K, gen);
		
		String[] names = new String[C];
		String[] states = new String[C];
		// ターンの履歴
		int[][] prevs = new int[C][];
		
		int[] colors = new int[M];
		Arrays.fill(colors, -1);
		int[][][] futures = new int[C][][];
		
		// ?
		for(int i = 0;i < C;i++){
			String name = run(players[i], "?");
			names[i] = name;
		}
		
		Arrays.sort(mines);
		
		// I
		for(int i = 0;i < C;i++){
			StringBuilder sb = new StringBuilder();
			sb.append("I\n");
			sb.append(C + " " + i + " " + 1 + "\n");
			sb.append(N + " " + M + " " + K + "\n");
			for(int j = 0;j < M;j++){
				sb.append(es[0][j] + " " + es[1][j] + "\n");
			}
			for(int j = 0;j < K;j++){
				sb.append(mines[j] + " ");
			}
			sb.append("\n");
			
			String res = run(players[i], sb.toString());
			try(Scanner in = new Scanner(res)){
				states[i] = in.next();
				int F = ni(in);
				futures[i] = new int[F][];
				Set<Integer> mineset = new HashSet<>();
				for(int j = 0;j < F;j++){
					futures[i][j] = new int[]{ni(in), ni(in)};
					assert mineset.add(futures[i][j][0]);
					assert Arrays.binarySearch(mines, futures[i][j][0]) >= 0;
				}
			}
		}
		
		// G
		int rem = M;
		game:
		while(true){
			for(int i = 0;i < C;i++){
//				tr("G " + i + " " + names[i]);
				StringBuilder sb = new StringBuilder();
				sb.append("G\n");
				sb.append(states[i] + "\n");
				
				for(int j = 0;j < C;j++){
					if(prevs[j] == null){
						sb.append(-1 + " " + -1 + "\n");
					}else{
						sb.append(prevs[j][0] + " " + prevs[j][1] + "\n");
					}
				}
				String res = run(players[i], sb.toString());
				try(Scanner in = new Scanner(res)){
					states[i] = in.next();
					int P = ni(in);
					assert P == i;
					int x = ni(in), y = ni(in);
					if(x != -1){
						prevs[i] = new int[]{x, y};
						boolean ok = false;
						for(int j = 0;j < M;j++){
							if(es[0][j] == x && es[1][j] == y && colors[j] == -1){
								colors[j] = i;
								ok = true;
								break;
							}
							if(es[0][j] == y && es[1][j] == x && colors[j] == -1){
								colors[j] = i;
								ok = true;
								break;
							}
						}
						if(!ok){
							print(N, M, K, mines, es, colors);
							throw new RuntimeException("invalid move: " + i + " " + x + "," + y);
						}
					}else{
						prevs[i] = null;
					}
					tr("HAND", i, x, y, calcScore(C, N, M, K, es, mines, futures, colors));
				}
				if(--rem == 0)break game;
			}
		}
		
		//// スコア計算
		long[] scores = calcScore(C, N, M, K, es, mines, futures, colors);
		
		logger.log("Game seed #" + seed);
		for(int i = 0;i < C;i++){
//			tr(String.format("score[%d]: %d", i, scores[i]));
			logger.log(String.format("score[%d]: %d", i, scores[i]));
		}
		print(N, M, K, mines, es, colors);
		return scores;
	}
	
	static long[] calcScore(int C, int N, int M, int K, int[][] es, int[] mines, int[][][] futures, int[] colors)
	{
		int[][] g = packU(N, es[0], es[1]);
		
		// 連結成分
		DJSet[] dss = new DJSet[C];
		for(int i = 0;i < C;i++){
			dss[i] = new DJSet(N);
			for(int j = 0;j < M;j++){
				if(colors[j] == i){
					dss[i].union(es[0][j], es[1][j]);
				}
			}
		}
		
		int[][] dists = new int[N][];
		long[] scores = new long[C];
		// 距離分
		for(int i = 0;i < K;i++){
			int mine = mines[i];
			int[] ds = dists(g, mine);
			dists[mine] = ds;
			for(int j = 0;j < N;j++){
				for(int k = 0;k < C;k++){
					if(dss[k].equiv(mine, j)){
						scores[k] += (long)ds[j] * ds[j];
					}
				}
			}
		}
		// futures分
		for(int i = 0;i < C;i++){
			for(int[] f : futures[i]){
				long d = dists[f[0]][f[1]];
				if(dss[i].equiv(f[0], f[1])){
					scores[i] += d*d*d;
				}else{
					scores[i] -= d*d*d;
				}
			}
		}
		return scores;
	}
	
	static void print(int N, int M, int K, int[] mines, int[][] es, int[] colors)
	{
		System.out.println(N + " " + M + " " + K);
		for(int mine : mines){
			System.out.print(mine + " ");
		}
		System.out.println();
		for(int i = 0;i < M;i++){
			System.out.println(es[0][i] + " " + es[1][i] + " " + colors[i]);
		}
	}
	
	static int[] dists(int[][] g, int start)
	{
		int n = g.length;
		int[] q = new int[n];
		int p = 0;
		q[p++] = start;
		int[] d = new int[n];
		Arrays.fill(d, 99999999);
		d[start] = 0;
		for(int z = 0;z < p;z++){
			int cur = q[z];
			for(int e : g[cur]){
				if(d[e] > d[cur] + 1){
					d[e] = d[cur] + 1;
					q[p++] = e;
				}
			}
		}
		return d;
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

	static class Logger
	{
		PrintWriter out;
		boolean withTime;
		
		public Logger(String filehead) { 
			try {
				out = new PrintWriter(filehead + ".log");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		public Logger(String filehead, LocalDateTime ldt) { 
			try {
				out = new PrintWriter(filehead + "-" + ldt.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".log");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		void time() { if(withTime)out.print("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "] ");}
		void log(Object... o) { time();out.println(Arrays.deepToString(o)); out.flush(); }
		void log(String s) { time();out.println(s); out.flush(); }
	}

	
	static String run(Class<?> player, String input)
	{
		try {
			Object instance = player.newInstance();
			player.getField("is").set(instance, new ByteArrayInputStream(input.getBytes()));
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			player.getField("out").set(instance, pw);
			player.getMethod("solve").invoke(instance);
			pw.flush();
			return sw.toString();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException
				| SecurityException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int[] distinct(int n, int k, SplittableRandom gen)
	{
		int[] a = new int[n];
		for(int i = 0;i < n;i++)a[i] = i;
		for(int i = 0;i < n-1 && i < k;i++){
			int j = gen.nextInt(n-i)+i;
			int d = a[j]; a[j] = a[i]; a[i] = d;
		}
		return Arrays.copyOf(a, k);
	}

	
	static int[][] genGraph(SplittableRandom gen, int n, int m, boolean loop)
	{
		int[] f = new int[m];
		int[] t = new int[m];
		for(int i = 0;i < m;i++){
			while(true){
				f[i] = gen.nextInt(n);
				t[i] = gen.nextInt(n);
				if(!loop && f[i] == t[i])continue;
				break;
			}
		}
		return new int[][]{f, t};
	}
	
	public static int[][] packU(int n, int[] from, int[] to){ return packU(n, from, to, from.length); }
	public static int[][] packU(int n, int[] from, int[] to, int sup)
	{
		int[][] g = new int[n][];
		int[] p = new int[n];
		for(int i = 0;i < sup;i++)p[from[i]]++;
		for(int i = 0;i < sup;i++)p[to[i]]++;
		for(int i = 0;i < n;i++)g[i] = new int[p[i]];
		for(int i = 0;i < sup;i++){
			g[from[i]][--p[from[i]]] = to[i];
			g[to[i]][--p[to[i]]] = from[i];
		}
		return g;
	}

	private static class Mock { static String INPUT; static InputStream is; static PrintWriter out; static int lenbuf, ptrbuf; static void solve(){throw new UnsupportedOperationException("Mock!");}};
	
	
	static int ni(Scanner in)
	{
		return Integer.parseInt(in.next());
	}

	private static void tr(Object... o) {
		System.out.println(Arrays.deepToString(o));
	}
}