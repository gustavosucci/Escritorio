package org.infor;
/*
Put header here


 */

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ResourceBundle;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class FXMLController implements Initializable {

    @FXML
    private Label lblOut;
    @FXML
    private TableView<Docente> tabla;
    @FXML
    private TableColumn<Docente,String> nombre;
    @FXML
    private TableColumn<Docente,String> correo;
    @FXML
    private TableColumn<Docente,LocalDate> fecha;
    
    @FXML
    private void btnClickAction(ActionEvent event) throws ParseException {
        tabla.setItems(getData());
    }

    @FXML
    private void eliminar(ActionEvent event) throws org.json.simple.parser.ParseException {
        Docente sel = tabla.getSelectionModel().getSelectedItem();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/docentes/"+sel.getId().toString()))
        .timeout(Duration.ofMinutes(2)).header("Content-Type", "application/json")
        .DELETE().build();

        String Respuesta =client.sendAsync(request, BodyHandlers.ofString())
        .thenApply(HttpResponse::body).join();

        if(Respuesta.isEmpty()) System.out.println("Borrado");
        else System.out.println("Hubo un error");
    }
    @FXML
    private void agregar(ActionEvent event) throws IOException {
        Stage stageTheLabelBelongs = (Stage) lblOut.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/agregar.fxml"));
        Parent pane = fxmlLoader.load();
        stageTheLabelBelongs.getScene().setRoot(pane);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nombre.setCellValueFactory(cellData -> cellData.getValue().NameProperty());
        correo.setCellValueFactory(cellData -> cellData.getValue().CorreoProperty());
        fecha.setCellValueFactory(cellData -> cellData.getValue().birthdayProperty());
    }
    
    private ObservableList<Docente> getData() throws ParseException {
        ObservableList<Docente> Data = FXCollections.observableArrayList();
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/docentes"))
        .timeout(Duration.ofMinutes(2))
        .header("Content-Type", "application/json")
        .build();

        String Respuesta = client.sendAsync(request, BodyHandlers.ofString())
        .thenApply(HttpResponse::body).join();

        try {
            JSONObject Datos = (JSONObject) new JSONParser().parse(Respuesta);
            Datos = (JSONObject) Datos.get("_embedded");
            JSONArray Lista = (JSONArray) Datos.get("docentes");
            for (int i=0;i<Lista.size();i++) {
                JSONObject Item = (JSONObject) Lista.get(i);
                String cod = ((JSONObject) ((JSONObject) Item.get("_links")).get("self")).get("href").toString();
                System.out.println(Item);System.out.println(cod);

                Data.add( 
                    new Docente(Long.parseLong(cod.substring(cod.lastIndexOf("/")+1, cod.length())),
                        Item.get("nombre").toString(),Item.get("email").toString(),
                    LocalDate.parse(Item.get("nacimiento").toString().substring(0, 10)))
                );
            }
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return Data;
    }
}
