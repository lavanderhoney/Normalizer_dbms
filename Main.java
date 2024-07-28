import java.util.*;
import Normalizer.Relation;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
     
        boolean flag = true;
        while (flag) {
            System.out.println("Enter the number of fds: ");
            int n = sc.nextInt();
            sc.nextLine();
            HashMap<String, String> dict_fds = new HashMap<>();
            String rel_schema = "";
            System.out.println("Enter the relation schema as a string:");
            rel_schema = sc.nextLine();
            
            System.out.println("Enter the " +n+" functional dependencies (as X Y):");
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

            //Example usage
            Relation r1 = new Relation(rel_schema, dict_fds);
            r1.getCandidateKeys();
            r1.getSuperKeys();
            r1.getCanonicalCover();

            System.out.println("Candidate Keys: " + r1.getCandidate_keys());
            System.out.println("Canonical Cover: " + r1.getCanonical_cover());

            r1.check_for2NF();
            if (!r1.is2NF()) {
                r1.convert_2NF();
                System.out.println("2NF form of relation: " + r1.getReln_2NF());
            }else{
                System.out.println("Relation already in 2NF.");
            }

            r1.check_for3NF();
            if (!r1.is3NF()) {
                r1.convert_3NF();
                System.out.println("3NF form of relation: " + r1.getReln_3NF());
            }else{
                System.out.println("Relation already in 3NF.");
            }
            
            r1.check_forBCNF();
            if (!r1.isBCNF()) {
                r1.convert_BCNF();
                System.out.println("BCNF form of relation: " + r1.getReln_BCNF());
            }else{
                System.out.println("Relation already in BCNF.");
            }


            System.out.println("Write 'true' to continue for more relations or 'false' to stop");
            flag = sc.nextBoolean();
        }

        /*
         * dict_fds is a map or dictionary that holds the functional dependencies.
         * The keys are determinant attributes, values are dependant attributes
         */

        sc.close();
    }

    public static void printRelations(Map<String, HashMap<String, String>> rel) {
        int i = 1;
        for (Map.Entry<String, HashMap<String, String>> entry : rel.entrySet()) {
            System.out.println("R" + i + "(" + entry.getKey() + ") " + "FD: " + entry.getValue());
            i++;
        }
        System.out.println(" ");
    }
}