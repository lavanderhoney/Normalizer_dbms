package Normalizer;

import java.util.ArrayList;
import java.util.HashMap;

public class Calculators {
    
     public ArrayList<String> calc_CK(HashMap<String, String> dict_fds, String schema) {
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
}
