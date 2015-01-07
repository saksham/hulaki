package com.hulaki.smtp.storage;


import com.hulaki.smtp.api.MailMessage;
import com.hulaki.smtp.exceptions.SmtpException;
import com.google.common.collect.Lists;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class SqliteMailMessageDao implements MailMessageDao {
    private static final Logger log = Logger.getLogger(SqliteMailMessageDao.class);
    private final String dbFilename;

    public SqliteMailMessageDao(String dbFilepath) {
        this.dbFilename = dbFilepath;
        init();
    }

    private void init() {
        Connection c;
        Statement stmt;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:" + this.dbFilename);
            log.info("Opened database successfully");

            stmt = c.createStatement();
            String sql = "DROP TABLE IF EXISTS emails;" +
                    "CREATE TABLE emails" +
                    "(recipient VARCHAR(50) NOT NULL," +
                    " sender VARCHAR(50) NOT NULL," +
                    " subject VARCHAR(150) NOT NULL," +
                    " body TEXT NOT NULL," +
                    " is_relayed BOOLEAN NOT NULL, " +
                    " inserted_on BIGINT NOT NULL);" +
                    "CREATE INDEX email_recipient ON emails(recipient);" +
                    "CREATE INDEX email_inserted_on ON emails(inserted_on);";

            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception e) {
            throw new SmtpException("Failed to initialize database", e);
        }
        log.info("Table created successfully");
    }


    @Override
    public void storeMessage(String recipient, MailMessage email) {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        template.update("INSERT INTO emails (sender, recipient, subject, body, is_relayed, inserted_on) " +
                        "VALUES(?, ?, ?, ?, ? , ?)",
                email.getFrom(), recipient, email.getSubject(), email.getBody(), email.isRelayed(), new DateTime().getMillis());
        log.info("Stored message for " + recipient);
    }

    @Override
    public List<MailMessage> retrieveMessages(String recipient) {
        List<MailMessage> mailMessages = Lists.newArrayList();

        JdbcTemplate template = new JdbcTemplate(getDataSource());
        List<Map<String, Object>> rows = template.queryForList("SELECT * FROM emails WHERE recipient = ? ORDER BY inserted_on", recipient);
        for(Map<String, Object> row : rows) {
            String sender = (String) row.get("sender");
            String subject = (String) row.get("subject");
            String body = (String) row.get("body");
            boolean isRelayed = (int) row.get("is_relayed") == 1;
            MailMessage mailMessage = new MailMessage(sender, recipient, subject, body, isRelayed);
            mailMessages.add(mailMessage);
        }
        log.info(String.format("Retrieved %d message for %s", mailMessages.size(), recipient));
        return mailMessages;
    }

    @Override
    public int countMessagesForRecipient(String recipient) {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        Map<String, Object> map = template.queryForMap("SELECT COUNT(*) AS COUNT FROM emails WHERE recipient = ? ", recipient);
        return (int) map.get("COUNT");
    }

    @Override
    public void clearMessagesForRecipient(String recipient) {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        template.update("DELETE FROM emails WHERE recipient = ?", recipient);
        log.info(String.format("Deleted message for %s", recipient));
    }

    @Override
    public void clearMessages() {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        template.execute("DELETE FROM emails");
        log.info(String.format("Deleted ALL message"));
    }

    @Override
    public int countAllMessagesReceived() {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        Map<String, Object> map = template.queryForMap("SELECT COUNT(*) AS COUNT FROM emails");
        return (int) map.get("COUNT");
    }

    private DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:" + this.dbFilename);
        return dataSource;
    }
}
