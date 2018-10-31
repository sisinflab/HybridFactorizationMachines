package ItemSimilarityManager;

import java.util.*;
import java.util.stream.Collectors;

public class ItemSimilarityModel {
    /** Rating Map           */ private  HashMap<Integer, HashMap<Integer, Double>> ratingsMap;
    /** Number of Recs       */ private  int numberOfRecs;
    /** Number of Neighbors  */ private  int numberOfNeighs;
    /** Number of Neighbors  */ public ItemSimilarityObject sim;
    /** Items Map            */ private Map<Integer, Integer> PublicitemsIFMatrix;
    /** Items Map            */ private Map<Integer, Integer> PrivateitemsIFMatrix;
    /** Items Factors Matrix */ private double[][] ifMatrix;
    /** Items Bias Matrix    */ private double[] ibMatrix;

    public ItemSimilarityModel(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, int numberOfNeighs, int numberOfRecs, double[][] ifMatrix, double[] ibMatrix, Map<Integer, Integer> PublicitemsIFMatrix, Map<Integer, Integer> PrivateitemsIFMatrix){
        this.ifMatrix = ifMatrix;
        this.ibMatrix = ibMatrix;
        this.PublicitemsIFMatrix = PublicitemsIFMatrix;
        this.PrivateitemsIFMatrix = PrivateitemsIFMatrix;
        this.ratingsMap = ratingsMap;
        this.numberOfNeighs = numberOfNeighs;
        this.numberOfRecs = numberOfRecs;
        sim = new ItemSimilarityObject(ratingsMap, numberOfNeighs,ifMatrix,ibMatrix,PublicitemsIFMatrix,PrivateitemsIFMatrix);
    }

    public List<String> scoreItems(int user){
        System.out.println(user);
        ArrayList<AbstractMap.SimpleEntry<Integer, Float>> recs = new ArrayList<>();
        Set<Integer> userItems = ratingsMap.get(user).keySet();
        Set<Integer> candidateItems = new HashSet<>(sim.getItemSet());
        candidateItems.removeAll(userItems);
        candidateItems.stream().map(item -> {
            ArrayList<AbstractMap.SimpleEntry<Integer, Float>> itemNeighs = sim.getNeighbors(item);
            HashMap<Integer, Float> neighborsMap = new HashMap<>();
            itemNeighs.stream().forEach(entry -> neighborsMap.put(entry.getKey(), entry.getValue()));
            HashSet<Integer> intersection = new HashSet<>();
            itemNeighs.stream().mapToInt(AbstractMap.SimpleEntry::getKey).forEachOrdered(intersection::add);
            intersection.retainAll(userItems);
            if (intersection.size() > 0) {
                float sum = (float) intersection.stream().mapToDouble(neighborsMap::get).sum();
                float den = (float) itemNeighs.stream().mapToDouble(AbstractMap.SimpleEntry::getValue).sum();

                return new AbstractMap.SimpleEntry<>(item, ((den != 0) || (sum != 0) ? (sum / den) : 0f));
            }else{
                return null;
            }
        }).filter(Objects::nonNull).forEach(recs::add);
        Collections.sort(recs,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        if (recs.size()<numberOfRecs){
        }else{
            recs = new ArrayList<>(recs.subList(0,numberOfRecs));
        }
        return recs.stream().map(rec -> user+"\t"+rec.getKey()+"\t"+rec.getValue()).collect(Collectors.toList());
    }
}
