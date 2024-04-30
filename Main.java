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

        ArrayList<String> candidate_keys = getCandidateKeys(dict_fds, rel_schema);
        ArrayList<String> super_keys = getSuperKeys(candidate_keys, rel_schema);

        System.out.println("Func dependencies: " + dict_fds);
        System.out.println("Candidate keys: " + candidate_keys);
        System.out.println(" ");
        // System.out.println("Super keys: " + super_keys);
        System.out.println("Canonical cover: " + getCanonicalCover(rel_schema, dict_fds));
        System.out.println(" ");

        System.out.println("Relation is 2NF? " + check2NF(rel_schema, candidate_keys, dict_fds));
        if (!check2NF(rel_schema, super_keys, dict_fds)) {
            System.out.println("2NF split: ");
            printRelations(convert2NF(rel_schema, candidate_keys, dict_fds));
        }

        System.out.println("Relation is 3NF? " + check3NF(rel_schema, candidate_keys, super_keys, dict_fds));
        if (!check3NF(rel_schema, candidate_keys, super_keys, dict_fds)) {
            System.out.println("3NF split: ");
            printRelations(convert3NF(rel_schema, candidate_keys, dict_fds));
        }

        System.out.println("Relation is BCNF? " + checkBCNF(dict_fds, super_keys));
        if (!checkBCNF(dict_fds, super_keys)) {
            System.out.println("BCNF split: ");
            printRelations(convertBCNF(rel_schema, dict_fds, super_keys));
        }
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
        // System.out.println("combos: " + combos);
        for (String attr : combos) {
            // to remove the superkeys, or attributes that are redundant
            if (attr != "" && check_sk.get(combos.indexOf(attr))) {
                String closure = sortString(getClosureSet(schema, attr, dict_fds));
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

    public static String getClosureSet(String schema, String s, HashMap<String, String> dict_fds) {
        StringBuilder finds = new StringBuilder();
        finds.append(s);
        Set<String> dets = dict_fds.keySet();
        ArrayList<String> determinants = new ArrayList<>();
        determinants.addAll(dets);
        for (int i = 0; i < determinants.size(); i++) {
            String attr = determinants.get(i);
            if (sortString(finds.toString()).equals(schema)) {
                return finds.toString();
            }
            if (searchIn(attr, finds.toString()) && !searchIn(dict_fds.get(attr), finds.toString())) {
                finds.append(dict_fds.get(attr));
                finds = refineFinds(finds.toString());
                determinants.remove(attr);
                i = -1;
            }
        }
        return finds.toString();
    }

    // safer way is to overload getClosureSet again for 2nf split
    public static HashMap<String, String> getClosureSetFD(String schema, String s,
            HashMap<String, String> dict_fds) {
        StringBuilder finds = new StringBuilder();
        finds.append(s);
        Set<String> dets = dict_fds.keySet();
        ArrayList<String> determinants = new ArrayList<>();
        determinants.addAll(dets);
        HashMap<String, String> temp_fd = new HashMap<>();
        for (int i = 0; i < determinants.size(); i++) {
            String attr = determinants.get(i);
            if (sortString(finds.toString()).equals(schema)) {
                // temp_closureset.add(finds.toString());
                break;
            }
            if (searchIn(attr, finds.toString()) && !searchIn(dict_fds.get(attr), finds.toString())) {
                finds.append(dict_fds.get(attr));
                finds = refineFinds(finds.toString());
                determinants.remove(attr);

                temp_fd.put(attr, dict_fds.get(attr));
                i = -1;
            }
        }

        // temp_closureset.add(finds.toString());
        // ans.add(temp_closureset);

        return temp_fd;
    }

    public static StringBuilder refineFinds(String finds) {
        HashSet<String> set = new HashSet<>();
        for (String s : finds.split("")) {
            set.add(s);
        }
        StringBuilder temp = new StringBuilder();
        for (String s : set) {
            temp.append(s);
        }
        StringBuilder temp_ = new StringBuilder(sortString(temp.toString()));
        return temp_;
    }

    /*
     * method overloading for canonical cover, changed data struct of the 2nd
     * parameter
     */
    public static String getClosureSet(String schema, String s, ArrayList<ArrayList<String>> dict_fds) {
        StringBuilder finds = new StringBuilder();
        finds.append(s);
        for (ArrayList<String> fd : dict_fds) {
            if (fd.get(0).length() <= finds.length()) {
                if (sortString(finds.toString()).equals(schema)) {
                    return finds.toString();
                }
                if (searchIn(fd.get(0), finds.toString()) && !searchIn(fd.get(1), finds.toString())) {
                    finds.append(fd.get(1));
                }
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

    public static ArrayList<String> findFDInArrayList(ArrayList<ArrayList<String>> dict_fds_2d, String target_key) {
        for (ArrayList<String> fd : dict_fds_2d) {
            if (fd.get(0).equals(target_key)) {
                return fd;
            }
        }
        return new ArrayList<String>();
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
                // check: a proper subset of the candidate key determines a non-prime
                // attribute. SO have to add check on the dependent as well, eg: what if p->p ?
                if (searchIn(attr, key) && !attr.equals(key) && !searchIn(fds.get(attr), key)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Map<String, HashMap<String, String>> convert2NF(String schema, ArrayList<String> ckeys,
            HashMap<String, String> fds) {
        /*
         * This function splits the relation to 2NF
         * schema: The relation schema String, provided by the user
         * ckeys: List of the candidate keys
         * fds: functional dependencies
         */
        Set<String> dets = fds.keySet();

        Map<String, HashMap<String, String>> rel_2nf = new HashMap<String, HashMap<String, String>>();
        ArrayList<String> split_lhs = new ArrayList<>();
        String all_attributes = "";
        for (String attr : dets) {
            for (String key : ckeys) {
                if (searchIn(attr, key) && !attr.equals(key) && !searchIn(fds.get(attr), key)) {
                    // a PD is found.
                    HashMap<String, String> fd_2nf = new HashMap<>();
                    String temp_ = attr;
                    split_lhs.add(attr);
                    temp_ += fds.get(attr);
                    fd_2nf.put(attr, fds.get(attr));
                    // adding Y+
                    String depndt_closure = getClosureSet(schema, fds.get(attr), fds);
                    fd_2nf.putAll(getClosureSetFD(schema, fds.get(attr), fds));

                    // System.out.println("FDs with Y+ as: " + depndt_closure + " is: " + fd_2nf);
                    temp_ += depndt_closure;
                    temp_ = refineFinds(temp_).toString(); // remove duplicating attributes
                    all_attributes += temp_;
                    rel_2nf.put(temp_, fd_2nf);

                    break;
                    // rel_2nf_fd.put(attr, fds.get(attr));
                }
            }
        }

        // Creating the joining relation
        all_attributes = refineFinds(all_attributes).toString();
        HashSet<String> tempSet = new HashSet<>();
        for (String s : split_lhs) {
            for (String ss : s.split("")) {
                tempSet.add(ss);
            }
        }
        for (String s : schema.split("")) {
            if (!all_attributes.contains(s)) {
                tempSet.add(s);
            }
        }
        String join_reln = "";
        for (String s : tempSet) {
            join_reln += s;
        }

        // combine all CKs
        // String relckey = "";
        // for (String ckey : ckeys) {
        // relckey += ckey;
        // }

        rel_2nf.put(join_reln, new HashMap<>());
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

    public static Map<String, HashMap<String, String>> convert3NF(String schema, ArrayList<String> ckeys,
            HashMap<String, String> fds) {
        Map<String, HashMap<String, String>> rel_3nf = new HashMap<>();
        ArrayList<ArrayList<String>> minimal = getCanonicalCover(schema, fds);
        List<String> depndts = new ArrayList<>();

        /*
         * Aggregate the decomposed FDs from the canonical cover and viola, that will be
         * the 3NF form, as per synthesis algorithm
         */

        for (int i = 0; i < minimal.size(); i++) {
            HashMap<String, String> temp_fd = new HashMap<>();
            String rln = "";
            if ((i + 1 < minimal.size()) && minimal.get(i).get(0).equals(minimal.get(i + 1).get(0))) {
                String dpndt = minimal.get(i).get(1);
                dpndt += minimal.get(i + 1).get(1);
                temp_fd.put(minimal.get(i).get(0), dpndt);
                rln = minimal.get(i).get(0);
                rln += dpndt;
                depndts.add(dpndt);
                i++;
            } else {
                temp_fd.put(minimal.get(i).get(0), minimal.get(i).get(1));
                rln = minimal.get(i).get(0);
                rln += minimal.get(i).get(1);
                depndts.add(minimal.get(i).get(1));
            }
            rel_3nf.put(rln, temp_fd);
        }

        // Create joining relation
        String reln = "";
        for (String attr : schema.split("")) {
            if (!depndts.contains(attr)) {
                reln += attr;
            }
        }
        rel_3nf.put(reln, new HashMap<>());
        return rel_3nf;
    }

    public static HashMap<String, String> ListFDtoHashMapFD(ArrayList<ArrayList<String>> dict_fds_2d) {
        HashMap<String, String> dict_fds = new HashMap<>();
        // iterate over dict_fds and combine the decomposed fds into single, like d-a,
        // d-b to d-ab
        for (ArrayList<String> fd : dict_fds_2d) {
            dict_fds.put(fd.get(0), fd.get(1));
        }
        return dict_fds;
    }

    public static ArrayList<ArrayList<String>> getCanonicalCover(String schema, HashMap<String, String> dict_fds) {
        ConcurrentHashMap<String, String> minimal = new ConcurrentHashMap<>(dict_fds);

        // Convert dict_fds HashMap into a 2D ArrayList, because HashMap doesnt allow
        // duplicate keys
        ArrayList<ArrayList<String>> dict_fds_2d = new ArrayList<ArrayList<String>>();
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
        // System.out.println("decomposed fds: " + dict_fds_2d);

        // Finding redundant FDs
        ArrayList<ArrayList<String>> dict_fds_2d_copy = new ArrayList<ArrayList<String>>();
        dict_fds_2d_copy.addAll(dict_fds_2d);
        // System.out.println("copy: " + dict_fds_2d_copy);
        for (ArrayList<String> fd : dict_fds_2d) {
            String closure_avec = sortString(getClosureSet(schema, fd.get(0), dict_fds_2d_copy));
            // System.out.println("ca of:" + fd.get(0) + " " + closure_avec);
            String rhs = fd.get(1);
            String lhs = fd.get(0);
            dict_fds_2d_copy.remove(fd);
            String closure_sans = sortString(getClosureSet(schema, lhs, dict_fds_2d_copy));
            // System.out.println("cs of:" + fd.get(0) + " " + closure_sans);

            /*
             * If equal, then this fd of particular attr is required so add it back to
             * dict_fds. Else, this fd is redundant and don't add it
             */
            if (!closure_avec.equals(closure_sans)) {
                ArrayList<String> tempfd = new ArrayList<>();
                tempfd.add(lhs);
                tempfd.add(rhs);
                // System.out.println("tempfd: " + tempfd);
                dict_fds_2d_copy.add(tempfd);
            }
        }
        // System.out.println("dict copy, canonical cover: " + dict_fds_2d_copy);
        ListFDtoHashMapFD(dict_fds_2d_copy);
        return dict_fds_2d_copy;
    }

    public static boolean checkBCNF(HashMap<String, String> fds, ArrayList<String> skeys) {
        for (Map.Entry<String, String> entry : fds.entrySet()) {
            if (!skeys.contains(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, HashMap<String, String>> convertBCNF(String schema, HashMap<String, String> fds,
            ArrayList<String> skeys) {
        Map<String, HashMap<String, String>> rel_bcnf = new HashMap<>();
        List<String> depndts = new ArrayList<>();
        for (Map.Entry<String, String> entry : fds.entrySet()) {
            String reln = "";
            HashMap<String, String> temp_fd = new HashMap<>();
            if (!skeys.contains(entry.getKey())) {
                // this FD violates BCNF
                reln = entry.getKey();
                reln += entry.getValue();
                depndts.add(entry.getValue());
                temp_fd.put(entry.getKey(), entry.getValue());
                rel_bcnf.put(reln, temp_fd);
            }
        }

        // Creating the joining relation
        String reln = "";
        for (String attr : schema.split("")) {
            if (!depndts.contains(attr)) {
                reln += attr;
            }
        }
        rel_bcnf.put(reln, new HashMap<>());

        return rel_bcnf;
    }
}