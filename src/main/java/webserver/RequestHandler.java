package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;

    private static final String indexPath = "./webapp/index.html";
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

//            HTML Request Message
//            startLine 분석
            String startLine = br.readLine();
            System.out.println(startLine);
            String[] startLineEntity = startLine.split(" ");
            String method = startLineEntity[0];
            String url = startLineEntity[1];
            String version = startLineEntity[2];
            System.out.printf("Method : %s\nurl : %s\nversion : %s\n", method, url, version);

            byte[] body = "Hello Wrold".getBytes();

            if (method.equals("GET") && requestHomePage(url)) {
                try{
                    body = Files.readAllBytes(Paths.get(indexPath));
//                    body = Files.readAllBytes(new File(indexPath).toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private boolean requestHomePage(String target) {
        return target.equals("/") || target.equals("/index.html");
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}