package qsol.qsolcdmplatformapigithub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import qsol.qsolcdmplatformapigithub.domain.TntData;
import qsol.qsolcdmplatformapigithub.dto.request.CursorResult;
import qsol.qsolcdmplatformapigithub.repository.DataRepository;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChartDataService {
    private final DataRepository dataRepository;

    // getMasterId()에서 가져온 masterId를 기준으로 DB에 저장된 데이터를 Cursor 방식으로 일정 부분 요청. Interval로 반복 (Vue로 return)
    public CursorResult<TntData> get(int masterId, Long cursorId, Pageable pageRequest) {
        log.info("--- get ChartDataService Run ---");
        List<TntData> csvList = getCsvList(masterId, cursorId, pageRequest);
        Long lastIdOfList = csvList.isEmpty() ? null : csvList.get(csvList.size() - 1).getId();
        return new CursorResult<>(csvList, hasNext(lastIdOfList));
    }

    private List<TntData> getCsvList(int masterId, Long cursorId, Pageable page) {
        log.info("--- getCsvList ChartDataService Run ---");
        return cursorId == null ?
                dataRepository.findAllByMasterIdOrderByIdAsc(masterId, page) :
                dataRepository.findByMasterIdAndIdGreaterThanOrderByIdAsc(masterId, cursorId, page);
    }

    private Boolean hasNext(Long id) {
        log.info("--- hasNext ChartDataService Run ---");
        if (id == null) {
            return false;
        }
        return dataRepository.existsByIdGreaterThan(id);
    }
}
