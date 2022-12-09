package qsol.qsolcdmplatformapigithub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import qsol.qsolcdmplatformapigithub.domain.CsvDetail;
import qsol.qsolcdmplatformapigithub.dto.request.CursorResult;
import qsol.qsolcdmplatformapigithub.exception.file.FileUnselectedException;
import qsol.qsolcdmplatformapigithub.repository.CsvDetailJdbcRepository;
import qsol.qsolcdmplatformapigithub.repository.CsvDetailRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FileService {

    private final CsvDetailRepository csvDetailRepository;
    private final CsvDetailJdbcRepository csvDetailBatchRepository;

    @Transactional
    public List<CsvDetail> read(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) {
            throw new FileUnselectedException();
        }

        List<CsvDetail> csvDetail = getCsvDetails(multipartFile);

        csvDetailBatchRepository.batchInsert(csvDetail);

        return csvDetail;
    }

    public CursorResult<CsvDetail> get(Long cursorId, PageRequest pageRequest) {
        List<CsvDetail> csvList = getCsvList(cursorId, pageRequest);

        Long lastIdOfList = csvList.isEmpty() ? null : csvList.get(csvList.size() - 1).getId();

        return new CursorResult<>(csvList, hasNext(lastIdOfList));
    }

    private List<CsvDetail> getCsvDetails(MultipartFile multipartFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), UTF_8));

        List<String> collect = br.lines().collect(Collectors.toList());
        return collect.stream()
                .map(result -> result.split(", "))
                .map(strings -> CsvDetail.builder()
                        .value(Long.valueOf(strings[1]))
                        .createdDate(LocalDateTime.now()).build())
                .collect(Collectors.toList());
    }

    private List<CsvDetail> getCsvList(Long id, Pageable page) {
        return id == null ?
                csvDetailRepository.findAllByOrderByIdAsc(page) :
                csvDetailRepository.findByIdGreaterThanOrderByIdAsc(id, page);
    }

    private Boolean hasNext(Long id) {
        if (id == null) {
            return false;
        }
        return csvDetailRepository.existsByIdGreaterThan(id);
    }

    public void deleteAll() {
        csvDetailRepository.deleteAllInBatch();
    }
}
