package FactureApp;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;

public class FactureApp extends JFrame {

    private JTextField clienteField;
    private JTextField[] productoFields = new JTextField[3];
    private JTextField[] valorUnitarioFields = new JTextField[3];
    private JTextField[] cantidadFields = new JTextField[3];
    private JTextField[] valorTotalProductoFields = new JTextField[3];  // Ahora JTextField en lugar de JLabel
    private JTextField valorTotalFacturaField;  // JTextField para total factura
    private JLabel mensajeFinalLabel;

    public FactureApp() {
        super("ORDEN DE COMPRA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cliente
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(new JLabel("Cliente:"), gbc);

        clienteField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(clienteField, gbc);

        // Encabezados
        String[] labels = {"Producto", "Vr. Unitario", "Cantidad", "Vr. Total"};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JLabel hdr = new JLabel(labels[i], SwingConstants.CENTER);
            hdr.setFont(new Font("Arial", Font.BOLD, 12));
            add(hdr, gbc);
        }

        // Filas productos
        for (int i = 0; i < 3; i++) {
            productoFields[i] = new JTextField(15);
            gbc.gridx = 0;
            gbc.gridy = 2 + i;
            add(productoFields[i], gbc);

            valorUnitarioFields[i] = new JTextField(10);
            gbc.gridx = 1;
            add(valorUnitarioFields[i], gbc);

            cantidadFields[i] = new JTextField(10);
            gbc.gridx = 2;
            add(cantidadFields[i], gbc);

            valorTotalProductoFields[i] = new JTextField(10);
            valorTotalProductoFields[i].setEditable(false);
            valorTotalProductoFields[i].setHorizontalAlignment(JTextField.RIGHT);
            valorTotalProductoFields[i].setText("$ 0");
            gbc.gridx = 3;
            add(valorTotalProductoFields[i], gbc);
        }

        // Botón calcular y generar pdf
        JButton calcularButton = new JButton("CALCULAR Y GENERAR PDF");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(calcularButton, gbc);

        // Campo total factura
        valorTotalFacturaField = new JTextField(10);
        valorTotalFacturaField.setEditable(false);
        valorTotalFacturaField.setHorizontalAlignment(JTextField.RIGHT);
        valorTotalFacturaField.setText("$ 0");
        valorTotalFacturaField.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;
        add(valorTotalFacturaField, gbc);

        // Mensaje final
        mensajeFinalLabel = new JLabel(" ");
        mensajeFinalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(mensajeFinalLabel, gbc);

        // Acción botón
        calcularButton.addActionListener(e -> calcularYGenerarPDF());

        pack();
    }

    private void calcularYGenerarPDF() {
        double totalFactura = 0;
        String cliente = clienteField.getText().trim().toUpperCase();

        String[][] productosData = new String[3][4];

        for (int i = 0; i < 3; i++) {
            try {
                String nombre = productoFields[i].getText().trim();
                double valorUnitario = Double.parseDouble(valorUnitarioFields[i].getText().trim());
                int cantidad = Integer.parseInt(cantidadFields[i].getText().trim());
                double totalProducto = valorUnitario * cantidad;

                totalFactura += totalProducto;
                valorTotalProductoFields[i].setText(String.format("$ %,d", (int) totalProducto));

                productosData[i][0] = nombre;
                productosData[i][1] = String.format("$ %,d", (int) valorUnitario);
                productosData[i][2] = String.valueOf(cantidad);
                productosData[i][3] = String.format("$ %,d", (int) totalProducto);
            } catch (NumberFormatException ex) {
                valorTotalProductoFields[i].setText("Error");
                JOptionPane.showMessageDialog(this, "Error en los datos del producto " + (i + 1), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        valorTotalFacturaField.setText(String.format("$ %,d", (int) totalFactura));
        mensajeFinalLabel.setText("Señor(a) " + cliente + " el total de su compra es: " + String.format("$ %,d", (int) totalFactura));

        generarPDF(cliente, productosData, totalFactura);
    }

    private void generarPDF(String cliente, String[][] productos, double totalFactura) {
        try {
            Document documento = new Document();
            String fechaArchivo = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String nombreArchivo = "Factura_" + cliente.replace(" ", "_") + "_" + fechaArchivo + ".pdf";
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));

            documento.open();

            // Encabezado con fecha
            Paragraph titulo = new Paragraph(
                    "ORDEN DE COMPRA\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)
            );
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph("Cliente: " + cliente));
            documento.add(new Paragraph(" "));

            // Tabla distribuida
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4f, 2f, 2f, 2f});
            tabla.setSpacingBefore(10);
            tabla.setSpacingAfter(10);

            com.itextpdf.text.Font fuenteTabla = FontFactory.getFont(FontFactory.HELVETICA, 10);

            String[] headers = {"Producto", "Vr. Unitario", "Cantidad", "Vr. Total"};
            for (String header : headers) {
                PdfPCell celda = new PdfPCell(new Paragraph(header, fuenteTabla));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(celda);
            }

            for (int i = 0; i < productos.length; i++) {
                for (int j = 0; j < 4; j++) {
                    String contenido = productos[i][j] != null ? productos[i][j] : "";
                    PdfPCell celda = new PdfPCell(new Paragraph(contenido, fuenteTabla));
                    celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tabla.addCell(celda);
                }
            }

            documento.add(tabla);

            // Total final en negrita alineado a la derecha
            com.itextpdf.text.Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph totalFinal = new Paragraph("Valor total de la factura: " + String.format("$ %,d", (int) totalFactura), fuenteNegrita);
            totalFinal.setAlignment(Element.ALIGN_RIGHT);
            totalFinal.setSpacingBefore(10);
            totalFinal.setSpacingAfter(10);
            documento.add(totalFinal);

            // Mensaje de agradecimiento centrado
            Paragraph gracias = new Paragraph("Gracias por su compra, " + cliente + ".");
            gracias.setAlignment(Element.ALIGN_CENTER);
            gracias.setSpacingBefore(10);
            documento.add(gracias);

            documento.close();

            JOptionPane.showMessageDialog(this, "Factura generada exitosamente:\n" + nombreArchivo);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar PDF: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FactureApp ventana = new FactureApp();
            ventana.setVisible(true);
        });
    }
}
