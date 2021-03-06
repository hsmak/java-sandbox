package jdbc.connectionpool.legacy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPoolLegacy {

    private final String urlMysql = "jdbc:mysql://localhost:3306/ConnectionPool_db";
    private final String urlDerby = "jdbc:derby:memory:ConnectionPool_db;create=true";

    private List<Connection> l;// how about using Deque!
    // private Deque<Connection> d;
    private int connections;

    /*
     * Static Block: loading the JDBC driver we need to do this only one time
     */
    static {

        /**
         * Note: Class.forName() is no longer necessary as of JDBC 4.0 since Drivers seem to self-register with the DriverManager automatically.
         */
        try {

//            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            System.exit(0);

        }
    }

    public ConnectionPoolLegacy(int connections) {
        this.connections = connections;

        l = new ArrayList<>(connections);

        int i = this.connections;
        while (i-- != 0) {

            l.add(addConnections(
                    urlDerby, "java",
                    "java"));
        }
    }

    private Connection addConnections(String url, String user, String password) {

        // System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;

        try {

            connection = DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            System.exit(0);

        }

        if (connection != null) {

            // System.out.println("You made it, take control your database now!");

        } else {

            System.out.println("Failed to make connection!");

        }

        return connection;
    }

    public int getSize() {
        return this.connections;
    }

    private boolean isEmpty() {
        if (this.connections == 0) {
            return true;
        }

        return false;
    }

    // public synchronized Connection pullConnection() {
    public Connection pullConnection() {

        synchronized (l) {

            System.out.println("Entering.." + Thread.currentThread().toString());

            while (isEmpty()) {

                try {
                    System.out.println("Waiting for a connection! "
                            + Thread.currentThread().toString());
                    // wait();
                    l.wait();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Granted.." + Thread.currentThread().toString());

            this.connections--;
            return l.remove(0);
        }
    }

    //	public synchronized void pushConnection(Connection c) {
    public void pushConnection(Connection c) {
        synchronized (l) {

            this.connections++;
            l.add(c);
            // notifyAll();
            l.notifyAll();
        }
    }

    public void shutdown(Connection c) throws SQLException {
        l.remove(c);
        c.close();
        this.connections--;
    }

    public void shutdownAll() throws SQLException {

        while (this.connections-- != 0) {
            Connection c = l.remove(this.connections);
            c.close();
        }
    }

    /*
     * this must not be exposed to outside world
     */
    public List<Connection> getConnectionList() {
        return this.l;
    }

}
