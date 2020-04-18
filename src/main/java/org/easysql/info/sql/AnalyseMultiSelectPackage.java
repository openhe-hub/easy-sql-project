package org.easysql.info.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.easysql.session.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class AnalyseMultiSelectPackage extends AnalyseSelectPackage {
    LinkedHashMap<String, Session<?>> sessions;
    LinkedHashMap<Session<?>, ColumnCursor> columnCursor;
    StringBuilder sql;

    public AnalyseMultiSelectPackage(StringBuilder toSelect, StringBuilder condition, ArrayList<Object> selectParas, LinkedHashMap<String, Session<?>> sessions, LinkedHashMap<Session<?>, ColumnCursor> columnCursor, StringBuilder sql) {
        super(toSelect, condition, selectParas);
        this.sessions = sessions;
        this.columnCursor = columnCursor;
        this.sql = sql;
    }
}
