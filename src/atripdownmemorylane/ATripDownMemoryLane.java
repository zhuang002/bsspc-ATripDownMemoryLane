/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atripdownmemorylane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 *
 * @author zhuan
 */
public class ATripDownMemoryLane {

    /**
     * @param args the command line arguments
     */
    static HashMap<Friend, Integer> eventRecords = new HashMap<Friend, Integer>();
    static ArrayList<Friend> questions = new ArrayList<Friend>();
    static Scanner sc = new Scanner(System.in);
    static HashMap<Friend, Integer> recursiveBackup = new HashMap<Friend,Integer>();

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
                case 1:
                    int t = sc.nextInt();
                    buildEvents(u, v, t);
                    break;
                case 2:
                    Friend friend = new Friend(u, v);
                    questions.add(friend);
                    break;
                default:
                    sc.nextLine();
            }
        }
    }

    private static void processQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            System.out.println(findShortestDistance(questions.get(i)));
        }
    }

    private static void buildEvents(int u, int v, int t) {
        Friend friend = new Friend(u, v);
        if (eventRecords.containsKey(friend)) {
            if ((int) eventRecords.get(friend) > t) {
                eventRecords.put(friend, t);
            }
        } else {
            eventRecords.put(friend, t);
        }
    }

    private static int findShortestDistance(Friend friend) {
        if (recursiveBackup.containsKey(friend))
            return recursiveBackup.get(friend);
        int distance = -1;

        for (Entry<Friend, Integer> node : eventRecords.entrySet()) {
            int distance1=-1;
            int distance2;
            Friend key = node.getKey();
            if (node.getKey().equals(friend)) {
                distance1 = node.getValue();
            } else {
                Friend newFriend = null;
                if (friend.a == key.a) {
                    newFriend = new Friend(key.b, friend.b);
                } else if (friend.a == key.b) {
                    newFriend = new Friend(key.b, friend.b);
                }
                if (newFriend != null) {
                    distance1 = node.getValue();
                    distance2 = findShortestDistance(newFriend);
                    if (distance2==-1)
                        distance1=-1;
                    else if (distance2 > distance1) {
                        distance1 = distance2;
                    }
                }
            }
            if (distance1!=-1 && (distance == -1 || distance1 < distance)) {
                distance = distance1;
            }
        }
        recursiveBackup.put(friend, distance);
        return distance;
    }
}

class Friend {

    public int a;
    public int b;

    public Friend(int u, int v) {
        if (u <= v) {
            this.a = u;
            this.b = v;
        } else {
            this.a = v;
            this.b = u;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != Friend.class ) return false;
        Friend friend = (Friend) o;
        return friend.a == this.a && friend.b == this.b;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + this.a;
        hash = 61 * hash + this.b;
        return hash;
    }

}
