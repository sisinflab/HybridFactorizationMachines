package BPRFM;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class FMDataModelArray implements DataModel {
    /** random               */ private Random random;
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap;
    /** Features Map         */ private HashMap<Integer, ArrayList<Integer>> featuresMap;
    /** Removed Map          */ private HashMap<Integer, ArrayList<Integer>> completionMap;
    /** TF-IDF               */ private static Map<Integer, HashMap<Integer, Float>> tfidf = new HashMap<>();
    /** Profiles Map         */ private HashMap<Integer, HashMap<Integer, Double>> profilesMap;
    /** Items Bias Matrix    */ private double globalBias;
    /** Features weights     */ private double[] wMatrix;
    /** Fact Interact Matrix */ private double[][] fiMatrix;
    /** Users Map            */ private Map<Integer, Integer> Publicusers = new HashMap<>();
    /** Users Map            */ private Map<Integer, Integer> Privateusers = new HashMap<>();
    /** Items Map            */ public  Map<Integer, Integer> Publicitems = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Privateitems = new HashMap<>();
    /** Features Map         */ private Map<String, Integer> PublicAllFeatures = new HashMap<>();
    /** Features Map         */ private Map<Integer, String> PrivateAllFeatures = new HashMap<>();
    /** Features Map         */ private Map<Integer, Integer> Publicfeatures = new HashMap<>();
    /** Features Map         */ private Map<Integer, Integer> Privatefeatures = new HashMap<>();
    /** Number of features   */ private int numberOfFeatures;
    /** Number of factors    */ private int numberOfFactors;
    /** Number of Users      */ private int nUsers;
    /** Number of Items      */ private int nItems;
    /** Number of Ratings    */ private int numRatings;
    /** Initial Mean         */ private float initMean;
    /** Initial Standard Dev */ private float initStdDev;
    /** Items Set            */ private Set<Integer> itemsSet = new HashSet<>();
    /** Users Set            */ private Set<Integer> usersSet;
    /** Added Latent Factors */ private int addLatentFactors = 0;


    public FMDataModelArray(HashMap<Integer, HashMap<Integer, Double>> ratingsMap,
                            HashMap<Integer, ArrayList<Integer>> map,
                            Map<Integer, HashMap<Integer, Float>> tfidf,
                            HashMap<Integer, HashMap<Integer, Double>> profiles,
                            float initMean,
                            float initStdDev,
                            int numberOfFeaturesToRemove,
                            HashMap<Integer, ArrayList<Integer>> completionMap) {
        this.random = new Random(42);
        this.ratingsMap = ratingsMap;
        this.featuresMap = map;

        this.tfidf = tfidf;
        this.profilesMap = profiles;
        this.initMean = initMean;
        this.initStdDev =initStdDev;
        this.numRatings = ratingsMap.values().stream().mapToInt(HashMap::size).sum();
        int i = 0;
        int k = 0;
        for (int user : ratingsMap.keySet()){
            for (int item : ratingsMap.get(user).keySet()){
                if ((!Publicitems.containsKey(item))&&featuresMap.containsKey(item)){
                    Publicitems.put(item,k);
                    Privateitems.put(k,item);
                    k++;
                }
            }
            Publicusers.put(user,i);
            Privateusers.put(i,user);
            i++;
        }
        int z = 0;
        for (int user : Publicusers.keySet()){
            if (!PublicAllFeatures.containsKey("u_"+user)){
                PublicAllFeatures.put("u_"+user,z);
                PrivateAllFeatures.put(z,"u_"+user);
                z++;
            }
        }
        for (int item : Publicitems.keySet()){
            if (!PublicAllFeatures.containsKey("i_"+item)){
                PublicAllFeatures.put("i_"+item,z);
                PrivateAllFeatures.put(z,"i_"+item);
                z++;
            }
        }
        this.numberOfFeatures =  PrivateAllFeatures.size();

        nUsers = Publicusers.size();
        nItems = Publicitems.size();
        ratingsMap.entrySet().stream().map(entry -> entry.getValue().keySet()).forEachOrdered(itemsSet::addAll);
        usersSet = new HashSet<>(ratingsMap.keySet());


        int j = 0;
        for (Map.Entry entry : map.entrySet()){
            for (Integer feature : (ArrayList<Integer>)entry.getValue()){
                if (!Publicfeatures.containsKey(feature)){
                    Publicfeatures.put(feature,j);
                    Privatefeatures.put(j,feature);
                    j++;
                }
            }
        }
        int maxVal = Collections.max(Publicfeatures.keySet());
        System.out.println();
        for(int az = maxVal+1;az< maxVal+addLatentFactors+1;az++){
            if (!Publicfeatures.containsKey(az)){
                Publicfeatures.put(az,j);
                Privatefeatures.put(j,az);
                j++;
            }
        }
        this.numberOfFactors = Publicfeatures.size();
        System.out.println("Number of Factors: "+ numberOfFactors);

        if(completionMap!=null){
            this.completionMap = completionMap;
        }else{
            this.completionMap = generateCompletionMap(featuresMap,numberOfFeaturesToRemove);
        }

        initializeData();
    }
    private void initializeData(){
        System.out.println(nUsers+"  "+nItems);
        globalBias = 0;
        System.out.println(numberOfFactors);
        wMatrix = new double[numberOfFeatures];
        for (int i = 0; i < numberOfFeatures;i++){
            wMatrix[i] = 0;
        }

        fiMatrix = new double[numberOfFeatures][numberOfFactors];
        for (int i = 0; i < numberOfFeatures;i++){
            for(int z = 0; z < numberOfFactors; z++) {
                fiMatrix[i][z] = lookupFactor(i,z);
            }
        }
    }

    private double lookupFactor(int feature, int factor){
        String publicFeature = PrivateAllFeatures.get(feature);
        String[] feat = publicFeature.split("_");
        if (feat[0].equals("u")){
            HashMap<Integer, Double> profileMap = profilesMap.get(Integer.valueOf(feat[1]));
            Double featureVal = profileMap.get(Privatefeatures.get(factor));
            if(factor<numberOfFactors-addLatentFactors){
                return (featureVal!=null)?(featureVal):0;
            }else{
                return 0;
            }
        }else if (feat[0].equals("i")){
            ArrayList<Integer> itemFeatures = featuresMap.get(Integer.valueOf(feat[1]));
            ArrayList<Integer> completionFeatures = completionMap.get(Integer.valueOf(feat[1]));
            HashMap<Integer, Float> tfidfOfItem = tfidf.get(Integer.valueOf(feat[1]));
            if(itemFeatures!=null){
                if(factor<numberOfFactors-addLatentFactors) {
                    return (itemFeatures.contains(Privatefeatures.get(factor))) ? (completionFeatures.contains(Privatefeatures.get(factor))? 0 : tfidfOfItem.get(Privatefeatures.get(factor)) ) : 0;
                }else{
                    return random.nextGaussian() * initStdDev + initMean;
                }
            }else{
                return 0;
            }
        }else{
            return random.nextGaussian() * initStdDev + initMean;
        }
    }
    public ArrayList<Integer> getItemFeatures(int item){
        return featuresMap.get(item);
    }
    public ArrayList<Integer> getCompletionItemFeatures(int item){
        return completionMap.get(item);
    }


    public double[] getUserMean(int u){
        HashMap<Integer, Double> userVotes = ratingsMap.get(u);
        double[] userMean = new double[numberOfFactors];
        Arrays.fill(userMean,0);
        userVotes.keySet().stream().forEach(k -> {
            double[] temp = fiMatrix[PublicAllFeatures.get("i_"+k)];
            Arrays.setAll(userMean, i -> userMean[i] + temp[i]);
        });
        double m = userVotes.size();
        Arrays.setAll(userMean, i -> userMean[i]/m);
        return userMean;
    }
    @Override
    public void update_uf(int u, double[] fs){
        fiMatrix[PublicAllFeatures.get("u_"+u)]=fs;
    }
    @Override
    public void update_if(int i, double[] fs){
        fiMatrix[PublicAllFeatures.get("i_"+i)]=fs;
    }
    @Override
    public void update_u_w(int i, double uw){
        wMatrix[Publicitems.get(i)]=uw;
    }
    @Override
    public void update_i_w(int i, double iw){
        wMatrix[Publicitems.get(i)]=iw;
    }

    @Override
    public double predict(int u, int i){
        double u_w = wMatrix[Publicusers.get(u)];
        double i_w = wMatrix[Publicitems.get(i)];
        double[] u_f = fiMatrix[PublicAllFeatures.get("u_"+u)];
        double[] i_f = fiMatrix[PublicAllFeatures.get("i_"+i)];

        return globalBias + u_w + i_w + dotProduct(u_f,i_f);
    }
    @Override
    public double[] getUserFactors(int u){
        return fiMatrix[PublicAllFeatures.get("u_"+u)];
    }
    @Override
    public double[] getItemFactors(int i){
        return fiMatrix[PublicAllFeatures.get("i_"+i)];
    }
    @Override
    public double getUserBias(int i){
        return wMatrix[Publicitems.get(i)];
    }
    @Override
    public double getItemBias(int i){
        return wMatrix[Publicitems.get(i)];
    }
    @Override
    public Random getRandom() {
        return random;
    }
    @Override
    public int getnUsers() {
        return nUsers;
    }
    @Override
    public int getnItems() {
        return nItems;
    }
    @Override
    public Set<Integer> getItemsSet() {
        return itemsSet;
    }
    @Override
    public int getNumRatings() {
        return numRatings;
    }
    @Override
    public Set<Integer> getUsersSet() {
        return usersSet;
    }
    private double dotProduct(double[] x, double[] y) {return IntStream.range(0,x.length).mapToDouble(i->x[i]*y[i]).sum();}
    @SuppressWarnings("Duplicates")
    public ArrayList<AbstractMap.SimpleEntry<Integer,Double>> getItemTopFeats(int item,int k){
        ArrayList<AbstractMap.SimpleEntry<Integer,Double>> featuresList = new ArrayList<>();
        for (int i =0;i<Publicfeatures.size();i++){
            featuresList.add(new AbstractMap.SimpleEntry<>(Privatefeatures.get(i),fiMatrix[PublicAllFeatures.get("i_"+item)][i]));
        }
        Collections.sort(featuresList,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        if (featuresList.size()<k){
        }else{
            featuresList = new ArrayList<>(featuresList.subList(0,k));
        }
        return featuresList;
    }
    @SuppressWarnings("Duplicates")
    public ArrayList<AbstractMap.SimpleEntry<Integer,Double>> getUserTopFeats(int user,int k){
        ArrayList<AbstractMap.SimpleEntry<Integer,Double>> featuresList = new ArrayList<>();
        for (int i =0;i<Publicfeatures.size();i++){
            featuresList.add(new AbstractMap.SimpleEntry<>(Privatefeatures.get(i),fiMatrix[PublicAllFeatures.get("u_"+user)][i]));
        }
        Collections.sort(featuresList,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        if (featuresList.size()<k){
        }else{
            featuresList = new ArrayList<>(featuresList.subList(0,k));
        }
        return featuresList;
    }
    @SuppressWarnings("Duplicates")
    public ArrayList<AbstractMap.SimpleEntry<Integer,Double>> getUserApproxTotalOrder(int user,int k){
        ArrayList<AbstractMap.SimpleEntry<Integer,Double>> featuresList = new ArrayList<>();
        for (int i =0;i<Publicfeatures.size();i++){
            int finalI = i;
            double meanFeature = ratingsMap.get(Publicusers.get(user)).keySet().stream().map(item -> (fiMatrix[PublicAllFeatures.get("i_"+item)][finalI]>0)?1:null).filter(Objects::nonNull).mapToDouble(it ->it).average().orElse(0);
            featuresList.add(new AbstractMap.SimpleEntry<>(Privatefeatures.get(i),fiMatrix[PublicAllFeatures.get("u_"+user)][i]*meanFeature));
        }
        Collections.sort(featuresList,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        if (featuresList.size()<k){
        }else{
            featuresList = new ArrayList<>(featuresList.subList(0,k));
        }
        return featuresList;
    }
    @SuppressWarnings("Duplicates")
    public Integer getUserPreferencesW(int user,int item1,int item2){
        double score1 = predict(user,item1);
        double score2 = predict(user,item2);
        double[] uFactors = getUserFactors(user);
        double[] i1Factors = getItemFactors(item1);
        double[] i2Factors = getItemFactors(item2);
        double bi1 = getItemBias(item1);
        double bi2 = getItemBias(item2);
        double[] UDotI1 = new double[Publicfeatures.size()];
        ArrayList<AbstractMap.SimpleEntry<Integer,Double>> UDotI1arraylist = new ArrayList<>();
        IntStream.range(0,Publicfeatures.size()).forEach(i -> UDotI1arraylist.add(new AbstractMap.SimpleEntry<>(i,uFactors[i]*i1Factors[i])));
        Collections.sort(UDotI1arraylist,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        double tempItem1 = score1;
        double tempItem2 = score2;
        tempItem1 -= bi1;
        tempItem2 -= bi2;
        if(tempItem1>tempItem2){

            for(int i =0;i<UDotI1arraylist.size();i++){
                tempItem1 -= UDotI1arraylist.get(i).getValue();
                tempItem2 -= uFactors[UDotI1arraylist.get(i).getKey()]*i2Factors[UDotI1arraylist.get(i).getKey()];
                if(tempItem1<=tempItem2)return Privatefeatures.get(UDotI1arraylist.get(i).getKey());
            }
            return -2;
        }else{
            return -1;
        }
    }
    @SuppressWarnings("Duplicates")
    public ArrayList<AbstractMap.SimpleEntry<Integer,Integer>> getUserPreferences(int user,int item1,int item2){
        ArrayList<AbstractMap.SimpleEntry<Integer,Double>> userOrder = getUserApproxTotalOrder(user,10);
        List<Integer> userList = userOrder.stream().map(e ->Publicfeatures.get(e.getKey())).collect(Collectors.toList());
        ArrayList<AbstractMap.SimpleEntry<Integer,Integer>> candidatePreferences = new ArrayList<>();
        double[] uFactors = getUserFactors(user);
        double[] i1Factors = getItemFactors(item1);
        double[] i2Factors = getItemFactors(item2);
        for (int i =0;i<Publicfeatures.size();i++){
            if(userList.contains(i)){
                for (int j = 0;j<Publicfeatures.size();j++){
                    if(i!=j){
                        if(userList.contains(j)){
                            if(uFactors[i]*i1Factors[i]>=uFactors[j]*i1Factors[j]){
                                if(uFactors[i]*i2Factors[i]<=uFactors[j]*i2Factors[j]){
                                    if(uFactors[i]*i1Factors[i]-uFactors[j]*i1Factors[j]>=uFactors[i]*i2Factors[i]-uFactors[j]*i2Factors[j]){
                                        candidatePreferences.add(new AbstractMap.SimpleEntry<>(Privatefeatures.get(i),Privatefeatures.get(j)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return candidatePreferences;
    }
    @SuppressWarnings("Duplicates")
    private Map<Integer, Integer> PublicItemsFinal = new HashMap<>();
    private Map<Integer, Integer> PrivateItemsFinal = new HashMap<>();
    private double[][] ifMatrix;
    private double[] ibMatrix;
    public double[][] computeFinalValues() {
        ifMatrix = new double[Publicitems.size()][numberOfFactors];
        int i =0;
        for (int row = 0; row < fiMatrix.length; row++){
            String publicFeature = PrivateAllFeatures.get(row);
            String[] feat = publicFeature.split("_");
            if (feat[0].equals("i")){
                ifMatrix[i] = fiMatrix[row];
                int item = Integer.valueOf(feat[1]);
                PublicItemsFinal.put(item,i);
                PrivateItemsFinal.put(i,item);
                i++;
            }
        }
        i =0;
        ibMatrix = new double[Publicitems.size()];
        for (int row = 0; row < wMatrix.length; row++){
            String publicFeature = PrivateAllFeatures.get(row);
            String[] feat = publicFeature.split("_");
            if (feat[0].equals("i")){
                ibMatrix[i] = wMatrix[row];
                i++;
            }
        }
        return ifMatrix;
    }
    public double[][] getIfMatrix() {
        return ifMatrix;
    }
    public double[] getIbMatrix() {
        return ibMatrix;
    }

    public Map<Integer, Integer> getPublicitems() {
        return PublicItemsFinal;
    }

    public Map<Integer, Integer> getPrivateitems() {
        return PrivateItemsFinal;
    }

    private HashMap<Integer, ArrayList<Integer>> generateCompletionMap(HashMap<Integer, ArrayList<Integer>> featuresMap, int numberOfRemovals){
        HashMap<Integer, ArrayList<Integer>> completionMap = new HashMap<>();
        Random localRandom = new Random(42);
        featuresMap.entrySet().stream().forEach(e ->{
            ArrayList<Integer> newList = new ArrayList<>();
            for(int i = 0; i < numberOfRemovals; i++){
                int index = localRandom.nextInt(e.getValue().size());
                newList.add(e.getValue().get(index));
            }
            completionMap.put(e.getKey(),newList);
        });
        return completionMap;
    }
    @SuppressWarnings("Duplicates")
    public HashMap<Integer, ArrayList<Integer>> generateTopCompletionMap(){
        HashMap<Integer, ArrayList<Integer>> completionMap = new HashMap<>();
        featuresMap.entrySet().stream().forEach(e ->{
            ArrayList<Integer> newList = new ArrayList<>();
            ArrayList<AbstractMap.SimpleEntry<Integer,Double>> featuresList = new ArrayList<>();
            for (int i =0;i<Publicfeatures.size();i++){
                featuresList.add(new AbstractMap.SimpleEntry<Integer,Double>(Privatefeatures.get(i),fiMatrix[PublicAllFeatures.get("i_"+e.getKey())][i]));
            }
            Collections.sort(featuresList,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
            if (featuresList.size()<1){
            }else{
                featuresList = new ArrayList<>(featuresList.subList(0,1));
                newList.add(featuresList.get(0).getKey());
            }
            completionMap.put(e.getKey(),newList);
        });
        return completionMap;
    }

}
