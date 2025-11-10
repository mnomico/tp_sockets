package com.unluki;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {

    // IP y Puerto del Servidor Switch
    private static final String IP_SERVIDOR_CONMUTADOR = "localhost";
    // private static final String IP_SERVIDOR_SWITCH = "localhost"; // (Usar si pruebas en la misma PC)
    private static final int PUERTO_SERVIDOR_CONMUTADOR = 6969;

    public static void main(String[] args) {

        try (
                // Conectarse al servidor
                Socket socket = new Socket(IP_SERVIDOR_CONMUTADOR, PUERTO_SERVIDOR_CONMUTADOR);

                // Objetos Entradas y Salidas
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                // Crear objeto para la entrada de datos
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Conectado al Servidor Conmutador en " + IP_SERVIDOR_CONMUTADOR + ":" + PUERTO_SERVIDOR_CONMUTADOR);

            //  Nombre BD
            System.out.print("Ingrese el nombre de la Base de Datos (Biblioteca - Supermercado - despues lo ve): ");
            String dbName = scanner.nextLine();

            System.out.print("Ingrese la consulta SQL: ");
            String sqlQuery = scanner.nextLine();

            // Pasar a XML
            String peticionXml = "<query>\n" +
                    "  <database>" + dbName + "</database>\n" +
                    "  <sql>" + sqlQuery + "</sql>\n" +
                    "</query>";

            // Enviar peticion al servidor conmutador
            System.out.println("\nEnviando petición:\n" + peticionXml);
            out.writeUTF(peticionXml);

            // Respuesta del servidor
            String respuestaXml = in.readUTF();
            System.out.println("\nRespuesta recibida del Servidor:\n" + respuestaXml);

        } catch (UnknownHostException e) {
            System.err.println("Host desconocido: " + IP_SERVIDOR_CONMUTADOR);
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        }
    }
}
