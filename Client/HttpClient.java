import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpClient {

  // Method responsible for handling the Http request GET & PUT to the server
  public static void HTTP_Request(String host, int port, String path,
      String method) throws UnknownHostException, IOException {

    //Resolve the hostname to an IP address
    InetAddress ip = InetAddress.getByName(host);

    //Open socket to a specific host and port
    Socket socket = new Socket(host, port);
        
    //Get input and output streams for the socket
    PrintWriter out = new PrintWriter(socket.getOutputStream());
    BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

    
    if(method.equals("OVER")){
      System.out.println(method);
      out.println(method);
       out.flush();
    }
    // HTTP GET
    else if (method.equals("GET")) {
      // Construct a HTTP GET request
      String request = "GET " + path + " HTTP/1.0\r\n"
              + "Accept: */*\r\n" + "Host: "+host+"\r\n"
              + "Connection: Close\r\n\r\n";

       System.out.println(request);

      //Sends off HTTP GET request
       out.println(request);
       out.flush();
    } else if (method.equals("PUT")) { 
        BufferedInputStream dataIn = null;
        BufferedOutputStream dataOut = null;

        try {
            dataIn = new BufferedInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream());
            dataOut = new BufferedOutputStream(socket.getOutputStream());

            System.out.println("Inside PUT method");
            File file = new File(path);
            int fileLength = (int) file.length();
            byte[] fileContent = scanFile(file, fileLength);

            String fileName = "index.html";
            String hostName = "127.0.0.1";
            // send HTTP Headers
            out.println("PUT " + fileName);
            out.println("Host: " + host);
            out.println();
            out.flush(); // flush character output stream buffer

            dataOut.write(fileContent, 0, fileLength);
            dataOut.flush();
	    System.out.println("200 OK: \nThe requested file" + " is successfully updated in the Server");
            socket.close();
            return;

        } catch (IOException ioe) {
            System.err.println("Error 404:\n File Not Found");
        } 
}
   else {
      System.out.println("Invalid HTTP method");
      socket.close();
      return;
    }
   
   
    
    // Reads HTTP response
    boolean loop = true;
    StringBuilder response = new StringBuilder(8096);
    while (loop) {
      if (in.ready()) {
        int i = 0;
        while (i != -1) {
          i = in.read();
          response.append((char) i);
        }
        loop = false;
      }
    }

     System.out.println(response);

        
    if (response.substring(response.indexOf(" ") + 1,
        response.indexOf(" ") + 4).equals("200")) {

      //Save the payload of the HTTP response message
      File file = new File("server_to_client.html");
      PrintWriter printWriter = new PrintWriter(file);
      printWriter.println(response.substring(response.indexOf("\r\n\r\n") + 118));
      printWriter.close();
    }
 
    //Closes socket
    socket.close();
  }
    
  public static int indexOf(byte[] outerArray, byte[] smallerArray) {
    for (int i = 0; i < outerArray.length - smallerArray.length + 1; ++i) {
        boolean found = true;
        for (int j = 0; j < smallerArray.length; ++j) {
            if (outerArray[i + j] != smallerArray[j]) {
                found = false;
                break;
            }
        }
        if (found)
            return i;
    }
    return -1;
}

private static byte[] scanFile(File file, int fileLength) throws IOException {
    FileInputStream fileIn = null;
    byte[] fileContent = new byte[fileLength];

    try {
        fileIn = new FileInputStream(file);
        fileIn.read(fileContent);
    } finally {
        if (fileIn != null)
            fileIn.close();
    }

    return fileContent;
}
  public static void main(String[] args) throws Exception {
       
     if(args.length == 0) {
	System.out.println("No arguments passed! \n"
				+ "Please pass the following arguments in order while running. \n"
						+ "Server name, Port no. of server,Http command, path of requested object");
	return;
       }
     
      // Reading the arguments
	String hostName = args[0];
	int portNumber = Integer.parseInt(args[1]);

	String httpMethod = args[2];
        httpMethod = httpMethod.toUpperCase();

	String path = args[3];

      // Generates HTTP request
      HTTP_Request(hostName, portNumber, path, httpMethod);

  }
}
