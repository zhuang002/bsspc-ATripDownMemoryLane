/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atripdownmemorylane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author zhuan
 */
public class ATripDownMemoryLane {

    /**
     * @param args the command line arguments
     */
    static Graph graph=new Graph(); // The graph structure
    static ArrayList<Set<Integer>> questions = new ArrayList<Set<Integer>>(); // The structure to save the questions
    static Scanner sc = new Scanner(System.in);
    

    public static void main(String[] args) {
        // TODO code application logic here

        inputData();
        processQuestions();
    }

    private static void inputData() {
        int n = sc.nextInt();
        int m = sc.nextInt();
        for (int i = 0; i < m; i++) {
            int op = sc.nextInt();
            int u = sc.nextInt();
            int v = sc.nextInt();

            switch (op) {
                case 1: // It indicate an event with 2 people at time.(u,v,t)
                    int t = sc.nextInt();
                    graph.addLink(u,v,t); // save the event record into the graph.
                    break;
                case 2: // it is a question.
                    Set<Integer> question = new HashSet();
                    question.add(u);
                    question.add(v);
                    questions.add(question);
                    break;
                default:
                    sc.nextLine();
            }
        }
    }

    private static void processQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            System.out.println(graph.getShortestRoute(questions.get(i)));
        }
    }

}
/**
 * The graph to save the event records. This is a typical searching for shortest route problem in a graph.
 * The nodes of the graph are peoples. If 2 people attend a same event, use a link in the graph to represent the record.
 * The length of the link is the time (t) of the event. If same pair of people attend events for several times (with
 * different t) the earliest time (smallest t) would be the length of link. A set of nodes connected by links is a 
 * friend circle. 
 * @author zhuan
 */
class Graph {
    /**
     * Saves the links of the graph. It is a HashMap uses a Set of 2 integers(nodes for 2 ends) as the key, 
     * and the value is the length of the link.
     */
    HashMap<Set<Integer>,Integer> links=new HashMap<>();
    /**
     * A redundant structure to save the links of nodes. The key is a node and the value is a set of nodes
     * that are linked with the key node, or in other word, child nodes of the key nodes.
     */
    HashMap<Integer,Set<Integer>> linkMap=new HashMap<>();
    /**
     * the structure to save the calculated results of getShoretestRoute() recursive function in order to minimize the
     * recalculation of same parameters. This is a alternative approach of dynamic planning.
     */
    HashMap<Set<Integer>, Integer> recursiveBackup = new HashMap<>();
    
    /**
     * Add a link to the graph
     * @param u a person (one of the end nodes of a link)
     * @param v another person (one of the end nodes of a link)
     * @param t the event time (length of the link)
     */
    public void addLink(int u, int v, int t) {
        Set<Integer> set=getKeySet(u,v);
        
        //add a link
        if (links.containsKey(set)) {
            //The link already exists
            if (links.get(set)>t)
                // the old time is later than the new time, we need to update. (using shortest length)
                links.put(set,t);
        }
        else links.put(set,t); // the link did not exist, we just add it.
        
        // add the redundant data for the link.
        Set<Integer> childNodes;
        if (linkMap.containsKey(u)) {
            //the node u has child nodes and has been recorded, we get the child node set.
            childNodes=linkMap.get(u);
        }
        else {
            // the node u has no child nodes and we need to generate the child node set.
            childNodes=new HashSet<>();
            this.linkMap.put(u, childNodes);
        }
        childNodes.add(v); // Add the v as a child of u.
        
        // Do the same thing for node v since it is a graph and the links are bidirectional.
        if (linkMap.containsKey(v)) {
            childNodes=linkMap.get(v);
        }
        else {
            childNodes=new HashSet<>();
            this.linkMap.put(v, childNodes);
        }
        childNodes.add(u);
    }

    /**
     * Get the shortest distance of 2 nodes (earliest time for 2 persons become friends)
     * @param pair The 2 node set contains 2 people.
     * @return The shortest distance (the earliest time that 2 people became friends). 
     * -1 for nodes not connected (2 persons are not friends)
     */
    int getShortestRoute(Set<Integer> pair) {
        Integer[] routeDesc=getRoutDesc(pair);
        return getShortestRoute(routeDesc[0],routeDesc[1]);
    }
    
    /**
     * A different method signature of getShortestRoute.
     * @param node1 The starting node (one of the persons)
     * @param node2 The ending node (another person)
     * @return The shortest distance between 2 nodes. -1 for 2 nodes not connected.
     */
    int getShortestRoute(int node1,int node2)
    {
        Set<Integer> key=getKeySet(node1,node2);
        
        if (this.recursiveBackup.containsKey(key)) 
            // if the result is previously calculated, we just return the previous value. 
            //-1 may be returned, which means we are currently recursively calculating the value and we should stop
            //here to avoid dead loop. (a circle in the graph)
            return this.recursiveBackup.get(key);
        this.recursiveBackup.put(key,-1); // Since we are calculating with this parameters, we need to put -1 here
        //to avoid dead loop.
                
        Integer[] neighbours = getNeighbours(node1); // get child nodes of node1 
        int directDistance=getDirectDistance(node1,node2); // if node1 and node2 are directly linked, we get the linked length.
        int distance=-1; // initialize the return value as -1.(disconnected)

        for (int neighbour:neighbours) {
            // loop over all child nodes, which means we will get route from node1 to node 2 through node1's child nodes.
            if (neighbour==node2) continue; // exclude the destiny.
            int distance1=getShortestRoute(node1,neighbour); // Get the shortest distance from node1 to neighbour.
            int distance2=getShortestRoute(neighbour,node2); // Get the shortest distance from neighbour to node2.
            int routeDistance = sumDistance(distance1,distance2); //Add up to get shortest distance from node1 to node2 through neighbour.
            
            distance=getShorterDistance(distance,routeDistance); //get the shortest distance from node1 to node2 throught all neighbours.
        }
        // get the shortest distance from node1 to node2 from direct distance and indirect distance(through neighbours)
        distance=getShorterDistance(distance,directDistance); 
        recursiveBackup.put(key, distance); // back up the result to gain performance.
        return distance;
    }

    /**
     * Get 2 integers from the Set (2 nodes)
     * @param pair the pair of friends (2 nodes inside)
     * @return the 2 element array contains 2 integers.
     */
    private Integer[] getRoutDesc(Set<Integer> pair) {
        return pair.toArray(new Integer[pair.size()]);
    }

    /**
     * Get neighbors from a node (child nodes that linked with the node)
     * @param node The node to retrieve neighbors.
     * @return An array of neighbors.
     */
    private Integer[] getNeighbours(Integer node) {
        if (this.linkMap.containsKey(node)) {
            //The node has child nodes.
            Set<Integer> childNodes=this.linkMap.get(node);
            return childNodes.toArray(new Integer[childNodes.size()]);
        }
        else {
            // No neighbors
            return new Integer[0];
        }
    }

    /**
     * Add up 2 distances. In this application the "Add distance" is not arithmetic addition. Instead
     * it is the larger one of the distances. The distance represent the earliest time of 2 people become friends.
     * When the become friends through other people, the time they become friends should be the latest time that 
     * the they are connected.
     * @param distance1 the distance of the first part (the earliest time the first person connects with the intermediate person)
     * @param distance2 the distance of the second part (the earliest time the second person connects with the intermediate person)
     * @return the distance between node1 and node2 (the earliest time that first person connects with the second person.)
     */
    private int sumDistance(int distance1, int distance2) {
        //If either of distance is -1 which means not connected, the node1 and node 2 are not connected. 
        if (distance1==-1 || distance2==-1) return -1;
        // return the larger distance (the time that 2 persons are connected.)
        return (distance1<distance2)?distance2:distance1;
    }

    /**
     * Get direct distance of 2 linked nodes. (the earliest event that both person attended.)
     * @param start The starting node.
     * @param end The ending node.
     * @return the direct distance.
     */
    private int getDirectDistance(int start, int end) {
        Set<Integer> key=getKeySet(start,end);
        
        if (this.links.containsKey(key)) {
            // 2 nodes are linked.
            return this.links.get(key);
        } 
        // 2 nodes are not liked.
        return -1;
    }

    /**
     * Get the shortest distance of 2 routes (the earliest time that 2 persons become friend 
     * through different third people).
     * @param distance distance of one route.
     * @param routeDistance distance of another route.
     * @return The shortest distance.
     */
    private int getShorterDistance(int distance, int routeDistance) {
        //If one of the distance is -1, it means this route is not a connected route. We should use another.
        if (distance==-1)
            return routeDistance;
        if (routeDistance==-1) 
            return distance;
        //If both routes are connected, choose the shorter one.
        return (distance>routeDistance)?routeDistance:distance;
    }

    /**
     * Convert 2 nodes to a key set.
     * @param node1 node 1
     * @param node2 node 2
     * @return The key set.
     */
    private Set<Integer> getKeySet(int node1, int node2) {
        Set<Integer> key=new HashSet<>();
        key.add(node1);
        key.add(node2);
        return key;
    }
}


