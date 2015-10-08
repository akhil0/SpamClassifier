import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Entity;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.apache.james.mime4j.parser.Field;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Denis Lunev <den@mozgoweb.com>
 */

public class parser {

    private StringBuffer txtBody;
    private StringBuffer htmlBody;
    private ArrayList<BodyPart> attachments;

    public String parseMessage(String fileName) {
    	StringBuffer parsedtext=null;
        FileInputStream fis = null;

        txtBody = new StringBuffer();
        htmlBody = new StringBuffer();
        attachments = new ArrayList();

        try {
            fis = new FileInputStream(fileName);
            
            Message mimeMsg = new Message(fis);

            if (mimeMsg.isMultipart()) {
                Multipart multipart = (Multipart) mimeMsg.getBody();
                parsedtext=parseBodyParts(multipart);
            } else {
                //If it's single part message, just get text body
                String text = getTxtPart(mimeMsg);
                Document doc = Jsoup.parse(text);
                parsedtext=txtBody.append(doc.body().text());
               
            }

           

                      

        } catch (IOException ex) {
            ex.fillInStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return parsedtext.toString();
    }

    /**
     * This method classifies bodyPart as text, html or attached file
     *
     * @param multipart
     * @return 
     * @throws IOException
     */
    private StringBuffer parseBodyParts(Multipart multipart) throws IOException {
    	StringBuffer parsedtext=new StringBuffer();
        for (BodyPart part : multipart.getBodyParts()) {
            if (part.isMimeType("text/plain")) {
                String txt = getTxtPart(part);
                Document doc = Jsoup.parse(txt);
                parsedtext=txtBody.append(doc.body().text());
            } else if (part.isMimeType("text/html")) {
                String html = getTxtPart(part);
                Document doc = Jsoup.parse(html);
                
                parsedtext=htmlBody.append(doc.body().text());
            } else if (part.getDispositionType() != null && !part.getDispositionType().equals("")) {
                //If DispositionType is null or empty, it means that it's multipart, not attached file
                attachments.add(part);
            }

            //If current part contains other, parse it again by recursion
            if (part.isMultipart()) {
                parseBodyParts((Multipart) part.getBody());
            }
        }
        return parsedtext;
    }

    /**
     *
     * @param part
     * @return
     * @throws IOException
     */
    private String getTxtPart(Entity part) throws IOException {
        //Get content from body
        TextBody tb = (TextBody) part.getBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tb.writeTo(baos);
        return new String(baos.toByteArray());
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        String path = "trec07p/data/inmail.1";

        parser newparser = new parser();
        String text=newparser.parseMessage(path);
        System.out.println(text);
    }
}