package ContentInitializer;

import java.util.*;
import java.util.Map.Entry;

class User {
	
	int activeUser;
	Map<Integer, ArrayList<Integer>> map;
	Map<Integer, Double> votes;
	Map<Integer, HashMap<Integer, Float>> tfidf;
	private HashMap<Integer, Float> userProfile = new HashMap<>();
	private HashSet<Integer> userItemsList = new HashSet<>();
	private HashSet<Integer> userFeaturesList = new HashSet<>();
	 
	User(Map<Integer, ArrayList<Integer>> map, int user, Map<Integer, Double> votes, Map<Integer, HashMap<Integer, Float>> tfidf) {
		this.map = map;
		this.votes = votes;
		this.activeUser = user;
		this.tfidf = tfidf;
	}



	HashMap<Integer, Double> getProfile(){
		HashMap<Integer, Double> profile = new HashMap<>();
		for (Entry<Integer, Float> entry : userProfile.entrySet()){
			profile.put(entry.getKey(), Double.valueOf(entry.getValue()));
		}
		return profile;
	}

	public void computeUserProfile(){
		for(Entry<Integer, Double> entry : votes.entrySet()){
			int item = entry.getKey();
			if(tfidf.get(item) != null){
				userItemsList.add(item);
				userFeaturesList.addAll(tfidf.get(item).keySet());
			}
		}
		for(int feature : userFeaturesList){
			ArrayList<Double> Weights = new ArrayList<>();
			for (int item : userItemsList)
			{
				Float weight = 0f;
				for(Entry<Integer,Float> pair : tfidf.get(item).entrySet()){
					if(pair.getKey()==feature)weight=pair.getValue();
				}
				Weights.add((double)weight);
			}
			double weightRes[] = stats(Weights);
			userProfile.put(feature, (float)weightRes[0]);
		}
	}
	
	private  double[] stats(ArrayList<Double> vec){
		int n = vec.size();
		double mean;
		double sum = 0;
		double stdDev;
		double min;
		for(Double d : vec)
		{ sum += d;}
		mean = sum/n;
		sum = 0;
		for(Double d : vec)
		{ sum += Math.pow((d-mean), 2);}
		stdDev = Math.sqrt(sum/n);
		min=mean;
		if(vec.size()!=0){
			if (stdDev!=0){min = (Collections.min(vec)-mean)/stdDev;}else{min=mean;}
		}
		return new double[]{mean,stdDev,min};
	}
	
}
