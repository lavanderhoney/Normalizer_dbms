package Normalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static Normalizer.Helpers.*;

public class Relation {
    // atrributes of relation class
    private String relString;
    private HashMap<String, String> functional_dependencies;
    private ArrayList<String> candidate_keys;
    private ArrayList<String> super_keys;
    private ArrayList<ArrayList<String>> canonical_cover;

    private boolean is2NF;
    private boolean is3NF;
    private boolean isBCNF;

    private Map<String, HashMap<String, String>> reln_2NF;
    private Map<String, HashMap<String, String>> reln_3NF;
    private Map<String, HashMap<String, String>> reln_BCNF;

    public Relation() {
        // Throw error
        System.out.println("Please provide the schema string and FDS");
    }

    public Relation(String rel, HashMap<String, String> fd) {
        relString = rel;
        functional_dependencies = fd;
    }

    public void getCandidateKeys() {
        candidate_keys = calc_CK(functional_dependencies, relString);
    }

    public void getSuperKeys() {
        super_keys = calc_SK(candidate_keys, relString);
    }

    public void getCanonicalCover() {
        canonical_cover = calc_CC(relString, functional_dependencies);
    }

    public void check_for2NF() {
        is2NF = check2NF(relString, candidate_keys, functional_dependencies);
    }

    public void convert_2NF() {
        if (!is2NF) {
            // throw custom error
            return;
        }
        reln_2NF = 
    }

    private ArrayList<String> calc_CK(HashMap<String, String> dict_fds, String schema) {
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

    private ArrayList<String> calc_SK(ArrayList<String> ckeys, String rel_schema) {
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

    private ArrayList<ArrayList<String>> calc_CC(String schema, HashMap<String, String> dict_fds) {
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

    private boolean check2NF(String schema, ArrayList<String> ckeys, HashMap<String, String> fds) {
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
                if (Helpers.searchIn(attr, key) && !attr.equals(key) && !Helpers.searchIn(fds.get(attr), key)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<String, HashMap<String, String>> convert2NF(String schema, ArrayList<String> ckeys,
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
                if (Helpers.searchIn(attr, key) && !attr.equals(key) && !Helpers.searchIn(fds.get(attr), key)) {
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
}
