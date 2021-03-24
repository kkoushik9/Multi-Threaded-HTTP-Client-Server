import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpServer implements Runnable{


    static boolean flag = false;
    private static Socket socket;
    private static ServerSocket serverSocket;

    public HttpServer(Socket socket){
        this.socket = socket;
    }

    /**
     * handles get requests
     */
    public void getRequest(PrintWriter printWriter, BufferedOutputStream bufferedOutputStream, String requestedFile){

        try{


            String path = "./" + requestedFile;

            System.out.println("Inside GET");

            File file = new File(path);
            int fileLength = (int)file.length();

            // replace the path here with your argument.
            byte[] fileData = Files.readAllBytes(Paths.get(path));


            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Server: "+ InetAddress.getLocalHost().getHostAddress().trim());
            printWriter.println("Date: "+ new Date());
            printWriter.println("Content-type: text/html");
            printWriter.println("Content-length: "+fileLength);
            printWriter.println();
            printWriter.flush();

            bufferedOutputStream.write(fileData, 0, fileLength);
            bufferedOutputStream.flush();

        }catch (Exception e){
            printWriter.println("HTTP/1.1 404 FILE NOT FOUND");
            printWriter.println("Date: "+ new Date());
            printWriter.println("Content-type: text/html");
            printWriter.println("Content-length: "+ 0);
            printWriter.println();
            printWriter.flush();

            System.out.println("NO FILE FOUND");
        }

    }

    /**
     * handles put requests
     */
    public void putRequest(PrintWriter printWriter, byte[] bytes, int byteSize, BufferedInputStream bufferedInputStream)
            throws IOException{


        System.out.println("Inside put method");

        FileOutputStream fileOutputStream = null;

        String fileContent = "";
        try{

            fileOutputStream = new FileOutputStream(new File("./client_to_server.html"));

            fileOutputStream.write(bytes, 0, byteSize);


            while(!Thread.interrupted()){

                byteSize = bufferedInputStream.read(bytes);

                if(byteSize > 0 || bufferedInputStream.available() > 0){

                    fileOutputStream.write(bytes);
                    fileContent += new String(bytes, 0, 1024);
                }else {
                    break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        finally {

            fileOutputStream.close();
        }

        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Server: "+ InetAddress.getLocalHost().getHostAddress().trim());
        printWriter.println("Date: " + new Date());
        printWriter.println();
        printWriter.flush();

        System.out.println("File has been updated");


    }

    public void invalidRequest(PrintWriter printWriter) throws UnknownHostException {
        printWriter.println("HTTP/1.1 501 Not Implemented");
        printWriter.println("Server: "+ InetAddress.getLocalHost().getHostAddress().trim());
        printWriter.println("Date: "+ new Date());
        printWriter.println("Content-type: text/html");
        printWriter.println("Content-length: 0");
        printWriter.println();
        printWriter.flush();
    }

    public static void main(String[] args){

        try{
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            System.out.print("Server listening on port: "+ args[0]);

            while(!Thread.interrupted()){
                HttpServer httpServer = new HttpServer(serverSocket.accept());
                System.out.println("Connection Accepted: "+ new Date());
              
                Thread thread = new Thread(httpServer);
                thread.start();
            }

            serverSocket.close();

        }catch (IOException e){
            System.err.println("Connection error: "+e.getMessage());
        }

    }

    public void run() {

        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        PrintWriter printWriter = null;
        String httpMethod = null;
        String requestedFile = null;

        byte[] bytes = new byte[8192];

        int byteCount = 0;

        try {
            bufferedInputStream = new BufferedInputStream(socket.getInputStream());
            printWriter = new PrintWriter(socket.getOutputStream());
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());

            byteCount = bufferedInputStream.read(bytes);
            if(byteCount==-1){
                return;
            }
            String input="";
            input += new String(bytes, 0, byteCount, "UTF-8");

            StringTokenizer tokenizer = new StringTokenizer(input);
            httpMethod = tokenizer.nextToken().toUpperCase();
            System.out.println("http method: "+httpMethod);
            
           if(httpMethod.equals("OVER")){
              flag = true;
              socket.close();
              serverSocket.close();
              System.out.println("Socket connections are closed and server is shutdown");
              return;
            }

            requestedFile = tokenizer.nextToken().toLowerCase();

            System.out.println("requested file: "+requestedFile);

            
            if(!httpMethod.equals("GET") && !httpMethod.equals("PUT")){
                invalidRequest(printWriter);
            }
            else if(httpMethod.equals("GET")){
                getRequest(printWriter, bufferedOutputStream, requestedFile);
            }
            else if(httpMethod.equals("PUT")){
               putRequest(printWriter, bytes, byteCount, bufferedInputStream);
            }

        } catch (IOException e) {
            System.out.println("in or out failed"+e);

        }finally {
            try{
                bufferedInputStream.close();
                bufferedOutputStream.close();
                printWriter.close();
                socket.close();
            }catch (Exception e){
                System.out.println("Resources not closed properly: "+e.getMessage());
            }
        }

    }

}
