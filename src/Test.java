import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;

public class Test {
	public static void main(String[] args) throws Exception {
		
		String filePath1 = "C:\\Users\\AKI\\workspace\\ML-II\\trec07p\\data\\inmail.1";
		File file = new File (filePath1);
		MimeMessage MimeMessage = MimeMessageUtils.createMimeMessage ( null , file);
		MimeMessageParser Parser = new MimeMessageParser (MimeMessage);
        MimeMessageParser mkv = Parser.parse();
        
        System.out.println(mkv.getHtmlContent());
	}
}