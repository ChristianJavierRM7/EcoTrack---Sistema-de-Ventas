package Entidades;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "proveedor")
@NamedQueries({
    @NamedQuery(name = "Proveedor.findAll", query = "SELECT p FROM Proveedor p"),
    @NamedQuery(name = "Proveedor.findById", query = "SELECT p FROM Proveedor p WHERE p.id = :id"),
    @NamedQuery(name = "Proveedor.findByRuc", query = "SELECT p FROM Proveedor p WHERE p.ruc = :ruc"),
    @NamedQuery(name = "Proveedor.findByNombre", query = "SELECT p FROM Proveedor p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Proveedor.findByTelefono", query = "SELECT p FROM Proveedor p WHERE p.telefono = :telefono"),
    @NamedQuery(name = "Proveedor.findByDireccion", query = "SELECT p FROM Proveedor p WHERE p.direccion = :direccion"),
    @NamedQuery(name = "Proveedor.findByRazon", query = "SELECT p FROM Proveedor p WHERE p.razon = :razon"),
    @NamedQuery(name = "Proveedor.findByCorreo", query = "SELECT p FROM Proveedor p WHERE p.correo = :correo"),
    @NamedQuery(name = "Proveedor.findByEstado", query = "SELECT p FROM Proveedor p WHERE p.estado = :estado"),
    @NamedQuery(name = "Proveedor.findByFecha", query = "SELECT p FROM Proveedor p WHERE p.fecha = :fecha")
})
public class Proveedor implements Serializable {

    private static final long serialVersionUID = 1L;

    // ➤ ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // ➤ RUC
    @Basic(optional = false)
    @NotNull
    @Column(name = "ruc")
    private int ruc;

    // ➤ Nombre
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "nombre")
    private String nombre;

    // ➤ Teléfono
    @Basic(optional = false)
    @NotNull
    @Column(name = "telefono")
    private int telefono;

    // ➤ Dirección
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "direccion")
    private String direccion;

    // ➤ Razón social
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "razon")
    private String razon;

    // ➤ Correo electrónico (NUEVO)
    @Size(max = 100)
    @Column(name = "correo")
    private String correo;

    // ➤ Estado (NUEVO) - Activo / Inactivo
    @Size(max = 20)
    @Column(name = "estado")
    private String estado;

    // ➤ Observaciones (NUEVO)
    @Column(name = "observaciones")
    private String observaciones;

    // ➤ Fecha registro
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    public Proveedor() {}

    public Proveedor(Integer id) {
        this.id = id;
    }

    public Proveedor(Integer id, int ruc, String nombre, int telefono,
                     String direccion, String razon, Date fecha) {
        this.id = id;
        this.ruc = ruc;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.razon = razon;
        this.fecha = fecha;
    }

    // =========================
    //        GETTERS & SETTERS
    // =========================

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getRuc() { return ruc; }
    public void setRuc(int ruc) { this.ruc = ruc; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getTelefono() { return telefono; }
    public void setTelefono(int telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getRazon() { return razon; }
    public void setRazon(String razon) { this.razon = razon; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Proveedor)) return false;
        Proveedor other = (Proveedor) object;
        return !((this.id == null && other.id != null) 
                || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "Entidades.Proveedor[ id=" + id + " ]";
    }
}
