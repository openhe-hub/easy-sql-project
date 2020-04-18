package org.easysql.info.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyseSelectPackage {
    StringBuilder toSelect;
    StringBuilder condition;
    ArrayList<Object> selectParas;
}
