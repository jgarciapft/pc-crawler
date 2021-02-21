import java.io.IOException;
import java.sql.*;

/*
 * PRÁCTICA 1
 *
 * Autor: Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */

public class PbdJDBC {

    private static Connection conexion = null; // Referencia a la conexión con la instancia de Oracle Database

    /**
     * Inicializa la conexión con la instancia de Oracle Database
     *
     * @return Si se pudo establer la conexión con éxito
     */
    public boolean dbConectar() {

        System.out.println("---dbConectar---");

        // Detalles de la conexion a la base de datos (VALORES DE REFERENCIA PROPORCIONADOS POR EL PROFESOR)

        String driver = "oracle.jdbc.OracleDriver";
        String servidor = "localhost"; // Direccion IP
        String puerto = "1521"; // Puerto
        String sid = "xe"; // Identificador del servicio o instancia
        String url = "jdbc:oracle:thin:@//" + servidor + ":" + puerto + "/" + sid;
        String usuario = "system"; // usuario 
        String contrasena = "12345"; // contrasena 

        // ESTABLER LA CONEXIÓN

        try {
            System.out.println("---Conectando a Oracle---");

            // Cargar el driver JDBC para Oracle
            Class.forName(driver);
            conexion = DriverManager.getConnection(url, usuario, contrasena);

            // Las transacciones se aplican automáticamente
            conexion.setAutoCommit(true);
            System.out.println("AUTO COMMIT: " + (conexion.getAutoCommit() ? "ON" : "OFF"));

            System.out.println("Conexión realizada a la base de datos " + conexion);
            return true;
        } catch (ClassNotFoundException e) { // FALLO No se ha encontrado el driver JDBC para Oracle
            e.printStackTrace();
            return false;
        } catch (SQLException e) { // FALLO No se ha podido conectar a la BD
            e.printStackTrace();
            return false;
        }
    }

    /* ------------------------------------------------------------------ */

    /**
     * Desconecta la conexión con la instancia de Oracle Database. Para invocar este método la conexión debe haberse
     * inicializado previamente
     *
     * @return Si la desconexión se realizó con éxito
     */
    public boolean dbDesconectar() {

        System.out.println("---dbDesconectar---");

        // CERRAR LA CONEXIÓN

        try {
            conexion.close();
            System.out.println("Desconexión realizada correctamente");
            return true;
        } catch (SQLException e) { // FALLO Algo fue mal al desconectar la conexión
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {

        PbdJDBC cliente = new PbdJDBC();
        System.out.println("---Programa principal---");

        // Conectarse a la base de datos

        if (!cliente.dbConectar())
            System.out.println("Fallo: Conexion no realizada.");

        // EJECUTAR PRUEBAS

        cliente.dbObtenerEmpleados1(); // Probar con DNI: 987654321
        cliente.dbObtenerEmpleados2(); // Probar con DNI: 987654321

        cliente.dbConsultarEmpleados();
        cliente.dbConsultarDepartamentos();

        cliente.dbInsertarDepartamentos(); // Probar con: { nombreDpto = INVESTIGACIÓN, número = 5 }
        cliente.dbConsultarDepartamentos();

        cliente.dbModificarDepartamentos(); // Modificar departamento 5: Cambiar nombre a DESCONOCIDO
        cliente.dbConsultarDepartamentos();

        cliente.dbBorrarDepartamentos(); // Borrar departamento 5
        cliente.dbConsultarDepartamentos();

        // Desconectarse de la base de datos

        if (!cliente.dbDesconectar())
            System.out.println("Desconexión no realizada");

        System.out.println("---Fin de programa---");
    }
}
