package Normalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Calculators {
    
    public static ArrayList<String> calc_CK(HashMap<String, String> dict_fds, String schema) {
        ArrayList<String> ck = new ArrayList<>();
        ArrayList<String> combos = new ArrayList<>();

        Helpers.allCombos(schema, 0, combos, "");

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
                String closure = Helpers.sortString(Helpers.getClosureSet(schema, attr, dict_fds));
                // System.out.println("CLosure of " + attr + ": " + closure);
                if (closure.equals(schema)) {
                    ck.add(attr);

                    /*
                     * if any attribute in the comibantion list contains the attr candidate key,
                     * than its corresponding position in the check_sk will be marked false
                     */
                    for (String s : combos) {
                        if (Helpers.searchIn(attr, s) && !s.equals(attr)) {
                            check_sk.set(combos.indexOf(s), false);
                        }
                    }
                }
            }
        }

        return ck;
    }

    public static ArrayList<String> calc_SK(ArrayList<String> ckeys, String rel_schema) {
        ArrayList<String> skey = new ArrayList<>();
        ArrayList<String> combos = new ArrayList<>();
        Helpers.allCombos(rel_schema, 0, combos, "");
        for (String attribute : combos) {
            for (String key : ckeys) {
                if (Helpers.searchIn(key, attribute)) {
                    skey.add(attribute);
                }
            }

        }
        return skey;
    }

    public static ArrayList<ArrayList<String>> calc_CC(String schema, HashMap<String, String> dict_fds) {
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

        // Finding redundant FDs
        ArrayList<ArrayList<String>> dict_fds_2d_copy = new ArrayList<ArrayList<String>>();
        dict_fds_2d_copy.addAll(dict_fds_2d);
        for (ArrayList<String> fd : dict_fds_2d) {
            String closure_avec = Helpers.sortString(Helpers.getClosureSet(schema, fd.get(0), dict_fds_2d_copy));
            String rhs = fd.get(1);
            String lhs = fd.get(0);
            dict_fds_2d_copy.remove(fd);
            String closure_sans = Helpers.sortString(Helpers.getClosureSet(schema, lhs, dict_fds_2d_copy));

            /*
             * If equal, then this fd of particular attr is required so add it back to
             * dict_fds. Else, this fd is redundant and don't add it
             */
            if (!closure_avec.equals(closure_sans)) {
                ArrayList<String> tempfd = new ArrayList<>();
                tempfd.add(lhs);
                tempfd.add(rhs);
                dict_fds_2d_copy.add(tempfd);
            }
        }
        Helpers.ListFDtoHashMapFD(dict_fds_2d_copy);
        return dict_fds_2d_copy;
    }
}
