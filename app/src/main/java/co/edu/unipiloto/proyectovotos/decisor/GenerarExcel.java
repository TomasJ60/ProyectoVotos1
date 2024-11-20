package co.edu.unipiloto.proyectovotos.decisor;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Font;

import co.edu.unipiloto.proyectovotos.Homes.HomeAdmin;
import co.edu.unipiloto.proyectovotos.R;

public class GenerarExcel extends AppCompatActivity {

    private Button btnGenerateExcel, btnVerProyectos, btnconteo;
    private FirebaseFirestore db;
    private static final int REQUEST_PERMISSION_WRITE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_excel);

        btnGenerateExcel = findViewById(R.id.btnGenerateExcel);
        btnVerProyectos = findViewById(R.id.btnVerProyectos);
        btnconteo = findViewById(R.id.conteo);
        db = FirebaseFirestore.getInstance();

        // Solicitar permisos de escritura
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
        }

        btnGenerateExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateExcelFile();
            }
        });
        btnVerProyectos.setOnClickListener(new View.OnClickListener() { // Acción del nuevo botón
            @Override
            public void onClick(View v) {
                // Cambia VerProyectosActivity.class al nombre de la actividad que debe abrir
                Intent intent = new Intent(GenerarExcel.this, HomeAdmin.class);
                startActivity(intent);
            }
        });

        btnconteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GenerarExcel.this, conteodevotosActivity.class);
                startActivity(intent);
            }
        });
    }

    private void generateExcelFile() {
        db.collection("registroVotacion")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, int[]> projectVotes = new HashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String projectId = document.getString("ProyectoVoto");
                            String voto = document.getString("voto");

                            if (projectId != null && voto != null) {
                                int[] votes = projectVotes.getOrDefault(projectId, new int[4]); // {sí, no, blanco, total}

                                switch (voto.toLowerCase()) {
                                    case "si":
                                        votes[0]++;
                                        break;
                                    case "no":
                                        votes[1]++;
                                        break;
                                    case "en blanco":
                                        votes[2]++;
                                        break;
                                }
                                votes[3]++; // Total de votos
                                projectVotes.put(projectId, votes);
                            }
                        }

                        writeExcelFile(projectVotes);
                    } else {
                        Toast.makeText(GenerarExcel.this, "Error al obtener datos de Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void writeExcelFile(Map<String, int[]> projectVotes) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Votos por Proyecto");

        // Configurar el estilo para el encabezado
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Configurar el estilo para las celdas de datos
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Establecer el ancho de las columnas
        sheet.setColumnWidth(0, 6000); // Proyecto
        sheet.setColumnWidth(1, 4000); // Votos Sí
        sheet.setColumnWidth(2, 4000); // Votos No
        sheet.setColumnWidth(3, 5000); // Votos en Blanco
        sheet.setColumnWidth(4, 4000); // Total Votos

        // Crear la fila de encabezado
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Proyecto");
        header.createCell(1).setCellValue("Votos Sí");
        header.createCell(2).setCellValue("Votos No");
        header.createCell(3).setCellValue("Votos en Blanco");
        header.createCell(4).setCellValue("Total Votos");

        // Aplicar estilo a las celdas de encabezado
        for (int i = 0; i <= 4; i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (Map.Entry<String, int[]> entry : projectVotes.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey()); // Nombre del proyecto
            row.createCell(1).setCellValue(entry.getValue()[0]); // Votos Sí
            row.createCell(2).setCellValue(entry.getValue()[1]); // Votos No
            row.createCell(3).setCellValue(entry.getValue()[2]); // Votos en Blanco
            row.createCell(4).setCellValue(entry.getValue()[3]); // Total Votos

            // Aplicar el estilo de datos a las celdas de cada fila
            for (int i = 0; i <= 4; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory(), "ReporteVotos.xlsx");
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            Toast.makeText(this, "Archivo Excel generado exitosamente en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al escribir el archivo Excel", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos otorgados", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de escritura es necesario para generar el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
