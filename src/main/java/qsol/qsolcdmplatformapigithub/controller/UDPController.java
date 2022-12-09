package qsol.qsolcdmplatformapigithub.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qsol.qsolcdmplatformapigithub.config.data.LoginUser;
import qsol.qsolcdmplatformapigithub.config.data.UserSession;
import qsol.qsolcdmplatformapigithub.config.data.Validator;
import qsol.qsolcdmplatformapigithub.domain.TntData;
import qsol.qsolcdmplatformapigithub.dto.request.CursorResult;
import qsol.qsolcdmplatformapigithub.service.ChartDataService;
import qsol.qsolcdmplatformapigithub.service.udp.UDPDataService;
import qsol.qsolcdmplatformapigithub.service.udp.UDPCommService;

import java.net.SocketException;
import java.net.UnknownHostException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UDPController {

    private static final int DEFAULT_SIZE = 200; // Vue에서 Chart를 구현할 때, 데이터를 DB에서 n개씩 가져와 보여주기 위한 설정 값
    private final UDPDataService udpDataService;
    private final UDPCommService udpCommService;
    private final ChartDataService chartDataService;
//    private final Validator validator;
    //    private final UDPService udpServer;


    @GetMapping("/start-request") // Client에 데이터 수집 시작 요청 송신 및 응답 수신. UDP 통신
    public void startRequest(@LoginUser UserSession userSession) throws UnknownHostException {
//        validator.validateLogin(userSession);
        log.info("-- startRequest UDPController Run ---");
        udpCommService.udpStartRequest("192.168.0.2"); // 요청 송신
        udpCommService.udpStartResponse(); // 응답 수신
        stopResponse(userSession);

    }

//    @GetMapping("/stop-request") // Client에 데이터 수집 중단 요청 송신 및 응답 수신. UDP 통신
    public void stopResponse(@LoginUser UserSession userSession) throws UnknownHostException { // Client로부터 측정 종료 수신, 응답 송신
//        validator.validateLogin(userSession);
        log.info("-- stopRequest UDPController Run ---");
        udpCommService.udpStopResponse(); // 종료 수신
        udpCommService.udpStopRequest("192.168.0.2"); // 응답 송신
    }

    @GetMapping("/data-request") // Client에 수집된 데이터 요청 송신 및 응답 수신. UDP 통신 후 Queue에 데이터 저장
    public void dataRequest(@LoginUser UserSession userSession) throws UnknownHostException, SocketException {
//        validator.validateLogin(userSession);
        log.info("-- dataRequest UDPController Run ---");
        udpDataService.udpDataRequest("192.168.0.2"); // 요청 송신
        udpDataService.udpDataResponse(); // 응답 수신 및 Queue 저장
        queueDataSave(); // Queue 데이터를 DB에 저장
    }

    @GetMapping("/tnt-masterId") // 가장 최근에 시작된 작업의 ID 값(masterId)을 요청 (Vue로 return)
    public Integer getMasterId(@LoginUser UserSession userSession) {
//        validator.validateLogin(userSession);
        log.info("-- getMasterId UDPController Run ---");
        Integer masterId = udpDataService.getMasterId();
        log.info("GET MASTER ID = {}", masterId);
        return masterId;
    }

    @GetMapping("/runInterval/") // getMasterId()에서 가져온 masterId를 기준으로 DB에 저장된 데이터를 Cursor 방식으로 일정 부분 요청. Interval로 반복 (Vue로 return)
    public CursorResult<TntData> getCsvData(@LoginUser UserSession userSession, @RequestParam int masterId, @RequestParam(required = false) Long cursorId, @RequestParam(required = false) Integer size) {
//        validator.validateLogin(userSession);
        log.info("-- runInterval UDPController Run ---");
        if (size == null) {
            size = DEFAULT_SIZE;
        }
        return chartDataService.get(masterId, cursorId, PageRequest.of(0, size));
    }

    public void queueDataSave() { // Queue에 저장된 데이터를 DB에 저장
        log.info("-- queueDataSave UDPController Run ---");
        udpDataService.queueDataSave();
    }

    /*@GetMapping("/udp")
    public void udpTest() throws SocketException {
        log.info("starting udp server ...");
        udpServer.receive();
    }*/

}
