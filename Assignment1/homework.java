import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class homework {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Node> nodeList;
		String algorithm = null, start = null, end = null;
		int liveTrafficCount = 0, sundayTrafficCount = 0;
		nodeList = new ArrayList<Node>();
		try {
			String InputPath = "./input.txt";
			String OutputPath = "./output.txt";
			FileReader fr = new FileReader(InputPath);
			BufferedReader br = new BufferedReader(fr);
			algorithm = br.readLine().toLowerCase();
			start = br.readLine();
			end = br.readLine();
			liveTrafficCount = Integer.valueOf(br.readLine().trim());
			String[] liveTraffic = new String[liveTrafficCount];
			for (int i = 0; i < liveTrafficCount; i++) {
				liveTraffic[i] = br.readLine();
			}
			sundayTrafficCount = Integer.valueOf(br.readLine().trim());
			String[] sundayTraffic = new String[sundayTrafficCount];
			for (int i = 0; i < sundayTrafficCount; i++) {
				sundayTraffic[i] = br.readLine();
			}
			br.close();
			Graph g = new Graph(algorithm, start, end, liveTraffic,
					sundayTraffic);
			if (g.algorithm.equals("bfs")) {
				BFS bfs = new BFS(g);
				bfs.printPathToFile(OutputPath);
			} else if (g.algorithm.equals("dfs")) {
				DFS dfs = new DFS(g);
				dfs.printPathToFile(OutputPath);
			} else if (g.algorithm.equals("ucs")) {
				UCS ucs = new UCS(g);
				ucs.printPathToFile(OutputPath);
			} else if (g.algorithm.equals("a*")) {
				AStar astar = new AStar(g);
				astar.printPathToFile(OutputPath);
			}
			// g.printGraph();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class Graph {
	public String algorithm, start, end;
	public String[] liveTraffic;
	public String[] sundayTraffic;
	static int nodeVal = 0;
	public HashMap<String, Node> nodeMap;
	public HashMap<String, Integer> heuristic;

	public Graph(String algorithm, String start, String end,
			String[] liveTraffic, String[] sundayTraffic) {
		// TODO Auto-generated constructor stub
		this.algorithm = algorithm;
		this.start = start;
		this.end = end;
		this.liveTraffic = liveTraffic;
		this.sundayTraffic = sundayTraffic;
		nodeMap = new HashMap<String, Node>();
		heuristic = new HashMap<String, Integer>();
		setupNodes();
		setHeuristic();
	}

	private void setupNodes() {
		for (int i = 0; i < liveTraffic.length; i++) {
			String[] details = liveTraffic[i].split(" ");
			String from = details[0];
			String to = details[1];
			int time = Integer.valueOf(details[2].trim());
			if (!nodeMap.containsKey(from) && !nodeMap.containsKey(to)) {
				Node f = new Node(from);
				Node t = new Node(to);
				f.addEdge(t, time);
				nodeMap.put(from, f);
				f.setOccurrence(Graph.nodeVal++);
				t.setOccurrence(Graph.nodeVal++);
				nodeMap.put(to, t);
			} else if (!nodeMap.containsKey(from) && nodeMap.containsKey(to)) {
				Node f = new Node(from);
				Node t = nodeMap.get(to);
				f.addEdge(t, time);
				f.setOccurrence(Graph.nodeVal++);
				nodeMap.put(from, f);
			} else if (nodeMap.containsKey(from) && !nodeMap.containsKey(to)) {
				Node t = new Node(to);
				t.setOccurrence(Graph.nodeVal++);
				nodeMap.put(to, t);
				nodeMap.get(from).addEdge(t, time);
			} else {
				nodeMap.get(from).addEdge(nodeMap.get(to), time);
			}
		}
	}

	private void setHeuristic() {
		for (int i = 0; i < sundayTraffic.length; i++) {
			String[] details = sundayTraffic[i].split(" ");
			heuristic.put(details[0], Integer.parseInt(details[1].trim()));
		}
	}

	public void printGraph() {
		for (String s : nodeMap.keySet()) {
			System.out.println(nodeMap.get(s));
		}
	}

}

class Node {
	private String ID;
	private Node parent;
	private int level;
	private int occurrence;
	private int timeFromSource;

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public ArrayList<Edge> getEdgeList() {
		return edgeList;
	}

	public void setEdgeList(ArrayList<Edge> edgeList) {
		this.edgeList = edgeList;
	}

	private ArrayList<Edge> edgeList;

	public Node(String id) {
		ID = id;
		edgeList = new ArrayList<Edge>();
		parent = null;
		level = 0;
		timeFromSource = 0;
		occurrence = 0;
	}

	public void addEdge(Node n, int t) {
		Edge ed = new Edge(n, t);
		boolean alreadyPresent = false;
		int index = -1;
		for (int i = 0; i < edgeList.size(); i++) {
			if (edgeList.get(i).getLinkNode().getID().equals(n.getID())) {
				index = i;
				alreadyPresent = true;
				break;
			}
		}
		if (alreadyPresent == false) {
			edgeList.add(ed);
		} else if (alreadyPresent == true && edgeList.get(index).getTime() > t) {
			edgeList.get(index).setTime(t);
		}

	}

	public void removeEdge(Edge n) {
		edgeList.remove(n);
	}

	public String printNode() {
		StringBuilder sb = new StringBuilder();
		sb.append("#############\n");
		sb.append(ID + "\n");
		for (int i = 0; i < edgeList.size(); i++) {
			sb.append(edgeList.get(i) + ",");
		}
		if (parent != null) {
			sb.append("\nParent = " + parent.getID());
		}
		sb.append("\nOccurrence = " + getOccurrence());
		sb.append("\nLevel = " + getLevel());
		sb.append("\nTimeFromSource = " + getTimeFromSource());
		sb.append("\n#############\n");
		return sb.toString();
	}

	@Override
	public String toString() {
		return printNode();
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getTimeFromSource() {
		return timeFromSource;
	}

	public void setTimeFromSource(int timeFromSource) {
		this.timeFromSource = timeFromSource;
	}

	public int getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(int occurrence) {
		this.occurrence = occurrence;
	}

}

class NodeComparator implements Comparator<NodeParent> {

	public int compare(NodeParent arg0, NodeParent arg1) {
		// TODO Auto-generated method stub
		if (arg0.getTimeFromSource() + arg0.heuristic > arg1
				.getTimeFromSource() + arg1.heuristic) {
			return 1;
		} else if (arg0.getTimeFromSource() + arg0.heuristic == arg1
				.getTimeFromSource() + arg1.heuristic) {
			if (arg0.getGlobalPos() < arg1.getGlobalPos()) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}

}

class NodeInStack {

	Node parent;
	String ID;
	int weight;
	int distanceFromSource;

	public NodeInStack(String ID, Node parent, int weight) {
		this.ID = ID;
		this.parent = parent;
		this.weight = weight;
		this.distanceFromSource = 0;
	}

}

class NodeParent {
	static int counter = 0;
	private Node current;
	private Node parent;
	int timeFromSource;
	int heuristic;
	int globalPos;

	public NodeParent(Node current, Node parent, int timeFromSource,
			int heuristic, int globalPos) {
		this.current = current;
		this.parent = parent;
		this.timeFromSource = timeFromSource;
		this.heuristic = heuristic;
		this.globalPos = globalPos;
	}

	public Node getCurrent() {
		return current;
	}

	public Node getParent() {
		return parent;
	}

	public int getTimeFromSource() {
		return timeFromSource;
	}

	public int getGlobalPos() {
		return globalPos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((current == null) ? 0 : current.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeParent other = (NodeParent) obj;
		if (current == null) {
			if (other.current != null)
				return false;
		} else if (!current.equals(other.current))
			return false;
		return true;
	}
}

class Edge {
	private Node linkNode;
	private int time;

	public Edge(Node n, int t) {
		setLinkNode(n);
		setTime(t);
	}

	@Override
	public String toString() {
		return "(" + linkNode.getID() + ", " + time + ")";
	}

	public Node getLinkNode() {
		return linkNode;
	}

	public void setLinkNode(Node linkNode) {
		this.linkNode = linkNode;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}

class BFS {
	Graph graph;
	HashMap<String, Integer> traversed;
	Queue<Node> queue;

	public BFS(Graph g) {
		graph = g;
		traversed = new HashMap<String, Integer>();
		queue = new LinkedList<Node>();
		traverse();
	}

	public void traverse() {
		int level = 0;
		boolean dest = false;
		Node start = graph.nodeMap.get(graph.start);
		start.setParent(null);
		queue.add(start);
		traversed.put(start.getID(), 1);
		while (queue.size() != 0 && dest == false) {
			Node n = queue.remove();
			if (n.getID().equals(graph.end)) {
				dest = true;
				break;
			}
			level = n.getLevel() + 1;
			ArrayList<Edge> edges = n.getEdgeList();
			for (int i = 0; i < edges.size(); i++) {
				if (!traversed.containsKey(edges.get(i).getLinkNode().getID())) {
					Node add = graph.nodeMap.get(edges.get(i).getLinkNode()
							.getID());
					add.setLevel(level);
					add.setParent(n);
					queue.add(add);
					traversed.put(add.getID(), 1);
				}
			}
		}
	}

	public void printPath() {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node n = graph.nodeMap.get(graph.end);
		nodeList.add(n);
		if (n.getParent() == null && graph.end.equals(graph.start)) {
			System.out.println(n.getID() + " " + 0);
		} else {
			while (n.getParent() != null) {
				nodeList.add(n.getParent());
				n = n.getParent();
			}
			for (int i = nodeList.size() - 1; i >= 0; i--) {
				System.out.println(nodeList.get(i).getID() + " "
						+ nodeList.get(i).getLevel());
			}
		}
	}

	public void printPathToFile(String filePath) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		PrintWriter writer = null;
		try {
			// "H:\\USC\\Artificial Intelligence\\HomeWork1\\result.txt","UTF-8"
			writer = new PrintWriter(filePath,"UTF-8");
			Node n = graph.nodeMap.get(graph.end);
			nodeList.add(n);
			if (n.getParent() == null && graph.end.equals(graph.start)) {
				writer.write(n.getID() + " " + 0);
			} else {
				while (n.getParent() != null) {
					nodeList.add(n.getParent());
					n = n.getParent();
				}
				for (int i = nodeList.size() - 1; i >= 0; i--) {
					writer.write(nodeList.get(i).getID() + " "
							+ nodeList.get(i).getLevel() + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

}


class DFS {
	Graph graph;
	HashMap<String, Integer> traversed;
	LinkedList<Node> list;
	
	public DFS(Graph g){
		graph = g;
		traversed = new HashMap<String, Integer>();
		list = new LinkedList<Node>();
		traverse1();
	}
	
	public void traverse(){
		int level = 0;
		boolean dest = false;
		Node start = graph.nodeMap.get(graph.start);
		start.setParent(null);
		list.add(start);
		traversed.put(start.getID(), 1);
		while(list.size()!=0 && dest == false){
			Node n = list.removeFirst();
			traversed.put(n.getID(), 1);
			if (n.getID().equals(graph.nodeMap.get(graph.end))) {
				dest = true;
			}
			level = n.getLevel()+1;
			ArrayList<Edge> edges = n.getEdgeList();
			for (int i = edges.size()-1; i >= 0; i--) {
				if (!traversed.containsKey(edges.get(i).getLinkNode().getID())) {
					Node add = graph.nodeMap.get(edges.get(i).getLinkNode()
							.getID());
					add.setLevel(level);
					add.setParent(n);
					list.addFirst(add);
				}
			}
		}
	}
	
	
	public void traverse1(){
		LinkedList<String> ll = new LinkedList<String>();
		int level = 0;
		boolean dest = false;
		Node start = graph.nodeMap.get(graph.start);
		start.setParent(null);
		list.add(start);
		ll.add(start.getID());
		traversed.put(start.getID(), 1);
		while(list.size()!=0 && dest == false){
			Node n = list.removeFirst();
			traversed.put(n.getID(), 1);
			if (n.getID().equals(graph.nodeMap.get(graph.end))) {
				dest = true;
			}
			level = n.getLevel()+1;
			ArrayList<Edge> edges = n.getEdgeList();
			for (int i = edges.size()-1; i >= 0; i--) {
				if (!traversed.containsKey(edges.get(i).getLinkNode().getID()) && !ll.contains(edges.get(i).getLinkNode().getID())) {
					Node add = graph.nodeMap.get(edges.get(i).getLinkNode()
							.getID());
					add.setLevel(level);
					add.setParent(n);
					list.addFirst(add);
					ll.add(add.getID());
				}
			}
		}
	}
	
	
	public void printPath(){
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node n = graph.nodeMap.get(graph.end);
		nodeList.add(n);
		if (n.getParent() == null) {
			System.out.println("No Path Found");
		} else {
			while (n.getParent() != null) {
				nodeList.add(n.getParent());
				n = n.getParent();
			}
			for (int i = nodeList.size() - 1; i >= 0; i--) {
				System.out.println(nodeList.get(i).getID() + " "
						+ nodeList.get(i).getLevel());
			}
		}
	}
	
	public void printPathToFile(String filePath) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		PrintWriter writer = null;
		try {
			// "H:\\USC\\Artificial Intelligence\\HomeWork1\\result.txt","UTF-8"
			writer = new PrintWriter(filePath,"UTF-8");
			Node n = graph.nodeMap.get(graph.end);
			nodeList.add(n);
			if (n.getParent() == null && graph.end.equals(graph.start)) {
				writer.write(n.getID() + " " + 0);
			} else {
				while (n.getParent() != null) {
					nodeList.add(n.getParent());
					n = n.getParent();
				}
				for (int i = nodeList.size() - 1; i >= 0; i--) {
					writer.write(nodeList.get(i).getID() + " "
							+ nodeList.get(i).getLevel() + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	
}



class UCS {

	Graph graph;
	HashMap<String, Integer> traversed;
	List<NodeParent> list = new ArrayList<NodeParent>();

	public UCS(Graph g) {
		traversed = new HashMap<String, Integer>();
		list = new ArrayList<NodeParent>();
		graph = g;
		traverse();
	}
	
	private void traverse(){
		LinkedList<NodeParent> open = new LinkedList<NodeParent>();
		LinkedList<Node> closed = new LinkedList<Node>();
		Node start = graph.nodeMap.get(graph.start);
		Node end = graph.nodeMap.get(graph.end);
		open.add(new NodeParent(start, null, 0, 0, NodeParent.counter++));
		boolean dest = false;
		while(dest == false){
			if(open.size()==0){
				break;
			}
			NodeParent np = open.remove(0);
			Node curNode = np.getCurrent();
			closed.add(curNode);
			curNode.setParent(np.getParent());
			curNode.setTimeFromSource(np.getTimeFromSource());
			if(curNode == end){
				dest = true;
				break;
			}
			ArrayList<Edge> edges = curNode.getEdgeList();
			int time = curNode.getTimeFromSource();
			for(int i=0; i<edges.size(); i++){
				// Case 1
				Node edNode = graph.nodeMap.get(edges.get(i).getLinkNode().getID());
				NodeParent nT = new NodeParent(edNode, null, 0, 0, 0);
				if(!open.contains(nT) && !closed.contains(edNode)){
					open.add(new NodeParent(edNode, curNode, time+edges.get(i).getTime(),0, NodeParent.counter++));
				}
				// Case 2
				else if(open.contains(nT)){
					int idx = open.indexOf(nT);
					NodeParent nodeP = open.get(idx);
					if(nodeP.timeFromSource>time+edges.get(i).getTime()){
						open.remove(idx);
						open.add(new NodeParent(edNode, curNode, time+edges.get(i).getTime(),0, NodeParent.counter++));
					}
				}
				// Case 3
				else if(closed.contains(edNode)){
					int idx = closed.indexOf(edNode);
					Node node = closed.get(idx);
					if(node.getTimeFromSource()>time+edges.get(i).getTime()){
						closed.remove(idx);
						open.add(new NodeParent(edNode, curNode, time+edges.get(i).getTime(),0, NodeParent.counter++));
					}
				}
			}
			Collections.sort(open, new NodeComparator());
		}
		if(dest==false){
			System.out.println("No destination found");
		}
	}

//	private void traverse() {
//		boolean dest = false;
//		Node start = graph.nodeMap.get(graph.start);
//		NodeParent n = null;
//		list.add(new NodeParent(start, null,0, NodeParent.counter++));
//		//traversed.put(start.getID(), 1);
//		while (list.size() != 0 && dest == false) {
//			Collections.sort(list, new NodeComparator());
//			n = list.remove(0);
//			n.getCurrent().setTimeFromSource(n.getTimeFromSource());
//			n.getCurrent().setParent(n.getParent());
//			if (n.getCurrent().getID().equals(graph.end)) {
//				dest = true;
//				break;
//			}
//			int time = n.getCurrent().getTimeFromSource();
//			ArrayList<Edge> edges = n.getCurrent().getEdgeList();
//			for (int i = 0; i < edges.size(); i++) {
//				if () {
//					// Modify this to add it to the list only if the distance from source is less than that of what already exists
//					// in the list
//					Node add = graph.nodeMap.get(edges.get(i).getLinkNode()
//							.getID());
//					list.add(new NodeParent(add,n.getCurrent(),time + edges.get(i).getTime(),NodeParent.counter++));
//				}
//			}
//		}
//	}
	
	
	private void traverse1() {
		boolean dest = false;
		Node start = graph.nodeMap.get(graph.start);
		NodeParent n = null;
		// UNCOMMENT
		//list.add(new NodeParent(start, null,0));
		traversed.put(start.getID(), 1);
		while (list.size() != 0 && dest == false) {
			Collections.sort(list, new NodeComparator());
			n = list.remove(0);
			while(list.size() != 0 && traversed.containsKey(n.getCurrent().getID())){
				n = list.remove(0);
			}
			n.getCurrent().setTimeFromSource(n.getTimeFromSource());
			n.getCurrent().setParent(n.getParent());
			traversed.put(n.getCurrent().getID(), 1);
			if (n.getCurrent().getID().equals(graph.end)) {
				dest = true;
				break;
			}
			int time = n.getCurrent().getTimeFromSource();
			ArrayList<Edge> edges = n.getCurrent().getEdgeList();
			for (int i = 0; i < edges.size(); i++) {
				if (!traversed.containsKey(edges.get(i).getLinkNode().getID())) {
					// Modify this to add it to the list only if the distance from source is less than that of what already exists
					// in the list
					Node add = graph.nodeMap.get(edges.get(i).getLinkNode()
							.getID());
					
					//UNCOMMENT
					//list.add(new NodeParent(add,n.getCurrent(),time + edges.get(i).getTime()));
				}
			}
		}
	}
	

	public void printPath() {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node n = graph.nodeMap.get(graph.end);
		nodeList.add(n);
		if (n.getParent() == null) {
			System.out.println("No Path Found");
		} else {
			while (n.getParent() != null) {
				nodeList.add(n.getParent());
				n = n.getParent();
			}
			for (int i = nodeList.size() - 1; i >= 0; i--) {
				System.out.println(nodeList.get(i).getID() + " "
						+ nodeList.get(i).getTimeFromSource());
			}
		}
	}
	
	
	public void printPathToFile(String filePath) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		PrintWriter writer = null;
		try {
			// "H:\\USC\\Artificial Intelligence\\HomeWork1\\result.txt","UTF-8"
			writer = new PrintWriter(filePath,"UTF-8");
			Node n = graph.nodeMap.get(graph.end);
			nodeList.add(n);
			if (n.getParent() == null && graph.end.equals(graph.start)) {
				writer.write(n.getID() + " " + 0);
			} else {
				while (n.getParent() != null) {
					nodeList.add(n.getParent());
					n = n.getParent();
				}
				for (int i = nodeList.size() - 1; i >= 0; i--) {
					writer.write(nodeList.get(i).getID() + " "
							+ nodeList.get(i).getTimeFromSource() + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

}


class AStar {
	Graph graph;
	HashMap<String, Integer> traversed;
	List<NodeParent> list = new ArrayList<NodeParent>();
	
	public AStar(Graph g){
		traversed = new HashMap<String, Integer>();
		list = new ArrayList<NodeParent>();
		graph = g;
		traverse();
	}
	
	private void traverse(){
		LinkedList<NodeParent> open = new LinkedList<NodeParent>();
		LinkedList<Node> closed = new LinkedList<Node>();
		Node start = graph.nodeMap.get(graph.start);
		Node end = graph.nodeMap.get(graph.end);
		open.add(new NodeParent(start, null, 0, 0,NodeParent.counter++));
		boolean dest = false;
		while(dest == false){
			if(open.size()==0){
				break;
			}
			NodeParent np = open.remove(0);
			Node curNode = np.getCurrent();
			closed.add(curNode);
			curNode.setParent(np.getParent());
			curNode.setTimeFromSource(np.getTimeFromSource());
			if(curNode == end){
				dest = true;
				break;
			}
			ArrayList<Edge> edges = curNode.getEdgeList();
			int time = curNode.getTimeFromSource();
			for(int i=0; i<edges.size(); i++){
				// Case 1
				Node edNode = graph.nodeMap.get(edges.get(i).getLinkNode().getID());
				NodeParent nT = new NodeParent(edNode, null,0, 0, 0);
				if(!open.contains(nT) && !closed.contains(edNode)){
					open.add(new NodeParent(edNode, curNode, time+edges.get(i).getTime(),graph.heuristic.get(edges.get(i).getLinkNode().getID()), NodeParent.counter++));
				}
				// Case 2
				else if(open.contains(nT)){
					int idx = open.indexOf(nT);
					NodeParent nodeP = open.get(idx);
					if(nodeP.timeFromSource>time+edges.get(i).getTime()){
						open.remove(idx);
						open.add(new NodeParent(edNode, curNode, time+edges.get(i).getTime(),graph.heuristic.get(edges.get(i).getLinkNode().getID()), NodeParent.counter++));
					}
				}
				// Case 3
				else if(closed.contains(edNode)){
					int idx = closed.indexOf(edNode);
					Node node = closed.get(idx);
					if(node.getTimeFromSource()>time+edges.get(i).getTime()){
						closed.remove(idx);
						open.add(new NodeParent(edNode, curNode, time+edges.get(i).getTime(),graph.heuristic.get(edges.get(i).getLinkNode().getID()), NodeParent.counter++));
					}
				}
			}
			Collections.sort(open, new NodeComparator());
		}
		if(dest==false){
			System.out.println("No destination found");
		}
	}
	
//	private void traverse1() {
//		boolean dest = false;
//		Node start = graph.nodeMap.get(graph.start);
//		NodeParent n = null;
//		list.add(new NodeParent(start, null,0, NodeParent.counter++));
//		traversed.put(start.getID(), 1);
//		while (list.size() != 0 && dest == false) {
//			Collections.sort(list, new NodeComparator());
//			n = list.remove(0);
//			while(list.size() != 0 && traversed.containsKey(n.getCurrent().getID())){
//				n = list.remove(0);
//			}
//			n.getCurrent().setTimeFromSource(n.getTimeFromSource());
//			n.getCurrent().setParent(n.getParent());
//			traversed.put(n.getCurrent().getID(), 1);
//			if (n.getCurrent().getID().equals(graph.end)) {
//				dest = true;
//				break;
//			}
//			int time = n.getCurrent().getTimeFromSource();
//			ArrayList<Edge> edges = n.getCurrent().getEdgeList();
//			for (int i = 0; i < edges.size(); i++) {
//				if (!traversed.containsKey(edges.get(i).getLinkNode().getID())) {
//					Node add = graph.nodeMap.get(edges.get(i).getLinkNode()
//							.getID());
//					list.add(new NodeParent(add,n.getCurrent(),time + edges.get(i).getTime()+graph.heuristic.get(edges.get(i).getLinkNode().getID()), NodeParent.counter++));
//				}
//			}
//		}
//	}
	
	
	public void printPath() {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node n = graph.nodeMap.get(graph.end);
		nodeList.add(n);
		if (n.getParent() == null) {
			System.out.println("No Path Found");
		} else {
			while (n.getParent() != null) {
				nodeList.add(n.getParent());
				n = n.getParent();
			}
			for (int i = nodeList.size() - 1; i >= 0; i--) {
				System.out.println(nodeList.get(i).getID() + " "
						+ nodeList.get(i).getTimeFromSource());
			}
		}
	}
	
	
	public void printPathToFile(String filePath) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		PrintWriter writer = null;
		try {
			// "H:\\USC\\Artificial Intelligence\\HomeWork1\\result.txt","UTF-8"
			writer = new PrintWriter(filePath,"UTF-8");
			Node n = graph.nodeMap.get(graph.end);
			nodeList.add(n);
			if (n.getParent() == null && graph.end.equals(graph.start)) {
				writer.write(n.getID() + " " + 0);
			} else {
				while (n.getParent() != null) {
					nodeList.add(n.getParent());
					n = n.getParent();
				}
				for (int i = nodeList.size() - 1; i >= 0; i--) {
					writer.write(nodeList.get(i).getID() + " "
							+ nodeList.get(i).getTimeFromSource() + "\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	
}
