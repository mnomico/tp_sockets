package com.unluki;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Conmutador {
    public static void main(String[] args){
        try (ServerSocket serverSocket = new ServerSocket(6969)){
            System.out.println("Se inicio el servidor en el puerto 6969");
            while (true){
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado con la ip : " + clienteSocket.getInetAddress());

                Thread clientHandlerThread = new Thread(new ClientHandler(clienteSocket));
                clientHandlerThread.start();

            }
        } catch (IOException e){
            System.out.println("no se pudo iniciar el servidor");
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream()))
            {
                String peticionXml = in.readUTF();
                System.out.println("Petición recibida:\n" + peticionXml);

                String nombreDB = parsearTag(peticionXml, "database");
                String consultaSql = parsearTag(peticionXml, "sql");

                String respuestaXml = consultarServidor(nombreDB, consultaSql);

                System.out.println("respuesta:\n" + respuestaXml);
                out.writeUTF(respuestaXml);


            } catch (Exception e) {
                System.out.println("No se consultar el servidor");
            }
        }

        private String consultarServidor(String nombreDB, String consultaSql){

            switch (nombreDB){
                case "biblioteca" :
                    return FireBird.ejecutarConsulta(consultaSql);
                case "supermercado" :
                    System.out.println("algo 2");
                case "despues se fija" :
                    System.out.println("algo 3");
            }

            return "";
        }


        private String parsearTag(String xml, String tagName) {
            try {
                String startTag = "<" + tagName + ">";
                String endTag = "</" + tagName + ">";
                int start = xml.indexOf(startTag) + startTag.length();
                int end = xml.indexOf(endTag);
                return xml.substring(start, end);
            } catch (Exception e) {
                return "";
            }
        }
    }
}