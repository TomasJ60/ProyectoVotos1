package co.edu.unipiloto.proyectovotos.decisor;

public class ConteoVotos {
    private String projectName;
    private String localidad;
    private String tipoDeProyecto;
    private int yesVotes;
    private int noVotes;
    private int blankVotes;

    public ConteoVotos(String projectName, String localidad, String tipoDeProyecto, int yesVotes, int noVotes, int blankVotes) {
        this.projectName = projectName;
        this.localidad = localidad;
        this.tipoDeProyecto = tipoDeProyecto;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.blankVotes = blankVotes;
    }

    // Getters
    public String getProjectName() { return projectName; }
    public String getLocalidad() { return localidad; }
    public String getTipoDeProyecto() { return tipoDeProyecto; }
    public int getYesVotes() { return yesVotes; }
    public int getNoVotes() { return noVotes; }
    public int getBlankVotes() { return blankVotes; }

    public int getTotalVotos() {
        return yesVotes + noVotes + blankVotes;
    }
}



