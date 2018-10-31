package BPRFM;

import ItemsAttributesManager.ItemsAttributesManager;
import java.util.*;


public class BPRFM2d {


    private Random random;
    FMDataModelArray dataModel;
    private float learningRate;
    private float biasRegularization;
    private float userRegularization;
    private float positiveItemRegularization;
    private float negativeItemRegularization;
    private boolean updateNegativeItemsFactors;
    private int D = 10;
    private boolean updateUsers;
    private boolean updateItems;

    private int numUsers;
    private int numItems;

    private Sampler sampler;

    private ArrayList<Triple> lossSamples;
    private HashMap<Integer, String> featuresNames = new HashMap<>();

    BPRFM2d(int D, float learningRate, float biasRegularization, float userRegularization, float positiveItemRegularization, float negativeItemRegularization, boolean updateNegativeItemsFactors, boolean updateUsers, boolean updateItems){
        this.D = D;
        this.learningRate = learningRate;
        this.biasRegularization = biasRegularization;
        this.userRegularization = userRegularization;
        this.positiveItemRegularization = positiveItemRegularization;
        this.negativeItemRegularization = negativeItemRegularization;
        this.updateNegativeItemsFactors = updateNegativeItemsFactors;
        this.updateUsers = updateUsers;
        this.updateItems = updateItems;
    }

    void setFeaturesName(String featuresPath){
        featuresNames = ItemsAttributesManager.loadFeaturesNames(featuresPath);
    }
    public void printItemTopFeatures(int item,int topK){
        dataModel.getItemTopFeats(item,topK).stream().forEachOrdered(e -> System.out.println(e.getValue()+"\t"+featuresNames.get(e.getKey())));
    }
    public Integer printItemOriginalFeatures(int item){
        dataModel.getItemFeatures(item).stream().forEachOrdered(i -> System.out.println(featuresNames.get(i)));
        return dataModel.getItemFeatures(item).size();
    }
    public double precisionFeatures(int item, int multiplier){
        ArrayList<Integer> original = dataModel.getItemFeatures(item);
        ArrayList<Integer> computed = new ArrayList<>();
        dataModel.getItemTopFeats(item,original.size()*multiplier).stream().forEach(e -> computed.add(e.getKey()));
        double sum = computed.stream().mapToDouble(i -> original.contains(i)?1:0).sum();
        return sum/(double)original.size();
    }
    @SuppressWarnings("Duplicates")
    public double precisionCompletionFeatures(int item,int multiplier){
        ArrayList<Integer> original = dataModel.getItemFeatures(item);
        ArrayList<Integer> completion = dataModel.getCompletionItemFeatures(item);
        ArrayList<Integer> computed = new ArrayList<>();
        dataModel.getItemTopFeats(item,original.size()*multiplier).stream().forEach(e -> computed.add(e.getKey()));
        double sum = computed.stream().mapToDouble(i -> completion.contains(i)?1:0).sum();
        return sum/(double)completion.size();
    }


    public void printUserTopFeatures(int user,int topK){
        dataModel.getUserTopFeats(user,topK).stream().forEachOrdered(e -> System.out.println(e.getValue()+"\t"+featuresNames.get(e.getKey())));
    }
    public void printUserTotalOrder(int user,int topK){
        dataModel.getUserApproxTotalOrder(user,topK).stream().forEachOrdered(e -> System.out.println(e.getValue()+"\t"+featuresNames.get(e.getKey())));
    }
    public ArrayList<AbstractMap.SimpleEntry<String,String>> getUserPreferences(int user,int item1,int item2){
        ArrayList<AbstractMap.SimpleEntry<String,String>> userPreferences = new ArrayList<>();
        dataModel.getUserPreferences(user,item1,item2).stream().forEachOrdered(e -> userPreferences.add(new AbstractMap.SimpleEntry<>(featuresNames.get(e.getKey()),featuresNames.get(e.getValue()))));
        return userPreferences;
    }
    public String getUserPreferencesW(int user,int item1,int item2){

        int val = dataModel.getUserPreferencesW(user,item1,item2);
        if(val==-1)return "bias";
        if(val == -2 )return "no bias";
        return featuresNames.get(val);
    }


    // gaussian version
    private void initializeData(FMDataModelArray dataModel, Sampler samp, float initMean, float initStdDev){
        this.dataModel = dataModel;
        this.random = this.dataModel.getRandom();
        sampler = samp;

        numUsers = dataModel.getnUsers();
        numItems = dataModel.getnItems();
    }


    private void createLossSamples(int numLossSamples){
        System.out.println("sampling " + numLossSamples + " <user,item i,item j> triples...");

        Map<Integer, ArrayList<Integer>> tempMap = sampler.sampleTripleMymedialite(numLossSamples);
        lossSamples = new ArrayList<>();
        ArrayList<Integer> listU = tempMap.get(1);
        ArrayList<Integer> listI = tempMap.get(2);
        ArrayList<Integer> listJ = tempMap.get(3);
        for (int i = 0; i < listU.size(); i++){
            Triple<Integer,Integer,Integer> triple = new Triple<>(listU.get(i),listI.get(i), listJ.get(i));
            lossSamples.add(triple);
        }
    }

    void Train(FMDataModelArray dataModel, Sampler samp, int numIters, float initMean, float initStdDev, String samplerName){
        initializeData(dataModel, samp, initMean, initStdDev);

        // apply rule of thumb to decide num samples over which to compute loss
        int numLossSamples  = (int)(Math.sqrt(numUsers) * 100);
        createLossSamples(numLossSamples);
        int numPosEvents = dataModel.getNumRatings();

        System.out.println("initial loss " + loss());


        for (int it = 0; it < numIters; it++) {
            System.out.println("starting iteration " + it);
            System.out.println("******************************");
            Map<Integer, ArrayList<Integer>> tempMap = sampler.Sample(samplerName, numPosEvents);
            ArrayList<Integer> listU = tempMap.get(1);
            ArrayList<Integer> listI = tempMap.get(2);
            ArrayList<Integer> listJ = tempMap.get(3);

            for (int i = 0; i < listU.size(); i++){
                updateFactors(listU.get(i),listI.get(i), listJ.get(i), updateUsers, updateItems, it);
            }
            System.out.println("iteration " + it + ": loss = " + loss());

//            double precisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionFeatures(i,1)).sum();
//            System.out.println("Overall Precision: "+precisionSum/(double)dataModel.Publicitems.size());
//            precisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionFeatures(i,2)).sum();
//            System.out.println("Overall Precision: "+precisionSum/(double)dataModel.Publicitems.size());
//            precisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionFeatures(i,3)).sum();
//            System.out.println("Overall Precision: "+precisionSum/(double)dataModel.Publicitems.size());
//            precisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionFeatures(i,4)).sum();
//            System.out.println("Overall Precision: "+precisionSum/(double)dataModel.Publicitems.size());
//            precisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionFeatures(i,5)).sum();
//            System.out.println("Overall Precision: "+precisionSum/(double)dataModel.Publicitems.size());
//            double completionPrecisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionCompletionFeatures(i,1)).sum();
//            System.out.println("Overall Completion Precision: "+completionPrecisionSum/(double)dataModel.Publicitems.size());
//            completionPrecisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionCompletionFeatures(i,2)).sum();
//            System.out.println("Overall Completion Precision: "+completionPrecisionSum/(double)dataModel.Publicitems.size());
//            completionPrecisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionCompletionFeatures(i,3)).sum();
//            System.out.println("Overall Completion Precision: "+completionPrecisionSum/(double)dataModel.Publicitems.size());
//            completionPrecisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionCompletionFeatures(i,4)).sum();
//            System.out.println("Overall Completion Precision: "+completionPrecisionSum/(double)dataModel.Publicitems.size());
//            completionPrecisionSum = dataModel.Publicitems.keySet().stream().mapToDouble(i -> precisionCompletionFeatures(i,5)).sum();
//            System.out.println("Overall Completion Precision: "+completionPrecisionSum/(double)dataModel.Publicitems.size());
        }

    }


    @SuppressWarnings("Duplicates")
    private void updateFactors(int u, int i, int j, boolean update_u, boolean update_i, int it){
        boolean update_j = updateNegativeItemsFactors;

        if(it < 100) {
            double[] userFactors = dataModel.getUserFactors(u);
            double[] itemFactors_i = dataModel.getItemFactors(i);
            double[] itemFactors_j = dataModel.getItemFactors(j);
            double itemWeight_i = dataModel.getItemBias(i);
            double itemWeight_j = dataModel.getItemBias(j);
            double x = dataModel.predict(u,i)-dataModel.predict(u,j);


            double z =  (1.0/(1.0+Math.exp(x)));
            // update bias terms
            if (update_i) {
                double d = z - biasRegularization * itemWeight_i;
                dataModel.update_i_w(i,itemWeight_i + learningRate * d);
            }
            if (update_j){
                double d = -z - biasRegularization * itemWeight_j;
                dataModel.update_i_w(j,itemWeight_j + learningRate * d);
            }

            for (int f = 0; f < userFactors.length; f++) {
                double w_uf = userFactors[f];
                double h_if = itemFactors_i[f];
                double h_jf = itemFactors_j[f];

                if (update_u) {
                    double update = (h_if - h_jf) * z - userRegularization * w_uf;

                    userFactors[f] =  (w_uf + learningRate * update);
                }

                if (update_i) {
                    double update = w_uf * z - positiveItemRegularization * h_if;
                    itemFactors_i[f] =  (h_if + learningRate * update);
                }

                if (update_j) {
                    double update = -w_uf * z - negativeItemRegularization * h_jf;
                    itemFactors_j[f] =  (h_jf + learningRate * update);
                }
            }
            dataModel.update_if(i,itemFactors_i);
            dataModel.update_if(j,itemFactors_j);
            dataModel.update_uf(u,userFactors);
        }else {
            double[] userFactors = dataModel.getUserFactors(u);
            double[] itemFactors_i = dataModel.getItemFactors(i);
            double[] itemFactors_j = dataModel.getItemFactors(j);
            double itemWeight_i = dataModel.getItemBias(i);
            double itemWeight_j = dataModel.getItemBias(j);
            double x = dataModel.predict(u,i)-dataModel.predict(u,j);

            double z =  (1.0/(1.0+Math.exp(x)));
            // update bias terms
            if (update_i) {
                double d = z - biasRegularization * itemWeight_i;
                dataModel.update_i_w(i,itemWeight_i + learningRate * d);
            }
            if (update_j){
                double d = -z - biasRegularization * itemWeight_j;
                dataModel.update_i_w(j,itemWeight_j + learningRate * d);
            }


            for (int f = 0; f < userFactors.length; f++) {
                double w_uf = userFactors[f];
                double h_if = itemFactors_i[f];
                double h_jf = itemFactors_j[f];

                if (update_u) {
                    double update = (h_if - h_jf) * z - userRegularization * w_uf;
                    userFactors[f] =  (w_uf + learningRate * update);
                }

                if (update_i) {
                    double update = w_uf * z - positiveItemRegularization * h_if;
                    itemFactors_i[f] =  (h_if + learningRate * update);
                }

                if (update_j) {
                    double update = -w_uf * z - negativeItemRegularization * h_jf;
                    itemFactors_j[f] =  (h_jf + learningRate * update);
                }
            }
            dataModel.update_uf(u,userFactors);
            dataModel.update_if(i,itemFactors_i);
            dataModel.update_if(j,itemFactors_j);
        }
    }


    private double loss(){  // TODO check con mymedialite ComputeObjective. non necessaria per la computazione ma solo per visualizzazione
        double rankingLoss = 0;
        for(Triple<Integer,Integer,Integer> uij:lossSamples){
            int u = uij.getFirst();
            int i = uij.getSecond();
            int j = uij.getThird();
            double x = dataModel.predict(u,i)-dataModel.predict(u,j);
            rankingLoss += 1.0/(1.0+Math.exp(x));
        }
        int complexity = 0;
        for(Triple<Integer,Integer,Integer> uij:lossSamples) {
            int u = uij.getFirst();
            int i = uij.getSecond();
            int j = uij.getThird();
            double[] userFactors = dataModel.getUserFactors(u);
            double[] itemFactors_i = dataModel.getItemFactors(i);
            double[] itemFactors_j = dataModel.getItemFactors(j);
            double itemBias_i = dataModel.getItemBias(i);
            double itemBias_j = dataModel.getItemBias(j);
            complexity += userRegularization* dotProduct(userFactors,userFactors); // TODO check euclidean norm vedi mymedialite
            complexity += positiveItemRegularization * dotProduct(itemFactors_i,itemFactors_i);
            complexity += negativeItemRegularization * dotProduct(itemFactors_j,itemFactors_j);
            complexity += biasRegularization * Math.pow(itemBias_i,2);
            complexity += biasRegularization * Math.pow(itemBias_j,2);
        }
        double loss = (rankingLoss + 0.5*complexity);
        return loss;
    }



    private double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }
}
