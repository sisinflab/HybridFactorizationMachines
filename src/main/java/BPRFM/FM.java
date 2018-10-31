package BPRFM;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FM {

    /** Data Model           */ private static FMDataModelArray dataModel;
    /** Learning Rate        */ private static float learningRate = 0.05F;
    /** Bias regularization  */ private static float biasRegularization = 0.0F;
    /** User regularization  */ private static float userRegularization = 0.0025F;
    /** Positive Item Reg    */ private static float positiveItemRegularization = 0.0025F;
    /** Negative Item Reg    */ private static float negativeItemRegularization = 0.00025F;
    /** Update Neg Item Fac  */ private static boolean updateNegativeItemsFactors = true;
    /** Number of Factors    */ private static int D = 10;
    /** Update Users Fac     */ private static boolean updateUsers = true;
    /** Update Items Fac     */ private static boolean updateItems = true;
    /** Number of iterations */ private static int numIters = 30;
    /** Type of sampling     */ private static String samplerName = "withoutReplacement";
    /** Initial val of Mean  */ private static float initMean = 0;
    /** Initial val of St.Dev*/ private static float initStdDev = 0.1F;
    /** Debug Mode           */ private static int debug = 1;
    private static BPRFM2d model;

    public static void useFM(HashMap<Integer, HashMap<Integer, Double>> ratingsMap,
                             HashMap<Integer, ArrayList<Integer>> featuresMap,
                             Map<Integer, HashMap<Integer, Float>> tfidf,
                            HashMap<Integer, HashMap<Integer,Double>> profiles,
                            int iterations,
                            int numberOfFeaturesToRemove,
                             HashMap<Integer, ArrayList<Integer>> completionMap,
                            String[] args) throws IOException {

        numIters = iterations;
        String featuresPath = args[0];

        dataModel = new FMDataModelArray(ratingsMap,featuresMap,tfidf,profiles,initMean,initStdDev,numberOfFeaturesToRemove,completionMap);
        model = new BPRFM2d(D, learningRate, biasRegularization, userRegularization, positiveItemRegularization, negativeItemRegularization, updateNegativeItemsFactors, updateUsers, updateItems);

        model.setFeaturesName(featuresPath);

        boolean sampleNegativeItemsEmpirically = true;
        Sampler sampler = new Sampler(sampleNegativeItemsEmpirically, dataModel.getRandom(),ratingsMap);

        model.Train(dataModel, sampler, numIters, initMean, initStdDev, samplerName);
        dataModel.computeFinalValues();
    }
    public static double[][] getItemsFactors(){
        return dataModel.getIfMatrix();
    }
    public static double[] getItemsBiases(){
        return dataModel.getIbMatrix();
    }
    public static Map<Integer, Integer> getPublicItems(){
        return dataModel.getPublicitems();
    }
    public static Map<Integer, Integer> getPrivateItems(){
        return dataModel.getPrivateitems();
    }
    public static HashMap<Integer, ArrayList<Integer>> generateTopCompletionMap(){
        return dataModel.generateTopCompletionMap();
    }

    @SuppressWarnings("Duplicates")
    public static void printModifications(String fileIn,String fileOut, AbstractMap.SimpleEntry<HashMap<Integer, ArrayList<String>>,HashMap<Integer, ArrayList<String>>> pair) throws IOException {
        HashMap<Integer, String> mapping = new HashMap<>();
        HashMap<Integer, ArrayList<String>> remMap = pair.getKey();
        HashMap<Integer, ArrayList<String>> addMap = pair.getValue();

        BufferedReader br = null;
		String cvsSplitBy = "\t";
		String line = "";
		String[] pattern;

		try {
			br = new BufferedReader(new FileReader(fileIn));
			while ((line = br.readLine()) != null) {
				pattern = line.split(cvsSplitBy);
				int itemID = Integer.parseInt(pattern[0]);
				String name = pattern[1];
                mapping.put(itemID,name);
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

        PrintWriter out= new PrintWriter(new FileWriter(fileOut));

        for (Integer i : remMap.keySet()) {
            String name = mapping.get(i);
            out.println("*****************************");
            out.println("**** Item: "+name);
            out.println("****Added Triples **********");
            ArrayList<String> addedFeatures = addMap.get(i);
            addedFeatures.stream().forEach(f-> {
                out.println("<"+name+">"+f);
            });
            out.println("****Removed Triples ********");
            ArrayList<String> removedFeatures = remMap.get(i);
            removedFeatures.stream().forEach(f-> {
                out.println("<"+name+">"+f);
            });
        }
        out.close();
    }

}
