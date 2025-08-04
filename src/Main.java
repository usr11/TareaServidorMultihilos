import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {

    //Creamos nuestro ServerSocket
    //Entramos a un bucle para esperar conexiones de clientes
    //Por cada cliente conectado, acepta el socket y se maneja con un hilo aparte
    public void init() throws IOException {

        var server = new ServerSocket(8080);
        var isAlive = true;
        while (isAlive) {
            System.out.println("Esperando un cliente...");
            var socket = server.accept();
            System.out.println("Cliente Conectado.");
            dispatchWorker(socket);
        }

    }

    //Creamos un hilo para cada cliente y después llamamos al metodo que maneja el request
    public void dispatchWorker(Socket socket) throws IOException {

        new Thread(
                ()-> {
                    try {
                        handleRequest(socket);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).start();

    }

    //Usamos un BufferedReader para leer línea por línea del InputStream del socket.
    //Si la línea empieza con un GET extraemos el nombre del documento y llamaos a sendResponse()
    public void handleRequest(Socket socket) throws Exception {

        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if(line.startsWith("GET")) {
                System.out.println(line);
                var resource = line.split(" ")[1].replaceAll("/", "");
                System.out.println("El cliente esta pendiente: " + resource);

                //Enviar response
                sendResponse(socket, resource);
            }
        }
    }

    //
    private void sendResponse(Socket socket, String resource) throws Exception {

        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        String notFoundResource = "notFound.html";

        var file = new File("");
        System.out.println(file.getAbsolutePath());

        var res = new File("resources/" +  resource);
        var notFound = new File("resources/" + notFoundResource);

        if(res.exists()) {
            var ct = contentType(resource);
            long length  = res.length();

            var fis  = new FileInputStream(res);

            //System.out.println(login.exists());

            enviarString(("HTTP/1.0 200 OK\r\n"), out);
            enviarString(("Content-Type: " + ct + "\r\n"), out);
            enviarString(("Content-Length: "+ length +"\r\n"), out);
            enviarString(("Connection: close\r\n"), out);
            enviarString(("\r\n"), out);

            enviarBytes(fis, out);

            fis.close();
            out.flush();


        } else {

            var ct = contentType(notFoundResource);
            var length = notFound.length();

            var fis  = new FileInputStream(notFound);

            //System.out.println("No se encontró el archivo.");
            enviarString(("HTTP/1.0 404 Not Found\r\n"),  out);
            enviarString(("Content-Type: " + ct + "\r\n"), out);
            enviarString(("Content-Length: "+ length +"\r\n"), out);
            enviarString(("Connection: close\r\n"), out);
            enviarString(("\r\n"), out);

            enviarBytes(fis, out);

            fis.close();
            out.flush();

        }

        out.close();
        socket.close();

    }

    private static void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }


    public static String contentType(String fileName) {

        var res = "application/octet-stream";

        if (fileName.endsWith(".htm") ||  fileName.endsWith(".html")) {
            res = "text/html";
        }
        if (fileName.endsWith(".jpg")){
            res = "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            res = "image/gif";
        }
        return res;
    }


    //Nuestra llamada al init() para arrancar el servidor
    public static void main(String[] args) throws IOException {

        Main main = new Main();
        main.init();

    }
}