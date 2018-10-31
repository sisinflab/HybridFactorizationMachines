package ItemSimilarityManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class MainItemSimilarity {

    /** Rating File          */ private static String filePath = "";
    /** Attribute File       */ private static String fileAttrPath = "";
    /** Recommendation File  */ private static String outPath  = "";

    public static void useItemSimilarity(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, double[][] ifMatrix,double[] ibMatrix,Map<Integer, Integer> PublicitemsIFMatrix,Map<Integer, Integer> PrivateitemsIFMatrix, String[] args, int numberOfNeighs, int numberOfRecs) throws IOException
    {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)filePath = args[i];
            if (i==1)fileAttrPath = args[i];
            if (i==2)outPath = args[i];
        }

        ItemSimilarityModel model = new ItemSimilarityModel(ratingsMap,numberOfNeighs,numberOfRecs,ifMatrix,ibMatrix,PublicitemsIFMatrix,PrivateitemsIFMatrix);

        HashSet<List<String>> results = new HashSet<>();
        ratingsMap.keySet().parallelStream().map(user -> model.scoreItems(user)).forEach(results::add);

        PrintWriter recs= new PrintWriter(new FileWriter(outPath));

        for (List<String> s : results) {
            if (s!=null){
                for (int i = 0; i < s.size(); i++) {
                    if (s.get(i)!=null){
                        recs.println(s.get(i));
                    }
                }
            }
        }
        recs.close();
    }
}


