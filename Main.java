import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // int n = 2;
        int n = 3;
        // System.out.println("Enter the number of fds: ");
        // int n = sc.nextInt();
        // sc.nextLine();

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
        // printLinks(links);
        ArrayList<String> candidate_keys = getCandidateKeys(dict_fds, rel_schema);
        ArrayList<String> super_keys = getSuperKeys(candidate_keys, rel_schema);
        System.out.println("Func dependencies: " + dict_fds);
        System.out.println("Candidate keys: " + candidate_keys);
        System.out.println("Relation is 2NF? " + check2NF(rel_schema, candidate_keys, dict_fds));
        convert2NF(rel_schema, candidate_keys, dict_fds);
        System.out.println("Super keys: " + super_keys);
        System.out.println("Relation is 3NF? " + check3NF(rel_schema, candidate_keys, super_keys, dict_fds));
        sc.close();
    }

    // --> put all the ck stuff in a separate file
    public static ArrayList<String> getCandidateKeys(HashMap<String, String> dict_fds, String schema) {
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

                    /*
                     * if any attribute in the comibantion list contains the attr candidate key,
                     * than its corresponding position in the check_sk will be marked false
                     */
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

    // method overloading for canonical cover, changed data struct of the 2nd
    // parameter
    public static String getClosureSet(String s, ArrayList<ArrayList<String>> dict_fds) {
        StringBuilder finds = new StringBuilder();
        finds.append(s);
        ArrayList<String> determinants = new ArrayList<>();

        for (ArrayList<String> fd : dict_fds) {
            determinants.add(fd.get(0));
        }
        for (int i = 0; i < determinants.size(); i++) {
            String attr = determinants.get(i);
            if (searchIn(attr, finds.toString())
                    && !searchIn(findDependentInArrayList(dict_fds, attr), finds.toString())) {
                finds.append(findDependentInArrayList(dict_fds, attr));
                determinants.remove(attr);
                i = -1;
            }
        }
        return finds.toString();
    }

    public static String findDependentInArrayList(ArrayList<ArrayList<String>> dict_fds_2d, String target_key) {
        for (ArrayList<String> fd : dict_fds_2d) {
            if (fd.get(0).equals(target_key)) {
                return fd.get(1);
            }
        }
        return " ";
    }

    public static boolean searchIn(String target, String findIn) {
        /*
         * function to search whether a target string is inside the findIn string.
         * For e.g: returns true for target="AC", findIn="ABC"
         * It is sort of a subset finder. Checks whether target is subset of findIn
         */
        boolean main_flag = true;
        boolean sub_flag = false;
        for (String s : target.split("")) {
            sub_flag = false;
            for (String fs : findIn.split("")) {
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

    public static boolean check2NF(String schema, ArrayList<String> ckeys, HashMap<String, String> fds) {
        /*
         * This function checks whether this relation is in 2NF or not.
         * schema: The relation schema String, provided by the user
         * ckeys: List of the candidate keys
         * fds: functional dependencies
         */

        Set<String> dets = fds.keySet();
        for (String key : ckeys) {
            for (String attr : dets) {
                if (searchIn(attr, key) && !attr.equals(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<String> convert2NF(String schema, ArrayList<String> ckeys,
            HashMap<String, String> fds) {
        /*
         * This function splits the relation to 2NF
         * schema: The relation schema String, provided by the user
         * ckeys: List of the candidate keys
         * fds: functional dependencies
         */

        ArrayList<String> rel_2nf = new ArrayList<String>();
        Set<String> dets = fds.keySet();
        for (String key : ckeys) {
            for (String attr : dets) {
                if (searchIn(attr, key) && !attr.equals(key)) {
                    // a pd is found.
                    String temp = getClosureSet(attr, fds);
                    rel_2nf.add(temp);
                }
            }
        }
        System.out.println("2NF split: " + rel_2nf);
        return rel_2nf;
    }

    public static ArrayList<String> getSuperKeys(ArrayList<String> ckeys, String rel_schema) {
        ArrayList<String> skey = new ArrayList<>();
        ArrayList<String> combos = new ArrayList<>();
        allCombos(rel_schema, 0, combos, "");
        for (String attribute : combos) {
            for (String key : ckeys) {
                if (searchIn(key, attribute)) {
                    skey.add(attribute);
                }
            }

        }
        return skey;
    }

    public static boolean check3NF(String schema, ArrayList<String> ckeys, ArrayList<String> skeys,
            HashMap<String, String> fds) {
        Set<String> determinants = fds.keySet();
        boolean aflag = true;
        for (String alpha : determinants) {
            if (!skeys.contains(alpha)) {
                aflag = false;
            }
        }

        boolean bflag = false;
        for (String alpha : determinants) {
            for (String ckey : ckeys) {
                if (searchIn(fds.get(alpha), ckey)) {
                    bflag = true;
                }
            }
        }

        return aflag || bflag;
    }

    public static ArrayList<String> convert3NF(String schema, ArrayList<String> ckeys,
            HashMap<String, String> fds) {
        ArrayList<String> rel_3nf = new ArrayList<>();

        return rel_3nf;
    }

    public static HashMap<String, String> getCanonicalCover(String schema, HashMap<String, String> dict_fds) {
        HashMap<String, String> minimal = dict_fds;
        // Convert dict_fds HashMap into a 2D ArrayList, necause HashMap doesnt allow
        // duplicate keys
        ArrayList<ArrayList<String>> dict_fds_2d = new ArrayList<>();
        for (Map.Entry<String, String> entry : minimal.entrySet()) {
            ArrayList<String> entryList = new ArrayList<>();
            entryList.add(entry.getKey());
            entryList.add(entry.getValue());
            dict_fds_2d.add(entryList);
        }
        // split the fds
        Set<String> lhs_ = minimal.keySet();
        for (String attr : lhs_) {
            String dpndt = minimal.get(attr);
            if (dpndt.length() > 1) {
                // String rhs = minimal.get(attr);
                minimal.remove(attr, dpndt);

                // To remove this composite FD from the list
                ArrayList<String> rem = new ArrayList<>();
                rem.add(attr);
                rem.add(dpndt);
                dict_fds_2d.remove(rem);

                // Add the decomposed FD
                String[] splitted_attributes = dpndt.split("");
                for (String s : splitted_attributes) {
                    ArrayList<String> tempfd = new ArrayList<>();
                    tempfd.add(attr);
                    tempfd.add(s);
                    dict_fds_2d.add(tempfd);
                }
            }
        }

        // Finding redundant FDs
        Set<String> lhs = minimal.keySet();
        for (String attr : lhs) {
            String closure_avec = getClosureSet(attr, minimal);
            String rhs = minimal.get(attr);
            minimal.remove(attr, rhs);
            String clousure_sans = getClosureSet(attr, minimal);

            // If equal, then this fd of particular attr is required so add it back to
            // dict_fds. Else, this fd is redundant and don't add it
            if (!closure_avec.equals(clousure_sans)) {
                minimal.put(attr, rhs);
            }
        }
        return minimal;
    }
}