package model;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvTable {
    List<String> headers = new ArrayList<>();
    List<List<String>> rows = new ArrayList<>();

    public void setHeaders(List<String> headers){
        this.headers = headers;
    }
    public List<String> getHeaders() {
        return this.headers;
    }

    public void addRow(List<String> row) throws Exception {
        if (row.size() != headers.size()){
            log.error("Wrong line size row: {}", row);
            throw new Exception(String.format("line size does not match with header size, headerSize: %s, lineSize: %s", headers.size(), row.size()));
        }
        this.rows.add(row);
    }


    public List<List<String>> getRows() {
        return this.rows;
    }

}

