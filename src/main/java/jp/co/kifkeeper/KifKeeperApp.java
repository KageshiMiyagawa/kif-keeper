package jp.co.kifkeeper;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KifKeeperApp {

//	public static void main(String[] args) {
//		SpringApplication.run(KifKeeperApp.class, args);
//	}
	
	static {
	    System.setProperty("java.awt.headless", "false");
	}
	
	public static void main(String[] args) {
		SpringApplication.run(KifKeeperApp.class, args);
		try {
            Desktop.getDesktop().browse(new URI("http://localhost:8080"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
	}
}
