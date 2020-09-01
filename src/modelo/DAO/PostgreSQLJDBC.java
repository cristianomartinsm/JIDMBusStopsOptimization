package modelo.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLJDBC {
	private static Connection connection;
	
	private static Connection conecta() {
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bdg", "cristiano", "cristiano");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Erro ao criar a conexão!");
		}

		return connection;
	}

	public static void fechaConexao() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Erro ao fechar a conexão!");
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				connection = conecta();
			}
		} catch (SQLException e) {
			System.err.println("Erro ao conferir a conexão!");
		}
		return connection;
	}
	
	
}