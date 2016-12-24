import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class homework {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileName = "./input.txt";
		String fileOutput = "./output.txt";
		TerritoryMapper tr = new TerritoryMapper();
		Parser p = new Parser(fileName, tr);
		Territory initialTerritory = tr.terList.get(0);
		if (Territory.mode.equals("MINIMAX")) {
			MiniMax mx = new MiniMax();
			Territory bestMove = mx.miniMax(initialTerritory);
			bestMove.writeToFile(fileOutput);
		} else if (Territory.mode.equals("ALPHABETA")) {
			AlphaBeta ab = new AlphaBeta();
			Territory bestMoveAB = ab.alphaBeta(initialTerritory);
			bestMoveAB.writeToFile(fileOutput);
		}
	}

}

class Parser {
	BufferedReader br;

	public Parser(String fileName, TerritoryMapper tr) {
		try {
			br = new BufferedReader(new FileReader(fileName));
			int N = Integer.parseInt(br.readLine().trim());
			Territory t = new Territory(N);
			String Mode = br.readLine().trim();
			Territory.mode = Mode;
			char ch = br.readLine().trim().toCharArray()[0];
			t.currentPlayer = ch;
			if (ch == 'X') {
				t.otherPlayer = 'O';
			} else {
				t.otherPlayer = 'X';
			}
			int depth = Integer.parseInt(br.readLine().trim());
			MiniMax.depth = depth;
			AlphaBeta.depth = depth;
			int[][] val = new int[N][N];
			for (int i = 0; i < N; i++) {
				String[] spl = br.readLine().trim().split(" ");
				for (int j = 0; j < spl.length; j++) {
					val[i][j] = Integer.parseInt(spl[j]);
				}
			}
			t.territory = val;
			char[][] chr = new char[N][N];
			for (int i = 0; i < N; i++) {
				String str = br.readLine().trim();
				for (int j = 0; j < str.length(); j++) {
					chr[i][j] = str.charAt(j);
				}
			}
			t.playerPos = chr;
			tr.addTerritory(t);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class TerritoryMapper {
	ArrayList<Territory> terList;
	
	public TerritoryMapper(){
		terList = new ArrayList<Territory>();
	}
	
	public void addTerritory(Territory t){
		terList.add(t);
	}
	
}

class Territory {
	static int[][] territory;
	static final char[] cols = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	char[][] playerPos;
	static char currentPlayer;
	int score;
	static char otherPlayer;
	static String mode;
	boolean isAlive = false;
	String move;
	boolean stake;
	static int N;
	
	public Territory(int N){
		playerPos = new char[N][N];
		this.N= N;
		stake = true;
	}
	
	public Territory(char[][] val){
		stake = true;
		playerPos = new char[N][N];
		for(int i=0; i<N; i++){
			for(int j=0; j<N; j++){
				playerPos[i][j] = val[i][j];
			}
		}
	}
	
	public int[] getScoreXY(){
		int x=0, y=0;
		for(int i=0; i<territory.length; i++){
			for(int j=0; j<territory.length; j++){
				if(playerPos[i][j]==currentPlayer){
					x += territory[i][j];
				}
				else if(playerPos[i][j]==otherPlayer){
					y += territory[i][j];
				}
			}
		}
		return new int[]{x,y};
	}
	
	public static void printTerritory(){
		for(int i=0; i<territory.length; i++){
			for(int j=0; j<territory[0].length; j++){
				System.out.print(territory[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public void printPlayerPositions(){
		for(int i=0; i<playerPos.length; i++){
			for(int j=0; j<playerPos[0].length; j++){
				System.out.print(playerPos[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public int gameScore(){
		int[] res = getScoreXY();
		return res[0]-res[1];
	}
	
	public void writeToFile(String filePath){
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(filePath,"UTF-8");
			pw.println(this.move);
			for(int i=0; i<N; i++){
				for(int j=0; j<N; j++){
					pw.print(playerPos[i][j]);
				}
				pw.println();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			pw.close();
		}
	}
	
	public boolean isGoalState(){
		int x=0, y=0;
		for(int i=0; i<territory.length; i++){
			for(int j=0; j<territory.length; j++){
				if(playerPos[i][j]=='.'){
					return false;
				}
			}
		}
		return true;
	}
}

class StakeRaid {

	// Modified version for better performance
	public ArrayList<Territory> possibleStakeRaids(Territory current,
			char player) {
		ArrayList<Territory> res = new ArrayList<Territory>();
		char[][] playerPos = current.playerPos;
		char opponent = player == 'X' ? 'O' : 'X';
		for (int i = 0; i < playerPos.length; i++) {
			for (int j = 0; j < playerPos.length; j++) {
				// Empty space available
				if (playerPos[i][j] == '.') {
					boolean stake = true;
					String move = " Stake";
					Territory t = new Territory(current.playerPos);
					t.playerPos[i][j] = player;
					t.move = Territory.cols[j] + String.valueOf(i + 1) + move;
					t.stake = stake;
					res.add(t);
				}
			}
		}

		for (int i = 0; i < playerPos.length; i++) {
			for (int j = 0; j < playerPos.length; j++) {
				if (playerPos[i][j] == '.') {
					// Complicated Logic -> Basically check if there exists a
					// player tile in Up,Down,Left,Right of the current tile
					boolean eliminate = false;
					if (i - 1 >= 0 && playerPos[i - 1][j] == player) {
						eliminate = true;
					}
					if (!eliminate && i + 1 < playerPos.length
							&& playerPos[i + 1][j] == player) {
						eliminate = true;
					}
					if (!eliminate && j - 1 >= 0
							&& playerPos[i][j - 1] == player) {
						eliminate = true;
					}
					if (!eliminate && j + 1 < playerPos.length
							&& playerPos[i][j + 1] == player) {
						eliminate = true;
					}

					// Complicated Logic -> Basically check if there exists an
					// opponent tile in Up, Down, Left, Right of the current
					// tile and convert it if eliminate is true
					// THIS IS RAID LOGIC
					if (eliminate) {
						Territory t = new Territory(current.playerPos);
						t.playerPos[i][j] = player;
						boolean changed = false;
						if (i - 1 >= 0 && playerPos[i - 1][j] == opponent) {
							t.playerPos[i - 1][j] = player;
							changed = true;
						}
						if (i + 1 < playerPos.length
								&& playerPos[i + 1][j] == opponent) {
							t.playerPos[i + 1][j] = player;
							changed = true;
						}
						if (j - 1 >= 0 && playerPos[i][j - 1] == opponent) {
							t.playerPos[i][j - 1] = player;
							changed = true;
						}
						if (j + 1 < playerPos.length
								&& playerPos[i][j + 1] == opponent) {
							t.playerPos[i][j + 1] = player;
							changed = true;
						}
						if (changed) {
							t.move = Territory.cols[j] + String.valueOf(i + 1)
									+ " Raid";
							t.stake = false;
							res.add(t);
						}
					}
				}
			}
		}
		return res;
	}
}

class MiniMax {
	
	StakeRaid st;
	static int depth;
	long counter = 0;
	
	public MiniMax(){
		st = new StakeRaid();
	}
	
	public Territory miniMax(Territory t){
		counter++;
		if(t.isGoalState()){
			t.score = t.gameScore();
			return t;
		}
		int depth = 0;
		int highestScore = Integer.MIN_VALUE;
		Territory best = null;
		ArrayList<Territory> moves = st.possibleStakeRaids(t, Territory.currentPlayer);
		int score = Integer.MIN_VALUE;
		depth++;
		for(Territory move: moves){
			counter++;
			Territory temp = min(move, depth);
			score = temp.score;
			//score = temp.gameScore();
//			System.out.println(move.stake);
//			move.printPlayerPositions();
			//temp.printPlayerPositions();
			
			
			//move.printPlayerPositions();
			//System.out.println(score);
			//System.out.println(move.move);
			//System.out.println("--------------");
			
			if(score>highestScore){
				highestScore = score;
				best = move;
				best.score = score;
			}
		}
		//System.out.println(counter);
		return best;
	}

	private Territory min(Territory t, int limit) {
		// TODO Auto-generated method stub
		if(limit >= MiniMax.depth || t.isGoalState()){
			t.score = t.gameScore();
			return t;
		}
		else{
			limit++;
			boolean hit = false;
			int lowestScore = Integer.MAX_VALUE;
			Territory worst = null;
			ArrayList<Territory> moves = st.possibleStakeRaids(t, Territory.otherPlayer);
			int score = Integer.MAX_VALUE;
			for(Territory move: moves){
				counter++;
				Territory tx = max(move,limit);
				score = tx.score;
				if(score<lowestScore){
					hit = true;
					lowestScore = score;
					worst = move;
					worst.score = score;
				}
			}
			return worst;
		}
	}

	private Territory max(Territory t, int limit) {
		// TODO Auto-generated method stub
		if(limit >= MiniMax.depth || t.isGoalState()){
			t.score = t.gameScore();
			return t;
		}
		else{
			limit++;
			int highestScore = Integer.MIN_VALUE;
			Territory best = null;
			ArrayList<Territory> moves = st.possibleStakeRaids(t, Territory.currentPlayer);
			int score = Integer.MIN_VALUE;
			for(Territory move: moves){
				counter++;
				Territory tx = min(move,limit);
				score = tx.score;
				if(score>highestScore){
					highestScore = score;
					best = move;
					best.score = score;
				}
			}
			return best;
		}
	}

}

class AlphaBeta {

	StakeRaid st;
	static int depth;
	long counter = 0;

	public AlphaBeta() {
		st = new StakeRaid();
	}

	public Territory alphaBeta(Territory t) {
		counter++;
		if (t.isGoalState()) {
			t.score = t.gameScore();
			return t;
		}
		boolean hit = false;
		int depth = 0;
		int highestScore = Integer.MIN_VALUE;
		Territory alpha = new Territory(Territory.N);
		alpha.score = Integer.MIN_VALUE;
		Territory beta = new Territory(Territory.N);
		beta.score = Integer.MAX_VALUE;

		Territory best = null;

		ArrayList<Territory> moves = st.possibleStakeRaids(t,
				Territory.currentPlayer);
		int score = Integer.MIN_VALUE;
		depth++;
		for (Territory move : moves) {
			counter++;
			Territory tx = minAB(move, depth, alpha, beta);
			score = tx.score;

//			move.printPlayerPositions();
//			System.out.println(score);
//			System.out.println(move.move);
//			System.out.println("-----------------");

			if (score > highestScore) {
				hit = true;
				highestScore = score;
				best = move;
				best.score = score;
				// alpha = best;
				// alpha.score = score;
			}
		}
		//System.out.println(counter);
		return best;
	}

	public Territory minAB(Territory t, int limit, Territory alpha,
			Territory beta) {
		// TODO Auto-generated method stub
		if (limit >= AlphaBeta.depth || t.isGoalState()) {
			t.score = t.gameScore();
			return t;
		} else {
			limit++;
			int lowestScore = beta.score;
			int score = beta.score;
			ArrayList<Territory> moves = st.possibleStakeRaids(t,
					Territory.otherPlayer);
			for (Territory move : moves) {
				counter++;
				Territory tx = maxAB(move, limit, alpha, beta);
				score = tx.score;
				if (score <= alpha.score) {
					move.score = score;
					return move;
				}
				if (score < lowestScore) {
					lowestScore = score;
					beta = move;
					beta.score = score;
				}
			}
			return beta;
		}
	}

	public Territory maxAB(Territory t, int limit, Territory alpha,
			Territory beta) {
		// TODO Auto-generated method stub
		if (limit >= AlphaBeta.depth || t.isGoalState()) {
			t.score = t.gameScore();
			return t;
		} else {
			boolean hit = false;
			counter++;
			limit++;
			int highestScore = alpha.score;
			int score = alpha.score;
			ArrayList<Territory> moves = st.possibleStakeRaids(t,
					Territory.currentPlayer);
			for (Territory move : moves) {
				Territory tx = minAB(move, limit, alpha, beta);
				score = tx.score;
				if (score >= beta.score) {
					move.score = score;
					return move;
				}
				if (score > highestScore) {
					hit = true;
					highestScore = score;
					alpha = move;
					alpha.score = score;
				}
			}
			return alpha;
		}
	}

}






