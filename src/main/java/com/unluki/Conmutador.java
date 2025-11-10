package com.unluki;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;


public class Conmutador {

    public static void main(String[] args) {
        Conmutador servidor = new Conmutador();
        servidor.crearSocket();
    }

    public void crearSocket() {
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
        }
    }

    private record ClientHandler(Socket socket) implements Runnable {

        @Override
            public void run() {
                try (DataInputStream in = new DataInputStream(socket.getInputStream());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
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
    
            private String consultarServidor(String nombreDB, String consultaSql) {

                BD baseDeDatos = null;
                switch (nombreDB) {
                    case "biblioteca":
                        baseDeDatos = new FireBird();
                        break;
                    case "supermercado":
                        baseDeDatos = new PostgreSQL();
                        break;
                    case "despues se fija":
                        System.out.println("algo 3");
                        break;
                }
                if (baseDeDatos == null) {
                    return "Error: no se pudo encontrar la base de datos.";
                }

                System.out.println(baseDeDatos.comprobarConexion(consultaSql));
                FormatoRespuesta xml = new XML();
                String output = "";
                try {
                    consultaSql = consultaSql.trim();
                    if (consultaSql.isEmpty()) {
                        output = "Error: Consulta inválida.";
                    } else if (consultaSql.toUpperCase().startsWith("SELECT")) {
                        output = xml.format(baseDeDatos.ejecutarSELECT(consultaSql));
                    } else if (consultaSql.toUpperCase().startsWith("INSERT") ||
                            consultaSql.toUpperCase().startsWith("UPDATE") ||
                            consultaSql.toUpperCase().startsWith("DELETE")) {
                        output = baseDeDatos.ejecutarDML(consultaSql);
                    }
                } catch (SQLException ex) {
                    output = "Error SQL: " + ex;
                }
                return output;
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