import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;











import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;











import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;



public class ManualSpamIndex {
	static HashMap<String, String> labelmap = new HashMap<String, String>();
	static int id = 1;
	public static void main(String[] args) throws IOException {

		Node node = nodeBuilder().client(true).clusterName("phoenixwings").node();
		Client client = node.client();
		// Walking thru Files in Folder
		
		String data3 = "";
		String filePath3 = "C:\\Users\\AKI\\workspace\\ML-II\\trec07p\\full\\index";
		try {
			data3 = String.join("\n", Files.readAllLines(Paths.get(filePath3) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		
		String[] parray = data3.split("\n");
		for(String s : parray) {
			String[] a = s.split(" ");
			String val = a[0];
			String id = a[1].substring(8);
			labelmap.put(id, val);
			
		}
		
		
		
		Files.walk(Paths.get("trec07p\\data")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				String filePath1 = filePath.toString();

				
				
				String text = "";
				
				try {
					text = readdata(filePath1);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				filePath1 = filePath1.substring(13);
				
				String value = "";
				String labval = "";

				// Breaking all DOCS into String Arrays
				
					if(id % 5 == 0){
						value = "test";
					}
					else {
						value = "train";
					}
					
					labval = labelmap.get(filePath1);
					
					System.out.println("ID: " + filePath1);
					String val = "";
					val = filePath1;
					
					try {
						IndexResponse response = client.prepareIndex("spam_dataset", "document", val)
								.setSource(jsonBuilder()
										.startObject()
										.field("text", text)
										.field("split", value)
										.field("label", labval)
										.endObject()
										)
										.execute()
										.actionGet();
					} catch (Exception e) {
						e.printStackTrace();
					}
				//System.out.println(id);
				id++;



			}
		});
	}
	
	
	private static String readdata(String filePath1) throws Exception, Exception {
		String text = "";
		File file = new File (filePath1);
		MimeMessage MimeMessage = MimeMessageUtils.createMimeMessage ( null , file);
        MimeMessageParser Parser = new MimeMessageParser (MimeMessage);
        MimeMessageParser mkv = Parser.parse();
        
        if(mkv.hasPlainContent()) {
      	  text = mkv.getPlainContent();
        }
        else if (mkv.hasHtmlContent()){
      	  Document prstxt = Jsoup.parse(mkv.getHtmlContent());
           text = prstxt.body().text().toString();
        }
        
		return text;
		
        
	}
}
