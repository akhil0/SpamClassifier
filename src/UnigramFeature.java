import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import ucar.ma2.Range.Iterator;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;


public class UnigramFeature {

	static ArrayList<String>  trainurls=new ArrayList<String>();
	static ArrayList<String>  testurls=new ArrayList<String>();
	static ArrayList<String>  totalurls=new ArrayList<String>();
	static HashMap<String, ArrayList> trainmap = new HashMap<String, ArrayList>();
	static HashMap<String, ArrayList> testmap = new HashMap<String, ArrayList>();
	static HashMap<String, Integer> wordmap = new HashMap<String, Integer>();
	static HashMap<String, Double> trainprecmap = new HashMap<String, Double>();
	static HashMap<String, Double> testprecmap = new HashMap<String, Double>();
	static HashMap<String, Double> scoremap = new HashMap<String, Double>();
	static int b = 0;
	static int a = 0;
	static int g = 1;
	static int sid = 6;
	static ArrayList spammap = new ArrayList<>();
	static ArrayList hammap = new ArrayList<>();
	static HashMap<String,String> labelmap = new HashMap<String,String>();
	static int wordid = 0;
	static String stringop = "";
	static ArrayList<String> wordlist = new ArrayList<String>();

	public static void main(String[] args) throws IOException, Exception {

		Node node = nodeBuilder().client(true).clusterName("phoenixwings") 
				.node();
		Client client = node.client();

		QueryBuilder qb = QueryBuilders.matchQuery("split", "train");
		SearchResponse response = client.prepareSearch("spam_dataset")
				.setQuery(qb).setSize(1000000).setNoFields().setTimeout("10000")
				.execute().actionGet();


		try
		{
			JSONObject json = new JSONObject(response.toString());
			JSONObject hits = json.getJSONObject("hits");
			JSONArray jarry = hits.getJSONArray("hits");
			//System.out.println(jarry.length());


			for(int i=0;i<jarry.length();i++)
			{

				JSONObject obj = jarry.getJSONObject(i);
				trainurls.add(obj.get("_id").toString());
			}


		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		//System.out.println("Test URLS = " + testurls.size());


		QueryBuilder qb1 = QueryBuilders.matchQuery("split", "test");
		SearchResponse response1 = client.prepareSearch("spam_dataset")
				.setQuery(qb1).setSize(100000).setNoFields().setTimeout("10000")
				.execute().actionGet();


		try
		{
			JSONObject json = new JSONObject(response1.toString());
			JSONObject hits = json.getJSONObject("hits");
			JSONArray jarry = hits.getJSONArray("hits");
			//System.out.println(jarry.length());
			for(int i=0;i<jarry.length();i++)
			{

				JSONObject obj = jarry.getJSONObject(i);
				testurls.add(obj.get("_id").toString());
			}






		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		//System.out.println("train Urls = " + trainurls.size()); 

		totalurls.addAll(trainurls);
		totalurls.addAll(testurls);


		QueryBuilder qb5 = QueryBuilders.matchQuery("label", "spam");
		SearchResponse response5 = client.prepareSearch("spam_dataset")
				.setQuery(qb5).setSize(1000000).setNoFields().setTimeout("10000")
				.execute().actionGet();


		try
		{
			JSONObject json = new JSONObject(response5.toString());
			JSONObject hits = json.getJSONObject("hits");
			JSONArray jarry = hits.getJSONArray("hits");
			//System.out.println(jarry.length());


			for(int i=0;i<jarry.length();i++)
			{

				JSONObject obj = jarry.getJSONObject(i);
				spammap.add(obj.get("_id").toString());
			}


		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Spam Map = " + spammap.size());


		QueryBuilder qb7 = QueryBuilders.matchQuery("label", "ham");
		SearchResponse response7 = client.prepareSearch("spam_dataset")
				.setQuery(qb7).setSize(100000).setNoFields().setTimeout("10000")
				.execute().actionGet();


		try
		{
			JSONObject json = new JSONObject(response7.toString());
			JSONObject hits = json.getJSONObject("hits");
			JSONArray jarry = hits.getJSONArray("hits");
			//System.out.println(jarry.length());
			for(int i=0;i<jarry.length();i++)
			{

				JSONObject obj = jarry.getJSONObject(i);
				hammap.add(obj.get("_id").toString());
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Ham Map = " + hammap.size()); 


		spammap.forEach(e -> {
			labelmap.put(e.toString(),"1");
		});

		hammap.forEach(e -> {
			labelmap.put(e.toString(),"0");
		});



		trainurls.forEach(e -> {
			System.out.println(e);
			String labelval = labelmap.get(e);
			GetResponse getResponse = client
					.prepareGet("spam_dataset", "document", e)
					.execute().actionGet();
			Map<String, Object> source = getResponse.getSource();
			ArrayList lines = new ArrayList();
			if(source.get("text")!= null) {
				String text = source.get("text").toString();
				text = text.replaceAll(">", " ").replaceAll("<", " ").replaceAll("_", " ").replaceAll("'", " ")
						.replaceAll("-", " ").replaceAll(",", " ")
						//.replaceAll(",", " ").replaceAll("=", " ").replaceAll(":", " ")
						//.replaceAll("-", " ").replaceAll("/", " ").replaceAll(".", " ")
						.replaceAll(" +", " ").trim().toLowerCase();
				String[] words = text.split(" ");
				
				for(String s : words) {
					if(wordmap.containsKey(s))
					{


					}
					else {
						wordid++;
						wordmap.put(s , wordid);
						wordlist.add(s);
					}

					int id = wordmap.get(s);
					if(lines.contains(id)) {
					}
					else {
						lines.add(id);
					}
					
				}
				
				trainmap.put(e, lines);
			}
			else
			{
				trainmap.put(e, lines);
			}

		});
		
		testurls.forEach(e -> {
			System.out.println(e);
			String labelval = labelmap.get(e);
			GetResponse getResponse = client
					.prepareGet("spam_dataset", "document", e)
					.execute().actionGet();
			Map<String, Object> source = getResponse.getSource();
			ArrayList lines = new ArrayList();
			if(source.get("text")!= null) {
				String text = source.get("text").toString();
				text = text.replaceAll(">", " ").replaceAll("<", " ").replaceAll("_", " ").replaceAll("'", " ")
						.replaceAll("-", " ").replaceAll(",", " ").replaceAll("'", "")
						//.replaceAll(",", " ").replaceAll("=", " ").replaceAll(":", " ")
						//.replaceAll("-", " ").replaceAll("/", " ").replaceAll(".", " ")
						.replaceAll(" +", " ").trim().toLowerCase();
				String[] words = text.split(" ");
				
				for(String s : words) {
					s = s.trim();
					if(wordmap.containsKey(s))
					{


					}
					else {
						wordid++;
						wordmap.put(s , wordid);
						wordlist.add(s);
					}

					int id = wordmap.get(s);
					
					if(lines.contains(id)){
					}
					else {
						lines.add(id);
					}
					
				}
				testmap.put(e, lines);
			}
			else
			{
				testmap.put(e, lines);
			}

		});
		
		
		
		File log = new File("C:\\Users\\AKI\\workspace\\ML-II\\training2.txt");

		FileWriter fileWriter = new FileWriter(log, true);

		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		trainurls.forEach(e -> {
			ArrayList str = trainmap.get(e);
			Collections.sort(str);
			String labelval = labelmap.get(e);
			stringop = labelval;
			str.forEach(d -> {
				stringop = stringop + " " + d + ":1";
			});
			try {
				bufferedWriter.write(stringop + "\n");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		bufferedWriter.close();


		File log2 = new File("C:\\Users\\AKI\\workspace\\ML-II\\testing2.txt");

		FileWriter fileWriter2 = new FileWriter(log2, true);

		BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);

		testurls.forEach(e -> {
			ArrayList str = testmap.get(e);
			Collections.sort(str);
			String labelval = labelmap.get(e);
			stringop = labelval;
			str.forEach(d -> {
				stringop = stringop + " " + d + ":1";
			});
			try {
				bufferedWriter2.write(stringop + "\n");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		bufferedWriter2.close();

		System.out.println("success");
		
		//Train.main(new String[]{"-s","11","-c", "0.001", "training2.txt", "model2"});
		//Predict.main(new String[]{"-q", "testing2.txt", "model2", "testingvalues2.txt"});
		//Predict.main(new String[]{"-q", "training2.txt", "model2", "trainingvalues2.txt"});
		
		String[] args1 = { "-s", "0",
				"training2.txt",
				"modelTrain.txt" };
		Train.main(args1);

		String[] args2 = { "-b", "1",
				"testing2.txt",
				"modelTrain.txt",
				"OutputScore1.txt" };
		Predict.main(args2);
		
		String[] args3 = { "-b", "1",
				"training2.txt",
				"modelTrain.txt",
				"OutputScore2.txt" };
		Predict.main(args3);

		/*String data6 = "";
		String filePath6 = "C:\\Users\\AKI\\workspace\\ML-II\\trainingvalues2.txt";
		try {
			data6 = String.join("\n", Files.readAllLines(Paths.get(filePath6) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray6 = data6.split("\n");
		
		trainurls.forEach(e ->  {
			String id = e.toString();
			String val = parray6[b];
			Double rval = Double.valueOf(val);
			b++;
			trainprecmap.put(id, rval);
			});
		//System.out.println(trainprecmap);
		
		String data7 = "";
		String filePath7 = "C:\\Users\\AKI\\workspace\\ML-II\\testingvalues2.txt";
		try {
			data7 = String.join("\n", Files.readAllLines(Paths.get(filePath7) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray7 = data7.split("\n");
		
		testurls.forEach(e ->  {
			String id = e.toString();
			String val = parray7[a];
			Double rval = Double.valueOf(val);
			a++;
			testprecmap.put(id, rval);
			});
		

		sortByValue(trainprecmap);
		sortByValue(testprecmap);
		
		File log3 = new File("C:\\Users\\AKI\\workspace\\ML-II\\traineval2.txt");
		FileWriter fileWriter3 = new FileWriter(log3, true);
		BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);	
		int c = 1;
		for(Entry e : trainprecmap.entrySet()) {
			String id  = e.getKey().toString();
			String val = e.getValue().toString();
			
			bufferedWriter3.write(id + " " + c + " " + val + " Exp" + "\n");
			c++;
		}
		bufferedWriter3.close();
		
		File log4 = new File("C:\\Users\\AKI\\workspace\\ML-II\\testeval2.txt");
		FileWriter fileWriter4 = new FileWriter(log4, true);
		BufferedWriter bufferedWriter4 = new BufferedWriter(fileWriter4);	
		int d = 1;
		for(Entry e : testprecmap.entrySet()) {
			String id  = e.getKey().toString();
			String val = e.getValue().toString();
			
			bufferedWriter4.write(id + " " + c + " " + val + " Exp" + "\n");
			d++;
		}
		bufferedWriter4.close();*/
		
		
		String data10 = "";
		String filePath10 = "C:\\Users\\AKI\\workspace\\ML-II\\modelTrain.txt";
		try {
			data10 = String.join("\n", Files.readAllLines(Paths.get(filePath10) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray10 = data10.split("\n");
		System.out.println(wordlist.size());
		
		java.util.Iterator<String> itr = wordlist.iterator();
		while(itr.hasNext())
		{
			String glo = itr.next();
			String val = parray10[sid];
			sid++;
			Double dval = Double.parseDouble(val);
			scoremap.put(glo, dval);
			if (sid == 737347)
				break;
		}
		
		sortByValue(scoremap);
		
		File log10 = new File("C:\\Users\\AKI\\workspace\\ML-II\\scoresmap.txt");
		FileWriter fileWriter10 = new FileWriter(log10, true);
		BufferedWriter bufferedWriter10 = new BufferedWriter(fileWriter10);	
		
		for(Entry e : scoremap.entrySet()) {
			String id  = e.getKey().toString();
			String val = e.getValue().toString();
			
			bufferedWriter10.write(id + "\t" + val + "\n");
			g++;
		}
		bufferedWriter10.close();
		
	}



	public static <K, V extends Comparable<? super V>> Map<K, V> 
	sortByValue( Map<K, V> map )
	{
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();

		st.sorted(Comparator.comparing(e -> e.getValue()))
		.forEach(e ->result.put(e.getKey(),e.getValue()));

		return result;
	}

}
