package ItemSimilarityManager;

import java.util.*;
import java.util.stream.IntStream;

public class ItemSimilarityObject {
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap;
    /** Item Rating Map      */ private HashMap<Integer, HashMap<Integer, Double>> itemRatingsMap = new HashMap<>();
    /** Similarity Matrix    */ private Float[][] simMatrix;
    /** Neighbors Map        */ private Map<Integer, ArrayList<AbstractMap.SimpleEntry<Integer, Float>>> neighborsMap = new HashMap<>();
    /** Number of Items      */ private int nItems;
    /** Users Map            */ private Map<Integer, Integer> Publicusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Publicitems = new HashMap<>();
    /** Users Map            */ private Map<Integer, Integer> Privateusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Privateitems = new HashMap<>();
    /** Number of Neighbors  */ private int numberOfNeighs = -1;
    /** Items Set            */ private Set<Integer> itemSet;
    /** Items Map            */ private Map<Integer, Integer> PublicitemsIFMatrix = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> PrivateitemsIFMatrix = new HashMap<>();
    /** Items Factors Matrix */ private double[][] ifMatrix;
    /** Items Bias Matrix    */ private double[] ibMatrix;

    public ItemSimilarityObject(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, int numberOfNeighs,double[][] ifMatrix,double[] ibMatrix,Map<Integer, Integer> PublicitemsIFMatrix,Map<Integer, Integer> PrivateitemsIFMatrix){
        this.ifMatrix = ifMatrix;
        this.ibMatrix = ibMatrix;
        this.PublicitemsIFMatrix = PublicitemsIFMatrix;
        this.PrivateitemsIFMatrix = PrivateitemsIFMatrix;
        this.numberOfNeighs = numberOfNeighs;
        this.ratingsMap = ratingsMap;
        int i = 0;
        int k = 0;
        for (int user : ratingsMap.keySet()){
            for (Map.Entry<Integer, Double> entry : ratingsMap.get(user).entrySet()){
                int   item      = entry.getKey();
                double rating    = entry.getValue();
                HashMap<Integer, Double> itemMap = itemRatingsMap.get(item);
                if (itemMap==null) itemMap = new HashMap<>();
                itemMap.put(user,rating);
                itemRatingsMap.put(item,itemMap);
                if (!Publicitems.containsKey(item)){
                    Publicitems.put(item,k);
                    Privateitems.put(k,item);
                    k++;
                }
            }
            Publicusers.put(user,i);
            Privateusers.put(i,user);
            i++;
        }
        this.nItems = Privateitems.keySet().size();
        this.simMatrix = new Float[nItems][nItems];
        this.itemSet = Publicitems.keySet();
        Run();
    }

    public ItemSimilarityObject(HashMap<Integer, HashMap<Integer, Double>> ratingsMap){
        this.ratingsMap = ratingsMap;
        int i = 0;
        int k = 0;
        for (int user : ratingsMap.keySet()){
            for (Map.Entry<Integer, Double> entry : ratingsMap.get(user).entrySet()){
                int   item      = entry.getKey();
                double rating    = entry.getValue();
                HashMap<Integer, Double> itemMap = itemRatingsMap.get(item);
                if (itemMap==null) itemMap = new HashMap<>();
                itemMap.put(user,rating);
                itemRatingsMap.put(item,itemMap);
                if (!Publicitems.containsKey(item)){
                    Publicitems.put(item,k);
                    Privateitems.put(k,item);
                    k++;
                }
            }
            Publicusers.put(user,i);
            Privateusers.put(i,user);
            i++;
        }
        this.nItems = Privateitems.keySet().size();
        this.simMatrix = new Float[nItems][nItems];
        this.itemSet = Publicitems.keySet();
        Run();
    }

    public void Run(){
        ProcessBCSimilarityFactors();
        computeNeighbors();
    }

    private void computeNeighbors(){
        final int[] debug = {0};
        IntStream.range(0, simMatrix[0].length).parallel().mapToObj(i -> {
            ArrayList<AbstractMap.SimpleEntry<Integer, Float>> neighs = new ArrayList<>();
            IntStream.range(i+1, simMatrix[0].length).mapToObj(col -> new AbstractMap.SimpleEntry<>(Privateitems.get(col),simMatrix[i][col])).forEachOrdered(neighs::add);
            IntStream.range(0, i).mapToObj(row -> new AbstractMap.SimpleEntry<>(Privateitems.get(row),simMatrix[row][i])).forEachOrdered(neighs::add);

            Collections.sort(neighs,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));

            if (numberOfNeighs != -1){
                neighs = new ArrayList<>(neighs.subList(0,numberOfNeighs));
            }
            System.out.println(debug[0]);
            debug[0]++;
            return new AbstractMap.SimpleEntry<>(Privateitems.get(i),neighs);
        }).forEachOrdered(e -> neighborsMap.put(e.getKey(),e.getValue()));
    }
    @SuppressWarnings("Duplicates")
    private void ProcessBCSimilarityFactors(){
        final int[] debug = {0};
        IntStream.range(0, nItems).parallel().forEach(i -> {
            System.out.println(debug[0]);
            debug[0]++;
            double[] item_i_array = ifMatrix[PublicitemsIFMatrix.get(Privateitems.get(i))];
            int finalI = i;
            IntStream.range(i+1, nItems).forEach(item_j_index -> {
                simMatrix[finalI][item_j_index] = computeBCSimilarityFactors(item_i_array,ifMatrix[PublicitemsIFMatrix.get(Privateitems.get(item_j_index))]);
            });
        });
    }
    public float computeBCSimilarityFactors(double[] subject_i_array, double[] subject_j_array){
        double num = 0f;
        double i_Weights = 0f;
        double j_Weights = 0f;
        for (int i = 0; i<subject_i_array.length; i++){
            num         = num + subject_i_array[i]*subject_j_array[i];
            i_Weights += Math.pow(subject_i_array[i], 2);
            j_Weights += Math.pow(subject_j_array[i], 2);
        }
        double den = (Math.sqrt(i_Weights)*Math.sqrt(j_Weights));
        return (float) ((den!=0)?num/den:0f);
    }


    public Map<Integer, ArrayList<AbstractMap.SimpleEntry<Integer, Float>>> getNeighborsMap() {
        return neighborsMap;
    }

    public ArrayList<AbstractMap.SimpleEntry<Integer, Float>> getNeighbors(int i) {
        return neighborsMap.get(i);
    }

    public Set<Integer> getItemSet() { return itemSet; }
}
