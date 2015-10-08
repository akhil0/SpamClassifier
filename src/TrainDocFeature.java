import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;


public class TrainDocFeature {
	static ArrayList<String>  trainurls=new ArrayList<String>();
	static ArrayList<String>  testurls=new ArrayList<String>();
	static HashMap<String, String> trainmap = new HashMap<String, String>();
	static HashMap<String, String> testmap = new HashMap<String, String>();
	static ArrayList spammap = new ArrayList<>();
	static ArrayList hammap = new ArrayList<>();
	static HashMap<String,String> labelmap = new HashMap<String,String>();
	static ArrayList<String>  totalurls =new ArrayList<String>();
	static String labelval = "";
	static HashMap<String, Double> trainprecmap = new HashMap<String, Double>();
	static HashMap<String, Double> testprecmap = new HashMap<String, Double>();
	static int b = 0;
	static int a = 0;
	static int id = 1;
	public static void main(String[] args) throws Exception {

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

		System.out.println("Test URLS = " + testurls.size());


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

		System.out.println("train Urls = " + trainurls.size()); 

		totalurls.addAll(trainurls);
		totalurls.addAll(testurls);

		System.out.println("total Urls = " + totalurls.size()); 

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



		System.out.println(labelmap.size());

		String data3 = "";
		String filePath3 = "C:\\Users\\AKI\\workspace\\ML-II\\spam.txt";
		try {
			data3 = String.join("\n", Files.readAllLines(Paths.get(filePath3) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray = data3.split("\n");

		for(String s : parray) {
			HashMap tempmap = new HashMap();


			QueryBuilder qb2 = QueryBuilders.matchQuery("text", s);
			SearchResponse response2 = client.prepareSearch("spam_dataset")
					.setQuery(qb2).setSize(1000000).setNoFields().setTimeout("10000")
					.execute().actionGet();
			try
			{
				JSONObject json = new JSONObject(response2.toString());
				JSONObject hits = json.getJSONObject("hits");
				JSONArray jarry = hits.getJSONArray("hits");
				for(int i=0;i<jarry.length();i++)
				{

					JSONObject obj = jarry.getJSONObject(i);
					tempmap.put(obj.get("_id").toString(),0);
				}
			}


			catch(Exception e) {
				e.printStackTrace();
			}

			trainurls.forEach(e -> {
				labelval = labelmap.get(e);
				if (tempmap.containsKey(e)) {

					if (trainmap.containsKey(e)) {
						String templist = trainmap.get(e);
						templist = templist + " " + id + ":1";
						trainmap.put(e,templist);
					}
					else {
						String templist = "";
						templist = labelval + " " + id + ":1";
						trainmap.put(e, templist);
					}
				}
				else {
					if (trainmap.containsKey(e)) {
						String templist = trainmap.get(e);
						templist = templist + " " + id+":0";
						trainmap.put(e,templist);
					}
					else {
						String templist = "";
						templist = labelval +" " +  id+":0";
						trainmap.put(e, templist);
					}
				}
			});


			testurls.forEach(e -> {
				labelval = labelmap.get(e);
				if (tempmap.containsKey(e)) {
					if (testmap.containsKey(e)) {

						String templist = testmap.get(e);
						templist = templist + " " + id + ":1";
						testmap.put(e,templist);
					}
					else {
						String templist = "";
						templist = labelval + " " + id + ":1";
						testmap.put(e, templist);
					}
				}
				else {
					if (testmap.containsKey(e)) {
						String templist = testmap.get(e);
						templist = templist + " " + id+":0";
						testmap.put(e,templist);
					}
					else {
						String templist = "";
						templist = labelval + " " +  id+":0";
						testmap.put(e, templist);
					}
				}
			});

			id++;
		}

		client.close();


		File log = new File("C:\\Users\\AKI\\workspace\\ML-II\\training.txt");

		FileWriter fileWriter = new FileWriter(log, true);

		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		trainurls.forEach(e -> {
			String str = trainmap.get(e);
			try {
				bufferedWriter.write(str + "\n");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		bufferedWriter.close();


		File log2 = new File("C:\\Users\\AKI\\workspace\\ML-II\\testing.txt");

		FileWriter fileWriter2 = new FileWriter(log2, true);

		BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);

		testurls.forEach(e -> {
			String str = testmap.get(e);
			try {
				bufferedWriter2.write(str + "\n");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		bufferedWriter2.close();

		System.out.println("success");


		Train.main(new String[]{"-s","11","-c", "0.001", "training.txt", "model"});
		Predict.main(new String[]{"-q", "testing.txt", "model", "testingvalues.txt"});
		Predict.main(new String[]{"-q", "training.txt", "model", "trainingvalues.txt"});

		
		String data6 = "";
		String filePath6 = "C:\\Users\\AKI\\workspace\\ML-II\\trainingvalues.txt";
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
		String filePath7 = "C:\\Users\\AKI\\workspace\\ML-II\\testingvalues.txt";
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
		
		File log3 = new File("C:\\Users\\AKI\\workspace\\ML-II\\traineval.txt");
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
		
		File log4 = new File("C:\\Users\\AKI\\workspace\\ML-II\\testeval.txt");
		FileWriter fileWriter4 = new FileWriter(log4, true);
		BufferedWriter bufferedWriter4 = new BufferedWriter(fileWriter4);	
		int d = 1;
		for(Entry e : testprecmap.entrySet()) {
			String id  = e.getKey().toString();
			String val = e.getValue().toString();
			String labelval = labelmap.get(id);
			bufferedWriter4.write(id + " " + c + " " + val + "\t" + labelval + "\n");
			d++;
		}
		bufferedWriter4.close();
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
