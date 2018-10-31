package ItemsAttributesManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ItemsAttributesManager {
    private static HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
    private static HashMap<Integer, String> featuresNames = new HashMap<>();
    public static HashMap<Integer, ArrayList<Integer>> loadMap(String fileRatings, String fileAttrPath,String fNames,int threshold, String propertiesPath,boolean additive) throws IOException
    {
        map = Utils.Companion.loadAttributeFile(fileAttrPath,"\t",0);
        HashSet<Integer> items = loadItemsSet(fileRatings,"\t",1);
        featuresNames = loadFeaturesNames(fNames);
        ArrayList<String> properties = loadProperties(propertiesPath);
        map = reduceAttributesMapPropertySelection(map,items,properties,additive,threshold);
        return map;
    }
    private static HashSet<Integer> loadItemsSet(String fileIn, String separator, int itemPosition) throws IOException {
        HashSet<Integer> items = new HashSet<>();
        String line;
        String[] pattern;
        BufferedReader br = new BufferedReader(new FileReader(fileIn));
        while ((line = br.readLine()) != null) {
            pattern = line.split(separator);

            Integer item = Integer.valueOf(pattern[itemPosition]);
            items.add(item);
        }
        return items;
    }

    @SuppressWarnings("Duplicates")
    public static HashMap<Integer, String> loadFeaturesNames(String fileIn) {
        HashMap<Integer, String> featuresNames = new HashMap<>();
        BufferedReader br = null;
        String cvsSplitBy = "\t";
        String line;
        String[] pattern;

        try {
            br = new BufferedReader(new FileReader(fileIn));
            while ((line = br.readLine()) != null) {
                pattern = line.split(cvsSplitBy);

                int featureId = Integer.parseInt(pattern[0]);
                String featureName = pattern[1];
                featuresNames.put(featureId, featureName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return featuresNames;
    }

    @SuppressWarnings("Duplicates")
    private static HashMap<Integer, ArrayList<Integer>> reduceAttributesMapPropertySelection(HashMap<Integer, ArrayList<Integer>> map,HashSet<Integer> items,ArrayList<String> properties,boolean additive, int threshold) throws IOException {
        HashMap<Integer, ArrayList<Integer>> newMap = new HashMap<>();
        HashMap<Integer,Integer> featuresPopularity = new HashMap<>();
        HashSet<Integer> acceptableFeatures;
        if(properties.isEmpty()){
            acceptableFeatures = featuresNames.entrySet().stream().map(Map.Entry::getKey).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        }else{
            acceptableFeatures = featuresNames.entrySet().stream().map(e -> {
                if(additive){
                    for(String s: properties){
                        if(e.getValue().contains(s)){
                            return e.getKey();
                        }
                    }
                    return null;
                }else{
                    for(String s: properties){
                        if(e.getValue().contains(s)){
                            return null;
                        }
                    }
                    return e.getKey();
                }
            }
            ).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        }
        System.out.println("acceptableFeatures: "+acceptableFeatures.size());
        System.out.println("Movielens 20M mapped items: "+map.size());
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
        {
            if (items.contains(entry.getKey())){
                ArrayList<Integer> itemFeatures = entry.getValue();
                Integer item = entry.getKey();
                if (itemFeatures==null){
                    System.out.println("Ho tentato di recuperare le feature di un movie ("+item+") non presente ");
                }else{
                    HashSet<Integer> bag = new HashSet<Integer>();
                    for (int i = 0; i < itemFeatures.size(); i++) {
                        Integer featureNumber = itemFeatures.get(i);
                        if ((!bag.contains(featureNumber))&&acceptableFeatures.contains(featureNumber)){
                            Integer numberOfItemsThatContainFeature = featuresPopularity.get(featureNumber);
                            if (numberOfItemsThatContainFeature==null){
                                numberOfItemsThatContainFeature = 1;
                            } else {
                                numberOfItemsThatContainFeature = numberOfItemsThatContainFeature+1;
                            }
                            featuresPopularity.put(featureNumber, numberOfItemsThatContainFeature);
                            bag.add(featureNumber);
                        }
                    }
                }
            }
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
        {
            if (items.contains(entry.getKey())){
                ArrayList<Integer> newItemFeatures = new ArrayList<>();
                for (int feature : entry.getValue()){
                    if(acceptableFeatures.contains(feature)){
                        if(featuresPopularity.get(feature)>threshold){
                            newItemFeatures.add(feature);
                        }
                    }
                }
                if(newItemFeatures.size()>0)newMap.put(entry.getKey(),newItemFeatures);
            }
        }
        System.out.println("Mapped items in Training set: "+newMap.size());
        return newMap;
    }
    private static ArrayList<String> loadProperties(String fileIn) {
        ArrayList<String> propertiesNames = new ArrayList<>();
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(fileIn));
            while ((line = br.readLine()) != null) {
                if(!line.startsWith("#"))propertiesNames.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return propertiesNames;
    }
    public static HashMap<Integer, HashMap<Integer, Double>> loadRatings(String fileIn, HashMap<Integer, ArrayList<Integer>> map){
        HashMap<Integer, HashMap<Integer, Double>> newRatingsMap = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Double>> oldRatings = Utils.Companion.loadRatingsFile(fileIn,"\t",0,1,2);
        oldRatings.forEach((key1, value1) -> {
            HashMap<Integer, Double> userVotes = new HashMap<>();
            value1.forEach((key, value) -> {
                if (map.containsKey(key)) userVotes.put(key, value);
            });
            if (!userVotes.isEmpty()) {
                newRatingsMap.put(key1, userVotes);
            } else {
                System.out.println("WARNING - Deleted user: " + key1);
            }
        });
        return newRatingsMap;
    }
}
