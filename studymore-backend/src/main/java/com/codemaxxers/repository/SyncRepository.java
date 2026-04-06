package com.codemaxxers.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class SyncRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SyncRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void upsertMultipleRows(String tableName, String pkColumn, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return;
        
        for (Map<String, Object> row : rows) {
            upsertSingleRow(tableName, pkColumn, row);
        }
    }

    public void upsertSingleRow(String tableName, String pkColumn, Map<String, Object> data) {
        if (data == null || data.isEmpty()) return;

        Object pkValue = data.get(pkColumn);
        if (pkValue == null) {
            System.err.println("Cannot upsert into " + tableName + " without primary key: " + pkColumn);
            return; 
        }

        String checkSql = "SELECT COUNT(1) FROM " + tableName + " WHERE " + pkColumn + " = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, pkValue);

        if (count != null && count > 0) {
            executeUpdate(tableName, pkColumn, pkValue, data);
        } else {
            executeInsert(tableName, data);
        }
    }

    // Helper Methods to build dynamic SQL

    private void executeUpdate(String tableName, String pkColumn, Object pkValue, Map<String, Object> data) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        List<Object> args = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey().equals(pkColumn)) continue; 
            
            if (i > 0) sql.append(", ");
            sql.append(entry.getKey()).append(" = ?");
    
            args.add(formatValue(entry.getKey(), entry.getValue()));
            i++;
        }
        
        sql.append(" WHERE ").append(pkColumn).append(" = ?");
        args.add(pkValue);
        
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    private void executeInsert(String tableName, Map<String, Object> data) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");
        List<Object> args = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (i > 0) {
                sql.append(", ");
                placeholders.append(", ");
            }
            sql.append(entry.getKey());
            placeholders.append("?");

            args.add(formatValue(entry.getKey(), entry.getValue()));
            i++;
        }
        
        sql.append(")");
        placeholders.append(")");
        sql.append(placeholders.toString());
        
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    private Object formatValue(String key, Object value) {

        if (value instanceof Integer && (key.startsWith("is_") || 
            key.equals("srs_enabled") || key.equals("dark_mode") || 
            key.equals("lock_in_mode") || key.equals("show_mascot") || 
            key.equals("start_sound") || key.equals("break_alert") || 
            key.equals("popups"))) {
            
            return (Integer) value != 0;
        }

        if (value instanceof String && (key.endsWith("_at") || key.endsWith("_time") || 
            key.endsWith("_date") || key.equals("timestamp"))) {
            
            String dateStr = (String) value;
            try {
                dateStr = dateStr.replace("T", " ");
                if (dateStr.length() == 10) {
                    dateStr += " 00:00:00";
                }
                return java.sql.Timestamp.valueOf(dateStr);
            } catch (Exception e) {
                return value;
            }
        }
        return value;
    }

    public void upsertMultipleRowsComposite(String tableName, List<String> pkColumns, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return;
        for (Map<String, Object> row : rows) {
            upsertSingleRowComposite(tableName, pkColumns, row);
        }
    }

    public void upsertSingleRowComposite(String tableName, List<String> pkColumns, Map<String, Object> data) {
        if (data == null || data.isEmpty()) return;

        StringBuilder whereClause = new StringBuilder();
        List<Object> pkValues = new ArrayList<>();
        
        for (int i = 0; i < pkColumns.size(); i++) {
            String col = pkColumns.get(i);
            Object val = data.get(col);
            if (val == null) {
                System.err.println("Missing composite PK component " + col + " for table " + tableName);
                return;
            }
            if (i > 0) whereClause.append(" AND ");
            whereClause.append(col).append(" = ?");
            pkValues.add(val);
        }

        String checkSql = "SELECT COUNT(1) FROM " + tableName + " WHERE " + whereClause;
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, pkValues.toArray());

        if (count != null && count > 0) {
            StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
            List<Object> args = new ArrayList<>();
            int i = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (pkColumns.contains(entry.getKey())) continue; 
                if (i > 0) sql.append(", ");
                sql.append(entry.getKey()).append(" = ?");
                args.add(formatValue(entry.getKey(), entry.getValue()));
                i++;
            }

            if (i > 0) {
                sql.append(" WHERE ").append(whereClause);
                args.addAll(pkValues);
                jdbcTemplate.update(sql.toString(), args.toArray());
            }
        } else {
            executeInsert(tableName, data); 
        }
    }

    
}