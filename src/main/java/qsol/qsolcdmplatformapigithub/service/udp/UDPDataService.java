package qsol.qsolcdmplatformapigithub.service.udp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import qsol.qsolcdmplatformapigithub.domain.TntData;
import qsol.qsolcdmplatformapigithub.domain.TntMaster;
import qsol.qsolcdmplatformapigithub.domain.UniqueNumber;
import qsol.qsolcdmplatformapigithub.exception.udp.TntMasterNotFoundException;
import qsol.qsolcdmplatformapigithub.exception.udp.UniqueNumberNotFoundException;
import qsol.qsolcdmplatformapigithub.repository.DataRepository;
import qsol.qsolcdmplatformapigithub.repository.MasterRepository;
import qsol.qsolcdmplatformapigithub.repository.UniqueNumberRepository;

import java.io.IOException;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@Service
@RequiredArgsConstructor
public class UDPDataService {
    private final UniqueNumberRepository uniqueNumberRepository;
    private final MasterRepository masterRepository;
    private final DataRepository dataRepository;
    private static final int MY_SERVER_PORT = 20050;
    private static final int SERVER_PORT = 20099;
    private final byte[] buf = new byte[65536]; // 수집 데이터에 대한 응답은 고정된 길이의 값이 아니기에 UDP 최대 크기 설정
    Queue<List<Long>> queue = new LinkedList<>(); // 전달 받는 약 16만개의 데이터를 담아둘 Queue를 생성

    private DatagramSocket socket;
    private String unique;

    public void udpDataRequest(String ip) throws UnknownHostException { // Client에 수집된 데이터 송신 요청
        log.info("--- udpStopRequest Run ---");
        InetAddress ia = InetAddress.getByName(ip); // 통신 IP 지정
        try {
            socket = new DatagramSocket(MY_SERVER_PORT); // 소켓 오픈. 본인의 port를 지정
            ByteBuffer buffer = ByteBuffer.allocate(10); // 통신 프로토콜에 의거, 데이터를 buffer, packet에 담아 upd 전송. 길이 10
            clear(buffer); // 버퍼 클리어
            buffer.order(ByteOrder.BIG_ENDIAN);// BIG_ENDIAN 형식

            UniqueNumber uniqueNumber = uniqueNumberRepository.findTopByOrderByIdDesc()
                    .orElseThrow(UniqueNumberNotFoundException::new); // DB에 가장 최근에 저장한 [측정 고유 번호]를 가져옴
            unique = uniqueNumber.getValue();
            System.out.println("uniqueNumber : " + unique);

            byte[] parseByte = new byte[8]; // DB에서 가져온 String 값의 [측정 고유 번호]를 1byte씩 나누어 넣기 위한 변수를 생성
            int count;
            int substring = 0;
            for(count = 0; count < 4; count++) { // [측정 고유 번호] 자료형 변환
                parseByte[count] = Byte.parseByte(unique.substring(substring, (substring+2)));
                substring+=2;
            }

            byte[] udpProtocol = getUdpProtocol(parseByte);
            buffer.put(udpProtocol); // 설정된 값을 buffer에 담기
            byte[] bufferData = buffer.array();

            DatagramPacket dp = new DatagramPacket( // packet으로 옮겨 담기
                    bufferData, bufferData.length, ia, SERVER_PORT);

            socket.send(dp); // 소켓으로 udp 전송
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            socket.close(); // 전송이 완료되면 Client로 부터 응답 받을 소켓을 새로 열어야하기에, 소켓 닫기
        }
    }

    public void udpDataResponse() throws SocketException {
        long count = 0L;
        int segmentTotal = 0;

        log.info("--- udpDataResponse Run ---");
        socket = new DatagramSocket(MY_SERVER_PORT); // 소켓 생성
        queue = new LinkedList<>(); // 큐 초기화
        boolean uniqueCheck = false; // Client로 부터 전달 받은 측정고유번호가 Server에서 갖고있는 번호와 일치하는지 확인 후, 이 후의 진행을 위해 true로 변경됨
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                List<Long> queueData = new ArrayList<>(); // 큐 배열의 한 공간에 들어갈 리스트를 생성 및 초기화
                socket.receive(packet); // udp 수신 대기

                String hexText = new java.math.BigInteger(packet.getData()).toString(16); // 데이터 핸들링을 위해 패킷 데이터 저장
                count++;
                // 들어온 패킷 데이터의 측정 고유 번호가 요청 시의 데이터와 일치하는지 확인 (처음 한번만)
                uniqueCheck = isFirst(count, uniqueCheck, hexText);

                if(uniqueCheck) { // 측정 고유 번호 일치 시 정상 진행
                    segmentTotal = Integer.parseUnsignedInt(hexText.substring(24,28), 16); // 모든 데이터를 수신한 뒤 소켓을 닫기 위해 데이터 총량을 기록
                    // 3208 length
                    String i = hexText.substring(32,3240); // 패킷에서 그래프에 표시할 실데이터 값만 빼서 저장

                    int start = 0; // 4byte( ex ▶ 0~8, 8~16 ▶ ff??????, ff??????)가 실데이터 1개.
                    int end = 8; // 그러므로 i의 총 length / 8 의 결과 값이 실데이터의 총 개수가 됨
                    int whileCount = 0;
                    while(whileCount < (i.length() / 8)) {
//                        if(whileCount == 0) { // 데이터 확인을 위한 로그
//                        System.out.println("data length : " + i.length() + " | real length : " + i.length() / 8);// 총 데이터의 실제 개수
//                        System.out.println("data 끝지점 : " + hexText.substring(3200,3240));
//                            System.out.println("i : " + i);
//                            BigInteger value = new BigInteger(i, 16);
//                            System.out.println("change : " + value);
//                        }

                        String dataString = (i.substring(start, end)); // 총 데이터에서 하나하나씩 리스트에 저장하기 위해 변수에 담음

//                    System.out.println("count : " + whileCount
//                            + " 🔘 start : " + start
//                            + " 🔘 end : " + end
//                            + " 🔘 data : " + dataString
//                            + " 🔘 real data : " + Integer.parseUnsignedInt(dataString, 16)); // 데이터 실제값 (10진수)
//                                + " 🔘 2 : " + Long.parseLong(dataString, 16)
//                                + " 🔘 3 : " + new BigInteger(dataString, 16));

                        if(!dataString.equals("ff000000")) { // 마지막 CheckSum은 저장하지 않음. ChenckSum이 아니라면 리스트에 데이터를 저장
                            queueData.add((long) Integer.parseUnsignedInt(dataString, 16)); // 데이터를 리스트에 추가
                        }
                        start += 8; end += 8; whileCount += 1;
                    }
                    queue.offer(queueData); // while문을 통해 약 400개의 데이터가 담긴 리스트를 큐의 한 공간에 배치
//                System.out.println("queue.size() : " + queue.size() + " | (segmentTotal-1) : " + (segmentTotal-1));
                    if((segmentTotal-1) == queue.size()) { // 1회당 400개 씩, 약 16만개의 모든 데이터를 수신 받은 경우, while문 break 후 소켓을 닫음
                        System.out.println("--- (socket.receive) while ▶ break ---");
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("--- socket.close() ---");
        socket.close(); // 모든 데이터를 큐에 임시 저장하였으므로 소켓을 닫음

        // 이 후 queueDataSave() 에서 Queue의 데이터를 DB에 저장하게 되는데, 그 전에 이번 작업을 구분하기 위한 masterId를 DB에 생성
        //TODO 빌더 -> 생성자 방식으로 변경.
        masterRepository.save(TntMaster.builder().build());
    }

    public void queueDataSave() { // Queue에 담긴 데이터를 DB에 저장 Queue[[400], [400], [400], [400] ... ]]
        log.info("--- queueDataSave Run ---");

        List<TntData> tntDataList = new ArrayList<>();
        int saveCount = 0;
        while (!queue.isEmpty()) { // 큐의 모든 데이터가 저장될 때까지 반복

            List<List<Long>> queueResponse = new ArrayList<>(); // 큐의 데이터들을 받아줄 변수 생성
            queueResponse.add(queue.poll()); // 큐의 맨 앞 데이터를 가져옴

            int count = 0;
            Integer masterId = getMasterId();

            toEntity(tntDataList, queueResponse, count, masterId);

            dataRepository.saveAll(tntDataList); // 차곡 차곡 쌓은 리스트를 한번에 저장 (약 400)

            saveCount+=1;
            System.out.println(saveCount + " save");
        }
        System.out.println("--- Complete Saving Of All Data In Queue ---");
    }


    //////////////////////////////////////////


    private boolean isFirst(long count, boolean uniqueCheck, String hexText) {
        if(count == 1) {
            System.out.println("--- hexText.length() : " + hexText.length() + " ---"); // 총 패킷 데이터의 길이
            System.out.println("✅" + count + "✅ 받아온 데이터 : "
                    + "\n🔘 IMS : " + hexText.substring(0,6)
                    + "\n🔘 Command : " + hexText.substring(6,8)
                    + "\n🔘 ACK : " + hexText.substring(8,10)
                    + "\n🔘 unique : " + hexText.substring(10,18)
                    + "\n🔘 channelNum : " + hexText.substring(18,20)
                    + "\n🔘 segment num : " + hexText.substring(20,24)
                    + "\n🔘 segment total : " + hexText.substring(24,28) + " Real Data Value : (" + Integer.parseUnsignedInt(hexText.substring(24,28), 16) + ")"
                    + "\n🔘 DATA COUNT : " + hexText.substring(28,32) + " Real Data Value : (" + Integer.parseUnsignedInt(hexText.substring(28,32), 16) + ")"
                    + "\n🔘 DATA : " + hexText.substring(32,3240));
            if(unique.equals(hexText.substring(10,18))) { // Client로 부터 전달 받은 측정고유번호가 Server에서 갖고있는 번호와 일치하는지 확인 후, 이 후의 진행을 위해 true로 변경됨
                uniqueCheck = true;
            } else {
                // 데이터가 일치하지 않는 경우의 Exception 추가 필요
            }
        }
        return uniqueCheck;
    }

    public Integer getMasterId() {
        TntMaster masterId = masterRepository.findTopByOrderByIdDesc().orElseThrow(TntMasterNotFoundException::new);
        return masterId.getId();
    }

    private byte[] getUdpProtocol(byte[] parseByte) {
        return new byte[] {
                0x49, 0x4d, 0x53, //IMS(3)
                0x06, // Command(1)
                0x00, // ACK(1)
                parseByte[0], parseByte[1], parseByte[2], parseByte[3] // 측정고유번호(4) // 해당 데이터를 제외한 나머지는 고정 값
                // 0xFF // checksum(1), 자동 설정
        };
    }

    private void toEntity(List<TntData> tntDataList, List<List<Long>> queueResponse, int count, Integer masterId) {
        /* TODO

        */
        while(count < queueResponse.get(0).size()) { // 첫번째 큐의 길이 만큼 반복 (약 400)
            TntData tntData = TntData.builder() // masterId와 실데이터값을 build
                    .masterId(masterId)
                    .data(queueResponse.get(0).get(count))
                    .build();
            tntDataList.add(tntData); // 리스트에 차곡 차곡 쌓음
            count++;
        }
    }

    public static void clear(Buffer buffer) {
        buffer.clear();
    }
}
