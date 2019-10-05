package org.easysql.session;

import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public class Transaction {
    @Getter
    private int level;
    private Connection conn;
    @Setter
    private Savepoint savepoint;
    public Transaction(Connection conn,int level){
        try {
            conn.setTransactionIsolation(level);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.conn=conn;
        this.level=level;
    }

    public void start(){
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void commit(){
        try {
            conn.commit();
        } catch (SQLException e) {
            try {
                if (savepoint!=null){
                    conn.rollback(savepoint);
                }
                else {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
