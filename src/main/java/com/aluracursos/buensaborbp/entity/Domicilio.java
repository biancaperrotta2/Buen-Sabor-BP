package com.aluracursos.buensaborbp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Domicilio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_domicilio;

    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private String codigoPostal;

    @Column(nullable = true)
    private String piso;

    @Column(nullable = true)
    private String departamento;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;

    @Override
    public String toString() {
        return
                "id=" + id_domicilio +
                ", calle='" + calle + '\'' +
                ", alias='" + alias + '\'' +
                ", numero=" + numero +
                ", piso='" + piso + '\'' +
                ", departamento='" + departamento + '\'' +
                ", activo=" + activo +
                ", codigoPostal=" + codigoPostal +
                ", localidad=" + localidad.getNombre();
    }

    public static Domicilio fromSnapshotString(String snapshot) {
        Domicilio domicilio = new Domicilio();
        String[] parts = snapshot.split(",");

        for (String part : parts) {
            String[] keyValue = part.split("=");
            String key = keyValue[0].trim();
            String value = keyValue.length > 1 ? keyValue[1].trim() : "";

            switch (key) {
                case "id":
                    domicilio.setId_domicilio(Long.parseLong(value));
                    break;
                case "calle":
                    domicilio.setCalle(value.replace("'", ""));
                    break;
                case "numero":
                    domicilio.setNumero(Integer.parseInt(value));
                    break;
                case "codigoPostal":
                    domicilio.setCodigoPostal(value);
                    break;
                case "localidad":
                    Localidad localidad = new Localidad();
                    localidad.setNombre(value);
                    domicilio.setLocalidad(localidad);
                    break;
            }
        }
        return domicilio;
    }
}
