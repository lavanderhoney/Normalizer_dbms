import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // int n = 2;
        // int n = 5;
        System.out.println("Enter the number of fds: ");
        int n = sc.nextInt();
        sc.nextLine();

        /*
         * dict_fds is a map or dictionary that holds the functional dependencies.
         * The keys are determinant attributes, values are dependant attributes
         */
        HashMap<String, String> dict_fds = new HashMap<>();
        String rel_schema = "";
        System.out.println("Enter the relation schema as a string:");
        rel_schema = sc.nextLine();

        for (int i = 0; i < n; i++) {
            String s = sc.nextLine();
            String[] fds = s.split(" "); // 0: determinant 1: dependent in fds string array
            String temp;
            if (dict_fds.containsKey(fds[0])) {
                temp = dict_fds.get(fds[0]);
                temp += fds[1];
                dict_fds.put(fds[0], temp);
            } else {
                dict_fds.put(fds[0], fds[1]);
            }
        }
        sc.close();

        Relation r1 = new Relation(rel_schema, dict_fds);
        r1.getCandidateKeys();
        System.out.println(r1.getCandidate_keys());
        // ArrayList<String> candidate_keys = getCandidateKeys(dict_fds, rel_schema);
        // ArrayList<String> super_keys = getSuperKeys(candidate_keys, rel_schema);

        // System.out.println("Func dependencies: " + dict_fds);
        // System.out.println("Candidate keys: " + candidate_keys);
        // System.out.println(" ");
        // // System.out.println("Super keys: " + super_keys);
        // System.out.println("Canonical cover: " + getCanonicalCover(rel_schema,
        // dict_fds));
        // System.out.println(" ");

        // System.out.println("Relation is 2NF? " + check2NF(rel_schema, candidate_keys,
        // dict_fds));
        // if (!check2NF(rel_schema, super_keys, dict_fds)) {
        // System.out.println("2NF split: ");
        // printRelations(convert2NF(rel_schema, candidate_keys, dict_fds));
        // }

        // System.out.println("Relation is 3NF? " + check3NF(rel_schema, candidate_keys,
        // super_keys, dict_fds));
        // if (!check3NF(rel_schema, candidate_keys, super_keys, dict_fds)) {
        // System.out.println("3NF split: ");
        // printRelations(convert3NF(rel_schema, candidate_keys, dict_fds));
        // }

        // System.out.println("Relation is BCNF? " + checkBCNF(dict_fds, super_keys));
        // if (!checkBCNF(dict_fds, super_keys)) {
        // System.out.println("BCNF split: ");
        // printRelations(convertBCNF(rel_schema, dict_fds, super_keys));
        // }
    }

    public static void printRelations(Map<String, HashMap<String, String>> rel) {
        int i = 1;
        for (Map.Entry<String, HashMap<String, String>> entry : rel.entrySet()) {
            System.out.println("R" + i + "(" + entry.getKey() + ") " + "FD: " + entry.getValue());
            i++;
        }
        System.out.println(" ");
    }

    // --> put all the ck stuff in a separate file
}