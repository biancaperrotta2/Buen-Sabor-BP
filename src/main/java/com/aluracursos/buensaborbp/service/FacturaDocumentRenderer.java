package com.aluracursos.buensaborbp.service;

import com.aluracursos.buensaborbp.dto.ClienteBaseResponse;
import com.aluracursos.buensaborbp.dto.DetalleFacturaResponse;
import com.aluracursos.buensaborbp.dto.EmpresaResponse;
import com.aluracursos.buensaborbp.dto.FacturaFullResponse;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

// Arma la misma factura en dos formatos, a partir de los mismos datos (FacturaFullResponse):
// HTML para la previsualización en pantalla y PDF (con OpenPDF) para descargar/abrir en
// una pestaña nueva. Ninguno de los dos consulta la base directamente.
@Component
public class FacturaDocumentRenderer {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String renderizarHtml(FacturaFullResponse factura) {
        ClienteBaseResponse cliente = factura.cliente();
        EmpresaResponse empresa = factura.empresa();

        StringBuilder filas = new StringBuilder();
        for (DetalleFacturaResponse detalle : factura.detallesFactura()) {
            filas.append("<tr>")
                    .append("<td>").append(escapar(detalle.nombreArticulo())).append("</td>")
                    .append("<td style=\"text-align:right\">").append(detalle.cantidad()).append("</td>")
                    .append("<td style=\"text-align:right\">").append(formatoMoneda(detalle.precioUnitario())).append("</td>")
                    .append("<td style=\"text-align:right\">").append(formatoMoneda(detalle.subtotal())).append("</td>")
                    .append("</tr>");
        }

        return """
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: Arial, sans-serif; color: #222; }
                table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                th, td { border-bottom: 1px solid #ddd; padding: 6px 8px; }
                th { text-align: left; background: #f5f5f5; }
                .totales td { border: none; padding: 2px 8px; }
                .totales .label { text-align: right; }
                .totales .valor { text-align: right; width: 120px; }
                h1 { font-size: 18px; margin-bottom: 0; }
              </style>
            </head>
            <body>
              <h1>%s</h1>
              <p>%s</p>
              <p><strong>Factura N°:</strong> %s<br>
                 <strong>Fecha:</strong> %s</p>
              <p><strong>Cliente:</strong> %s<br>
                 <strong>Email:</strong> %s</p>
              <table>
                <thead>
                  <tr><th>Producto</th><th>Cantidad</th><th>Precio unitario</th><th>Subtotal</th></tr>
                </thead>
                <tbody>
                  %s
                </tbody>
              </table>
              <table class="totales">
                <tr><td class="label">Subtotal:</td><td class="valor">%s</td></tr>
                %s
                %s
                <tr><td class="label"><strong>Total:</strong></td><td class="valor"><strong>%s</strong></td></tr>
              </table>
              <p><strong>Forma de pago:</strong> %s</p>
            </body>
            </html>
            """.formatted(
                empresa != null ? escapar(empresa.nombre()) : "El Buen Sabor",
                empresa != null ? "CUIT: " + empresa.cuit() : "",
                factura.numeroFactura(),
                factura.fecha() != null ? factura.fecha().format(FORMATO_FECHA) : "",
                cliente != null ? escapar(cliente.nombre() + " " + cliente.apellido()) : "",
                cliente != null ? escapar(cliente.email()) : "",
                filas,
                formatoMoneda(factura.subtotal()),
                filaOpcional("Envío:", factura.envio()),
                filaOpcional("Descuento:", restaSiCorresponde(factura.descuento())),
                formatoMoneda(factura.total()),
                escapar(factura.metodoPago())
            );
    }

    public byte[] renderizarPdf(FacturaFullResponse factura) {
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        try {
            PdfWriter.getInstance(document, salida);
            document.open();

            EmpresaResponse empresa = factura.empresa();
            document.add(new Paragraph(empresa != null ? empresa.nombre() : "El Buen Sabor", fuenteTitulo));
            if (empresa != null) {
                document.add(new Paragraph("CUIT: " + empresa.cuit(), fuenteNormal));
            }
            document.add(new Paragraph("Factura N°: " + factura.numeroFactura(), fuenteNormal));
            document.add(new Paragraph("Fecha: " + (factura.fecha() != null ? factura.fecha().format(FORMATO_FECHA) : ""), fuenteNormal));
            document.add(Chunk.NEWLINE);

            ClienteBaseResponse cliente = factura.cliente();
            if (cliente != null) {
                document.add(new Paragraph("Cliente: " + cliente.nombre() + " " + cliente.apellido(), fuenteNormal));
                document.add(new Paragraph("Email: " + cliente.email(), fuenteNormal));
            }
            document.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4, 1, 2, 2});
            agregarEncabezado(tabla, "Producto", fuenteNegrita);
            agregarEncabezado(tabla, "Cantidad", fuenteNegrita);
            agregarEncabezado(tabla, "Precio unitario", fuenteNegrita);
            agregarEncabezado(tabla, "Subtotal", fuenteNegrita);

            for (DetalleFacturaResponse detalle : factura.detallesFactura()) {
                tabla.addCell(new Phrase(detalle.nombreArticulo(), fuenteNormal));
                tabla.addCell(celdaDerecha(String.valueOf(detalle.cantidad()), fuenteNormal));
                tabla.addCell(celdaDerecha(formatoMoneda(detalle.precioUnitario()), fuenteNormal));
                tabla.addCell(celdaDerecha(formatoMoneda(detalle.subtotal()), fuenteNormal));
            }
            document.add(tabla);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Subtotal: " + formatoMoneda(factura.subtotal()), fuenteNormal));
            if (esPositivo(factura.envio())) {
                document.add(new Paragraph("Envío: " + formatoMoneda(factura.envio()), fuenteNormal));
            }
            if (esPositivo(factura.descuento())) {
                document.add(new Paragraph("Descuento: -" + formatoMoneda(factura.descuento()), fuenteNormal));
            }
            document.add(new Paragraph("Total: " + formatoMoneda(factura.total()), fuenteNegrita));
            document.add(new Paragraph("Forma de pago: " + factura.metodoPago(), fuenteNormal));

            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", e);
        }

        return salida.toByteArray();
    }

    private void agregarEncabezado(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(new java.awt.Color(245, 245, 245));
        tabla.addCell(celda);
    }

    private PdfPCell celdaDerecha(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return celda;
    }

    private boolean esPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal restaSiCorresponde(BigDecimal descuento) {
        return esPositivo(descuento) ? descuento : null;
    }

    private String filaOpcional(String etiqueta, BigDecimal valor) {
        if (!esPositivo(valor)) {
            return "";
        }
        return "<tr><td class=\"label\">%s</td><td class=\"valor\">%s</td></tr>".formatted(etiqueta, formatoMoneda(valor));
    }

    private String formatoMoneda(BigDecimal valor) {
        return valor == null ? "$ 0,00" : "$ " + valor.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
