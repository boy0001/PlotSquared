package com.intellectualcrafters.plot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

/**
 * Connects to and uses a MySQL database
 *
 * @author -_Husky_-
 * @author tips48
 */
public class MySQL extends Database {
	private final String user;
	private final String database;
	private final String password;
	private final String port;
	private final String hostname;

	private Connection connection;

	/**
	 * Creates a new MySQL instance
	 * 
	 * @param plugin
	 *            Plugin instance
	 * @param hostname
	 *            Name of the host
	 * @param port
	 *            Port number
	 * @param database
	 *            Database name
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 */
	public MySQL(Plugin plugin, String hostname, String port, String database, String username, String password) {
		super(plugin);
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.user = username;
		this.password = password;
		this.connection = null;
	}

	@Override
	public Connection openConnection() throws SQLException, ClassNotFoundException {
		if (checkConnection()) {
			return this.connection;
		}
		Class.forName("com.mysql.jdbc.Driver");
		this.connection =
				DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user, this.password);
		return this.connection;
	}

	@Override
	public boolean checkConnection() throws SQLException {
		return (this.connection != null) && !this.connection.isClosed();
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	@Override
	public boolean closeConnection() throws SQLException {
		if (this.connection == null) {
			return false;
		}
		this.connection.close();
		return true;
	}

	@Override
	public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
		if (checkConnection()) {
			openConnection();
		}

		Statement statement = this.connection.createStatement();

		ResultSet result = statement.executeQuery(query);

		return result;
	}

	@Override
	public int updateSQL(String query) throws SQLException, ClassNotFoundException {
		if (checkConnection()) {
			openConnection();
		}

		Statement statement = this.connection.createStatement();

		int result = statement.executeUpdate(query);

		return result;
	}

}
