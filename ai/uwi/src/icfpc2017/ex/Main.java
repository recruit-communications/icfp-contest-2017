package icfpc2017.ex;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
//		String enigma = "[{\"source\":23,\"target\":27},{\"source\":26,\"target\":27},{\"source\":3,\"target\":5},{\"source\":11,\"target\":14},{\"source\":24,\"target\":27},{\"source\":19,\"target\":22},{\"source\":20,\"target\":22},{\"source\":1,\"target\":4},{\"source\":0,\"target\":7},{\"source\":21,\"target\":22},{\"source\":2,\"target\":4},{\"source\":2,\"target\":6},{\"source\":30,\"target\":32},{\"source\":29,\"target\":32},{\"source\":31,\"target\":32},{\"source\":10,\"target\":15},{\"source\":28,\"target\":32},{\"source\":1,\"target\":8},{\"source\":2,\"target\":9},{\"source\":25,\"target\":27},{\"source\":4,\"target\":18},{\"source\":5,\"target\":18},{\"source\":5,\"target\":19},{\"source\":17,\"target\":19},{\"source\":17,\"target\":20},{\"source\":16,\"target\":20},{\"source\":16,\"target\":21},{\"source\":4,\"target\":21},{\"source\":18,\"target\":22},{\"source\":2,\"target\":23},{\"source\":0,\"target\":23},{\"source\":0,\"target\":24},{\"source\":3,\"target\":24},{\"source\":3,\"target\":25},{\"source\":1,\"target\":25},{\"source\":1,\"target\":26},{\"source\":2,\"target\":26},{\"source\":12,\"target\":28},{\"source\":9,\"target\":28},{\"source\":9,\"target\":29},{\"source\":8,\"target\":29},{\"source\":8,\"target\":30},{\"source\":11,\"target\":30},{\"source\":11,\"target\":31},{\"source\":12,\"target\":31},{\"source\":13,\"target\":33},{\"source\":10,\"target\":33},{\"source\":10,\"target\":34},{\"source\":7,\"target\":34},{\"source\":7,\"target\":35},{\"source\":6,\"target\":35},{\"source\":6,\"target\":36},{\"source\":13,\"target\":36},{\"source\":33,\"target\":37},{\"source\":34,\"target\":37},{\"source\":35,\"target\":37},{\"source\":36,\"target\":37},{\"source\":23,\"target\":35},{\"source\":18,\"target\":25},{\"source\":26,\"target\":29}";
//		String enigma = "{\"source\":27,\"target\":29},{\"source\":15,\"target\":17},{\"source\":16,\"target\":17},{\"source\":28,\"target\":29},{\"source\":15,\"target\":16},{\"source\":27,\"target\":28},{\"source\":12,\"target\":14},{\"source\":9,\"target\":11},{\"source\":10,\"target\":11},{\"source\":13,\"target\":14},{\"source\":9,\"target\":10},{\"source\":12,\"target\":13},{\"source\":0,\"target\":9},{\"source\":6,\"target\":9},{\"source\":6,\"target\":10},{\"source\":8,\"target\":10},{\"source\":0,\"target\":11},{\"source\":8,\"target\":11},{\"source\":6,\"target\":12},{\"source\":3,\"target\":12},{\"source\":3,\"target\":13},{\"source\":7,\"target\":13},{\"source\":6,\"target\":14},{\"source\":7,\"target\":14},{\"source\":8,\"target\":15},{\"source\":7,\"target\":15},{\"source\":7,\"target\":16},{\"source\":5,\"target\":16},{\"source\":8,\"target\":17},{\"source\":5,\"target\":17},{\"source\":24,\"target\":26},{\"source\":21,\"target\":23},{\"source\":22,\"target\":23},{\"source\":25,\"target\":26},{\"source\":21,\"target\":22},{\"source\":24,\"target\":25},{\"source\":3,\"target\":21},{\"source\":18,\"target\":21},{\"source\":18,\"target\":22},{\"source\":20,\"target\":22},{\"source\":3,\"target\":23},{\"source\":20,\"target\":23},{\"source\":18,\"target\":24},{\"source\":1,\"target\":24},{\"source\":1,\"target\":25},{\"source\":19,\"target\":25},{\"source\":18,\"target\":26},{\"source\":19,\"target\":26},{\"source\":20,\"target\":27},{\"source\":19,\"target\":27},{\"source\":19,\"target\":28},{\"source\":4,\"target\":28},{\"source\":20,\"target\":29},{\"source\":4,\"target\":29},{\"source\":36,\"target\":38},{\"source\":33,\"target\":35},{\"source\":34,\"target\":35},{\"source\":37,\"target\":38},{\"source\":33,\"target\":34},{\"source\":36,\"target\":37},{\"source\":5,\"target\":33},{\"source\":30,\"target\":33},{\"source\":30,\"target\":34},{\"source\":32,\"target\":34},{\"source\":5,\"target\":35},{\"source\":32,\"target\":35},{\"source\":30,\"target\":36},{\"source\":4,\"target\":36},{\"source\":4,\"target\":37},{\"source\":31,\"target\":37},{\"source\":30,\"target\":38},{\"source\":31,\"target\":38},{\"source\":32,\"target\":39},{\"source\":31,\"target\":39},{\"source\":31,\"target\":40},{\"source\":2,\"target\":40},{\"source\":32,\"target\":41},{\"source\":2,\"target\":41},{\"source\":39,\"target\":40},{\"source\":40,\"target\":41},{\"source\":39,\"target\":41}";
		String enigma = "{\"source\":81,\"target\":91},{\"source\":57,\"target\":68},{\"source\":32,\"target\":68},{\"source\":52,\"target\":68},{\"source\":32,\"target\":52},{\"source\":5,\"target\":49},{\"source\":5,\"target\":74},{\"source\":8,\"target\":35},{\"source\":53,\"target\":71},{\"source\":59,\"target\":77},{\"source\":22,\"target\":53},{\"source\":2,\"target\":24},{\"source\":27,\"target\":40},{\"source\":25,\"target\":40},{\"source\":36,\"target\":40},{\"source\":40,\"target\":61},{\"source\":25,\"target\":44},{\"source\":3,\"target\":78},{\"source\":30,\"target\":85},{\"source\":3,\"target\":67},{\"source\":61,\"target\":67},{\"source\":65,\"target\":67},{\"source\":1,\"target\":30},{\"source\":1,\"target\":65},{\"source\":22,\"target\":51},{\"source\":42,\"target\":72},{\"source\":42,\"target\":53},{\"source\":53,\"target\":72},{\"source\":22,\"target\":72},{\"source\":6,\"target\":45},{\"source\":64,\"target\":87},{\"source\":17,\"target\":50},{\"source\":50,\"target\":82},{\"source\":1,\"target\":82},{\"source\":1,\"target\":67},{\"source\":54,\"target\":77},{\"source\":16,\"target\":38},{\"source\":9,\"target\":18},{\"source\":7,\"target\":10},{\"source\":11,\"target\":12},{\"source\":6,\"target\":14},{\"source\":7,\"target\":15},{\"source\":10,\"target\":15},{\"source\":8,\"target\":20},{\"source\":0,\"target\":21},{\"source\":14,\"target\":23},{\"source\":4,\"target\":24},{\"source\":10,\"target\":26},{\"source\":15,\"target\":26},{\"source\":4,\"target\":27},{\"source\":11,\"target\":29},{\"source\":12,\"target\":29},{\"source\":3,\"target\":30},{\"source\":13,\"target\":31},{\"source\":5,\"target\":32},{\"source\":0,\"target\":34},{\"source\":31,\"target\":36},{\"source\":34,\"target\":37},{\"source\":18,\"target\":38},{\"source\":0,\"target\":39},{\"source\":21,\"target\":39},{\"source\":33,\"target\":39},{\"source\":13,\"target\":41},{\"source\":31,\"target\":41},{\"source\":33,\"target\":43},{\"source\":42,\"target\":45},{\"source\":26,\"target\":46},{\"source\":7,\"target\":47},{\"source\":10,\"target\":47},{\"source\":15,\"target\":47},{\"source\":34,\"target\":47},{\"source\":37,\"target\":47},{\"source\":43,\"target\":48},{\"source\":0,\"target\":49},{\"source\":21,\"target\":49},{\"source\":39,\"target\":49},{\"source\":35,\"target\":51},{\"source\":10,\"target\":52},{\"source\":16,\"target\":54},{\"source\":4,\"target\":55},{\"source\":24,\"target\":55},{\"source\":0,\"target\":56},{\"source\":21,\"target\":56},{\"source\":34,\"target\":56},{\"source\":49,\"target\":56},{\"source\":52,\"target\":56},{\"source\":9,\"target\":57},{\"source\":12,\"target\":58},{\"source\":19,\"target\":58},{\"source\":23,\"target\":58},{\"source\":29,\"target\":58},{\"source\":15,\"target\":59},{\"source\":14,\"target\":60},{\"source\":23,\"target\":60},{\"source\":58,\"target\":60},{\"source\":26,\"target\":62},{\"source\":46,\"target\":62},{\"source\":59,\"target\":62},{\"source\":46,\"target\":63},{\"source\":59,\"target\":63},{\"source\":62,\"target\":63},{\"source\":14,\"target\":64},{\"source\":19,\"target\":64},{\"source\":23,\"target\":64},{\"source\":58,\"target\":64},{\"source\":60,\"target\":64},{\"source\":13,\"target\":66},{\"source\":31,\"target\":66},{\"source\":36,\"target\":66},{\"source\":45,\"target\":69},{\"source\":14,\"target\":70},{\"source\":53,\"target\":70},{\"source\":20,\"target\":71},{\"source\":28,\"target\":72},{\"source\":7,\"target\":73},{\"source\":10,\"target\":73},{\"source\":34,\"target\":73},{\"source\":47,\"target\":73},{\"source\":52,\"target\":73},{\"source\":43,\"target\":74},{\"source\":48,\"target\":74},{\"source\":11,\"target\":75},{\"source\":12,\"target\":75},{\"source\":29,\"target\":75},{\"source\":58,\"target\":75},{\"source\":33,\"target\":76},{\"source\":43,\"target\":76},{\"source\":48,\"target\":76},{\"source\":74,\"target\":76},{\"source\":20,\"target\":77},{\"source\":61,\"target\":78},{\"source\":22,\"target\":79},{\"source\":69,\"target\":79},{\"source\":13,\"target\":80},{\"source\":31,\"target\":80},{\"source\":44,\"target\":80},{\"source\":57,\"target\":81},{\"source\":17,\"target\":82},{\"source\":74,\"target\":82},{\"source\":33,\"target\":83},{\"source\":43,\"target\":83},{\"source\":48,\"target\":83},{\"source\":76,\"target\":83},{\"source\":13,\"target\":84},{\"source\":41,\"target\":84},{\"source\":21,\"target\":85},{\"source\":33,\"target\":85},{\"source\":39,\"target\":85},{\"source\":75,\"target\":85},{\"source\":23,\"target\":86},{\"source\":37,\"target\":86},{\"source\":58,\"target\":86},{\"source\":4,\"target\":87},{\"source\":24,\"target\":87},{\"source\":55,\"target\":87},{\"source\":2,\"target\":88},{\"source\":6,\"target\":88},{\"source\":28,\"target\":88},{\"source\":2,\"target\":89},{\"source\":28,\"target\":89},{\"source\":88,\"target\":89},{\"source\":59,\"target\":90},{\"source\":63,\"target\":90},{\"source\":46,\"target\":91},{\"source\":59,\"target\":91},{\"source\":62,\"target\":91},{\"source\":63,\"target\":91},{\"source\":90,\"target\":91},{\"source\":33,\"target\":92},{\"source\":43,\"target\":92},{\"source\":48,\"target\":92},{\"source\":76,\"target\":92},{\"source\":83,\"target\":92},{\"source\":50,\"target\":93},{\"source\":10,\"target\":94},{\"source\":15,\"target\":94},{\"source\":26,\"target\":94},{\"source\":62,\"target\":94},{\"source\":73,\"target\":94},{\"source\":11,\"target\":95},{\"source\":40,\"target\":95},{\"source\":78,\"target\":95},{\"source\":0,\"target\":96},{\"source\":21,\"target\":96},{\"source\":39,\"target\":96},{\"source\":56,\"target\":96},{\"source\":85,\"target\":96}]";
		enigma = enigma.replaceAll("[^0-9]", " ");
		long[][] g = new long[97][97];
		try(Scanner sc = new Scanner(enigma)){
			while(sc.hasNextInt()){
				int f = sc.nextInt();
				int t = sc.nextInt();
				tr(f, t);
				g[f][t] = g[t][f] = 1;
			}
		}
//		tr(enigma);
		tr(minCut(g));
		
		
//		circle();
	}
	
	
	static void circle()
	{
		int[] from = new int[65];
		int[] to = new int[65];
		int p = 0;
		for(int i = 0;i < 13;i++){
			from[p] = i; to[p] = (i+1)%13; p++;
		}
		for(int i = 0;i < 13;i++){
			from[p] = i+13; to[p] = (i+1)%13+13; p++;
		}
		for(int i = 0;i < 13;i++){
			from[p] = i; to[p] = i+13; p++;
		}
		for(int i = 0;i < 13;i++){
			from[p] = i; to[p] = (i+1)%13+13; p++;
		}
		for(int i = 0;i < 13;i++){
			from[p] = i+13; to[p] = 26; p++;
		}
		
		int[][] es = {
		{
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
			12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,	
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
			12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,	
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
		},
		{
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0,
			13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,	12,
			12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,	
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0,
			24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24
		}};

		
		int n = 27;
		long[][] g = new long[n][n];
		for(int i = 0;i < p;i++){
			g[from[i]][to[i]] = 1;
			g[to[i]][from[i]] = 1;
		}
		tr(minCut(g));
	}
	
	public static void tr(Object... o) { System.out.println(Arrays.deepToString(o)); }
	
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
	
}

