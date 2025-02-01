package model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Slf4j
public class CsvTable {
    @Setter
    List<String> headers = new ArrayList<>();
    List<List<String>> rows = new ArrayList<>();
    MetaData metaData = new MetaData();

    public void addRow(List<String> row) throws Exception {
        if (row.size() != headers.size()){
            log.error("Wrong line size row: {}", row);
            throw new Exception(String.format("line size does not match with header size, headerSize: %s, lineSize: %s", headers.size(), row.size()));
        }
        //check if any of the values are null or empty
        for (int i=0; i<row.size(); i++) {
            if (row.get(i) == null || row.get(i).isBlank()) {
                log.debug("{} index in row {} in null or empty", i, row);
            }
        }
        this.rows.add(row);
    }


    @Getter
    @Setter
    public static class MetaData {
        String headerColor;
        String highlightColor;
        Set<Integer> highlightLineIndices;

        public MetaData() {
            this.highlightLineIndices = new HashSet<>();
        }
    }


}

