package cl.mercadonexo.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ENTIDAD — Categoría de productos.
 * Segunda entidad del modelo, relacionada con {@link Producto} mediante una
 * relación @ManyToOne unidireccional (una categoría agrupa muchos productos).
 */
@Entity
@Table(name = "categoria",
       uniqueConstraints = @UniqueConstraint(name = "uq_categoria_nombre", columnNames = "nombre"))
@NamedQuery(name = "Categoria.listar",
            query = "SELECT c FROM Categoria c ORDER BY c.nombre")
@NamedQuery(name = "Categoria.porNombre",
            query = "SELECT c FROM Categoria c WHERE c.nombre = :nombre")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Basic(optional = false)
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(name = "descripcion", length = 200)
    private String descripcion;

    public Categoria() {
    }

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Categoria{" + "id=" + id + ", nombre=" + nombre + '}';
    }
}
