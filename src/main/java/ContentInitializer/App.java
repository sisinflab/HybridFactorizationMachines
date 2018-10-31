package ContentInitializer;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class App 
{

 	private static Map<Integer, ArrayList<Integer>> map = new HashMap<>();
 	private static Map<Integer, HashMap<Integer, Double>> allVotes = new HashMap<>();
	private static Map<Integer, HashMap<Integer, Float>> tfidf = new HashMap<>();
 	
    public static HashMap<Integer, HashMap<Integer, Double>> useContent(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, HashMap<Integer, ArrayList<Integer>> featMap, String[] args ) throws IOException
    {
    	allVotes = ratingsMap;
    	map = featMap;
        String fileVotes = args[0];
		String fileAss = args[1];
		System.out.println( "fileRatings: " + fileVotes);
		System.out.println( "fileAttributes: " + fileAss);
		tfidf = TfIdf.getMap(map);
        HashMap<Integer, HashMap<Integer,Double>> profiles = new HashMap<>();
        allVotes.entrySet().parallelStream().map(map2 ->{
        	User a = new User(map,map2.getKey(),map2.getValue(),tfidf);
        	a.computeUserProfile();
            return new AbstractMap.SimpleEntry<>(map2.getKey(),a.getProfile());
        }).forEachOrdered(entry -> profiles.put(entry.getKey(),entry.getValue()));
        return profiles;
    }

	public static Map<Integer, HashMap<Integer, Float>> getTfidf() {
		return tfidf;
	}

}
