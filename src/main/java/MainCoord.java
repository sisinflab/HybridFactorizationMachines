import BPRFM.FM;
import ContentInitializer.App;
import ItemSimilarityManager.MainItemSimilarity;
import ItemsAttributesManager.ItemsAttributesManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainCoord {

    private static String filePath;
    private static String outPath;
    private static String fileAttrPath;
    private static String featuresNames;
    private static int nIterations = 30;
    private static int numberOfFeaturesToRemove = 0;
    private static int threshold = 10;
    private static boolean additive = true;
    private static int numOfNeighs = 80;
    private static int numberOfRecs = 100;

    private static String propertiesPath = "properties.conf";


    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)filePath = args[i];
            if (i==1)fileAttrPath = args[i];
            if (i==2)outPath = args[i];
            if (i==3)featuresNames = args[i];
            if (i==4)nIterations = Integer.parseInt(args[i]);
            if (i==5)numberOfFeaturesToRemove = Integer.parseInt(args[i]);
            if (i==6)additive = Boolean.parseBoolean(args[i]);
        }
        System.out.println("Content");
        HashMap<Integer, ArrayList<Integer>> map = ItemsAttributesManager.loadMap(filePath,fileAttrPath,featuresNames,threshold,propertiesPath,additive);
        HashMap<Integer, HashMap<Integer, Double>> ratingsMap = ItemsAttributesManager.loadRatings(filePath,map);
        HashMap<Integer,HashMap<Integer,Double>> profiles = App.useContent(ratingsMap,map,new String[] {filePath,fileAttrPath});
        Map<Integer, HashMap<Integer, Float>> tfidf = App.getTfidf();
        System.out.println("BPRFM");
        FM.useFM(ratingsMap,map,tfidf,profiles,nIterations,numberOfFeaturesToRemove,null,new String[] {featuresNames});
//        HashMap<Integer, ArrayList<Integer>> completionMap = FM.generateTopCompletionMap();
//        FM.useFM(ratingsMap,map,tfidf,profiles,nIterations,numberOfFeaturesToRemove,completionMap,new String[] {filePath,fileAttrPath,outPath,featuresNames});
//        FM.printModifications(mappingPath,modificationsPath,FM.getMaps());
        System.out.println("ItemSimilarityManager");
        MainItemSimilarity.useItemSimilarity(ratingsMap,FM.getItemsFactors(),FM.getItemsBiases(),FM.getPublicItems(),FM.getPrivateItems(),new String[] {filePath,fileAttrPath,outPath}, numOfNeighs, numberOfRecs);
    }



}
