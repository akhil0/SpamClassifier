import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;


import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;


public class DQBuild {

	static HashMap<String, String> okapitf = new HashMap<String,String>(); 
	static HashMap<String, String> label = new HashMap<String,String>(); 
	static HashMap<String, String> bm25 = new HashMap<String,String>(); 
	static HashMap<String, String> tfidf = new HashMap<String,String>(); 
	static HashMap<String, String> laplace = new HashMap<String,String>(); 
	static HashMap<String, String> jm = new HashMap<String,String>(); 
	static HashMap<String, String> trainmap = new HashMap<String,String>(); 
	static HashMap<String, String> testmap = new HashMap<String,String>(); 
	static ArrayList<String> idlist = new ArrayList<String>();
	static ArrayList<String> trainlist = new ArrayList<String>();
	static ArrayList<String> testlist = new ArrayList<String>();
	static HashMap<String, Double> trainprecmap = new HashMap<String, Double>();
	static HashMap<String, Double> testprecmap = new HashMap<String, Double>();
	static ArrayList<String> querylist = new ArrayList<>(Arrays.asList("89", "55" ,"56","71","64","62","93",
			"99","58","77","54","87","94","100","89","61","95","68","57","97","98","60","80","63","91"));
	static int b = 0;
	static int a = 0;
	
	public static void main(String[] args) throws Exception {

		String data = "";
		String filePath = "C:\\Users\\AKI\\workspace\\ML-I\\qrels.txt";
		try {
			data = String.join("\n", Files.readAllLines(Paths.get(filePath) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray = data.split("\n");


		for(String s : parray) {
			String[] linedata = s.split(" ");
			label.put(linedata[0]+":"+linedata[2] , linedata[3]);
			idlist.add(linedata[0]+":"+linedata[2]);
			
			
		}
		System.out.println(label.size());

		//Okapi TF
		String data1 = "";
		String filePath1 = "C:\\Users\\AKI\\workspace\\ML-I\\okapitf.txt";
		try {
			data1 = String.join("\n", Files.readAllLines(Paths.get(filePath1) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray1 = data1.split("\n");


		for(String s : parray1) {
			String[] linedata = s.split(" ");
			okapitf.put(linedata[0]+":"+linedata[2] , linedata[4]);
		}
		System.out.println(okapitf.size());

		//TF-IDF
		String data2 = "";
		String filePath2 = "C:\\Users\\AKI\\workspace\\ML-I\\tfidf.txt";
		try {
			data2 = String.join("\n", Files.readAllLines(Paths.get(filePath2) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray2 = data2.split("\n");


		for(String s : parray2) {
			String[] linedata = s.split(" ");
			tfidf.put(linedata[0]+":"+linedata[2] , linedata[4]);
		}
		System.out.println(tfidf.size());


		//Bm25
		String data3 = "";
		String filePath3 = "C:\\Users\\AKI\\workspace\\ML-I\\bm25.txt";
		try {
			data3 = String.join("\n", Files.readAllLines(Paths.get(filePath3) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray3 = data3.split("\n");


		for(String s : parray3) {
			String[] linedata = s.split(" ");
			bm25.put(linedata[0]+":"+linedata[2] , linedata[4]);
		}
		System.out.println(bm25.size());

		//Laplace
		String data4 = "";
		String filePath4 = "C:\\Users\\AKI\\workspace\\ML-I\\laplace.txt";
		try {
			data4 = String.join("\n", Files.readAllLines(Paths.get(filePath4) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray4 = data4.split("\n");


		for(String s : parray4) {
			String[] linedata = s.split(" ");
			laplace.put(linedata[0]+":"+linedata[2] , linedata[4]);
		}
		System.out.println(laplace.size());

		//JM Smoothig
		String data5 = "";
		String filePath5 = "C:\\Users\\AKI\\workspace\\ML-I\\jm.txt";
		try {
			data5 = String.join("\n", Files.readAllLines(Paths.get(filePath5) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] parray5 = data5.split("\n");


		for(String s : parray5) {
			String[] linedata = s.split(" ");
			jm.put(linedata[0]+":"+linedata[2] , linedata[4]);
		}
		System.out.println(jm.size());

		idlist.forEach(e ->  {
			String id = e.toString();
			String[] a = id.split(":");
			if(querylist.contains(a[0])){
			
			if(a[0].equals("59") || a[0].equals("68") || a[0].equals("89") || a[0].equals("91") || a[0].equals("99"))
			{
				testmap.put(id,"");
				testlist.add(id);
			}
			else
			{
				trainmap.put(id, "");
				trainlist.add(id);
			}
			}
		});



		
		File log = new File("C:\\Users\\AKI\\workspace\\ML-I\\training.txt");
		Iterator<String> trainitr = trainmap.keySet().iterator();
		

		
			FileWriter fileWriter = new FileWriter(log, true);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			trainlist.forEach(e ->  {
				String id = e.toString();
				String labelval = label.get(id);
				String okapitfval = "0";
				String bm25val = "0";
				String tfidfval = "0";
				String laplaceval = "0";
				String jmval = "0";

				if(okapitf.containsKey(id)){
					okapitfval = okapitf.get(id);
				}
				if(bm25.containsKey(id)){
					bm25val = bm25.get(id);
				}
				if(tfidf.containsKey(id)){
					tfidfval = tfidf.get(id);
				}
				if(laplace.containsKey(id)){
					laplaceval = laplace.get(id);
				}
				if(jm.containsKey(id)){
					jmval = jm.get(id);
				}
				try {
					//bufferedWriter.write(id + "," + okapitfval + "," + tfidfval + "," + bm25val + "," + laplaceval + "," + jmval + "," + labelval + "\n");
					bufferedWriter.write(labelval + " 1:" + okapitfval + " 2:" + tfidfval + " 3:" + bm25val + " 4:" + laplaceval + " 5:" + jmval + " "  + "\n");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			});

			bufferedWriter.close();




			System.out.println("Done");


		


		File log1 = new File("C:\\Users\\AKI\\workspace\\ML-I\\testing.txt");
		//Iterator<String> testitr = testmap.keySet().iterator();
		//int idlo = 0;

			FileWriter fileWriter1 = new FileWriter(log1, true);

			BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);

			testlist.forEach(e ->  {
				//idlo++;
				//System.out.println(idlo);
				String id = e.toString();
				String labelval = label.get(id);
				String okapitfval = "0";
				String bm25val = "0";
				String tfidfval = "0";
				String laplaceval = "0";
				String jmval = "0";

				if(okapitf.containsKey(id)){
					okapitfval = okapitf.get(id);
				}
				if(bm25.containsKey(id)){
					bm25val = bm25.get(id);
				}
				if(tfidf.containsKey(id)){
					tfidfval = tfidf.get(id);
				}
				if(laplace.containsKey(id)){
					laplaceval = laplace.get(id);
				}
				if(jm.containsKey(id)){
					jmval = jm.get(id);
				}
				try {
					//bufferedWriter1.write(id + "," + okapitfval + "," + tfidfval + "," + bm25val + "," + laplaceval + "," + jmval + "," + labelval + "\n");
					bufferedWriter1.write(labelval + " 1:" + okapitfval + " 2:" + tfidfval + " 3:" + bm25val + " 4:" + laplaceval + " 5:" + jmval + " "  + "\n");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			});

			bufferedWriter1.close();
			
			
			
			Train.main(new String[]{"-s","11","-c", "0.001", "training.txt", "model"});
			Predict.main(new String[]{"-q", "testing.txt", "model", "testingvalues.txt"});
			Predict.main(new String[]{"-q", "training.txt", "model", "trainingvalues.txt"});

			String data6 = "";
			String filePath6 = "C:\\Users\\AKI\\workspace\\ML-I\\trainingvalues.txt";
			try {
				data6 = String.join("\n", Files.readAllLines(Paths.get(filePath6) ,Charset.forName("ISO-8859-1")));

			} catch (Exception e) {
				e.printStackTrace();
			}

			String[] parray6 = data6.split("\n");
			
			trainlist.forEach(e ->  {
				String id = e.toString();
				String val = parray6[b];
				Double rval = Double.valueOf(val);
				b++;
				trainprecmap.put(id, rval);
				});
			//System.out.println(trainprecmap);
			
			String data7 = "";
			String filePath7 = "C:\\Users\\AKI\\workspace\\ML-I\\testingvalues.txt";
			try {
				data7 = String.join("\n", Files.readAllLines(Paths.get(filePath7) ,Charset.forName("ISO-8859-1")));

			} catch (Exception e) {
				e.printStackTrace();
			}

			String[] parray7 = data7.split("\n");
			
			testlist.forEach(e ->  {
				String id = e.toString();
				String val = parray7[a];
				Double rval = Double.valueOf(val);
				a++;
				testprecmap.put(id, rval);
				});
			
			
			
			sortByValue(trainprecmap);
			sortByValue(testprecmap);
			
			File log3 = new File("C:\\Users\\AKI\\workspace\\ML-I\\traineval.txt");
			FileWriter fileWriter3 = new FileWriter(log3, true);
			BufferedWriter bufferedWriter3 = new BufferedWriter(fileWriter3);	
			int c = 1;
			for(Entry e : trainprecmap.entrySet()) {
				String id  = e.getKey().toString();
				String val = e.getValue().toString();
				String a[] = id.split(":");
				bufferedWriter3.write(a[0] + " Q0 " + a[1] + " " + c + " " + val + " Exp" + "\n");
				c++;
			}
			bufferedWriter3.close();
			
			File log4 = new File("C:\\Users\\AKI\\workspace\\ML-I\\testeval.txt");
			FileWriter fileWriter4 = new FileWriter(log4, true);
			BufferedWriter bufferedWriter4 = new BufferedWriter(fileWriter4);	
			int d = 1;
			for(Entry e : testprecmap.entrySet()) {
				String id  = e.getKey().toString();
				String val = e.getValue().toString();
				String a[] = id.split(":");
				bufferedWriter4.write(a[0] + " Q0 " + a[1] + " " + c + " " + val + " Exp" + "\n");
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
