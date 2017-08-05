package icfpc2017;
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
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class ANTIGROWAPI {
	static Scanner in;
	static PrintWriter out;
	static String INPUT = "";

	static void solve() {
		char phase = in.next().charAt(0);
		if(phase == '?'){
			// 初回入力
			out.println("PASSONLY");
			out.flush();
		}else if(phase == 'I'){
			// 初回入力2
			int N = ni(), P = ni();
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
			out.println(-1 + " " + -1);
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
		int N; // プレー人数
		int P; // お前のID(0~N-1)
		List<List<Edge>> g; // グラフ
		BitSet mines; // mineかどうか
		List<List<Integer>> mindistss; // 最短経路長
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
