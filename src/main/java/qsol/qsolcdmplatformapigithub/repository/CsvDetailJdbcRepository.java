package qsol.qsolcdmplatformapigithub.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import qsol.qsolcdmplatformapigithub.domain.CsvDetail;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CsvDetailJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<CsvDetail> detailList) {
        jdbcTemplate.batchUpdate("insert into csv_detail (created_date, value) values (?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                        CsvDetail csvDetail = detailList.get(i);
                        ps.setObject(1, csvDetail.getCreatedDate());
                        ps.setLong(2, csvDetail.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return detailList.size();
                    }
                });
    }
}
