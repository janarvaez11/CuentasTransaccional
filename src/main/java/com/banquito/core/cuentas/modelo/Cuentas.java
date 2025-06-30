package com.banquito.core.cuentas.modelo;

import jakarta.persistence.*;

import java.time.Instant;

import com.banquito.core.cuentas.enums.EstadoGeneralCuentasEnum;

@Entity
@Table(name = "cuentas", schema = "cuentastransaccional")
public class Cuentas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta", nullable = false)
    private Integer id;

    /** Antes: @ManyToOne -> TiposCuentas */
    @Column(name = "id_tipo_cuenta", nullable = false)
    private Integer tipoCuentaId;

    /** Antes: @ManyToOne -> TasasIntereses */
    @Column(name = "id_tasa_interes", nullable = false)
    private Integer tasaInteresId;

    @Column(name = "codigo_cuenta", nullable = false, length = 20)
    private String codigoCuenta;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private Instant fechaModificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoGeneralCuentasEnum estado = EstadoGeneralCuentasEnum.ACTIVO;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Cuentas() {
    }

    public Cuentas(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTipoCuentaId() {
        return tipoCuentaId;
    }

    public void setTipoCuentaId(Integer tipoCuentaId) {
        this.tipoCuentaId = tipoCuentaId;
    }

    public Integer getTasaInteresId() {
        return tasaInteresId;
    }

    public void setTasaInteresId(Integer tasaInteresId) {
        this.tasaInteresId = tasaInteresId;
    }

    public String getCodigoCuenta() {
        return codigoCuenta;
    }

    public void setCodigoCuenta(String codigoCuenta) {
        this.codigoCuenta = codigoCuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Instant getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Instant fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public EstadoGeneralCuentasEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoGeneralCuentasEnum estado) {
        this.estado = estado;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cuentas other = (Cuentas) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Cuentas [id=" + id + ", tipoCuentaId=" + tipoCuentaId + ", tasaInteresId=" + tasaInteresId
                + ", codigoCuenta=" + codigoCuenta + ", nombre=" + nombre + ", descripcion=" + descripcion
                + ", fechaCreacion=" + fechaCreacion + ", fechaModificacion=" + fechaModificacion + ", estado=" + estado
                + ", version=" + version + "]";
    }

}