package com.urpay.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import oracle.jdbc.driver.OracleDriver;

/**
 * Standalone MQ Job Runner for MTO Code Table Refresh Jobs.
 *
 * Triggers IBM MQ jobs and verifies DB updates on EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV.
 *
 * Configuration via mq-job-config.properties:
 * <pre>
 * env=SIT
 *
 * # MQ SIT
 * mq.sit.host=192.168.100.67
 * mq.sit.port=1456
 * mq.sit.channel=NLACEQM01T.SVRCONN
 * mq.sit.queue.manager=NLACEQM01T
 * mq.sit.user=mqconn
 *
 * # MQ Preprod
 * mq.preprod.host=192.168.138.5
 * mq.preprod.port=1456
 * mq.preprod.channel=NLACEQM01O.SVRCONN
 * mq.preprod.queue.manager=NLACEQM01O
 * mq.preprod.user=mqclntcon
 *
 * # DB SIT
 * db.sit.url=jdbc:oracle:thin:@//192.168.102.37:1521/wltdbsit.neoleap.com.sa
 * db.sit.username=EPAY_TESTING
 * db.sit.password=changeme
 *
 * # DB Preprod
 * db.preprod.url=jdbc:oracle:thin:@//10.10.116.85:1521/WLTPPRD
 * db.preprod.username=EUA_JBHASIN_C
 * db.preprod.password=changeme
 * </pre>
 */
public class MQJobRunner {

    private final Properties config;
    private final String env;
    private java.sql.Connection dbConnection;

    // ──────────────────────────── MQ Job definitions ────────────────────────────

    public static final String REFRESH_MG_CODE_TABLE = "RefreshMGCodeTableRq";
    public static final String REFRESH_TAHWEEL_BANK_BRANCHES = "GetH2hCountriesRq";
    public static final String REFRESH_TRANSFAST_CODE_TABLE = "RefreshTransfastCodeTableRq";
    public static final String REFRESH_WU_CODE_TABLE = "RefreshWUCodeTableRq";
    public static final String RIA_REFRESH_CODE_TABLE = "RIARefreshCodeTableRq";

    private static final String DB_COUNT_QUERY = "SELECT COUNT(*) AS CNT FROM EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV";

    // ──────────────────────────── Constructors ──────────────────────────────────

    /**
     * Load configuration from a properties file path.
     */
    public MQJobRunner(String propertiesFilePath) throws IOException {
        this.config = new Properties();
        try (InputStream is = new FileInputStream(propertiesFilePath)) {
            config.load(is);
        }
        this.env = config.getProperty("env", "SIT").toUpperCase();
    }

    /**
     * Load configuration from a classpath resource.
     */
    public MQJobRunner(InputStream propertiesStream) throws IOException {
        this.config = new Properties();
        config.load(propertiesStream);
        this.env = config.getProperty("env", "SIT").toUpperCase();
    }

    /**
     * Provide configuration directly.
     */
    public MQJobRunner(Properties config) {
        this.config = config;
        this.env = config.getProperty("env", "SIT").toUpperCase();
    }

    // ──────────────────────────── MQ Operations ────────────────────────────────

    /**
     * Send a trigger message (empty JSON {}) to the given MQ queue.
     */
    public void triggerMQJob(String queueName) throws JMSException {
        triggerMQJob(queueName, "{}");
    }

    /**
     * Send a message to the given MQ queue.
     */
    public void triggerMQJob(String queueName, String messageBody) throws JMSException {
        String prefix = isPreprod() ? "mq.preprod." : "mq.sit.";
        String host = config.getProperty(prefix + "host");
        int port = Integer.parseInt(config.getProperty(prefix + "port"));
        String channel = config.getProperty(prefix + "channel");
        String queueManager = config.getProperty(prefix + "queue.manager");
        String user = config.getProperty(prefix + "user");

        javax.jms.Connection connection = null;
        Session session = null;
        javax.jms.MessageProducer producer = null;

        try {
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
            cf.setIntProperty(WMQConstants.WMQ_PORT, port);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManager);
            cf.setStringProperty(WMQConstants.USERID, user);

            connection = cf.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            producer = session.createProducer(destination);

            TextMessage message = session.createTextMessage(messageBody);
            connection.start();
            producer.send(message);

            log("MQ message sent to queue [" + queueName + "] on " + env);
        } finally {
            closeQuietly(producer);
            closeQuietly(session);
            closeQuietly(connection);
        }
    }

    // ──────────────────────────── DB Operations ────────────────────────────────

    /**
     * Get row count from EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV.
     */
    public int getMTOCodeTableCount() throws SQLException {
        ensureDBConnection();
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(DB_COUNT_QUERY)) {
            if (rs.next()) {
                int count = rs.getInt("CNT");
                log("MTO_CNTRY_CRNCY_DLV record count: " + count);
                return count;
            }
        }
        return 0;
    }

    /**
     * Execute any SELECT query and return a single column value.
     */
    public String queryValue(String query, String columnName) throws SQLException {
        ensureDBConnection();
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getString(columnName);
            }
        }
        return "";
    }

    /**
     * Execute a SELECT query and return all rows as a list of maps.
     */
    public List<Map<String, String>> queryAllRows(String query) throws SQLException {
        ensureDBConnection();
        List<Map<String, String>> rows = new ArrayList<>();
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                rows.add(row);
            }
        }
        log("Query returned " + rows.size() + " rows");
        return rows;
    }

    /**
     * Query EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV and save all rows to a JSON file.
     */
    public void saveResultsToJson(String outputFilePath) throws SQLException, IOException {
        String query = "SELECT * FROM EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV";
        List<Map<String, String>> rows = queryAllRows(query);
        writeJson(rows, outputFilePath);
        log("DB results saved to " + outputFilePath + " (" + rows.size() + " records)");
    }

    /**
     * Execute any query and save results to a JSON file.
     */
    public void saveQueryResultsToJson(String query, String outputFilePath) throws SQLException, IOException {
        List<Map<String, String>> rows = queryAllRows(query);
        writeJson(rows, outputFilePath);
        log("Query results saved to " + outputFilePath + " (" + rows.size() + " records)");
    }

    private void writeJson(List<Map<String, String>> rows, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < rows.size(); i++) {
            sb.append("  {");
            Map<String, String> row = rows.get(i);
            int j = 0;
            for (Map.Entry<String, String> entry : row.entrySet()) {
                if (j > 0) sb.append(", ");
                sb.append("\"").append(escapeJson(entry.getKey())).append("\": ");
                if (entry.getValue() == null) {
                    sb.append("null");
                } else {
                    sb.append("\"").append(escapeJson(entry.getValue())).append("\"");
                }
                j++;
            }
            sb.append("}");
            if (i < rows.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ──────────────────────────── Job Runners ──────────────────────────────────

    /**
     * Trigger a job and verify DB has records. Returns the count.
     */
    public int triggerJobAndVerify(String queueName, int waitSeconds) throws Exception {
        log("Triggering job: " + queueName);
        triggerMQJob(queueName);
        log("Waiting " + waitSeconds + "s for job to process...");
        Thread.sleep(waitSeconds * 1000L);
        int count = getMTOCodeTableCount();
        if (count > 0) {
            log("PASS - " + queueName + " : MTO_CNTRY_CRNCY_DLV has " + count + " records");
        } else {
            log("FAIL - " + queueName + " : MTO_CNTRY_CRNCY_DLV table is empty");
        }
        return count;
    }

    /**
     * Trigger a job, verify DB, and save results to JSON file.
     */
    public int triggerJobAndSaveToJson(String queueName, int waitSeconds, String outputFilePath) throws Exception {
        int count = triggerJobAndVerify(queueName, waitSeconds);
        if (count > 0) {
            saveResultsToJson(outputFilePath);
        }
        return count;
    }

    public int refreshMoneyGramCodeTable() throws Exception {
        return triggerJobAndVerify(REFRESH_MG_CODE_TABLE, 10);
    }

    public int refreshTahweelBankBranches() throws Exception {
        return triggerJobAndVerify(REFRESH_TAHWEEL_BANK_BRANCHES, 10);
    }

    public int refreshTransfastCodeTable() throws Exception {
        return triggerJobAndVerify(REFRESH_TRANSFAST_CODE_TABLE, 10);
    }

    public int refreshWUCodeTable() throws Exception {
        return triggerJobAndVerify(REFRESH_WU_CODE_TABLE, 10);
    }

    public int riaRefreshCodeTable() throws Exception {
        return triggerJobAndVerify(RIA_REFRESH_CODE_TABLE, 10);
    }

    /**
     * Run all 5 refresh jobs sequentially.
     */
    public void runAllJobs() throws Exception {
        refreshMoneyGramCodeTable();
        refreshTahweelBankBranches();
        refreshTransfastCodeTable();
        refreshWUCodeTable();
        riaRefreshCodeTable();
    }

    // ──────────────────────────── Cleanup ───────────────────────────────────────

    public void close() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
                log("DB connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ──────────────────────────── Internal helpers ──────────────────────────────

    private boolean isPreprod() {
        return env.contains("PREPROD");
    }

    private void ensureDBConnection() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            String prefix = isPreprod() ? "db.preprod." : "db.sit.";
            String url = config.getProperty(prefix + "url");
            String username = config.getProperty(prefix + "username");
            String password = config.getProperty(prefix + "password");

            DriverManager.registerDriver(new OracleDriver());
            dbConnection = DriverManager.getConnection(url, username, password);
            log("DB connection established to " + env);
        }
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void log(String message) {
        System.out.println("[MQJobRunner] " + message);
    }

    // ──────────────────────────── Main (standalone execution) ───────────────────

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "mq-job-config.properties";

        try {
            MQJobRunner runner = new MQJobRunner(configPath);
            try {
                if (args.length > 1) {
                    // Run specific job by queue name
                    runner.triggerJobAndVerify(args[1], 10);
                } else {
                    // Run all jobs
                    runner.runAllJobs();
                }
            } finally {
                runner.close();
            }
        } catch (Exception e) {
            System.err.println("MQJobRunner failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
