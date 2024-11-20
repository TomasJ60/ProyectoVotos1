package co.edu.unipiloto.proyectovotos.votos;

public class Proyecto {
    private String proyectoId;
    private String id;
    private String titulo;
    private String descripcion;
    private String direccion;
    private String entidad;
    private String nombrePlaneador;

    // Constructor
    public Proyecto(String id, String titulo, String descripcion, String direccion, String entidad) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.entidad = entidad;
    }

    public Proyecto(String proyectoId, String titulo) {
        this.proyectoId = proyectoId;
        this.titulo = titulo;
        this.nombrePlaneador = nombrePlaneador;
    }

    public Proyecto() {
    }

    //proyecto
    public String getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(String proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getNombrePlaneador() {
        return nombrePlaneador;
    }


    // Getters
    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEntidad() {
        return entidad;
    }
}

