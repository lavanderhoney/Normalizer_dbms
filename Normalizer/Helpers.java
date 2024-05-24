package Normalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Helpers {
    public static void main(String[] args) {

    }

    public static void printRelations(Map<String, HashMap<String, String>> rel) {
        int i = 1;
        for (Map.Entry<String, HashMap<String, String>> entry : rel.entrySet()) {
            System.out.println("R" + i + "(" + entry.getKey() + ") " + "FD: " + entry.getValue());
            i++;
        }
        System.out.println(" ");
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

    public static HashMap<String, String> ListFDtoHashMapFD(ArrayList<ArrayList<String>> dict_fds_2d) {
        HashMap<String, String> dict_fds = new HashMap<>();
        // iterate over dict_fds and combine the decomposed fds into single, like d-a,
        // d-b to d-ab
        for (ArrayList<String> fd : dict_fds_2d) {
            dict_fds.put(fd.get(0), fd.get(1));
        }
        return dict_fds;
    }

}
