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
            // We do not want to update the primary key itself
            if (entry.getKey().equals(pkColumn)) continue; 
            
            if (i > 0) sql.append(", ");
            sql.append(entry.getKey()).append(" = ?");
            args.add(entry.getValue());
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
            args.add(entry.getValue());
            i++;
        }
        
        sql.append(")");
        placeholders.append(")");
        sql.append(placeholders.toString());
        
        jdbcTemplate.update(sql.toString(), args.toArray());
    }
}