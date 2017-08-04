package icfpc2017;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

public class T {
	static Scanner in;
	static PrintWriter out;
	static String INPUT = "";

	static void solve() {
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
