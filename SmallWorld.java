import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Collection;
public class SmallWorld{
	public static void main(String[] args) throws Exception{
		ArrayList<ActorRecord> act = new ArrayList<ActorRecord>();

        String[] fnames = new String[] {"actresses.list.gz","actors.list.gz"};

		String content;
		String[] tkn;
        int count = -1;
        
        BST<String, MovieRecord> movieBST = new BST<String, MovieRecord>(); 
        // START OF READ AND DICTIONARY CONSTRUCTION
        long startTime = System.currentTimeMillis();
        int k = 0;
        int limit = 120000; // Limits the amount of actors to be read
        System.out.println("CURRENT LIMIT: " + limit);

        for(String fname : fnames){
        /*  Reads the actors from the files, and creates a dictionary
            where a key is a the name of a movie, and the value for said
            key is a MovieRecord object.
        */
            RetrieveActors ra = new RetrieveActors(fname);
            while(((content = ra.getNext()) != null) && k < limit) {
                ++count;
                tkn = content.split("@@@");
                ActorRecord ar = new ActorRecord(tkn[0]);
                for (int i = 1; i < tkn.length; ++i){
                    if(tkn[i].substring(0, 2).equals("FM")){ // only keep tv series
                        ar.addMovie(tkn[i].substring(2));
                        String movie = tkn[i].substring(2);
                        MovieRecord currMovie = movieBST.find(movie);
                        if(currMovie == null){ //If the movie is not in the dictionary
                            MovieRecord newMovieRecord = new MovieRecord(movie);
                            newMovieRecord.addActor(count);
                            movieBST.insert(movie, newMovieRecord);
                        }
                        else{
                            currMovie.addActor(count);
                        }
                    }  
                }
                act.add(ar);
                k++; // COMMENT OUT THIS LINE TO DO ALL ACTORS/ACTRESSES
            }
            ra.close();
        }
                
        long endTime = System.currentTimeMillis();
        System.out.println("Read in "+act.size()+" actors and actresses in time "+(endTime-startTime)+" ms.");
        //END OF READ AND BUILDING DICTIONARY


        //START OF GRAPH CONSTRUCTION
        startTime = System.currentTimeMillis();

        Graphl actorGraph = new Graphl(act.size());
        //Graph list implementation
    
		for(MovieRecord curRecord : movieBST.values()){            
        //If two actors are in the same movie make an edge between them
            for(int i : curRecord.actors){
                for(int j : curRecord.actors){
                    if(i==j || actorGraph.isEdge(i,j)){
                        continue;
                    }
                    actorGraph.setEdge(i, j, 0);
                    actorGraph.setEdge(j, i, 0);
                }
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Construted graph in time " + (endTime-startTime)+ " ms.");
        System.out.println("Number of vertex:" + actorGraph.n() +"\nNumber of edges: " + actorGraph.e());
        //END OF GRAPH CONSTRUCTION
        

        //START OF QUERIES 
        Scanner scan = new Scanner(System.in);
        int start, goal;
        do{
            start = Integer.parseInt(scan.next());
            goal = Integer.parseInt(scan.next());
            startTime = System.currentTimeMillis();
            if((start != 0 || goal != 0) && goal != -1){
                printShortestDistance(act, actorGraph, start, goal);
            }
            else if(goal == -1){
                getConnectedComp(actorGraph, start);
                int startEdges = 0;
                for(int i : actorGraph.neighbors(start)){
                    startEdges++;
                }
                System.out.println("Edges connected to actor/actress " + act.get(start).name + ": " + startEdges);
                System.out.println("Size of connected component of actor/actress " + start + ": " + startComp.size());
                System.out.println("Print connected component? (y/n)");
                String answer = scan.next();
                if(answer.equals("y")){
                    System.out.println(startComp);
                }
                int maxDistanceFromStart = 0;
                for(int actor : startComp){
                    int crawl = actor;
                    int dist = 0;
                    while (actorGraph.getMark(crawl) != -2) {
                        dist++;
                        //path.add(graph.getMark(crawl));
                        crawl = actorGraph.getMark(crawl);
                    }
                    if(dist > maxDistanceFromStart){
                        maxDistanceFromStart = dist;
                    }
                }
                System.out.println("Maximum distance from actor/actress " + start + ": " + maxDistanceFromStart);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Search and path reconstruction in time: " + (endTime-startTime)+ " ms.");
        }while(start != 0 || goal != 0);	
        scan.close();
        //END OF QUERIES
    }
	private static void printShortestDistance(ArrayList<ActorRecord> act, Graphl graph, int start, int goal){

        if (BFS(graph, start, goal) == false){
            System.out.println("No connection.");
            return;
        }
 
        ArrayList<Integer> path = new ArrayList<Integer>();
        int crawl = goal;
        int dist = 0;
        path.add(crawl);
        while (graph.getMark(crawl) != -2) {
            dist++;
            path.add(graph.getMark(crawl));
            crawl = graph.getMark(crawl);
        }

        String chain = "";
        for (int i = path.size() - 1; i > 0; i--) {
            ActorRecord currActor = act.get(path.get(i));
            ActorRecord predActor = act.get(path.get(i-1));
            String commonMovie = "";
            for(int j = 0; j < currActor.movies.size(); j++){
                if(predActor.appearedIn(currActor.movies.get(j))){
                    commonMovie = currActor.movies.get(j);
                    break;
                }
            }
            chain += currActor.name + " appeared with " + predActor.name + " in " + commonMovie + "\n";
        }
        // Print header
        System.out.println("Shortest path between " + act.get(start).name + " and " + act.get(goal).name );
        // Print distance
        System.out.println("Distance: "+ dist +"; the chain is:");
        // Print chain
        System.out.println(chain);
    }
    public static ArrayList<Integer> startComp;
	private static boolean BFS(Graphl graph, int start, int goal){
        LinkedList<Integer> queue = new LinkedList<Integer>();
 
        // initially all vertices are unvisited
        for (int i = 0; i < graph.n(); i++){
            graph.setMark(i, -1);
        }
 
        graph.setMark(start, -2);
        queue.add(start);
 
        // bfs Algorithm
        while (!queue.isEmpty()) {
            int curr = queue.remove();
            for (int i : graph.neighbors(curr)) {
                if (graph.getMark(i) == -1) {
                    graph.setMark(i, curr);
                    queue.add(i);
                    //stop if goal vertex is found
                    if (i == goal)
                        return true;
                    if(goal == -1)
                        startComp.add(i);
                }
            }
        }
        return false;
    }
    public static void getConnectedComp(Graphl graph, int start){
        startComp = new ArrayList<Integer>();
        BFS(graph, start, -1);
    }
}
