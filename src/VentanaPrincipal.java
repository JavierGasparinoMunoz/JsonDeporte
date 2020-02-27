import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VentanaPrincipal extends JFrame implements ActionListener, ListSelectionListener {
    JButton traerDatos;
    JLabel titulo, imagen;
    JList listaLigas;
    JTextArea listaDatos;
    DefaultListModel modeloLista;
    JPanel panelCentral, panelArriba, panelCentralCentro;
    Container container;

    public VentanaPrincipal() {
    }

    public void initGUI() {
        instancias();
        configurarContainer();
        acciones();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(new Dimension(1500, 600));
    }

    private void instancias() {
        container = this.getContentPane();
        traerDatos = new JButton("Cargar datos");
        titulo = new JLabel("Ligas España");
        imagen = new JLabel("");
        listaDatos = new JTextArea();
        modeloLista = new DefaultListModel();
        listaLigas = new JList(modeloLista);
        panelCentral = new JPanel();
        panelArriba = new JPanel();
        panelCentralCentro = new JPanel();
    }

    private void configurarContainer() {
        this.setLayout(new BorderLayout());
        this.add(configurarArriba(), BorderLayout.NORTH);
        this.add(configurarCentro(), BorderLayout.CENTER);
    }

    private JPanel configurarArriba() {
        panelArriba.setLayout(new FlowLayout());
        panelArriba.add(titulo, BorderLayout.NORTH);
        return panelArriba;
    }

    private JPanel configurarCentro() {
        panelCentral.setLayout(new BorderLayout());
        panelCentral.add(configurarCentroCentro(), BorderLayout.CENTER);
        panelCentral.add(traerDatos, BorderLayout.SOUTH);
        return panelCentral;
    }

    private JPanel configurarCentroCentro() {
        panelCentralCentro.setLayout(new GridLayout(1, 3));
        panelCentralCentro.add(new JScrollPane(listaLigas));
        panelCentralCentro.add(imagen);
        panelCentralCentro.add(new JScrollPane(listaDatos));
        return panelCentralCentro;
    }

    private void acciones() {
        traerDatos.addActionListener(this);
        listaLigas.addListSelectionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == traerDatos) {
            modeloLista.clear();
            new MiWorker().execute();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == listaLigas) {
            Ligas seleccionada = (Ligas) modeloLista.getElementAt(listaLigas.getSelectedIndex());
            String link = String.format("%s", seleccionada.getStrTeamBadge());
            String link2 = String.format("Nombre del equipo: " + "%s" + "%n%n Liga: " + "%s" + "%n%n Nombre del estadio: " + "%s" + "%n%n Descripcion: " + "%s", seleccionada.getStrTeam(),seleccionada.getStrLeague(),seleccionada.getStrStadium(),seleccionada.getStrDescriptionES());
            URL urlImagen,urlDatos;
            try {
                urlImagen = new URL(link);
                //urlDatos = new URL(link2);
                BufferedImage imagenInternet = ImageIO.read(urlImagen);
                //BufferedReader reader = text.read(urlDatos);
                imagen.setIcon(new ImageIcon(imagenInternet));
                listaDatos.setText(link2);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class MiWorker extends SwingWorker<Boolean, Void> {

        URL url;
        HttpURLConnection conexion;
        BufferedReader lector;
        StringBuilder stringBuilder = new StringBuilder();

        @Override
        protected Boolean doInBackground() throws Exception {
            try {
                url = new URL("https://www.thesportsdb.com/api/v1/json/1/search_all_teams.php?s=Soccer&c=Spain");
                conexion = (HttpURLConnection) url.openConnection();
                lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String linea;
            while ((linea = lector.readLine()) != null) {
                stringBuilder.append(linea);
            }

            JSONObject jsonEntero = new JSONObject(stringBuilder.toString());
            JSONArray jsonArrayResultados = jsonEntero.getJSONArray("teams");
            for (int i = 0; i < jsonArrayResultados.length(); i++) {
                if (i == 0) {
                    traerDatos.setEnabled(false);
                } else if (i == jsonArrayResultados.length() - 1) {
                    traerDatos.setEnabled(true);
                }
                JSONObject objeto = jsonArrayResultados.getJSONObject(i);
                Gson gson = new Gson();
                Ligas ligas = gson.fromJson(objeto.toString(), Ligas.class);
                modeloLista.addElement(ligas);
                Thread.sleep(100);
            }

            return true;
        }
    }
}



