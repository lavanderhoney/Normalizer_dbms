package Normalizer;

import static Normalizer.Helpers.getClosureSet;
import static Normalizer.Helpers.getClosureSetFD;
import static Normalizer.Helpers.refineFinds;
import static Normalizer.Helpers.searchIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Converters {
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

    public static Map<String, HashMap<String, String>> convert3NF(String schema, ArrayList<String> ckeys,
            HashMap<String, String> fds, ArrayList<ArrayList<String>> minimal) {
        Map<String, HashMap<String, String>> rel_3nf = new HashMap<>();
        // ArrayList<ArrayList<String>> minimal = canonical_cover;
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
