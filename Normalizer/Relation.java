package Normalizer;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

//Importing the functionalities
import static Normalizer.Calculators.*;
import static Normalizer.Checkers.*;
import static Normalizer.Converters.*;

@Getter
@Setter
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
        this.relString = rel;
        this.functional_dependencies = fd;
    }

    public void getCandidateKeys() {
        this.candidate_keys = calc_CK(functional_dependencies, relString);
    }

    public void getSuperKeys() {
        this.super_keys = calc_SK(candidate_keys, relString);
    }

    public void getCanonicalCover() {
        this.canonical_cover = calc_CC(relString, functional_dependencies);
    }

    public void check_for2NF() {
        this.is2NF = check2NF(relString, candidate_keys, functional_dependencies);
    }

    public void check_for3NF() {
        this.is3NF = check3NF(relString, candidate_keys, super_keys, functional_dependencies);
    }

    public void check_forBCNF() {
        this.isBCNF = checkBCNF(functional_dependencies, super_keys);
    }

    public void convert_2NF() {
        this.reln_2NF = convert2NF(relString, candidate_keys, functional_dependencies);
    }

    public void convert_3NF() {
        //In the case if user doesn't invoke the getCaninocalCover method
        if (this.canonical_cover==null) {
            getCanonicalCover();
        }
        this.reln_3NF = convert3NF(relString, candidate_keys, functional_dependencies,canonical_cover);
    }

    public void convert_BCNF() {
        this.reln_BCNF = convertBCNF(relString, functional_dependencies, candidate_keys);
    }
}
