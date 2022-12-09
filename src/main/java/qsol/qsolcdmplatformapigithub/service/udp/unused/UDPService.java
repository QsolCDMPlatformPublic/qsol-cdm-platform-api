package qsol.qsolcdmplatformapigithub.service.udp.unused;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import qsol.qsolcdmplatformapigithub.domain.TntData;
import qsol.qsolcdmplatformapigithub.domain.TntMaster;
import qsol.qsolcdmplatformapigithub.dto.request.CursorResult;
import qsol.qsolcdmplatformapigithub.repository.DataRepository;
import qsol.qsolcdmplatformapigithub.repository.MasterRepository;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class UDPService {

    private final MasterRepository masterRepository;
    private final DataRepository dataRepository;
    private DatagramSocket socket;

    private final byte[] buf = new byte[65536]; // UDP 최대크기

    public void receive() throws SocketException {
        socket = new DatagramSocket(20099); // 20099 포트로 소켓 열기
        boolean running = true;
        boolean startMessage = false;
        TntMaster topByOrderByIdDesc = null;
        List<TntData> tntDataList = new ArrayList<>();
        int count = 0;

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // udp 수신 대기

                if (!startMessage) {
                    String message = new String(packet.getData(), 0, packet.getLength()); // 수신된 데이터를 String으로 변환하여 담음
                    if (message.equals("1")) {// 시작 전에 master Table의 id 숫자 1 증가
                        System.out.println("--- 시작 값 1 들어옴 --- ");
                        TntMaster tntMaster = TntMaster
                                .builder()
                                .build();
                        masterRepository.save(tntMaster);
//                        topByOrderByIdDesc = masterRepository.findTopByOrderByIdDesc(); // 위치 바뀜
                    }
                    startMessage = true;
                } else {
                    byte[] data = packet.getData();
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    DataInputStream in = new DataInputStream(bais);

                    while (in.available() > 0) {
                        String element = in.readUTF();

                        if (element.equals("")) {
                            System.out.println("element 값 ' '으로 인한 while문 break;");
                        } else {
                            TntData tntData = TntData.builder()
                                    .masterId(topByOrderByIdDesc.getId())
                                    .data(Long.valueOf(element))
                                    .build();
                            tntDataList.add(tntData);
                        }
                    }
                    count++;
                    dataRepository.saveAll(tntDataList);
                    System.out.println("✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅✅RECEIVE COUNT = " + count);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                break;
            }

        }
        if (!running) {
            socket.setSoTimeout(10000);
        }
    }

    public CursorResult<TntData> get(int masterId, Long cursorId, Pageable pageRequest) {
        List<TntData> csvList = getCsvList(masterId, cursorId, pageRequest);
        Long lastIdOfList = csvList.isEmpty() ? null : csvList.get(csvList.size() - 1).getId();
        return new CursorResult<>(csvList, hasNext(lastIdOfList));
    }

    private List<TntData> getCsvList(int masterId, Long cursorId, Pageable page) {
        return cursorId == null ?
                dataRepository.findAllByMasterIdOrderByIdAsc(masterId, page) :
                dataRepository.findByMasterIdAndIdGreaterThanOrderByIdAsc(masterId, cursorId, page);
    }

    private Boolean hasNext(Long id) {
        if (id == null) {
            return false;
        }
        return dataRepository.existsByIdGreaterThan(id);
    }
}
