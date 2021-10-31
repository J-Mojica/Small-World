# Small-World
Small world is a Java program that constructs a graph where each vertex is an actor. If any two actors have worked in a common movie,
then an edge is built between them. Once the graph is fully constructed then it allows you to do queries by giving two indexes
that represent two actors. It will perform breadth-first search to find the shortest path between those two actors.
If a path exists between these two actors, it will print the distance and the chain of movies and actors that connect them together.

The program gets the actor data from compressed data base files that used to be distributed by the Internet Movie Database Ltd. (IMDb).
Unfortunately, IMDb no longer distributes these files.

# How it works
The program can be divided into 3 stages:
- Reading the data base entries and building the movies dictionary
- Constructing the graph
- Queries
## Reading data base entries and building the movies dictionary
First the program creates an empty dictionary, implemented with a [Binary Search Tree](/BST.java) (BST).
An unsorted list implementation of the dictionary would provide O(1) time complexity to insert a new entry,
but to find a record the time complexity would be O(n^2). With a sorted list implementation the time complexity for
search would be O(log(n)), but the time complexity for insertion would be O(n^2). 
Since a large number of additions *and* searches on the
dictionary will be performed, a BST implementation is the most appropriate because
a BST provides O(log(n)) time complexity (assuming the tree is balanced) for both inserting and searching a record in the dictionary.

The keys of the dictionary will be movie names as strings, and the values will be [MovieRecord](/MovieRecord.java) objects.
The MovieRecord class is, in essence, a wrapper for the name of the movie and a list of all actors who participated in the movie.
The actors in this case are represented by an integer, the number of their entry in the data base. It contains a method `addActor(Integer m)` that is useful to
add new actors that participated in an already existing MovieRecord as we encounter them in the data base entries.

An entry in the data base looks like this:
```
Cronin, Doug (I)  Chasing Dreams (1982) [Sheriff] <28>
                  Scared Straight! (1978)
```
The program reads the entries from compressed files containing the records of the data base
for actors through the use of the [RetrieveActors class](/RetrieveActors.java).
This class reads the compressed actor data without decompressing
the entire file. It contains a method `getNext()` that returns a string for the next
actor, or null if there are no more actor records in the file. The string comprises
a list of strings separated by “@@@”. The first string is the actor’s name, and
subsequent strings are the movies that the actor appeared in. Each movie string
has two initial characters; a “TV” indicates that the title is a made-for-TV movie,
a “TS” indicates a TV series or mini-series, a “VO” indicates a video, and “FM”
indicates a film.
For example, the entry displayed above would be returned as
```
Cronin, Doug (I)@@@FMChasing Dreams (1982)@@@FMScared Straight! (1978)@@@
```
For every actor entry in the data base, we check every movie they have participated in according to their entry.
If a movie does not exist in the dictionary already, we create a new [MovieRecord](/MovieRecord.java) object for that movie,
we add the current actor to this record and we add the record to the dictionary with it's name as the key. If the movie 
exists in the dictionary, we just retrieve the MovieRecord object corresponding to this movie and add the current actor
to the record.

## Constructing the Graph
First we construct a graph able to contain all actors (counted as they were being read).
The graph is implemented as an [adjacency list graph](/Graphl.java), i.e, each vertex (actor)
is represented by a list of the vertices (other actors) to which is connected by an edge.
This implementation is chosen over an adjacency matrix implementation because of the 
large amount of vertex that will be in the graph. Let *n* represent the number of actors in our graph,
an adjacency matrix implementation would require a 2D array of size *n*^2, i.e, an array of size
*n* for every actor. Although this implementation would allow one to tell whether two vertices are connected
in constant time, the amount of memory required to store the graph given that *n* is in the order of tens
of millions is way too big. With the adjacency list implementation, each list for each vertex only stores
the other vertex to which is conected to. So we only need space for all the vertices plus 
all the edges, which is **much less** than *n*^2.

Now, since we already have a dictionary where we can look up a movie and get a list of all the actors that participated in said movie,
all that is left to do is for each movie make an edge between all the actors that participated in it. To save space, duplicate edges are not
allowed. So, if two actors participated in two different movies together, we will only connect them once.

## Queries
The program prompts the user for two indices: the actor from which to start from (source) and the actor that we want to reach (destination).
It will then perform breadth-first search, visiting every vertex with a path to the source vertex until it reaches the destination
vertex. If a path exists then it will print the shortest distance along with the chain of movies and actors tat connect the source actor and the destination actor.
If a path does not exist it will then print "No connection."

If the user inputs -1 as the destination then the program will perform breadth-first search to determine the component of the graph that
is connected to the source vertex. It then prints tha number of vertices that are part of this connected component and the user has the 
option to print said compoinent.

If the user inputs 0 as the source and destination then the program ends.

# Sample session
```
$ java -Xms16g SmallWorld
Enter source and destination indices:
200 300
Shortest path between Aabø, Ingrid Bergan and Aaloka
Distance: 5; the chain is:
Aabø, Ingrid Bergan appeared with Fougner, Erlend B. in D˚arlig kjemi (2007)
Fougner, Erlend B. appeared with Figenschow, Kristian in Hjelp, vi er russ (2011)
Figenschow, Kristian appeared with Skarsg˚ard, Stellan in Insomnia (1997)
Skarsg˚ard, Stellan appeared with Khote, Viju in The Perfect Murder (1988)
Khote, Viju appeared with Aaloka in Dak Bangla (1987)

Enter source and destination indices:
728113 1599709
Shortest path between Lamarr, Hedy and Bacon, Kevin (I)
Distance: 2; the chain is:
Lamarr, Hedy appeared with Andrews, Julie (I) in Mondo Hollywood (1967)
Andrews, Julie (I) appeared with Bacon, Kevin (I) in Boffo! Tinseltown’s Bombs and Blockbusters (2006)

Enter source and destination indices:
2000 3000
No path between Abe, Yukari (II) and Abraham, Gloria

Enter source and destination indices:
0 0
$
```
---
This was made as my solution for one of the problem sets for the CS 114 Introduction to Computer Science II course at the New Jersey Institute of Technology
during the Fall 2020 Semester. The instructor was Dr. James Calvin. All files with the exception of `SmallWorld.java` were provided by the instructor.
