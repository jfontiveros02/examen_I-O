package pio.daw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {

        ArrayList<Radionuclido> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("/residuos.csv"))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] datos = linea.split(";");

                String id = datos[0];
                String isotopo = datos[1];
                double actEsp = Double.parseDouble(datos[2]);
                double masa = Double.parseDouble(datos[3]);
                LocalDateTime fecha = LocalDateTime.parse(datos[4]);

                Radionuclido rn = new Radionuclido(id, isotopo, actEsp, masa, fecha);
                lista.add(rn);
            }

        } catch (IOException e) {
            System.out.println("Error al leer: " + e);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("facturaResiduos.txt"))) {
            for (Radionuclido ob : lista) {
                pw.println(ob.toFactura());
                pw.println();
            }
        } catch (IOException e) {
            System.out.println("Error de escritura: " + e);
        }

        System.out.println("Lista: " + lista.size());
    }
}
