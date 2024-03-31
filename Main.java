import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = 5;

        HashMap<String, String> dict_fds = new HashMap<>();
        String rel_schema = "";
        System.out.println("Enter the relation schema as a string:");

        rel_schema = sc.nextLine();

        for (int i = 0; i < n; i++) {
            String s = sc.nextLine();
            String[] fds = s.split(" "); // 0: determinant 1: dependent
            String temp;
            if (dict_fds.containsKey(fds[0])) {
                temp = dict_fds.get(fds[0]);
                temp += fds[1];
                dict_fds.put(fds[0], temp);
            } else {
                dict_fds.put(fds[0], fds[1]);
            }
        }
        // printLinks(links);
        ArrayList<String> candidate_keys = getCKeys(dict_fds, rel_schema);
        System.out.println("Func dependencies: " + dict_fds);
        System.out.println("Candidate keys: " + candidate_keys);
        sc.close();
    }

    public static ArrayList<String> getCKeys(HashMap<String, String> dict_fds, String schema) {
        ArrayList<String> ck = new ArrayList<>();
        ArrayList<String> combos = new ArrayList<>();

        allCombos(schema, 0, combos, "");

        // Initializing and creating a boolean list, which will help in efficiently
        // checking for redundant attributes in finding candidate key
        ArrayList<Boolean> check_sk = new ArrayList<Boolean>(combos.size());
        for (int i = 0; i < combos.size(); i++) {
            check_sk.add(true);
        }

        for (String attr : combos) {
            // to remove the superkeys, or attributes that are redundant
            if (check_sk.get(combos.indexOf(attr))) {
                String closure = sortString(getClosureSet(attr, dict_fds));
                // System.out.println("CLosure of " + attr + ": " + closure);
                if (closure.equals(schema)) {
                    ck.add(attr);

                    // if any attribute in the comibantion list contains the attr candidate key,
                    // than its corresponding position in the check_sk will be marked false
                    for (String s : combos) {
                        if (searchIn(attr, s) && !s.equals(attr)) {
                            check_sk.set(combos.indexOf(s), false);
                        }
                    }
                }
            }
        }

        return ck;
    }

    public static String getClosureSet(String s, HashMap<String, String> dict_fds) {
        StringBuilder finds = new StringBuilder();
        finds.append(s);
        Set<String> dets = dict_fds.keySet();
        ArrayList<String> determinants = new ArrayList<>();
        determinants.addAll(dets);
        for (int i = 0; i < determinants.size(); i++) {
            String attr = determinants.get(i);
            if (searchIn(attr, finds.toString()) && !searchIn(dict_fds.get(attr), finds.toString())) {
                finds.append(dict_fds.get(attr));
                determinants.remove(attr);
                i = -1;
            }
        }
        return finds.toString();
    }

    public static boolean searchIn(String target, String finds) {
        boolean main_flag = true;
        boolean sub_flag = false;
        for (String s : target.split("")) {
            sub_flag = false;
            for (String fs : finds.split("")) {
                if (s.equals(fs)) {
                    sub_flag = true;
                }
            }
            if (!sub_flag) {
                main_flag = false;
            }
        }
        return main_flag;
    }

    public static String sortString(String inputString) {
        char tempArray[] = inputString.toCharArray();
        Arrays.sort(tempArray);
        return new String(tempArray);
    }

    public static void allCombos(String schema, int indx, ArrayList<String> res, String temp) {
        if (indx == schema.length()) {
            if (!temp.equals(schema)) {
                res.add(temp);
            }
            return;
        }
        allCombos(schema, indx + 1, res, temp);
        temp += schema.charAt(indx);
        allCombos(schema, indx + 1, res, temp);
        // return res;
    }

}