import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xuyong on 17/7/9.
 */
public class ConcurrencyTest {

    public static Connection conn;
    public static List<Connection> conns = new ArrayList<>();
    public static HikariDataSource ds;

    public static void main(String[] args) {
        String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        String host = "localhost";
        String dbName = "BaseKG";
        String user = "root";
        String password = "fudan@188";
        String DB_URL = "jdbc:mysql://" + host + "/" + dbName + "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=utf-8";
        try {
            Class.forName(JDBC_DRIVER);
            for (int i = 0; i < 1; i++) {
                conn = DriverManager.getConnection(DB_URL, user, password);
                conns.add(conn);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(user);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
        //ds.setMaxStatementsPerConnection(0);
        //ds.setMaxStatements(0);
        //ds.setCheckoutTimeout(1000);

        System.out.println("start test");

        int count = 2000;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        long startTime = System.currentTimeMillis();   //获取开始时间
        for (int i = 0; i < count; i++)
            executorService.execute(new ConcurrencyTest().new Task(cyclicBarrier));

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("running time of " + count + " data:" + String.valueOf(endTime - startTime));
    }

    public class Task implements Runnable {
        private CyclicBarrier cyclicBarrier;

        public Task(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                //cyclicBarrier.await();
                long startTime = System.currentTimeMillis();   //获取开始时间
                //Connection conn = ds.getConnection();
                Connection conn = ds.getConnection();
                String sql = "select * from log where id= 1";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.execute();
                statement.close();
                conn.close();
                long endTime = System.currentTimeMillis();
                System.out.println(String.valueOf(endTime - startTime));
                // 测试内容
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}