package Normalizer;

import static Normalizer.Helpers.searchIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Checkers {
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
                if (Helpers.searchIn(attr, key) && !attr.equals(key) && !Helpers.searchIn(fds.get(attr), key)) {
                    return false;
                }
            }
        }
        return true;
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

     public static boolean checkBCNF(HashMap<String, String> fds, ArrayList<String> skeys) {
        for (Map.Entry<String, String> entry : fds.entrySet()) {
            if (!skeys.contains(entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}
